package com.archeGlobal.one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.screens.OtpVerificationScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class OtpVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: ""
        
        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)
        val controller = OtpVerificationController(navigator, this)

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
