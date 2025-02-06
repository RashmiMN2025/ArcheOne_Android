package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xone.controller.OtpVerificationController
import com.example.xone.ui.screens.OtpVerificationScreen
import com.example.xone.ui.theme.XOneTheme

class OtpVerificationActivity : ComponentActivity() {
    private val otpVerificationController = OtpVerificationController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: "" // Pass phone and employeeId properly based on your implementation
        val employeeId = intent.getStringExtra("employeeId") ?: ""

        setContent {
            XOneTheme {
                OtpVerificationScreen(
                    controller = otpVerificationController,
                    email = email,
                    mobile = mobile,
                    employeeId = employeeId
                )
            }
        }
    }
}
