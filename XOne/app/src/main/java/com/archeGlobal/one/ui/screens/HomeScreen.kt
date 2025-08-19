package com.archeGlobal.one.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HomeController
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.MpinController
import com.archeGlobal.one.model.AboutMeModel
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.model.HomeModel
import com.archeGlobal.one.model.WelcomeBackgroundModel
import com.archeGlobal.one.network.FeedbackRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.components.CelebrationBanner
import com.archeGlobal.one.ui.components.CelebrationDialog
import com.archeGlobal.one.ui.components.EmptyFavorites
import com.archeGlobal.one.ui.components.EventPopup
import com.archeGlobal.one.ui.components.FooterScaffold
import com.archeGlobal.one.ui.components.PrideMonthDialog
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.*
import com.archeGlobal.one.ui.theme.getColorForApp
import com.archeGlobal.one.utils.BiometricHelper
import com.archeGlobal.one.utils.ImageCache
import com.archeGlobal.one.utils.UserDataManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileHeader(
    model: HomeModel,
    onShowProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }

    // Handle back swipe or back button press
    BackHandler(enabled = true) {
        activity?.finishAffinity() // Exit the app and go to the mobile home screen
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
    ) {
        // Background image with clip
        Image(
            painter = painterResource(id = R.drawable.header_home),
            contentDescription = "Header Background",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content without overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 27.dp, end = 16.dp, top = 70.dp, bottom = 16.dp), // Adjusted top padding from 60.dp to 50.dp and bottom from 24.dp to 16.dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.width(5.dp))
                // Profile picture
                Surface(
                    modifier = Modifier
                        .size(90.dp, 95.dp)
                        .padding(top = 8.dp)
                        .clickable(onClick = onShowProfileClick),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    // If profile picture URL is available, display it using Coil
                    if (model.profilePicture != null && model.profilePicture.isNotEmpty()) {
                        Log.d("HomeScreen", "Loading profile picture: ${model.profilePicture}")

                        // Use ImageCache version for recomposition
                        val context = LocalContext.current
                        val cacheVersion = ImageCache.profileImageVersion.value
                        key(model.profilePicture, cacheVersion) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Always show the person icon first as a placeholder
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.DarkGray
                                )

                                // Load the actual profile image on top
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageCache.createProfileImageRequest(
                                            context = context,
                                            url = model.profilePicture
                                        ),
                                        onSuccess = { Log.d("HomeScreen", "Profile image loaded successfully: ${model.profilePicture}") }
                                    ),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        // Default profile icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = model.userName,
                        fontSize = 23.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Text(
                        text = model.designation,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )

                    Text(
                        text = model.department,
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )

                    Text(
                        text = model.employeeId,
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveHomeScreen(
    model: HomeModel,
    employeeData: AboutMeModel,
    onItemClick: (HomeItem) -> Unit,
    onAllAppsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onShowProfileClick: () -> Unit,
    onToggleFavorite: (HomeItem) -> Unit,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit,
    onXCardClick: () -> Unit,
    isAuthenticating: Boolean = false,
    onRefresh: () -> Unit = {},
    controller: HomeController,
    eventData: EventResponse? = null,
    showEventPopup: Boolean = false,
    onDismissEventPopup: () -> Unit = {},
    navigator: com.archeGlobal.one.navigation.Navigator? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val windowSizeClass = calculateWindowSizeClass(activity ?: return)
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 3 // Phone portrait
        WindowWidthSizeClass.Medium -> 5 // Large phone/Small tablet
        WindowWidthSizeClass.Expanded -> 6 // Tablet landscape
        else -> 3
    }

    HomeScreenContent(
        model = model,
        employeeData = employeeData,
        onItemClick = onItemClick,
        onAllAppsClick = onAllAppsClick,
        onFavoritesClick = onFavoritesClick,
        onShowProfileClick = onShowProfileClick,
        onToggleFavorite = onToggleFavorite,
        onFooterHomeClick = onFooterHomeClick,
        onFooterChatClick = onFooterChatClick,
        onFooterSOSClick = onFooterSOSClick,
        onFooterProfileClick = onFooterProfileClick,
        onXCardClick = onXCardClick,
        isAuthenticating = isAuthenticating,
        onRefresh = onRefresh,
        controller = controller,
        eventData = eventData,
        showEventPopup = showEventPopup,
        onDismissEventPopup = onDismissEventPopup,
        columns = columns, // Pass the column count
        navigator = navigator
    )
}

@Composable
fun HomeScreenContent(
    model: HomeModel,
    employeeData: AboutMeModel,
    onItemClick: (HomeItem) -> Unit,
    onAllAppsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onShowProfileClick: () -> Unit,
    onToggleFavorite: (HomeItem) -> Unit,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit,
    onXCardClick: () -> Unit,
    isAuthenticating: Boolean = false,
    onRefresh: () -> Unit = {},
    controller: HomeController,
    eventData: EventResponse? = null,
    showEventPopup: Boolean = false,
    onDismissEventPopup: () -> Unit = {},
    columns: Int = 3, // Default to 3 for phones
    navigator: com.archeGlobal.one.navigation.Navigator? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
        val context = LocalContext.current

        // Initialize UserDataManager and LoginController for refresh functionality
        val userDataManager = remember { UserDataManager.getInstance(context) }
        val loginController = remember { navigator?.let { LoginController(context, it) } }

        // Observe the locked state
        val lockedState = userDataManager.preferencesManager.lockedState.collectAsState().value
        // Initialize background model with proper colors to prevent black screen
        val backgroundModel = remember(lockedState) { WelcomeBackgroundModel() }

        val appContext = LocalContext.current
        val mpinController = remember { MpinController(appContext) }
        val biometricHelper = remember { BiometricHelper(appContext) }
        val isBiometricEnabled = remember { biometricHelper.canUseBiometric() && biometricHelper.isBiometricEnabled() }
        var enteredMpin by remember { mutableStateOf("") }
        var mpinError by remember { mutableStateOf<String?>(null) }
        val focusRequesters = List(4) { remember { FocusRequester() } }
        var isVerifyingMpin by remember { mutableStateOf(false) }
        var selectedApp by remember { mutableStateOf<HomeItem?>(null) }
        var selectedPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
        var isRefreshing by remember { mutableStateOf(false) }

        // SwipeRefresh state
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

        // Get user data (replace with your actual user data source)
        val apiService = RetrofitClient.apiService

        // State to track the current view (All Apps or Favorites)
        var currentView by remember { mutableStateOf("All Apps") }

        // --- Rating Pop-up Logic ---
        var navigationCount by rememberSaveable { mutableStateOf(0) }
        var showRatingDialog by rememberSaveable { mutableStateOf(false) }
        var rating by rememberSaveable { mutableStateOf(0) }
        var feedbackText by rememberSaveable { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        // Register the callback with the controller
        LaunchedEffect(Unit) {
            controller.onShowRatingDialog = { showRatingDialog = true }
        }

        // Handle refresh functionality with login API call
        fun performRefresh() {
            isRefreshing = true
            val refreshStartTime = System.currentTimeMillis()

            // Get stored user credentials
            val lastEmail = userDataManager.preferencesManager.getString("last_user_email", "") ?: ""
            val lastMobile = userDataManager.preferencesManager.getString("last_user_mobile", "") ?: ""
            val lastEmployeeId = userDataManager.preferencesManager.getString("last_user_employee_id", "") ?: ""
            val currentToken = userDataManager.preferencesManager.getAuthToken()

            if (currentToken != null && loginController != null && lastEmail.isNotEmpty()) {
                Log.d("HomeScreen", "Refreshing with stored credentials - Email: $lastEmail")

                loginController.loginWithToken(
                    token = currentToken,
                    email = lastEmail,
                    mobile = lastMobile,
                    employeeId = lastEmployeeId
                ) { message, isError ->
                    // Calculate elapsed time and ensure minimum 2-second loading
                    val elapsedTime = System.currentTimeMillis() - refreshStartTime
                    val remainingTime = maxOf(0, 2000 - elapsedTime) // 2000ms = 2 seconds

                    // Use coroutine to handle the delay
                    CoroutineScope(Dispatchers.Main).launch {
                        if (remainingTime > 0) {
                            kotlinx.coroutines.delay(remainingTime)
                        }

                        if (isError) {
                            Log.e("HomeScreen", "Refresh failed: $message")
                            // Check if token expired (should navigate to login)
                            if (message.contains("token", ignoreCase = true) || message.contains("unauthorized", ignoreCase = true) ||
                                message.contains("expired", ignoreCase = true)
                            ) {
                                Log.d("HomeScreen", "Token expired during refresh, navigating to login")
                                navigator?.navigateToLoginScreen()
                            }
                        } else {
                            Log.d("HomeScreen", "Refresh successful: $message")
                            // Refresh the home screen data
                            controller.refreshUserData()
                            onRefresh()
                        }
                        isRefreshing = false
                    }
                }
            } else {
                Log.e("HomeScreen", "Cannot refresh - missing credentials or controller")
                // Ensure minimum 2-second loading even for error case
                CoroutineScope(Dispatchers.Main).launch {
                    val elapsedTime = System.currentTimeMillis() - refreshStartTime
                    val remainingTime = maxOf(0, 2000 - elapsedTime)
                    if (remainingTime > 0) {
                        kotlinx.coroutines.delay(remainingTime)
                    }
                    isRefreshing = false
                }
            }
        }

        // Effect to handle refresh completion
        LaunchedEffect(isRefreshing) {
            if (isRefreshing && loginController == null) {
                // Fallback to original refresh if no login controller available
                onRefresh()
                kotlinx.coroutines.delay(1000)
                isRefreshing = false
            }
        }

        // Collect Pride Month related states
        val isPrideMonth = controller.isPrideMonth.collectAsState().value
        val showPrideMonthDialog = controller.showPrideMonthDialog.collectAsState().value
        val isUsingPrideIcon = remember { mutableStateOf(controller.isUsingPrideIcon()) }
        
        // Mark services as seen after a short delay to let user see New stickers
        LaunchedEffect(model.categories) {
            if (model.categories.isNotEmpty()) {
                // Wait 2 seconds before marking services as seen
                // This gives user time to see the New stickers on fresh install
                kotlinx.coroutines.delay(2000)
                val allServices = model.categories.values.flatten()
                controller.markServicesAsSeen(allServices)
            }
        }

        // Show Pride Month Dialog if it's Pride Month and dialog should be shown
        if (isPrideMonth && showPrideMonthDialog) {
            PrideMonthDialog(
                isUsingPrideIcon = isUsingPrideIcon.value,
                onDismiss = {
                    controller.dismissPrideMonthDialog()
                },
                onToggleIcon = {
                    controller.togglePrideIcon()
                    isUsingPrideIcon.value = controller.isUsingPrideIcon()
                }
            )
        }

        // Always show MPIN prompt if locked and biometric is not enabled
        if (lockedState && !isBiometricEnabled) {
            // Overlay to block all interaction and blur background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .blur(12.dp)
                    .zIndex(100f) // High zIndex to block everything
                    .pointerInput(Unit) {} // Block pointer events
            )

            // Universal loader when verifying MPIN
            if (isVerifyingMpin) {
                UniversalLoader(isLoading = true)
            }

            val navController = androidx.navigation.compose.rememberNavController()

            // Keyboard-aware MPIN prompt box
            val density = LocalDensity.current
            val keyboardHeight = WindowInsets.ime.getBottom(density)
            val isKeyboardVisible = keyboardHeight > 0

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(101f),
                contentAlignment = if (isKeyboardVisible) Alignment.TopCenter else Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFFFEF7F2),
                    shadowElevation = 24.dp,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .widthIn(min = 340.dp, max = 420.dp)
                        .padding(horizontal = 16.dp)
                        .then(
                            if (isKeyboardVisible) {
                                Modifier.padding(top = 32.dp)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                            .widthIn(min = 340.dp, max = 420.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Red lock icon
                        Icon(
                            painter = painterResource(id = R.drawable.lock), // Use your red lock icon
                            contentDescription = "Lock",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        // Title
                        Text(
                            "Enter Your MPIN",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Subtitle
                        Column(
                            modifier = Modifier
                                .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        ) {
                            Text(
                                "Please enter your 4-digit MPIN to unlock the app",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        // "Enter MPIN" label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Enter MPIN",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Color(0xFF7B7B7B),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // MPIN digit boxes
                        var focusedIndex by remember { mutableStateOf(-1) }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 0 until 4) {
                                // ...inside your Row for MPIN digit boxes...
                                OutlinedTextField(
                                    value = enteredMpin.getOrNull(i)?.toString() ?: "",
                                    onValueChange = { value ->
                                        if (value.length <= 1 && value.all { it.isDigit() }) {
                                            val chars = enteredMpin.padEnd(4).toCharArray()
                                            chars[i] = value.firstOrNull() ?: ' '
                                            enteredMpin = String(chars).replace(" ", "")
                                            if (value.isNotEmpty() && i < 3) {
                                                focusRequesters[i + 1].requestFocus()
                                            }
                                        }
                                        if (value.isEmpty() && i > 0) {
                                            val chars = enteredMpin.padEnd(4).toCharArray()
                                            chars[i] = ' '
                                            enteredMpin = String(chars).replace(" ", "")
                                            focusRequesters[i - 1].requestFocus()
                                        }
                                    },
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(65.dp)
                                        .focusRequester(focusRequesters[i])
                                        .padding(horizontal = 4.dp)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                focusedIndex = i
                                            }
                                        }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (focusedIndex == i) Color(0xFFDD3825) else Color.Gray,
                                            shape = MaterialTheme.shapes.medium
                                        ),
                                    textStyle = TextStyle(
                                        fontSize = 28.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    ),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        disabledContainerColor = Color.White,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        disabledTextColor = Color.Black,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    isError = mpinError != null && enteredMpin.length == 4
                                )
                                if (i < 3) Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                        // Unlock button
                        Button(
                            onClick = {
                                if (enteredMpin.isEmpty()) {
                                    mpinError = "Please enter the MPIN"
                                } else if (enteredMpin.length < 4) {
                                    mpinError = "Please enter the MPIN"
                                } else {
                                    isVerifyingMpin = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        kotlinx.coroutines.delay(700)
                                        if (mpinController.validateMpin(enteredMpin)) {
                                            mpinError = null
                                            enteredMpin = ""
                                            userDataManager.preferencesManager.setAppLockState(false)
                                        } else {
                                            mpinError = "Invalid MPIN"
                                            enteredMpin = ""
                                        }
                                        isVerifyingMpin = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                contentColor = Color.White
                            ),
                            enabled = !isVerifyingMpin
                        ) {
                            Text(
                                "Unlock",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Reset MPIN button
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(appContext, MpinActivity::class.java)
                                intent.putExtra("resetMpin", true)
                                appContext.startActivity(intent)
                            },
                            modifier = Modifier
                                .width(140.dp)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0B4AA),
                                contentColor = Color(0xFFDD3825),
                                disabledContainerColor = Color(0xFFE0B4AA),
                                disabledContentColor = Color(0xFFDD3825)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFDD3825)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "Reset MPIN",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (mpinError != null) {
                            Text(
                                text = mpinError!!,
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Show event popup if available and visibility is true
        // Only show regular event popup if it's not Pride Month
        if (!isPrideMonth && eventData != null && showEventPopup) {
            Log.d("HomeScreen", "Showing event popup with data: Title=${eventData.title}, Image=${eventData.image}")
            Log.d("HomeScreen", "Event description: ${eventData.description}")
            EventPopup(
                event = eventData,
                onDismiss = onDismissEventPopup
            )
        } else {
            Log.d("HomeScreen", "Not showing event popup - eventData present: ${eventData != null}, showEventPopup: $showEventPopup")
            if (eventData != null) {
                Log.d("HomeScreen", "Event data exists but popup flag is false - Title: ${eventData.title}")
            }
        }

        // Show celebration dialog
        val showCelebrationDialog = controller.showCelebrationDialog.collectAsState().value
        val celebrationData = controller.celebrationData.collectAsState().value
        if (showCelebrationDialog) {
            CelebrationDialog(
                celebrationData = celebrationData,
                onDismiss = { controller.dismissCelebrationDialog() },
                onWishesClick = { email, name, type -> controller.onCelebrationWishesClick(email, name, type) },
                onViewAllClick = {
                    controller.dismissCelebrationDialog()
                    // Navigate to AllCelebrationActivity
                    val intent = android.content.Intent(context, com.archeGlobal.one.AllCelebrationActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }

        // Show WhatsNew dialog
        val showWhatsNewDialog = controller.showWhatsNewDialog.collectAsState().value
        val whatsNewData = com.archeGlobal.one.utils.UserDataManager.getInstance(context).getWhatsNewData()
        if (showWhatsNewDialog && !whatsNewData.isNullOrEmpty()) {
            com.archeGlobal.one.ui.components.WhatsNewDialog(
                whatsNewItems = whatsNewData,
                onDismiss = { controller.dismissWhatsNewDialog() }
            )
        }

        // Wrap with FooterScaffold for bottom navigation
        FooterScaffold(
            footerNavigation = model.footerNavigation,
            isUsingPrideIcon = isUsingPrideIcon.value,
            onFooterHomeClick = onFooterHomeClick,
            onFooterChatClick = onFooterChatClick,
            onFooterSOSClick = onFooterSOSClick,
            onFooterProfileClick = onFooterProfileClick
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main content with conditional blur and pull-to-refresh
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(
                            radius = if (lockedState && !isBiometricEnabled) {
                                12.dp
                            } else if (isAuthenticating) {
                                10.dp
                            } else {
                                0.dp
                            }
                        )
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = { /* Handle drag end */ },
                                onHorizontalDrag = { _, dragAmount ->
                                    if (dragAmount > 50) {
                                        // Swiped from left to right
                                        currentView = "All Apps"
                                    } else if (dragAmount < -50) {
                                        // Swiped from right to left
                                        currentView = "Favorites"
                                    }
                                }
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        backgroundModel.topColor,
                                        backgroundModel.middleColor,
                                        backgroundModel.bottomColor
                                    )
                                )
                            )
                    ) {
                        ProfileHeader(
                            model = model,
                            onShowProfileClick = onShowProfileClick
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Celebration banner
                        val celebrationData = controller.celebrationData.collectAsState().value
                        CelebrationBanner(
                            celebrationData = celebrationData,
                            onClick = { controller.showCelebrationDialog() }
                        )

                        // Pride banner with pins - click to open change icon dialog
                        if (isPrideMonth) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .background(
                                        color = Color(0x1ADD3825),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { controller.showPrideMonthDialog() },
                                    modifier = Modifier
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.leftpin),
                                        contentDescription = "Change Icon",
                                        tint = Color(0xFFDD3825),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Celebrating love, equality, and pride this month and always.",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { // Show Pride Month dialog when text is clicked
                                            controller.showPrideMonthDialog()
                                        }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { controller.showPrideMonthDialog() },
                                    modifier = Modifier
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.rightpin),
                                        contentDescription = "Change Icon",
                                        tint = Color(0xFFDD3825),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Toggle Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    onAllAppsClick(); currentView = "All Apps"
                                },
                                modifier = Modifier.width(150.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentView == "All Apps") {
                                        Color(0xFFDD3825)
                                    } else {
                                        CardBackground
                                    },
                                    contentColor = if (currentView == "All Apps") {
                                        Color.White
                                    } else {
                                        Color.Black
                                    }
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentView == "All Apps") Color(0xFFDD3825) else DividerColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "All Apps",
                                    fontSize = 16.sp, // Added font size
                                    fontFamily = GraphikFontFamily, // Added font family
                                    fontWeight = FontWeight.Medium // Added font weight
                                )
                            }

                            Spacer(modifier = Modifier.width(30.dp))

                            Button(
                                onClick = {
                                    onFavoritesClick(); currentView = "Favorites"
                                },
                                modifier = Modifier.width(150.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentView == "Favorites") {
                                        Color(0xFFDD3825)
                                    } else {
                                        CardBackground
                                    },
                                    contentColor = if (currentView == "Favorites") {
                                        Color.White
                                    } else {
                                        Color.Black
                                    }
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentView == "Favorites") Color(0xFFDD3825) else DividerColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Favorites",
                                    fontSize = 16.sp, // Added font size
                                    fontFamily = GraphikFontFamily, // Added font family
                                    fontWeight = FontWeight.Medium // Added font weight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Content area with SwipeRefresh
                        Box(modifier = Modifier.weight(1f)) {
                            SwipeRefresh(
                                state = swipeRefreshState,
                                onRefresh = { performRefresh() },
                                modifier = Modifier.fillMaxSize(),
                                indicator = { _, _ -> // Empty indicator - we'll use UniversalLoader instead
                                }
                            ) {
                                if (currentView == "All Apps") {
                                    // All Apps View
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp)
                                    ) {
                                        model.categories.forEach { (category, items) ->
                                            item {
                                                CategoryHeader(
                                                    title = category,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                )
                                            }

                                            items(items.chunked(columns)) { rowItems ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    rowItems.forEach { item ->
                                                        AppItem(
                                                            title = item.title,
                                                            isFavorite = item.isFavorite,
                                                            // Wrap onClick with handleNavigation!
                                                            onClick = { onItemClick(item) },
                                                            onFavoriteClick = { onToggleFavorite(item) },
                                                            modifier = Modifier.weight(1f),
                                                            showFavoriteButton = false,
                                                            isSelected = false,
                                                            onLongPress = { position ->
                                                                selectedApp = item
                                                                selectedPosition = position
                                                            },
                                                            isNew = item.isNew,
                                                            stickerText = item.stickerText
                                                        )
                                                    }
                                                    repeat(columns - rowItems.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                            }
                                        }
                                    }
                                } else if (currentView == "Favorites") {
                                    // Favorites View
                                    if (model.favorites.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            EmptyFavorites()
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                                        ) {
                                            model.favorites.forEach { (category, items) ->
                                                item {
                                                    CategoryHeader(
                                                        title = category,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp)
                                                    )
                                                }

                                                items(items.chunked(columns)) { rowItems ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        rowItems.forEach { item ->
                                                            AppItem(
                                                                title = item.title,
                                                                isFavorite = true,
                                                                // Wrap onClick with handleNavigation!
                                                                onClick = { onItemClick(item) },
                                                                onFavoriteClick = { onToggleFavorite(item) },
                                                                modifier = Modifier.weight(1f),
                                                                showFavoriteButton = false,
                                                                isSelected = false,
                                                                onLongPress = { position ->
                                                                    selectedApp = item
                                                                    selectedPosition = position
                                                                },
                                                                isNew = item.isNew,
                                                                stickerText = item.stickerText
                                                            )
                                                        }
                                                        repeat(columns - rowItems.size) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            } // Close SwipeRefresh
                        }
                    }
                }
            }

            // Show universal loader for all refresh operations
            if (isRefreshing) {
                UniversalLoader(isLoading = isRefreshing)
            }

            // Semi-transparent overlay when an app is selected
            if (selectedApp != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .blur(radius = 8.dp)
                        .clickable(onClick = {
                            selectedApp = null
                            selectedPosition = null
                        })
                )
            }

            // Overlay the selected app
            if (selectedApp != null) {
                selectedPosition?.let { (x, y) ->
                    val density = LocalDensity.current
                    val itemSize = 80.dp
                    val scaleFactor = 1.2f // Slightly bigger than original
                    val itemSizePx = with(density) { itemSize.toPx() }

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (x - itemSizePx * scaleFactor / 2).toInt(),
                                    y = (y - itemSizePx - 15).toInt() // Position exactly above with exact pixel offset
                                )
                            }
                    ) {
                        val formattedTitle = formatServiceTitle(selectedApp!!.title)

                        Card(
                            modifier = Modifier
                                .width(115.dp) // Set fixed width
                                .height(115.dp), // Set fixed height
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    AppIcon(title = selectedApp!!.title, modifier = Modifier.size(50.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = formattedTitle,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        lineHeight = 14.sp,
                                        overflow = TextOverflow.Visible,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Show favorite dialog
            if (selectedApp != null && selectedPosition != null) {
                selectedPosition?.let { (x, y) ->
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val dialogWidth = 160.dp // Return to original width
                    val density = LocalDensity.current

                    val dialogWidthPx = with(density) { dialogWidth.toPx() }
                    val screenWidthPx = with(density) { screenWidth.toPx() }
                    val itemSizePx = with(density) { 80.dp.toPx() }
                    val scaleFactor = 1.1f // Same as app scale factor

                    // Calculate x position (centered with the app)
                    val xOffset = when {
                        x + (dialogWidthPx / 2) > screenWidthPx -> screenWidthPx - dialogWidthPx - 16f
                        x - (dialogWidthPx / 2) < 0 -> 16f
                        else -> x - (dialogWidthPx / 2)
                    }

                    // Reduce the yOffset to decrease the space between the service card and the dialog
                    val yOffset = y - itemSizePx - 180 // Reduced from 235 to 200

                    Card(
                        modifier = Modifier
                            .width(dialogWidth)
                            .offset {
                                IntOffset(
                                    x = xOffset.toInt(),
                                    y = yOffset.toInt()
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = if (selectedApp!!.isFavorite) "Remove from Favourites" else "Add to Favourites",
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onToggleFavorite(selectedApp!!)
                                    selectedApp = null
                                    selectedPosition = null
                                }
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Show authentication overlay if authenticating
            if (isAuthenticating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Optional: Add a fingerprint icon or loading indicator here
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // --- Rating Dialog ---
            if (showRatingDialog) {
                Dialog(onDismissRequest = { showRatingDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "How was your experience?",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                maxLines = 2,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center, // Center align the text
                                modifier = Modifier.fillMaxWidth() // Make sure it uses the full width
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                for (i in 1..5) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (i <= rating) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                                        ),
                                        contentDescription = "Star $i",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clickable {
                                                rating = i
                                                // If 4 or 5 stars selected, redirect to Play Store immediately
                                                if (i >= 4) {
                                                    // Submit feedback first
                                                    val feedbackRequest = FeedbackRequest(
                                                        name = employeeData.name,
                                                        email = employeeData.email,
                                                        category = "App rating",
                                                        feedback = null,
                                                        rating = i,
                                                        platform = "Android",
                                                        deviceName = android.os.Build.MODEL,
                                                        version = android.os.Build.VERSION.RELEASE
                                                    )

                                                    // Submit feedback in background
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            apiService.submitFeedback(feedbackRequest)
                                                        } catch (e: Exception) {
                                                            // Log error but don't show to user
                                                        }
                                                    }

                                                    // Redirect to Play Store immediately
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.archeGlobal.one"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Unable to open Play Store", Toast.LENGTH_SHORT).show()
                                                    }

                                                    // Close dialog
                                                    showRatingDialog = false
                                                    rating = 0
                                                    feedbackText = ""
                                                }
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            // Show feedback field if rating is 3 or below and user has selected a rating
                            if (rating in 1..3) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = feedbackText,
                                    onValueChange = { feedbackText = it },
                                    placeholder = { Text("Please tell us what could be better") },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 4
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (rating == 0) {
                                        Toast.makeText(context, "Please select a rating.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (rating <= 3 && feedbackText.isBlank()) {
                                        Toast.makeText(context, "Please provide feedback.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isSubmitting = true

                                    // Prepare request (only for 1-3 star ratings, 4-5 stars are handled on selection)
                                    val feedbackRequest = FeedbackRequest(
                                        name = employeeData.name,
                                        email = employeeData.email,
                                        category = "App rating",
                                        feedback = feedbackText,
                                        rating = rating,
                                        platform = "Android",
                                        deviceName = android.os.Build.MODEL,
                                        version = android.os.Build.VERSION.RELEASE
                                    )

                                    // Call API
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val response = apiService.submitFeedback(feedbackRequest)
                                            withContext(Dispatchers.Main) {
                                                isSubmitting = false
                                                if (response.isSuccessful) {
                                                    Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                                                    showRatingDialog = false
                                                    rating = 0
                                                    feedbackText = ""
                                                } else {
                                                    Toast.makeText(context, "Failed to submit feedback.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isSubmitting = false
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                    disabledContainerColor = Color(0xFFDD3825), // keep red even when disabled
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                )
                            ) {
                                Text(
                                    if (isSubmitting) "Submit" else "Submit",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Update this helper function to better format long titles
private fun formatServiceTitle(title: String): String {
    // Special cases for specific long titles - forcing proper line breaks
    return when (title) {
        "DeskCart" -> "DeskCart"
        "Vision" -> "Vision"
        "Core Values" -> "CoreValues"
        "Communique" -> "Communique"
        "About Us" -> "AboutUs"
        "Calendar" -> "Calendar"
        "Holiday Calendar" -> "Holiday\nCalendar"
        "New Onboarding" -> "New\nOnboarding"
        "TravelDesk" -> "TravelDesk"
        "HelpDesk" -> "HelpDesk"
        "Goal Setting/KPI" -> "Goal\nSetting/KPI"
        "Business Card" -> "Business\nCard"
        "My Documents" -> "My\nDocuments"
        "MyDocuments" -> "My\nDocuments"
        "ZenTask" -> "ZenTask"
        "My Career" -> "My\nCareer"
        "Admin" -> "Admin"
        "Medical" -> "Medical"
        "ID" -> "ID"
        "MyPay" -> "MyPay"
        "SAP" -> "SAP"
        "Ample" -> "Ample"
        "SOS" -> "SOS"
        "Connect" -> "Connect"
        "Locations" -> "Locations"
        "Policy" -> "Policy"
        "Profile" -> "Profile"
        "TimeSheet", "Timesheet" -> "TimeSheet"
        "Leave" -> "Leave"
        "eLearning" -> "eLearning"
        "Asset" -> "Asset"
        "Greetings" -> "Greetings"
        "Profile Connect" -> "Profile\nConnect"
        "Arche Odyssey" -> "Arche\nOdyssey"
        "Idea Vault", "IdeaVault" -> "IdeaVault"
        "Smart Collateral" -> "Smart\nCollateral"
        else -> {
            // For any other multi-word titles, always split at a space
            if (title.contains(" ")) {
                val words = title.split(" ")
                if (words.size >= 2) {
                    // If there are multiple words, split appropriately
                    if (words.size == 2) {
                        // Just two words, simple split
                        "${words[0]}\n${words[1]}"
                    } else {
                        // More than two words, balance the lines
                        val midpoint = words.size / 2
                        val firstPart = words.take(midpoint).joinToString(" ")
                        val secondPart = words.drop(midpoint).joinToString(" ")
                        "$firstPart\n$secondPart"
                    }
                } else {
                    title
                }
            } else if (title.length > 7) {
                // For long single words, split in half
                val mid = title.length / 2
                title.substring(0, mid) + "\n" + title.substring(mid)
            } else {
                title
            }
        }
    }
}

@Composable
private fun AppItem(
    title: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: (Pair<Float, Float>) -> Unit,
    isNew: Boolean = false,
    stickerText: String = "New"
) {
    var itemPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    val context = LocalContext.current
    // Format the title for better display
    val formattedTitle = formatServiceTitle(title)

    Card(
        modifier = modifier
            .aspectRatio(0.95f)
            .padding(3.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                itemPosition = Pair(
                    position.x + (coordinates.size.width / 2),
                    position.y + (coordinates.size.height / 2)
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    },
                    onLongPress = {
                        itemPosition?.let { pos -> onLongPress(pos) }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Icon at the top
                AppIcon(title = title, modifier = Modifier.size(50.dp))

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formattedTitle,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // New sticker in top-right corner with straight right edge and curved left edge
            if (isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp)
                        .background(
                            color = Color(0xFFDD3825),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 0.dp,
                                bottomStart = 12.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .padding(horizontal = 9.dp, vertical = 2.dp)
                        .height(16.dp)
                ) {
                    Text(
                        text = stickerText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIcon(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Check if it's a default app
        when (title) {
            "My Documents", "MyDocuments", "ID", "Asset", "Business Card", "Leave", "DeskCart", "Smart Collateral",
            "eLearning", "My Career", "Timesheet", "TimeSheet", "Goal Setting/KPI", "Admin", "Vision",
            "MyPay", "SAP", "Ample", "SOS", "Holiday Calendar", "Calendar", "About Us", "Communique", "Core Values", "CoreValues", "Greetings", "Medical", "Blogs",
            "Locations", "TravelDesk", "Policy", "New Onboarding", "Profile", "Profile Connect", "ZenTask", "Password Reset", "Know Your Org", "Arche Odyssey", "ZingHR", "IdeaVault", "Pulse", "HelpDesk" -> {
                Surface(
                    modifier = Modifier.size(128.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(
                            id = when (title.lowercase().replace(" ", "")) {
                                "deskcart" -> R.drawable.deskcart
                                "vision" -> R.drawable.vision
                                "corevalues" -> R.drawable.core_values
                                "communique" -> R.drawable.communique
                                "aboutus" -> R.drawable.about_us
                                "mydocuments" -> R.drawable.mydocuments
                                "id" -> R.drawable.id
                                "asset" -> R.drawable.asset
                                "businesscard" -> R.drawable.xcard
                                "leave" -> R.drawable.leave
                                "elearning" -> R.drawable.elearning
                                "mycareer" -> R.drawable.mycareer
                                "timesheet" -> R.drawable.timesheet
                                "goalsetting/kpi", "goal" -> R.drawable.goals
                                "admin" -> R.drawable.admin
                                "mypay" -> R.drawable.finance
                                "sap" -> R.drawable.sap
                                "ample" -> R.drawable.ample
                                "sos" -> R.drawable.sos
                                "calendar" -> R.drawable.holiday
                                "holidaycalendar" -> R.drawable.holiday
                                "greetings" -> R.drawable.greetings
                                "medical" -> R.drawable.medical
                                "blogs" -> R.drawable.xconnect
                                "locations" -> R.drawable.locations
                                "traveldesk" -> R.drawable.travel
                                "policy" -> R.drawable.policy
                                "newonboarding" -> R.drawable.new_onboarding
                                "profile" -> R.drawable.profile
                                "profileconnect" -> R.drawable.profile
                                "zentask" -> R.drawable.todo
                                "passwordreset" -> R.drawable.password_reset
                                "knowyourog" -> R.drawable.know_your_org
                                "archeodyssey" -> R.drawable.arche_odyssey
                                "zinghr" -> R.drawable.zinghr
                                "ideavault" -> R.drawable.idea_vault
                                "pulse" -> R.drawable.pulse
                                "helpdesk" -> R.drawable.helpdesk
                                "smartcollateral" -> R.drawable.smart
                                else -> R.drawable.mydocuments
                            }
                        ),
                        contentDescription = title,
                        modifier = Modifier
                            .padding(5.dp) // Increase padding if needed
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            else -> {
                // Original placeholder design for non-default apps
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = getColorForApp(title).copy(alpha = 0.1f)
                ) {
                    Box {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pattern = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, size.height)
                                moveTo(size.width, 0f)
                                lineTo(0f, size.height)
                            }
                            drawPath(
                                path = pattern,
                                color = Color.White.copy(alpha = 0.1f),
                                style = Stroke(width = 1f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = getColorForApp(title),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
