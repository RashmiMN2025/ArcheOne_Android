package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
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
        
        Log.d(TAG, "Loaded data from preferences - User: ${userData != null}, " +
                "Offices: ${officesData?.size ?: 0}, " +
                "Policies: ${policiesData?.size ?: 0}, " +
                "SosBlogs: ${sosBlogsData?.size ?: 0}")
    }
    
    fun getUserData(): UserData? = userData
    
    fun getOfficesData(): List<Office>? = officesData
    
    fun getPoliciesData(): List<PolicyModel.Policy>? = policiesData
    
    fun getSosBlogsData(): List<SosBlogModel>? = sosBlogsData
    
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
                filePath = policy.filePath
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
        
        // Save to persistent storage
        preferencesManager.saveUserData(newUserData)
        preferencesManager.saveOfficesData(response.offices)
        preferencesManager.savePoliciesData(newPoliciesData)
        preferencesManager.saveSosBlogsData(newSosBlogsData)
        
        Log.d(TAG, "Saved user data to preferences: ${newUserData?.name}")
    }
    
    fun clearUserData() {
        // Clear in-memory cache
        userData = null
        officesData = null
        policiesData = null
        sosBlogsData = null
        
        // Clear persistent storage
        preferencesManager.clearAllUserData()
        
        Log.d(TAG, "User data cleared from both memory and preferences")
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