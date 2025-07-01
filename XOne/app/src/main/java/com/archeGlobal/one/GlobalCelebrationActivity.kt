package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.GlobalCelebrationController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ResponsiveGlobalCelebrationScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class GlobalCelebrationActivity : ComponentActivity() {
    private lateinit var controller: GlobalCelebrationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        controller = GlobalCelebrationController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                ResponsiveGlobalCelebrationScreen(
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
