package com.archeGlobal.one.model

data class ConsumptionReportModel(
    val selectedTab: ConsumptionTab = ConsumptionTab.STOCK,
    val selectedLocation: String = "Bengaluru",
    val locations: List<String> = emptyList(),
    val stockCategories: List<ConsumptionStockCategory> = emptyList(),
    val usageCategories: List<UsageCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ConsumptionTab {
    STOCK, USAGE
}

data class ConsumptionStockCategory(
    val id: String,
    val title: String,
    val items: List<ConsumptionStockItem>
)

data class ConsumptionStockItem(
    val name: String,
    val quantity: Double,
    val color: String = "#4FC3F7" // Default blue color
)

data class UsageCategory(
    val id: String,
    val title: String,
    val items: List<UsageItem>
)

data class UsageItem(
    val name: String,
    val quantity: Double,
    val color: String = "#F44336" // Default red color
)

// Extension functions to convert API data to consumption report data
fun List<StockItem>.toConsumptionStockCategories(): List<ConsumptionStockCategory> {
    val groupedByCategory = this.groupBy { it.category }
    
    val categories = mutableListOf<ConsumptionStockCategory>()
    
    // Add "All Stock" category with all items
    if (this.isNotEmpty()) {
        categories.add(
            ConsumptionStockCategory(
                id = "all_stock",
                title = "All Stock",
                items = this.map { stockItem ->
                    ConsumptionStockItem(
                        name = stockItem.itemName,
                        quantity = stockItem.totalStock.toDoubleOrNull() ?: 0.0,
                        color = getStockColor(stockItem.totalStock.toDoubleOrNull()?.toInt() ?: 0)
                    )
                }
            )
        )
    }
    
    // Add categories by type
    groupedByCategory.forEach { (category, items) ->
        categories.add(
            ConsumptionStockCategory(
                id = category.lowercase().replace(" ", "_"),
                title = "$category Stock",
                items = items.map { stockItem ->
                    ConsumptionStockItem(
                        name = stockItem.itemName,
                        quantity = stockItem.totalStock.toDoubleOrNull() ?: 0.0,
                        color = getStockColor(stockItem.totalStock.toDoubleOrNull()?.toInt() ?: 0)
                    )
                }
            )
        )
    }
    
    return categories
}

fun List<StockItem>.toUsageCategories(): List<UsageCategory> {
    val groupedByCategory = this.groupBy { it.category }
    
    val categories = mutableListOf<UsageCategory>()
    
    // Add "All Usage" category with all items
    if (this.isNotEmpty()) {
        categories.add(
            UsageCategory(
                id = "all_usage",
                title = "All Usage",
                items = this.map { stockItem ->
                    UsageItem(
                        name = stockItem.itemName,
                        quantity = stockItem.utilization.toDoubleOrNull() ?: 0.0,
                        color = "#F44336" // Red color for usage
                    )
                }
            )
        )
    }
    
    // Add categories by type
    groupedByCategory.forEach { (category, items) ->
        categories.add(
            UsageCategory(
                id = category.lowercase().replace(" ", "_") + "_usage",
                title = "$category Usage",
                items = items.map { stockItem ->
                    UsageItem(
                        name = stockItem.itemName,
                        quantity = stockItem.utilization.toDoubleOrNull() ?: 0.0,
                        color = "#F44336" // Red color for usage
                    )
                }
            )
        )
    }
    
    return categories
}

private fun getStockColor(quantity: Int): String {
    return when {
        quantity == 0 -> "#E0E0E0" // Grey for zero stock
        quantity < 10 -> "#F44336" // Red for low stock
        else -> "#4FC3F7" // Blue for good stock
    }
}
