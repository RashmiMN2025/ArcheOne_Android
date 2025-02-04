package com.example.xone.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.model.*

class LocationsController {
    private var _locationState = mutableStateOf(LocationState())
    private var locationState by _locationState

    fun getCountries(): List<Country> {
        return listOf(
            Country(
                name = "USA",
                location = Location(
                    name = "Sunnyvale",
                    address = "Netcon Technologies Inc.\n1567 S Wolfe Rd, Sunnyvale, California, USA 94087",
                    email = "info@netcon.in"
                )
            ),
            Country(
                name = "India",
                email = "info@netcon.in",
                states = listOf(
                    State("Tamil Nadu", listOf(
                        Location(
                            name = "Coimbatore, Registered Office",
                            address = "Netcon Technologies India Private Ltd.\nDVP Building First Floor,\nOld No.25/1, New No. 4,\nKalapatti Main Road,\nCivil Aerodrome Post,\nNehru Nagar West,\nCoimbatore – 641014.",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "coimbatore_map.pdf"
                        ),
                        Location(
                            name = "Chennai, HQ",
                            address = "3rd Floor, Karunaa Corner,\nDoor No.10, Spur Tank Road,\nChetpet, Chennai-600031",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "chennai_map.pdf"
                        )
                    )),
                    State("Karnataka", listOf(
                        Location(
                            name = "Bangalore, Karnataka",
                            address = "Shah Sultan Complex\n5th Floor, #17, Ali Asker Road,\nBangalore, Karnataka 560 052.",
                            hrNumber = "9972124303",
                            hrName = "Vignesh",
                            email = "info@netcon.in",
                            adminName = "Vishnu P",
                            adminNumber = "7639321769",
                            hasFloorMap = true,
                            mapFileName = "bangalore_map.pdf"
                        )
                    )),
                    State("Telangana", listOf(
                        Location(
                            name = "Hyderabad, Telangana",
                            address = "#202, Skill Avenue, 5-10-191,\nHill Fort Road,\nSaifabad, Hyderabad, Telangana 500004.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Kerala", listOf(
                        Location(
                            name = "Ernakulam, Kerala",
                            address = "No. S119, Monlash Business Centre,\nCrescens Tower, 4th floor,\nNH-47, Changampuzha Nagar Post,\nErnakulam, Kerala 682033.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Maharashtra", listOf(
                        Location(
                            name = "Mumbai, Maharashtra",
                            address = "Awfis One International Center,\nTower 1- 8th floor, Senapati Bapat Marg, Dadar West, Prabhadevi, Lower Parel,\nMumbai, Maharashtra 400013.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Delhi", listOf(
                        Location(
                            name = "New Delhi, Delhi",
                            address = "G 18 Suneja Tower 2, District Centre,\nJanakpuri, New Delhi, Delhi 110058.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Uttar Pradesh", listOf(
                        Location(
                            name = "Noida, Uttar Pradesh",
                            address = "5th Floor, Tower C,\nGreen Boulevard, B- Block,\nSector 62, Noida, Uttar Pradesh 201309.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Gujarat", listOf(
                        Location(
                            name = "Ahmedabad, Gujarat",
                            address = "GCP Business Centre, 101-104,\nOpp Memnagar Firestation,\nVijay Cross Road, Memnagar,\nAhmedabad, Gujarat 38001.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Andhra Pradesh", listOf(
                        Location(
                            name = "Visakhapatnam, Andhra Pradesh",
                            address = "No. 7-5-109, Pushpa Vihar,\nPanduranga Puram,\nVisakhapatnam, Andhra Pradesh 530003.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Chhattisgarh", listOf(
                        Location(
                            name = "Raipur, Chhattisgarh",
                            address = "C-240/6, Vallabh Nagar\nRaipur, Chhattisgarh 492001.",
                            email = "info@netcon.in"
                        )
                    )),
                    State("Haryana", listOf(
                        Location(
                            name = "Gurgaon, Haryana",
                            address = "Times Square, 4th Floor, B-Block,\nSushant Lok - 1, Gurgaon, Haryana 122002.",
                            email = "info@netcon.in"
                        )
                    ))
                )
            ),
            Country(
                name = "Dubai",
                location = Location(
                    name = "Dubai",
                    address = "PO Box 24000, M 48-49, The Curve Building, Al Qouz 3, Dubai, United Arab Emirates",
                    email = "info@netcon.in"
                )
            ),
            Country(
                name = "Singapore",
                location = Location(
                    name = "Singapore",
                    address = "Netcon Technologies Pte. Ltd.#13-14, Golden Mile Tower, 6001 Beach Road, Singapore – 199589",
                    email = "info@netcon.in"
                )
            ),
            Country(
                name = "Africa",
                location = Location(
                    name = "Uganda",
                    address = "Netcon Technologies India Private Ltd, Plot 42, Lugogo House, Rotary Avenue, P.O Box 26657, Kampala, Uganda",
                    email = "info@netcon.in"
                )
            ),
            Country(
                name = "Latin America",
                location = Location(
                    name = "Peru",
                    address = "Jr. Soldado Cabada 237, Barranco, Lima 15063, Perú",
                    email = "info@netcon.in"
                )
            )
        )
    }

    private fun needsCitySelection(state: State): Boolean {
        return state.name == "Tamil Nadu"
    }

    fun selectCountry(country: Country?) {
        locationState = locationState.copy(
            selectedCountry = country,
            selectedState = null,
            selectedLocation = null
        )
    }

    fun selectState(state: State?) {
        if (state == null) {
            locationState = locationState.copy(
                selectedState = null,
                selectedLocation = null
            )
            return
        }

        if (needsCitySelection(state)) {
            locationState = locationState.copy(
                selectedState = state,
                selectedLocation = null
            )
        } else {
            locationState = locationState.copy(
                selectedState = null,
                selectedLocation = state.locations.firstOrNull()
            )
        }
    }

    fun selectLocation(location: Location?) {
        locationState = locationState.copy(selectedLocation = location)
    }

    fun showFloorMap(show: Boolean) {
        locationState = locationState.copy(showFloorMap = show)
    }

    fun showContactInfo(show: Boolean) {
        locationState = locationState.copy(showContactInfo = show)
    }

    fun getCurrentState() = _locationState.value
} 