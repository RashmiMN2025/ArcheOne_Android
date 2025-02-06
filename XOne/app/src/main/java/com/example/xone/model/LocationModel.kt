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
    val locations: List<LocationInfo> = listOf(
        LocationInfo(
            name = "USA",
            companyName = "Netcon Technologies Inc.",
            address = "1567 S Wolfe Rd, Sunnyvale CA, USA 94087",
            email = "info@netcon.in",
            hasMultipleLocations = true
        ),
        LocationInfo(
            name = "India",
            companyName = "Netcon Technologies India Private Ltd.",
            address = "DVP Building First Floor, Old No.25/1, New No. 4, Kalapatti Main Road, Civil Aerodrome Post, Nehru Nagar West, Coimbatore, India – 641014",
            email = "info@netcon.in",
            hasMultipleLocations = true,
            states = listOf(
                StateInfo(
                    name = "Tamil Nadu",
                    locations = listOf(
                        LocationInfo(
                            name = "Chennai",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "3rd Floor, Karunaa Corner, Door No.10, Spur Tank Road, Chetpet, Chennai-600031",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "chennai_map.pdf"
                        ),
                        LocationInfo(
                            name = "Coimbatore",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "DVP Building First Floor, Old No.25/1, New No. 4, Kalapatti Main Road, Civil Aerodrome Post, Nehru Nagar West, Coimbatore – 641014",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "coimbatore_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Karnataka",
                    locations = listOf(
                        LocationInfo(
                            name = "Bangalore, Karnataka",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "Shah Sultan Complex, 5th Floor, #17, Ali Asker Road, Bangalore, Karnataka 560 052",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "bangalore_map.pdf",
                            hrName = "Vignesh",
                            hrNumber = "9972124303",
                            adminName = "Vishnu P",
                            adminNumber = "7639321769"
                        )
                    )
                ),
                StateInfo(
                    name = "Telangana",
                    locations = listOf(
                        LocationInfo(
                            name = "Hyderabad, Telangana",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "#202, Skill Avenue, 5-10-191, Hill Fort Road, Saifabad, Hyderabad, Telangana 500004",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "hyderabad_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Kerala",
                    locations = listOf(
                        LocationInfo(
                            name = "Ernakulam, Kerala",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "No. S119, Monlash Business Centre, Crescens Tower, 4th floor, NH-47, Changampuzha Nagar Post, Ernakulam, Kerala 682033",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "ernakulam_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Maharashtra",
                    locations = listOf(
                        LocationInfo(
                            name = "Mumbai, Maharashtra",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "Awfis One International Center, Tower 1- 8th floor, Senapati Bapat Marg, Dadar West, Mumbai, Maharashtra 400013",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "mumbai_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Delhi",
                    locations = listOf(
                        LocationInfo(
                            name = "New Delhi, Delhi",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "G 18 Suneja Tower 2, District Centre, Janakpuri, New Delhi, Delhi 110058",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "delhi_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Uttar Pradesh",
                    locations = listOf(
                        LocationInfo(
                            name = "Noida, Uttar Pradesh",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "5th Floor, Tower C, Green Boulevard, B- Block, Sector 62, Noida, Uttar Pradesh 201309",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "noida_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Gujarat",
                    locations = listOf(
                        LocationInfo(
                            name = "Ahmedabad, Gujarat",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "GCP Business Centre, 101-104, Opp Memnagar Firestation, Vijay Cross Road, Memnagar, Ahmedabad, Gujarat 38001",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "ahmedabad_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Andhra Pradesh",
                    locations = listOf(
                        LocationInfo(
                            name = "Visakhapatnam, Andhra Pradesh",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "No. 7-5-109, Pushpa Vihar, Panduranga Puram, Visakhapatnam, Andhra Pradesh 530003",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "vizag_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Chhattisgarh",
                    locations = listOf(
                        LocationInfo(
                            name = "Raipur, Chhattisgarh",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "C-240/6, Vallabh Nagar, Raipur, Chhattisgarh 492001",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "raipur_map.pdf"
                        )
                    )
                ),
                StateInfo(
                    name = "Haryana",
                    locations = listOf(
                        LocationInfo(
                            name = "Gurgaon, Haryana",
                            companyName = "Netcon Technologies India Private Ltd.",
                            address = "Times Square, 4th Floor, B-Block, Sushant Lok - 1, Gurgaon, Haryana 122002",
                            email = "info@netcon.in",
                            hasFloorMap = true,
                            mapFileName = "gurgaon_map.pdf"
                        )
                    )
                )
            )
        ),
        LocationInfo(
            name = "Dubai",
            companyName = "Netcon Technologies",
            address = "PO Box 24000, M 48-49, The Curve Building, Al Qouz 3, Dubai, United Arab Emirates",
            email = "info@netcon.in",
            hasMultipleLocations = true
        ),
        LocationInfo(
            name = "Singapore",
            companyName = "Netcon Technologies Pte. Ltd.",
            address = "#13-14, Golden Mile Tower, 6001 Beach Road, Singapore – 199589",
            email = "info@netcon.in",
            hasMultipleLocations = true
        ),
        LocationInfo(
            name = "Africa",
            companyName = "Netcon Technologies India Private Ltd.",
            address = "Plot 42, Lugogo House, Rotary Avenue, P.O Box 26657, Kampala, Uganda",
            email = "info@netcon.in",
            hasMultipleLocations = true
        ),
        LocationInfo(
            name = "Latin America",
            companyName = "Netcon Technologies",
            address = "Jr. Soldado Cabada 237, Barranco, Lima 15063, Perú",
            email = "info@netcon.in",
            hasMultipleLocations = true
        )
    ),
    val selectedLocation: LocationInfo? = null,
    val selectedState: StateInfo? = null,
    val showingDetails: Boolean = false,
    val showingStateList: Boolean = false,
    val showingFloorMap: Boolean = false,
    val showingContactInfo: Boolean = false
) 