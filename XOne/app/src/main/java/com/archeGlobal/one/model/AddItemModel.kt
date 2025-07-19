package com.archeGlobal.one.model

data class AddItemModel(
    val showDialog: Boolean = false,
    val selectedLocation: String = "Bengaluru",
    val selectedType: String = "Stationary",
    val itemName: String = "",
    val itemNumber: String = "",
    val unit: String = "NA",
    val brand: String = "NA",
    val totalStock: String = "",
    val locations: List<String> = listOf("Bengaluru", "Chennai", "Coimbatore"),
    val types: List<String> = listOf("Stationary", "Writing", "Paper", "Office Supplies", "Hygiene"),
    val isLoading: Boolean = false
)
