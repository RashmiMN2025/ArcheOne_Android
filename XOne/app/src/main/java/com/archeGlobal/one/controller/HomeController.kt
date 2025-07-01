package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.AssetActivity
import com.archeGlobal.one.BusinessCardActivity
import com.archeGlobal.one.CommuniqueActivity
import com.archeGlobal.one.GreetingsActivity
import com.archeGlobal.one.HolidayCalendarActivity
import com.archeGlobal.one.LocationsActivity
import com.archeGlobal.one.PolicyActivity
import com.archeGlobal.one.model.AboutMeModel
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.model.HomeModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.ImageCache
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Comparator
import java.util.Date
import java.util.Locale

class HomeController(
    private val navigator: Navigator,
    private val context: Context,
    initialModel: HomeModel = HomeModel()
) {
    var employeeData by mutableStateOf(
        AboutMeModel(
            name = OtpVerificationController.getUserData()?.name ?: "",
            email = OtpVerificationController.getUserData()?.email ?: ""
        )
    )
    private val _eventData = MutableStateFlow<com.archeGlobal.one.model.EventResponse?>(null)
    val eventData: StateFlow<com.archeGlobal.one.model.EventResponse?> = _eventData.asStateFlow()

    private val _showEventPopup = MutableStateFlow(false)
    val showEventPopup: StateFlow<Boolean> = _showEventPopup.asStateFlow()

    // Pride Month specific state
    private val _isPrideMonth = MutableStateFlow(false)
    val isPrideMonth: StateFlow<Boolean> = _isPrideMonth.asStateFlow()

    private val _showPrideMonthDialog = MutableStateFlow(false)
    val showPrideMonthDialog: StateFlow<Boolean> = _showPrideMonthDialog.asStateFlow()

    // Companion object and other class members follow
    companion object {
        private const val PREF_NAME = "event_preferences"
        private const val KEY_LAST_SHOWN_DATE = "last_shown_date"
        private const val KEY_PRIDE_MONTH_SHOWN = "pride_month_shown"
        private const val KEY_USING_PRIDE_ICON = "using_pride_icon"
    }

    // Initialize event handling
    init {
        Log.d("EventController", "Initializing HomeController and fetching daily event")
        Log.d("EventController", "UserDataManager instance: ${UserDataManager.getInstance(context)}")

        // Check if current month is June (Pride Month)
        checkIfPrideMonth()

        if (_isPrideMonth.value) {
            // Don't automatically show Pride Month dialog - only show when pinned message is clicked
            // Just set the flag that it's Pride Month, but don't show dialog
            Log.d("EventController", "It's Pride Month, but not showing dialog automatically")
        } else {
            // If not Pride Month, fetch the regular daily event
            fetchEventFromLoginData()
        }
    }

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
        if (navigationCount % 10 == 0) {
            onShowRatingDialog?.invoke()
        }
        action()
    }

    // Event-related methods
    // Process event data to ensure image URLs are valid and properly formatted
    private fun processEventData(event: com.archeGlobal.one.model.EventResponse?): com.archeGlobal.one.model.EventResponse? {
        if (event == null) return null
        val originalImageUrl = event.image ?: ""

        Log.d("EventController", "Processing event image URL: $originalImageUrl")

        // Check if the image URL is valid and properly formatted
        if (originalImageUrl.isBlank()) {
            Log.d("EventController", "Image URL is blank or null, returning original event")
            return event // Return original event if image is blank or null
        }

        // Ensure the URL is properly formatted (starts with http:// or https://)
        val formattedImageUrl = if (!originalImageUrl.startsWith("http://") && !originalImageUrl.startsWith("https://")) {
            // Assuming pulse.netcon.in is the base for relative paths
            "https://pulse.netcon.in:7000/$originalImageUrl".trim()
        } else {
            originalImageUrl.trim()
        }

        Log.d("EventController", "Formatted event image URL: $formattedImageUrl")
        return event.copy(image = formattedImageUrl)
    }

    private fun fetchEventFromLoginData() {
        Log.d("EventController", "Starting to fetch daily event from login response")
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                // Get event data from UserDataManager instead of making a separate API call
                val userDataManager = UserDataManager.getInstance(context)
                var eventResponse = userDataManager.getEventData() // eventResponse is EventResponse?
                Log.d("EventController", "Fetched event from UserDataManager: Title=${eventResponse?.title}, Image=${eventResponse?.image}")

                // Process the event data (e.g., format image URL)
                eventResponse = processEventData(eventResponse)

                // Update the StateFlow with the processed event data
                _eventData.value = eventResponse
                Log.d("EventController", "Updated _eventData StateFlow. New value: Title=${_eventData.value?.title}, Image=${_eventData.value?.image}")

                // Switch to main thread for UI updates related to event popup visibility
                withContext(Dispatchers.Main) {
                    checkIfShouldShowEvent() // This will use the new _eventData.value
                }
            } catch (e: Exception) {
                Log.e("EventController", "Exception while fetching event from login data: ${e.message}")
                e.printStackTrace()
                // Do not create a mock event on exception
                _eventData.value = null
            }
        }
    }

    // Helper function to check if event response is empty
    private fun isEventResponseEmpty(event: com.archeGlobal.one.model.EventResponse?): Boolean {
        if (event == null) return true
        
        // Check if all important fields are null or empty
        val hasTitle = !event.title.isNullOrBlank()
        val hasDescription = !event.description.isNullOrBlank()
        val hasImage = !event.image.isNullOrBlank()
        val hasDate = !event.date.isNullOrBlank()
        
        // Event is considered empty if it has no meaningful content
        val isEmpty = !hasTitle && !hasDescription && !hasImage && !hasDate
        
        Log.d("EventController", "Event emptiness check - hasTitle: $hasTitle, hasDescription: $hasDescription, hasImage: $hasImage, hasDate: $hasDate, isEmpty: $isEmpty")
        
        return isEmpty
    }

    private fun checkIfShouldShowEvent() {
        // If it's Pride Month, don't show regular events and don't auto-show Pride Month dialog
        if (_isPrideMonth.value) {
            Log.d("EventController", "It's Pride Month, not showing regular events or auto Pride Month dialog")
            return
        }

        // Only show the event if we have event data and it's not empty
        val currentEventData = _eventData.value
        if (currentEventData == null || isEventResponseEmpty(currentEventData)) {
            Log.d("EventController", "Event data is null or empty, not showing popup")
            _showEventPopup.value = false
            return
        }

        Log.d("EventController", "Event data available: Title=${currentEventData.title}, Image=${currentEventData.image}")

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastShownDate = sharedPref.getString(KEY_LAST_SHOWN_DATE, "")
        val currentDate = getCurrentDate()

        Log.d("EventController", "Last shown date: '$lastShownDate', Current date: '$currentDate'")

        // Show popup if it hasn't been shown today
        if (lastShownDate != currentDate) {
            Log.d("EventController", "Setting showEventPopup to TRUE - not shown today yet")
            _showEventPopup.value = true

            // For debugging purposes, let's log the current state
            Log.d("EventController", "Current state - showEventPopup: ${_showEventPopup.value}, eventData: ${_eventData.value != null}")
        } else {
            Log.d("EventController", "Setting showEventPopup to FALSE - already shown today")
            _showEventPopup.value = false
        }
    }

    fun dismissEventPopup() {
        Log.d("EventController", "dismissEventPopup called")

        // Save the current date as the last shown date
        val currentDate = getCurrentDate()
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        Log.d("EventController", "Saving last shown date: $currentDate")

        with(sharedPref.edit()) {
            putString(KEY_LAST_SHOWN_DATE, currentDate)
            apply()
        }

        // Verify the date was saved correctly
        val savedDate = sharedPref.getString(KEY_LAST_SHOWN_DATE, "")
        Log.d("EventController", "Verified saved date: $savedDate")

        // Hide the popup
        _showEventPopup.value = false
        Log.d("EventController", "Set showEventPopup to false")
    }

    // Format date as yyyy-MM-dd
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Check if current month is June (Pride Month)
    open fun checkIfPrideMonth() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)

        // June is month 5 in Calendar (0-based index)
        _isPrideMonth.value = currentMonth == Calendar.JUNE

        Log.d("EventController", "Current month: ${currentMonth + 1}, Is Pride Month: ${_isPrideMonth.value}")
    }

    // Check if we should show the Pride Month dialog
    fun checkIfShouldShowPrideMonthDialog() {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val prideMonthShown = sharedPref.getBoolean(KEY_PRIDE_MONTH_SHOWN, false)

        // Show Pride Month dialog if it hasn't been shown yet during this Pride Month
        if (!prideMonthShown) {
            Log.d("EventController", "Setting showPrideMonthDialog to TRUE - not shown yet this Pride Month")
            _showPrideMonthDialog.value = true

            // Mark as shown
            with(sharedPref.edit()) {
                putBoolean(KEY_PRIDE_MONTH_SHOWN, true)
                apply()
            }
        } else {
            Log.d("EventController", "Not showing Pride Month dialog - already shown this Pride Month")
        }
    }

    // Force show Pride Month dialog (triggered by pin)
    fun showPrideMonthDialog() {
        _showPrideMonthDialog.value = true
    }

    // Dismiss Pride Month dialog
    fun dismissPrideMonthDialog() {
        Log.d("EventController", "dismissPrideMonthDialog called")
        _showPrideMonthDialog.value = false
    }

    // Toggle between default and Pride Month launcher icons by enabling/disabling
    // activity-alias components declared in AndroidManifest.xml
    fun togglePrideIcon() {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentlyUsingPrideIcon = sharedPref.getBoolean(KEY_USING_PRIDE_ICON, false)

        // Component names for the two launcher aliases
        val defaultAlias = android.content.ComponentName(
            context,
            "com.archeGlobal.one.SplashAlias"
        )
        val prideAlias = android.content.ComponentName(
            context,
            "com.archeGlobal.one.SplashAliasPride"
        )

        val pm = context.packageManager
        if (currentlyUsingPrideIcon) {
            // Switch back to default icon
            pm.setComponentEnabledSetting(
                defaultAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                prideAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        } else {
            // Switch to pride icon
            pm.setComponentEnabledSetting(
                prideAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                defaultAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        }

        // Persist the new state
        sharedPref.edit().putBoolean(KEY_USING_PRIDE_ICON, !currentlyUsingPrideIcon).apply()

        // Notify user – the launcher might take a moment to refresh
        val msg = if (currentlyUsingPrideIcon) {
            "Switched back to regular app icon"
        } else {
            "Switched to Pride app icon"
        }
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()

        // Close the dialog
        dismissPrideMonthDialog()
    }

    // Check if we're using the Pride icon
    fun isUsingPrideIcon(): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_USING_PRIDE_ICON, false)
    }

    var model by mutableStateOf(
        HomeModel(
            userName = OtpVerificationController.getUserData()?.name ?: "",
            designation = OtpVerificationController.getUserData()?.designation ?: "",
            department = OtpVerificationController.getUserData()?.department ?: "",
            employeeId = OtpVerificationController.getUserData()?.employeeId ?: "",
            profilePicture = OtpVerificationController.getUserData()?.profilePic,
            showAllApps = true,
            categories = OtpVerificationController.getUserData()?.let { userData ->
                userData.services
                    .groupBy { it.category }
                    .toSortedMap(
                        Comparator { a, b ->
                            // If either is MyApps, handle special case
                            when {
                                a.equals("MyApps", ignoreCase = true) && !b.equals("MyApps", ignoreCase = true) -> 1
                                !a.equals("MyApps", ignoreCase = true) && b.equals("MyApps", ignoreCase = true) -> -1
                                else -> a.compareTo(b, ignoreCase = true)
                            }
                        }
                    )
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
        )
    )
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
                    val intent = Intent(context, HolidayCalendarActivity::class.java)
                    context.startActivity(intent)
                }
                "greetings" -> {
                    val intent = Intent(context, GreetingsActivity::class.java)
                    context.startActivity(intent)
                }
                "policy" -> {
                    val intent = Intent(context, PolicyActivity::class.java)
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
                "zentask" -> {
                    Log.d("HomeController", "Navigating to Zentask")
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
                "mypay" -> navigator.navigateToMyPay()
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
                "sos" -> {
                    Log.d("SOS", "Navigating to SOS")
                    navigator.navigateToSOS(false)
                }
                "travel" -> {
                    Log.d("HomeController", "Navigating to Travel Screen")
                    navigator.navigateToTravel()
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
                } else -> {
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
                    .toSortedMap(
                        Comparator { a, b ->
                            // If either is MyApps, handle special case
                            when {
                                a.equals("MyApps", ignoreCase = true) && !b.equals("MyApps", ignoreCase = true) -> 1
                                !a.equals("MyApps", ignoreCase = true) && b.equals("MyApps", ignoreCase = true) -> -1
                                else -> a.compareTo(b, ignoreCase = true)
                            }
                        }
                    )
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
