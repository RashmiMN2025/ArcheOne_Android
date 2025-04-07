package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.archeGlobal.one.controller.SOSController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.SOSScreen
import android.graphics.Color
import android.os.Build
import com.archeGlobal.one.HomeActivity

class SOSActivity : ComponentActivity() {
    private val controller: SOSController by viewModels()
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        // CRITICAL: Set theme and display parameters BEFORE super.onCreate
        if (intent.getBooleanExtra("fromPdfViewer", false) || intent.getBooleanExtra("preventWhiteBar", false)) {
            setTheme(R.style.Theme_XOne)
        }

        super.onCreate(savedInstanceState)

        // Aggressive handling to prevent any white bars by making everything edge-to-edge
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )

        // Make status bar completely transparent
        window.statusBarColor = Color.TRANSPARENT

        // Enable edge-to-edge and immersive mode
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Apply multiple system UI visibility flags for maximum compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = ViewCompat.getWindowInsetsController(window.decorView)
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        navigator = AndroidNavigator(this)

        // Retrieve the showHeader value from the intent
        val showHeader = intent.getBooleanExtra("showHeader", true) // Default to true if not provided

        // Apply one final delay before rendering content to ensure all UI changes are applied
        window.decorView.post {
            setContent {
                SOSScreen(
                    controller = controller,
                    onNavigateToRaiseConcern = {
                        val intent = Intent(this, RaiseConcernActivity::class.java)
                        startActivity(intent)
                    },
                    onBackPressed = { finish() },
                    onSOSBlogClick = { blog ->
                        val intent = Intent(this, SOSDetailActivity::class.java)
                        intent.putExtra("blog", blog)
                        startActivity(intent)
                    },

                    onNavigateToEmergencyContact = {
                        Log.d("SOSActivity", "onNavigateToEmergencyContact callback triggered")
                        navigator.navigateToLocations(showHeader)
                    },

                    showHeader = showHeader // Pass the showHeader value dynamically
                )
            }
        }
    }

    // Apply edge-to-edge in onResume too for extra insurance
    override fun onResume() {
        super.onResume()
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
