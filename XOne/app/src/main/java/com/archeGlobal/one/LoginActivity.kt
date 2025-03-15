package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.ui.screens.LoginScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)

        setContent {
            XOneTheme {
                LoginScreen(controller = loginController, navigator = navigator)
            }
        }
    }
}
