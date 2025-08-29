package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.DeskCartModel
import com.archeGlobal.one.model.EmployeeDetails
import com.archeGlobal.one.model.StationaryItem
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.DeskCartEligibilityRequest
import com.archeGlobal.one.network.DeskCartPlaceOrderRequest
import com.archeGlobal.one.network.DeskCartOrderItem
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.FileDownloadHelper
import com.archeGlobal.one.utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class DeskCartController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {
    var model by mutableStateOf(DeskCartModel(isInitialLoading = true))
        private set

    private val userDataManager = UserDataManager.getInstance(context)
    private val fileDownloadHelper = FileDownloadHelper(context)

    init {
        loadEligibilityData()
    }

    private fun loadEligibilityData() {
        val userData = userDataManager.getUserData()
        val email = userData?.email ?: "biswajit.d@arche.global"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DeskCartController", "Fetching eligibility data for email: $email")
                val response = RetrofitClient.apiService.getDeskCartEligibility(
                    DeskCartEligibilityRequest(email = email)
                )

                if (response.isSuccessful && response.body() != null) {
                    val eligibilityResponse = response.body()!!
                    
                    // Process data on background thread
                    val stationaryItems = eligibilityResponse.order.map { apiItem ->
                        StationaryItem(
                            id = apiItem.materialId,
                            name = apiItem.name,
                            iconName = getIconNameFromItem(apiItem.name),
                            imageUrl = apiItem.imageUrl,
                            currentQuantity = 0,
                            maxQuantity = apiItem.limit
                        )
                    }

                    val employeeDetails = EmployeeDetails(
                        emailId = userData?.email ?: email,
                        employeeId = userData?.employeeId ?: "NT1426",
                        department = userData?.department ?: "Project Department"
                    )

                    // Only update UI state on main thread
                    withContext(Dispatchers.Main) {
                        model = model.copy(
                            employeeDetails = employeeDetails,
                            stationaryItems = stationaryItems,
                            isAdmin = eligibilityResponse.isAdmin,
                            isLoading = false,
                            isInitialLoading = false
                        )

                        Log.d("DeskCartController", "Eligibility data loaded: ${stationaryItems.size} items, isAdmin: ${eligibilityResponse.isAdmin}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        handleError("Failed to load eligibility data: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun handleError(message: String) {
        Log.e("DeskCartController", message)
        model = model.copy(
            isLoading = false,
            isInitialLoading = false,
            error = message
        )
    }

    // Map API item names to existing icon names
    private fun getIconNameFromItem(itemName: String): String {
        return when {
            itemName.contains("Pen", ignoreCase = true) -> "ic_pen"
            itemName.contains("Pencil", ignoreCase = true) -> "ic_pencil"
            itemName.contains("Notepad", ignoreCase = true) -> "ic_notepad"
            itemName.contains("Eraser", ignoreCase = true) -> "ic_eraser"
            else -> "ic_pen" // Default icon
        }
    }

    fun onBackPressed() {
        Log.d("DeskCartController", "Back pressed - navigating to Home")
        navigator.navigateToHome()
    }

    fun onAdminDashboardClick() {
        Log.d("DeskCartController", "Admin Dashboard clicked")
        navigator.navigateToAdminDashboard()
    }

    fun onIncreaseQuantity(item: StationaryItem) {
        if (item.currentQuantity < item.maxQuantity) {
            val updatedItems = model.stationaryItems.toMutableList()
            val index = updatedItems.indexOfFirst { it.id == item.id }
            if (index != -1) {
                updatedItems[index] = updatedItems[index].copy(currentQuantity = updatedItems[index].currentQuantity + 1)
                model = model.copy(stationaryItems = updatedItems)
                Log.d("DeskCartController", "Increased ${item.name} quantity to ${updatedItems[index].currentQuantity}")
            }
        }
    }

    fun onDecreaseQuantity(item: StationaryItem) {
        if (item.currentQuantity > 0) {
            val updatedItems = model.stationaryItems.toMutableList()
            val index = updatedItems.indexOfFirst { it.id == item.id }
            if (index != -1) {
                updatedItems[index] = updatedItems[index].copy(currentQuantity = updatedItems[index].currentQuantity - 1)
                model = model.copy(stationaryItems = updatedItems)
                Log.d("DeskCartController", "Decreased ${item.name} quantity to ${updatedItems[index].currentQuantity}")
            }
        }
    }

    fun onPlaceOrder() {
        val selectedItems = model.stationaryItems.filter { it.currentQuantity > 0 }

        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Please select at least one item to place order", Toast.LENGTH_SHORT).show()
            return
        }

        model = model.copy(isLoading = true)

        val userData = userDataManager.getUserData()
        val email = userData?.email ?: model.employeeDetails.emailId
        val employeeId = userData?.employeeId ?: model.employeeDetails.employeeId
        val employeeName = userData?.name ?: "User"
        val department = userData?.department ?: model.employeeDetails.department
        val location = userData?.location ?: "Office"

        // Create order items
        val orderItems = selectedItems.map { item ->
            DeskCartOrderItem(
                materialId = item.id,
                count = item.currentQuantity
            )
        }

        // Create place order request
        val placeOrderRequest = DeskCartPlaceOrderRequest(
            email = email,
            employeeId = employeeId,
            employeeName = employeeName,
            department = department,
            location = location,
            items = orderItems
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DeskCartController", "Placing order for ${selectedItems.size} items")
                val response = RetrofitClient.apiService.placeDeskCartOrder(placeOrderRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val orderResponse = response.body()!!
                        
                        // Reset quantities after successful order
                        val resetItems = model.stationaryItems.map {
                            it.copy(currentQuantity = 0)
                        }

                        model = model.copy(
                            stationaryItems = resetItems,
                            isLoading = false,
                            orderPlaced = true
                        )

                        // Show success message with order ID
                        val orderId = orderResponse.orders.firstOrNull()?.orderId ?: "N/A"
                        Toast.makeText(
                            context,
                            "Order placed successfully with ID: $orderId",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d("DeskCartController", "Order placed successfully: ${orderResponse.message}")
                        
                        // Navigate to order history activity after successful order
                        Log.d("DeskCartController", "Navigating to Order History Activity after successful order placement")
                        navigator.navigateToOrderHistoryActivity()
                    } else {
                        handleOrderError("Failed to place order: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleOrderError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun handleOrderError(message: String) {
        Log.e("DeskCartController", message)
        model = model.copy(
            isLoading = false,
            error = message
        )
        Toast.makeText(context, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
    }

    fun getTotalItemsSelected(): Int {
        return model.stationaryItems.sumOf { it.currentQuantity }
    }

    fun clearError() {
        model = model.copy(error = null)
    }

    fun onHistoryClick() {
        Log.d("DeskCartController", "History button clicked - navigating to OrderHistoryActivity (like admin dashboard)")
        navigator.navigateToOrderHistoryActivity()
    }

    fun downloadStockReport(category: String = "All", location: String? = null) {
        if (!PermissionHelper.hasStoragePermission(context)) {
            model = model.copy(downloadError = "Storage permission required to download files")
            return
        }

        val userLocation = location ?: userDataManager.getUserData()?.location ?: "Office"
        downloadReport(category, userLocation, isUsage = false)
    }

    fun downloadUsageReport(category: String = "All", location: String? = null) {
        if (!PermissionHelper.hasStoragePermission(context)) {
            model = model.copy(downloadError = "Storage permission required to download files")
            return
        }

        val userLocation = location ?: userDataManager.getUserData()?.location ?: "Office"
        downloadReport(category, userLocation, isUsage = true)
    }

    private fun downloadReport(category: String, location: String, isUsage: Boolean) {
        // Update loading state
        model = if (isUsage) {
            model.copy(isDownloadingUsage = true, downloadError = null)
        } else {
            model.copy(isDownloadingStock = true, downloadError = null)
        }

        val viewType = if (isUsage) "usage" else "stock"
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // URL encode parameters
                val encodedCategory = URLEncoder.encode(category, "UTF-8")
                val encodedLocation = URLEncoder.encode(location, "UTF-8")
                
                // Construct download URL matching iOS implementation
                val downloadUrl = "https://dev.arche.global/deskcart/stocklist/csv?category=$encodedCategory&location=$encodedLocation&view=$viewType"
                
                Log.d("DeskCartController", "Downloading report from: $downloadUrl")
                
                val response = RetrofitClient.apiService.downloadReport(downloadUrl)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        
                        // Save file using FileDownloadHelper
                        val result = fileDownloadHelper.saveCSVFile(responseBody, category, location, isUsage)
                        
                        if (result.success) {
                            model = model.copy(
                                isDownloadingStock = false,
                                isDownloadingUsage = false,
                                lastDownloadedFile = result.filePath,
                                downloadError = null
                            )
                            
                            val reportType = if (isUsage) "Monthly Usage Report" else "Stock Report"
                            Toast.makeText(
                                context,
                                "$reportType downloaded successfully to Downloads folder",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            Log.d("DeskCartController", "File downloaded successfully: ${result.filePath}")
                        } else {
                            handleDownloadError(result.errorMessage ?: "Failed to save file")
                        }
                    } else {
                        handleDownloadError("Failed to download report: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleDownloadError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun handleDownloadError(message: String) {
        Log.e("DeskCartController", "Download error: $message")
        model = model.copy(
            isDownloadingStock = false,
            isDownloadingUsage = false,
            downloadError = message
        )
        Toast.makeText(context, "Download failed: $message", Toast.LENGTH_SHORT).show()
    }

    fun openDownloadedFile() {
        model.lastDownloadedFile?.let { filePath ->
            fileDownloadHelper.openFileLocation(filePath)
        }
    }

    fun shareDownloadedFile() {
        model.lastDownloadedFile?.let { filePath ->
            fileDownloadHelper.shareFile(filePath)
        }
    }

    fun clearDownloadError() {
        model = model.copy(downloadError = null)
    }
}
