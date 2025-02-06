package com.example.xone.controller

import android.util.Log
import com.example.xone.network.RetrofitClient
import com.example.xone.network.SendOtpRequest
import com.example.xone.network.VerifyOtpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginController {
    // First step: Send OTP
    fun sendOtp(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        // Validate inputs
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
                val request = SendOtpRequest(
                    email = email,
                    mobile = mobile,
                    employeeid = employeeId
                )
                
                val response = RetrofitClient.apiService.sendOtp(request).execute()
                val responseBody = response.body()
                val errorBody = response.errorBody()?.string()
                
                Log.d("LoginController", "Response code: ${response.code()}")
                Log.d("LoginController", "Response body: $responseBody")
                Log.d("LoginController", "Error body: $errorBody")

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            Log.d("LoginController", "Success response: ${responseBody.message}")
                            callback(responseBody.message, responseBody.status != 200)
                        }
                        errorBody != null -> {
                            try {
                                val errorJson = JSONObject(errorBody)
                                val errorMessage = errorJson.optString("message", "Server error occurred")
                                Log.e("LoginController", "Error response: $errorMessage")
                                callback(errorMessage, true)
                            } catch (e: Exception) {
                                Log.e("LoginController", "Error parsing error body: ${e.message}")
                                callback("Server error occurred", true)
                            }
                        }
                        else -> {
                            Log.e("LoginController", "Empty response")
                            callback("Server error occurred", true)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginController", "Network error: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback("Network error: ${e.message}", true)
                }
            }
        }
    }

    // Second step: Verify OTP and Login
    fun verifyOtpAndLogin(
        email: String,
        mobile: String,
        employeeId: String,
        otp: String,
        callback: (String, Boolean) -> Unit
    ) {
        if (otp.isEmpty()) {
            callback("Please enter OTP", true)
            return
        }

        Log.d("LoginController", "Verifying OTP and logging in...")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = VerifyOtpRequest(
                    email = email,
                    mobile = mobile,
                    employeeid = employeeId,
                    otpFromUser = otp
                )
                
                val response = RetrofitClient.apiService.verifyOtp(request).execute()
                val responseBody = response.body()
                val errorBody = response.errorBody()?.string()
                
                Log.d("LoginController", "Login response code: ${response.code()}")
                Log.d("LoginController", "Login response body: $responseBody")
                Log.d("LoginController", "Login error body: $errorBody")

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            Log.d("LoginController", "Login successful: ${responseBody.message}")
                            callback(responseBody.message, false)
                        }
                        errorBody != null -> {
                            try {
                                val errorJson = JSONObject(errorBody)
                                val errorMessage = errorJson.optString("message", "Login failed")
                                Log.e("LoginController", "Login error: $errorMessage")
                                callback(errorMessage, true)
                            } catch (e: Exception) {
                                Log.e("LoginController", "Error parsing login error: ${e.message}")
                                callback("Login failed", true)
                            }
                        }
                        else -> {
                            Log.e("LoginController", "Empty login response")
                            callback("Login failed", true)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginController", "Login network error: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback("Network error during login: ${e.message}", true)
                }
            }
        }
    }
}
