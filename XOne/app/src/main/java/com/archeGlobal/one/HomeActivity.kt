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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.archeGlobal.one.controller.*
import com.archeGlobal.one.controller.ConsumptionReportController
import com.archeGlobal.one.controller.SmartCollateralController
import com.archeGlobal.one.model.DeskCartOrderHistory
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.components.WhatsNewDialog
import com.archeGlobal.one.ui.screens.*
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.BiometricHelper
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import com.google.gson.Gson
import java.net.URLDecoder

class HomeActivity : AppCompatActivity() {
    private lateinit var controller: HomeController
    private lateinit var otpVerificationController: OtpVerificationController
    private lateinit var userDataManager: UserDataManager
    private lateinit var navigator: AndroidNavigator
    private lateinit var helpDeskController: HelpDeskController
    private lateinit var meetSpaceController: MeetSpaceController
    private lateinit var preferencesManager: PreferencesManager

    // Lazy-loaded controllers - only initialized when actually needed
    internal val locationsController by lazy {
        Log.d("HomeActivity", "Lazy initializing LocationsController")
        LocationsController(this@HomeActivity)
    }
    internal val businessCardController by lazy {
        Log.d("HomeActivity", "Lazy initializing BusinessCardController")
        BusinessCardControllerImpl(this@HomeActivity, navigator)
    }
    internal val policyController by lazy {
        Log.d("HomeActivity", "Lazy initializing PolicyController")
        PolicyController(this@HomeActivity, navigator)
    }
    internal val assetController by lazy {
        Log.d("HomeActivity", "Lazy initializing AssetController")
        AssetController(this@HomeActivity, navigator)
    }
    internal val profileController by lazy {
        Log.d("HomeActivity", "Lazy initializing ProfileController")
        ProfileController(this@HomeActivity, navigator)
    }
    private val sosController by lazy {
        Log.d("HomeActivity", "Lazy initializing SOSController")
        SOSController(application)
    }
    private val holidayCalendarController by lazy {
        Log.d("HomeActivity", "Lazy initializing HolidayCalendarController")
        HolidayCalendarController(RetrofitClient.apiService, UserRepository(this@HomeActivity), this@HomeActivity)
    }
    private val aboutMeController by lazy {
        Log.d("HomeActivity", "Lazy initializing AboutMeController")
        AboutMeController(navigator)
    }
    private val addressController by lazy {
        Log.d("HomeActivity", "Lazy initializing AddressController")
        AddressController(navigator)
    }
    private val emergencyContactController by lazy {
        Log.d("HomeActivity", "Lazy initializing EmergencyContactController")
        EmergencyContactController(navigator)
    }
    internal val chatController by lazy {
        Log.d("HomeActivity", "Lazy initializing ChatController")
        ChatController(this@HomeActivity, navigator)
    }
    private val communiqueController by lazy {
        Log.d("HomeActivity", "Lazy initializing CommuniqueController")
        CommuniqueController(this@HomeActivity, navigator)
    }
    private val archeOdysseyController by lazy {
        Log.d("HomeActivity", "Lazy initializing ArcheOdysseyController")
        ArcheOdysseyController(navigator)
    }
    internal val travelController by lazy {
        Log.d("HomeActivity", "Lazy initializing TravelController")
        TravelController(navigator, this@HomeActivity)
    }
    private val greetingsController by lazy {
        Log.d("HomeActivity", "Lazy initializing GreetingsController")
        GreetingsController(this@HomeActivity, navigator)
    }
    private val ideaVaultController by lazy {
        Log.d("HomeActivity", "Lazy initializing IdeaVaultController")
        IdeaVaultController(this@HomeActivity, navigator)
    }
    private val holidayOptionsController by lazy {
        Log.d("HomeActivity", "Lazy initializing HolidayOptionsController")
        HolidayOptionsController(this@HomeActivity, navigator)
    }
    private val orderController by lazy {
        Log.d("HomeActivity", "Lazy initializing OrderController")
        OrderController(this@HomeActivity, navigator, lifecycleScope)
    }
    private val consumptionReportController by lazy {
        Log.d("HomeActivity", "Lazy initializing ConsumptionReportController")
        ConsumptionReportController(this@HomeActivity, navigator)
    }
    private val smartCollateralcontroller by lazy {
        Log.d("HomeActivity", "Lazy initializing SmartCollateralController")
        SmartCollateralController(this@HomeActivity)
    }
    internal val approvalRequestsController by lazy {
        Log.d("HomeActivity", "Lazy initializing ApprovalRequestsController")
        ApprovalRequestsController(this@HomeActivity)
    }
    internal val deskCartController by lazy {
        Log.d("HomeActivity", "Lazy initializing DeskCartController")
        Log.d("HomeActivity", "DeskCartController access stack trace: ${Thread.currentThread().stackTrace.take(10).joinToString("\n")}")
        DeskCartController(this@HomeActivity, navigator)
    }
    internal val myRequestsController by lazy {
        Log.d("HomeActivity", "Lazy initializing MyRequestsController")
        MyRequestsController(this@HomeActivity)
    }
    // Lazy controllers can be accessed directly by property name
    // No explicit getter methods needed - Kotlin generates them automatically

    private var lastPauseTime: Long = 0
    private val BACKGROUND_THRESHOLD = 1000 * 30 // 30 seconds
    private var isFromLogin = false // Flag to track if we're coming from login
    private var isAuthenticating = mutableStateOf(false) // New state for biometric authentication
    private var isLocked = false
    private var biometricPromptShown = false
    private var currentSourceActivity = mutableStateOf<String?>(null) // Track source activity for back navigation
    private var showUpdateDialog by mutableStateOf(false)

    private fun refreshHomeData() {
        // Refresh home screen data without API calls
        Log.d("HomeActivity", "Refreshing home screen data without API calls")

        // Update the home controller's data
        controller.refreshUserData()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent

        val navigateTo = intent.getStringExtra("navigateTo")
        val ticketCategory = intent.getStringExtra("ticketCategory")
        val source = intent.getStringExtra("source")
        val clearBackStack = intent.getBooleanExtra("clearBackStack", false)
        val directNavigateTo = intent.getStringExtra("direct_navigate_to")
        val sourceActivity = intent.getStringExtra("source_activity")

        // Handle direct action-based navigation
        val directAction = intent.action

        Log.d(
            "HomeActivity",
            "onNewIntent called with navigateTo=$navigateTo, directNavigateTo=$directNavigateTo, sourceActivity=$sourceActivity, ticketCategory=$ticketCategory, source=$source, clearBackStack=$clearBackStack",
        )

        // Handle direct navigation from DeskCart
        if (directNavigateTo == "order_history") {
            Log.d("HomeActivity", "Direct navigation to order_history via onNewIntent")
            // Update the source activity state for proper back navigation
            currentSourceActivity.value = sourceActivity
            Log.d("HomeActivity", "Updated currentSourceActivity to: $sourceActivity")
            navigator.navController?.navigate("order_history") {
                launchSingleTop = true
            }
            return
        }

        // Handle clear back stack and navigate to home
        if (clearBackStack && navigateTo == "home") {
            navigator.navController?.navigate("home") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            Log.d("HomeActivity", "Cleared back stack and navigated to home via onNewIntent")
            return
        }

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

        Log.d("HomeActivity", "onCreate called")
        Log.d("HomeActivity", "Intent extras: ${intent.extras?.keySet()?.joinToString()}")
        Log.d("HomeActivity", "direct_navigate_to: ${intent.getStringExtra("direct_navigate_to")}")

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        userDataManager = UserDataManager.getInstance(this)
        navigator = AndroidNavigator(this)
        controller = HomeController(navigator, this)
        helpDeskController = HelpDeskController(this)
        preferencesManager = PreferencesManager(this)

        // Check if we're coming from login
        val fromLogin = intent.getBooleanExtra("fromLogin", false)

        // If MPIN is not set and we're NOT coming from login, redirect to MPIN setup
        // Users coming from login should not be forced to set up MPIN
        if (!com.archeGlobal.one.utils.MpinManager
                .checkMpinExists(this)
        ) {
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
            if (com.archeGlobal.one.utils.MpinManager
                    .checkMpinExists(this)
            ) {
                userDataManager.preferencesManager.setAppLockState(true)
            }
        }

        if ((fromMpin || fromLogin) && !biometricPromptShown) {
            biometricPromptShown = true
            val biometricHelper =
                com.archeGlobal.one.utils
                    .BiometricHelper(this)
            if (biometricHelper.canUseBiometric() && !biometricHelper.isBiometricEnabled()) {
                android.app.AlertDialog
                    .Builder(this)
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
                            onError = { _ -> },
                        )
                    }.setNegativeButton("No", null)
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
            },
        )

        // Initialize controllers that need context
        otpVerificationController =
            OtpVerificationController(
                navigator = navigator, // Use the existing navigator instance
                context = this,
            )

        // Check if we need to navigate to a specific destination
        val destination = intent.getStringExtra("destination")
        val navigateTo = intent.getStringExtra("navigateTo")
        val ticketCategory = intent.getStringExtra("ticketCategory")
        val source = intent.getStringExtra("source")
        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)
        val fromOtp = intent.getBooleanExtra("FROM_OTP", false) // Print the intent extras for debugging
        Log.d(
            "HomeActivity",
            "onCreate with intent extras: destination=$destination, navigateTo=$navigateTo, ticketCategory=$ticketCategory, source=$source, isEmergencyContact=$isEmergencyContact",
        )
        Log.d("HomeActivity", "All extras: ${intent.extras?.keySet()?.joinToString()}")
        Log.d("HomeActivity", "fromOtp=$fromOtp")

        setContent {
            XOneTheme {
                val navController = rememberNavController()

                // Use the class-level navigator instead of creating a new one
                navigator.setNavController(navController)

                // HomeController already initialized in onCreate() - don't recreate it!

                // Controllers are now lazy-loaded - they will be initialized only when accessed
                // This prevents bulk API calls on app startup
                android.util.Log.d("HomeActivity", "Controllers converted to lazy initialization - no bulk loading!")

                // Set navigation callback for helpdesk controller (already initialized in onCreate)
                helpDeskController.setNavigationCallback { route ->
                    navController.navigate(route)
                }

                meetSpaceController = MeetSpaceController(this)

                if (intent.getBooleanExtra("showUpdateDialog", false) || preferencesManager.getBoolean("showUpdateDialog", false)) {
                    showUpdateDialog = true
                }

                var isLoading by remember { mutableStateOf(false) }

                // Get the current intent (which might be updated by onNewIntent)
                val currentIntent = intent
                val currentNavigateTo = currentIntent.getStringExtra("navigateTo")
                val currentTicketCategory = currentIntent.getStringExtra("ticketCategory")
                val currentSource = currentIntent.getStringExtra("source")
                val currentDestination = currentIntent.getStringExtra("destination")
                val clearBackStack = currentIntent.getBooleanExtra("clearBackStack", false)
                val currentAction = currentIntent.action
                val directNavigateTo = currentIntent.getStringExtra("direct_navigate_to")
                val sourceActivity = currentIntent.getStringExtra("source_activity")
                val orderData = currentIntent.getStringExtra("orderData")

                // Restore order data from intent if navigating to order_history_detail
                if (currentNavigateTo == "order_history_detail" && orderData != null) {
                    try {
                        val order = Gson().fromJson(orderData, DeskCartOrderHistory::class.java)
                        OrderHistoryController.selectedOrderForDetails = order
                        Log.d("HomeActivity", "Restored order data from intent for order: ${order.order_Id}")
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Failed to parse order data from intent", e)
                    }
                }

                // Use reactive state for source activity (updated by onNewIntent)
                val reactiveSourceActivity by currentSourceActivity
                val effectiveSourceActivity = reactiveSourceActivity ?: sourceActivity

                // Set initial source activity state if not already set
                LaunchedEffect(sourceActivity) {
                    if (currentSourceActivity.value == null && sourceActivity != null) {
                        currentSourceActivity.value = sourceActivity
                        Log.d("HomeActivity", "Set initial currentSourceActivity to: $sourceActivity")
                    }
                }

                // Determine start destination based on intent - SIMPLE LOGIC
                val startDestination =
                    when {
                        directNavigateTo == "order_history" -> "order_history" // NEW: Direct navigation from DeskCart
                        currentAction == "navigate_to_order_history" -> "order_history" // Direct action navigation
                        clearBackStack && currentNavigateTo == "home" -> "home" // Force home when clearing back stack
                        currentNavigateTo == "track_tickets" && currentTicketCategory != null -> "track_tickets"
                        currentNavigateTo == "order_received" -> "order_received"
                        currentNavigateTo == "order_history" -> "order_history"
                        currentNavigateTo == "order_history_detail" -> "home" // Start at home then navigate to detail
                        currentNavigateTo == "consumption_report" && !clearBackStack -> "consumption_report"
                        else -> "home"
                    }

                Log.d(
                    "HomeActivity",
                    "Intent parameters - navigateTo: $currentNavigateTo, directNavigateTo: $directNavigateTo, startDestination: $startDestination",
                )

                // Handle data loading and navigation setup
                LaunchedEffect(
                    currentDestination,
                    currentNavigateTo,
                    currentTicketCategory,
                    currentSource,
                    isEmergencyContact,
                    clearBackStack,
                    directNavigateTo,
                ) {
                    // Set up track_tickets controller FIRST if that's our destination
                    if (startDestination == "track_tickets" && currentTicketCategory != null) {
                        if (currentSource != null) {
                            helpDeskController.setNavigationSource(currentSource)
                        }
                        // Load tickets data directly without calling navigate()
                        helpDeskController.loadTicketsData(currentTicketCategory)
                        Log.d("HomeActivity", "Starting at track_tickets with category: $currentTicketCategory from source: $currentSource")
                    }

                    // Handle order_history destination (similar to track_tickets setup)
                    if (startDestination == "order_history") {
                        Log.d("HomeActivity", "Starting at order_history destination from source: $currentSource")
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
                                    com.archeGlobal.one.utils
                                        .setFirstTimeLogin(this@HomeActivity, false)
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
                                // Trigger celebration data fetch after token refresh
                                controller.onLoginCompleted()
                                // Note: HelpDesk data will be loaded only when user navigates to HelpDesk screen
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
                                        Log.d(
                                            "HomeActivity",
                                            "Navigating to track_tickets with category: $currentTicketCategory from source: $currentSource",
                                        )
                                    } else if (route == "order_history") {
                                        navController.navigate("order_history")
                                        Log.d("HomeActivity", "Navigating to order_history")
                                    } else if (route == "order_history_detail") {
                                        val orderId = currentIntent.getStringExtra("orderId") ?: ""
                                        navController.navigate("order_history_detail/$orderId")
                                        Log.d("HomeActivity", "Navigating to order_history_detail with orderId: $orderId")
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
                                } else if (route == "order_history_detail") {
                                    val orderId = currentIntent.getStringExtra("orderId") ?: ""
                                    navController.navigate("order_history_detail/$orderId")
                                    Log.d("HomeActivity", "OTP flow: Navigating to order_history_detail with orderId: $orderId")
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
                    startDestination = startDestination,
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
                        },
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
                            onFooterHeadsUpClick = { navController.navigate("headsup") },
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
                            navigator = navigator,
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
                        },
                    ) {
                        // Call onChatScreenEnter when entering the chat screen
                        LaunchedEffect(Unit) {
                            chatController.onChatScreenEnter()
                        }

                        ChatScreen(
                            viewModel = chatController.viewModel,
                            navController = navController,
                            onBackPressed = chatController::onBackPressed,
                            showBottomBar = true,
                        )
                    }

                    // Add HeadsUp screen composable
                    composable(
                        route = "headsup",
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
                        },
                    ) {
                        HeadsUpScreen(
                            footerNavigation = controller.model.footerNavigation,
                            isUsingPrideIcon = controller.isUsingPrideIcon(),
                            onFooterHomeClick = { navController.navigate("home") },
                            onFooterChatClick = { navController.navigate("chat") },
                            onFooterHeadsUpClick = { /* Already on HeadsUp */ },
                            onFooterSOSClick = { navController.navigate("sos") },
                            onFooterProfileClick = { navController.navigate("profile") },
                            onHistoryClick = { tab -> navController.navigate("post_history?initialTab=$tab") },
                        )
                    }

                    // Post History Screen
                    composable(
                        route = "post_history?initialTab={initialTab}",
                        arguments = listOf(
                            navArgument("initialTab") {
                                type = NavType.IntType
                                defaultValue = 0
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
                        },
                    ) { backStackEntry ->
                        val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
                        PostHistoryScreen(
                            onBackPressed = { navController.popBackStack() },
                            initialTab = initialTab
                        )
                    }

                    // Add raise concern screen with customizable title
                    composable(
                        route = "raise_concern/{title}?category={category}&subcategory={subcategory}&faqId={faqId}",
                        arguments =
                            listOf(
                                navArgument("title") { type = NavType.StringType },
                                navArgument("category") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("subcategory") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("faqId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
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
                        },
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title") ?: "Raise a Concern"
                        val decodedTitle = java.net.URLDecoder.decode(title, "UTF-8")
                        val category = backStackEntry.arguments?.getString("category")
                        val decodedCategory = category?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                        val subcategory = backStackEntry.arguments?.getString("subcategory")
                        val decodedSubcategory = subcategory?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                        val faqId = backStackEntry.arguments?.getString("faqId")
                        val decodedFaqId = faqId?.let { java.net.URLDecoder.decode(it, "UTF-8") }

                        RaiseConcernScreen(
                            onBackPressed = { navController.popBackStack() },
                            title = decodedTitle,
                            prefilledCategory = decodedCategory,
                            prefilledSubcategory = decodedSubcategory,
                            prefilledFaqId = decodedFaqId,
                            helpDeskController = helpDeskController,
                            onNavigateToTrackTickets = { category ->
                                helpDeskController.navigateToTrackTickets(category)
                            },
                        )
                    }

                    composable(
                        route = "locations?showHeader={showHeader}",
                        arguments =
                            listOf(
                                navArgument("showHeader") {
                                    type = NavType.BoolType
                                    defaultValue = true
                                },
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
                        },
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
                            onBackToHome = { navigator.navigateToHome() },
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
                        },
                    ) {
                        BusinessCardScreen(
                            businessCard = businessCardController.businessCard,
                            controller = businessCardController,
                            onBackPressed = { navController.popBackStack() },
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
                        },
                    ) {
                        IdeaVaultScreen(
                            onBackPressed = { navController.popBackStack() },
                            controller = ideaVaultController, // Pass the initialized controller
                            apiService = RetrofitClient.apiService,
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
                        },
                    ) {
                        PolicyScreen(
                            model = policyController.model.value,
                            onPolicyClick = policyController::onPolicyClick,
                            onBackClick = policyController::onBackClick,
                            isLoading = policyController.isLoading.value,
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
                        },
                    ) {
                        CommuniqueScreen(
                            model = communiqueController.model,
                            onCommuniqueClick = communiqueController::onCommuniqueClick,
                            onBackPressed = { navController.popBackStack() },
                            isLoading = communiqueController.isLoading.value,
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
                        },
                    ) {
                        AssetScreen(
                            model = assetController.model,
                            controller = assetController,
                            onBackPressed = { navController.popBackStack() },
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
                        },
                    ) {
                        HolidayOptionsScreen(
                            controller = holidayOptionsController,
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
                        },
                    ) {
                        HolidayCalendarScreen(
                            controller = holidayCalendarController,
                            onBackPressed = { navController.navigate("home") },
                            onMonthClick = { month ->
                                navController.navigate("monthDetail/$month")
                            },
                            onHolidayListClick = { pdfUrl ->
                                // Use our PDFViewerScreen with navigator
                                val currentYear =
                                    java.util.Calendar
                                        .getInstance()
                                        .get(java.util.Calendar.YEAR)
                                navigator.navigateToPDFViewer(pdfUrl, "Holiday List $currentYear")
                                Log.d("HomeActivity", "Opening holiday list PDF in PDFViewerScreen: $pdfUrl")
                            },
                        )
                    }

                    composable("monthDetail/{month}") { backStackEntry ->
                        val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 1
                        MonthDetailScreen(
                            month = month,
                            controller = holidayCalendarController,
                            onBackPressed = { navController.popBackStack() },
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
                        },
                    ) {
                        ProfileScreen(
                            controller = profileController,
                            footerNavigation =
                                FooterNavigationModel(
                                    showHome = false,
                                    showChat = false,
                                    showSOS = false,
                                    showProfile = true,
                                ),
                            onFooterHomeClick = { navController.navigate("home") },
                            onFooterChatClick = { navigator.navigateToChat() },
                            onFooterHeadsUpClick = { navController.navigate("headsup") },
                            onFooterSOSClick = { navController.navigate("sos") },
                            onFooterProfileClick = { /* Already on Profile screen */ },
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
                        },
                    ) {
                        AboutMeScreen(
                            controller = aboutMeController,
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
                        },
                    ) {
                        AddressDetailsScreen(
                            controller = addressController,
                        )
                    }

                    composable("core_values") {
                        CoreValuesScreen(
                            onBackPressed = { navController.popBackStack() },
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
                        },
                    ) {
                        EmergencyContactScreen(
                            controller = emergencyContactController,
                        )
                    }

                    composable(
                        route = "pdf_viewer/{pdfUrl}?title={title}",
                        arguments =
                            listOf(
                                navArgument("pdfUrl") {
                                    type = NavType.StringType
                                    nullable = false
                                },
                                navArgument("title") {
                                    type = NavType.StringType
                                    defaultValue = "PDF Viewer"
                                },
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
                        },
                    ) { backStackEntry ->
                        val encodedPdfUrl = backStackEntry.arguments?.getString("pdfUrl") ?: ""
                        val title = backStackEntry.arguments?.getString("title") ?: "PDF Viewer"

                        // Safely decode the URL
                        val pdfUrl =
                            try {
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
                            onBackClick = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = "sos?showHeader={showHeader}",
                        arguments =
                            listOf(
                                navArgument("showHeader") {
                                    type = NavType.BoolType
                                    defaultValue = true // Default to true if not provided
                                },
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
                        },
                    ) { backStackEntry ->
                        val showHeader = backStackEntry.arguments?.getBoolean("showHeader") ?: true

                        var showRaiseConcern by remember { mutableStateOf(false) }

                        if (showRaiseConcern) {
                            RaiseConcernScreen(
                                onBackPressed = { showRaiseConcern = false },
                                onNavigateToTrackTickets = { category ->
                                    helpDeskController.navigateToTrackTickets(category)
                                },
                            )
                        } else {
                            SOSScreen(
                                controller = sosController,
                                onNavigateToRaiseConcern = { showRaiseConcern = true },
                                onBackPressed = if (showHeader) {
                                    { /* footer tab – back disabled */ }
                                } else {
                                    { navController.popBackStack() }
                                },
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
                                onFooterHeadsUpClick = {
                                    navController.navigate("headsup")
                                },
                                onFooterSOSClick = {
                                    // Already on SOS screen, do nothing
                                },
                                onFooterProfileClick = {
                                    // Navigate to profile
                                    navController.navigate("profile")
                                },
                                showHeader = showHeader, // Pass the showHeader value dynamically
                            )
                        }
                    }

                    composable(
                        "sosDetail/{blog}",
                        arguments = listOf(navArgument("blog") { type = NavType.StringType }),
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
                        },
                    ) { backStackEntry ->
                        val json = backStackEntry.arguments?.getString("blog")
                        val blog = Gson().fromJson(json, SosBlogModel::class.java)

                        SOSDetailScreen(
                            blog = blog,
                            onBackPressed = { navController.popBackStack() },
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
                        },
                    ) {
                        VisionScreen(
                            onBackPressed = { navigator.navigateToHome() },
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
                        },
                    ) {
                        GreetingsScreen(
                            controller = greetingsController,
                            onBackPressed = { navigator.navigateToHome() },
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
                        },
                    ) {
                        val globalCelebrationController =
                            GlobalCelebrationController(
                                this@HomeActivity,
                                navigator,
                                greetingsController,
                            )
                        GlobalCelebrationScreen(
                            controller = globalCelebrationController,
                            onBackPressed = { navigator.navigateToGreetings() },
                        )
                    }

                    // Add the service_not_available route
                    composable(
                        route = "service_not_available?serviceName={serviceName}",
                        arguments =
                            listOf(
                                navArgument("serviceName") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
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
                        },
                    ) { backStackEntry ->
                        val serviceName = backStackEntry.arguments?.getString("serviceName")
                        ServiceNotAvailableScreen(
                            navController = navController,
                            serviceName = serviceName,
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
                        },
                    ) {
                        TravelScreen(
                            controller = travelController,
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
                        },
                    ) {
                        TravelHistoryScreen(
                            controller = travelController,
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
                        },
                    ) {
                        // Only show the detail screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelRequestDetailScreen(
                                controller = travelController,
                                travelRequest = travelRequest,
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
                        },
                    ) {
                        // Only show the detail screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelHistoryDetailScreen(
                                controller = travelController,
                                travelRequest = travelRequest,
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
                        },
                    ) {
                        TravelApprovalsScreen(
                            controller = travelController,
                        )
                    }

                    // Add the travel_admin_dashboard route
                    composable(
                        route = "travel_admin_dashboard",
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
                        },
                    ) {
                        TravelAdminDashboardScreen(
                            controller = travelController,
                        )
                    }

                    // Add the travel_approval_detail route
                    composable(
                        route = "travel_approval_detail",
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
                        },
                    ) {
                        // Only show the detail screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelApprovalDetailScreen(
                                controller = travelController,
                                travelRequest = travelRequest,
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
                        },
                    ) {
                        // Only show the confirmation screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelApprovalConfirmScreen(
                                controller = travelController,
                                travelRequest = travelRequest,
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
                        },
                    ) {
                        // Only show the approval screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelApproveScreen(
                                controller = travelController,
                                travelRequest = travelRequest,
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
                        },
                    ) {
                        // Only show the rejection screen if a travel request is selected
                        travelController.selectedTravelRequest?.let { travelRequest ->
                            TravelRejectScreen(
                                controller = travelController,
                                travelRequest = travelRequest,
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
                        },
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
                        },
                    ) {
                        HelpDeskScreen(controller = helpDeskController)
                    }

                    composable(
                        route = "track_tickets",
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
                        },
                    ) {
                        TicketTrackingScreen(
                            controller = helpDeskController,
                        )
                    }

                    composable(
                        route = "faq_detail/{faqId}",
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
                        },
                    ) { backStackEntry ->
                        val faqId = backStackEntry.arguments?.getString("faqId") ?: ""
                        FAQDetailScreen(faqId = faqId, controller = helpDeskController)
                    }
                    // Order details route
                    composable(
                        route = "order_details/{orderId}",
                        arguments =
                            listOf(
                                navArgument("orderId") {
                                    type = NavType.StringType
                                    nullable = false
                                },
                            ),
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300),
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        },
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                        // Get order details from companion object
                        OrderReceivedController.selectedOrderForDetails?.let { orderItem ->
                            val orderHistoryDetailsController =
                                remember {
                                    OrderHistoryDetailsController(this@HomeActivity, navigator)
                                }
                            OrderDetailsScreen(
                                controller = orderHistoryDetailsController,
                                orderItem = orderItem,
                            )
                        } ?: run {
                            // Show error state if no order found
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Order not found",
                                        color = Color.Red,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 16.sp,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navigator.navigateToOrderReceived() },
                                    ) {
                                        Text("Go Back")
                                    }
                                }
                            }
                        }
                    }

                    // Order History Detail route
                    composable(
                        route = "order_history_detail/{orderId}",
                        arguments =
                            listOf(
                                navArgument("orderId") {
                                    type = NavType.StringType
                                    nullable = false
                                },
                            ),
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300),
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300),
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300),
                            )
                        },
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                        // Get order details from OrderHistoryController companion object
                        OrderHistoryController.selectedOrderForDetails?.let { orderItem ->
                            val orderHistoryController =
                                remember(effectiveSourceActivity) {
                                    OrderHistoryController(this@HomeActivity, navigator, effectiveSourceActivity)
                                }
                            OrderHistoryDetailScreen(
                                controller = orderHistoryController,
                                orderItem = orderItem,
                            )
                        } ?: run {
                            // Show error state if no order found
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Order not found",
                                        color = Color.Red,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 16.sp,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navigator.navigateToOrderHistory() },
                                    ) {
                                        Text("Go Back to Order History")
                                    }
                                }
                            }
                        }
                    }

                    // Order Received route - for admin dashboard
                    composable(
                        route = "order_received",
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
                        },
                    ) {
                        val orderReceivedController =
                            remember {
                                OrderReceivedController(this@HomeActivity, navigator)
                            }
                        OrderReceivedScreen(
                            model = orderReceivedController.model,
                            controller = orderReceivedController,
                        )
                    }

                    // Order History route - for user's DeskCart order history
                    composable(
                        route = "order_history",
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
                        },
                    ) {
                        val orderHistoryController =
                            remember(effectiveSourceActivity) {
                                OrderHistoryController(this@HomeActivity, navigator, effectiveSourceActivity)
                            }
                        OrderHistoryScreen(
                            model = orderHistoryController.model,
                            controller = orderHistoryController,
                        )
                    }

                    // Consumption Report route - for admin dashboard
                    composable(
                        route = "consumption_report",
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
                        },
                    ) {
                        ConsumptionReportScreen(
                            model = consumptionReportController.model,
                            controller = consumptionReportController,
                        )
                    }

                    composable(
                        route = "smart_collateral",
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
                        },
                    ) {
                        SmartCollateralScreen(
                            controller = smartCollateralcontroller,
                            onBackPressed = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = "meetspace",
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
                        },
                    ) {
                        MeetSpaceScreen(
                            controller = meetSpaceController,
                            onBackPressed = { navController.popBackStack() },
                        )
                    }

                    // Attendance full-page screen
                    composable(
                        route = "attendance",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        LaunchedEffect(backStackEntry) {
                            backStackEntry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                controller.attendanceController.fetchLeaveBalances()
                            }
                        }
                        AttendanceScreen(
                            controller = controller.attendanceController,
                            onBack = { navController.popBackStack() },
                            onLeaveCardClick = { leaveType, date ->
                                navController.navigate("apply_leave?leaveType=${java.net.URLEncoder.encode(leaveType, "UTF-8")}&date=${date}")
                            },
                            onRegularizeClick = { date ->
                                navController.navigate("regularize?date=${date}")
                            },
                            onOutdoorDutyClick = { date ->
                                navController.navigate("apply_outdoor_duty?date=${date}")
                            },
                            onWfhClick = { date ->
                                navController.navigate("apply_wfh?date=${date}")
                            },
                            onHistoryClick = {
                                navController.navigate("my_requests")
                            },
                        )
                    }

                    // My Requests list screen
                    composable(
                        route = "my_requests",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        val scope = rememberCoroutineScope()
                        var isNavigatingBack by remember { mutableStateOf(false) }
                        LaunchedEffect(backStackEntry) {
                            backStackEntry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                myRequestsController.fetchRequests()
                            }
                        }
                        com.archeGlobal.one.ui.screens.MyRequestsScreen(
                            controller = myRequestsController,
                            onBack = {
                                if (!isNavigatingBack) {
                                    isNavigatingBack = true
                                    scope.launch {
                                        delay(2000)
                                        navController.popBackStack()
                                    }
                                }
                            },
                            onRequestClick = { eventId ->
                                val item = myRequestsController.requests.find { it.eventId == eventId }
                                if (item != null) {
                                    com.archeGlobal.one.controller.MyRequestsController.selectedRequest = item
                                    navController.navigate("user_approval_history")
                                }
                            },
                        )
                        UniversalLoader(isLoading = isNavigatingBack)
                    }

                    // User Approval History detail screen
                    composable(
                        route = "user_approval_history",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) {
                        val item = com.archeGlobal.one.controller.MyRequestsController.selectedRequest
                        if (item != null) {
                            com.archeGlobal.one.ui.screens.UserApprovalHistoryScreen(
                                item = item,
                                onBack = { navController.popBackStack() },
                                onCancelRequest = { onDone ->
                                    myRequestsController.cancelRequest(
                                        eventId = item.eventId,
                                        onSuccess = {
                                            onDone()
                                            navController.popBackStack()
                                        },
                                        onError = { onDone() },
                                    )
                                },
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }

                    // Apply Leave screen
                    composable(
                        route = "apply_leave?leaveType={leaveType}&date={date}",
                        arguments = listOf(
                            androidx.navigation.navArgument("leaveType") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("date") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                        ),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        val leaveType = backStackEntry.arguments?.getString("leaveType")?.let {
                            java.net.URLDecoder.decode(it, "UTF-8")
                        } ?: ""
                        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                        val initialDate = if (dateStr.isNotEmpty()) {
                            try { java.time.LocalDate.parse(dateStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        } else java.time.LocalDate.now()
                        ApplyLeaveScreen(
                            initialLeaveType = leaveType,
                            initialDate = initialDate,
                            attendanceController = controller.attendanceController,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // Apply Regularization screen
                    composable(
                        route = "regularize?date={date}",
                        arguments = listOf(
                            androidx.navigation.navArgument("date") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                        ),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                        val initialDate = if (dateStr.isNotEmpty()) {
                            try { java.time.LocalDate.parse(dateStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        } else java.time.LocalDate.now()
                        ApplyRegularizationScreen(
                            date = initialDate,
                            attendanceController = controller.attendanceController,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // Apply WFH screen
                    composable(
                        route = "apply_wfh?date={date}",
                        arguments = listOf(
                            androidx.navigation.navArgument("date") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                        ),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                        val initialDate = if (dateStr.isNotEmpty()) {
                            try { java.time.LocalDate.parse(dateStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        } else java.time.LocalDate.now()
                        ApplyWfhScreen(
                            initialDate = initialDate,
                            attendanceController = controller.attendanceController,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // Apply Outdoor Duty screen
                    composable(
                        route = "apply_outdoor_duty?date={date}",
                        arguments = listOf(
                            androidx.navigation.navArgument("date") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                        ),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                        val initialDate = if (dateStr.isNotEmpty()) {
                            try { java.time.LocalDate.parse(dateStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        } else java.time.LocalDate.now()
                        ApplyOutdoorDutyScreen(
                            initialDate = initialDate,
                            attendanceController = controller.attendanceController,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // Approval Requests screen
                    composable(
                        route = "approval_requests",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) {
                        ApprovalRequestsScreen(
                            controller = approvalRequestsController,
                            onBack = { navController.popBackStack() },
                            onRequestClick = { request ->
                                val dateText = if (request.startDate == request.endDate || request.endDate.isNullOrEmpty()) {
                                    request.startDate ?: ""
                                } else {
                                    "${request.startDate} - ${request.endDate}"
                                }
                                navController.navigate(
                                    "approval_request_detail" +
                                        "?id=${java.net.URLEncoder.encode(request.eventId ?: "", "UTF-8")}" +
                                        "&name=${java.net.URLEncoder.encode(request.employeeName ?: "", "UTF-8")}" +
                                        "&leaveType=${java.net.URLEncoder.encode(request.requestType ?: "", "UTF-8")}" +
                                        "&code=${java.net.URLEncoder.encode(request.employeeCode ?: "", "UTF-8")}" +
                                        "&date=${java.net.URLEncoder.encode(dateText, "UTF-8")}" +
                                        "&duration=${java.net.URLEncoder.encode(request.leaveDuration ?: "Full", "UTF-8")}" +
                                        "&reason=${java.net.URLEncoder.encode(request.reason ?: "", "UTF-8")}" +
                                        "&description=${java.net.URLEncoder.encode(request.description ?: "", "UTF-8")}" +
                                        "&status=${java.net.URLEncoder.encode(request.status ?: "pending", "UTF-8")}" +
                                        "&punchIn=${java.net.URLEncoder.encode(request.punchIn ?: "", "UTF-8")}" +
                                        "&punchOut=${java.net.URLEncoder.encode(request.punchOut ?: "", "UTF-8")}",
                                )
                            },
                        )
                    }

                    // Approval Request Detail screen
                    composable(
                        route = "approval_request_detail?id={id}&name={name}&leaveType={leaveType}&code={code}&date={date}&duration={duration}&reason={reason}&description={description}&status={status}&punchIn={punchIn}&punchOut={punchOut}",
                        arguments = listOf(
                            androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("name") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("leaveType") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("code") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("date") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("duration") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("reason") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("description") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("status") { type = androidx.navigation.NavType.StringType; defaultValue = "Pending" },
                            androidx.navigation.navArgument("punchIn") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("punchOut") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                        ),
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                    ) { backStackEntry ->
                        val args = backStackEntry.arguments
                        com.archeGlobal.one.ui.screens.ApprovalRequestDetailScreen(
                            eventId = java.net.URLDecoder.decode(args?.getString("id") ?: "", "UTF-8"),
                            employeeName = java.net.URLDecoder.decode(args?.getString("name") ?: "", "UTF-8"),
                            leaveType = java.net.URLDecoder.decode(args?.getString("leaveType") ?: "", "UTF-8"),
                            employeeCode = java.net.URLDecoder.decode(args?.getString("code") ?: "", "UTF-8"),
                            date = java.net.URLDecoder.decode(args?.getString("date") ?: "", "UTF-8"),
                            duration = java.net.URLDecoder.decode(args?.getString("duration") ?: "", "UTF-8"),
                            reason = java.net.URLDecoder.decode(args?.getString("reason") ?: "", "UTF-8"),
                            description = java.net.URLDecoder.decode(args?.getString("description") ?: "", "UTF-8"),
                            status = java.net.URLDecoder.decode(args?.getString("status") ?: "Pending", "UTF-8"),
                            punchIn = java.net.URLDecoder.decode(args?.getString("punchIn") ?: "", "UTF-8"),
                            punchOut = java.net.URLDecoder.decode(args?.getString("punchOut") ?: "", "UTF-8"),
                            onBack = { navController.popBackStack() },
                            onApprove = { approvalRequestsController.fetchManagerApprovals() },
                            onReject = { approvalRequestsController.fetchManagerApprovals() }
                        )
                    }

                }

                // Update Required Dialog
                if (showUpdateDialog) {
                    UpdateRequiredDialog(
                        onUpdateClick = {
                            preferencesManager.setBoolean("showUpdateDialog", true)

                            // Open Play Store
                            val intent =
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            try {
                                startActivity(intent)
                                Log.d("HomeActivity", "Play Store intent launched successfully for package: $packageName")
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Failed to launch Play Store intent: ${e.message}")
                                // Fallback to web-based Play Store URL
                                val webIntent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                try {
                                    startActivity(webIntent)
                                    Log.d("HomeActivity", "Web Play Store intent launched successfully for package: $packageName")
                                } catch (e: Exception) {
                                    Log.e("HomeActivity", "Failed to open web Play Store: ${e.message}")
                                    Toast
                                        .makeText(
                                            this@HomeActivity,
                                            "Unable to open Play Store. Please try again.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        },
                        onDismiss = {
                            showUpdateDialog = false
                            preferencesManager.setBoolean("showUpdateDialog", false)
                        },
                    )
                }

                val showWhatsNewDialog by controller.showWhatsNewDialog.collectAsState()

                val whatsNewData = UserDataManager.getInstance(this).getWhatsNewData()

                if (showWhatsNewDialog && !whatsNewData.isNullOrEmpty()) {
                    WhatsNewDialog(
                        whatsNewItems = whatsNewData,
                        appVersion = "1.5",
                        onDismiss = {
                            controller.dismissWhatsNewDialog()
                        },
                    )
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
        // policyController is now lazy-loaded, so we can call onCleared directly if needed
        // policyController.onCleared()
    }

    override fun onResume() {
        super.onResume()
        val biometricHelper = BiometricHelper(this)
        val isLoggedIn = userDataManager.isLoggedIn()
        val isLocked = userDataManager.preferencesManager.getAppLockState()
        val appLifecycleObserver = XOneApplication.getInstance().getAppLifecycleObserver()

        showUpdateDialog = false
        preferencesManager.setBoolean("showUpdateDialog", false)
        preferencesManager.setString("preUpdateVersion", "")

        if (isLoggedIn) {
            refreshHomeData()
        }

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

                    val intent =
                        Intent(this@HomeActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("session_expired", true)
                        }
                    startActivity(intent)
                    finish()
                },
            )
        }
    }

    /**
     * Public method to navigate directly to order history
     * Used by AndroidNavigator for cross-activity navigation
     */
    fun navigateToOrderHistory() {
        navigator.navController?.navigate("order_history") {
            launchSingleTop = true
        }
    }

    fun showUpdateDialog() {
        showUpdateDialog = true
        preferencesManager.setBoolean("showUpdateDialog", true)
    }

    @Composable
    fun UpdateRequiredDialog(
        onUpdateClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF6F4EE),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Download icon
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "Update Required",
                        tint = Color(0xFFDD3825),
                        modifier = Modifier.size(48.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    androidx.compose.material3.Text(
                        text = "Update Required",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    androidx.compose.material3.Text(
                        text = "A new version of ArcheOne is available. You must update to continue using the app.",
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Update button
                    Button(
                        onClick = onUpdateClick,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        androidx.compose.material3.Text(
                            text = "Update Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                        )
                    }
                }
            }
        }
    }
}
