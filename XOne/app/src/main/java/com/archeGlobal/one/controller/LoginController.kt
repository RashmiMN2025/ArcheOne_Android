package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.network.*
import com.archeGlobal.one.navigation.Navigator
import kotlinx.coroutines.*
import org.json.JSONObject
import com.archeGlobal.one.utils.UserDataManager

class LoginController(
    private val context: Context,
    private val navigator: Navigator
) {
    // Step 1: Send OTP
    fun sendOtp(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        when {
            email.isEmpty() -> {
                callback("Please enter email", true)
                return
            }

            mobile.isEmpty() -> {
                callback("Please enter mobile number", true)
                return
            }

            employeeId.isEmpty() -> {
                callback("Please enter employee ID", true)
                return
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                callback("Please enter a valid email address", true)
                return
            }
        }

        Log.d("LoginController", "Sending OTP request for email: $email")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = SendOtpRequest(email, mobile, employeeId)
                val response = RetrofitClient.apiService.sendOtp(request).execute()
                val responseBody = response.body()
                val errorBody = response.errorBody()?.string()

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            callback(responseBody.message, responseBody.status != 200)
                        }

                        errorBody != null -> {
                            val errorMessage =
                                JSONObject(errorBody).optString("message", "Server error occurred")
                            callback(errorMessage, true)
                        }

                        else -> {
                            callback("Server error occurred", true)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback("Network error: ${e.message}", true)
                }
            }
        }
    }

    fun loginWithToken(
    token: String,
    email: String,
    mobile: String,
    employeeId: String,
    callback: (String, Boolean) -> Unit
    ) {
        Log.d("LoginController", "Token used for login: $token")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = LoginRequest(email, mobile, employeeId)
                val response = RetrofitClient.apiService.login(token, request).execute()
                val responseBody = response.body()
                val errorBody = response.errorBody()?.string()

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            // Save user data here!
                            UserDataManager.getInstance(context).saveUserDataFromResponse(responseBody, token)
                            UserDataManager.getInstance(context).setIsLoggedIn(true)      // <-- Add this
                            UserDataManager.getInstance(context).setHasLoggedIn(true)     // <-- Add this
                            callback("Login successful", false)
                        }
                        errorBody != null -> {
                            val errorMessage =
                                JSONObject(errorBody).optString("message", "Server error occurred")
                            callback(errorMessage, true)
                        }
                        else -> {
                            callback("Server error occurred", true)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback("Network error: ${e.message}", true)
                }
            }
        }
    }
}