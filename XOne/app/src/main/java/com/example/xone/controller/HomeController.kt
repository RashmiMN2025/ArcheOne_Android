package com.example.xone.controller

import com.example.xone.model.HomeModel
import com.example.xone.model.HomeItem
import com.example.xone.navigation.Navigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import com.example.xone.navigation.AndroidNavigator

class HomeController(private val navigator: Navigator) {
    var model by mutableStateOf(HomeModel(
        userName = "Annamalai",
        designation = "Graduate Engineer Trainee",
        department = "Delivery",
        employeeId = "NT1347",
        defaultApps = listOf(
            HomeItem("My Documents", "mydocuments", false),
            HomeItem("ID", "id", false),
            HomeItem("Asset", "asset", false),
            HomeItem("XCard", "xcard", false),
            HomeItem("Leave", "leave", false),
            HomeItem("eLearning", "elearning", false),
            HomeItem("My Career", "mycareer", false),
            HomeItem("TimeSheet", "timesheet", false),
            HomeItem("Goal", "goal", false)
        ),
        categories = mapOf(
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
        )
    ))
        private set

    fun onItemClick(item: HomeItem) {
        Log.d("HomeController", "onItemClick: ${item.title}")
        when (item.title.lowercase()) {
            "locations" -> {
                Log.d("HomeController", "Navigating to Locations")
                navigator.navigateToLocations()
            }
            "xcard" -> {
                Log.d("HomeController", "Navigating to Business Card")
                navigator.navigateToBusinessCard()
            }
            "id" -> navigator.navigateToID()
            "asset" -> navigator.navigateToAsset()
            "timesheet" -> navigator.navigateToTimesheet()
            "leave" -> navigator.navigateToLeave()
            "my documents", "mydocuments" -> navigator.navigateToMyDocuments()
            "my career" -> navigator.navigateToMyCareer()
            "elearning" -> navigator.navigateToELearning()
            "goal setting/kpi", "goal" -> navigator.navigateToGoalSetting()
            "medical" -> navigator.navigateToMedical()
            "finance" -> navigator.navigateToFinance()
            "admin" -> navigator.navigateToAdmin()
            "hr" -> navigator.navigateToHR()
            "holiday calendar" -> navigator.navigateToHolidayCalendar()
            "client calendar" -> navigator.navigateToClientCalendar()
            "greetings" -> navigator.navigateToGreetings()
            "xconnect" -> navigator.navigateToXConnect()
            "helpdesk" -> navigator.navigateToHelpdesk()
            "announcements" -> navigator.navigateToAnnouncements()
            "xprofile" -> navigator.navigateToXProfile()
            "password reset" -> navigator.navigateToPasswordReset()
            "policy" -> navigator.navigateToPolicy()
            "sos" -> navigator.navigateToSOS()
            "travel & expenses" -> navigator.navigateToTravelExpenses()
            "sap" -> navigator.navigateToSAP()
        }
    }

    fun onSearchQueryChanged(query: String) {
        val allApps = model.categories.values.flatten()
        val filtered = if (query.isEmpty()) {
            emptyList()
        } else {
            allApps.filter { 
                it.title.lowercase().contains(query.lowercase()) 
            }
        }
        model = model.copy(
            searchQuery = query,
            filteredApps = filtered
        )
    }

    fun onAllAppsClick() {
        model = model.copy(showAllApps = true, viewFavorites = false)
    }

    fun onFavoritesClick() {
        model = model.copy(viewFavorites = true, showAllApps = false)
    }

    fun onShowProfileClick() {
        navigator.navigateToXProfile()
    }

    fun onToggleFavorite(item: HomeItem) {
        val updatedCategories = model.categories.mapValues { (_, items) ->
            items.map { 
                if (it.title == item.title) it.copy(isFavorite = !it.isFavorite)
                else it
            }
        }
        model = model.copy(categories = updatedCategories)
    }

    fun onFooterHomeClick() {
        // Already on home screen, no action needed
    }

    fun onFooterChatClick() {
        navigator.navigateToChat()
    }

    fun onFooterSOSClick() {
        navigator.navigateToSOS()
    }

    fun onFooterProfileClick() {
        navigator.navigateToXProfile()
    }

    fun onXCardClick() {
        navigator.navigateToBusinessCard()
    }
} 