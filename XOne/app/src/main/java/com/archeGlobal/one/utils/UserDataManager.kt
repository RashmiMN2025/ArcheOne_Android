package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.AssetDetails
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
        
        Log.d(TAG, "Loaded data from preferences - User: ${userData != null}, " +
                "Offices: ${officesData?.size ?: 0}, " +
                "Policies: ${policiesData?.size ?: 0}, " +
                "SosBlogs: ${sosBlogsData?.size ?: 0}, " +
                "AssetDetails: ${assetDetails?.size ?: 0}")
    }
    
    fun getUserData(): UserData? = userData
    
    fun getOfficesData(): List<Office>? = officesData
    
    fun getPoliciesData(): List<PolicyModel.Policy>? = policiesData
    
    fun getSosBlogsData(): List<SosBlogModel>? = sosBlogsData
    
    fun getAssetDetails(): List<AssetDetail>? = assetDetails
    
    fun isLoggedIn(): Boolean = preferencesManager.isLoggedIn()
    
    fun getAuthToken(): String? = preferencesManager.getAuthToken()
    
    fun saveUserDataFromResponse(response: VerifyOtpResponse, token: String) {
        // Save token
        preferencesManager.saveAuthToken(token)
        
        // Create UserData object
        val newUserData = response.user?.let {
            UserData(
                name = it.name,
                designation = it.designation,
                department = it.department,
                employeeId = it.employeeid,
                email = it.email,
                mobile = it.mobile,
                location = it.location,
                services = response.services ?: emptyList(),
                profilePic = response.profile_pic,
                sosContact = response.sos,
                userDetails = it.userDetails
            )
        }
        
        // Process policies
        val newPoliciesData = response.policiesList.map { policy ->
            PolicyModel.Policy(
                policyName = policy.policyName,
                filePath = policy.filePath,
                showSosButton = policy.showSosButton,
                previewUrl = policy.previewUrl
            )
        }
        
        // Process SOS blogs
        val newSosBlogsData = response.sosBlogs?.map { sosBlog ->
            SosBlogModel(
                name = sosBlog.name,
                description = sosBlog.description,
                imageUrl = sosBlog.imageUrl,
                details = sosBlog.details
            )
        } ?: emptyList()
        
        // Update in-memory cache
        userData = newUserData
        officesData = response.offices
        policiesData = newPoliciesData
        sosBlogsData = newSosBlogsData
        assetDetails = response.assetDetails
        
        // Save to persistent storage
        preferencesManager.saveUserData(newUserData)
        preferencesManager.saveOfficesData(response.offices)
        preferencesManager.savePoliciesData(newPoliciesData)
        preferencesManager.saveSosBlogsData(newSosBlogsData)
        preferencesManager.saveAssetDetails(response.assetDetails)
        
        Log.d(TAG, "Saved user data to preferences: ${newUserData?.name}")
    }
    
    fun clearUserData() {
        // Clear in-memory cache
        userData = null
        officesData = null
        policiesData = null
        sosBlogsData = null
        assetDetails = null
        
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