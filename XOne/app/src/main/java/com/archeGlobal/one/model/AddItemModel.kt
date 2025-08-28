package com.archeGlobal.one.model

import java.text.SimpleDateFormat
import java.util.*

private fun getCurrentDate(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}

data class AddItemModel(
    val showDialog: Boolean = false,
    val mode: DialogMode = DialogMode.ADD,
    val selectedLocation: String = "Bengaluru",
    val selectedType: String = "Stationery",
    val selectedAccessType: String = "Admin",
    val selectedItem: String = "",
    val itemId: String = "",
    val addItem: String = "",
    val quantityUpdateType: String = "Update New Quantity",
    val existingStock: String = "",
    val usedStockQuantity: String = "",
    val newStockQuantity: String = "",
    val updatedBy: String = "",
    val brand: String = "",
    val unit: String = "",
    val stockSuppliedDate: String = getCurrentDate(),
    val stockSuppliedTime: String = "8:00 AM",
    val locations: List<String> = listOf("Bengaluru", "Chennai", "Coimbatore"),
    val types: List<String> = listOf("Stationery", "HK_Consumables", "Party_Essentials"),
    val quantityUpdateTypes: List<String> = listOf("Update New Quantity", "Update Used Quantity"),
    val items: List<String> = listOf("_S1", "_S2", "_S3", "_S4", "_S5"),
    val isLoading: Boolean = false
)

enum class DialogMode {
    ADD, UPDATE
}
