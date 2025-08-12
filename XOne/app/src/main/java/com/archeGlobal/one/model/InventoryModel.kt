package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class InventoryModel(
    val isLoading: Boolean = false,
    val selectedLocation: String = "Bengaluru",
    val selectedType: String = "All",
    val searchQuery: String = "",
    val locations: List<String> = listOf("Bengaluru", "Chennai", "Coimbatore"),
    val types: List<String> = listOf("All", "Writing", "Paper", "Office Supplies", "Hygiene"),
    val inventoryItems: List<InventoryItem> = emptyList(),
    val allItems: List<InventoryItem> = emptyList(),
    val errorMessage: String? = null
)

data class StockListResponse(
    val status: Int,
    val data: List<StockItem>
)

data class StockItem(
    @SerializedName("Sl_No") val slNo: Int,
    @SerializedName("Item/Material_Category") val category: String,
    @SerializedName("Item/Material_ID") val itemId: String,
    @SerializedName("Item/Material_Name") val itemName: String,
    @SerializedName("Unit") val unit: String,
    @SerializedName("Brand") val brand: String,
    @SerializedName("Opening_Stock") val openingStock: String,
    @SerializedName("New_Stock") val newStock: String,
    @SerializedName("Total_Stock") val totalStock: String,
    @SerializedName("Consumption") val consumption: String,
    @SerializedName("Closing_Stock") val closingStock: String,
    @SerializedName("Location") val location: String,
    @SerializedName("Updated_By_") val updatedBy: String,
    @SerializedName("Last_Updated_Date_Time") val lastUpdatedDateTime: String,
    @SerializedName("stock_supplied_date") val stockSuppliedDate: String,
    @SerializedName("Utilization") val utilization: String
)

data class InventoryItem(
    val id: String,
    val name: String,
    val itemNumber: String,
    val unit: String,
    val closingStock: Double,
    val updatedBy: String,
    val suppliedDate: String,
    val lastUpdated: String,
    val iconName: String,
    val category: String,
    val location: String,
    val brand: String,
    val totalStock: Int = closingStock.toInt() // For backward compatibility
)

// Extension function to convert StockItem to InventoryItem
fun StockItem.toInventoryItem(): InventoryItem {
    return InventoryItem(
        id = itemId,
        name = itemName,
        itemNumber = itemId,
        unit = unit,
        closingStock = closingStock.toDoubleOrNull() ?: 0.0,
        updatedBy = updatedBy,
        suppliedDate = formatDateTime(stockSuppliedDate),
        lastUpdated = formatDateTime(lastUpdatedDateTime),
        iconName = getIconFromCategory(category),
        category = category,
        location = location,
        brand = brand
    )
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        // Parse ISO date and format to readable format
        val instant = java.time.Instant.parse(dateTimeString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy 'at' hh:mm a")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateTimeString // Return original if parsing fails
    }
}

private fun getIconFromCategory(category: String): String {
    return when (category.lowercase()) {
        "hk_consumables" -> "ic_handwash"
        "writing" -> "ic_pen"
        "paper" -> "ic_notepad"
        "office supplies" -> "ic_stapler"
        "hygiene" -> "ic_tissues"
        else -> "ic_file"
    }
}

