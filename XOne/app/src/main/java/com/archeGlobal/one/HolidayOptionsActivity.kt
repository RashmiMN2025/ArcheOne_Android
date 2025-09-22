package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.HolidayOptionsController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.HolidayOptionsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class HolidayOptionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val controller = HolidayOptionsController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                HolidayOptionsScreen(controller = controller)
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
