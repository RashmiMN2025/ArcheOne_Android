package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.ui.screens.OnboardingScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.PreferencesManager

class OnboardingActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        preferencesManager = PreferencesManager(applicationContext)

        setContent {
            XOneTheme {
                OnboardingScreen(
                    onGetStartedClick = {
                        // Mark onboarding as completed
                        preferencesManager.setFirstLaunchComplete()

                        // Navigate to login screen
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onSkipClick = {
                        // Mark onboarding as completed
                        preferencesManager.setFirstLaunchComplete()

                        // Navigate to login screen
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}
