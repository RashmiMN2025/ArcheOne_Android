package com.archeGlobal.one.model

data class InventoryModel(
    val isLoading: Boolean = false,
    val selectedLocation: String = "Bengaluru",
    val selectedType: String = "All",
    val locations: List<String> = listOf("Bengaluru", "Chennai", "Coimbatore"),
    val types: List<String> = listOf("All", "Writing", "Paper", "Office Supplies", "Hygiene"),
    val inventoryItems: List<InventoryItem> = getDefaultInventoryItems()
)

data class InventoryItem(
    val id: String,
    val name: String,
    val itemNumber: String,
    val totalStock: Int,
    val lastUpdated: String,
    val iconName: String,
    val category: String
)

private fun getDefaultInventoryItems(): List<InventoryItem> {
    return listOf(
        InventoryItem(
            id = "pen",
            name = "Pen",
            itemNumber = "ARSTATA001",
            totalStock = 260,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_pen",
            category = "Writing"
        ),
        InventoryItem(
            id = "pencil",
            name = "Pencil",
            itemNumber = "ARSTATA002",
            totalStock = 170,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_pencil",
            category = "Writing"
        ),
        InventoryItem(
            id = "marker",
            name = "Marker",
            itemNumber = "ARSTATA003",
            totalStock = 90,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_marker",
            category = "Writing"
        ),
        InventoryItem(
            id = "notepad",
            name = "Notepad",
            itemNumber = "ARSTATA004",
            totalStock = 60,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_notepad",
            category = "Paper"
        ),
        InventoryItem(
            id = "eraser",
            name = "Eraser",
            itemNumber = "ARSTATA005",
            totalStock = 80,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_eraser",
            category = "Writing"
        ),
        InventoryItem(
            id = "sticky_note",
            name = "Sticky Note",
            itemNumber = "ARSTATA006",
            totalStock = 50,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_sticky_note",
            category = "Paper"
        ),
        InventoryItem(
            id = "envelope_dl",
            name = "Envelope DL",
            itemNumber = "ARSTATA007",
            totalStock = 100,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_envelope_dl",
            category = "Paper"
        ),
        InventoryItem(
            id = "envelope_a4",
            name = "Envelope A4",
            itemNumber = "ARSTATA008",
            totalStock = 75,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_envelope_a4",
            category = "Paper"
        ),
        InventoryItem(
            id = "tissues",
            name = "Tissues",
            itemNumber = "ARHK001",
            totalStock = 35,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_tissues",
            category = "Hygiene"
        ),
        InventoryItem(
            id = "handwash",
            name = "Handwash",
            itemNumber = "ARHK002",
            totalStock = 6,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_handwash",
            category = "Hygiene"
        ),
        InventoryItem(
            id = "stapler",
            name = "Stapler",
            itemNumber = "ARBSSTA001",
            totalStock = 4,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_stapler",
            category = "Office Supplies"
        ),
        InventoryItem(
            id = "tape",
            name = "Tape",
            itemNumber = "ARBSSTA002",
            totalStock = 3,
            lastUpdated = "8 Jul 2024 at 11:45 AM",
            iconName = "ic_tape",
            category = "Office Supplies"
        )
    )
} 