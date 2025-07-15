package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.EncryptedAPIHelper
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
                error.handleError(callback)
            } else if (response != null) {
                Log.d("LoginController", "Send OTP successful: ${response.message}")
                callback(response.message, response.status != 200)
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
                val request = LoginRequest(email, mobile, employeeId)
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
                                if (!hasMpinSet) {
                                    // MPIN not set, go to MPIN setup
                                    navigator.navigateToMpinSetup(email, mobile, employeeId, token)
                                } else {
                                    // MPIN already set, go directly to home
                                    UserDataManager.getInstance(context).saveUserDataFromResponse(responseBody, token)
                                    UserDataManager.getInstance(context).setIsLoggedIn(true)
                                    UserDataManager.getInstance(context).setHasLoggedIn(true)
                                    navigator.navigateToHome(true, true, email, mobile, employeeId)
                                }
                            }
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
