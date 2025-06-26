package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.archeGlobal.one.controller.ChatController
import com.archeGlobal.one.model.ChatBottomNavigationBar
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ChatScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class ChatActivity : ComponentActivity() {
    private lateinit var chatController: ChatController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XOneTheme {
                val navController = rememberNavController()
                val navigator = AndroidNavigator(this)
                navigator.setNavController(navController)

                chatController = ChatController(this, navigator)

                // Disable back swipe gesture
                BackHandler(enabled = true) {
                    // Handle back press manually if needed
                }

                Scaffold(
                    bottomBar = {
                        val sharedPref = getSharedPreferences("event_preferences", android.content.Context.MODE_PRIVATE)
                        val isUsingPrideIcon = sharedPref.getBoolean("using_pride_icon", false)

                        ChatBottomNavigationBar(
                            onHomeClick = { navigator.navigateToHome() },
                            onChatClick = { /* Already on Chat screen */ },
                            onSOSClick = { navigator.navigateToSOS(true) },
                            onProfileClick = { navigator.navigateToProfile() },
                            isUsingPrideIcon = isUsingPrideIcon
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "chat"
                        ) {
                            composable("chat") {
                                // Call onChatScreenEnter when entering the chat screen
                                chatController.onChatScreenEnter()

                                ChatScreen(
                                    viewModel = chatController.viewModel,
                                    navController = navController,
                                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
