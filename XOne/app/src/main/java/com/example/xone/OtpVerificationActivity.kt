package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xone.controller.LoginController
import com.example.xone.controller.OtpVerificationController
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.ui.screens.OtpVerificationScreen
import com.example.xone.ui.theme.XOneTheme

class OtpVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: ""
        
        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)
        val controller = OtpVerificationController(navigator, loginController)

        setContent {
            XOneTheme {
                OtpVerificationScreen(
                    controller = controller,
                    email = email,
                    mobile = mobile,
                    employeeId = employeeId
                )
            }
        }
    }
}
