package com.example.xone.model

data class Location(
    val name: String,
    val address: String,
    val hrNumber: String? = null,
    val hrName: String? = null,
    val email: String? = null,
    val adminName: String? = null,
    val adminNumber: String? = null,
    val hasFloorMap: Boolean = false,
    val mapFileName: String? = null
)

data class State(
    val name: String,
    val locations: List<Location>
)

data class Country(
    val name: String,
    val address: String? = null,
    val email: String? = null,
    val states: List<State>? = null,  // null if country has single location
    val location: Location? = null     // non-null if country has single location
)

data class LocationState(
    val selectedCountry: Country? = null,
    val selectedState: State? = null,
    val selectedLocation: Location? = null,
    val showFloorMap: Boolean = false,
    val showContactInfo: Boolean = false
) 