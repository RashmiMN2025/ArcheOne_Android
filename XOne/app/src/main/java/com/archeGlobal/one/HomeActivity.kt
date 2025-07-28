package com.archeGlobal.one

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.archeGlobal.one.controller.*
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.ui.screens.*
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.BiometricHelper
import com.archeGlobal.one.utils.UserDataManager
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLDecoder

class HomeActivity : AppCompatActivity() {
    private lateinit var controller: HomeController
    private lateinit var otpVerificationController: OtpVerificationController
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
    private lateinit var communiqueController: CommuniqueController
    private lateinit var archeOdysseyController: ArcheOdysseyController
    private lateinit var travelController: TravelController
    private lateinit var userDataManager: UserDataManager
    private lateinit var navigator: AndroidNavigator
    private lateinit var greetingsController: GreetingsController
    private lateinit var ideaVaultController: IdeaVaultController
    private lateinit var holidayOptionsController: HolidayOptionsController
    private lateinit var helpDeskController: HelpDeskController
    private var lastPauseTime: Long = 0
    private val BACKGROUND_THRESHOLD = 1000 * 30 // 30 seconds
    private var isFromLogin = false // Flag to track if we're coming from login
    private var isAuthenticating = mutableStateOf(false) // New state for biometric authentication
    private var isLocked = false
    private var biometricPromptShown = false

    private fun refreshHomeData() {
        val userData = userDataManager.getUserData()
        val token = userDataManager.getAuthToken() ?: return
        val email = userData?.email ?: return
        val mobile = userData?.mobile ?: return
        val employeeId = userData?.employeeId ?: return

        lifecycleScope.launch {
            otpVerificationController.loginWithToken(
                token = token,
                email = email,
                mobile = mobile,
                employeeId = employeeId,
                fromHome = true
            ) { message, isError ->
                if (isError) {
                    if (message.contains("Invalid Token", ignoreCase = true) ||
                        message.contains("Token Expired", ignoreCase = true) ||
                        message.contains("401", ignoreCase = true)
                    ) {
                        Toast.makeText(this@HomeActivity, "Session expired. Please authenticate to continue.", Toast.LENGTH_SHORT).show()

                        // Preserve that this is not a first-time user for session expiry
                        com.archeGlobal.one.utils.setFirstTimeLogin(this@HomeActivity, false)

                        // Clear session data but preserve MPIN and biometric for re-auth
                        userDataManager.clearSessionData()

                        // Navigate to login with session expired flag
                        val intent = Intent(this@HomeActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("session_expired", true)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
                // Update the home controller's data
                controller.refreshUserData()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent

        val navigateTo = intent.getStringExtra("navigateTo")
        val ticketCategory = intent.getStringExtra("ticketCategory")
        val source = intent.getStringExtra("source")

        Log.d("HomeActivity", "onNewIntent called with navigateTo=$navigateTo, ticketCategory=$ticketCategory, source=$source")

        if (navigateTo == "track_tickets" && ticketCategory != null) {
            // Initialize helpdesk controller if not already done and navigate directly
            if (::helpDeskController.isInitialized) {
                // Set the source screen for proper back navigation
                if (source != null) {
                    helpDeskController.setNavigationSource(source)
                }
                // Load tickets data and navigate directly without going through home screen
                helpDeskController.loadTicketsData(ticketCategory)
                navigator.navController?.navigate("track_tickets")
                Log.d("HomeActivity", "Navigating directly to track_tickets with category: $ticketCategory via onNewIntent")
            }
        }
    }

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        userDataManager = UserDataManager.getInstance(this)
        navigator = AndroidNavigator(this)

        // Check if we're coming from login
        val fromLogin = intent.getBooleanExtra("fromLogin", false)

        // If MPIN is not set and we're NOT coming from login, redirect to MPIN setup
        // Users coming from login should not be forced to set up MPIN
        if (!com.archeGlobal.one.utils.MpinManager.checkMpinExists(this)) {
            val loginIntent = android.content.Intent(this, com.archeGlobal.one.LoginActivity::class.java)
            loginIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        // Set the class-level variable
        isFromLogin = fromLogin

        val fromMpin = intent.getBooleanExtra("fromMpin", false)
        val email = intent.getStringExtra("email") ?: userDataManager.getUserData()?.email ?: ""
        val mobile = intent.getStringExtra("mobile") ?: userDataManager.getUserData()?.mobile ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: userDataManager.getUserData()?.employeeId ?: ""
        val token = intent.getStringExtra("token") ?: userDataManager.getAuthToken() ?: ""
        val mpin = intent.getStringExtra("mpin") ?: ""

        // If coming from login or MPIN setup, don't show MPIN prompt on home screen
        if (fromLogin || fromMpin) {
            userDataManager.preferencesManager.setAppLockState(false)
            // Mark authentication session as active when coming from login/MPIN
            XOneApplication.getInstance().getAppLifecycleObserver().setAuthenticationSessionActive(true)
        } else {
            // If not coming from login/MPIN setup and MPIN exists, set lock state to true
            // This ensures MPIN prompt shows when app is reopened
            if (com.archeGlobal.one.utils.MpinManager.checkMpinExists(this)) {
                userDataManager.preferencesManager.setAppLockState(true)
            }
        }

        if ((fromMpin || fromLogin) && !biometricPromptShown) {
            biometricPromptShown = true
            val biometricHelper = com.archeGlobal.one.utils.BiometricHelper(this)
            if (biometricHelper.canUseBiometric() && !biometricHelper.isBiometricEnabled()) {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Enable Fingerprint Login")
                    .setMessage("Would you like to use fingerprint for faster login next time?")
                    .setPositiveButton("Yes") { _, _ ->
                        biometricHelper.showBiometricPrompt(
                            activity = this,
                            title = "Setup Fingerprint",
                            subtitle = "Verify your fingerprint to enable quick login",
                            onSuccess = {
                                biometricHelper.saveCredentials(email, mobile, employeeId, token)
                                userDataManager.preferencesManager.setBiometricEnabled(true)
                                userDataManager.preferencesManager.setAppLockState(false)
                            },
                            onError = { _ -> }
                        )
                    }
                    .setNegativeButton("No", null)
                    .setCancelable(false)
                    .show()
            }
        }

        // Disable back navigation to login
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navController = navigator.navController ?: return
                    if (navController.currentDestination?.route == "greetings") {
                        navigator.navigateToHome()
                    } else {
                        // Either exit the app or show a toast
                        Toast.makeText(this@HomeActivity, "Press Home to exit the app", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        // Initialize controllers that need context
        otpVerificationController = OtpVerificationController(
            navigator = navigator, // Use the existing navigator instance
            context = this
        )

        // Check if we need to navigate to a specific destination
        val destination = intent.getStringExtra("destination")
        val navigateTo = intent.getStringExtra("navigateTo")
        val ticketCategory = intent.getStringExtra("ticketCategory")
        val source = intent.getStringExtra("source")
        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)
        val fromOtp = intent.getBooleanExtra("FROM_OTP", false) // Print the intent extras for debugging
        Log.d("HomeActivity", "onCreate with intent extras: destination=$destination, navigateTo=$navigateTo, ticketCategory=$ticketCategory, source=$source, isEmergencyContact=$isEmergencyContact")
        Log.d("HomeActivity", "All extras: ${intent.extras?.keySet()?.joinToString()}")
        Log.d("HomeActivity", "fromOtp=$fromOtp")

        setContent {
            XOneTheme {
                val navController = rememberNavController()

                // Use the class-level navigator instead of creating a new one
                navigator.setNavController(navController)

                // Initialize controllers with correct parameter order
                controller = HomeController(navigator, this@HomeActivity)

                // Initialize controllers that need context
                holidayCalendarController = HolidayCalendarController(
                    RetrofitClient.apiService,
                    UserRepository(this@HomeActivity),
                    this@HomeActivity
                )

                // Initialize holiday options controller
                holidayOptionsController = HolidayOptionsController(this@HomeActivity, navigator)

                // Initialize greetings controller
                greetingsController = GreetingsController(this@HomeActivity, navigator)
                ideaVaultController = IdeaVaultController(navigator)
                locationsController = LocationsController(this@HomeActivity)
                businessCardController = BusinessCardControllerImpl(this@HomeActivity, navigator)
                policyController = PolicyController(this@HomeActivity, navigator)
                assetController = AssetController(this@HomeActivity, navigator)
                profileController = ProfileController(this@HomeActivity, navigator)
                sosController = SOSController(application)
                aboutMeController = AboutMeController(navigator)
                addressController = AddressController(navigator)
                emergencyContactController = EmergencyContactController(navigator)
                chatController = ChatController(this@HomeActivity, navigator)
                communiqueController = CommuniqueController(this@HomeActivity, navigator)
                archeOdysseyController = ArcheOdysseyController(navigator)
                // Initialize travel controller as class-level property
                travelController = TravelController(navigator, this@HomeActivity)
                // Log that the travel controller was initialized
                android.util.Log.d("HomeActivity", "TravelController initialized with navigator: ${navigator.hashCode()}")

                // Initialize helpdesk controller
                helpDeskController = HelpDeskController(this@HomeActivity)
                helpDeskController.setNavigationCallback { route ->
                    navController.navigate(route)
                }
                var isLoading by remember { mutableStateOf(false) }

                // Get the current intent (which might be updated by onNewIntent)
                val currentIntent = intent
                val currentNavigateTo = currentIntent.getStringExtra("navigateTo")
                val currentTicketCategory = currentIntent.getStringExtra("ticketCategory")
                val currentSource = currentIntent.getStringExtra("source")
                val currentDestination = currentIntent.getStringExtra("destination")

                // Determine start destination based on intent
                val startDestination = if (currentNavigateTo == "track_tickets" && currentTicketCategory != null) {
                    "track_tickets"
                } else {
                    "home"
                }

                // Handle data loading and navigation setup
                LaunchedEffect(currentDestination, currentNavigateTo, currentTicketCategory, currentSource, isEmergencyContact) {
                    // Set up track_tickets controller FIRST if that's our destination
                    if (startDestination == "track_tickets" && currentTicketCategory != null) {
                        if (currentSource != null) {
                            helpDeskController.setNavigationSource(currentSource)
                        }
                        // Load tickets data directly without calling navigate()
                        helpDeskController.loadTicketsData(currentTicketCategory)
                        Log.d("HomeActivity", "Starting at track_tickets with category: $currentTicketCategory from source: $currentSource")
                    }

                    if (!fromOtp) {
                        val token = userDataManager.getAuthToken() ?: "your_token_here"
                        isLoading = true // Start loading
                        otpVerificationController.loginWithToken(token, email, mobile, employeeId, true, false, true) { message, isError ->
                            isLoading = false // Stop loading
                            if (isError) {
                                if (message.contains("Invalid Token")) {
                                    Toast.makeText(this@HomeActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                                    // Preserve that this is not a first-time user for session expiry
                                    com.archeGlobal.one.utils.setFirstTimeLogin(this@HomeActivity, false)
                                    // Clear all user data
                                    userDataManager.clearUserData()
                                    navigator.navigateToLoginScreen()
                                }
                                // Don't show toast for non-critical errors when returning to app
                                // This prevents the "Login failed" message from appearing
                                // Log the error instead for debugging purposes
                                Log.d("HomeActivity", "Token refresh result: $message, isError: $isError")
                            } else {
                                // Successful token refresh, update user data silently
                                controller.refreshUserData()
                            }

                            // Handle navigation after token refresh - only if not already at the destination
                            if (startDestination == "home") {
                                currentDestination?.let { dest ->
                                    navController.navigate(dest)
                                }

                                currentNavigateTo?.let { route ->
                                    // Handle track_tickets with category
                                    if (route == "track_tickets" && currentTicketCategory != null) {
                                        // Set the source screen for proper back navigation
                                        if (currentSource != null) {
                                            helpDeskController.setNavigationSource(currentSource)
                                        }
                                        // Load tickets with the specified category before navigating
                                        helpDeskController.navigateToTrackTickets(currentTicketCategory)
                                        Log.d("HomeActivity", "Navigating to track_tickets with category: $currentTicketCategory from source: $currentSource")
                                    } else {
                                        navController.navigate(route)
                                        Log.d("HomeActivity", "Navigating to: $route")
                                    }
                                }
                            }
                        }
                    } else {
                        // Handle navigation immediately if coming from OTP - only if not already at the destination
                        if (startDestination == "home") {
                            currentDestination?.let { dest ->
                                navController.navigate(dest)
                            }

                            currentNavigateTo?.let { route ->
                                // Handle track_tickets with category
                                if (route == "track_tickets" && currentTicketCategory != null) {
                                    // Load tickets with the specified category before navigating
                                    helpDeskController.navigateToTrackTickets(currentTicketCategory)
                                    Log.d("HomeActivity", "Navigating to track_tickets with category: $currentTicketCategory")
                                } else {
                                    navController.navigate(route)
                                    Log.d("HomeActivity", "Navigating to: $route")
                                }
                            }
                        }
                    }
                }
                // UniversalLoader(isLoading = isLoading)
                NavHost(
                    navController = navController,
                    startDestination = startDestination
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
                        val eventData = controller.eventData.collectAsState().value
                        val showEventPopup = controller.showEventPopup.collectAsState().value
                        var showBiometricPrompt by remember { mutableStateOf(fromMpin) }

                        Log.d("HomeActivity", "Event data present: ${eventData != null}, showEventPopup: $showEventPopup")
                        if (eventData != null) {
                            Log.d("HomeActivity", "Event details - Title: ${eventData.title}, Image: ${eventData.image}")
                        } else {
                            Log.d("HomeActivity", "No event data available to display")
                        }

                        ResponsiveHomeScreen(
                            model = controller.model,
                            employeeData = controller.employeeData,
                            onItemClick = controller::onItemClick,
                            onAllAppsClick = controller::onAllAppsClick,
                            onFavoritesClick = controller::onFavoritesClick,
                            onShowProfileClick = controller::onShowProfileClick,
                            onToggleFavorite = controller::onToggleFavorite,
                            onFooterHomeClick = controller::onFooterHomeClick,
                            onFooterChatClick = { navigator.navigateToChat() },
                            onFooterSOSClick = controller::onFooterSOSClick,
                            onFooterProfileClick = controller::onFooterProfileClick,
                            onXCardClick = controller::onXCardClick,
                            isAuthenticating = isAuthenticating.value,
                            onRefresh = { refreshHomeData() },
                            controller = controller,
                            // Pass event-related parameters
                            eventData = eventData,
                            showEventPopup = showEventPopup,
                            onDismissEventPopup = controller::dismissEventPopup,
                            navigator = navigator
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
                        // Call onChatScreenEnter when entering the chat screen
                        LaunchedEffect(Unit) {
                            chatController.onChatScreenEnter()
                        }

                        ChatScreen(
                            viewModel = chatController.viewModel,
                            navController = navController,
                            onBackPressed = chatController::onBackPressed,
                            showBottomBar = true
                        )
                    }

                    // Add raise concern screen with customizable title
                    composable(
                        route = "raise_concern/{title}?category={category}",
                        arguments = listOf(
                            navArgument("title") { type = NavType.StringType },
                            navArgument("category") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
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
                        val title = backStackEntry.arguments?.getString("title") ?: "Raise a Concern"
                        val decodedTitle = java.net.URLDecoder.decode(title, "UTF-8")
                        val category = backStackEntry.arguments?.getString("category")
                        val decodedCategory = category?.let { java.net.URLDecoder.decode(it, "UTF-8") }

                        RaiseConcernScreen(
                            onBackPressed = { navController.popBackStack() },
                            title = decodedTitle,
                            prefilledCategory = decodedCategory
                        )
                    }

                    composable(
                        route = "locations?showHeader={showHeader}",
                        arguments = listOf(
                            navArgument("showHeader") {
                                type = NavType.BoolType
                                defaultValue = true
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
                        val showHeader = intent.getBooleanExtra("showHeader", false)
                        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)

                        if (isEmergencyContact) {
                            Log.d("HomeActivity", "Locations route accessed with isEmergencyContact=true")
                            intent.removeExtra("isEmergencyContact")
                        }

                        LocationsScreen(
                            navController = navController,
                            controller = locationsController,
                            isEmergencyContact = isEmergencyContact,
                            showHeader = showHeader,
                            onBackToHome = { navigator.navigateToHome() }
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
                        route = "idea_vault",
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
                        IdeaVaultScreen(
                            onBackPressed = { navController.popBackStack() },
                            controller = ideaVaultController, // Pass the initialized controller
                            apiService = RetrofitClient.apiService
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
                            model = policyController.model.value,
                            onPolicyClick = policyController::onPolicyClick,
                            onBackClick = policyController::onBackClick,
                            isLoading = policyController.isLoading.value
                        )
                    }

                    composable(
                        route = "communique",
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
                        CommuniqueScreen(
                            model = communiqueController.model,
                            onCommuniqueClick = communiqueController::onCommuniqueClick,
                            onBackPressed = { navController.popBackStack() },
                            isLoading = communiqueController.isLoading.value
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
                        route = "holiday_options",
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
                        HolidayOptionsScreen(
                            controller = holidayOptionsController
                        )
                    }

                    composable(
                        route = "calendar",
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
                            onBackPressed = { navController.navigate("home") },
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

                    composable("core_values") {
                        CoreValuesScreen(
                            onBackPressed = { navController.popBackStack() }
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
                            onBackPressed = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "vision",
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
                        VisionScreen(
                            onBackPressed = { navigator.navigateToHome() }
                        )
                    }; // <-- Add comma to separate composables

                    composable(
                        route = "greetings",
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
                        GreetingsScreen(
                            controller = greetingsController,
                            onBackPressed = { navigator.navigateToHome() }
                        )
                    }; // <-- Add comma to separate composables

                    composable(
                        route = "global_celebration",
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
                        val globalCelebrationController = GlobalCelebrationController(
                            this@HomeActivity,
                            navigator,
                            greetingsController
                        )
                        GlobalCelebrationScreen(
                            controller = globalCelebrationController,
                            onBackPressed = { navigator.navigateToGreetings() }
                        )
                    }

                    // Add the service_not_available route
                    composable(
                        route = "service_not_available?serviceName={serviceName}",
                        arguments = listOf(
                            navArgument("serviceName") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) { backStackEntry ->
                        val serviceName = backStackEntry.arguments?.getString("serviceName")
                        ServiceNotAvailableScreen(
                            navController = navController,
                            serviceName = serviceName
                        )
                    }

                    // Add the travel route
                    composable(
                        route = "travel",
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
                        TravelScreen(
                            controller = travelController
                        )
                    }

                    // Add the travel_history route
                    composable(
                        route = "travel_history",
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
                        TravelHistoryScreen(
                            controller = travelController
                        )
                    }

                    // Add the travel_request_detail route
                    composable(
                        route = "travel_request_detail",
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
                        // Only show the detail screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelRequestDetailScreen(
                                controller = travelController,
                                travelRequest = travelRequest
                            )
                        } ?: run {
                            // If no travel request is selected, go back to travel history
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Add the travel_history_detail route
                    composable(
                        route = "travel_history_detail",
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
                        // Only show the detail screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelHistoryDetailScreen(
                                controller = travelController,
                                travelRequest = travelRequest
                            )
                        } ?: run {
                            // If no travel request is selected, go back to travel history
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Add the travel_approvals route
                    composable(
                        route = "travel_approvals",
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
                        TravelApprovalsScreen(
                            controller = travelController
                        )
                    }

                    // Add the travel_approval_detail route
                    composable(
                        route = "travel_approval_detail",
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
                        // Only show the detail screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelApprovalDetailScreen(
                                controller = travelController,
                                travelRequest = travelRequest
                            )
                        } ?: run {
                            // If no travel request is selected, go back to travel approvals
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Add the travel_approval_confirm route
                    composable(
                        route = "travel_approval_confirm",
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
                        // Only show the confirmation screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelApprovalConfirmScreen(
                                controller = travelController,
                                travelRequest = travelRequest
                            )
                        } ?: run {
                            // If no travel request is selected, go back to travel approvals
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Add the travel_approve route
                    composable(
                        route = "travel_approve",
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
                        // Only show the approval screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelApproveScreen(
                                controller = travelController,
                                travelRequest = travelRequest
                            )
                        } ?: run {
                            // If no travel request is selected, go back to travel approvals
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Add the travel_reject route
                    composable(
                        route = "travel_reject",
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
                        // Only show the rejection screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelRejectScreen(
                                controller = travelController,
                                travelRequest = travelRequest
                            )
                        } ?: run {
                            // If no travel request is selected, go back to travel approvals
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Add the travel_approval_details route
                    composable(
                        route = "travel_approval_details",
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
                        travelController.selectedTravelRequest?.let { tr ->
                            TravelApprovalDetailsScreen(controller = travelController, travelRequest = tr)
                        } ?: run {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        }
                    }

                    // Helpdesk routes
                    composable(
                        route = "helpdesk",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
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
                        HelpDeskScreen(controller = helpDeskController)
                    }

                    composable(
                        route = "track_tickets",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
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
                        TicketTrackingScreen(controller = helpDeskController)
                    }

                    composable(
                        route = "faq_detail/{faqId}",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }
                    ) { backStackEntry ->
                        val faqId = backStackEntry.arguments?.getString("faqId") ?: ""
                        FAQDetailScreen(faqId = faqId, controller = helpDeskController)
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

    override fun onResume() {
        super.onResume()
        val biometricHelper = BiometricHelper(this)
        val isLoggedIn = userDataManager.isLoggedIn()
        val isLocked = userDataManager.preferencesManager.getAppLockState()
        val appLifecycleObserver = XOneApplication.getInstance().getAppLifecycleObserver()

        if (isLoggedIn) {
            refreshHomeData()
        }

        // Only show biometric if:
        // 1. User is logged in
        // 2. Biometric is available and enabled
        // 3. App is locked (this is set by AppLifecycleObserver when app goes to background)
        // 4. Haven't already authenticated in this session
        // 5. Not already showing authentication
        if (isLoggedIn &&
            biometricHelper.canUseBiometric() &&
            biometricHelper.isBiometricEnabled() &&
            isLocked &&
            !appLifecycleObserver.isAuthenticationSessionActive() &&
            !isAuthenticating.value
        ) {
            isAuthenticating.value = true
            biometricHelper.showBiometricPrompt(
                activity = this,
                onSuccess = {
                    isAuthenticating.value = false
                    appLifecycleObserver.setAuthenticationSessionActive(true)
                    userDataManager.preferencesManager.setAppLockState(false) // Unlock the app
                },
                onError = { error ->
                    isAuthenticating.value = false
                    Log.w("HomeActivity", "Biometric authentication failed: $error")

                    // On authentication error/cancellation, navigate to login with session expired
                    // This allows users to use MPIN or other auth methods as fallback
                    val intent = Intent(this@HomeActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("session_expired", true)
                    }
                    startActivity(intent)
                    finish()
                }
            )
        }
    }

    /**
     * Provides access to the ChatController for other components
     * Used by AndroidNavigator to clear chat history when navigating
     */
    fun getChatController(): ChatController? {
        return if (::chatController.isInitialized) chatController else null
    }
}
