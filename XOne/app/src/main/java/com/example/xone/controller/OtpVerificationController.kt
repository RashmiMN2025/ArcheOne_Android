package com.example.xone.controller

import android.util.Log
import com.example.xone.network.RetrofitClient
import com.example.xone.network.VerifyOtpRequest
import com.example.xone.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OtpVerificationController(private val navigator: Navigator) {
    fun verifyOtp(email: String, mobile: String, employeeId: String, otp: String, callback: (String, Boolean) -> Unit) {
        if (otp.isEmpty()) {
            callback("Please enter OTP", true)
            return
        }

        Log.d("OtpVerificationController", "Verifying OTP...")
        
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
                
                Log.d("OtpVerificationController", "Response code: ${response.code()}")
                Log.d("OtpVerificationController", "Response body: $responseBody")

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful && responseBody != null -> {
                            Log.d("OtpVerificationController", "OTP verified successfully")
                            callback(responseBody.message, false)
                            // Navigate to home screen
                            navigator.navigateToHome()
                        }
                        errorBody != null -> {
                            try {
                                val errorJson = JSONObject(errorBody)
                                val errorMessage = errorJson.optString("message", "Verification failed")
                                Log.e("OtpVerificationController", "Error: $errorMessage")
                                callback(errorMessage, true)
                            } catch (e: Exception) {
                                Log.e("OtpVerificationController", "Error parsing error body: ${e.message}")
                                callback("Verification failed", true)
                            }
                        }
                        else -> {
                            Log.e("OtpVerificationController", "Empty response")
                            callback("Verification failed", true)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OtpVerificationController", "Network error: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback("Network error: ${e.message}", true)
                }
            }
        }
    }

    fun resendOtp(email: String, mobile: String, employeeId: String, callback: (String) -> Unit) {
        // Implementation for resending OTP
    }
}
