package com.archeGlobal.one

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.screens.*
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.BiometricHelper
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLDecoder
import android.content.DialogInterface

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
    private lateinit var userDataManager: UserDataManager
    private lateinit var navigator: AndroidNavigator
    private var lastPauseTime: Long = 0
    private val BACKGROUND_THRESHOLD = 1000 * 30 // 30 seconds
    private var isFromLogin = false // Flag to track if we're coming from login
    private var isAuthenticating = mutableStateOf(false) // New state for biometric authentication

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
                    if (message.contains("Invalid Token")) {
                        Toast.makeText(this@HomeActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                        userDataManager.clearUserData()
                        navigator.navigateToLoginScreen()
                    } else {
                        Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
                // Update the home controller's data
                controller.refreshUserData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDataManager = UserDataManager.getInstance(this)
        navigator = AndroidNavigator(this)
        
        // Check if we're coming from login
        isFromLogin = intent.getBooleanExtra("fromLogin", false)

        val showBiometricSetup = intent.getBooleanExtra("showBiometricSetup", false)
        val email = intent.getStringExtra("email") ?: userDataManager.getUserData()?.email ?: ""
        val mobile = intent.getStringExtra("mobile") ?: userDataManager.getUserData()?.mobile ?: ""
        val employeeId = intent.getStringExtra("employeeId") ?: userDataManager.getUserData()?.employeeId ?: ""

        // Handle biometric setup if needed
        if (showBiometricSetup) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val biometricHelper = BiometricHelper(this)
                try {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Enable Fingerprint Login")
                        .setMessage("Would you like to use fingerprint for faster login next time?")
                        .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                            biometricHelper.showBiometricPrompt(
                                activity = this,
                                title = "Setup Fingerprint",
                                subtitle = "Verify your fingerprint to enable quick login",
                                onSuccess = {
                                    biometricHelper.saveCredentials(email, mobile, employeeId)
                                    Toast.makeText(this, "Fingerprint login enabled successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(this, "Failed to setup fingerprint: $error", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        .setNegativeButton("No", null)
                        .show()
                } catch (e: Exception) {
                    Log.e("BiometricSetup", "Failed to show dialog", e)
                }
            }, 1000)
        }

        // Disable back navigation to login
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Either exit the app or show a toast
                Toast.makeText(this@HomeActivity, "Press Home to exit the app", Toast.LENGTH_SHORT).show()
            }
        })
        // Initialize controllers that need context
        otpVerificationController = OtpVerificationController(
            navigator = AndroidNavigator(this),
            context = this
        )

        // Check if we need to navigate to a specific destination
        val destination = intent.getStringExtra("destination")
        val navigateTo = intent.getStringExtra("navigateTo")
        val isEmergencyContact = intent.getBooleanExtra("isEmergencyContact", false)
        val fromOtp = intent.getBooleanExtra("FROM_OTP", false)
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
                communiqueController = CommuniqueController(this, navigator)
                archeOdysseyController = ArcheOdysseyController(navigator)
                var isLoading by remember { mutableStateOf(false) }

                // If we have a destination or navigateTo, navigate to it
                LaunchedEffect(destination, navigateTo, isEmergencyContact) {
                    if (!fromOtp) {
                        val token = userDataManager.getAuthToken() ?: "your_token_here"
                        isLoading = true // Start loading
                        otpVerificationController.loginWithToken(token, email, mobile, employeeId, true) { message, isError ->
                            isLoading = false // Stop loading
                            if (isError) {
                                if (message.contains("Invalid Token")) {
                                    Toast.makeText(this@HomeActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                                    // Clear all user data
                                    userDataManager.clearUserData()
                                    navigator.navigateToLoginScreen()
                                } else {
                                    Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                //  Handle successful login, e.g., navigate to home
                                //Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    destination?.let {
                        navController.navigate(it)
                    }

                    navigateTo?.let {
                        navController.navigate(it)

                        // Log the navigation attempt for debugging
                        Log.d("HomeActivity", "Navigating to $it with isEmergencyContact=$isEmergencyContact")
                    }
                }
                // UniversalLoader(isLoading = isLoading)
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
                            onXCardClick = controller::onXCardClick,
                            isAuthenticating = isAuthenticating.value,
                            onRefresh = { refreshHomeData() }
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
                        route = "arche_odyssey",
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
                        ArcheOdysseyScreen(
                            controller = archeOdysseyController,
                            onBackPressed = archeOdysseyController::onBackPressed
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
                            onBackClick = communiqueController::onBackClick,
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
                            onBackPressed = { navController.popBackStack() }
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

    override fun onPause() {
        super.onPause()
        if (!isFromLogin) {
            lastPauseTime = System.currentTimeMillis()
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Skip biometric check if we're coming from login
        if (!isFromLogin && System.currentTimeMillis() - lastPauseTime > BACKGROUND_THRESHOLD) {
            val biometricHelper = BiometricHelper(this)
            if (biometricHelper.canUseBiometric() && biometricHelper.isBiometricEnabled()) {
                isAuthenticating.value = true // Set authenticating state to true
                biometricHelper.showBiometricPrompt(
                    activity = this,
                    title = "Verify Identity",
                    subtitle = "Use your fingerprint to continue",
                    onSuccess = {
                        // Continue with the app
                        Log.d("BiometricCheck", "Biometric verification successful")
                        isAuthenticating.value = false // Reset authenticating state
                    },
                    onError = { error ->
                        // If biometric fails, go back to login
                        Log.e("BiometricCheck", "Biometric verification failed: $error")
                        Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show()
                        userDataManager.clearUserData()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
        
        // Reset the flag after first resume
        if (isFromLogin) {
            isFromLogin = false
        }
    }
}