package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.MyDocumentsController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.screens.MyDocumentsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

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