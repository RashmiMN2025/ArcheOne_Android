package com.example.xone.controller

import com.example.xone.model.HomeModel
import com.example.xone.model.HomeItem
import com.example.xone.navigation.Navigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import android.content.Context
import com.example.xone.utils.PreferencesManager
import android.content.Intent
import com.example.xone.SOSActivity
import com.example.xone.model.FooterNavigationModel

class HomeController(
    private val navigator: Navigator,
    private val context: Context
) {
    private val preferencesManager = PreferencesManager(context)
    
    var model by mutableStateOf(HomeModel(
        userName = OtpVerificationController.getUserData()?.name ?: "",
        designation = OtpVerificationController.getUserData()?.designation ?: "",
        department = OtpVerificationController.getUserData()?.department ?: "",
        employeeId = OtpVerificationController.getUserData()?.employeeId ?: "",
        showAllApps = true,
        categories = OtpVerificationController.getUserData()?.let { userData ->
            userData.services
                .groupBy { it.category }
                .mapValues { (_, services) ->
                    services.map { service ->
                        HomeItem(
                            title = service.service,
                            icon = service.icon ?: service.service.lowercase().replace(" ", ""),
                            isFavorite = service.favourite,
                            category = service.category
                        )
                    }
                }
        } ?: emptyMap(),
        favorites = preferencesManager.getFavorites(),
        footerNavigation = FooterNavigationModel(
            showHome = true,
            showChat = false,
            showSOS = false,
            showProfile = false
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
            "business card" -> {
                Log.d("HomeController", "Navigating to Business Card")
                navigator.navigateToBusinessCard()
            }
            "id" -> navigator.navigateToID()
            "asset" -> navigator.navigateToAsset()
            "timesheet" -> navigator.navigateToTimesheet()
            "leave" -> navigator.navigateToLeave()
            "my documents", "mydocuments" -> {
                Log.d("MyDocuments", "Navigating to My Documents")
                navigator.navigateToMyDocuments()
            }
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
            "connect" -> {
                Log.d("XConnect", "Navigating to XConnect")
                navigator.navigateToXConnect()
            }
            "helpdesk" -> navigator.navigateToHelpdesk()
            "announcements" -> navigator.navigateToAnnouncements()
            "xprofile" -> navigator.navigateToXProfile()
            "password reset" -> navigator.navigateToPasswordReset()
            "policy" -> navigator.navigateToPolicy()
            "sos" -> {
                Log.d("SOS", "Navigating to SOS")
                navigator.navigateToSOS()
            }
            "travel & expenses" -> navigator.navigateToTravelExpenses()
            "sap" -> navigator.navigateToSAP()
        }
    }

    fun onAllAppsClick() {
        Log.d("HomeController", "All Apps clicked. Current state: ${model.showAllApps}")
        model = model.copy(
            showAllApps = true,
            viewFavorites = false
        )
        Log.d("HomeController", "New state - showAllApps: ${model.showAllApps}, viewFavorites: ${model.viewFavorites}")
    }

    fun onFavoritesClick() {
        Log.d("HomeController", "Favorites clicked. Current state: ${model.viewFavorites}")
        model = model.copy(
            viewFavorites = true,
            showAllApps = false
        )
        Log.d("HomeController", "New state - showAllApps: ${model.showAllApps}, viewFavorites: ${model.viewFavorites}")
    }

    fun onShowProfileClick() {
        navigator.navigateToXProfile()
    }

    fun onToggleFavorite(item: HomeItem) {
        val currentFavorites = model.favorites.toMutableMap()
        val category = item.category.ifEmpty { "Default" }
        
        val categoryFavorites = currentFavorites[category]?.toMutableList() ?: mutableListOf()
        
        if (item.isFavorite) {
            // Remove from favorites
            categoryFavorites.removeAll { it.title == item.title }
            if (categoryFavorites.isEmpty()) {
                currentFavorites.remove(category)
            } else {
                currentFavorites[category] = categoryFavorites
            }
        } else {
            // Add to favorites
            categoryFavorites.add(item.copy(isFavorite = true))
            currentFavorites[category] = categoryFavorites
        }

        // Update model and save to preferences
        model = model.copy(favorites = currentFavorites)
        preferencesManager.saveFavorites(currentFavorites)

        // Update item's favorite status in categories
        val updatedCategories = model.categories.mapValues { (_, items) ->
            items.map { 
                if (it.title == item.title) {
                    it.copy(isFavorite = !it.isFavorite)
                } else {
                    it
                }
            }
        }

        model = model.copy(
            categories = updatedCategories
        )
    }

    fun onFooterHomeClick() {
        // Already on home screen, no action needed
    }

    fun onFooterChatClick() {
        navigator.navigateToChat()
    }

    fun onFooterSOSClick() {
        // Use the navigator to navigate to SOS screen
        navigator.navigateToSOS()
    }

    fun onFooterProfileClick() {
        navigator.navigateToProfile()
    }

    fun onXCardClick() {
        navigator.navigateToBusinessCard()
    }

    fun refreshUserData() {
        model = model.copy(
            userName = OtpVerificationController.getUserData()?.name ?: "",
            designation = OtpVerificationController.getUserData()?.designation ?: "",
            department = OtpVerificationController.getUserData()?.department ?: "",
            employeeId = OtpVerificationController.getUserData()?.employeeId ?: ""
        )
    }

    fun getCurrentViewItems(): List<HomeItem> {
        return when {
            model.viewFavorites -> model.favorites.values.flatten()
            else -> model.categories.values.flatten()
        }
    }
} 