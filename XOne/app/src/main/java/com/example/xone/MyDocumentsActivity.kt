package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xone.controller.MyDocumentsController
import com.example.xone.controller.OtpVerificationController
import com.example.xone.ui.screens.MyDocumentsScreen
import com.example.xone.ui.theme.XOneTheme

class MyDocumentsActivity : ComponentActivity() {
    private val controller = MyDocumentsController(this)
    private val employeeId = OtpVerificationController.getUserData()?.employeeId ?: ""  // Get actual employee ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                MyDocumentsScreen(
                    controller = controller,
                    context = this,
                    employeeId = employeeId,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}