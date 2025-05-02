package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.AssetDetails
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.network.AssetDetail
import com.archeGlobal.one.network.Office
import com.archeGlobal.one.network.VerifyOtpResponse

/**
 * Singleton class to manage user data throughout the application.
 * This ensures all components have access to the same user data instance.
 */
class UserDataManager private constructor(context: Context) {
    
    private val preferencesManager = PreferencesManager(context.applicationContext)
    
    // In-memory cached data for quick access
    private var userData: UserData? = null
    private var officesData: List<Office>? = null
    private var policiesData: List<PolicyModel.Policy>? = null 
    private var sosBlogsData: List<SosBlogModel>? = null
    private var assetDetails: List<AssetDetail>? = null
    private var communiqueData: List<CommuniqueModel.Communique>? = null
    private var greetingsData: Map<String, List<String>>? = null
    
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
        
        Log.d(TAG, "Loaded data from preferences - User: ${userData != null}, " +
                "Offices: ${officesData?.size ?: 0}, " +
                "Policies: ${policiesData?.size ?: 0}, " +
                "SosBlogs: ${sosBlogsData?.size ?: 0}, " +
                "AssetDetails: ${assetDetails?.size ?: 0}, " +
                "Communique: ${communiqueData?.size ?: 0}, " +
                "Greetings: ${greetingsData?.size ?: 0}")
    }
    
    fun getUserData(): UserData? = userData
    
    fun getOfficesData(): List<Office>? = officesData
    
    fun getPoliciesData(): List<PolicyModel.Policy>? = policiesData
    
    fun getSosBlogsData(): List<SosBlogModel>? = sosBlogsData
    
    fun getAssetDetails(): List<AssetDetail>? = assetDetails
    
    fun getCommuniqueData(): List<CommuniqueModel.Communique>? = communiqueData
    
    fun getGreetingsData(): Map<String, List<String>>? = greetingsData
    
    fun isLoggedIn(): Boolean = preferencesManager.isLoggedIn()
    
    fun getAuthToken(): String? = preferencesManager.getAuthToken()
    
    fun getLastLoginTime(): Long? = preferencesManager.getLong(PREF_LAST_LOGIN_TIME)
    
    fun saveUserDataFromResponse(response: VerifyOtpResponse, token: String) {
        preferencesManager.saveAuthToken(token)
        preferencesManager.saveLong(PREF_LAST_LOGIN_TIME, System.currentTimeMillis())
        
        // Correctly assign the full list of greetings from the response
        val fullGreetingsData = response.greetings ?: emptyMap()
        
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
        // Update in-memory greetings cache with the full map
        greetingsData = fullGreetingsData
        
        // Save to persistent storage
        preferencesManager.saveUserData(newUserData)
        preferencesManager.saveOfficesData(response.offices)
        preferencesManager.savePoliciesData(response.policiesList)
        preferencesManager.saveSosBlogsData(sosBlogsData)
        preferencesManager.saveAssetDetails(response.assetDetails)
        preferencesManager.saveCommuniqueData(communiqueData)
        // Call the correct save function with the full greetings map
        preferencesManager.saveGreetingsList(greetingsData)
        
        Log.d(TAG, "Saved user data to preferences: ${newUserData?.name}")
        Log.d(TAG, "Saved greetings data with ${greetingsData?.size} categories.")
        greetingsData?.forEach { (category, urls) ->
            Log.d(TAG, "Category '$category' has ${urls.size} greetings.")
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
        
        // Clear persistent storage
        preferencesManager.clearAllUserData()
        
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
    
    fun logout() {
        userData = null
        officesData = null
        policiesData = null
        sosBlogsData = null
        assetDetails = null
        communiqueData = null
        greetingsData = null
        preferencesManager.clearAllUserData()
        preferencesManager.clearAuthToken()
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