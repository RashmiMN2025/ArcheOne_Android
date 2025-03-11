package com.example.xone.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.WebViewActivity
import com.example.xone.model.*
import com.example.xone.utils.PdfUtils
import com.example.xone.network.Office as NetworkOffice
import com.example.xone.network.RegionalOffice as NetworkRegionalOffice
import com.example.xone.controller.OtpVerificationController

class LocationsController(private val context: Context) {
    private var _locationState by mutableStateOf(LocationScreenState())
    
    init {
        // Initialize locations from login response
        val offices = OtpVerificationController.getOfficesData()
        Log.d("LocationsController", "Received offices data: $offices")
        
        if (offices != null) {
            val locationsList = mutableListOf<LocationInfo>()
            
            offices.forEach { office: NetworkOffice ->
                Log.d("LocationsController", "Processing office: ${office.country}")
                when (office.country) {
                    "India" -> {
                        Log.d("LocationsController", "Processing Indian office with ${office.regionaloffice.size} regional offices")
                        locationsList.add(LocationInfo(
                            name = "India",
                            companyName = office.companyName ?: "Arche Global Pvt Ltd",
                            address = office.address,
                            email = office.email ?: "info@netcon.in",
                            hasMultipleLocations = true,
                            states = createIndianStates(office.regionaloffice)
                        ))
                    }
                    else -> {
                        Log.d("LocationsController", "Processing ${office.country} office")
                        locationsList.add(LocationInfo(
                            name = office.country,
                            companyName = office.companyName ?: "Arche Global Pvt Ltd",
                            address = office.address,
                            email = office.email ?: "info@netcon.in",
                            hasMultipleLocations = false
                        ))
                    }
                }
            }
            
            Log.d("LocationsController", "Created locations list with ${locationsList.size} locations")
            _locationState = LocationScreenState(locations = locationsList)
        } else {
            Log.e("LocationsController", "Offices data is null")
        }
    }

    private fun createIndianStates(regionalOffices: List<NetworkRegionalOffice>): List<StateInfo> {
        val stateMap = mutableMapOf<String, MutableList<NetworkRegionalOffice>>()

        regionalOffices.forEach { office ->
            val stateName = office.region
            if (!stateMap.containsKey(stateName)) {
                stateMap[stateName] = mutableListOf()
            }
            stateMap[stateName]?.add(office)
        }

        return stateMap.map { (stateName, offices) ->
            StateInfo(
                name = stateName,
                locations = offices.map { office ->
                    LocationInfo(
                        name = office.region,  // Use exact region name from API
                        companyName = office.companyName ?: "Arche Global Pvt Ltd",
                        address = office.address,
                        email = office.email ?: "info@netcon.in",
                        hasFloorMap = !office.floorMap.isNullOrEmpty(),
                        mapFileName = office.floorMap,
                        hrName = office.hrName?.takeIf { it.isNotEmpty() },
                        hrNumber = office.hrContact?.takeIf { it.isNotEmpty() },
                        adminName = office.adminName?.takeIf { it.isNotEmpty() },
                        adminNumber = office.adminContact?.takeIf { it.isNotEmpty() },
                        redirection = office.redirection
                    )
                }
            )
        }
    }

    fun getLocations() = _locationState.locations
    
    fun getState() = _locationState
    
    fun selectLocation(location: LocationInfo) {
        if (location.states != null) {
            // This is for the main India location
            _locationState = _locationState.copy(
                selectedLocation = location,
                showingStateList = true,
                showingDetails = false
            )
        } else {
            // For individual locations (including non-Indian locations)
            _locationState = _locationState.copy(
                selectedLocation = location,
                showingDetails = true,
                showingStateList = false
            )
        }
    }
    
    fun selectState(state: StateInfo) {
        _locationState = _locationState.copy(
            selectedState = state,
            showingStateList = false,
            showingDetails = true
        )
    }
    
    fun showFloorMap(mapUrl: String) {
        Log.d("LocationsController", "Attempting to open floor map in browser: $mapUrl")
        try {
            // Open in browser
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(mapUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            _locationState = _locationState.copy(showingFloorMap = true)
            Log.d("LocationsController", "Opened floor map in browser")
        } catch (e: Exception) {
            Log.e("LocationsController", "Error opening floor map: ${e.message}")
            Toast.makeText(
                context,
                "Unable to open PDF. Please check your connection or try again later.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    fun showContactInfo(show: Boolean) {
        _locationState = _locationState.copy(showingContactInfo = show)
    }
    
    fun onBackPressed(): Boolean {
        return when {
            _locationState.showingContactInfo -> {
                _locationState = _locationState.copy(showingContactInfo = false)
                true
            }
            _locationState.showingFloorMap -> {
                _locationState = _locationState.copy(showingFloorMap = false)
                true
            }
            _locationState.showingDetails -> {
                // If we're showing details of a state within India
                if (_locationState.selectedState != null) {
                    // Go back to India page
                    val indiaLocation = _locationState.locations.find { it.name == "India" }
                    _locationState = _locationState.copy(
                        showingDetails = false,
                        showingStateList = true,
                        selectedLocation = indiaLocation,
                        selectedState = null
                    )
                } else {
                    // For other countries or India itself, go back to main locations list
                    _locationState = _locationState.copy(
                        showingDetails = false,
                        showingStateList = false,
                        selectedLocation = null,
                        selectedState = null
                    )
                }
                true
            }
            _locationState.showingStateList -> {
                // Always go back to main locations list when in state list view
                _locationState = _locationState.copy(
                    showingStateList = false,
                    selectedLocation = null,
                    selectedState = null
                )
                true
            }
            else -> false
        }
    }

    fun selectStateLocation(state: StateInfo) {
        // Handle all states consistently, including Tamil Nadu
        val location = state.locations.firstOrNull() ?: return
        _locationState = _locationState.copy(
            selectedLocation = location,
            selectedState = state,  // Always set the selectedState so back navigation works correctly
            showingStateList = false,
            showingDetails = true
        )
    }

    fun resetState() {
        _locationState = _locationState.copy(
            selectedLocation = null,
            selectedState = null,
            showingDetails = false,
            showingStateList = false,
            showingFloorMap = false,
            showingContactInfo = false
        )
    }

    fun initializeLocations() {
        val offices = OtpVerificationController.getOfficesData()
        Log.d("LocationsController", "Reinitializing with offices data: $offices")
        
        if (offices != null) {
            val locationsList = mutableListOf<LocationInfo>()
            
            offices.forEach { office: NetworkOffice ->
                Log.d("LocationsController", "Processing office: ${office.country}")
                when (office.country) {
                    "India" -> {
                        Log.d("LocationsController", "Processing Indian office with ${office.regionaloffice.size} regional offices")
                        locationsList.add(LocationInfo(
                            name = "India",
                            companyName = office.companyName ?: "Arche Global Pvt Ltd",
                            address = office.address,
                            email = office.email ?: "info@netcon.in",
                            hasMultipleLocations = true,
                            states = createIndianStates(office.regionaloffice)
                        ))
                    }
                    else -> {
                        Log.d("LocationsController", "Processing ${office.country} office")
                        locationsList.add(LocationInfo(
                            name = office.country,
                            companyName = office.companyName ?: "Arche Global Pvt Ltd",
                            address = office.address,
                            email = office.email ?: "info@netcon.in",
                            hasMultipleLocations = false
                        ))
                    }
                }
            }
            
            Log.d("LocationsController", "Created locations list with ${locationsList.size} locations")
            _locationState = LocationScreenState(locations = locationsList)
        } else {
            Log.e("LocationsController", "Offices data is null")
        }
    }
} 