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
        HomeItem("ID", "id_icon"),
        HomeItem("Asset", "asset_icon"),
        HomeItem("Timesheet", "timesheet_icon"),
        HomeItem("Leave", "leave_icon"),
        HomeItem("MyDocuments", "documents_icon"),
        HomeItem("My Career", "career_icon"),
        HomeItem("eLearning", "learning_icon"),
        HomeItem("Goal Setting/KPI", "goal_icon"),
        HomeItem("XCard", "xcard_icon")
    ),
    val categories: Map<String, List<HomeItem>> = mapOf(
        "Productivity" to listOf(
            HomeItem("Timesheet", "timesheet_icon"),
            HomeItem("Leave", "leave_icon"),
            HomeItem("My Career", "career_icon"),
            HomeItem("eLearning", "learning_icon"),
            HomeItem("Goal Setting/KPI", "goal_icon"),
            HomeItem("XCard", "xcard_icon"),
            HomeItem("Admin", "admin_icon"),
            HomeItem("New Onboarding", "onboarding_icon"),
            HomeItem("SOS", "sos_icon"),
            HomeItem("XProfile", "profile_icon")
        ),
        "Information" to listOf(
            HomeItem("Asset", "asset_icon"),
            HomeItem("MyDocuments", "documents_icon"),
            HomeItem("Holiday Calendar", "holiday_icon"),
            HomeItem("Locations", "location_icon"),
            HomeItem("Policy", "policy_icon")
        ),
        "Social" to listOf(
            HomeItem("Greetings", "greetings_icon"),
            HomeItem("XConnect", "xconnect_icon")
        ),
        "Enterprise Applications" to listOf(
            HomeItem("Finance", "finance_icon"),
            HomeItem("Medical", "medical_icon"),
            HomeItem("Travel & Expenses", "travel_icon"),
            HomeItem("SAP", "sap_icon")
        )
    )
)

data class HomeItem(
    val title: String,
    val icon: String
) 