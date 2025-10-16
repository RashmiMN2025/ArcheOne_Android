package com.archeGlobal.one.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.ui.theme.XOneTheme

class MpinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val isReset = intent.getBooleanExtra("resetMpin", false)
        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: ""
        val token = intent.getStringExtra("token") ?: ""
        setContent {
            XOneTheme {
                ResponsiveMpinScreen(
                    isReset = isReset,
                    onMpinSet = { mpin, questions ->
                        com.archeGlobal.one.utils.MpinManager
                            .saveMpin(this, mpin)
                        if (!isReset) {
                            com.archeGlobal.one.utils.MpinManager
                                .saveSecurityQuestions(this, questions)
                        }
                        val token = intent.getStringExtra("token") ?: ""
                        val email = intent.getStringExtra("email") ?: ""
                        val mobile = intent.getStringExtra("mobile") ?: ""
                        val employeeId = intent.getStringExtra("employeeId") ?: ""

                        // Fetch user data using the token and save it
                        val loginController =
                            com.archeGlobal.one.controller.LoginController(
                                this,
                                com.archeGlobal.one.navigation
                                    .AndroidNavigator(this),
                            )
                        loginController.loginWithToken(token, email, mobile, employeeId) { msg, isError ->
                            if (!isError) {
                                com.archeGlobal.one.utils.CustomToast
                                    .showErrorToast(this, "Your MPIN set successfully")
                                // Only after successful login, save user data and set login state
                                val userDataManager =
                                    com.archeGlobal.one.utils.UserDataManager
                                        .getInstance(this)
                                userDataManager.setIsLoggedIn(true)
                                userDataManager.setHasLoggedIn(true)
                                com.archeGlobal.one.utils
                                    .setFirstTimeLogin(this, false)
                                // userDataManager.saveUserDataFromResponse(...) // If not already done in LoginController
                            }
                            // Pass all data to HomeActivity
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                val intent =
                                    android.content
                                        .Intent(
                                            this,
                                            com.archeGlobal.one.HomeActivity::class.java,
                                        ).apply {
                                            flags =
                                                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                                android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            putExtra("email", email)
                                            putExtra("mobile", mobile)
                                            putExtra("employeeId", employeeId)
                                            putExtra("token", token)
                                            putExtra("mpin", mpin)
                                            putExtra("fromMpin", true)
                                        }
                                startActivity(intent)
                                finish()
                            }, 1200)
                        }
                    },
                    onForgotMpin = {
                        finish()
                    },
                )
            }
        }
    }
}
