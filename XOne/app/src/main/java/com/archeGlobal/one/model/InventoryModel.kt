package com.archeGlobal.one.model

data class InventoryModel(
    val isLoading: Boolean = false,
    val selectedLocation: String = "Bengaluru",
    val selectedType: String = "All",
    val searchQuery: String = "",
    val locations: List<String> = listOf("Bengaluru", "Chennai", "Coimbatore"),
    val types: List<String> = listOf("All", "Writing", "Paper", "Office Supplies", "Hygiene"),
    val inventoryItems: List<InventoryItem> = getDefaultInventoryItems(),
    val allItems: List<InventoryItem> = getDefaultInventoryItems()
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
    val totalStock: Int = closingStock.toInt() // For backward compatibility
)

private fun getDefaultInventoryItems(): List<InventoryItem> {
    return listOf(
        InventoryItem(
            id = "s1",
            name = "S1",
            itemNumber = "AGHK_001",
            unit = "Litres",
            closingStock = 5.0,
            updatedBy = "Harish",
            suppliedDate = "8 Sep 2025 at 8:00 AM",
            lastUpdated = "8 Sep 2025 at 8:00 AM",
            iconName = "ic_pen",
            category = "Writing"
        ),
        InventoryItem(
            id = "pen",
            name = "Pen",
            itemNumber = "ARSTATA001",
            unit = "Pieces",
            closingStock = 260.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_pen",
            category = "Writing"
        ),
        InventoryItem(
            id = "pencil",
            name = "Pencil",
            itemNumber = "ARSTATA002",
            unit = "Pieces",
            closingStock = 170.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_pencil",
            category = "Writing"
        ),
        InventoryItem(
            id = "marker",
            name = "Marker",
            itemNumber = "ARSTATA003",
            unit = "Pieces",
            closingStock = 90.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_marker",
            category = "Writing"
        ),
        InventoryItem(
            id = "notepad",
            name = "Notepad",
            itemNumber = "ARSTATA004",
            unit = "Pieces",
            closingStock = 60.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_notepad",
            category = "Paper"
        ),
        InventoryItem(
            id = "eraser",
            name = "Eraser",
            itemNumber = "ARSTATA005",
            unit = "Pieces",
            closingStock = 80.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_eraser",
            category = "Writing"
        ),
        InventoryItem(
            id = "sticky_note",
            name = "Sticky Note",
            itemNumber = "ARSTATA006",
            unit = "Packs",
            closingStock = 50.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_sticky_note",
            category = "Paper"
        ),
        InventoryItem(
            id = "envelope_dl",
            name = "Envelope DL",
            itemNumber = "ARSTATA007",
            unit = "Pieces",
            closingStock = 100.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_envelope_dl",
            category = "Paper"
        ),
        InventoryItem(
            id = "envelope_a4",
            name = "Envelope A4",
            itemNumber = "ARSTATA008",
            unit = "Pieces",
            closingStock = 75.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_envelope_a4",
            category = "Paper"
        ),
        InventoryItem(
            id = "tissues",
            name = "Tissues",
            itemNumber = "ARHK001",
            unit = "Packs",
            closingStock = 35.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_tissues",
            category = "Hygiene"
        ),
        InventoryItem(
            id = "handwash",
            name = "Handwash",
            itemNumber = "ARHK002",
            unit = "Bottles",
            closingStock = 6.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_handwash",
            category = "Hygiene"
        ),
        InventoryItem(
            id = "stapler",
            name = "Stapler",
            itemNumber = "ARBSSTA001",
            unit = "Pieces",
            closingStock = 4.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_stapler",
            category = "Office Supplies"
        ),
        InventoryItem(
            id = "tape",
            name = "Tape",
            itemNumber = "ARBSSTA002",
            unit = "Rolls",
            closingStock = 3.0,
            updatedBy = "Admin",
            suppliedDate = "8 Jul 2024 at 11:45 AM",
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_tape",
            category = "Office Supplies"
        )
    )
}
