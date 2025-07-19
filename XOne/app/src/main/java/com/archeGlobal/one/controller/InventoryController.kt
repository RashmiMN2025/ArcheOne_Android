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
import com.archeGlobal.one.navigation.Navigator
import kotlinx.coroutines.launch

class InventoryController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {

    var model by mutableStateOf(InventoryModel())
        private set

    var addItemModel by mutableStateOf(AddItemModel())
        private set

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

    fun onAddItemNameChanged(name: String) {
        addItemModel = addItemModel.copy(itemName = name)
    }

    fun onAddItemNumberChanged(number: String) {
        addItemModel = addItemModel.copy(itemNumber = number)
    }

    fun onAddItemUnitChanged(unit: String) {
        addItemModel = addItemModel.copy(unit = unit)
    }

    fun onAddItemBrandChanged(brand: String) {
        addItemModel = addItemModel.copy(brand = brand)
    }

    fun onAddItemTotalStockChanged(stock: String) {
        addItemModel = addItemModel.copy(totalStock = stock)
    }

    fun onAddItemSubmit() {
        if (validateAddItemForm()) {
            viewModelScope.launch {
                addItemModel = addItemModel.copy(isLoading = true)
                try {
                    // Here you would typically call an API to add the item
                    // For now, we'll simulate adding the item to the local list
                    val newItem = InventoryItem(
                        id = generateItemId(),
                        name = addItemModel.itemName,
                        itemNumber = addItemModel.itemNumber,
                        totalStock = addItemModel.totalStock.toIntOrNull() ?: 0,
                        lastUpdated = getCurrentDateTime(),
                        iconName = "ic_file", // Default icon
                        category = addItemModel.selectedType
                    )

                    // Add item to the current list
                    val updatedItems = model.inventoryItems + newItem
                    model = model.copy(inventoryItems = updatedItems)

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

    private fun validateAddItemForm(): Boolean {
        return addItemModel.itemName.isNotBlank() &&
            addItemModel.itemNumber.isNotBlank() &&
            addItemModel.totalStock.isNotBlank() &&
            addItemModel.totalStock.toIntOrNull() != null
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
        val allItems = InventoryModel().inventoryItems
        val filteredItems = when (model.selectedType) {
            "All" -> allItems
            else -> allItems.filter { it.category == model.selectedType }
        }
        // For now, we're not filtering by location, but you could add that logic here
        model = model.copy(inventoryItems = filteredItems)
    }

    fun refreshInventory() {
        viewModelScope.launch {
            model = model.copy(isLoading = true)
            // Simulate API call
            kotlinx.coroutines.delay(1000)
            model = model.copy(isLoading = false)
        }
    }
}