package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navigation
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import com.example.xone.controller.*
import com.example.xone.ui.screens.*
import com.example.xone.ui.theme.XOneTheme
import com.example.xone.navigation.AndroidNavigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

class MainActivity : ComponentActivity() {
    private lateinit var welcomeController: WelcomeController
    private lateinit var homeController: HomeController
    private lateinit var locationsController: LocationsController
    private lateinit var businessCardController: BusinessCardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigator = AndroidNavigator(this)
        welcomeController = WelcomeController(navigator)
        homeController = HomeController(navigator, this)
        locationsController = LocationsController(this)
        businessCardController = BusinessCardController(this, navigator)

        enableEdgeToEdge()
        
        setContent {
            val navController = rememberNavController()
            
            navigator.setNavController(navController)
            
            XOneTheme {
                Scaffold { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = "welcome") {
                            composable("welcome") {
                                WelcomeScreen(
                                    model = welcomeController.getWelcomeData(),
                                    onXOneClick = welcomeController::onXOneClick,
                                    onPulseClick = welcomeController::onPulseClick
                                )
                            }
                            composable("home") {
                                HomeScreen(
                                    model = homeController.model,
                                    onItemClick = homeController::onItemClick,
                                    onAllAppsClick = homeController::onAllAppsClick,
                                    onFavoritesClick = homeController::onFavoritesClick,
                                    onShowProfileClick = homeController::onShowProfileClick,
                                    onToggleFavorite = homeController::onToggleFavorite,
                                    onFooterHomeClick = homeController::onFooterHomeClick,
                                    onFooterChatClick = homeController::onFooterChatClick,
                                    onFooterSOSClick = homeController::onFooterSOSClick,
                                    onFooterProfileClick = homeController::onFooterProfileClick,
                                    onXCardClick = homeController::onXCardClick
                                )
                            }
                            composable("locations") {
                                LocationsScreen(
                                    navController = navController,
                                    controller = locationsController
                                )
                            }
                            composable(
                                route = "business_card",
                                enterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300)
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300)
                                    )
                                },
                                popEnterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300)
                                    )
                                },
                                popExitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300)
                                    )
                                }
                            ) {
                                BusinessCardScreen(
                                    businessCard = businessCardController.businessCard,
                                    controller = businessCardController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
