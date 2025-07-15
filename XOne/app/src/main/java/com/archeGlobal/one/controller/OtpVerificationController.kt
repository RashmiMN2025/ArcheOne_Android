package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.XOneApplication
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.EncryptedAPIHelper
import com.archeGlobal.one.utils.handleError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpVerificationController(
    private val navigator: Navigator,
    private val context: Context
) {
    private val userDataManager = UserDataManager.getInstance(context)
    private val encryptedAPIHelper = EncryptedAPIHelper(context)

    fun verifyOtp(
        email: String,
        mobile: String,
        employeeId: String,
        otpFromUser: String,
        isBiometric: Boolean = false,
        callback: (String, Boolean) -> Unit
    ) {
        val request = VerifyOtpRequest(email, mobile, employeeId, otpFromUser, isBiometric)
        Log.d("OtpVerification", "Sending encrypted OTP verification request: $request")

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "otpVerify",
            method = "POST",
            request = request,
            responseClass = OtpVerifyResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            if (error != null) {
                Log.e("OtpVerification", "OTP verification failed: ${error.errorMessage}")
                error.handleError(callback)
            } else if (response != null && response.status == 200) {
                val token = response.token
                Log.d("OtpVerification", "Token received: $token")

                if (token.isNotEmpty()) {
                    // Save the token for future use
                    loginWithToken(token, email, mobile, employeeId, false, true, false) { msg, isError ->
                        if (!isError) {
                            // Instead of navigating to Home, go to MPIN setup
                            if (navigator is com.archeGlobal.one.navigation.AndroidNavigator) {
                                val mpinController = com.archeGlobal.one.controller.MpinController(context)
                                if (mpinController.isMpinSet()) {
                                    // MPIN already set, go directly to Home and set fromLogin=true
                                    navigator.navigateToHome(
                                        true,    // fromOtp (set to true to indicate login just happened)
                                        true, // <-- this extra is important for fingerprint prompt
                                        email = email,
                                        mobile = mobile,
                                        employeeId = employeeId
                                    )
                                } else {
                                    // MPIN not set, go to MPIN setup
                                    navigator.navigateToMpinSetup(email, mobile, employeeId, token)
                                }
                            }
                        }
                        callback(msg, isError)
                    }
                } else {
                    callback("OTP verified, but no token received!", true)
                }
            } else {
                Log.e("OtpVerification", "OTP verification failed: Invalid response")
                callback("OTP verification failed", true)
            }
        }
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
        fromHome: Boolean = false,
        fromOtp: Boolean = false,
        shouldNavigateToHome: Boolean = true,
        callback: (String, Boolean) -> Unit
    ) {
        val request = LoginRequest(email, mobile, employeeId)
        Log.d("LoginProcess", "Sending encrypted login request with token: Bearer $token")

        // Store the token temporarily for the encrypted request
        val preferencesManager = com.archeGlobal.one.utils.PreferencesManager(context)
        preferencesManager.saveAuthToken(token)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "login",
            method = "POST",
            request = request,
            responseClass = VerifyOtpResponse::class.java,
            withAuthHeader = true // This will use the token we just saved
        ) { response, error ->
            if (error != null) {
                Log.e("LoginProcess", "Login failed: ${error.errorMessage}")
                error.handleError { message, isError ->
                    callback("Login failed: $message", isError)
                }
            } else if (response != null && response.status == 200) {
                Log.d("LoginProcess", "Login successful")
                
                // Save user data from the response
                userDataManager.saveUserDataFromResponse(response, token)
                userDataManager.setIsLoggedIn(true)
                userDataManager.setHasLoggedIn(true)
                
                // Navigate if needed
                if (!fromHome && shouldNavigateToHome) {
                    if (fromOtp) {
                        navigator.navigateToHome(fromOtp, true, email, mobile, employeeId)
                    } else {
                        navigator.navigateToHome(fromOtp)
                    }
                }
                callback("Login successful", false)
            } else {
                Log.e("LoginProcess", "Login failed: Invalid response")
                callback("Login failed: Invalid response", true)
            }
        }
    }

    fun resendOtp(email: String, mobile: String, employeeId: String, callback: (String) -> Unit) {
        val request = SendOtpRequest(email, mobile, employeeId)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "send-otp",
            method = "POST",
            request = request,
            responseClass = SendOtpResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            if (error != null) {
                Log.e("OtpVerification", "Resend OTP failed: ${error.errorMessage}")
                callback("Failed to resend OTP: ${error.errorMessage}")
            } else if (response != null && response.status == 200) {
                callback("OTP sent successfully.")
            } else {
                callback("Failed to resend OTP")
            }
        }
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
