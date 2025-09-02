package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.ApiGreetingCategory
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.network.AssetDetail
import com.archeGlobal.one.network.FAQCategory
import com.archeGlobal.one.network.Office
import com.archeGlobal.one.network.SmartCollateralCategory
import com.archeGlobal.one.network.VerifyOtpResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Singleton class to manage user data throughout the application.
 * This ensures all components have access to the same user data instance.
 */
class UserDataManager private constructor(context: Context) {

    val preferencesManager = PreferencesManager(context.applicationContext)
    private val gson = Gson()

    // In-memory cached data for quick access
    private var userData: UserData? = null
    private var officesData: List<Office>? = null
    private var policiesData: List<PolicyModel.Policy>? = null
    private var sosBlogsData: List<SosBlogModel>? = null
    private var assetDetails: List<AssetDetail>? = null
    private var communiqueData: List<CommuniqueModel.Communique>? = null
    private var greetingsData: Map<String, List<String>>? = null
    private var greetingCategoriesData: List<ApiGreetingCategory>? = null
    private var eventData: EventResponse? = null
    private var faqData: List<FAQCategory>? = null
    private var whatsNewData: List<com.archeGlobal.one.network.WhatsNewItem>? = null
    private var lastUsername: String? = null
    private var smartCollateralList: List<SmartCollateralCategory>? = null
    private var smartCollateralData: List<SmartCollateralCategory>? = null
    // Private var isLoggedIn: Boolean = false
    // private var hasLoggedIn: Boolean = false

    // Callback for when user data becomes ready
    private var onUserDataReadyCallbacks: MutableList<() -> Unit> = mutableListOf()

    fun saveSmartCollateral(list: List<SmartCollateralCategory>) {
        smartCollateralList = list
        preferencesManager.saveSmartCollateral(list) // Optional: persist if desired
    }

    fun getSmartCollateralList(): List<SmartCollateralCategory>? = smartCollateralList

    // Check if user data is ready and available
    fun isUserDataReady(): Boolean {
        val isReady = userData?.name?.isNotBlank() == true
        Log.d(TAG, "UserDataManager.isUserDataReady(): $isReady (userData.name: '${userData?.name}')")
        return isReady
    }

//    fun getSmartCollateralData(): List<SmartCollateralCategory>? {
//        // Assuming you have saved this data to preferences similarly
//        return preferencesManager.getSmartCollateralData()
//    }

    fun getSmartCollateralData(): List<SmartCollateralCategory>? = smartCollateralData


    private val PREF_LAST_LOGIN_TIME = "last_login_time"

    companion object {
        private const val TAG = "UserDataManager"

        @Volatile
        private var INSTANCE: UserDataManager? = null

        fun getInstance(context: Context): UserDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDataManager(context).also { INSTANCE = it }
            }
        }
    }

    init {
        // Load data from SharedPreferences on initialization
        loadDataFromPreferences()
        Log.d(TAG, "UserDataManager initialized with data: ${userData?.name}")
    }

    private fun loadDataFromPreferences() {
        userData = preferencesManager.getUserData()
        officesData = preferencesManager.getOfficesData()
        policiesData = preferencesManager.getPoliciesData()
        sosBlogsData = preferencesManager.getSosBlogsData()
        assetDetails = preferencesManager.getAssetDetails()
        communiqueData = preferencesManager.getCommuniqueData()
        greetingsData = preferencesManager.getGreetings()
        greetingCategoriesData = preferencesManager.getGreetingCategories()
        faqData = preferencesManager.getFAQData()

        // Load event data from preferences
        val eventDataJson = preferencesManager.getEventData()
        if (eventDataJson != null) {
            try {
                eventData = gson.fromJson(eventDataJson, EventResponse::class.java)
                Log.d(TAG, "Loaded event data from preferences: ${eventData?.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing event data from preferences: ${e.message}")
                eventData = null
            }
        } else {
            Log.d(TAG, "No event data found in preferences")
            eventData = null
        }

        Log.d(
            TAG,
            "Loaded data from preferences - User: ${userData != null}, " +
                "Offices: ${officesData?.size ?: 0}, " +
                "Policies: ${policiesData?.size ?: 0}, " +
                "SosBlogs: ${sosBlogsData?.size ?: 0}, " +
                "AssetDetails: ${assetDetails?.size ?: 0}, " +
                "Communique: ${communiqueData?.size ?: 0}, " +
                "Greetings: ${greetingsData?.size ?: 0}, " +
                "GreetingCategories: ${greetingCategoriesData?.size ?: 0}, " +
                "FAQ: ${faqData?.size ?: 0}"
        )
    }

    fun setIsLoggedIn(value: Boolean) {
        preferencesManager.setBoolean("isLoggedIn", value)
    }

    fun isLoggedIn(): Boolean = preferencesManager.getBoolean("isLoggedIn", false)

    fun setHasLoggedIn(value: Boolean) {
        preferencesManager.setBoolean("hasLoggedIn", value)
    }

    fun hasUserLoggedIn(): Boolean = preferencesManager.getBoolean("hasLoggedIn", false)

    fun setLastUsername(username: String?) {
        lastUsername = username
        preferencesManager.setString("lastUsername", username ?: "")
    }

    fun getLastUsername(): String? {
        if (lastUsername == null) {
            val stored = preferencesManager.getString("lastUsername", "")
            lastUsername = if (stored.isNullOrEmpty()) null else stored
        }
        return lastUsername
    }

    fun getUserData(): UserData? = userData

    fun getOfficesData(): List<Office>? = officesData

    fun getPoliciesData(): List<PolicyModel.Policy>? = policiesData

    fun getSosBlogsData(): List<SosBlogModel>? = sosBlogsData

    fun getAssetDetails(): List<AssetDetail>? = assetDetails

    fun getCommuniqueData(): List<CommuniqueModel.Communique>? = communiqueData

    fun getGreetingsData(): Map<String, List<String>>? = greetingsData

    fun getFAQData(): List<FAQCategory>? = faqData

    fun getWhatsNewData(): List<com.archeGlobal.one.network.WhatsNewItem>? = whatsNewData

    fun getAuthToken(): String? = preferencesManager.getAuthToken()

    fun getLastLoginTime(): Long? = preferencesManager.getLong(PREF_LAST_LOGIN_TIME)

    fun saveUserDataFromResponse(response: VerifyOtpResponse, token: String) {
        preferencesManager.saveAuthToken(token)
        preferencesManager.saveLong(PREF_LAST_LOGIN_TIME, System.currentTimeMillis())

        // Always preserve last user credentials for re-authentication
        response.user?.let { user ->
            preferencesManager.setString("last_user_email", user.email ?: "")
            preferencesManager.setString("last_user_mobile", user.mobile ?: "")
            preferencesManager.setString("last_user_employee_id", user.employeeid ?: "")
            preferencesManager.setString("last_user_name", user.name ?: "")
        }

        Log.d(TAG, "UserDataManager: Refreshing in-memory cache after data save")

        // Process the greeting categories with messages from the new API format
        val apiGreetingCategories = response.greetingCategories1?.map { category ->
            ApiGreetingCategory(
                id = category.id,
                name = category.name,
                files = category.files,
                message = category.message,
                subfolder = category.subfolder // <-- fix: include subfolder
            )
        } ?: emptyList()

        // Create a map of greeting categories from the greetingCategories API response
        val fullGreetingsData = apiGreetingCategories.associate { category ->
            category.name to category.files
        }

        val newUserData = response.user?.let {
            UserData(
                name = it.name,
                designation = it.designation,
                department = it.department,
                employeeId = it.employeeid,
                email = it.email,
                mobile = it.mobile,
                location = it.location,
                services = response.services,
                profilePic = response.profile_pic,
                sosContact = response.sos,
                userDetails = it.userDetails,
                // Use the full greetings map here
                greetings = fullGreetingsData
            )
        }

        // Update in-memory cache
        userData = newUserData
        officesData = response.offices
        policiesData = response.policiesList
        sosBlogsData = response.sosBlogs.map { sosBlog ->
            SosBlogModel(
                name = sosBlog.name,
                description = sosBlog.description,
                imageUrl = sosBlog.imageUrl,
                details = sosBlog.details
            )
        }
        assetDetails = response.assetDetails
        communiqueData = response.communique.map { communique ->
            CommuniqueModel.Communique(
                communiqueName = communique.communiqueName,
                filePath = communique.filePath
            )
        }
        // Update in-memory caches for greetings
        greetingsData = fullGreetingsData
        greetingCategoriesData = apiGreetingCategories

        // Save FAQ data from login response
        faqData = response.faqList

        // Save WhatsNew data from login response
        whatsNewData = response.whatsNew

        // Save event data from login response
        eventData = response.eventData
        val localEventData = eventData // Use local variable to avoid smart cast issue
        if (localEventData != null) {
            Log.d(TAG, "EventResponse object (localEventData) found in login response. Title: ${localEventData.title}")
            // Save event data to preferences
            try {
                val eventDataJson = gson.toJson(localEventData)
                preferencesManager.saveEventData(eventDataJson)
                Log.d(TAG, "Saved EventResponse (localEventData) to preferences. Title: ${localEventData.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving EventResponse (localEventData) to preferences: ${e.message}")
            }
        } else {
            Log.d(TAG, "No event data found in login response")
        }

        smartCollateralData = response.smartCollateral

        // Save to persistent storage
        preferencesManager.saveUserData(newUserData)
        preferencesManager.saveOfficesData(response.offices)
        preferencesManager.savePoliciesData(response.policiesList)
        preferencesManager.saveSosBlogsData(sosBlogsData)
        preferencesManager.saveAssetDetails(response.assetDetails)
        preferencesManager.saveCommuniqueData(communiqueData)
        // Call the correct save function with the full greetings map
        preferencesManager.saveGreetingsList(greetingsData)
        // Save the new greeting categories with messages
        preferencesManager.saveGreetingCategories(greetingCategoriesData)
        // Save FAQ data to persistent storage
        preferencesManager.saveFAQData(faqData)
        preferencesManager.saveSmartCollateralData(response.smartCollateral)

        Log.d(TAG, "Saved user data to preferences: ${newUserData?.name}")
        Log.d(TAG, "Saved greetings data with ${greetingsData?.size} categories.")
        greetingsData?.forEach { (category, urls) ->
            Log.d(TAG, "Category '$category' has ${urls.size} greetings.")
        }
        Log.d(TAG, "Saved greeting categories with ${greetingCategoriesData?.size} items with messages.")
        
        // CRITICAL: Reload data from preferences to refresh in-memory cache
        // This ensures getUserData() immediately returns the fresh data on fresh install
        loadDataFromPreferences()
        Log.d(TAG, "UserDataManager: In-memory cache refreshed. getUserData() now returns: ${userData?.name}")
        
        // Notify all registered callbacks that user data is now ready
        if (isUserDataReady()) {
            Log.d(TAG, "User data is ready, notifying ${onUserDataReadyCallbacks.size} callbacks")
            onUserDataReadyCallbacks.forEach { callback ->
                try {
                    callback()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in user data ready callback", e)
                }
            }
            
            // Pre-warm network connections for DeskCart and TrackTickets APIs
            // This simulates the app restart behavior where connections are already established
            warmUpConnections()
        }
    }

    fun clearUserData() {
        // Clear in-memory cache
        userData = null
        officesData = null
        policiesData = null
        sosBlogsData = null
        assetDetails = null
        communiqueData = null
        greetingsData = null
        greetingCategoriesData = null
        faqData = null
        lastUsername = null

        // Clear persistent storage
        preferencesManager.clearAllUserData()
        preferencesManager.setString("lastUsername", "")
        preferencesManager.clearBiometricCredentials()
    }

    // Clear only session data but preserve MPIN and biometric data for re-authentication
    fun clearSessionData() {
        // Clear in-memory cache
        userData = null
        officesData = null
        policiesData = null
        sosBlogsData = null
        assetDetails = null
        communiqueData = null
        greetingsData = null
        greetingCategoriesData = null
        faqData = null

        // Clear session data but preserve MPIN and biometric data
        preferencesManager.clearSessionData()
        // Set logged in state to false but preserve hasLoggedIn to true
        setIsLoggedIn(false)
        // Don't clear lastUsername, MPIN, or biometric credentials
        Log.d(TAG, "Session data cleared from both memory and preferences, preserving MPIN and biometric credentials")
    }

    fun updateProfilePicture(profilePicUrl: String?) {
        // Get current user data
        val currentUserData = userData

        if (currentUserData != null && profilePicUrl != null) {
            // Create updated user data with the new profile picture URL
            val updatedUserData = currentUserData.copy(profilePic = profilePicUrl)

            // Update in-memory cache
            userData = updatedUserData

            // Save to persistent storage
            preferencesManager.saveUserData(updatedUserData)

            // Clear any cached profile data
            try {
                // This will help force a fresh download next time
                val timestamp = System.currentTimeMillis()
                preferencesManager.setProfileUpdateTimestamp(timestamp)
                Log.d(TAG, "Updated profile picture URL: $profilePicUrl with timestamp: $timestamp")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile cache timestamp", e)
            }
        } else {
            Log.e(TAG, "Cannot update profile picture: User data is null or profilePicUrl is null")
        }
    }

    fun getGreetings(): Map<String, List<String>>? = greetingsData

    fun getGreetingCategoriesData(): List<ApiGreetingCategory>? = greetingCategoriesData

    /**
     * Get service URL by service name
     */
    fun getServiceUrl(serviceName: String): String? {
        return userData?.services?.find { it.service.equals(serviceName, ignoreCase = true) }?.url
    }

    fun getEventData(): EventResponse? {
        val localEventData = eventData // Use local variable to avoid smart cast issue
        if (localEventData != null) {
            Log.d(TAG, "Returning cached event data: ${localEventData.title}")
            return localEventData
        }

        // If not in memory, try to load from preferences
        val eventDataJson = preferencesManager.getEventData()
        if (eventDataJson != null) {
            try {
                val loadedEventData = gson.fromJson(eventDataJson, EventResponse::class.java)
                // Update in-memory cache
                eventData = loadedEventData
                Log.d(TAG, "Loaded event data from preferences: ${loadedEventData.title}")
                return loadedEventData
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing event data from preferences: ${e.message}")
            }
        } else {
            Log.d(TAG, "No event data found in preferences")
        }

        return null
    }

    fun logout() {
        userData = null
        officesData = null
        policiesData = null
        sosBlogsData = null
        assetDetails = null
        communiqueData = null
        greetingsData = null
        greetingCategoriesData = null
        faqData = null
        eventData = null
        preferencesManager.clearAllUserData()
        preferencesManager.clearAuthToken()
        preferencesManager.saveEventData(null) // Clear event data from preferences
        // Clear callbacks on logout
        onUserDataReadyCallbacks.clear()
        Log.d(TAG, "Cleared all user data on logout")
    }

    // Methods to manage user data ready callbacks
    fun addUserDataReadyCallback(callback: () -> Unit) {
        onUserDataReadyCallbacks.add(callback)
        Log.d(TAG, "Added user data ready callback. Total callbacks: ${onUserDataReadyCallbacks.size}")
        
        // If user data is already ready, call the callback immediately
        if (isUserDataReady()) {
            Log.d(TAG, "User data is already ready, calling callback immediately")
            try {
                callback()
            } catch (e: Exception) {
                Log.e(TAG, "Error in immediate callback", e)
            }
        }
    }

    fun removeUserDataReadyCallback(callback: () -> Unit) {
        onUserDataReadyCallbacks.remove(callback)
        Log.d(TAG, "Removed user data ready callback. Total callbacks: ${onUserDataReadyCallbacks.size}")
    }

    fun clearUserDataReadyCallbacks() {
        onUserDataReadyCallbacks.clear()
        Log.d(TAG, "Cleared all user data ready callbacks")
    }
    
    private fun warmUpConnections() {
        // Background thread to pre-warm network connections
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            warmUpConnectionsAsync()
        }, 200) // Small delay to let login complete fully
    }
    
    private fun warmUpConnectionsAsync() {
        try {
            Log.d(TAG, "Starting connection warm-up for DeskCart and TrackTickets APIs")
            
            // Use coroutine for async network calls
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userData = getUserData()
                    if (userData != null) {
                        // Warm up connections concurrently
                        val deskCartJob = async {
                            warmUpDeskCartConnection(userData.email ?: "")
                        }
                        val ticketsJob = async {
                            warmUpTrackTicketsConnection(userData.name ?: "")
                        }
                        
                        // Wait for both to complete (or timeout quickly)
                        withTimeoutOrNull(2000) {
                            deskCartJob.await()
                            ticketsJob.await()
                        }
                        Log.d(TAG, "Connection warm-up completed")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Connection warm-up completed with minor issues (expected): ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Connection warm-up failed (not critical): ${e.message}")
        }
    }
    
    private suspend fun warmUpDeskCartConnection(email: String) {
        try {
            Log.d(TAG, "Warming up DeskCart connection...")
            val request = com.archeGlobal.one.network.DeskCartEligibilityRequest(email = email)
            // Make the API call to establish HTTP connection (result doesn't matter)
            com.archeGlobal.one.network.RetrofitClient.apiService.getDeskCartEligibility(request)
            Log.d(TAG, "DeskCart connection warm-up completed")
        } catch (e: Exception) {
            Log.d(TAG, "DeskCart warm-up established connection: ${e.message}")
        }
    }
    
    private suspend fun warmUpTrackTicketsConnection(userName: String) {
        try {
            Log.d(TAG, "Warming up TrackTickets connection...")
            val request = com.archeGlobal.one.network.TicketsRequest(
                name = userName,
                category = "Helpdesk",
                subcategory = null
            )
            
            // TrackTickets uses callback-based Retrofit, so we need to convert to coroutine
            suspendCancellableCoroutine<Unit> { continuation ->
                com.archeGlobal.one.network.RetrofitClient.apiService.getTickets(request).enqueue(
                    object : retrofit2.Callback<com.archeGlobal.one.network.TicketsResponse> {
                        override fun onResponse(call: retrofit2.Call<com.archeGlobal.one.network.TicketsResponse>, response: retrofit2.Response<com.archeGlobal.one.network.TicketsResponse>) {
                            Log.d(TAG, "TrackTickets connection warm-up completed")
                            continuation.resume(Unit)
                        }
                        override fun onFailure(call: retrofit2.Call<com.archeGlobal.one.network.TicketsResponse>, t: Throwable) {
                            Log.d(TAG, "TrackTickets warm-up established connection: ${t.message}")
                            continuation.resume(Unit)
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "TrackTickets warm-up completed: ${e.message}")
        }
    }
}
