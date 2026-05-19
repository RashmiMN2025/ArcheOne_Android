package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.archeGlobal.one.HomeActivity
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.OnboardingActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.utils.BiometricHelper
import com.archeGlobal.one.utils.MpinManager
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.setFirstTimeLogin

class SplashActivity : ComponentActivity() {
    private lateinit var userDataManager: UserDataManager
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userDataManager = UserDataManager.getInstance(applicationContext)
        preferencesManager = PreferencesManager(applicationContext)

        // Set Splash Screen UI
        setContent {
            XOneSplashScreen()
        }

        // Delay for 2 seconds and then check if it's first launch or user is logged in
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if this is the first launch of the app
            if (preferencesManager.isFirstLaunch()) {
                // First launch, show onboarding screens
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
                return@postDelayed
            }

            // If the app was updated since the last launch, force the
            // "Login with Different User" flow: clear all user data and redirect to
            // LoginActivity so the user re-authenticates fresh on the new version.
            if (handleAppUpdateIfNeeded()) {
                clearAllUserDataForUpdate()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("forceDifferentUserMode", true)
                    putExtra("clearFields", true)
                }
                startActivity(intent)
                finish()
                return@postDelayed
            }

            // Not first launch, not just-updated. Check if user is logged in.
            val isLoggedIn = userDataManager.isLoggedIn()
            Log.d("SplashActivity", "Login status: $isLoggedIn, userData: ${userDataManager.getUserData()?.name}")

            if (isLoggedIn) {
                // User is logged in, go to Home screen
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // User is not logged in, go to Login screen
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }

    /**
     * Returns true if the app's versionName has changed since the last launch.
     * Also persists the new version + installType so the check fires exactly once
     * per update. Returns false on first install (no stored version yet) — the
     * normal first-install path in HomeController handles that case.
     */
    private fun handleAppUpdateIfNeeded(): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val currentVersion = packageInfo.versionName ?: "unknown"
            val storedVersion = preferencesManager.getAppVersion()

            if (storedVersion.isEmpty() || storedVersion == currentVersion) {
                return false
            }

            Log.d("SplashActivity", "App update detected: $storedVersion -> $currentVersion")
            preferencesManager.setAppVersion(currentVersion)
            preferencesManager.setInstallType("UPDATED")
            preferencesManager.clearSeenServices()
            true
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error checking app version", e)
            false
        }
    }

    /**
     * Mirrors the "Login with Different User" cleanup in LoginScreen — wipes the
     * current session and the preserved last-user fields so the next login starts
     * from scratch.
     */
    private fun clearAllUserDataForUpdate() {
        userDataManager.clearUserData()
        userDataManager.setIsLoggedIn(false)
        userDataManager.setHasLoggedIn(false)
        setFirstTimeLogin(applicationContext, true)
        MpinManager.clearAllMpinData(applicationContext)
        BiometricHelper(applicationContext).disableBiometric()

        preferencesManager.setString("last_user_email", "")
        preferencesManager.setString("last_user_mobile", "")
        preferencesManager.setString("last_user_employee_id", "")
        preferencesManager.setString("last_user_name", "")
        preferencesManager.setString("session_expired_email", "")
        preferencesManager.setString("session_expired_mobile", "")
        preferencesManager.setString("session_expired_employee_id", "")
        preferencesManager.setString("session_expired_name", "")
        preferencesManager.setString("office_ip", "")

        Log.d("SplashActivity", "All user data cleared after app update")
    }
}

@Composable
fun XOneSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.arche2),
                contentDescription = "Company Logo",
                modifier =
                    Modifier
                        .size(110.dp)
                        .offset(y = (-40).dp),
            )
        }
    }
}
