package com.archeGlobal.one.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.InventoryActivity
import com.archeGlobal.one.model.AddItemModel
import com.archeGlobal.one.model.DialogMode
import com.archeGlobal.one.model.InventoryItem
import com.archeGlobal.one.model.InventoryModel
import com.archeGlobal.one.model.toInventoryItem
import com.archeGlobal.one.model.AddInventoryItemRequest
import com.archeGlobal.one.model.UpdateInventoryItemRequest
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.launch

class InventoryController(
    private val context: Context,
    private val navigator: Navigator,
    private val sourceScreen: String? = null
) : ViewModel() {

    private val userDataManager = UserDataManager.getInstance(context)

    companion object {
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        private var cachedInventoryItems: List<InventoryItem>? = null
        private var cacheTimestamp: Long = 0L
        
        fun setCachedData(inventoryItems: List<InventoryItem>) {
            cachedInventoryItems = inventoryItems
            cacheTimestamp = System.currentTimeMillis()
            android.util.Log.d("InventoryController", "Cached ${inventoryItems.size} inventory items")
        }
        
        private fun isCacheValid(): Boolean {
            return cachedInventoryItems != null && 
                   (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
        }
    }

    var model by mutableStateOf(InventoryModel())
        private set

    var addItemModel by mutableStateOf(AddItemModel())
        private set

    init {
        loadInventoryDataIfNeeded()
    }
    
    private fun loadInventoryDataIfNeeded() {
        // Only call API when coming from AdminDashboard or if cache is invalid
        if (sourceScreen == "AdminDashboard" || !isCacheValid()) {
            android.util.Log.d("InventoryController", "Loading fresh data from API (source: $sourceScreen, cache valid: ${isCacheValid()})")
            loadInventoryData()
        } else {
            android.util.Log.d("InventoryController", "Using cached data (source: $sourceScreen)")
            cachedInventoryItems?.let { inventoryItems ->
                val locations = inventoryItems.map { it.location }.distinct().sorted()
                val categories = inventoryItems.map { it.category }.distinct().sorted()
                val types = listOf("All") + categories
                
                model = model.copy(
                    inventoryItems = inventoryItems,
                    allItems = inventoryItems,
                    locations = locations,
                    types = types,
                    isLoading = false,
                    errorMessage = null
                )
                filterItems()
            }
        }
    }

    fun onBackPressed() {
        if (context is InventoryActivity) {
            context.finishWithAnimation()
        } else {
            navigator.navigateToHome()
        }
    }

    fun onLocationSelected(location: String) {
        model = model.copy(selectedLocation = location)
        filterItems()
    }

    fun onTypeSelected(type: String) {
        model = model.copy(selectedType = type)
        filterItems()
    }

    fun onSearchQueryChanged(query: String) {
        model = model.copy(searchQuery = query)
        filterItems()
    }

    fun onItemClick(item: InventoryItem) {
        // Open dialog in UPDATE mode with pre-filled data
        addItemModel = addItemModel.copy(
            showDialog = true,
            mode = DialogMode.UPDATE,
            selectedLocation = item.location.ifEmpty { "Bengaluru" },
            selectedType = item.category.ifEmpty { "HK_Consumables" },
            selectedItem = item.name,
            addItem = item.id,
            existingStock = item.totalStock.toString(),
            usedStockQuantity = "",
            brand = item.brand.ifEmpty { "Schevaran" },
            unit = item.unit,
            updatedBy = userDataManager.getUserData()?.name ?: "",
            quantityUpdateType = "Update Used Quantity" // Default to most common use case
        )
    }

    fun onAddItemClick() {
        addItemModel = addItemModel.copy(
            showDialog = true,
            mode = DialogMode.ADD,
            updatedBy = userDataManager.getUserData()?.name ?: ""
        )
    }

    fun onAddItemDismiss() {
        addItemModel = addItemModel.copy(showDialog = false)
        resetAddItemForm()
    }

    fun onAddItemLocationSelected(location: String) {
        addItemModel = addItemModel.copy(selectedLocation = location)
    }

    fun onAddItemTypeSelected(type: String) {
        addItemModel = addItemModel.copy(selectedType = type)
    }

    fun onAddItemAccessTypeSelected(accessType: String) {
        addItemModel = addItemModel.copy(selectedAccessType = accessType)
    }

    fun onAddItemSelected(item: String) {
        addItemModel = addItemModel.copy(selectedItem = item)
    }

    fun onAddItemQuantityUpdateTypeSelected(quantityUpdateType: String) {
        addItemModel = addItemModel.copy(quantityUpdateType = quantityUpdateType)
    }

    fun onAddItemUsedStockQuantityChanged(quantity: String) {
        addItemModel = addItemModel.copy(usedStockQuantity = quantity)
    }

    fun onAddItemExistingStockChanged(stock: String) {
        addItemModel = addItemModel.copy(existingStock = stock)
    }

    fun onAddItemNewStockQuantityChanged(quantity: String) {
        addItemModel = addItemModel.copy(newStockQuantity = quantity)
    }

    fun onAddItemUpdatedByChanged(updatedBy: String) {
        addItemModel = addItemModel.copy(updatedBy = updatedBy)
    }

    fun onAddItemBrandChanged(brand: String) {
        addItemModel = addItemModel.copy(brand = brand)
    }

    fun onAddItemUnitChanged(unit: String) {
        addItemModel = addItemModel.copy(unit = unit)
    }

    fun onAddItemStockSuppliedDateChanged(date: String) {
        addItemModel = addItemModel.copy(stockSuppliedDate = date)
    }

    fun onAddItemStockSuppliedTimeChanged(time: String) {
        addItemModel = addItemModel.copy(stockSuppliedTime = time)
    }

    fun onUpdateStock() {
        when (addItemModel.mode) {
            DialogMode.ADD -> addNewInventoryItem()
            DialogMode.UPDATE -> updateExistingInventoryItem()
        }
    }

    private fun addNewInventoryItem() {
        if (validateAddItemForm()) {
            viewModelScope.launch {
                addItemModel = addItemModel.copy(isLoading = true)
                try {
                    val request = AddInventoryItemRequest(
                        updatedBy = addItemModel.updatedBy,
                        itemCategory = addItemModel.selectedType, // Keep original case
                        view = addItemModel.selectedAccessType, // Keep original case
                        itemName = addItemModel.selectedItem,
                        unit = addItemModel.unit,
                        brand = addItemModel.brand,
                        openingStock = addItemModel.existingStock,
                        location = addItemModel.selectedLocation,
                        suppliedDate = getCurrentDate() // Use current date as default
                    )

                    val response = RetrofitClient.apiService.addInventoryItem(request)
                    if (response.isSuccessful && response.body()?.status == 200) {
                        // Success - refresh inventory list
                        loadInventoryData()
                        addItemModel = addItemModel.copy(showDialog = false, isLoading = false)
                        Toast.makeText(context, "The item ${addItemModel.selectedItem} has been added to the inventory.", Toast.LENGTH_LONG).show()
                        resetAddItemForm()
                    } else {
                        addItemModel = addItemModel.copy(isLoading = false)
                        // Handle API error
                    }
                } catch (e: Exception) {
                    addItemModel = addItemModel.copy(isLoading = false)
                    // Handle network error
                }
            }
        }
    }

    private fun updateExistingInventoryItem() {
        if (validateUpdateItemForm()) {
            viewModelScope.launch {
                addItemModel = addItemModel.copy(isLoading = true)
                try {
                    // Calculate itemCount based on quantity update type
                    val baseQuantity = addItemModel.newStockQuantity.toIntOrNull() ?: 0
                    
                    // Debug logging
                    android.util.Log.d("InventoryController", "=== UPDATE DEBUG INFO ===")
                    android.util.Log.d("InventoryController", "selectedItem: '${addItemModel.selectedItem}'")
                    android.util.Log.d("InventoryController", "addItem (itemId): '${addItemModel.addItem}'")
                    android.util.Log.d("InventoryController", "quantityUpdateType: '${addItemModel.quantityUpdateType}'")
                    android.util.Log.d("InventoryController", "newStockQuantity: '${addItemModel.newStockQuantity}'")
                    android.util.Log.d("InventoryController", "baseQuantity: $baseQuantity")
                    
                    val isUsedQuantityUpdate = addItemModel.quantityUpdateType == "Update Used Quantity"
                    val itemCount = baseQuantity // Always send positive numbers
                    
                    android.util.Log.d("InventoryController", "Final itemCount: $itemCount")
                    android.util.Log.d("InventoryController", "updateUtilization: $isUsedQuantityUpdate")
                    android.util.Log.d("InventoryController", "=========================")
                    
                    val request = UpdateInventoryItemRequest(
                        updatedBy = addItemModel.updatedBy,
                        itemName = addItemModel.selectedItem,
                        itemId = addItemModel.addItem,
                        itemCount = itemCount,
                        brand = addItemModel.brand,
                        unit = addItemModel.unit,
                        suppliedDate = getCurrentDate(),
                        updateUtilization = isUsedQuantityUpdate, // true for used quantity, false for new quantity
                        location = addItemModel.selectedLocation
                    )

                    android.util.Log.d("InventoryController", "Sending API request...")
                    val response = RetrofitClient.apiService.updateInventoryItem(request)
                    android.util.Log.d("InventoryController", "API Response Code: ${response.code()}")
                    android.util.Log.d("InventoryController", "API Response Body: ${response.body()}")
                    
                    if (response.isSuccessful && response.body()?.status == 200) {
                        // Get the actual total stock from API response
                        val updatedTotalStock = response.body()?.data?.totalStock ?: "0"
                        
                        // Success - refresh inventory list
                        loadInventoryData()
                        addItemModel = addItemModel.copy(showDialog = false, isLoading = false)
                        Toast.makeText(context, "The stock for ${addItemModel.selectedItem} item has been updated to $updatedTotalStock.", Toast.LENGTH_LONG).show()
                        resetAddItemForm()
                    } else {
                        android.util.Log.e("InventoryController", "API Error - Response Code: ${response.code()}, Status: ${response.body()?.status}")
                        addItemModel = addItemModel.copy(isLoading = false)
                        Toast.makeText(context, "Failed to update inventory: ${response.body()?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("InventoryController", "Network Error: ${e.message}", e)
                    addItemModel = addItemModel.copy(isLoading = false)
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateAddItemForm(): Boolean {
    if (addItemModel.selectedItem.isBlank()) {
        Toast.makeText(context, "Please enter item name", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.unit.isBlank()) {
        Toast.makeText(context, "Please enter unit", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.existingStock.isBlank()) {
        Toast.makeText(context, "Please enter opening stock", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.existingStock.toIntOrNull() == null) {
        Toast.makeText(context, "Please enter a valid number for opening stock", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.updatedBy.isBlank()) {
        Toast.makeText(context, "Please enter your name in 'Added By' field", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.brand.isBlank()) {
        Toast.makeText(context, "Please enter brand name", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

    private fun validateUpdateItemForm(): Boolean {
    if (addItemModel.newStockQuantity.isBlank()) {
        Toast.makeText(context, "Please enter new stock quantity", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.newStockQuantity.toIntOrNull() == null) {
        Toast.makeText(context, "Please enter a valid number for new stock quantity", Toast.LENGTH_SHORT).show()
        return false
    }
    if (addItemModel.updatedBy.isBlank()) {
        Toast.makeText(context, "Please enter your name in 'Updated By' field", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

    private fun resetAddItemForm() {
        addItemModel = AddItemModel()
    }

    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun generateItemId(): String {
        return "item_${System.currentTimeMillis()}"
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = java.text.SimpleDateFormat("d MMM yyyy 'at' h:mm a", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    private fun filterItems() {
        var filteredItems = model.allItems

        // Filter by search query
        if (model.searchQuery.isNotBlank()) {
            filteredItems = filteredItems.filter { item ->
                item.name.contains(model.searchQuery, ignoreCase = true) ||
                    item.itemNumber.contains(model.searchQuery, ignoreCase = true)
            }
        }

        // Filter by type
        if (model.selectedType != "All") {
            filteredItems = filteredItems.filter { it.category == model.selectedType }
        }

        // Filter by location
        if (model.selectedLocation.isNotBlank()) {
            filteredItems = filteredItems.filter { it.location == model.selectedLocation }
        }

        model = model.copy(inventoryItems = filteredItems)
    }

    private fun loadInventoryData() {
        viewModelScope.launch {
            model = model.copy(isLoading = true)
            try {
                val response = RetrofitClient.apiService.getStockList()
                if (response.isSuccessful) {
                    val stockListResponse = response.body()
                    if (stockListResponse?.status == 200) {
                        val inventoryItems = stockListResponse.data.map { it.toInventoryItem() }
                        
                        // Cache the data
                        setCachedData(inventoryItems)
                        
                        // Extract unique locations and categories from API data
                        val locations = inventoryItems.map { it.location }.distinct().sorted()
                        val categories = inventoryItems.map { it.category }.distinct().sorted()
                        val types = listOf("All") + categories
                        
                        model = model.copy(
                            inventoryItems = inventoryItems,
                            allItems = inventoryItems,
                            locations = locations,
                            types = types,
                            isLoading = false,
                            errorMessage = null
                        )
                        
                        // Apply initial filter
                        filterItems()
                        android.util.Log.d("InventoryController", "Inventory data loaded and cached successfully: ${inventoryItems.size} items")
                    } else {
                        model = model.copy(
                            isLoading = false,
                            errorMessage = "Failed to load inventory data"
                        )
                    }
                } else {
                    model = model.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                model = model.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun refreshInventory() {
        loadInventoryData()
    }
}