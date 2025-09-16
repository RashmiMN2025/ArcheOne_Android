package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.RegionalFestivalsController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ResponsiveRegionalFestivalsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class RegionalFestivalsActivity : ComponentActivity() {
    private lateinit var controller: RegionalFestivalsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        controller = RegionalFestivalsController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                ResponsiveRegionalFestivalsScreen(
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
