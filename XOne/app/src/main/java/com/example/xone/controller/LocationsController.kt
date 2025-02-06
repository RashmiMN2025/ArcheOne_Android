package com.example.xone.controller

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.model.*
import com.example.xone.utils.PdfUtils

class LocationsController(private val context: Context) {
    private var _locationState by mutableStateOf(LocationScreenState())
    
    private val CHENNAI_LOCATION = LocationInfo(
        name = "Chennai, HQ",
        companyName = "Netcon Technologies India Private Ltd.",
        address = "3rd Floor, Karunaa Corner, Door No.10, Spur Tank Road, Chetpet, Chennai-600031",
        email = "info@netcon.in",
        hasFloorMap = true,
        mapFileName = "chennai_map.pdf"
    )

    private val COIMBATORE_LOCATION = LocationInfo(
        name = "Coimbatore, Registered Office",
        companyName = "Netcon Technologies India Private Ltd.",
        address = "No. 439, 4th Floor, Lakshmi Complex, Cross Cut Road, Gandhipuram, Coimbatore, Tamil Nadu 641012",
        email = "info@netcon.in",
        hasFloorMap = true,
        mapFileName = "coimbatore_map.pdf"
    )
    
    fun getLocations() = _locationState.locations
    
    fun getState() = _locationState
    
    fun selectLocation(location: LocationInfo) {
        if (location.states != null) {
            _locationState = _locationState.copy(
                selectedLocation = location,
                showingStateList = true,
                showingDetails = false
            )
        } else {
            _locationState = _locationState.copy(
                selectedLocation = location,
                selectedState = _locationState.selectedState,
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
    
    fun showFloorMap(show: Boolean) {
        _locationState = _locationState.copy(showingFloorMap = show)
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
                _locationState = _locationState.copy(
                    showingDetails = false,
                    showingStateList = true,
                    selectedLocation = _locationState.locations.find { it.name == "India" }
                )
                true
            }
            _locationState.showingStateList -> {
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

    fun getIndiaLocations(): List<LocationInfo> {
        return listOf(
            LocationInfo(
                name = "Chennai",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "3rd Floor, Karunaa Corner, Door No.10, Spur Tank Road, Chetpet, Chennai-600031",
                email = "info@netcon.in",
                hasFloorMap = true,
                mapFileName = "chennai_map.pdf"
            ),
            LocationInfo(
                name = "Bangalore",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "Shah Sultan Complex, 5th Floor, #17, Ali Asker Road, Bangalore, Karnataka 560 052",
                email = "info@netcon.in",
                hasFloorMap = true,
                mapFileName = "bangalore_map.pdf",
                hrName = "Vignesh",
                hrNumber = "9972124303",
                adminName = "Vishnu P",
                adminNumber = "7639321769"
            ),
            LocationInfo(
                name = "Hyderabad",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "#202, Skill Avenue, 5-10-191, Hill Fort Road, Saifabad, Hyderabad, Telangana 500004",
                email = "info@netcon.in"
            ),
            LocationInfo(
                name = "Mumbai",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "Awfis One International Center, Tower 1- 8th floor, Senapati Bapat Marg, Dadar West, Mumbai, Maharashtra 400013",
                email = "info@netcon.in"
            ),
            LocationInfo(
                name = "Delhi",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "G 18 Suneja Tower 2, District Centre, Janakpuri, New Delhi, Delhi 110058",
                email = "info@netcon.in"
            ),
            LocationInfo(
                name = "Noida",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "5th Floor, Tower C, Green Boulevard, B- Block, Sector 62, Noida, Uttar Pradesh 201309",
                email = "info@netcon.in"
            ),
            LocationInfo(
                name = "Ahmedabad",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "GCP Business Centre, 101-104, Opp Memnagar Firestation, Vijay Cross Road, Memnagar, Ahmedabad, Gujarat 38001",
                email = "info@netcon.in"
            ),
            LocationInfo(
                name = "Visakhapatnam",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "No. 7-5-109, Pushpa Vihar, Panduranga Puram, Visakhapatnam, Andhra Pradesh 530003",
                email = "info@netcon.in"
            ),
            LocationInfo(
                name = "Ernakulam",
                companyName = "Netcon Technologies India Private Ltd.",
                address = "No. S119, Monlash Business Centre, Crescens Tower, 4th floor, NH-47, Changampuzha Nagar Post, Ernakulam, Kerala 682033",
                email = "info@netcon.in"
            )
        )
    }

    fun showTamilNaduLocations(state: StateInfo) {
        _locationState = _locationState.copy(
            selectedState = state,
            selectedLocation = _locationState.locations.find { it.name == "India" },
            showingStateList = true,
            showingDetails = false
        )
    }

    fun selectStateLocation(state: StateInfo) {
        if (state.name == "Tamil Nadu") {
            _locationState = _locationState.copy(
                selectedState = state,
                showingStateList = true,
                showingDetails = false
            )
        } else {
            val location = state.locations.firstOrNull() ?: return
            _locationState = _locationState.copy(
                selectedLocation = location,
                selectedState = state,
                showingStateList = false,
                showingDetails = true
            )
        }
    }

    fun showFloorMap(mapFileName: String) {
        PdfUtils.openPdfFromAssets(context, mapFileName)
        _locationState = _locationState.copy(showingFloorMap = true)
    }

    fun selectSpecialLocation(locationName: String) {
        val location = when (locationName) {
            "Chennai" -> CHENNAI_LOCATION
            "Coimbatore" -> COIMBATORE_LOCATION
            else -> return
        }
        
        _locationState = _locationState.copy(
            selectedLocation = location,
            showingDetails = true,
            showingStateList = false
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
} 