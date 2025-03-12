package com.example.xone.repository

import android.content.Context
import android.content.SharedPreferences

class UserRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "user_prefs", Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_USER_STATE = "user_state"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
    }
    
    // Save user state from login response
    fun saveUserState(state: String) {
        sharedPreferences.edit().putString(KEY_USER_STATE, state).apply()
    }
    
    // Get user state
    fun getUserState(): String? {
        return sharedPreferences.getString(KEY_USER_STATE, null)
    }
    
    // Save basic user info
    fun saveUserInfo(name: String, email: String, employeeId: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ID, employeeId)
        }.apply()
    }
    
    // Clear user data on logout
    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
} 