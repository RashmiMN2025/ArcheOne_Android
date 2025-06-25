package com.archeGlobal.one.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.theme.XOneTheme

class MpinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isReset = intent.getBooleanExtra("resetMpin", false)
        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: ""
        val token = intent.getStringExtra("token") ?: ""
        setContent {
            XOneTheme {
                ResponsiveMpinScreen(
                    isReset = isReset,
                    onMpinSet = { mpin, questions ->
                        com.archeGlobal.one.utils.MpinManager.saveMpin(this, mpin)
                        if (!isReset) {
                            com.archeGlobal.one.utils.MpinManager.saveSecurityQuestions(this, questions)
                        }
                        // Pass all data to HomeActivity
                        val intent = android.content.Intent(this, com.archeGlobal.one.HomeActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("email", email)
                            putExtra("mobile", mobile)
                            putExtra("employeeId", employeeId)
                            putExtra("token", token)
                            putExtra("mpin", mpin)
                            putExtra("fromMpin", true)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onForgotMpin = {
                        finish()
                    }
                )
            }
        }
    }
}