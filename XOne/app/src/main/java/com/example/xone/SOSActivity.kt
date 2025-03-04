package com.example.xone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import com.example.xone.controller.SOSController
import com.example.xone.model.FooterNavigationModel
import com.example.xone.ui.components.FooterScaffold
import com.example.xone.ui.screens.RaiseConcernScreen
import com.example.xone.ui.screens.SOSScreen

class SOSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showRaiseConcern by remember { mutableStateOf(false) }
            val controller = SOSController(context = this)
            
            // Create footer navigation model with SOS selected
            val footerNavigation = FooterNavigationModel(
                showHome = false,
                showChat = false,
                showSOS = true,
                showProfile = false
            )

            FooterScaffold(
                footerNavigation = footerNavigation,
                onFooterHomeClick = { 
                    val intent = Intent(this@SOSActivity, HomeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                },
                onFooterChatClick = { /* Implement chat navigation */ },
                onFooterSOSClick = { /* Already on SOS screen */ },
                onFooterProfileClick = { 
                    val intent = Intent(this@SOSActivity, HomeActivity::class.java).apply {
                        putExtra("destination", "profile")
                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }
            ) {
                AnimatedVisibility(
                    visible = showRaiseConcern,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    RaiseConcernScreen(onBackPressed = { showRaiseConcern = false })
                }
                
                AnimatedVisibility(
                    visible = !showRaiseConcern,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SOSScreen(
                        controller = controller,
                        onNavigateToRaiseConcern = { showRaiseConcern = true },
                        onBackPressed = { 
                            finish()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        }
                    )
                }
            }
        }
    }
}
