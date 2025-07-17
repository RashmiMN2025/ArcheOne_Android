package com.archeGlobal.one

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.OtpVerificationScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.ui.theme.GraphikFontFamily

class OtpVerificationActivity : AppCompatActivity() {
    private var showUpdateDialog by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email") ?: ""
        val mobile = intent.getStringExtra("mobile") ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: ""

        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)
        val controller = OtpVerificationController(navigator, this)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Handle back press to go back to original login screen
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Navigate back to login screen with extra to force original login form
                    val intent = Intent(this@OtpVerificationActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("forceOriginalLogin", true) // Force original login state
                    }
                    startActivity(intent)
                    finish()
                }
            }
        )

        setContent {
            XOneTheme {
                OtpVerificationScreen(
                    controller = controller,
                    email = email,
                    mobile = mobile,
                    employeeId = employeeId
                )
                
                // Update Required Dialog
                if (showUpdateDialog) {
                    UpdateRequiredDialog(
                        onUpdateClick = {
                            // Open Play Store
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                            startActivity(intent)
                        },
                        onDismiss = { showUpdateDialog = false }
                    )
                }
            }
        }
    }
    
    fun showUpdateDialog() {
        showUpdateDialog = true
    }
}
