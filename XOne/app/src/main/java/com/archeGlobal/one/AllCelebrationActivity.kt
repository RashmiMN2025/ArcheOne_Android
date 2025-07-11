package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.archeGlobal.one.controller.HomeController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.AllCelebrationScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class AllCelebrationActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator
    private lateinit var homeController: HomeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        navigator = AndroidNavigator(this)
        homeController = HomeController(navigator, this)

        setContent {
            XOneTheme {
                AllCelebrationScreen(
                    controller = homeController,
                    onBackPressed = { finish() }
                )
            }
        }
    }
} 