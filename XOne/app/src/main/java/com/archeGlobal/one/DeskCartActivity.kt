package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.DeskCartController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.DeskCartScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class DeskCartActivity : ComponentActivity() {
    private lateinit var controller: DeskCartController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        controller = DeskCartController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                DeskCartScreen(
                    model = controller.model,
                    controller = controller
                )
            }
        }
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left, // enter animation for previous activity
            R.anim.slide_out_right // exit animation for current activity
        )
    }
}
