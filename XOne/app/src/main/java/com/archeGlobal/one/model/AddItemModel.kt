package com.archeGlobal.one.model

data class AddItemModel(
    val showDialog: Boolean = false,
    val selectedLocation: String = "Bengaluru",
    val selectedType: String = "HK_Consumables",
    val selectedItem: String = "_S1",
    val existingStock: String = "22.0",
    val newStockQuantity: String = "22.0",
    val updatedBy: String = "Biswa",
    val brand: String = "Schevaran",
    val unit: String = "Litres",
    val stockSuppliedDate: String = "8 Sep 2025",
    val stockSuppliedTime: String = "8:00 AM",
    val locations: List<String> = listOf("Bengaluru", "Chennai", "Coimbatore"),
    val types: List<String> = listOf("HK_Consumables", "Stationary", "Writing", "Paper", "Office Supplies", "Hygiene"),
    val items: List<String> = listOf("_S1", "_S2", "_S3", "_S4", "_S5"),
    val isLoading: Boolean = false
)
