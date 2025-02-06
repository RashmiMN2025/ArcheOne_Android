package com.example.xone.model

data class HomeModel(
    val userName: String = "",
    val designation: String = "",
    val department: String = "",
    val employeeId: String = "",
    val searchQuery: String = "",
    val showAllApps: Boolean = false,
    val viewFavorites: Boolean = false,
    val showSearchAndFavorites: Boolean = false,
    val defaultApps: List<HomeItem> = emptyList(),
    val categories: Map<String, List<HomeItem>> = mapOf(
        "Productivity" to listOf(
            HomeItem("Timesheet", "timesheet", false),
            HomeItem("Leave", "leave", false),
            HomeItem("My Career", "mycareer", false),
            HomeItem("eLearning", "elearning", false),
            HomeItem("Goal Setting/KPI", "goal", false),
            HomeItem("XCard", "xcard", false),
            HomeItem("Admin", "admin", false),
            HomeItem("New Onboarding", "onboarding", false),
            HomeItem("SOS", "sos", false),
            HomeItem("XProfile", "xprofile", false)
        ),
        "Information" to listOf(
            HomeItem("Asset", "asset", false),
            HomeItem("MyDocuments", "mydocuments", false),
            HomeItem("Holiday Calendar", "holiday", false),
            HomeItem("Locations", "locations", false),
            HomeItem("Policy", "policy", false)
        ),
        "Social" to listOf(
            HomeItem("Greetings", "greetings", false),
            HomeItem("XConnect", "xconnect", false)
        ),
        "Enterprise Applications" to listOf(
            HomeItem("Medical", "medical", false),
            HomeItem("Finance", "finance", false),
            HomeItem("Travel & Expenses", "travel", false),
            HomeItem("SAP", "sap", false)
        )
    ),
    val filteredApps: List<HomeItem> = emptyList(),
    val favorites: List<HomeItem> = emptyList(),
    val footerNavigation: FooterNavigationModel = FooterNavigationModel()
)

data class HomeItem(
    val title: String,
    val icon: String,
    var isFavorite: Boolean = false,
    val category: String = ""
) 