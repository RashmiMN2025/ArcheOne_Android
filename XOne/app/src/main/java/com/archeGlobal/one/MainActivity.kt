package com.archeGlobal.one

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import com.archeGlobal.one.controller.*
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.ui.screens.*
import com.archeGlobal.one.ui.theme.XOneTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.network.RetrofitClient

class MainActivity : ComponentActivity() {
    private lateinit var welcomeController: WelcomeController
    private lateinit var homeController: HomeController
    private lateinit var locationsController: LocationsController
    private lateinit var businessCardController: BusinessCardControllerImpl
    private lateinit var preferencesManager: PreferencesManager
    private val NOTIFICATION_PERMISSION_CODE = 123

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        
        // Initialize RetrofitClient
        RetrofitClient.initialize(applicationContext)
        Log.d("MainActivity", "RetrofitClient initialized")

        preferencesManager = PreferencesManager(applicationContext)
        
        // Get the flag indicating whether to show the welcome screen
        val showWelcomeScreen = intent.getBooleanExtra("showWelcomeScreen", false)

        // Disable back button when showing welcome screen
        if (showWelcomeScreen) {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // This is the first launch, so just finish the app when back is pressed
                    finish()
                }
            })
        }
        
        val navigator = AndroidNavigator(this)
        
        // Create a custom WelcomeController that marks first launch as complete
        welcomeController = object : WelcomeController(navigator) {
            override fun onXOneClick() {
                // Mark first launch as complete
                preferencesManager.setFirstLaunchComplete()
                // Continue with normal navigation
                super.onXOneClick()
            }
        }
        
        homeController = HomeController(navigator, this)
        locationsController = LocationsController(this)
        businessCardController = BusinessCardControllerImpl(this, navigator)

        // Connect ProfileController with HomeController to update profile picture
        ProfileController.setHomeController(homeController)

        enableEdgeToEdge()
        
        setContent {
            val navController = rememberNavController()
            
            LaunchedEffect(navController) {
    navigator.setNavController(navController)
}
            
            XOneTheme {
                Scaffold { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Set the start destination based on whether we should show the welcome screen
                        val startDestination = if (showWelcomeScreen) "welcome" else "home"
                        
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("welcome") {
                                WelcomeScreen(
                                    onXOneClick = welcomeController::onXOneClick,
                                )
                            }

                            composable("home") {
                                // Collect event-related state flows
                                val eventData = homeController.eventData.collectAsState().value
                                val showEventPopup = homeController.showEventPopup.collectAsState().value
                                
                                // Debug logs for event popup
                                Log.d("MainActivity", "Event data: $eventData")
                                Log.d("MainActivity", "Show event popup: $showEventPopup")
                                
                                HomeScreen(
                                    model = homeController.model,
                                    employeeData = homeController.employeeData, // <-- Add this line
                                    onItemClick = homeController::onItemClick,
                                    onAllAppsClick = homeController::onAllAppsClick,
                                    onFavoritesClick = homeController::onFavoritesClick,
                                    onShowProfileClick = homeController::onShowProfileClick,
                                    onToggleFavorite = homeController::onToggleFavorite,
                                    onFooterHomeClick = homeController::onFooterHomeClick,
                                    onFooterChatClick = homeController::onFooterChatClick,
                                    onFooterProfileClick = homeController::onFooterProfileClick,
                                    onFooterSOSClick = homeController::onFooterSOSClick,
                                    onXCardClick = homeController::onXCardClick,
                                    // Pass event data and visibility state
                                    eventData = eventData,
                                    showEventPopup = showEventPopup,
                                    onDismissEventPopup = homeController::dismissEventPopup,
                                    controller = homeController // <-- Add this
                                )
                            }

                            composable("locations") {
                                // Check if this is from emergency contact view
                                val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)
                                
                                // Reset the intent extra to avoid persisting it across navigations
                                if (isEmergencyContact) {
                                    Log.d("MainActivity", "Locations route accessed with isEmergencyContact=true")
                                    intent.removeExtra("isEmergencyContact")
                                } else {
                                    Log.d("MainActivity", "Locations route accessed with isEmergencyContact=false (normal navigation)")
                                }
                                
                                LocationsScreen(
                                    navController = navController,
                                    controller = locationsController,
                                    isEmergencyContact = isEmergencyContact,
                                    showHeader = true,
                                    onBackToHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
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

                            composable("core_values") {
                                CoreValuesScreen(
                                    onBackPressed = { navController.popBackStack() }
                                )
                            }
                        }

                       // Delay printNavigationGraph until NavHost is fully initialized
                       LaunchedEffect(Unit) {
                        navigator.printNavigationGraph()
                        }
                    }
                }
            }
        }
    }
}
