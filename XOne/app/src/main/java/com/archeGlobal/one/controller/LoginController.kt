package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.DeviceInfoUtils
import com.archeGlobal.one.utils.EncryptedAPIHelper
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.handleError
import kotlinx.coroutines.*
import org.json.JSONObject

class LoginController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val encryptedAPIHelper = EncryptedAPIHelper(context)

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

        Log.d("LoginController", "Sending OTP request for email: $email, mobile: $mobile, employeeId: $employeeId")

        val request = SendOtpRequest(email, mobile, employeeId)
        Log.d("LoginController", "Request payload: email=$email, mobile=$mobile, employeeId=$employeeId")
        Log.d("LoginController", "Making encrypted request to: ${RetrofitClient.BASE_URL}send-otp")

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "send-otp",
            method = "POST",
            request = request,
            responseClass = SendOtpResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            if (error != null) {
                Log.e("LoginController", "Send OTP failed: ${error.errorMessage}")
                // Extract the actual error message from API response
                callback(error.errorMessage, true)
            } else if (response != null) {
                Log.d("LoginController", "Send OTP response: ${response.message}, status: ${response.status}")
                // Check if the API response indicates an error even with 200 status
                if (response.status != 200) {
                    callback(response.message, true)
                } else {
                    callback(response.message, false)
                }
            } else {
                callback("Unknown error occurred", true)
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
                val deviceInfo = DeviceInfoUtils.getAllDeviceInfo(context)
                val request = LoginRequest(
                    email = email,
                    mobile = mobile,
                    employeeId = employeeId,
                    platform = deviceInfo.platform,
                    deviceModel = deviceInfo.deviceModel,
                    osVersion = deviceInfo.osVersion,
                    appVersion = deviceInfo.appVersion
                )
                val response = RetrofitClient.apiService.login(token, request).execute()
                val responseBody = response.body()
                val errorBody = response.errorBody()?.string()

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            // Check if MPIN is already set up before navigating
                            val mpinController = com.archeGlobal.one.controller.MpinController(context)
                            val hasMpinSet = mpinController.isMpinSet()

                            if (navigator is com.archeGlobal.one.navigation.AndroidNavigator) {
                                Log.d("LoginController", "MFA Login - MPIN set: $hasMpinSet, Email: $email")
                                if (!hasMpinSet) {
                                    // MPIN not set, go to MPIN setup
                                    Log.d("LoginController", "Navigating to MPIN setup")
                                    navigator.navigateToMpinSetup(email, mobile, employeeId, token)
                                } else {
                                    // MPIN already set, go directly to home
                                    Log.d("LoginController", "Navigating to home screen")
                                    UserDataManager.getInstance(context).saveUserDataFromResponse(responseBody, token)
                                    UserDataManager.getInstance(context).setIsLoggedIn(true)
                                    UserDataManager.getInstance(context).setHasLoggedIn(true)
                                    navigator.navigateToHome(true, true, email, mobile, employeeId)
                                }
                            }
                            callback("Login successful", false)
                        }
                        errorBody != null -> {
                            Log.e("LoginController", "API Error Response: $errorBody")
                            Log.e("LoginController", "Response Code: ${response.code()}")

                            try {
                                // Parse the error response to extract the message
                                val errorJson = JSONObject(errorBody)
                                val errorMessage = errorJson.optString("message", "")
                                
                                // Check if it's a 403 (Forbidden) - app update required
                                if (response.code() == 403) {
                                    // Show update dialog
                                    if (navigator is com.archeGlobal.one.navigation.AndroidNavigator) {
                                        navigator.showUpdateDialog()
                                    }
                                    val updateMessage = if (errorMessage.isNotBlank()) errorMessage else "App update required"
                                    callback(updateMessage, true)
                                } else {
                                    // Use the API error message if available, otherwise use a default message
                                    val finalErrorMessage = if (errorMessage.isNotBlank()) {
                                        errorMessage
                                    } else {
                                        when (response.code()) {
                                            400 -> "Invalid login details. Please check your credentials."
                                            401 -> "Invalid credentials. Please try again."
                                            404 -> "User not found. Please check your details."
                                            500 -> "Server error. Please try again later."
                                            else -> "Login failed. Please try again."
                                        }
                                    }
                                    callback(finalErrorMessage, true)
                                }
                            } catch (e: Exception) {
                                Log.e("LoginController", "Error parsing error response: ${e.message}")
                                val fallbackMessage = when (response.code()) {
                                    400 -> "Invalid login details. Please check your credentials."
                                    401 -> "Invalid credentials. Please try again."
                                    403 -> "App update required"
                                    404 -> "User not found. Please check your details."
                                    500 -> "Server error. Please try again later."
                                    else -> "Login failed. Please try again."
                                }
                                callback(fallbackMessage, true)
                            }
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
