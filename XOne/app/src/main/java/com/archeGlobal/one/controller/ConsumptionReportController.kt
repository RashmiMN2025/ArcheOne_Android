package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.ConsumptionReportModel
import com.archeGlobal.one.model.ConsumptionTab
import com.archeGlobal.one.model.toConsumptionStockCategories
import com.archeGlobal.one.model.toUsageCategories
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.FileDownloadHelper
import com.archeGlobal.one.utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class ConsumptionReportController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {
    var model by mutableStateOf(ConsumptionReportModel())
        private set
    
    private val fileDownloadHelper = FileDownloadHelper(context)

    companion object {
        private var cachedStockData: List<com.archeGlobal.one.model.StockItem>? = null
        private var lastLoadTime: Long = 0
        private const val CACHE_DURATION = 5 * 60 * 1000L // 5 minutes cache
    }

    init {
        loadConsumptionDataIfNeeded()
    }

    private fun loadConsumptionDataIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val isCacheValid = cachedStockData != null && 
                          (currentTime - lastLoadTime) < CACHE_DURATION

        if (isCacheValid) {
            // Use cached data
            processStockData(cachedStockData!!)
            Log.d("ConsumptionReportController", "Using cached stock data")
        } else {
            // Load fresh data
            loadConsumptionData()
        }
    }

    private fun processStockData(stockData: List<com.archeGlobal.one.model.StockItem>) {
        val filteredData = if (model.selectedLocation.isNotBlank()) {
            stockData.filter { it.location == model.selectedLocation }
        } else {
            stockData
        }
        
        val stockCategories = filteredData.toConsumptionStockCategories()
        val usageCategories = filteredData.toUsageCategories()
        
        model = model.copy(
            stockCategories = stockCategories,
            usageCategories = usageCategories,
            isLoading = false,
            error = null
        )
    }

    private fun loadConsumptionData() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                model = model.copy(isLoading = true)
            }
            try {
                val response = RetrofitClient.apiService.getStockList()
                if (response.isSuccessful) {
                    val stockListResponse = response.body()
                    if (stockListResponse?.status == 200) {
                        val stockData = stockListResponse.data
                        
                        // Cache the results
                        cachedStockData = stockData
                        lastLoadTime = System.currentTimeMillis()
                        
                        // Process and update UI
                        withContext(Dispatchers.Main) {
                            processStockData(stockData)
                        }
                        Log.d("ConsumptionReportController", "Consumption data loaded successfully")
                    } else {
                        withContext(Dispatchers.Main) {
                            model = model.copy(
                                isLoading = false,
                                error = "Failed to load consumption data"
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        model = model.copy(
                            isLoading = false,
                            error = "Network error: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ConsumptionReportController", "Error loading consumption data", e)
                withContext(Dispatchers.Main) {
                    model = model.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun onBackPressed() {
        Log.d("ConsumptionReportController", "Back pressed - navigating to Admin Dashboard")
        navigator.navigateToAdminDashboard()
    }

    fun onTabSelected(tab: ConsumptionTab) {
        Log.d("ConsumptionReportController", "Tab selected: $tab")
        model = model.copy(selectedTab = tab)
    }

    fun onLocationSelected(location: String) {
        Log.d("ConsumptionReportController", "Location selected: $location")
        model = model.copy(selectedLocation = location)
        // TODO: Load data for the selected location
        loadDataForLocation(location)
    }

    fun onDownloadReport(categoryId: String) {
        if (!PermissionHelper.hasStoragePermission(context)) {
            Toast.makeText(context, "Storage permission required to download files", Toast.LENGTH_SHORT).show()
            return
        }

        val isUsage = model.selectedTab == ConsumptionTab.USAGE
        val location = model.selectedLocation.ifBlank { "Office" }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // URL encode parameters
                val encodedCategory = URLEncoder.encode(categoryId, "UTF-8")
                val encodedLocation = URLEncoder.encode(location, "UTF-8")
                val viewType = if (isUsage) "usage" else "stock"
                
                // Construct download URL matching iOS implementation
                val downloadUrl = "https://dev.arche.global/deskcart/stocklist/csv?category=$encodedCategory&location=$encodedLocation&view=$viewType"
                
                Log.d("ConsumptionReportController", "Downloading report from: $downloadUrl")
                
                val response = RetrofitClient.apiService.downloadReport(downloadUrl)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        
                        // Save file using FileDownloadHelper
                        val result = fileDownloadHelper.saveCSVFile(responseBody, categoryId, location, isUsage)
                        
                        if (result.success) {
                            val reportType = if (isUsage) "Monthly Usage Report" else "Stock Report"
                            Toast.makeText(
                                context,
                                "$reportType for $categoryId downloaded successfully to Downloads folder",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            Log.d("ConsumptionReportController", "File downloaded successfully: ${result.filePath}")
                        } else {
                            Toast.makeText(
                                context,
                                "Download failed: ${result.errorMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ConsumptionReportController", "Download error: ${result.errorMessage}")
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to download report: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("ConsumptionReportController", "API error: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ConsumptionReportController", "Network error downloading report", e)
                }
            }
        }
    }

    private fun loadDataForLocation(location: String) {
        // Reload all data with the new location filter
        loadConsumptionData()
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}
