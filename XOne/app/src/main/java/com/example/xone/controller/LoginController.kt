package com.example.xone.controller

import android.content.Context
import android.util.Log
import com.example.xone.model.UserData
import com.example.xone.network.RetrofitClient
import com.example.xone.network.SendOtpRequest
import com.example.xone.network.VerifyOtpRequest
import com.example.xone.network.Office
import com.example.xone.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginController(
    private val context: Context,
    private val navigator: Navigator
) {
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

        Log.d("LoginController", "Verifying OTP: $otp for email: $email")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = VerifyOtpRequest(
                    email = email,
                    mobile = mobile,
                    employeeid = employeeId,
                    otpFromUser = otp
                )
                
                // Log the request body
                Log.d("LoginController", "OTP verification request: $request")
                
                val response = RetrofitClient.apiService.verifyOtp(request).execute()
                val responseBody = response.body()
                val errorBody = response.errorBody()?.string()
                
                Log.d("LoginController", "OTP verification response code: ${response.code()}")
                Log.d("LoginController", "OTP verification response body: $responseBody")
                Log.d("LoginController", "OTP verification error body: $errorBody")

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            Log.d("LoginController", "Login successful: ${responseBody.message}")
                            userData = UserData(
                                name = responseBody.name,
                                designation = responseBody.designation,
                                department = responseBody.department,
                                employeeId = responseBody.employeeid,
                                email = responseBody.email,
                                mobile = responseBody.mobile,
                                location = responseBody.location,
                                services = responseBody.services
                            )
                            officesData = responseBody.offices
                            LocationsController(context).initializeLocations()
                            callback(responseBody.message, false)
                            navigator.navigateToHome()  // Add navigation to home
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

    // Add companion object to store user data
    companion object {
        private var userData: UserData? = null
        private var officesData: List<Office>? = null
        
        fun getUserData(): UserData? = userData
        fun getOfficesData(): List<Office>? {
            Log.d("LoginController", "Getting offices data: $officesData")
            return officesData
        }
        
        fun setUserData(data: UserData) {
            userData = data
        }
        
        fun setOfficesData(offices: List<Office>) {
            Log.d("LoginController", "Setting offices data: $offices")
            officesData = offices
        }
    }
}
