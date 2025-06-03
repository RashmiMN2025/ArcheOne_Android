package com.archeGlobal.one.controller

import com.archeGlobal.one.model.HomeModel
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.navigation.Navigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import android.content.Context
import android.content.Intent
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.utils.ImageCache
import java.util.Comparator
import android.net.Uri
import com.archeGlobal.one.AssetActivity
import com.archeGlobal.one.BusinessCardActivity
import com.archeGlobal.one.CommuniqueActivity
import com.archeGlobal.one.GreetingsActivity
import com.archeGlobal.one.HolidayOptionsActivity
import com.archeGlobal.one.LocationsActivity
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.model.AboutMeModel

class HomeController(
    private val navigator: Navigator,
    private val context: Context
) {
    var employeeData by mutableStateOf(
        AboutMeModel(
            name = OtpVerificationController.getUserData()?.name ?: "",
            email = OtpVerificationController.getUserData()?.email ?: ""
        )
    )
    private val preferencesManager = PreferencesManager(context)
    
    // Helper method to navigate within the same activity
    private fun navigate(route: String) {
        if (navigator is AndroidNavigator) {
            navigator.navController?.navigate(route)
        }
    }

    // Add these:
    private var navigationCount = 0
    var onShowRatingDialog: (() -> Unit)? = null

    private fun handleNavigation(action: () -> Unit) {
        navigationCount++
        if (navigationCount % 5 == 0) {
            onShowRatingDialog?.invoke()
        }
        action()
    }
    
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
                .toSortedMap(Comparator { a, b ->
                    // If either is MyApps, handle special case
                    when {
                        a.equals("MyApps", ignoreCase = true) && !b.equals("MyApps", ignoreCase = true) -> 1
                        !a.equals("MyApps", ignoreCase = true) && b.equals("MyApps", ignoreCase = true) -> -1
                        else -> a.compareTo(b, ignoreCase = true)
                    }
                })
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
        handleNavigation {
            // ...existing navigation logic...
            Log.d("HomeController", "onItemClick: ${item.title}")
        when (item.title.lowercase()) {
            "locations" -> {
                val intent = Intent(context, LocationsActivity::class.java)
                context.startActivity(intent)
            }
            "business card" -> {
                val intent = Intent(context, BusinessCardActivity::class.java)
                context.startActivity(intent)
            }
            "asset" -> {
                val intent = Intent(context, AssetActivity::class.java)
                context.startActivity(intent)
            }
            "calendar" -> {
                val intent = Intent(context, HolidayOptionsActivity::class.java)
                context.startActivity(intent)
            }
            "greetings" -> {
                val intent = Intent(context, GreetingsActivity::class.java)
                context.startActivity(intent)
            }
            "profile connect" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Profile Connect")
                navigate("service_not_available?serviceName=Profile Connect")
            }
            "profile" -> {
                Log.d("HomeController", "Navigating to Profile")
                navigator.navigateToProfile()
            }
            "checkmate" -> {
                Log.d("HomeController", "Navigating to checkmate")
                navigator.navigateToTodo()
            }
            "id" -> navigator.navigateToID()
            "timesheet" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Timesheet")
                navigate("service_not_available?serviceName=Timesheet")
            }
            "leave" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Leave")
                navigate("service_not_available?serviceName=Leave")
            }
            "my documents", "mydocuments" -> {
                Log.d("MyDocuments", "Navigating to My Documents")
                navigator.navigateToMyDocuments()
            }
            "my career" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for My Career")
                navigate("service_not_available?serviceName=My Career")
            }
            "elearning" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for eLearning")
                navigate("service_not_available?serviceName=eLearning")
            }
            "goal setting/kpi", "goal" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Goal Setting/KPI")
                navigate("service_not_available?serviceName=Goal Setting/KPI")
            }
            "medical" -> navigator.navigateToMedical()
            "finance" -> navigator.navigateToFinance()
            "zinghr" -> {
                Log.d("HomeController", "Navigating to ZingHR")
                navigator.navigateToZingHR()
            }
            "admin" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Admin")
                navigate("service_not_available?serviceName=Admin")
            }
            "hr" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for HR")
                navigate("service_not_available?serviceName=HR")
            }
            "client calendar" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Client Calendar")
                navigate("service_not_available?serviceName=Client Calendar")
            }
            "connect" -> {
                Log.d("XConnect", "Navigating to XConnect")
                navigator.navigateToXConnect()
            }
            "blogs" -> {
                Log.d("XConnect", "Navigating to XConnect for blogs")
                navigator.navigateToXConnect("Blogs")
            }
            "helpdesk" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Helpdesk")
                navigate("service_not_available?serviceName=Helpdesk")
            }
            "announcements" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Announcements")
                navigate("service_not_available?serviceName=Announcements")
            }
            "xprofile" -> navigator.navigateToXProfile()
            "password reset" -> {
                Log.d("HomeController", "Navigating to Service Not Available screen for Password Reset")
                navigate("service_not_available?serviceName=Password Reset")
            }
            "policy" -> navigator.navigateToPolicy()
            "sos" -> {
                Log.d("SOS", "Navigating to SOS")
                navigator.navigateToSOS(false)
            }
            "travel" -> {
                Log.d("HomeController", "Navigating to Travel & Expenses")
                val intent = Intent(context, WebViewActivity::class.java).apply {
                    putExtra("fileUrl", "https://ithsmart.travelhouseindia.in/travel/travel_web.xhtml")
                    putExtra("title", "Travel & Expenses")
                }
                context.startActivity(intent)
            }
            "sap" -> navigator.navigateToSAP()
            "ample" -> navigator.navigateToAmple()
            "about us", "aboutus" -> {
                Log.d("HomeController", "Navigating to About Us")
                navigator.navigateToAboutUs()
            }
            "corevalues", "core values" -> {
                Log.d("HomeController", "Navigating to Core Values")
                navigator.navigateToCoreValues()
            }
            "vision" -> {
                Log.d("HomeController", "Navigating to Vision")
                navigator.navigateToVision()
            }
            "communique" -> {
                val intent = Intent(context, CommuniqueActivity::class.java)
                context.startActivity(intent)
            }
            "archeodyssey", "arche odyssey" -> {
                Log.d("HomeController", "Navigating to Arche Odyssey")
                navigator.navigateToArcheOdyssey()
            }
            "know your org" -> {
                Log.d("HomeController", "Navigating to Arche Odyssey via Know Your Org")
                navigator.navigateToArcheOdyssey()
            }
            "pulse" -> {
                Log.d("HomeController", "Navigating to Pulse")
                navigator.openPulseLogin()
            }
            "ideavault", "idea vault" -> {
                Log.d("HomeController", "Navigating to Idea Vault")
                navigator.navigateToIdeaVault()
            }            else -> {
                // Default case for any non-handled services
                Log.d("HomeController", "Navigating to Service Not Available screen for ${item.title}")
                navigate("service_not_available?serviceName=${Uri.encode(item.title)}")
            }
        }
        }
    }

    fun onAllAppsClick() = handleNavigation {
        Log.d("HomeController", "All Apps clicked. Current state: ${model.showAllApps}")
        model = model.copy(
            showAllApps = true,
            viewFavorites = false
        )
        Log.d("HomeController", "New state - showAllApps: ${model.showAllApps}, viewFavorites: ${model.viewFavorites}")
    }

    fun onFavoritesClick() = handleNavigation {
        Log.d("HomeController", "Favorites clicked. Current state: ${model.viewFavorites}")
        model = model.copy(
            viewFavorites = true,
            showAllApps = false
        )
        Log.d("HomeController", "New state - showAllApps: ${model.showAllApps}, viewFavorites: ${model.viewFavorites}")
    }

    fun onShowProfileClick() = handleNavigation {
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

    fun onFooterHomeClick() = handleNavigation {
        // Already on home screen, no action needed
    }

    fun onFooterChatClick() = handleNavigation {
        navigator.navigateToChat()
    }

    fun onFooterSOSClick() = handleNavigation {
        // Use the navigator to navigate to SOS screen
        navigator.navigateToSOS(true)
    }

    fun onFooterProfileClick() = handleNavigation {
        navigator.navigateToProfile()
    }

    fun onXCardClick() = handleNavigation {
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
                    .toSortedMap(Comparator { a, b ->
                        // If either is MyApps, handle special case
                        when {
                            a.equals("MyApps", ignoreCase = true) && !b.equals("MyApps", ignoreCase = true) -> 1
                            !a.equals("MyApps", ignoreCase = true) && b.equals("MyApps", ignoreCase = true) -> -1
                            else -> a.compareTo(b, ignoreCase = true)
                        }
                    })
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