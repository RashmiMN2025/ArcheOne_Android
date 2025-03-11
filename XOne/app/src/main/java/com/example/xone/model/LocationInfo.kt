import com.example.xone.model.StateInfo

data class LocationInfo(
    val name: String,
    val companyName: String = "",
    val address: String = "",
    val email: String = "",
    val hasMultipleLocations: Boolean = false,
    val states: List<StateInfo> = emptyList(),
    val hasFloorMap: Boolean = false,
    val mapFileName: String? = null,
    val hrName: String? = null,
    val hrNumber: String? = null,
    val adminName: String? = null,
    val adminNumber: String? = null,
    val redirection: String? = null
) 