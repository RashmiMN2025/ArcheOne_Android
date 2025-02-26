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
import com.example.xone.controller.PolicyController
import com.example.xone.controller.AssetController
import com.example.xone.controller.HolidayCalendarController
import com.example.xone.controller.ProfileController
import com.example.xone.ui.screens.HomeScreen
import com.example.xone.ui.screens.LocationsScreen
import com.example.xone.ui.screens.BusinessCardScreen
import com.example.xone.ui.screens.PolicyScreen
import com.example.xone.ui.screens.AssetScreen
import com.example.xone.ui.screens.HolidayCalendarScreen
import com.example.xone.ui.screens.ProfileScreen
import com.example.xone.ui.theme.XOneTheme

class HomeActivity : ComponentActivity() {
    private lateinit var controller: HomeController
    private lateinit var locationsController: LocationsController
    private lateinit var businessCardController: BusinessCardController
    private lateinit var policyController: PolicyController
    private lateinit var assetController: AssetController
    private lateinit var profileController: ProfileController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val navigator = AndroidNavigator(this)
            navigator.setNavController(navController)

            // Initialize controllers
            controller = HomeController(navigator, this)
            locationsController = LocationsController(this)
            businessCardController = BusinessCardController(this, navigator)
            policyController = PolicyController(this, navigator)
            assetController = AssetController(this, navigator)
            profileController = ProfileController(this, navigator)

            XOneTheme {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            model = controller.model,
                            onItemClick = controller::onItemClick,
                            onAllAppsClick = controller::onAllAppsClick,
                            onFavoritesClick = controller::onFavoritesClick,
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
                    composable("policy") {
                        PolicyScreen(
                            model = policyController.model,
                            onPolicyClick = policyController::onPolicyClick,
                            onDownloadClick = policyController::onDownloadClick,
                            onBackClick = policyController::onBackClick
                        )
                    }
                    composable("asset") {
                        AssetScreen(
                            model = assetController.model,
                            controller = assetController
                        )
                    }

                    composable("holiday_calendar") {
                        HolidayCalendarScreen(
                            controller = HolidayCalendarController(navigator), // Pass navigator here
                            onBackPressed = { navigator.navigateToHome() } // Use navigator for back action
                        )
                    }


                    composable("profile") {
                        ProfileScreen(
                            controller = profileController
                        )
                    }
                }
            }
        }
    }
}