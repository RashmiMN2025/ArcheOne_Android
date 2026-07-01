package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.DeviceInfoUtils
import com.archeGlobal.one.utils.EncryptedAPIHelper
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.*
import org.json.JSONObject

class LoginController(
    private val context: Context,
    private val navigator: Navigator,
) {
    private val encryptedAPIHelper = EncryptedAPIHelper(context)

    // Step 1: Send OTP
    fun sendOtp(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit,
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

            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches() -> {
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
            withAuthHeader = false,
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
        callback: (String, Boolean) -> Unit,
    ) {
        Log.d("LoginController", "===== LoginWithToken Called =====")
        Log.d("LoginController", "Email: '$email' (length: ${email.length})")
        Log.d("LoginController", "Mobile: '$mobile' (length: ${mobile.length})")
        Log.d("LoginController", "EmployeeId: '$employeeId' (length: ${employeeId.length})")
        Log.d("LoginController", "Token: '${token.take(30)}...' (length: ${token.length})")
        Log.d("LoginController", "==================================")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceInfo = DeviceInfoUtils.getAllDeviceInfo(context)
                val request =
                    LoginRequest(
                        email = email,
                        mobile = mobile,
                        employeeId = employeeId,
                        platform = deviceInfo.platform,
                        deviceModel = deviceInfo.deviceModel,
                        osVersion = deviceInfo.osVersion,
                        appVersion = deviceInfo.appVersion,
                        deviceId = deviceInfo.deviceId,
                    )
                
                Log.d("LoginController", "LoginRequest object created:")
                Log.d("LoginController", "  email: '${request.email}'")
                Log.d("LoginController", "  mobile: '${request.mobile}'")
                Log.d("LoginController", "  employeeId: '${request.employeeId}'")
                Log.d("LoginController", "  platform: '${request.platform}'")
                Log.d("LoginController", "  deviceModel: '${request.deviceModel}'")
                
                // Use EncryptedAPIHelper with MSAL token for backend authentication
                // This automatically handles token formatting and encryption
                Log.d("LoginController", "Calling backend login API via EncryptedAPIHelper with MSAL token...")
                encryptedAPIHelper.makeEncryptedCallWithMsalToken(
                    endpoint = "login/v2",
                    method = "POST",
                    request = request,
                    responseClass = VerifyOtpResponse::class.java,
                    callback = { response, error ->
                        // Launch coroutine on Main dispatcher to handle UI updates and callbacks
                        CoroutineScope(Dispatchers.Main).launch {
                            when {
                                error == null && response != null -> {
                                    if (navigator is com.archeGlobal.one.navigation.AndroidNavigator) {
                                        com.archeGlobal.one.utils.PreferencesManager(context).clearPunchState()
                                        UserDataManager.getInstance(context).saveUserDataFromResponse(response, token)
                                        UserDataManager.getInstance(context).setIsLoggedIn(true)
                                        UserDataManager.getInstance(context).setHasLoggedIn(true)
                                        navigator.navigateToHome(true, true, email, mobile, employeeId)
                                    }
                                    Log.d("LoginController", "✓ Login successful via EncryptedAPIHelper")
                                    callback("Login successful", false)
                                }
                                error != null -> {
                                    Log.e("LoginController", "✗ Login failed: ${error.errorMessage}")
                                    callback(error.errorMessage, true)
                                }
                                else -> {
                                    callback("Login failed", true)
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginController", "Exception in loginWithToken: ${e.message}")
                    e.printStackTrace()
                    callback("Login failed: ${e.message}", true)
                }
            }
        }
    }
}
