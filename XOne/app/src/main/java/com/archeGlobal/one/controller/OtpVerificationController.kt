package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.archeGlobal.one.XOneApplication
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.BiometricHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpVerificationController(
    private val navigator: Navigator,
    private val context: Context
) {
    private val userDataManager = UserDataManager.getInstance(context)

    fun verifyOtp(
        email: String,
        mobile: String,
        employeeId: String,
        otpFromUser: String,
        isBiometric: Boolean = false,
        callback: (String, Boolean) -> Unit
    ) {
        val request = VerifyOtpRequest(email, mobile, employeeId, otpFromUser, isBiometric)
        Log.d("OtpVerification", "Sending OTP verification request: $request")

        RetrofitClient.apiService.verifyOtp(request).enqueue(object : retrofit2.Callback<OtpVerifyResponse> {
            override fun onResponse(call: retrofit2.Call<OtpVerifyResponse>, response: retrofit2.Response<OtpVerifyResponse>) {
                Log.d("OtpVerification", "OTP Response Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.status == 200) {
                    val token = response.body()?.token ?: ""
                    Log.d("OtpVerification", "Token received: $token")

                    if (token.isNotEmpty()) {
                        // Save the token for future use
                        loginWithToken(token, email, mobile, employeeId,false,true,callback,)
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

    fun verifyWithBiometric(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        // Skip OTP and use biometric authentication
        val dummyOtp = "000000" // This won't be validated server-side when isBiometric is true
        verifyOtp(email, mobile, employeeId, dummyOtp, true, callback)
    }

     fun loginWithToken(
        token: String,
        email: String,
        mobile: String,
        employeeId: String,
        fromHome:Boolean = false,
        fromOtp: Boolean = false,
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

                        if(!fromHome){
                            // Pass biometric setup flag to HomeActivity
                            if (fromOtp) {
                                val biometricHelper = BiometricHelper(context)
                                val canUse = biometricHelper.canUseBiometric()
                                val isEnabled = biometricHelper.isBiometricEnabled()
                                if (canUse && !isEnabled) {
                                    navigator.navigateToHome(fromOtp, true, email, mobile, employeeId)
                                } else {
                                    navigator.navigateToHome(fromOtp)
                                }
                            } else {
                                navigator.navigateToHome(fromOtp)
                            }
                        }
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

    private fun showBiometricSetupDialog(email: String, mobile: String, employeeId: String) {
        Log.d("BiometricSetup", "Starting showBiometricSetupDialog")
        
        val activity = context as? FragmentActivity
        if (activity == null) {
            Log.e("BiometricSetup", "Context is not a FragmentActivity: ${context.javaClass.simpleName}")
            return
        }
        
        try {
            android.app.AlertDialog.Builder(activity)
                .setTitle("Enable Fingerprint Login")
                .setMessage("Would you like to use fingerprint for faster login next time?")
                .setPositiveButton("Yes") { _, _ ->
                    Log.d("BiometricSetup", "User clicked Yes")
                    val biometricHelper = BiometricHelper(context)
                    biometricHelper.showBiometricPrompt(
                        activity = activity,
                        title = "Setup Fingerprint",
                        subtitle = "Verify your fingerprint to enable quick login",
                        onSuccess = {
                            Log.d("BiometricSetup", "Biometric setup successful")
                            biometricHelper.saveCredentials(email, mobile, employeeId)
                            Toast.makeText(context, "Fingerprint login enabled successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Log.e("BiometricSetup", "Biometric setup failed: $error")
                            Toast.makeText(context, "Failed to setup fingerprint: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .setNegativeButton("No") { _, _ ->
                    Log.d("BiometricSetup", "User clicked No")
                }
                .show()
            Log.d("BiometricSetup", "Dialog shown successfully")
        } catch (e: Exception) {
            Log.e("BiometricSetup", "Failed to show dialog", e)
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
        fun getCommuniquesData(): List<CommuniqueModel.Communique>? = UserDataManager.getInstance(XOneApplication.getInstance()).getCommuniqueData()
        
        fun clearUserData() {
            UserDataManager.getInstance(XOneApplication.getInstance()).clearUserData()
            Log.d("UserData", "User data cleared during logout")
        }
    }
}
