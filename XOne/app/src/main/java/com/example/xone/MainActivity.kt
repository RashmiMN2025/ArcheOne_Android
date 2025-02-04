package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.xone.controller.HomeController
import com.example.xone.controller.WelcomeController
import com.example.xone.ui.screens.HomeScreen
import com.example.xone.ui.screens.WelcomeScreen
import com.example.xone.ui.theme.XOneTheme
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.model.WelcomeBackgroundModel
import com.example.xone.controller.LocationsController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.xone.ui.screens.LocationsScreen

class MainActivity : ComponentActivity() {
    private lateinit var welcomeController: WelcomeController
    private lateinit var homeController: HomeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            XOneTheme {
                val navController = rememberNavController()
                val navigator = AndroidNavigator(this, navController)
                
                // Initialize controllers
                welcomeController = WelcomeController(navigator)
                homeController = HomeController(navigator)
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "welcome",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("welcome") {
                            WelcomeScreen(
                                model = welcomeController.getWelcomeData(),
                                backgroundModel = WelcomeBackgroundModel(),
                                onXOneClick = welcomeController::onXOneClick,
                                onPulseClick = welcomeController::onPulseClick
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                model = homeController.getHomeData(),
                                onItemClick = homeController::onItemClick,
                                onAllAppsClick = homeController::onAllAppsClick,
                                onFavoritesClick = homeController::onFavoritesClick,
                                onSearchQueryChanged = homeController::onSearchQueryChanged,
                                onShowProfileClick = homeController::onShowProfileClick,
                                onToggleFavorite = homeController::onToggleFavorite,
                                onFooterHomeClick = homeController::onFooterHomeClick,
                                onFooterChatClick = homeController::onFooterChatClick,
                                onFooterSOSClick = homeController::onFooterSOSClick,
                                onFooterProfileClick = homeController::onFooterProfileClick
                            )
                        }
                        composable("locations") {
                            LocationsScreen(
                                navController = navController,
                                name = "John Doe", // Replace with actual user data
                                department = "Software Development",
                                designation = "Senior Developer"
                            )
                        }
                    }
                }
            }
        }
    }
}