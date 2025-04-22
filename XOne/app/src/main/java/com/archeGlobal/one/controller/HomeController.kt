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
            "my career" -> {
                Log.d("HomeController", "My Career service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "My Career service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "elearning" -> {
                Log.d("HomeController", "eLearning service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "eLearning service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "goal setting/kpi", "goal" -> {
                Log.d("HomeController", "Goal Setting/KPI service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Goal Setting/KPI service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "medical" -> navigator.navigateToMedical()
            "finance" -> navigator.navigateToFinance()
            "admin" -> {
                Log.d("HomeController", "Admin service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Admin service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "hr" -> {
                Log.d("HomeController", "HR service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "HR service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "holiday calendar" -> navigator.navigateToHolidayCalendar()
            "client calendar" -> {
                Log.d("HomeController", "Client Calendar service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Client Calendar service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "greetings" -> {
                Log.d("HomeController", "Greetings service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Greetings service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "connect" -> {
                Log.d("XConnect", "Navigating to XConnect")
                navigator.navigateToXConnect()
            }
            "helpdesk" -> {
                Log.d("HomeController", "Helpdesk service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Helpdesk service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "announcements" -> {
                Log.d("HomeController", "Announcements service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Announcements service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "xprofile" -> navigator.navigateToXProfile()
            "password reset" -> {
                Log.d("HomeController", "Password Reset service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "Password Reset service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            "policy" -> navigator.navigateToPolicy()
            "sos" -> {
                Log.d("SOS", "Navigating to SOS")
                navigator.navigateToSOS(false)
            }
            "travel & expenses" -> navigator.navigateToTravelExpenses()
            "sap" -> navigator.navigateToSAP()
            else -> {
                // Default case for any non-handled services
                Log.d("HomeController", "${item.title} service not available yet")
                android.widget.Toast.makeText(
                    context,
                    "${item.title} service is not available yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
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
        val userData = OtpVerificationController.getUserData()
        model = model.copy(
            userName = userData?.name ?: "",
            designation = userData?.designation ?: "",
            department = userData?.department ?: "",
            employeeId = userData?.employeeId ?: "",
            profilePicture = userData?.profilePic,
            categories = userData?.let { data ->
                data.services
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
            favorites = preferencesManager.getFavorites()
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