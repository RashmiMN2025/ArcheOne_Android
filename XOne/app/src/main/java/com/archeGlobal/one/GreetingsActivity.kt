package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.GreetingsController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ResponsiveGreetingsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class GreetingsActivity : ComponentActivity() {
    private lateinit var controller: GreetingsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        controller = GreetingsController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                ResponsiveGreetingsScreen(
                    controller = controller,
                    onBackPressed = { 
                        // Check if we came from a child screen (Global Celebration/Regional Festivals)
                        val parentActivity = intent.getStringExtra("parent_activity")
                        if (parentActivity == "home") {
                            // Navigate back to Home instead of previous activity
                            val homeIntent = Intent(this@GreetingsActivity, HomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(homeIntent)
                            finish()
                        } else {
                            // Normal back behavior
                            finish()
                        }
                    }
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
