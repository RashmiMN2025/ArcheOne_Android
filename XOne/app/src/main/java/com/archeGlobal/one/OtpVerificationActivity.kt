package com.archeGlobal.one

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.service.OtpNotificationListenerService
import com.archeGlobal.one.ui.screens.OtpVerificationScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.CustomToast
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager

class OtpVerificationActivity : AppCompatActivity() {
    private var showUpdateDialog by mutableStateOf(false)
    private var shouldNavigateToLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Proactive Play Store Update Check
        com.archeGlobal.one.utils.AppUpdateUtils.checkForUpdates(this) {
            showUpdateDialog()
        }

        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: ""
        val stayLoggedIn = intent.getBooleanExtra("stayLoggedIn", false)

        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)
        val controller = OtpVerificationController(navigator, this)
        val prefs = PreferencesManager(this)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Handle back press to go back to original login screen
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (showUpdateDialog) {
                        // Prevent back press when update dialog is shown
                        CustomToast.show(
                            this@OtpVerificationActivity,
                            "Please update the app to continue.",
                            android.widget.Toast.LENGTH_SHORT,
                        )
                    } else {
                        // Navigate back to login screen with extra to force original login form
                        val intent =
                            Intent(this@OtpVerificationActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("forceOriginalLogin", true)
                            }
                        startActivity(intent)
                        finish()
                    }
                }
            },
        )

        setContent {
            XOneTheme {
                OtpVerificationScreen(
                    controller = controller,
                    email = email,
                    mobile = mobile,
                    employeeId = employeeId,
                    stayLoggedIn = stayLoggedIn,
                )

                // Update Required Dialog
                if (showUpdateDialog) {
                    UpdateRequiredDialog(
                        onUpdateClick = {
                            // 1. Force Logout & Total Preference Wipe (Hard Reset)
                            prefs.clearAll()
                            com.archeGlobal.one.utils.MpinManager.clearAllMpinData(this@OtpVerificationActivity)
                            
                            Log.w("OtpVerificationActivity", "MANDATORY UPDATE: Performed Hard Reset of all data and MPIN during OTP stage.")

                            // 2. Prepare LoginActivity as the new root
                            val loginIntent = Intent(this@OtpVerificationActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(loginIntent)

                            // 3. Launch Play Store
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK 
                            }

                            try {
                                startActivity(intent)
                                finish() // Close OtpVerificationActivity
                            } catch (e: Exception) {
                                launchWebFallback()
                            }
                        },
                        onDismiss = { },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Proactive Play Store Update Check
        com.archeGlobal.one.utils.AppUpdateUtils.checkForUpdates(this) {
            showUpdateDialog()
        }

        // If returning from Play Store and shouldNavigateToLogin is true, navigate to LoginActivity
        if (shouldNavigateToLogin) {
            val loginIntent =
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("forceOriginalLogin", true)
                }
            startActivity(loginIntent)
            finish()
        }
    }

    private fun launchWebFallback() {
        val packageName = packageName
        val webIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        try {
            if (webIntent.resolveActivity(packageManager) != null) {
                startActivity(webIntent)
                Log.d("OtpVerificationActivity", "Web Play Store intent launched successfully for package: $packageName")
                // Finish OtpVerificationActivity immediately to ensure browser is in foreground
                finish()
            } else {
                Log.e("OtpVerificationActivity", "No browser found to open web Play Store")
                CustomToast.show(this, "Unable to open Play Store. Please try again.", android.widget.Toast.LENGTH_SHORT)
            }
        } catch (e: Exception) {
            Log.e("OtpVerificationActivity", "Failed to open web Play Store: ${e.message}")
            CustomToast.show(this, "Unable to open Play Store. Please try again.", android.widget.Toast.LENGTH_SHORT)
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled?.contains(packageName) == true
    }

    override fun onDestroy() {
        super.onDestroy()
        OtpNotificationListenerService.clearOtp()
    }

    fun showUpdateDialog() {
        showUpdateDialog = true
    }
}
