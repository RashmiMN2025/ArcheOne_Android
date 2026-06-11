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

    private var lastPauseTime: Long = 0
    private val BACKGROUND_THRESHOLD = 1000 * 30 // 30 seconds
    private var isFromLogin = false // Flag to track if we're coming from login
    private var isAuthenticating = mutableStateOf(false) // New state for biometric authentication
    private var isLocked = false
    private var biometricPromptShown = false
    private var currentSourceActivity = mutableStateOf<String?>(null) // Track source activity for back navigation
    private var showUpdateDialog by mutableStateOf(false)

    private fun refreshHomeData() {
        Log.d("HomeActivity", "Refreshing home screen data without API calls")
        controller.refreshUserData()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val navigateTo = intent.getStringExtra("navigateTo")
        val ticketCategory = intent.getStringExtra("ticketCategory")
        val source = intent.getStringExtra("source")
        val clearBackStack = intent.getBooleanExtra("clearBackStack", false)
        val directNavigateTo = intent.getStringExtra("direct_navigate_to")
        val sourceActivity = intent.getStringExtra("source_activity")

        Log.d("HomeActivity", "onNewIntent called with navigateTo=$navigateTo")

        if (directNavigateTo == "order_history") {
            currentSourceActivity.value = sourceActivity
            navigator.navController?.navigate("order_history") {
                launchSingleTop = true
            }
            return
        }

        if (clearBackStack && navigateTo == "home") {
            navigator.navController?.navigate("home") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            return
        }

        if (navigateTo == "track_tickets" && ticketCategory != null) {
            if (::helpDeskController.isInitialized) {
                if (source != null) {
                    helpDeskController.setNavigationSource(source)
                }
                helpDeskController.loadTicketsData(ticketCategory)
                navigator.navController?.navigate("track_tickets")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Proactive Play Store Update Check - Every time the user returns to the app
        com.archeGlobal.one.utils.AppUpdateUtils.checkForUpdates(this) {
            showUpdateDialog()
        }

        val biometricHelper = BiometricHelper(this)
        val isLoggedIn = userDataManager.isLoggedIn()
        val isLocked = userDataManager.preferencesManager.getAppLockState()
        val appLifecycleObserver = XOneApplication.getInstance().getAppLifecycleObserver()

        // Removed: showUpdateDialog = false (This was accidentally clearing the update requirement)
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
                    userDataManager.preferencesManager.setAppLockState(false)
                },
                onError = { error ->
                    isAuthenticating.value = false
                    Log.w("HomeActivity", "Biometric authentication failed: $error")
                    val intent = Intent(this@HomeActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("session_expired", true)
                    }
                    startActivity(intent)
                    finish()
                },
            )
        }
    }

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("HomeActivity", "onCreate called")
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Initialize core dependencies FIRST
        userDataManager = UserDataManager.getInstance(this)
        navigator = AndroidNavigator(this)
        controller = HomeController(navigator, this)
        helpDeskController = HelpDeskController(this)
        preferencesManager = PreferencesManager(this)

        // NOW check for updates from Play Store (uses preferencesManager inside showUpdateDialog)
        com.archeGlobal.one.utils.AppUpdateUtils.checkForUpdates(this) {
            showUpdateDialog()
        }

        val fromLogin = intent.getBooleanExtra("fromLogin", false)
        isFromLogin = fromLogin
        val fromMpin = intent.getBooleanExtra("fromMpin", false)
        val email = intent.getStringExtra("email") ?: userDataManager.getUserData()?.email ?: ""
        val mobile = intent.getStringExtra("mobile") ?: userDataManager.getUserData()?.mobile ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: userDataManager.getUserData()?.employeeId ?: ""
        val token = intent.getStringExtra("token") ?: userDataManager.getAuthToken() ?: ""

        if (fromLogin || fromMpin) {
            userDataManager.preferencesManager.setAppLockState(false)
            XOneApplication.getInstance().getAppLifecycleObserver().setAuthenticationSessionActive(true)
        } else {
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
                            onError = { _ -> },
                        )
                    }.setNegativeButton("No", null)
                    .setCancelable(false)
                    .show()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = navigator.navController ?: return
                if (navController.currentDestination?.route == "greetings") {
                    navigator.navigateToHome()
                } else {
                    Toast.makeText(this@HomeActivity, "Press Home to exit the app", Toast.LENGTH_SHORT).show()
                }
            }
        })

        otpVerificationController = OtpVerificationController(navigator = navigator, context = this)

        val destination = intent.getStringExtra("destination")
        val navigateTo = intent.getStringExtra("navigateTo")
        val ticketCategory = intent.getStringExtra("ticketCategory")
        val source = intent.getStringExtra("source")
        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)
        val fromOtp = intent.getBooleanExtra("FROM_OTP", false)

        setContent {
            XOneTheme {
                val navController = rememberNavController()
                navigator.setNavController(navController)
                helpDeskController.setNavigationCallback { route -> navController.navigate(route) }
                meetSpaceController = MeetSpaceController(this)

                if (intent.getBooleanExtra("showUpdateDialog", false) || preferencesManager.getBoolean("showUpdateDialog", false)) {
                    showUpdateDialog = true
                }

                var isLoading by remember { mutableStateOf(false) }
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

                if (currentNavigateTo == "order_history_detail" && orderData != null) {
                    try {
                        val order = Gson().fromJson(orderData, DeskCartOrderHistory::class.java)
                        OrderHistoryController.selectedOrderForDetails = order
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Failed to parse order data")
                    }
                }

                val reactiveSourceActivity by currentSourceActivity
                val effectiveSourceActivity = reactiveSourceActivity ?: sourceActivity

                LaunchedEffect(sourceActivity) {
                    if (currentSourceActivity.value == null && sourceActivity != null) {
                        currentSourceActivity.value = sourceActivity
                    }
                }

                val startDestination = when {
                    directNavigateTo == "order_history" -> "order_history"
                    currentAction == "navigate_to_order_history" -> "order_history"
                    clearBackStack && currentNavigateTo == "home" -> "home"
                    currentNavigateTo == "track_tickets" && currentTicketCategory != null -> "track_tickets"
                    currentNavigateTo == "order_received" -> "order_received"
                    currentNavigateTo == "order_history" -> "order_history"
                    currentNavigateTo == "consumption_report" && !clearBackStack -> "consumption_report"
                    else -> "home"
                }

                LaunchedEffect(currentDestination, currentNavigateTo, currentTicketCategory, currentSource, isEmergencyContact, clearBackStack, directNavigateTo) {
                    if (startDestination == "track_tickets" && currentTicketCategory != null) {
                        if (currentSource != null) helpDeskController.setNavigationSource(currentSource)
                        helpDeskController.loadTicketsData(currentTicketCategory)
                    }

                    if (!fromOtp) {
                        val token = userDataManager.getAuthToken() ?: ""
                        isLoading = true
                        otpVerificationController.loginWithToken(token, email, mobile, employeeId, true, false, true) { message, isError ->
                            isLoading = false
                            if (isError) {
                                if (message.contains("Invalid Token")) {
                                    Toast.makeText(this@HomeActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                                    com.archeGlobal.one.utils.setFirstTimeLogin(this@HomeActivity, false)
                                    userDataManager.clearUserData()
                                    navigator.navigateToLoginScreen()
                                }
                            } else {
                                controller.refreshUserData()
                                controller.onLoginCompleted()
                            }

                            if (startDestination == "home") {
                                currentDestination?.let { dest -> navController.navigate(dest) }
                                currentNavigateTo?.let { route ->
                                    if (route == "track_tickets" && currentTicketCategory != null) {
                                        if (currentSource != null) helpDeskController.setNavigationSource(currentSource)
                                        helpDeskController.navigateToTrackTickets(currentTicketCategory)
                                    } else if (route == "order_history") {
                                        navController.navigate("order_history")
                                    } else if (route == "order_history_detail") {
                                        val orderId = currentIntent.getStringExtra("orderId") ?: ""
                                        navController.navigate("order_history_detail/$orderId")
                                    } else {
                                        navController.navigate(route)
                                    }
                                }
                            }
                        }
                    } else {
                        if (startDestination == "home") {
                            currentDestination?.let { dest -> navController.navigate(dest) }
                            currentNavigateTo?.let { route ->
                                if (route == "track_tickets" && currentTicketCategory != null) {
                                    helpDeskController.navigateToTrackTickets(currentTicketCategory)
                                } else if (route == "order_history_detail") {
                                    val orderId = currentIntent.getStringExtra("orderId") ?: ""
                                    navController.navigate("order_history_detail/$orderId")
                                } else {
                                    navController.navigate(route)
                                }
                            }
                        }
                    }
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(route = "home", enterTransition = { fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(300)) }) {
                        val eventData = controller.eventData.collectAsState().value
                        val showEventPopup = controller.showEventPopup.collectAsState().value
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
                            eventData = eventData,
                            showEventPopup = showEventPopup,
                            onDismissEventPopup = controller::dismissEventPopup,
                            navigator = navigator,
                        )
                    }

                    composable(route = "chat") {
                        LaunchedEffect(Unit) { chatController.onChatScreenEnter() }
                        ChatScreen(viewModel = chatController.viewModel, navController = navController, onBackPressed = chatController::onBackPressed, showBottomBar = true)
                    }

                    composable(route = "headsup") {
                        HeadsUpScreen(
                            footerNavigation = controller.model.footerNavigation,
                            isUsingPrideIcon = controller.isUsingPrideIcon(),
                            onFooterHomeClick = { navController.navigate("home") },
                            onFooterChatClick = { navController.navigate("chat") },
                            onFooterHeadsUpClick = { },
                            onFooterSOSClick = { navController.navigate("sos") },
                            onFooterProfileClick = { navController.navigate("profile") },
                            onHistoryClick = { tab -> navController.navigate("post_history?initialTab=$tab") },
                        )
                    }

                    composable(route = "post_history?initialTab={initialTab}", arguments = listOf(navArgument("initialTab") { type = NavType.IntType; defaultValue = 0 })) { backStackEntry ->
                        val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
                        PostHistoryScreen(onBackPressed = { navController.popBackStack() }, initialTab = initialTab)
                    }

                    composable(route = "raise_concern/{title}?category={category}&subcategory={subcategory}&faqId={faqId}", arguments = listOf(navArgument("title") { type = NavType.StringType }, navArgument("category") { type = NavType.StringType; nullable = true }, navArgument("subcategory") { type = NavType.StringType; nullable = true }, navArgument("faqId") { type = NavType.StringType; nullable = true })) { backStackEntry ->
                        val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "Raise a Concern", "UTF-8")
                        val category = backStackEntry.arguments?.getString("category")?.let { URLDecoder.decode(it, "UTF-8") }
                        val subcategory = backStackEntry.arguments?.getString("subcategory")?.let { URLDecoder.decode(it, "UTF-8") }
                        val faqId = backStackEntry.arguments?.getString("faqId")?.let { URLDecoder.decode(it, "UTF-8") }
                        RaiseConcernScreen(onBackPressed = { navController.popBackStack() }, title = title, prefilledCategory = category, prefilledSubcategory = subcategory, prefilledFaqId = faqId, helpDeskController = helpDeskController, onNavigateToTrackTickets = { cat -> helpDeskController.navigateToTrackTickets(cat) })
                    }

                    composable(route = "locations?showHeader={showHeader}", arguments = listOf(navArgument("showHeader") { type = NavType.BoolType; defaultValue = true })) {
                        val showHeaderLocal = intent.getBooleanExtra("showHeader", false)
                        val isEmergencyContactLocal = intent.getBooleanExtra("isEmergencyContact", false)
                        if (isEmergencyContactLocal) intent.removeExtra("isEmergencyContact")
                        LocationsScreen(navController = navController, controller = locationsController, isEmergencyContact = isEmergencyContactLocal, showHeader = showHeaderLocal, onBackToHome = { navigator.navigateToHome() })
                    }

                    composable(route = "business_card") { BusinessCardScreen(businessCard = businessCardController.businessCard, controller = businessCardController, onBackPressed = { navController.popBackStack() }) }
                    composable(route = "idea_vault") { IdeaVaultScreen(onBackPressed = { navController.popBackStack() }, controller = ideaVaultController, apiService = RetrofitClient.apiService) }
                    composable(route = "policy") { PolicyScreen(model = policyController.model.value, onPolicyClick = policyController::onPolicyClick, onBackClick = policyController::onBackClick, isLoading = policyController.isLoading.value) }
                    composable(route = "communique") { CommuniqueScreen(model = communiqueController.model, onCommuniqueClick = communiqueController::onCommuniqueClick, onBackPressed = { navController.popBackStack() }, isLoading = communiqueController.isLoading.value) }
                    composable(route = "asset") { AssetScreen(model = assetController.model, controller = assetController, onBackPressed = { navController.popBackStack() }) }
                    composable(route = "holiday_options") { HolidayOptionsScreen(controller = holidayOptionsController) }
                    composable(route = "calendar") { HolidayCalendarScreen(controller = holidayCalendarController, onBackPressed = { navController.navigate("home") }, onMonthClick = { month -> navController.navigate("monthDetail/$month") }, onHolidayListClick = { pdfUrl -> navigator.navigateToPDFViewer(pdfUrl, "Holiday List") }) }
                    composable("monthDetail/{month}") { backStackEntry -> val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 1; MonthDetailScreen(month = month, controller = holidayCalendarController, onBackPressed = { navController.popBackStack() }) }

                    composable(route = "profile") {
                        ProfileScreen(controller = profileController, footerNavigation = FooterNavigationModel(showHome = false, showChat = false, showSOS = false, showProfile = true), onFooterHomeClick = { navController.navigate("home") }, onFooterChatClick = { navigator.navigateToChat() }, onFooterHeadsUpClick = { navController.navigate("headsup") }, onFooterSOSClick = { navController.navigate("sos") }, onFooterProfileClick = { })
                    }

                    composable(route = "aboutme") { AboutMeScreen(controller = aboutMeController) }
                    composable(route = "addressdetails") { AddressDetailsScreen(controller = addressController) }
                    composable("core_values") { CoreValuesScreen(onBackPressed = { navController.popBackStack() }) }
                    composable(route = "emergencycontact") { EmergencyContactScreen(controller = emergencyContactController) }

                    composable(route = "pdf_viewer/{pdfUrl}?title={title}", arguments = listOf(navArgument("pdfUrl") { type = NavType.StringType }, navArgument("title") { type = NavType.StringType; defaultValue = "PDF Viewer" })) { backStackEntry ->
                        val pdfUrl = URLDecoder.decode(backStackEntry.arguments?.getString("pdfUrl") ?: "", "UTF-8")
                        val title = backStackEntry.arguments?.getString("title") ?: "PDF Viewer"
                        PDFViewerScreen(pdfUrl = pdfUrl, title = title, onBackClick = { navController.popBackStack() })
                    }

                    composable(route = "sos?showHeader={showHeader}", arguments = listOf(navArgument("showHeader") { type = NavType.BoolType; defaultValue = true })) { backStackEntry ->
                        val showHeaderLocal = backStackEntry.arguments?.getBoolean("showHeader") ?: true
                        var showRaiseConcernLocal by remember { mutableStateOf(false) }
                        if (showRaiseConcernLocal) {
                            RaiseConcernScreen(onBackPressed = { showRaiseConcernLocal = false }, onNavigateToTrackTickets = { cat -> helpDeskController.navigateToTrackTickets(cat) })
                        } else {
                            SOSScreen(
                                controller = sosController,
                                onNavigateToRaiseConcern = { showRaiseConcernLocal = true },
                                onBackPressed = if (showHeaderLocal) { {} } else { { navController.popBackStack() } },
                                onSOSBlogClick = { blog -> val blogJson = Uri.encode(Gson().toJson(blog)); navController.navigate("sosDetail/$blogJson") },
                                onNavigateToEmergencyContact = {
                                    intent.putExtra("isEmergencyContact", true)
                                    intent.putExtra("showHeader", showHeaderLocal)
                                    navController.navigate("locations") { popUpTo("home") { saveState = true }; launchSingleTop = true }
                                },
                                onFooterHomeClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                                onFooterChatClick = { navigator.navigateToChat() },
                                onFooterHeadsUpClick = { navController.navigate("headsup") },
                                onFooterSOSClick = { },
                                onFooterProfileClick = { navController.navigate("profile") },
                                showHeader = showHeaderLocal
                            )
                        }
                    }

                    composable("sosDetail/{blog}", arguments = listOf(navArgument("blog") { type = NavType.StringType })) { backStackEntry ->
                        val blog = Gson().fromJson(backStackEntry.arguments?.getString("blog"), SosBlogModel::class.java)
                        SOSDetailScreen(blog = blog, onBackPressed = { navController.popBackStack() })
                    }

                    composable(route = "vision") { VisionScreen(onBackPressed = { navigator.navigateToHome() }) }
                    composable(route = "greetings") { GreetingsScreen(controller = greetingsController, onBackPressed = { navigator.navigateToHome() }) }
                    composable(route = "global_celebration") { GlobalCelebrationScreen(controller = GlobalCelebrationController(this@HomeActivity, navigator, greetingsController), onBackPressed = { navigator.navigateToGreetings() }) }
                    composable(route = "service_not_available?serviceName={serviceName}", arguments = listOf(navArgument("serviceName") { type = NavType.StringType; nullable = true })) { backStackEntry -> ServiceNotAvailableScreen(navController = navController, serviceName = backStackEntry.arguments?.getString("serviceName")) }

                    composable(route = "travel") { TravelScreen(controller = travelController) }
                    composable(route = "travel_history") { TravelHistoryScreen(controller = travelController) }
                    composable(route = "travel_request_detail") { travelController.selectedTravelRequest?.let { tr -> TravelRequestDetailScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }
                    composable(route = "travel_history_detail") { travelController.selectedTravelRequest?.let { tr -> TravelHistoryDetailScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }
                    composable(route = "travel_approvals") { TravelApprovalsScreen(controller = travelController) }
                    composable(route = "travel_admin_dashboard") { TravelAdminDashboardScreen(controller = travelController) }
                    composable(route = "travel_approval_detail") { travelController.selectedTravelRequest?.let { tr -> TravelApprovalDetailScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }
                    composable(route = "travel_approval_confirm") { travelController.selectedTravelRequest?.let { tr -> TravelApprovalConfirmScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }
                    composable(route = "travel_approve") { travelController.selectedTravelRequest?.let { tr -> TravelApproveScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }
                    composable(route = "travel_reject") { travelController.selectedTravelRequest?.let { tr -> TravelRejectScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }
                    composable(route = "travel_approval_details") { travelController.selectedTravelRequest?.let { tr -> TravelApprovalDetailsScreen(controller = travelController, travelRequest = tr) } ?: LaunchedEffect(Unit) { navController.popBackStack() } }

                    composable(route = "helpdesk") { HelpDeskScreen(controller = helpDeskController) }
                    composable(route = "track_tickets") { TicketTrackingScreen(controller = helpDeskController) }
                    composable(route = "faq_detail/{faqId}") { backStackEntry -> FAQDetailScreen(faqId = backStackEntry.arguments?.getString("faqId") ?: "", controller = helpDeskController) }

                    composable(route = "order_details/{orderId}") { OrderReceivedController.selectedOrderForDetails?.let { item -> OrderDetailsScreen(controller = OrderHistoryDetailsController(this@HomeActivity, navigator), orderItem = item) } ?: navigator.navigateToOrderReceived() }
                    composable(route = "order_history_detail/{orderId}") { OrderHistoryController.selectedOrderForDetails?.let { item -> OrderHistoryDetailScreen(controller = OrderHistoryController(this@HomeActivity, navigator, effectiveSourceActivity), orderItem = item) } ?: navigator.navigateToOrderHistory() }
                    composable(route = "order_received") { OrderReceivedScreen(model = OrderReceivedController(this@HomeActivity, navigator).model, controller = OrderReceivedController(this@HomeActivity, navigator)) }
                    composable(route = "order_history") { OrderHistoryScreen(model = OrderHistoryController(this@HomeActivity, navigator, effectiveSourceActivity).model, controller = OrderHistoryController(this@HomeActivity, navigator, effectiveSourceActivity)) }
                    composable(route = "consumption_report") { ConsumptionReportScreen(model = consumptionReportController.model, controller = consumptionReportController) }
                    composable(route = "smart_collateral") { SmartCollateralScreen(controller = smartCollateralcontroller, onBackPressed = { navController.popBackStack() }) }
                    composable(route = "meetspace") { MeetSpaceScreen(controller = meetSpaceController, onBackPressed = { navController.popBackStack() }) }

                    composable(route = "attendance") {
                        AttendanceScreen(
                            controller = controller.attendanceController,
                            onBack = { navController.popBackStack() },
                            onLeaveCardClick = { type, date -> navController.navigate("apply_leave?leaveType=${java.net.URLEncoder.encode(type, "UTF-8")}&date=$date") },
                            onRegularizeClick = { date -> lifecycleScope.launch { if (controller.attendanceController.validateRegularization(date).first) navController.navigate("regularize?date=$date") } },
                            onOutdoorDutyClick = { date -> navController.navigate("apply_outdoor_duty?date=$date") },
                            onWfhClick = { date -> navController.navigate("apply_wfh?date=$date") },
                            onHistoryClick = { myRequestsController.fetchRequests(); navController.navigate("my_requests") }
                        )
                    }

                    composable(route = "my_requests") {
                        LaunchedEffect(Unit) { myRequestsController.fetchRequests() }
                        MyRequestsScreen(controller = myRequestsController, onBack = { navController.popBackStack() }, onRequestClick = { id -> val item = myRequestsController.requests.find { it.eventId == id }; if (item != null) { MyRequestsController.selectedRequest = item; navController.navigate("user_approval_history") } })
                    }

                    composable(route = "user_approval_history") {
                        val item = MyRequestsController.selectedRequest
                        if (item != null) {
                            UserApprovalHistoryScreen(item = item, onBack = { navController.popBackStack() }, onCancelRequest = { done -> myRequestsController.cancelRequest(item.eventId, item.category, { done(); myRequestsController.fetchRequests(); controller.attendanceController.fetchLeaveBalances(); navController.popBackStack() }, { done() }) }, onRequestDeletion = { r, done -> myRequestsController.requestDeletion(item.eventId, r, { done(); myRequestsController.fetchRequests(); navController.popBackStack() }, { msg -> Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_LONG).show(); done() }) })
                        } else navController.popBackStack()
                    }

                    composable(route = "apply_leave?leaveType={leaveType}&date={date}", arguments = listOf(navArgument("leaveType") { defaultValue = "" }, navArgument("date") { defaultValue = "" })) { backStackEntry ->
                        val lType = URLDecoder.decode(backStackEntry.arguments?.getString("leaveType") ?: "", "UTF-8")
                        val dStr = backStackEntry.arguments?.getString("date") ?: ""
                        val iDate = try { java.time.LocalDate.parse(dStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        ApplyLeaveScreen(initialLeaveType = lType, initialDate = iDate, attendanceController = controller.attendanceController, onBack = { navController.popBackStack() })
                    }

                    composable(route = "regularize?date={date}", arguments = listOf(navArgument("date") { defaultValue = "" })) { backStackEntry ->
                        val dStr = backStackEntry.arguments?.getString("date") ?: ""
                        val iDate = try { java.time.LocalDate.parse(dStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        ApplyRegularizationScreen(date = iDate, attendanceController = controller.attendanceController, onBack = { navController.popBackStack() })
                    }

                    composable(route = "apply_wfh?date={date}", arguments = listOf(navArgument("date") { defaultValue = "" })) { backStackEntry ->
                        val dStr = backStackEntry.arguments?.getString("date") ?: ""
                        val iDate = try { java.time.LocalDate.parse(dStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        ApplyWfhScreen(initialDate = iDate, attendanceController = controller.attendanceController, onBack = { navController.popBackStack() })
                    }

                    composable(route = "apply_outdoor_duty?date={date}", arguments = listOf(navArgument("date") { defaultValue = "" })) { backStackEntry ->
                        val dStr = backStackEntry.arguments?.getString("date") ?: ""
                        val iDate = try { java.time.LocalDate.parse(dStr) } catch (e: Exception) { java.time.LocalDate.now() }
                        ApplyOutdoorDutyScreen(initialDate = iDate, attendanceController = controller.attendanceController, onBack = { navController.popBackStack() })
                    }

                    composable(route = "approval_requests") { ApprovalRequestsScreen(controller = approvalRequestsController, onBack = { navController.popBackStack() }, onRequestClick = { r -> val dText = if (r.startDate == r.endDate || r.endDate.isNullOrEmpty()) r.startDate ?: "" else "${r.startDate} - ${r.endDate}"; navController.navigate("approval_request_detail?id=${java.net.URLEncoder.encode(r.eventId ?: "", "UTF-8")}&name=${java.net.URLEncoder.encode(r.employeeName ?: "", "UTF-8")}&leaveType=${java.net.URLEncoder.encode(r.requestType ?: "", "UTF-8")}&code=${java.net.URLEncoder.encode(r.employeeCode ?: "", "UTF-8")}&date=${java.net.URLEncoder.encode(dText, "UTF-8")}&duration=${java.net.URLEncoder.encode(r.leaveDuration ?: "", "UTF-8")}&reason=${java.net.URLEncoder.encode(r.reason ?: "", "UTF-8")}&description=${java.net.URLEncoder.encode(r.description ?: "", "UTF-8")}&status=${java.net.URLEncoder.encode(r.status ?: "pending", "UTF-8")}&punchIn=${java.net.URLEncoder.encode(r.punchIn ?: "", "UTF-8")}&punchOut=${java.net.URLEncoder.encode(r.punchOut ?: "", "UTF-8")}&category=${java.net.URLEncoder.encode(r.category ?: "approval request", "UTF-8")}&secondApprover=${java.net.URLEncoder.encode(r.secondApprover ?: "", "UTF-8")}&secondApproverEmail=${java.net.URLEncoder.encode(r.secondApproverEmail ?: "", "UTF-8")}") }) }

                    composable(route = "approval_request_detail?id={id}&name={name}&leaveType={leaveType}&code={code}&date={date}&duration={duration}&reason={reason}&description={description}&status={status}&punchIn={punchIn}&punchOut={punchOut}&category={category}&secondApprover={secondApprover}&secondApproverEmail={secondApproverEmail}") { backStackEntry ->
                        val a = backStackEntry.arguments
                        ApprovalRequestDetailScreen(
                            eventId = URLDecoder.decode(a?.getString("id") ?: "", "UTF-8"),
                            employeeName = URLDecoder.decode(a?.getString("name") ?: "", "UTF-8"),
                            leaveType = URLDecoder.decode(a?.getString("leaveType") ?: "", "UTF-8"),
                            employeeCode = URLDecoder.decode(a?.getString("code") ?: "", "UTF-8"),
                            date = URLDecoder.decode(a?.getString("date") ?: "", "UTF-8"),
                            duration = URLDecoder.decode(a?.getString("duration") ?: "", "UTF-8"),
                            reason = URLDecoder.decode(a?.getString("reason") ?: "", "UTF-8"),
                            description = URLDecoder.decode(a?.getString("description") ?: "", "UTF-8"),
                            status = URLDecoder.decode(a?.getString("status") ?: "Pending", "UTF-8"),
                            punchIn = URLDecoder.decode(a?.getString("punchIn") ?: "", "UTF-8"),
                            punchOut = URLDecoder.decode(a?.getString("punchOut") ?: "", "UTF-8"),
                            category = URLDecoder.decode(a?.getString("category") ?: "approval request", "UTF-8"),
                            secondApprover = URLDecoder.decode(a?.getString("secondApprover") ?: "", "UTF-8").ifEmpty { null },
                            secondApproverEmail = URLDecoder.decode(a?.getString("secondApproverEmail") ?: "", "UTF-8").ifEmpty { null },
                            onBack = { navController.popBackStack() }, onApprove = { approvalRequestsController.fetchManagerApprovals() }, onReject = { approvalRequestsController.fetchManagerApprovals() }
                        )
                    }
                }

                if (showUpdateDialog) {
                    UpdateRequiredDialog(
                        onUpdateClick = {
                            // 1. Force Logout & Total Preference Wipe (Hard Reset)
                            preferencesManager.clearAll()
                            com.archeGlobal.one.utils.MpinManager.clearAllMpinData(this@HomeActivity)
                            
                            Log.w("HomeActivity", "MANDATORY UPDATE: Performed Hard Reset of all data and MPIN.")

                            // 2. Prepare LoginActivity as the new root (to ensure return to login)
                            val loginIntent = Intent(this@HomeActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(loginIntent)

                            // 3. Launch Play Store
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply { 
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK 
                            }
                            try { 
                                startActivity(intent) 
                                finish() // Close HomeActivity
                            } catch (e: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply { 
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK 
                                }
                                try { 
                                    startActivity(webIntent)
                                    finish()
                                } catch (e2: Exception) { 
                                    Toast.makeText(this@HomeActivity, "Unable to open Play Store", Toast.LENGTH_SHORT).show() 
                                }
                            }
                        },
                        onDismiss = { 
                            // In force update, we don't really allow dismissal, 
                            // but if they manage to, we still keep the dialog state.
                            showUpdateDialog = false 
                            preferencesManager.setBoolean("showUpdateDialog", false)
                        }
                    )
                }

                val showWhatsNewLocal by controller.showWhatsNewDialog.collectAsState()
                val wData = UserDataManager.getInstance(this).getWhatsNewData()
                if (showWhatsNewLocal && !wData.isNullOrEmpty()) {
                    WhatsNewDialog(whatsNewItems = wData, appVersion = "1.5", onDismiss = { controller.dismissWhatsNewDialog() })
                }
            }
        }
    }

    override fun onBackPressed() {
        if (intent.getStringExtra("destination") == "profile") {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun navigateToOrderHistory() {
        navigator.navController?.navigate("order_history") { launchSingleTop = true }
    }

    fun showUpdateDialog() {
        showUpdateDialog = true
        preferencesManager.setBoolean("showUpdateDialog", true)
    }

    @Composable
    fun UpdateRequiredDialog(onUpdateClick: () -> Unit, onDismiss: () -> Unit) {
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF6F4EE), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = "Update Required", tint = Color(0xFFDD3825), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Text(text = "Update Required", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = GraphikFontFamily, color = Color.Black, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Text(text = "A new version of ArcheOne is available. You must update to continue using the app.", fontSize = 16.sp, fontFamily = GraphikFontFamily, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onUpdateClick, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825), contentColor = Color.White), shape = RoundedCornerShape(8.dp)) {
                        androidx.compose.material3.Text(text = "Update Now", fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = GraphikFontFamily)
                    }
                }
            }
        }
    }
}
