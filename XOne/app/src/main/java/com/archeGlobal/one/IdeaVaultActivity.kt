package com.archeGlobal.one.ui.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.archeGlobal.one.controller.IdeaVaultController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.network.ApiService
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.theme.XOneTheme

class IdeaVaultActivity : AppCompatActivity() {
    private lateinit var controller: IdeaVaultController
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize the navigator
        navigator = AndroidNavigator(this)

        // Initialize the controller
        controller = IdeaVaultController(this, navigator)

        // Get the ApiService instance
        val apiService: ApiService = RetrofitClient.apiService

        setContent {
            XOneTheme {
                IdeaVaultScreen(
                    onBackPressed = { finish() },
                    controller = controller, // Pass the initialized controller
                    apiService = apiService
                )
            }
        }
    }
}
