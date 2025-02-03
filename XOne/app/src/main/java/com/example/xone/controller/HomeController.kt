package com.example.xone.controller

import com.example.xone.model.HomeModel
import com.example.xone.model.HomeItem
import com.example.xone.navigation.Navigator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class HomeController(private val navigator: Navigator) {
    private var _homeModel by mutableStateOf(HomeModel(
        userName = "John Doe",
        designation = "Senior Developer",
        department = "Software Development",
        employeeId = "SH0003",
        showAllApps = false
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
        _homeModel = _homeModel.copy(
            showAllApps = true,
            viewFavorites = false
        )
    }
    
    fun onFavoritesClick() {
        _homeModel = _homeModel.copy(
            showAllApps = false,
            viewFavorites = true
        )
    }
    
    fun onItemClick(title: String) {
        when (title) {
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
            "Locations" -> navigator.navigateToLocations()
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
} 