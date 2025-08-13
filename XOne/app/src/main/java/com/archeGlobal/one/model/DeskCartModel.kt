package com.archeGlobal.one.model

data class DeskCartModel(
    val employeeDetails: EmployeeDetails = EmployeeDetails(),
    val stationaryItems: List<StationaryItem> = getDefaultStationaryItems(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderPlaced: Boolean = false
)

data class EmployeeDetails(
    val emailId: String = "",
    val employeeId: String = "",
    val department: String = ""
)

data class StationaryItem(
    val id: String,
    val name: String,
    val iconName: String, // For drawable resource mapping
    val imageUrl: String? = null, // For API image URLs
    val currentQuantity: Int = 0,
    val maxQuantity: Int,
    val category: String = "stationary"
)

fun getDefaultStationaryItems(): List<StationaryItem> {
    return listOf(
        StationaryItem(
            id = "pen",
            name = "Pen",
            iconName = "ic_pen",
            maxQuantity = 10
        ),
        StationaryItem(
            id = "pencil",
            name = "Pencil",
            iconName = "ic_pencil",
            maxQuantity = 10
        ),
        StationaryItem(
            id = "notepad",
            name = "NotePad",
            iconName = "ic_notepad",
            maxQuantity = 5
        ),
        StationaryItem(
            id = "marker",
            name = "Marker",
            iconName = "ic_marker",
            maxQuantity = 3
        ),
        StationaryItem(
            id = "envelope_dl",
            name = "Envelope DL",
            iconName = "ic_envelope_dl",
            maxQuantity = 1
        ),
        StationaryItem(
            id = "envelope_a4",
            name = "Envelope A4",
            iconName = "ic_envelope_a4",
            maxQuantity = 1
        ),
        StationaryItem(
            id = "stapler",
            name = "Stapler",
            iconName = "ic_stapler",
            maxQuantity = 1
        ),
        StationaryItem(
            id = "glue",
            name = "Glue",
            iconName = "ic_glue",
            maxQuantity = 1
        ),
        StationaryItem(
            id = "scissor",
            name = "Scissor",
            iconName = "ic_scissor",
            maxQuantity = 2
        ),
        StationaryItem(
            id = "tape",
            name = "Tape",
            iconName = "ic_tape",
            maxQuantity = 2
        ),
        StationaryItem(
            id = "punching_machine",
            name = "Punching Machine",
            iconName = "ic_punching_machine",
            maxQuantity = 1
        )
    )
}
