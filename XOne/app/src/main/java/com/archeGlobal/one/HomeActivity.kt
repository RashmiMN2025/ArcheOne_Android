package com.archeGlobal.one

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.controller.HomeController
import com.archeGlobal.one.controller.LocationsController
import com.archeGlobal.one.controller.PolicyController
import com.archeGlobal.one.controller.AssetController
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.controller.ProfileController
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.ui.screens.HomeScreen
import com.archeGlobal.one.ui.screens.LocationsScreen
import com.archeGlobal.one.ui.screens.BusinessCardScreen
import com.archeGlobal.one.ui.screens.PolicyScreen
import com.archeGlobal.one.ui.screens.AssetScreen
import com.archeGlobal.one.ui.screens.HolidayCalendarScreen
import com.archeGlobal.one.ui.screens.MonthDetailScreen
import com.archeGlobal.one.ui.screens.ProfileScreen
import com.archeGlobal.one.controller.*
import com.archeGlobal.one.ui.screens.*
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.network.RetrofitClient
import com.google.gson.Gson
import com.archeGlobal.one.controller.AddressController
import com.archeGlobal.one.controller.EmergencyContactController

class HomeActivity : ComponentActivity() {
    private lateinit var controller: HomeController
    private lateinit var locationsController: LocationsController
    private lateinit var businessCardController: BusinessCardControllerImpl
    private lateinit var policyController: PolicyController
    private lateinit var assetController: AssetController
    private lateinit var profileController: ProfileController
    private lateinit var sosController: SOSController
    private lateinit var holidayCalendarController: HolidayCalendarController
    private lateinit var aboutMeController: AboutMeController
    private lateinit var addressController: AddressController
    private lateinit var emergencyContactController: EmergencyContactController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if we need to navigate to a specific destination
        val destination = intent.getStringExtra("destination")

        // Initialize controllers that need context
        holidayCalendarController = HolidayCalendarController(
            RetrofitClient.apiService,
            UserRepository(this)
        )

        setContent {
            XOneTheme {
                val navController = rememberNavController()
                val navigator = AndroidNavigator(this)
                navigator.setNavController(navController)

                // Initialize controllers with correct parameter order
                controller = HomeController(navigator, this)
                locationsController = LocationsController(this)
                businessCardController = BusinessCardControllerImpl(this, navigator)
                policyController = PolicyController(this, navigator)
                assetController = AssetController(this, navigator)
                profileController = ProfileController(this, navigator)
                sosController = SOSController(application)
                aboutMeController = AboutMeController(navigator)
                addressController = AddressController(navigator)
                emergencyContactController = EmergencyContactController(navigator)

                // If we have a destination, navigate to it
                LaunchedEffect(destination) {
                    destination?.let {
                        navController.navigate(it)
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable(
                        route = "home",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
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

                    composable(
                        route = "locations",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
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
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        BusinessCardScreen(
                            businessCard = businessCardController.businessCard,
                            controller = businessCardController
                        )
                    }

                    composable(
                        route = "policy",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        PolicyScreen(
                            model = policyController.model,
                            onPolicyClick = policyController::onPolicyClick,
                            onDownloadClick = policyController::onDownloadClick,
                            onBackClick = policyController::onBackClick
                        )
                    }

                    composable(
                        route = "asset",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        AssetScreen(
                            model = assetController.model,
                            controller = assetController
                        )
                    }

                    composable(
                        route = "holiday_calendar",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        HolidayCalendarScreen(
                            controller = holidayCalendarController,
                            onBackPressed = { navigator.navigateToHome() },
                            onMonthClick = { month ->
                                navController.navigate("monthDetail/$month")
                            },
                            onHolidayListClick = { pdfUrl ->
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(pdfUrl)
                                }
                                startActivity(intent)
                            }
                        )
                    }

                    composable("monthDetail/{month}") { backStackEntry ->
                        val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 1
                        MonthDetailScreen(
                            month = month,
                            controller = holidayCalendarController,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "profile",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        ProfileScreen(
                            controller = profileController,
                            footerNavigation = FooterNavigationModel(
                                showHome = false,
                                showChat = false,
                                showSOS = false,
                                showProfile = true
                            ),
                            onFooterHomeClick = { navController.navigate("home") },
                            onFooterChatClick = { /* Implement chat navigation */ },
                            onFooterSOSClick = { navController.navigate("sos") },
                            onFooterProfileClick = { /* Already on Profile screen */ }
                        )
                    }

                    composable(
                        route = "aboutme",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        AboutMeScreen(
                            controller = aboutMeController
                        )
                    }

                    composable(
                        route = "addressdetails",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        AddressDetailsScreen(
                            controller = addressController
                        )
                    }

                    composable(
                        route = "emergencycontact",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        EmergencyContactScreen(
                            controller = emergencyContactController
                        )
                    }

                    composable(
                        route = "sos",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        var showRaiseConcern by remember { mutableStateOf(false) }

                        if (showRaiseConcern) {
                            RaiseConcernScreen(onBackPressed = { showRaiseConcern = false })
                        } else {
                            SOSScreen(
                                controller = sosController,
                                onNavigateToRaiseConcern = { showRaiseConcern = true },
                                onBackPressed = { navController.popBackStack() },
                                onSOSBlogClick = { blogId ->
                                    // Convert blog object to JSON and pass it as a parameter
                                    val blogJson = Uri.encode(Gson().toJson(blogId))
                                    navController.navigate("sosDetail/$blogJson")
                                }
                            )
                        }
                    }

                    composable(
                        "sosDetail/{blog}",
                        arguments = listOf(navArgument("blog") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val json = backStackEntry.arguments?.getString("blog")
                        val blog = Gson().fromJson(json, SosBlogModel::class.java)

                        SOSDetailScreen(blog = blog) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (intent.getStringExtra("destination") == "profile") {
            // If we navigated directly to profile, go back to home
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        } else {
            super.onBackPressed()
        }
    }
}