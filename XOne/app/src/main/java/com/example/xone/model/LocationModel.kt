package com.example.xone.model

data class LocationInfo(
    val name: String,
    val companyName: String,
    val address: String,
    val email: String,
    val hasMultipleLocations: Boolean = false,
    val hasFloorMap: Boolean = false,
    val mapFileName: String? = null,
    val hrNumber: String? = null,
    val hrName: String? = null,
    val adminName: String? = null,
    val adminNumber: String? = null,
    val states: List<StateInfo>? = null
)

data class StateInfo(
    val name: String,
    val locations: List<LocationInfo>
)

data class LocationScreenState(
    val locations: List<LocationInfo> = emptyList(),
    val selectedLocation: LocationInfo? = null,
    val selectedState: StateInfo? = null,
    val showingDetails: Boolean = false,
    val showingStateList: Boolean = false,
    val showingFloorMap: Boolean = false,
    val showingContactInfo: Boolean = false
) 