package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
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
        backgroundRefresh: Boolean = false,
        callback: (String, Boolean) -> Unit
    ) {
        val request = VerifyOtpRequest(email, mobile, employeeId, otpFromUser, isBiometric)
        Log.d("OtpVerification", "Sending encrypted OTP verification request: $request")

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "otpVerify",
            method = "POST",
            request = request,
            responseClass = OtpVerifyResponse::class.java,
            withAuthHeader = false,
            handleTokenExpiration = false // Disable automatic navigation for OTP errors
        ) { response, error ->
            if (error != null) {
                Log.e("OtpVerification", "OTP verification failed: ${error.errorMessage}")
                Log.e("OtpVerification", "Error type: ${error::class.java.simpleName}")
                Log.e("OtpVerification", "Full error details: $error")

                // Check if error message contains 403 or update-related keywords
                val errorMsg = error.errorMessage.lowercase()
                val isForbiddenError = error is APIError.Forbidden || errorMsg.contains("403") || errorMsg.contains("forbidden") ||
                    errorMsg.contains("update") ||
                    errorMsg.contains("version")

                if (isForbiddenError) {
                    Log.d("OtpVerification", "Detected 403/update-related error in OTP verification - showing update dialog")
                    Log.d("OtpVerification", "Error check: is Forbidden=${error is APIError.Forbidden}, message='${error.errorMessage}'")
                    showUpdateDialog()
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
                                    val mpinController = com.archeGlobal.one.controller.MpinController(context)
                                    if (mpinController.isMpinSet()) {
                                        // MPIN already set, go directly to Home
                                        navigator.navigateToHome(
                                            true, // fromOtp (set to true to indicate login just happened)
                                            true, // showBiometricPrompt for existing users
                                            email = email,
                                            mobile = mobile,
                                            employeeId = employeeId
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

    fun verifyWithBiometric(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        // Skip OTP and use biometric authentication
        val dummyOtp = "000000" // This won't be validated server-side when isBiometric is true
        verifyOtp(email, mobile, employeeId, dummyOtp, true, false, callback)
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
        val deviceInfo = DeviceInfoUtils.getAllDeviceInfo(context)
        val request = LoginRequest(
            email = email,
            mobile = mobile,
            employeeId = employeeId,
            platform = deviceInfo.platform,
            deviceModel = deviceInfo.deviceModel,
            osVersion = deviceInfo.osVersion,
            appVersion = deviceInfo.appVersion
        )
        Log.d("LoginProcess", "Sending encrypted login request with token: Bearer $token")

        // Store the token temporarily for the encrypted request
        val preferencesManager = com.archeGlobal.one.utils.PreferencesManager(context)
        preferencesManager.saveAuthToken(token)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "login",
            method = "POST",
            request = request,
            responseClass = VerifyOtpResponse::class.java,
            withAuthHeader = true, // This will use the token we just saved
            handleTokenExpiration = false // Disable automatic navigation for login errors during OTP flow
        ) { response, error ->
            if (error != null) {
                Log.e("LoginProcess", "Login failed: ${error.errorMessage}")
                Log.e("LoginProcess", "Error type: ${error::class.java.simpleName}")
                Log.e("LoginProcess", "Full error details: $error")

                // Check if error message contains 403 or update-related keywords
                val errorMsg = error.errorMessage.lowercase()
                val isForbiddenError = error is APIError.Forbidden || errorMsg.contains("403") || errorMsg.contains("forbidden") ||
                    errorMsg.contains("update") ||
                    errorMsg.contains("version")

                if (isForbiddenError) {
                    Log.d("LoginProcess", "Detected 403/update-related error in login - showing update dialog")
                    Log.d("LoginProcess", "Error check: is Forbidden=${error is APIError.Forbidden}, message='${error.errorMessage}'")
                    showUpdateDialog()
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
                    UserDataManager.getInstance(context).saveSmartCollateral(it)  // persist
                }

                // Clear session expired preserved data after successful login
                val preferencesManager = com.archeGlobal.one.utils.PreferencesManager(context)
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

    fun resendOtp(email: String, mobile: String, employeeId: String, callback: (String) -> Unit) {
        val request = SendOtpRequest(email, mobile, employeeId)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "send-otp",
            method = "POST",
            request = request,
            responseClass = SendOtpResponse::class.java,
            withAuthHeader = false,
            handleTokenExpiration = false // Disable automatic navigation for resend OTP errors
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

    private fun showUpdateDialog() {
        Log.d("OtpVerification", "showUpdateDialog called - thread: ${Thread.currentThread().name}")

        // Always show Toast as immediate feedback
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(context, "App Update Required - Please update from Play Store", android.widget.Toast.LENGTH_LONG).show()
            Log.d("OtpVerification", "Toast shown")
        }

        // Ensure dialog creation happens on main thread
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                Log.d("OtpVerification", "Creating custom update dialog")
                showCustomUpdateDialog()
            } catch (e: Exception) {
                Log.e("OtpVerification", "Failed to create/show update dialog: ${e.message}", e)
                // Fallback: Try to open Play Store directly
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    Log.d("OtpVerification", "Opened Play Store as fallback")
                } catch (fallbackError: Exception) {
                    Log.e("OtpVerification", "Fallback also failed: ${fallbackError.message}")
                }
            }
        }
    }

    private fun showCustomUpdateDialog() {
        // Use a simple, clean AlertDialog with custom styling
        val builder = android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)

        // Create a clean white layout with rounded corners
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            // Create rounded white background
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(android.graphics.Color.WHITE)
            }
            background = drawable
            gravity = android.view.Gravity.CENTER
            // Set fixed width to make it narrower
            layoutParams = android.view.ViewGroup.LayoutParams(
                (320 * context.resources.displayMetrics.density).toInt(), // 320dp width
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Red circle with download icon
        val iconContainer = android.widget.FrameLayout(context).apply {
            val size = 120
            layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                gravity = android.view.Gravity.CENTER
                setMargins(0, 0, 0, 32)
            }
        }

        val circleView = android.view.View(context).apply {
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(android.graphics.Color.parseColor("#E53E3E"))
            }
            background = drawable
            layoutParams = android.widget.FrameLayout.LayoutParams(120, 120)
        }

        val arrowView = android.widget.TextView(context).apply {
            text = "↓"
            textSize = 28f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        iconContainer.addView(circleView)
        iconContainer.addView(arrowView)
        layout.addView(iconContainer)

        // Title
        val titleView = android.widget.TextView(context).apply {
            text = "Update Required"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            gravity = android.view.Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }
        layout.addView(titleView)

        // Message
        val messageView = android.widget.TextView(context).apply {
            text = "A new version of ArcheOne is available. You must update to continue using the app."
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 40)
            }
        }
        layout.addView(messageView)

        // Update button
        val updateButton = android.widget.Button(context).apply {
            text = "Update Now"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(android.graphics.Color.parseColor("#E53E3E"))
            }
            background = drawable
            setPadding(80, 32, 80, 32)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            setOnClickListener {
                Log.d("OtpVerification", "Update button clicked - opening Play Store")
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("OtpVerification", "Failed to open Play Store: ${e.message}")
                    // Fallback to web browser
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                    webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(webIntent)
                }
            }
        }
        layout.addView(updateButton)

        builder.setView(layout)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == android.view.KeyEvent.KEYCODE_BACK
        }

        // Show dialog with safety checks
        if (context is android.app.Activity) {
            if (!context.isFinishing && !context.isDestroyed) {
                dialog.show()
                Log.d("OtpVerification", "Clean update dialog shown successfully")
            } else {
                Log.w("OtpVerification", "Activity is finishing/destroyed, cannot show dialog")
            }
        } else {
            dialog.show()
            Log.d("OtpVerification", "Clean update dialog shown (non-Activity context)")
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
        fun getSmartCollateralData(): List<SmartCollateralCategory>? = UserDataManager.getInstance(XOneApplication.getInstance()).getSmartCollateralList()

        fun clearUserData() {
            UserDataManager.getInstance(XOneApplication.getInstance()).clearUserData()
            Log.d("UserData", "User data cleared during logout")
        }
    }
}
