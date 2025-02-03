package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.xone.controller.WelcomeController
import com.example.xone.ui.screens.WelcomeScreen
import com.example.xone.ui.theme.XOneTheme
import com.example.xone.navigation.AndroidNavigator

class MainActivity : ComponentActivity() {
    private lateinit var controller: WelcomeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigator = AndroidNavigator(this)
        controller = WelcomeController(navigator)
        enableEdgeToEdge()
        setContent {
            XOneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(
                        model = controller.getWelcomeData(),
                        onXOneClick = controller::onXOneClick,
                        onPulseClick = controller::onPulseClick,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}