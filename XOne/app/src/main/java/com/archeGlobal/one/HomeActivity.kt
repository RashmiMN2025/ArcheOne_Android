package com.archeGlobal.one

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.*
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.ui.screens.*
import com.archeGlobal.one.ui.theme.XOneTheme
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLDecoder

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
    private lateinit var chatController: ChatController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable back navigation to login
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Either exit the app or show a toast
                Toast.makeText(this@HomeActivity, "Press Home to exit the app", Toast.LENGTH_SHORT).show()
            }
        })

        // Check if we need to navigate to a specific destination
        val destination = intent.getStringExtra("destination")
        val navigateTo = intent.getStringExtra("navigateTo")
        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)
        
        // Print the intent extras for debugging
        Log.d("HomeActivity", "onCreate with intent extras: destination=$destination, navigateTo=$navigateTo, isEmergencyContact=$isEmergencyContact")
        Log.d("HomeActivity", "All extras: ${intent.extras?.keySet()?.joinToString()}")

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
                chatController = ChatController(this, navigator)

                // If we have a destination or navigateTo, navigate to it
                LaunchedEffect(destination, navigateTo, isEmergencyContact) {
                    destination?.let {
                        navController.navigate(it)
                    }
                    
                    navigateTo?.let {
                        navController.navigate(it)
                        
                        // Log the navigation attempt for debugging
                        Log.d("HomeActivity", "Navigating to $it with isEmergencyContact=$isEmergencyContact")
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable(
                        route = "home",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
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
                            onFooterChatClick = { navigator.navigateToChat() },
                            onFooterSOSClick = controller::onFooterSOSClick,
                            onFooterProfileClick = controller::onFooterProfileClick,
                            onXCardClick = controller::onXCardClick
                        )
                    }

                    // Add chat screen composable
                    composable(
                        route = "chat",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        ChatScreen(
                            viewModel = chatController.viewModel,
                            navController = navController,
                            onBackPressed = chatController::onBackPressed,
                            showBottomBar = true
                        )
                    }

                    composable(
                        route = "locations?showHeader={showHeader}",
                        arguments = listOf(
                            navArgument("showHeader") {
                                type = NavType.BoolType
                                defaultValue = true // Default to true if not provided
                            }
                        ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) { backStackEntry ->
                        // Retrieve the showHeader parameter from the navigation arguments
                        val showHeader = intent.getBooleanExtra("showHeader", false)

                        // Check if this is from emergency contact view
                        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)

                        // Reset the intent extra to avoid persisting it across navigations
                        if (isEmergencyContact) {
                            Log.d("HomeActivity", "Locations route accessed with isEmergencyContact=true")
                            intent.removeExtra("isEmergencyContact")
                        } else {
                            Log.d("HomeActivity", "Locations route accessed with isEmergencyContact=false (normal navigation)")
                        }

                        // Pass the showHeader parameter dynamically to LocationsScreen
                        LocationsScreen(
                            navController = navController,
                            controller = locationsController,
                            isEmergencyContact = isEmergencyContact,
                            showHeader = showHeader
                        )
                    }
                    composable(
                        route = "business_card",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
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
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        PolicyScreen(
                            model = policyController.model,
                            onPolicyClick = policyController::onPolicyClick,
                            onBackClick = policyController::onBackClick,
                            isLoading = policyController.isLoading.value
                        )
                    }

                    composable(
                        route = "asset",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
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
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        HolidayCalendarScreen(
                            controller = holidayCalendarController,
                            onBackPressed = { navigator.navigateToHome() },
                            onMonthClick = { month ->
                                navController.navigate("monthDetail/$month")
                            },
                            onHolidayListClick = { pdfUrl ->
                                // Use our PDFViewerScreen with navigator
                                navigator.navigateToPDFViewer(pdfUrl, "Holiday List 2025")
                                Log.d("HomeActivity", "Opening holiday list PDF in PDFViewerScreen: $pdfUrl")
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
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
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
                            onFooterChatClick = { navigator.navigateToChat() },
                            onFooterSOSClick = { navController.navigate("sos") },
                            onFooterProfileClick = { /* Already on Profile screen */ }
                        )
                    }

                    composable(
                        route = "aboutme",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        AboutMeScreen(
                            controller = aboutMeController
                        )
                    }

                    composable(
                        route = "addressdetails",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        AddressDetailsScreen(
                            controller = addressController
                        )
                    }

                    composable(
                        route = "emergencycontact",
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        EmergencyContactScreen(
                            controller = emergencyContactController
                        )
                    }

                    composable(
                        route = "pdf_viewer/{pdfUrl}?title={title}",
                        arguments = listOf(
                            navArgument("pdfUrl") { 
                                type = NavType.StringType
                                nullable = false
                            },
                            navArgument("title") { 
                                type = NavType.StringType
                                defaultValue = "PDF Viewer"
                            }
                        ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) { backStackEntry ->
                        val encodedPdfUrl = backStackEntry.arguments?.getString("pdfUrl") ?: ""
                        val title = backStackEntry.arguments?.getString("title") ?: "PDF Viewer"
                        
                        // Safely decode the URL
                        val pdfUrl = try {
                            val decoded = URLDecoder.decode(encodedPdfUrl, "UTF-8")
                            Log.d("HomeActivity", "Successfully decoded PDF URL: $decoded")
                            decoded
                        } catch (e: Exception) {
                            Log.e("HomeActivity", "Error decoding PDF URL: ${e.message}", e)
                            // If decoding fails, pass the encoded URL and let the PDFViewerScreen handle the error
                            encodedPdfUrl
                        }
                        
                        PDFViewerScreen(
                            pdfUrl = pdfUrl,
                            title = title,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "sos?showHeader={showHeader}",
                        arguments = listOf(
                            navArgument("showHeader") {
                                type = NavType.BoolType
                                defaultValue = true // Default to true if not provided
                            }
                        )
                    ) { backStackEntry ->
                        val showHeader = backStackEntry.arguments?.getBoolean("showHeader") ?: true

                        var showRaiseConcern by remember { mutableStateOf(false) }

                        if (showRaiseConcern) {
                            RaiseConcernScreen(onBackPressed = { showRaiseConcern = false })
                        } else {
                            SOSScreen(
                                controller = sosController,
                                onNavigateToRaiseConcern = { showRaiseConcern = true },
                                onBackPressed = { navigator.navigateToHome() },
                                onSOSBlogClick = { blogId ->
                                    // Convert blog object to JSON and pass it as a parameter
                                    val blogJson = Uri.encode(Gson().toJson(blogId))
                                    navController.navigate("sosDetail/$blogJson")
                                },
                                onNavigateToEmergencyContact = {
                                    Log.d("HomeActivity", "onNavigateToEmergencyContact callback triggered")

                                    // Set the flag in the current activity BEFORE navigation
                                    intent.putExtra("isEmergencyContact", true)
                                    intent.putExtra("showHeader", showHeader)

                                    // Navigate to locations screen with emergency contact flag
                                    navController.navigate("locations") {
                                        // Make sure we don't save the state of other screens
                                        popUpTo("home") {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = false // Don't restore previous state
                                    }
                                },
                                onFooterHomeClick = {
                                    // Navigate to home screen
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onFooterChatClick = {
                                    navigator.navigateToChat()
                                },
                                onFooterSOSClick = {
                                    // Already on SOS screen, do nothing
                                },
                                onFooterProfileClick = {
                                    // Navigate to profile
                                    navController.navigate("profile")
                                },
                                showHeader = showHeader // Pass the showHeader value dynamically
                            )
                        }
                    }

                    composable(
                        "sosDetail/{blog}",
                        arguments = listOf(navArgument("blog") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val json = backStackEntry.arguments?.getString("blog")
                        val blog = Gson().fromJson(json, SosBlogModel::class.java)

                        SOSDetailScreen(
                            blog = blog,
                            onBackPressed = { navController.popBackStack() },
                            onFooterHomeClick = { 
                                // Navigate to home screen
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onFooterChatClick = {
                                navigator.navigateToChat()
                            },
                            onFooterSOSClick = {
                                // Go back to main SOS screen
                                navController.navigate("sos") {
                                    popUpTo("sos") { inclusive = true }
                                }
                            },
                            onFooterProfileClick = {
                                // Navigate to profile
                                navController.navigate("profile")
                            }
                        )
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

    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up controllers that need to clear resources
        if (::policyController.isInitialized) {
            policyController.onCleared()
        }
    }
}