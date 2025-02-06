package com.example.xone.model

data class HomeModel(
    val userName: String = "",
    val designation: String = "",
    val department: String = "",
    val employeeId: String = "SH0003",
    val searchQuery: String = "",
    val showAllApps: Boolean = false,
    val viewFavorites: Boolean = false,
    val filteredApps: List<HomeItem> = emptyList(),
    val favorites: List<HomeItem> = emptyList(),
    val defaultApps: List<HomeItem> = listOf(
        HomeItem("ID", "id_icon", false),
        HomeItem("Asset", "asset_icon", false),
        HomeItem("Timesheet", "timesheet_icon", false),
        HomeItem("Leave", "leave_icon", false),
        HomeItem("MyDocuments", "documents_icon", false),
        HomeItem("My Career", "career_icon", false),
        HomeItem("eLearning", "learning_icon", false),
        HomeItem("Goal Setting/KPI", "goal_icon", false),
        HomeItem("XCard", "xcard_icon", false)
    ),
    val categories: Map<String, List<HomeItem>> = mapOf(
        "Productivity" to listOf(
            HomeItem("Timesheet", "timesheet_icon", false),
            HomeItem("Leave", "leave_icon", false),
            HomeItem("My Career", "career_icon", false),
            HomeItem("eLearning", "learning_icon", false),
            HomeItem("Goal Setting/KPI", "goal_icon", false),
            HomeItem("XCard", "xcard_icon", false),
            HomeItem("Admin", "admin_icon", false),
            HomeItem("New Onboarding", "onboarding_icon", false),
            HomeItem("SOS", "sos_icon", false),
            HomeItem("XProfile", "profile_icon", false)
        ),
        "Information" to listOf(
            HomeItem("Asset", "asset_icon", false),
            HomeItem("MyDocuments", "documents_icon", false),
            HomeItem("Holiday Calendar", "holiday_icon", false),
            HomeItem("Locations", "location_icon", false),
            HomeItem("Policy", "policy_icon", false)
        ),
        "Social" to listOf(
            HomeItem("Greetings", "greetings_icon", false),
            HomeItem("XConnect", "xconnect_icon", false)
        ),
        "Enterprise Applications" to listOf(
            HomeItem("Finance", "finance_icon", false),
            HomeItem("Medical", "medical_icon", false),
            HomeItem("Travel & Expenses", "travel_icon", false),
            HomeItem("SAP", "sap_icon", false)
        )
    ),
    val footerNavigation: FooterNavigationModel = FooterNavigationModel(),
    val showSearchAndFavorites: Boolean = false
)

data class HomeItem(
    val title: String,
    val icon: String,
    val isFavorite: Boolean = false
) 