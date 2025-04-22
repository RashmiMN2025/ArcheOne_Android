package com.archeGlobal.one.utils

import android.content.Context
import android.content.SharedPreferences
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.network.AssetDetail
import com.archeGlobal.one.network.Office
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "XOne_preferences",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveFavorites(favorites: Map<String, List<HomeItem>>) {
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun getFavorites(): Map<String, List<HomeItem>> {
        val json = sharedPreferences.getString(KEY_FAVORITES, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, List<HomeItem>>>() {}.type
            val favorites: Map<String, List<HomeItem>> = gson.fromJson(json, type)
            // Ensure favorites are sorted alphabetically by category
            favorites.toSortedMap(String.CASE_INSENSITIVE_ORDER)
        } else {
            emptyMap()
        }
    }
    
    // Save the authentication token
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    // Get the saved authentication token
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
    
    // Clear auth token on logout
    fun clearAuthToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }
    
    // Save user data
    fun saveUserData(userData: UserData?) {
        if (userData == null) {
            sharedPreferences.edit().remove(KEY_USER_DATA).apply()
        } else {
            val json = gson.toJson(userData)
            sharedPreferences.edit().putString(KEY_USER_DATA, json).apply()
        }
    }
    
    // Get user data
    fun getUserData(): UserData? {
        val json = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<UserData>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    // Save offices data
    fun saveOfficesData(offices: List<Office>?) {
        if (offices == null) {
            sharedPreferences.edit().remove(KEY_OFFICES_DATA).apply()
        } else {
            val json = gson.toJson(offices)
            sharedPreferences.edit().putString(KEY_OFFICES_DATA, json).apply()
        }
    }
    
    // Get offices data
    fun getOfficesData(): List<Office>? {
        val json = sharedPreferences.getString(KEY_OFFICES_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<Office>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    // Save policies data
    fun savePoliciesData(policies: List<PolicyModel.Policy>?) {
        if (policies == null) {
            sharedPreferences.edit().remove(KEY_POLICIES_DATA).apply()
        } else {
            val json = gson.toJson(policies)
            sharedPreferences.edit().putString(KEY_POLICIES_DATA, json).apply()
        }
    }
    
    // Get policies data
    fun getPoliciesData(): List<PolicyModel.Policy>? {
        val json = sharedPreferences.getString(KEY_POLICIES_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<PolicyModel.Policy>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    // Save SOS blogs data
    fun saveSosBlogsData(sosBlogs: List<SosBlogModel>?) {
        if (sosBlogs == null) {
            sharedPreferences.edit().remove(KEY_SOS_BLOGS_DATA).apply()
        } else {
            val json = gson.toJson(sosBlogs)
            sharedPreferences.edit().putString(KEY_SOS_BLOGS_DATA, json).apply()
        }
    }
    
    // Get SOS blogs data
    fun getSosBlogsData(): List<SosBlogModel>? {
        val json = sharedPreferences.getString(KEY_SOS_BLOGS_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<SosBlogModel>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    // Save asset details
    fun saveAssetDetails(assetDetails: List<AssetDetail>?) {
        if (assetDetails == null) {
            sharedPreferences.edit().remove(KEY_ASSET_DETAILS).apply()
        } else {
            val json = gson.toJson(assetDetails)
            sharedPreferences.edit().putString(KEY_ASSET_DETAILS, json).apply()
        }
    }
    
    // Get asset details
    fun getAssetDetails(): List<AssetDetail>? {
        val json = sharedPreferences.getString(KEY_ASSET_DETAILS, null)
        return if (json != null) {
            val type = object : TypeToken<List<AssetDetail>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    // Clear all user-related data on logout
    fun clearAllUserData() {
        sharedPreferences.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_DATA)
            remove(KEY_OFFICES_DATA)
            remove(KEY_POLICIES_DATA)
            remove(KEY_SOS_BLOGS_DATA)
            remove(KEY_ASSET_DETAILS)
        }.apply()
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_OFFICES_DATA = "offices_data"
        private const val KEY_POLICIES_DATA = "policies_data"
        private const val KEY_SOS_BLOGS_DATA = "sos_blogs_data"
        private const val KEY_ASSET_DETAILS = "asset_details"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_PROFILE_UPDATE_TIMESTAMP = "profile_update_timestamp"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_EMAIL = "biometric_email"
        private const val KEY_BIOMETRIC_MOBILE = "biometric_mobile"
        private const val KEY_BIOMETRIC_EMPLOYEE_ID = "biometric_employee_id"
    }
    
    // Check if this is the first launch of the app
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }
    
    // Mark that the app has been launched before
    fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
    }

    // Store profile update timestamp
    fun setProfileUpdateTimestamp(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_PROFILE_UPDATE_TIMESTAMP, timestamp).apply()
    }

    // Get the profile update timestamp
    fun getProfileUpdateTimestamp(): Long {
        return sharedPreferences.getLong(KEY_PROFILE_UPDATE_TIMESTAMP, 0)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun saveBiometricCredentials(email: String, mobile: String, employeeId: String) {
        sharedPreferences.edit().apply {
            putString(KEY_BIOMETRIC_EMAIL, email)
            putString(KEY_BIOMETRIC_MOBILE, mobile)
            putString(KEY_BIOMETRIC_EMPLOYEE_ID, employeeId)
        }.apply()
    }

    fun getBiometricCredentials(): Triple<String, String, String>? {
        val email = sharedPreferences.getString(KEY_BIOMETRIC_EMAIL, null)
        val mobile = sharedPreferences.getString(KEY_BIOMETRIC_MOBILE, null)
        val employeeId = sharedPreferences.getString(KEY_BIOMETRIC_EMPLOYEE_ID, null)
        
        return if (email != null && mobile != null && employeeId != null) {
            Triple(email, mobile, employeeId)
        } else null
    }

    fun clearBiometricData() {
        sharedPreferences.edit().apply {
            remove(KEY_BIOMETRIC_ENABLED)
            remove(KEY_BIOMETRIC_EMAIL)
            remove(KEY_BIOMETRIC_MOBILE)
            remove(KEY_BIOMETRIC_EMPLOYEE_ID)
        }.apply()
    }
} 