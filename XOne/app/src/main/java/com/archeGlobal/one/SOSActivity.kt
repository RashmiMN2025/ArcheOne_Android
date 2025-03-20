package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.archeGlobal.one.controller.SOSController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.SOSScreen

class SOSActivity : ComponentActivity() {
    private val controller: SOSController by viewModels()
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        navigator = AndroidNavigator(this)

        setContent {
            SOSScreen(
                controller = controller,
                onNavigateToRaiseConcern = { /* Navigate to Raise Concern */ },
                onBackPressed = { finish() },
                onSOSBlogClick = { blog ->
                    val intent = Intent(this, SOSDetailActivity::class.java)
                    intent.putExtra("blog", blog)
                    startActivity(intent)
                },
                onFooterHomeClick = { 
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onFooterChatClick = {
                    // Navigate to Chat screen when available
                },
                onFooterSOSClick = {
                    // Already on SOS screen, do nothing
                },
                onFooterProfileClick = {
                    navigator.navigateToXProfile()
                }
            )
        }
    }
}
