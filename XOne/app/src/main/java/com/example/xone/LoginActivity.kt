package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.controller.LoginController
import com.example.xone.ui.screens.LoginScreen
import com.example.xone.ui.theme.XOneTheme

class LoginActivity : ComponentActivity() {
    private val loginController = LoginController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginController = LoginController()
        val navigator = AndroidNavigator(this)

        setContent {
            XOneTheme {
                LoginScreen(controller = loginController, navigator = navigator)
            }
        }
    }
}
