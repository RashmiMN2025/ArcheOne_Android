package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.XOneApplication
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpVerificationController(
    private val navigator: Navigator,
    private val loginController: LoginController,
    private val context: Context
) {
    private val userDataManager = UserDataManager.getInstance(context)

    fun verifyOtp(
        email: String,
        mobile: String,
        employeeId: String,
        otpFromUser: String,
        callback: (String, Boolean) -> Unit
    ) {
        val request = VerifyOtpRequest(email, mobile, employeeId, otpFromUser)
        Log.d("OtpVerification", "Sending OTP verification request: $request")

        RetrofitClient.apiService.verifyOtp(request).enqueue(object : retrofit2.Callback<OtpVerifyResponse> {
            override fun onResponse(call: retrofit2.Call<OtpVerifyResponse>, response: retrofit2.Response<OtpVerifyResponse>) {
                Log.d("OtpVerification", "OTP Response Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.status == 200) {
                    val token = response.body()?.token ?: ""
                    Log.d("OtpVerification", "Token received: $token")

                    if (token.isNotEmpty()) {
                        // Save the token for future use
                        loginWithToken(token, email, mobile, employeeId, callback)
                    } else {
                        callback("OTP verified, but no token received!", true)
                    }
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        // Parse JSON to extract just the message
                        try {
                            val jsonObject = org.json.JSONObject(errorBody)
                            if (jsonObject.has("message")) {
                                jsonObject.getString("message")
                            } else {
                                errorBody
                            }
                        } catch (e: Exception) {
                            // If JSON parsing fails, return the original error message
                            errorBody
                        }
                    } catch (e: Exception) {
                        "Error parsing response"
                    }
                    Log.e("OtpVerification", "OTP verification failed: $errorMessage")
                    callback(errorMessage, true)
                }
            }

            override fun onFailure(call: retrofit2.Call<OtpVerifyResponse>, t: Throwable) {
                Log.e("OtpVerification", "Network error: ${t.message}")
                callback("Network error: ${t.message}", true)
            }
        })
    }

    private fun loginWithToken(
        token: String,
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = LoginRequest(email, mobile, employeeId)
                Log.d("LoginProcess", "Sending login request with token: Bearer $token")

                val response = RetrofitClient.apiService.login(token, request).execute()
                val responseBody = response.body()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseBody != null && responseBody.status == 200) {
                        // Save all user data through the centralized UserDataManager
                        userDataManager.saveUserDataFromResponse(responseBody, token)
                        
                        Log.d("LoginProcess", "Login successful")
                        callback("Login successful", false)
                        navigator.navigateToHome()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("LoginProcess", "Login failed: $errorBody")
                        callback("Login failed: $errorBody", true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginProcess", "Network error: ${e.message}")
                    callback("Network error: ${e.message}", true)
                }
            }
        }
    }

    fun resendOtp(email: String, mobile: String, employeeId: String, callback: (String) -> Unit) {
        val request = SendOtpRequest(email, mobile, employeeId) // Assuming your request requires only an email

        RetrofitClient.apiService.sendOtp(request).enqueue(object : retrofit2.Callback<SendOtpResponse> {
            override fun onResponse(call: retrofit2.Call<SendOtpResponse>, response: retrofit2.Response<SendOtpResponse>) {
                if (response.isSuccessful && response.body()?.status == 200) {
                    callback("OTP sent successfully.")
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string() ?: "Failed to resend OTP"
                        // Parse JSON to extract just the message
                        try {
                            val jsonObject = org.json.JSONObject(errorBody)
                            if (jsonObject.has("message")) {
                                jsonObject.getString("message")
                            } else {
                                errorBody
                            }
                        } catch (e: Exception) {
                            // If JSON parsing fails, return the original error message
                            errorBody
                        }
                    } catch (e: Exception) {
                        "Failed to resend OTP"
                    }
                    callback(errorMessage)
                }
            }

            override fun onFailure(call: retrofit2.Call<SendOtpResponse>, t: Throwable) {
                callback("Network error: ${t.message}")
            }
        })
    }
    
    companion object {
        // Helper methods to access user data from UserDataManager
        fun getUserData(): UserData? = UserDataManager.getInstance(XOneApplication.getInstance()).getUserData()
        fun getOfficesData(): List<Office>? = UserDataManager.getInstance(XOneApplication.getInstance()).getOfficesData()
        fun getPoliciesData(): List<PolicyModel.Policy>? = UserDataManager.getInstance(XOneApplication.getInstance()).getPoliciesData()
        fun getSosBlogsData(): List<SosBlogModel>? = UserDataManager.getInstance(XOneApplication.getInstance()).getSosBlogsData()
        fun getAssetDetails(): List<AssetDetail>? = UserDataManager.getInstance(XOneApplication.getInstance()).getAssetDetails()
        
        fun clearUserData() {
            UserDataManager.getInstance(XOneApplication.getInstance()).clearUserData()
            Log.d("UserData", "User data cleared during logout")
        }
    }
}
