package com.archeGlobal.one.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.archeGlobal.one.model.ApiGreetingCategory
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.network.AssetDetail
import com.archeGlobal.one.network.FAQCategory
import com.archeGlobal.one.network.Office
import com.archeGlobal.one.network.SmartCollateralCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(
    context: Context,
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            "XOne_preferences",
            Context.MODE_PRIVATE,
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
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).commit()
    }

    // Get the saved authentication token
    fun getAuthToken(): String? = sharedPreferences.getString(KEY_AUTH_TOKEN, null)

    // Check if this is the first launch of the app
    fun isFirstLaunch(): Boolean = sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)

    // Check if user is logged in
    fun isLoggedIn(): Boolean = getAuthToken() != null

    // Clear auth token on logout
    fun clearAuthToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    // Save user data
    fun saveUserData(userData: UserData?) {
        if (userData == null) {
            sharedPreferences.edit().remove(KEY_USER_DATA).commit()
        } else {
            val json = gson.toJson(userData)
            sharedPreferences.edit().putString(KEY_USER_DATA, json).commit()
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
            sharedPreferences.edit().remove(KEY_OFFICES_DATA).commit()
        } else {
            val json = gson.toJson(offices)
            sharedPreferences.edit().putString(KEY_OFFICES_DATA, json).commit()
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
            sharedPreferences.edit().remove(KEY_POLICIES_DATA).commit()
        } else {
            val json = gson.toJson(policies)
            sharedPreferences.edit().putString(KEY_POLICIES_DATA, json).commit()
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
            sharedPreferences.edit().remove(KEY_SOS_BLOGS_DATA).commit()
        } else {
            val json = gson.toJson(sosBlogs)
            sharedPreferences.edit().putString(KEY_SOS_BLOGS_DATA, json).commit()
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
            sharedPreferences.edit().remove(KEY_ASSET_DETAILS).commit()
        } else {
            val json = gson.toJson(assetDetails)
            sharedPreferences.edit().putString(KEY_ASSET_DETAILS, json).commit()
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

    // Save communique data
    fun saveCommuniqueData(communique: List<CommuniqueModel.Communique>?) {
        if (communique == null) {
            sharedPreferences.edit().remove(KEY_COMMUNIQUE_DATA).commit()
        } else {
            val json = gson.toJson(communique)
            sharedPreferences.edit().putString(KEY_COMMUNIQUE_DATA, json).commit()
        }
    }

    // Get communique data
    fun getCommuniqueData(): List<CommuniqueModel.Communique>? {
        val json = sharedPreferences.getString(KEY_COMMUNIQUE_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<CommuniqueModel.Communique>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    // Save FAQ data
    fun saveFAQData(faqData: List<FAQCategory>?) {
        if (faqData == null) {
            sharedPreferences.edit().remove(KEY_FAQ_DATA).commit()
        } else {
            val json = gson.toJson(faqData)
            sharedPreferences.edit().putString(KEY_FAQ_DATA, json).commit()
        }
    }

    // Get FAQ data
    fun getFAQData(): List<FAQCategory>? {
        val json = sharedPreferences.getString(KEY_FAQ_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<FAQCategory>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    // Clear all user-related data on logout
    fun clearAllUserData() {
        sharedPreferences
            .edit()
            .apply {
                remove(KEY_AUTH_TOKEN)
                remove(KEY_USER_DATA)
                remove(KEY_OFFICES_DATA)
                remove(KEY_POLICIES_DATA)
                remove(KEY_SOS_BLOGS_DATA)
                remove(KEY_ASSET_DETAILS)
                remove(KEY_COMMUNIQUE_DATA)
                remove(KEY_FAQ_DATA)
                remove(KEY_EVENT_DATA)
                remove(KEY_APP_LOCKED) // Clear app lock state when session expires
                remove(KEY_BIOMETRIC_ENABLED) // Clear biometric settings
                remove(KEY_BIOMETRIC_EMAIL)
                remove(KEY_BIOMETRIC_MOBILE)
                remove(KEY_BIOMETRIC_EMPLOYEE_ID)
                // Clear punch state so a new user gets their own data from login API
                remove(KEY_IS_PUNCHED_IN)
                remove(KEY_PUNCH_IN_TIME)
                remove(KEY_PUNCH_IN_DATE_TIME)
                remove(KEY_PUNCH_ID)
                remove(KEY_PUNCH_OUT_TIME)
                remove(KEY_LAST_PUNCH_DATE)
                remove(KEY_TIME_SPENT)
            }.apply()

        // Update the locked state flow
        _lockedState.value = false
    }

    // Clear only session data but preserve MPIN and biometric data for re-authentication
    fun clearSessionData() {
        sharedPreferences
            .edit()
            .apply {
                remove(KEY_AUTH_TOKEN)
                remove(KEY_USER_DATA)
                remove(KEY_OFFICES_DATA)
                remove(KEY_POLICIES_DATA)
                remove(KEY_SOS_BLOGS_DATA)
                remove(KEY_ASSET_DETAILS)
                remove(KEY_COMMUNIQUE_DATA)
                remove(KEY_FAQ_DATA)
                remove(KEY_EVENT_DATA)
                remove(KEY_APP_LOCKED) // Clear app lock state when session expires
                remove(KEY_SEEN_SERVICES) // Clear seen services so New stickers can appear again
                remove(KEY_INSTALL_TYPE) // Clear install type so it can be determined fresh on next login
                // Clear punch state so a new user gets their own data from login API
                remove(KEY_IS_PUNCHED_IN)
                remove(KEY_PUNCH_IN_TIME)
                remove(KEY_PUNCH_IN_DATE_TIME)
                remove(KEY_PUNCH_ID)
                remove(KEY_PUNCH_OUT_TIME)
                remove(KEY_LAST_PUNCH_DATE)
                remove(KEY_TIME_SPENT)
                // Keep MPIN and biometric data for re-authentication
                // remove(KEY_BIOMETRIC_ENABLED) - Keep this
                // remove(KEY_BIOMETRIC_EMAIL) - Keep this
                // remove(KEY_BIOMETRIC_MOBILE) - Keep this
                // remove(KEY_BIOMETRIC_EMPLOYEE_ID) - Keep this
                // remove(KEY_BIOMETRIC_TOKEN) - Keep this temporarily for re-auth
                // Keep last user data for re-authentication
                // remove("last_user_email") - Keep this
                // remove("last_user_mobile") - Keep this
                // remove("last_user_employee_id") - Keep this
                // remove("last_user_name") - Keep this
            }.apply()

        // Update the locked state flow
        _lockedState.value = false
    }

    // Locked state management with MutableStateFlow for better reactivity
    private val _lockedState = MutableStateFlow(getAppLockState())
    val lockedState: StateFlow<Boolean> = _lockedState.asStateFlow()

    // Check if app is locked
    fun getAppLockState(): Boolean = sharedPreferences.getBoolean(KEY_APP_LOCKED, false)

    // Set app locked state
    fun setAppLockState(locked: Boolean) {
        Log.d("PreferencesManager", "Setting locked state to: $locked")
        sharedPreferences.edit().putBoolean(KEY_APP_LOCKED, locked).apply()
        _lockedState.value = locked
    }

    // App version and service tracking methods
    fun getAppVersion(): String = sharedPreferences.getString(KEY_APP_VERSION, "") ?: ""

    fun setAppVersion(version: String) {
        sharedPreferences.edit().putString(KEY_APP_VERSION, version).apply()
    }

    fun getSeenServices(): Set<String> = sharedPreferences.getStringSet(KEY_SEEN_SERVICES, emptySet()) ?: emptySet()

    fun addSeenService(serviceName: String) {
        val seenServices = getSeenServices().toMutableSet()
        seenServices.add(serviceName)
        sharedPreferences.edit().putStringSet(KEY_SEEN_SERVICES, seenServices).apply()
    }

    fun markAllServicesAsSeen(serviceNames: List<String>) {
        val seenServices = getSeenServices().toMutableSet()
        seenServices.addAll(serviceNames)
        sharedPreferences.edit().putStringSet(KEY_SEEN_SERVICES, seenServices).apply()
    }

    fun isServiceNew(serviceName: String): Boolean = !getSeenServices().contains(serviceName)

    fun clearSeenServices() {
        sharedPreferences.edit().remove(KEY_SEEN_SERVICES).apply()
    }

    fun setInstallType(type: String) {
        sharedPreferences.edit().putString(KEY_INSTALL_TYPE, type).apply()
    }

    fun getInstallType(): String = sharedPreferences.getString(KEY_INSTALL_TYPE, "NEW") ?: "NEW"

    fun clearInstallType() {
        sharedPreferences.edit().remove(KEY_INSTALL_TYPE).apply()
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_OFFICES_DATA = "offices_data"
        private const val KEY_POLICIES_DATA = "policies_data"
        private const val KEY_SOS_BLOGS_DATA = "sos_blogs_data"
        private const val KEY_ASSET_DETAILS = "asset_details"
        private const val KEY_COMMUNIQUE_DATA = "communique_data"
        private const val KEY_FAQ_DATA = "faq_data"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_APP_LOCKED = "app_locked"
        private const val KEY_PROFILE_UPDATE_TIMESTAMP = "profile_update_timestamp"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_EVENT_DATA = "event_data"
        private const val KEY_TASKS_DATA = "tasks_data"
        private const val KEY_BIOMETRIC_EMAIL = "biometric_email"
        private const val KEY_BIOMETRIC_MOBILE = "biometric_mobile"
        private const val KEY_BIOMETRIC_EMPLOYEE_ID = "biometric_employee_id"
        private const val KEY_BIOMETRIC_TOKEN = "biometric_token"
        private const val KEY_GREETINGS_DATA = "greetings_data"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_SEEN_SERVICES = "seen_services"
        private const val KEY_INSTALL_TYPE = "install_type"
        private const val KEY_SMART_COLLATERAL_DATA = "smart_collateral_data"
        private const val KEY_NOTIFICATION_ACCESS_ASKED = "notification_access_asked"

        // Punch state keys
        private const val KEY_IS_PUNCHED_IN = "is_punched_in"
        private const val KEY_PUNCH_IN_TIME = "punch_in_time"
        private const val KEY_PUNCH_IN_DATE_TIME = "punch_in_date_time"
        private const val KEY_PUNCH_ID = "punch_id"
        private const val KEY_PUNCH_OUT_TIME = "punch_out_time"
        private const val KEY_LAST_PUNCH_DATE = "last_punch_date"
        private const val KEY_TIME_SPENT = "punch_time_spent"
    }

    fun clearPunchState() {
        sharedPreferences.edit().apply {
            remove(KEY_IS_PUNCHED_IN)
            remove(KEY_PUNCH_IN_TIME)
            remove(KEY_PUNCH_IN_DATE_TIME)
            remove(KEY_PUNCH_ID)
            remove(KEY_PUNCH_OUT_TIME)
            remove(KEY_LAST_PUNCH_DATE)
            remove(KEY_TIME_SPENT)
        }.apply()
    }

    fun savePunchState(isPunchedIn: Boolean, punchInTime: String, punchInDateTime: Long, punchId: Int, punchOutTime: String, lastPunchDate: String, timeSpent: String) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_PUNCHED_IN, isPunchedIn)
            putString(KEY_PUNCH_IN_TIME, punchInTime)
            putLong(KEY_PUNCH_IN_DATE_TIME, punchInDateTime)
            putInt(KEY_PUNCH_ID, punchId)
            putString(KEY_PUNCH_OUT_TIME, punchOutTime)
            putString(KEY_LAST_PUNCH_DATE, lastPunchDate)
            putString(KEY_TIME_SPENT, timeSpent)
        }.apply()
    }

    fun getPunchState(): PunchState {
        return PunchState(
            isPunchedIn = sharedPreferences.getBoolean(KEY_IS_PUNCHED_IN, false),
            punchInTime = sharedPreferences.getString(KEY_PUNCH_IN_TIME, "") ?: "",
            punchInDateTime = sharedPreferences.getLong(KEY_PUNCH_IN_DATE_TIME, 0),
            punchId = sharedPreferences.getInt(KEY_PUNCH_ID, -1),
            punchOutTime = sharedPreferences.getString(KEY_PUNCH_OUT_TIME, "") ?: "",
            lastPunchDate = sharedPreferences.getString(KEY_LAST_PUNCH_DATE, "") ?: "",
            timeSpent = sharedPreferences.getString(KEY_TIME_SPENT, "00 h 00 m") ?: "00 h 00 m"
        )
    }

    data class PunchState(
        val isPunchedIn: Boolean,
        val punchInTime: String,
        val punchInDateTime: Long,
        val punchId: Int,
        val punchOutTime: String,
        val lastPunchDate: String,
        val timeSpent: String
    )

    fun setBoolean(
        key: String,
        value: Boolean,
    ) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = sharedPreferences.getBoolean(key, default)

    fun setString(
        key: String,
        value: String,
    ) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(
        key: String,
        default: String,
    ): String? = sharedPreferences.getString(key, default)

    fun setInt(
        key: String,
        value: Int,
    ) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getInt(
        key: String,
        default: Int,
    ): Int = sharedPreferences.getInt(key, default)

    // Mark that the app has been launched before
    fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
    }

    fun hasAskedNotificationAccess(): Boolean =
        sharedPreferences.getBoolean(KEY_NOTIFICATION_ACCESS_ASKED, false)

    fun setNotificationAccessAsked() {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ACCESS_ASKED, true).apply()
    }

    // Store profile update timestamp
    fun setProfileUpdateTimestamp(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_PROFILE_UPDATE_TIMESTAMP, timestamp).apply()
    }

    // Get the profile update timestamp
    fun getProfileUpdateTimestamp(): Long = sharedPreferences.getLong(KEY_PROFILE_UPDATE_TIMESTAMP, 0)

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun saveBiometricCredentials(
        email: String,
        mobile: String,
        employeeId: String,
        token: String,
    ) {
        sharedPreferences
            .edit()
            .apply {
                putString(KEY_BIOMETRIC_EMAIL, email)
                putString(KEY_BIOMETRIC_MOBILE, mobile)
                putString(KEY_BIOMETRIC_EMPLOYEE_ID, employeeId)
                putString(KEY_BIOMETRIC_TOKEN, token)
                putBoolean(KEY_BIOMETRIC_ENABLED, true)
            }.apply()
    }

    fun getBiometricCredentialsWithToken(): Quad<String, String, String, String>? {
        val email = sharedPreferences.getString(KEY_BIOMETRIC_EMAIL, null)
        val mobile = sharedPreferences.getString(KEY_BIOMETRIC_MOBILE, null)
        val employeeId = sharedPreferences.getString(KEY_BIOMETRIC_EMPLOYEE_ID, null)
        val token = sharedPreferences.getString(KEY_BIOMETRIC_TOKEN, null)
        return if (email != null && mobile != null && employeeId != null && token != null) {
            Quad(email, mobile, employeeId, token)
        } else {
            null
        }
    }

    fun clearBiometricCredentials() {
        setBoolean(KEY_BIOMETRIC_ENABLED, false)
        setString(KEY_BIOMETRIC_EMAIL, "")
        setString(KEY_BIOMETRIC_MOBILE, "")
        setString(KEY_BIOMETRIC_EMPLOYEE_ID, "")
        setString(KEY_BIOMETRIC_TOKEN, "")
    }

    fun clearBiometricData() {
        sharedPreferences
            .edit()
            .apply {
                remove(KEY_BIOMETRIC_ENABLED)
                remove(KEY_BIOMETRIC_EMAIL)
                remove(KEY_BIOMETRIC_MOBILE)
                remove(KEY_BIOMETRIC_EMPLOYEE_ID)
                remove(KEY_BIOMETRIC_TOKEN)
            }.apply()
    }

    fun saveLong(
        key: String,
        value: Long,
    ) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun getLong(key: String): Long? =
        if (sharedPreferences.contains(key)) {
            sharedPreferences.getLong(key, 0)
        } else {
            null
        }

    fun saveGreetingsList(greetings: Map<String, List<String>>?) {
        if (greetings == null) {
            sharedPreferences.edit().remove(KEY_GREETINGS_DATA).commit()
        } else {
            val json = gson.toJson(greetings)
            sharedPreferences.edit().putString(KEY_GREETINGS_DATA, json).commit()
        }
    }

    // Keep getGreetings as it correctly parses Map<String, List<String>>
    fun getGreetings(): Map<String, List<String>>? {
        val json = sharedPreferences.getString(KEY_GREETINGS_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun saveSmartCollateral(list: List<SmartCollateralCategory>) {
        // Use Gson to serialize, save to SharedPreferences
    }

    fun saveSmartCollateralData(list: List<SmartCollateralCategory>?) {
        if (list == null) {
            sharedPreferences.edit().remove(KEY_SMART_COLLATERAL_DATA).commit()
        } else {
            val json = gson.toJson(list)
            sharedPreferences.edit().putString(KEY_SMART_COLLATERAL_DATA, json).commit()
        }
    }

    fun getSmartCollateralData(): List<SmartCollateralCategory>? {
        val json = sharedPreferences.getString(KEY_SMART_COLLATERAL_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<SmartCollateralCategory>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    // New methods for greeting categories with messages
    private val KEY_GREETING_CATEGORIES = "greeting_categories_data"

    fun saveGreetingCategories(categories: List<ApiGreetingCategory>?) {
        if (categories == null) {
            sharedPreferences.edit().remove(KEY_GREETING_CATEGORIES).commit()
        } else {
            val json = gson.toJson(categories)
            sharedPreferences.edit().putString(KEY_GREETING_CATEGORIES, json).commit()
        }
    }

    fun getGreetingCategories(): List<ApiGreetingCategory>? {
        val json = sharedPreferences.getString(KEY_GREETING_CATEGORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<ApiGreetingCategory>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    // Save tasks data
    fun saveTasks(tasksJson: String) {
        sharedPreferences.edit().putString(KEY_TASKS_DATA, tasksJson).apply()
    }

    // Get tasks data
    fun getTasks(): String = sharedPreferences.getString(KEY_TASKS_DATA, "") ?: ""

    // Save event data
    fun saveEventData(eventDataJson: String?) {
        if (eventDataJson == null) {
            Log.d("PreferencesManager", "Removing event data from preferences")
            sharedPreferences.edit().remove(KEY_EVENT_DATA).apply()
        } else {
            Log.d("PreferencesManager", "Saving event data to preferences: ${eventDataJson.take(100)}...")
            sharedPreferences.edit().putString(KEY_EVENT_DATA, eventDataJson).apply()
        }
    }

    // Get event data
    fun getEventData(): String? {
        val eventData = sharedPreferences.getString(KEY_EVENT_DATA, null)
        Log.d("PreferencesManager", "Retrieved event data from preferences: ${eventData?.take(100) ?: "null"}...")
        return eventData
    }
}
