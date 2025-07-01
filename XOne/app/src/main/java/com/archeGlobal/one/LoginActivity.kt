package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ResponsiveLoginScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import androidx.activity.enableEdgeToEdge

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)

        // Check if we should force original login form
        val forceOriginalLogin = intent.getBooleanExtra("forceOriginalLogin", false)
        val forceDifferentUserMode = intent.getBooleanExtra("forceDifferentUserMode", false)

        // Handle back press in login screen - exit app instead of going back
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Exit the app when back is pressed at login
                    finish()
                }
            }
        )

        setContent {
            XOneTheme {
                ResponsiveLoginScreen(
                    controller = loginController,
                    navigator = navigator,
                    forceOriginalLogin = forceOriginalLogin,
                    forceDifferentUserMode = forceDifferentUserMode
                )
            }
        }
    }
}
