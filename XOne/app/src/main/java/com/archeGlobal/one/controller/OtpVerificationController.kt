package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.XOneApplication
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.*
import com.archeGlobal.one.utils.DeviceInfoUtils
import com.archeGlobal.one.utils.EncryptedAPIHelper
import com.archeGlobal.one.utils.UserDataManager

class OtpVerificationController(
    private val navigator: Navigator,
    private val context: Context,
) {
    private val userDataManager = UserDataManager.getInstance(context)
    private val encryptedAPIHelper = EncryptedAPIHelper(context)

    fun verifyOtp(
        email: String,
        mobile: String,
        employeeId: String,
        otpFromUser: String,
        isBiometric: Boolean = false,
        backgroundRefresh: Boolean = false,
        appVersion: String,
        deviceModel: String,
        deviceId: String,
        platform: String,
        osVersion: String,
        stayLoggedIn: Boolean,
        callback: (String, Boolean) -> Unit,
    ) {
        val request =
            VerifyOtpRequest(
                email = email,
                mobile = mobile,
                employeeId = employeeId,
                otpFromUser = otpFromUser,
                isBiometric = isBiometric,
                appVersion = appVersion,
                deviceModel = deviceModel,
                deviceId = deviceId,
                platform = platform,
                osVersion = osVersion,
                stayLoggedIn = stayLoggedIn,
            )
        Log.d("OtpVerification", "Sending encrypted OTP verification request: $request")

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "otpVerify",
            method = "POST",
            request = request,
            responseClass = OtpVerifyResponse::class.java,
            withAuthHeader = false,
            handleTokenExpiration = false, // Disable automatic navigation for OTP errors
        ) { response, error ->
            if (error != null) {
                Log.e("OtpVerification", "OTP verification failed: ${error.errorMessage}")
                Log.e("OtpVerification", "Error type: ${error::class.java.simpleName}")
                Log.e("OtpVerification", "Full error details: $error")

                // Check if error message contains 403 or update-related keywords
                val errorMsg = error.errorMessage.lowercase()
                val isForbiddenError =
                    error is APIError.Forbidden ||
                        errorMsg.contains("403") ||
                        errorMsg.contains("forbidden") ||
                        errorMsg.contains("update") ||
                        errorMsg.contains("version")

                if (isForbiddenError) {
                    Log.d("OtpVerification", "Detected 403/update-related error in OTP verification - showing update dialog")
                    Log.d("OtpVerification", "Error check: is Forbidden=${error is APIError.Forbidden}, message='${error.errorMessage}'")
//                    showUpdateDialog()
                    callback("App update required", true)
                } else {
                    Log.d("OtpVerification", "Not a 403 error, showing error message without navigation")
                    // For OTP verification errors, just show the message without navigation
                    callback(error.errorMessage, true)
                }
            } else if (response != null && response.status == 200) {
                val token = response.token
                Log.d("OtpVerification", "Token received: $token")

                if (token.isNotEmpty()) {
                    if (backgroundRefresh) {
                        // For background refresh, only save data without navigation
                        loginWithToken(token, email, mobile, employeeId, false, true, false) { msg, isError ->
                            if (!isError) {
                                Log.d("OtpVerification", "Background token refresh successful - data updated without navigation")
                            }
                            callback(msg, isError)
                        }
                    } else {
                        // Save the token for future use and handle navigation properly
                        loginWithToken(token, email, mobile, employeeId, false, true, true) { msg, isError ->
                            if (!isError) {
                                // Login successful - for session-expired users or biometric/MPIN login, navigate directly to home
                                Log.d("OtpVerification", "OTP verification and login successful for user: $email")

                                if (navigator is com.archeGlobal.one.navigation.AndroidNavigator) {
                                    val mpinController =
                                        com.archeGlobal.one.controller
                                            .MpinController(context)
                                    if (mpinController.isMpinSet()) {
                                        // MPIN already set, go directly to Home
                                        navigator.navigateToHome(
                                            true, // fromOtp (set to true to indicate login just happened)
                                            true, // showBiometricPrompt for existing users
                                            email = email,
                                            mobile = mobile,
                                            employeeId = employeeId,
                                        )
                                    } else {
                                        // MPIN not set, go to MPIN setup (for first-time users)
                                        navigator.navigateToMpinSetup(email, mobile, employeeId, token)
                                    }
                                }
                            }
                            callback(msg, isError)
                        }
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

    fun loginWithToken(
        token: String,
        email: String,
        mobile: String,
        employeeId: String,
        fromHome: Boolean = false,
        fromOtp: Boolean = false,
        shouldNavigateToHome: Boolean = true,
        callback: (String, Boolean) -> Unit,
    ) {
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
        Log.d("LoginProcess", "Sending encrypted login request with token: Bearer $token")

        // Store the token temporarily for the encrypted request
        val preferencesManager =
            com.archeGlobal.one.utils
                .PreferencesManager(context)
        preferencesManager.saveAuthToken(token)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "login",
            method = "POST",
            request = request,
            responseClass = VerifyOtpResponse::class.java,
            withAuthHeader = true, // This will use the token we just saved
            handleTokenExpiration = false, // Disable automatic navigation for login errors during OTP flow
        ) { response, error ->
            if (error != null) {
                Log.e("LoginProcess", "Login failed: ${error.errorMessage}")
                Log.e("LoginProcess", "Error type: ${error::class.java.simpleName}")
                Log.e("LoginProcess", "Full error details: $error")

                // Check if error message contains 403 or update-related keywords
                val errorMsg = error.errorMessage.lowercase()
                val isForbiddenError =
                    error is APIError.Forbidden ||
                        errorMsg.contains("403") ||
                        errorMsg.contains("forbidden") ||
                        errorMsg.contains("update") ||
                        errorMsg.contains("version")

                if (isForbiddenError) {
                    Log.d("LoginProcess", "Detected 403/update-related error in login - showing update dialog")
                    Log.d("LoginProcess", "Error check: is Forbidden=${error is APIError.Forbidden}, message='${error.errorMessage}'")
//                    showUpdateDialog()
                    callback("App update required", true)
                } else {
                    Log.d("LoginProcess", "Not a 403 error, showing error message without navigation")
                    // For login errors during OTP flow, just show the message without navigation
                    callback("Login failed: ${error.errorMessage}", true)
                }
            } else if (response != null && response.status == 200) {
                Log.d("LoginProcess", "Login successful")

                // Save user data from the response
                userDataManager.saveUserDataFromResponse(response, token)
                userDataManager.setIsLoggedIn(true)
                userDataManager.setHasLoggedIn(true)

                val smartCollateral = response.smartCollateral

                smartCollateral?.let {
                    UserDataManager.getInstance(context).saveSmartCollateral(it) // persist
                }

                // Clear session expired preserved data after successful login
                val preferencesManager =
                    com.archeGlobal.one.utils
                        .PreferencesManager(context)
                preferencesManager.setString("session_expired_email", "")
                preferencesManager.setString("session_expired_mobile", "")
                preferencesManager.setString("session_expired_employee_id", "")
                preferencesManager.setString("session_expired_name", "")

                // Navigate if needed - but only for non-explicit navigation cases
                if (!fromHome && shouldNavigateToHome && !fromOtp) {
                    // For non-OTP cases, navigate to home
                    navigator.navigateToHome(false)
                }
                // For OTP cases, navigation is handled explicitly in verifyOtp method
                callback("Login successful", false)
            } else {
                Log.e("LoginProcess", "Login failed: Invalid response")
                callback("Login failed: Invalid response", true)
            }
        }
    }

    fun resendOtp(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String) -> Unit,
    ) {
        val request = SendOtpRequest(email, mobile, employeeId)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "send-otp",
            method = "POST",
            request = request,
            responseClass = SendOtpResponse::class.java,
            withAuthHeader = false,
            handleTokenExpiration = false, // Disable automatic navigation for resend OTP errors
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

        fun getCommuniquesData(): List<CommuniqueModel.Communique>? =
            UserDataManager.getInstance(XOneApplication.getInstance()).getCommuniqueData()

        fun getSmartCollateralData(): List<SmartCollateralCategory>? =
            UserDataManager.getInstance(XOneApplication.getInstance()).getSmartCollateralList()

        fun clearUserData() {
            UserDataManager.getInstance(XOneApplication.getInstance()).clearUserData()
            Log.d("UserData", "User data cleared during logout")
        }
    }
}
