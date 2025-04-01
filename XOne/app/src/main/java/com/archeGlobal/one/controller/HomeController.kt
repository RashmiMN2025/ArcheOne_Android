package com.archeGlobal.one.controller

import com.archeGlobal.one.model.HomeModel
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.navigation.Navigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import android.content.Context
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.utils.ImageCache

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
        profilePicture = OtpVerificationController.getUserData()?.profilePic,
        showAllApps = true,
        categories = OtpVerificationController.getUserData()?.let { userData ->
            userData.services
                .groupBy { it.category }
                .toSortedMap(String.CASE_INSENSITIVE_ORDER)
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
                navigator.navigateToLocations(showHeader = true)
            }
            "business card" -> {
                Log.d("HomeController", "Navigating to Business Card")
                navigator.navigateToBusinessCard()
            }
            "profile connect" -> {
                Log.d("HomeController", "Profile Connect service not available yet")
                // Show a Toast message informing the user
                android.widget.Toast.makeText(
                    context,
                    "Profile Connect service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                // Do not navigate anywhere
            }
            "profile" -> {
                Log.d("HomeController", "Navigating to Profile")
                navigator.navigateToProfile()
            }
            "to do" -> {
                Log.d("HomeController", "To Do page not available yet")
                // Show a Toast message informing the user
                android.widget.Toast.makeText(
                    context,
                    "To Do functionality is coming soon",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                // No navigation yet as page is not created
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
                navigator.navigateToSOS(false)
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

        // Sort favorites by category and update model
        val sortedFavorites = currentFavorites.toSortedMap(String.CASE_INSENSITIVE_ORDER)
        
        // Update model and save to preferences
        model = model.copy(favorites = sortedFavorites)
        preferencesManager.saveFavorites(sortedFavorites)

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
        navigator.navigateToSOS(true)
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
            employeeId = OtpVerificationController.getUserData()?.employeeId ?: "",
            profilePicture = OtpVerificationController.getUserData()?.profilePic
        )
    }

    fun getCurrentViewItems(): List<HomeItem> {
        return when {
            model.viewFavorites -> model.favorites.values.flatten()
            else -> model.categories.values.flatten()
        }
    }

    fun updateProfilePicture(profilePicUrl: String?) {
        Log.d("HomeController", "Updating profile picture to: $profilePicUrl")
        
        // Invalidate the image cache first to ensure fresh loading
        ImageCache.invalidateProfileImageCache()
        
        // Force a model update with a new instance to trigger recomposition
        model = model.copy(
            profilePicture = profilePicUrl,
            // Adding a small change to any property forces recomposition
            userName = model.userName
        )
        
        // Call refreshUserData after a short delay to ensure UI updates
        // This helps when we're on the home screen and need immediate refresh
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            refreshUserData()
        }, 300) // Short delay to ensure the update propagates
    }
} 