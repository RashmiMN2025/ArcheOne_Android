package com.archeGlobal.one.controller

import android.util.Log
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.archeGlobal.one.model.UserData


class OtpVerificationController(
    private val navigator: Navigator,
    private val loginController: LoginController
) {

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
                        loginWithToken(token, email, mobile, employeeId, callback)
                    } else {
                        callback("OTP verified, but no token received!", true)
                    }
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error"
                    } catch (e: Exception) {
                        "Error parsing response"
                    }
                    Log.e("OtpVerification", "OTP verification failed: $errorMessage")
                    callback("OTP verification failed: $errorMessage", true)
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

                val response = RetrofitClient.apiService.login(" $token", request).execute()
                val responseBody = response.body()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseBody != null && responseBody.status == 200) {
                        saveUserData(responseBody)
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

    // Save user data after successful login
    private fun saveUserData(response: VerifyOtpResponse) {
        userData = response.user?.let {
            UserData(
                name = it.name,
                designation = it.designation,
                department = it.department,
                employeeId = it.employeeid,
                email = it.email,
                mobile = it.mobile,
                location = it.location,
                services = response.services ?: emptyList(),
                profilePic = response.profile_pic,
                sosContact = response.sos,
                userDetails = it.userDetails
            )
        }

        officesData = response.offices
        policiesData = response.policiesList.map { policy ->
            PolicyModel.Policy(
                policyName = policy.policyName, // Ensure correct property mapping
                filePath = policy.filePath
            )
        }

        sosBlogsData = response.sosBlogs?.map { sosBlog ->
            SosBlogModel(
                name = sosBlog.name,
                description = sosBlog.description,
                imageUrl = sosBlog.imageUrl,
                details = sosBlog.details
            )
        } ?: emptyList()

        Log.d("UserData", "User data saved: $userData")
        Log.d("PoliciesData", "Policies saved: $policiesData")
        Log.d("SOSBlogsData", "SOS Blogs saved: $sosBlogsData")
    }


    // Companion object for storing user data
    companion object {
        private var userData: UserData? = null
        private var officesData: List<Office>? = null
        private var policiesData: List<PolicyModel.Policy>? = null
        private var sosBlogsData: List<SosBlogModel>? = null

        fun getUserData(): UserData? = userData
        fun getOfficesData(): List<Office>? = officesData
        fun getPoliciesData(): List<PolicyModel.Policy>? = policiesData
        
        fun clearUserData() {
            userData = null
            officesData = null
            policiesData = null
            Log.d("UserData", "User data cleared during logout")
        }
        fun getSosBlogsData(): List<SosBlogModel>? = sosBlogsData
    }

    fun resendOtp(email: String, mobile: String, employeeId: String, callback: (String) -> Unit) {
        val request = SendOtpRequest(email, mobile, employeeId) // Assuming your request requires only an email

        RetrofitClient.apiService.sendOtp(request).enqueue(object : retrofit2.Callback<SendOtpResponse> {
            override fun onResponse(call: retrofit2.Call<SendOtpResponse>, response: retrofit2.Response<SendOtpResponse>) {
                if (response.isSuccessful && response.body()?.status == 200) {
                    callback("OTP sent successfully.")
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to resend OTP"
                    callback(errorMessage)
                }
            }

            override fun onFailure(call: retrofit2.Call<SendOtpResponse>, t: Throwable) {
                callback("Network error: ${t.message}")
            }
        })
    }


}
