package com.archeGlobal.one.controller

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.InventoryActivity
import com.archeGlobal.one.model.AddItemModel
import com.archeGlobal.one.model.InventoryItem
import com.archeGlobal.one.model.InventoryModel
import com.archeGlobal.one.model.toInventoryItem
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.launch

class InventoryController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {

    var model by mutableStateOf(InventoryModel())
        private set

    var addItemModel by mutableStateOf(AddItemModel())
        private set

    init {
        loadInventoryData()
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
        // Handle item click - could navigate to item detail/edit page
        // For now, just show a message or handle stock update
    }

    fun onAddItemClick() {
        addItemModel = addItemModel.copy(showDialog = true)
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

    fun onAddItemSelected(item: String) {
        addItemModel = addItemModel.copy(selectedItem = item)
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
        if (validateUpdateStockForm()) {
            viewModelScope.launch {
                addItemModel = addItemModel.copy(isLoading = true)
                try {
                    // Here you would typically call an API to update the stock
                    // For now, we'll simulate updating the item stock
                    val updatedItem = InventoryItem(
                        id = generateItemId(),
                        name = addItemModel.selectedItem,
                        itemNumber = addItemModel.selectedItem,
                        unit = addItemModel.unit,
                        closingStock = addItemModel.newStockQuantity.toDoubleOrNull() ?: 0.0,
                        updatedBy = addItemModel.updatedBy,
                        suppliedDate = "${addItemModel.stockSuppliedDate} at ${addItemModel.stockSuppliedTime}",
                        lastUpdated = getCurrentDateTime(),
                        iconName = "ic_file", // Default icon
                        category = addItemModel.selectedType,
                        location = addItemModel.selectedLocation,
                        brand = addItemModel.brand
                    )

                    // Add item to both the current list and all items list
                    val updatedItems = model.inventoryItems + updatedItem
                    val updatedAllItems = model.allItems + updatedItem
                    model = model.copy(inventoryItems = updatedItems, allItems = updatedAllItems)

                    // Close dialog and reset form
                    addItemModel = addItemModel.copy(showDialog = false, isLoading = false)
                    resetAddItemForm()
                } catch (e: Exception) {
                    addItemModel = addItemModel.copy(isLoading = false)
                    // Handle error - could show toast or error message
                }
            }
        }
    }

    private fun validateUpdateStockForm(): Boolean {
        return addItemModel.selectedItem.isNotBlank() &&
            addItemModel.newStockQuantity.isNotBlank() &&
            addItemModel.newStockQuantity.toDoubleOrNull() != null &&
            addItemModel.updatedBy.isNotBlank()
    }

    private fun resetAddItemForm() {
        addItemModel = AddItemModel()
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
