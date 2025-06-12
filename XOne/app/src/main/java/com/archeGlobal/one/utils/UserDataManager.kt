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
import com.archeGlobal.one.network.Office
import com.archeGlobal.one.network.VerifyOtpResponse
import com.google.gson.Gson

/**
 * Singleton class to manage user data throughout the application.
 * This ensures all components have access to the same user data instance.
 */
class UserDataManager private constructor(context: Context) {
    
    private val preferencesManager = PreferencesManager(context.applicationContext)
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
    private var lastUsername: String? = null
    // Private var isLoggedIn: Boolean = false
    // private var hasLoggedIn: Boolean = false
    
    private val PREF_LAST_LOGIN_TIME = "last_login_time"
    
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
        
        Log.d(TAG, "Loaded data from preferences - User: ${userData != null}, " +
                "Offices: ${officesData?.size ?: 0}, " +
                "Policies: ${policiesData?.size ?: 0}, " +
                "SosBlogs: ${sosBlogsData?.size ?: 0}, " +
                "AssetDetails: ${assetDetails?.size ?: 0}, " +
                "Communique: ${communiqueData?.size ?: 0}, " +
                "Greetings: ${greetingsData?.size ?: 0}, " +
                "GreetingCategories: ${greetingCategoriesData?.size ?: 0}")
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
    
    fun getAuthToken(): String? = preferencesManager.getAuthToken()
    
    fun getLastLoginTime(): Long? = preferencesManager.getLong(PREF_LAST_LOGIN_TIME)
    
    fun saveUserDataFromResponse(response: VerifyOtpResponse, token: String) {
        preferencesManager.saveAuthToken(token)
        preferencesManager.saveLong(PREF_LAST_LOGIN_TIME, System.currentTimeMillis())
        
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
        
        Log.d(TAG, "Saved user data to preferences: ${newUserData?.name}")
        Log.d(TAG, "Saved greetings data with ${greetingsData?.size} categories.")
        greetingsData?.forEach { (category, urls) ->
            Log.d(TAG, "Category '$category' has ${urls.size} greetings.")
        }
        Log.d(TAG, "Saved greeting categories with ${greetingCategoriesData?.size} items with messages.")
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
    lastUsername = null

    // Clear persistent storage
    preferencesManager.clearAllUserData()
    preferencesManager.setString("lastUsername", "")
    preferencesManager.clearBiometricCredentials()
    Log.d(TAG, "User data cleared from both memory and preferences")
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
        eventData = null
        preferencesManager.clearAllUserData()
        preferencesManager.clearAuthToken()
        preferencesManager.saveEventData(null) // Clear event data from preferences
        Log.d(TAG, "Cleared all user data on logout")
    }
    
    companion object {
        private const val TAG = "UserDataManager"
        
        @Volatile
        private var INSTANCE: UserDataManager? = null
        
        fun getInstance(context: Context): UserDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDataManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}