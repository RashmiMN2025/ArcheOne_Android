package com.example.xone.controller

import com.example.xone.model.HomeModel
import com.example.xone.model.HomeItem
import com.example.xone.navigation.AndroidNavigator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log

class HomeController(private val navigator: AndroidNavigator) {
    private var _homeModel by mutableStateOf(HomeModel(
        userName = "Annamalai",  // Changed from "Biswajit Dixit"
        designation = "Graduate Engineer Trainee",  // Changed from "Senior iOS Developer"
        department = "Delivery",  // Same as before
        employeeId = "NT1347",  // Changed from "NT1426"
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
        )
    ))
    
    fun getHomeData(): HomeModel = _homeModel
    
    fun onSearchQueryChanged(query: String) {
        val allApps = _homeModel.categories.values.flatten()
        val filtered = if (query.isEmpty()) {
            emptyList()
        } else {
            allApps.filter { 
                it.title.lowercase().contains(query.lowercase()) 
            }
        }
        _homeModel = _homeModel.copy(
            searchQuery = query,
            filteredApps = filtered
        )
    }
    
    fun onAllAppsClick() {
        _homeModel = if (_homeModel.showAllApps) {
            // If All Apps is currently selected, switch to default view
            _homeModel.copy(
                showAllApps = false,
                viewFavorites = false,
                showSearchAndFavorites = false,
                searchQuery = ""  // Clear search when going back to default
            )
        } else {
            // If All Apps is not selected, switch to All Apps view
            _homeModel.copy(
                showAllApps = true,
                viewFavorites = false,
                showSearchAndFavorites = true
            )
        }
    }
    
    fun onFavoritesClick() {
        _homeModel = if (_homeModel.viewFavorites) {
            // If Favorites is currently selected, switch to default view
            _homeModel.copy(
                showAllApps = false,
                viewFavorites = false,
                showSearchAndFavorites = false,
                searchQuery = ""  // Clear search when going back to default
            )
        } else {
            // If Favorites is not selected, switch to Favorites view
            _homeModel.copy(
                showAllApps = false,
                viewFavorites = true,
                showSearchAndFavorites = true
            )
        }
    }
    
    fun onItemClick(title: String) {
        Log.d("HomeController", "onItemClick: $title")
        when (title) {
            "Locations" -> {
                Log.d("HomeController", "Navigating to Locations")
                navigator.navigateToLocations()
            }
            "ID" -> navigator.navigateToID()
            "Asset" -> navigator.navigateToAsset()
            "Timesheet" -> navigator.navigateToTimesheet()
            "Leave" -> navigator.navigateToLeave()
            "MyDocuments" -> navigator.navigateToMyDocuments()
            "My Career" -> navigator.navigateToMyCareer()
            "eLearning" -> navigator.navigateToELearning()
            "Goal Setting/KPI" -> navigator.navigateToGoalSetting()
            "XCard" -> navigator.navigateToXCard()
            "Medical" -> navigator.navigateToMedical()
            "Finance" -> navigator.navigateToFinance()
            "Admin" -> navigator.navigateToAdmin()
            "HR" -> navigator.navigateToHR()
            "Holiday Calendar" -> navigator.navigateToHolidayCalendar()
            "Client Calendar" -> navigator.navigateToClientCalendar()
            "Greetings" -> navigator.navigateToGreetings()
            "XConnect" -> navigator.navigateToXConnect()
            "Helpdesk" -> navigator.navigateToHelpdesk()
            "Announcements" -> navigator.navigateToAnnouncements()
            "XProfile" -> navigator.navigateToXProfile()
            "Password Reset" -> navigator.navigateToPasswordReset()
            "Policy" -> navigator.navigateToPolicy()
            "SOS" -> navigator.navigateToSOS()
            "Travel & Expenses" -> navigator.navigateToTravelExpenses()
            "SAP" -> navigator.navigateToSAP()
        }
    }

    fun onShowProfileClick() {
        // TODO: Implement profile navigation
    }

    fun onToggleFavorite(title: String) {
        val allApps = _homeModel.categories.values.flatten()
        val updatedApps = allApps.map { app ->
            if (app.title == title) {
                app.copy(isFavorite = !app.isFavorite)
            } else {
                app
            }
        }
        
        val updatedCategories = _homeModel.categories.mapValues { (_, apps) ->
            apps.map { app ->
                updatedApps.find { it.title == app.title } ?: app
            }
        }
        
        val updatedFavorites = updatedApps.filter { it.isFavorite }
        
        _homeModel = _homeModel.copy(
            categories = updatedCategories,
            favorites = updatedFavorites
        )
    }

    fun onFooterHomeClick() {
        // Already on home, no action needed
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

    fun updateViewMode(showAllApps: Boolean, viewFavorites: Boolean) {
        _homeModel = _homeModel.copy(
            showAllApps = showAllApps,
            viewFavorites = viewFavorites,
            showSearchAndFavorites = showAllApps || viewFavorites
        )
    }
} 