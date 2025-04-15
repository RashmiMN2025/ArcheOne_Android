package com.archeGlobal.one

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.ui.screens.LoginScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)

        // Handle back press in login screen - exit app instead of going back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Exit the app when back is pressed at login
                finish()
            }
        })

        setContent {
            XOneTheme {
                LoginScreen(controller = loginController, navigator = navigator)
            }
        }
    }
}
