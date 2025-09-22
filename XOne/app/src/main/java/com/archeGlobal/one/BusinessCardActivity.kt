package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.BusinessCardControllerImpl
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.BusinessCardScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class BusinessCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Create the controller
        val controller = BusinessCardControllerImpl(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                BusinessCardScreen(
                    businessCard = controller.businessCard,
                    controller = controller,
                    onBackPressed = { finish() },
                )
            }
        }
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left, // enter animation for previous activity
            R.anim.slide_out_right, // exit animation for current activity
        )
    }
}
