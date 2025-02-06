package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.controller.HomeController
import com.example.xone.controller.LocationsController
import com.example.xone.controller.BusinessCardController
import com.example.xone.ui.screens.HomeScreen
import com.example.xone.ui.screens.LocationsScreen
import com.example.xone.ui.screens.BusinessCardScreen
import com.example.xone.ui.theme.XOneTheme

class HomeActivity : ComponentActivity() {
    private lateinit var locationsController: LocationsController
    private lateinit var businessCardController: BusinessCardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val navController = rememberNavController()
            val navigator = AndroidNavigator(this)
            navigator.setNavController(navController)
            
            // Initialize controllers
            val controller = HomeController(navigator)
            locationsController = LocationsController(this)
            businessCardController = BusinessCardController(this, navigator)

            XOneTheme {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            model = controller.model,
                            onItemClick = controller::onItemClick,
                            onAllAppsClick = controller::onAllAppsClick,
                            onFavoritesClick = controller::onFavoritesClick,
                            onSearchQueryChanged = controller::onSearchQueryChanged,
                            onShowProfileClick = controller::onShowProfileClick,
                            onToggleFavorite = controller::onToggleFavorite,
                            onFooterHomeClick = controller::onFooterHomeClick,
                            onFooterChatClick = controller::onFooterChatClick,
                            onFooterSOSClick = controller::onFooterSOSClick,
                            onFooterProfileClick = controller::onFooterProfileClick,
                            onXCardClick = controller::onXCardClick
                        )
                    }
                    composable("locations") {
                        LocationsScreen(
                            navController = navController,
                            controller = locationsController
                        )
                    }
                    composable("business_card") {
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