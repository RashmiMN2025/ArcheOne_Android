package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.archeGlobal.one.controller.LocationsController
import com.archeGlobal.one.ui.screens.LocationsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class LocationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                val navController = rememberNavController()
                val controller = LocationsController(this)
                // You can pass showHeader and isEmergencyContact as needed
                LocationsScreen(
                    navController = navController,
                    controller = controller,
                    isEmergencyContact = false,
                    showHeader = true,
                    onBackToHome = { finish() } // <-- This will close LocationsActivity and return to Home
                )
            }
        }
    }
}
