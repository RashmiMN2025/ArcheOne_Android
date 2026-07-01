package com.archeGlobal.one.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.MSALAuthenticationController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.model.AuthResponse
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.components.CompanyLogo
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.BiometricHelper
import com.archeGlobal.one.utils.CustomToast
import com.archeGlobal.one.utils.DeviceInfoUtils
import com.archeGlobal.one.utils.MpinManager
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.isFirstTimeLogin
import com.archeGlobal.one.utils.setFirstTimeLogin

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveLoginScreen(
    controller: LoginController,
    navigator: Navigator,
    forceOriginalLogin: Boolean = false,
    forceDifferentUserMode: Boolean = false,
    clearFields: Boolean = false,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val contentPadding =
        when (windowSizeClass?.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 16.dp // Phone
            WindowWidthSizeClass.Medium -> 48.dp // Large phone/small tablet
            WindowWidthSizeClass.Expanded -> 120.dp // Tablet
            else -> 16.dp
        }
    LoginScreen(
        controller = controller,
        navigator = navigator,
        forceOriginalLogin = forceOriginalLogin,
        forceDifferentUserMode = forceDifferentUserMode,
        contentPadding = contentPadding,
        clearFields = clearFields,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    controller: LoginController,
    navigator: Navigator,
    forceOriginalLogin: Boolean = false,
    forceDifferentUserMode: Boolean = false,
    contentPadding: Dp = 16.dp, // <-- Add this parameter
    clearFields: Boolean = false,
) {
    val context = LocalContext.current
    val componentActivity = context as? ComponentActivity
    val msalAuthenticationController: MSALAuthenticationController? = remember(componentActivity) {
        componentActivity?.let { MSALAuthenticationController(it, AndroidNavigator(it)) }
    }
    var msalInitialized by remember { mutableStateOf(false) }
    val userDataManager = UserDataManager.getInstance(context)
    val preferencesManager = PreferencesManager(context)

    var email by remember { mutableStateOf(if (clearFields) "" else preferencesManager.getString("lastEmail", "") ?: "") }
    var mobile by remember { mutableStateOf(if (clearFields) "" else preferencesManager.getString("lastMobile", "") ?: "") }
    var employeeId by remember { mutableStateOf(if (clearFields) "" else preferencesManager.getString("lastEmployeeId", "") ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mobileVisible by remember { mutableStateOf(false) }
    var firstTimeLogin by remember { mutableStateOf(forceOriginalLogin || isFirstTimeLogin(context)) }
    var authResponse by remember { mutableStateOf<AuthResponse?>(null) }
    var showMfaTermsDialog by remember { mutableStateOf(false) }
    var showOtpTermsDialog by remember { mutableStateOf(false) }
    // Inside your LoginScreen composable:
    var stayLoggedIn by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }

    // Get session expired status from intent
    val activity = context as? Activity
    val sessionExpired = activity?.intent?.getBooleanExtra("session_expired", false) ?: false

    // Get last user name from preserved data (works for both logout and session expiry)
    val lastEmployeeName =
        if (sessionExpired) {
            preferencesManager.getString("session_expired_name", "") ?: preferencesManager.getString("last_user_name", "")
                ?: userDataManager.getLastUsername()
        } else {
            preferencesManager.getString("last_user_name", "") ?: userDataManager.getLastUsername()
        }

    // For session expiry, treat as returning user if we have preserved data
    val shouldTreatAsReturningUser = sessionExpired && !lastEmployeeName.isNullOrEmpty()

    val isLoggedIn = userDataManager.isLoggedIn()
    val hasLoggedIn = userDataManager.hasUserLoggedIn()
    val biometricHelper = remember { BiometricHelper(context) }
    var isDifferentUserMode by remember { mutableStateOf(forceDifferentUserMode) }

    // Make biometric button state reactive - don't use remember so it re-evaluates
    val canUseBiometric = biometricHelper.canUseBiometric()
    val isBiometricEnabled = biometricHelper.isBiometricEnabled()
    val showBiometricButton = canUseBiometric && isBiometricEnabled

    // Debug biometric state
    LaunchedEffect(Unit) {
        Log.d(
            "LoginScreen",
            "Biometric State - canUseBiometric: $canUseBiometric, isBiometricEnabled: $isBiometricEnabled, showBiometricButton: $showBiometricButton",
        )
    }

    // Update firstTimeLogin when the screen is created, considering session expiry
    LaunchedEffect(Unit) {
        val calculatedFirstTime = forceOriginalLogin || (isFirstTimeLogin(context) && !shouldTreatAsReturningUser)
        firstTimeLogin = calculatedFirstTime
        Log.d(
            "LoginScreen",
            "Screen Created: firstTimeLogin=$firstTimeLogin, forceOriginalLogin=$forceOriginalLogin, isFirstTimeLogin=${isFirstTimeLogin(
                context,
            )}, sessionExpired=$sessionExpired, shouldTreatAsReturningUser=$shouldTreatAsReturningUser",
        )
    }

    var showFingerprint by remember { mutableStateOf(false) }

    // Update showFingerprint when relevant conditions change
    LaunchedEffect(
        firstTimeLogin,
        showBiometricButton,
        forceDifferentUserMode,
        sessionExpired,
        shouldTreatAsReturningUser,
        isBiometricEnabled,
    ) {
        // Show fingerprint for returning users (including session expired) if biometric is available
        showFingerprint = showBiometricButton && (!firstTimeLogin || sessionExpired || shouldTreatAsReturningUser)
        // Reset isDifferentUserMode after normal logout (when it's not forced)
        if (!forceDifferentUserMode && !firstTimeLogin) {
            isDifferentUserMode = false
        }

        // Debug logging
        Log.d(
            "LoginScreen",
            "Biometric Debug: showBiometricButton=$showBiometricButton, firstTimeLogin=$firstTimeLogin, isDifferentUserMode=$isDifferentUserMode, sessionExpired=$sessionExpired, shouldTreatAsReturningUser=$shouldTreatAsReturningUser",
        )
        Log.d(
            "LoginScreen",
            "Biometric Debug: canUseBiometric=${biometricHelper.canUseBiometric()}, isBiometricEnabled=${biometricHelper.isBiometricEnabled()}",
        )
        Log.d("LoginScreen", "Biometric Debug: showFingerprint=$showFingerprint")
    }

    // Removed policy WebView state variables - now using external browser

    var selectedLoginMethod by remember { mutableStateOf("OTP") }
    var showOtpFields by remember { mutableStateOf(forceOriginalLogin || firstTimeLogin) }

    var showOtpButton by remember { mutableStateOf(forceOriginalLogin || firstTimeLogin) }

    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(componentActivity) {
        if (componentActivity != null && !msalInitialized) {
            msalAuthenticationController?.initializeMSAL { success ->
                msalInitialized = success
            }
        }
    }

    fun startMfaSignIn() {
        // CRITICAL: Wait for MSAL initialization to complete
        if (!msalInitialized) {
            Log.w("LoginScreen", "MFA sign-in attempted but MSAL not initialized yet")
            errorMessage = "Authentication system initializing. Please wait and try again."
            return
        }

        val activity = context as? Activity
        if (activity == null) {
            errorMessage = "Unable to start authentication"
            return
        }
        isLoading = true
        errorMessage = null

        msalAuthenticationController?.performSignIn(activity) { success, msg ->
            isLoading = false
            if (success) {
                // After successful MSAL + Graph + Backend login:
                // - User details fetched from Microsoft Graph
                // - Backend login successful
                // - Session token received
                // - Navigate to home with authenticated user details
                if (msalAuthenticationController?.isAuthenticated?.value == true) {
                    // Extract user details from MSAL authentication controller
                    // These were populated during performSignIn flow:
                    // 1. Microsoft Graph fetch → populated email, mobile, employeeId
                    // 2. Backend login → validated and stored
                    val email = msalAuthenticationController?.userEmail?.value ?: ""
                    navigator.navigateToHome(true, false, email, "", "")
                }
            } else {
                errorMessage = msg
            }
        }
    }

    val userData = userDataManager.getUserData()

    // Always try to get preserved user data (works for both logout and session expiry)
    val lastUserEmail =
        if (sessionExpired) {
            preferencesManager.getString("session_expired_email", "") ?: preferencesManager.getString("last_user_email", "")
        } else {
            preferencesManager.getString("last_user_email", "")
        }
    val lastUserMobile =
        if (sessionExpired) {
            preferencesManager.getString("session_expired_mobile", "") ?: preferencesManager.getString("last_user_mobile", "")
        } else {
            preferencesManager.getString("last_user_mobile", "")
        }
    val lastUserEmployeeId =
        if (sessionExpired) {
            preferencesManager.getString("session_expired_employee_id", "") ?: preferencesManager.getString("last_user_employee_id", "")
        } else {
            preferencesManager.getString("last_user_employee_id", "")
        }

    val preservedUserData =
        if (!lastUserEmail.isNullOrBlank() && !lastUserMobile.isNullOrBlank() && !lastUserEmployeeId.isNullOrBlank()) {
            UserData(
                name = lastEmployeeName ?: "",
                email = lastUserEmail,
                mobile = lastUserMobile,
                employeeId = lastUserEmployeeId,
                designation = "",
                department = "",
                location = "",
                services = emptyList(),
                profilePic = null,
                sosContact = null,
                userDetails = null,
                greetings = emptyMap(),
            )
        } else {
            null
        }

    val effectiveUserData = userData ?: preservedUserData

    LaunchedEffect(Unit) {
        if (effectiveUserData != null) {
            if (email.isEmpty()) email = effectiveUserData.email ?: ""
            if (mobile.isEmpty()) mobile = effectiveUserData.mobile ?: ""
            if (employeeId.isEmpty()) employeeId = effectiveUserData.employeeId ?: ""
        }
    }

    // Auto-refresh token using stored credentials for better data freshness
    var autoRefreshAttempted by remember { mutableStateOf(false) }
    var isAutoRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(effectiveUserData, isLoggedIn, autoRefreshAttempted) {
        // If we have preserved user data, user is not logged in, and haven't attempted auto-refresh yet
        if (effectiveUserData != null && !isLoggedIn && !autoRefreshAttempted && !forceOriginalLogin && !forceDifferentUserMode) {
            autoRefreshAttempted = true
            isAutoRefreshing = true
            Log.d("LoginScreen", "Attempting auto token refresh with preserved credentials")

            // Try to auto-refresh token using preserved credentials
            val otpController =
                OtpVerificationController(
                    navigator = navigator,
                    context = context,
                )

            // Create a special controller that doesn't auto-navigate
            val backgroundOtpController =
                OtpVerificationController(
                    navigator = navigator,
                    context = context,
                )

            val deviceInfo = DeviceInfoUtils.getAllDeviceInfo(context)
            // Use backgroundRefresh flag to avoid auto-navigation
            backgroundOtpController.verifyOtp(
                email = effectiveUserData.email ?: "",
                mobile = effectiveUserData.mobile ?: "",
                employeeId = effectiveUserData.employeeId ?: "",
                otpFromUser = "", // Empty OTP
                isBiometric = true, // Skip OTP validation
                backgroundRefresh = true,
                appVersion = deviceInfo.appVersion,
                deviceModel = deviceInfo.deviceModel,
                deviceId = deviceInfo.deviceId,
                platform = deviceInfo.platform,
                osVersion = deviceInfo.osVersion,
                stayLoggedIn = stayLoggedIn,
            ) { message, isError ->
                isAutoRefreshing = false
                Log.d("LoginScreen", "Background token refresh result: isError=$isError, message=$message")
                if (!isError) {
                    // Success: Fresh data loaded, but stay on login screen
                    Log.d("LoginScreen", "Background token refresh successful - fresh data loaded, staying on login screen")
                } else {
                    // Failed: Continue with manual login methods
                    Log.d("LoginScreen", "Background token refresh failed: $message")
                }
            }
        }
    }

    LaunchedEffect(firstTimeLogin, isDifferentUserMode, showBiometricButton, effectiveUserData, sessionExpired) {
        Log.d(
            "LoginScreen",
            "Reevaluating login method: hasEffectiveUserData=${effectiveUserData != null}, isLoggedIn=$isLoggedIn, sessionExpired=$sessionExpired, firstTimeLogin=$firstTimeLogin",
        )

        // If we have preserved user data and user is not logged in (token expired/logout), prioritize MFA first
        if (effectiveUserData != null && !isLoggedIn) {
            // For returning users with preserved data (including session expired), prefer MFA by default
            selectedLoginMethod = "MFA"
            showOtpFields = false
            showOtpButton = false
            isDifferentUserMode = false
        }
        // If forceOriginalLogin is true, always show original login form
        else if (forceOriginalLogin) {
            showOtpButton = true
            selectedLoginMethod = "OTP"
            showOtpFields = true
            isDifferentUserMode = false
        } else {
            // Show OTP button only for first-time users or different users
            showOtpButton = firstTimeLogin || isDifferentUserMode

            if (firstTimeLogin || isDifferentUserMode) {
                selectedLoginMethod = "OTP"
                showOtpFields = true
            } else {
                // For existing users (including session expired), default to MFA
                selectedLoginMethod = "MFA"
                showOtpFields = false
            }
        }
    }

    // Show Toast message for errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // Use custom toast for better handling of long messages
            CustomToast.showErrorToast(context, message)
            errorMessage = null
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFFE0DCD1), // Light Beige
                                    Color(0xFFC8C8CA), // Light Gray
                                    Color(0xFF474749), // Dark Gray
                                ),
                        ),
                    ).padding(bottom = 32.dp, start = contentPadding, end = contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // Company Logo
                CompanyLogo(modifier = Modifier.height(120.dp))

                Spacer(modifier = Modifier.height(20.dp))

                // ...inside your Column after the logo and Spacer...

                if (!lastEmployeeName.isNullOrEmpty()) {
                    Text(
                        text = "Welcome, $lastEmployeeName",
                        fontSize = 22.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center,
                    )
                }

                // Hide auto-refresh indicator to avoid showing text to user

                Text(
                    text = "Log in with",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ...inside your Column after the "Log in with" Text...

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.97f)
                            .height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // For new users (firstTimeLogin or isDifferentUserMode): OTP first, then MFA
                    // For returning users: MFA first, then MPIN, then Fingerprint

                    if (firstTimeLogin || isDifferentUserMode) {
                        // OTP button - first for new users
                        if (showOtpButton) {
                            Button(
                                onClick = {
                                    selectedLoginMethod = "OTP"
                                    showOtpFields = true
                                },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(0.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = if (selectedLoginMethod == "OTP") Color(0xFFE0B4AA) else Color.White, // Light shade when selected
                                        contentColor = if (selectedLoginMethod == "OTP") Color(0xFFDD3825) else Color.Black,
                                    ),
                                border = BorderStroke(0.5.dp, Color(0xFFDD3825)),
                            ) {
                                Text(
                                    "OTP",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }

                        // MFA button - second for new users
                        Button(
                            onClick = { selectedLoginMethod = "MFA" },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = if (selectedLoginMethod == "MFA") Color(0xFFE0B4AA) else Color.White, // Light shade when selected
                                    contentColor = if (selectedLoginMethod == "MFA") Color(0xFFDD3825) else Color.Black,
                                ),
                            border = BorderStroke(0.5.dp, Color(0xFFDD3825)),
                        ) {
                            Text(
                                "MFA",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    } else {
                        // For returning users: MFA first
                        Button(
                            onClick = { selectedLoginMethod = "MFA" },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = if (selectedLoginMethod == "MFA") Color(0xFFE0B4AA) else Color.White, // Light shade when selected
                                    contentColor = if (selectedLoginMethod == "MFA") Color(0xFFDD3825) else Color.Black,
                                ),
                            border = BorderStroke(0.5.dp, Color(0xFFDD3825)),
                        ) {
                            Text(
                                "MFA",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }

                        // Show OTP button for returning users if needed
                        if (showOtpButton) {
                            Button(
                                onClick = {
                                    selectedLoginMethod = "OTP"
                                    showOtpFields = true
                                },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(0.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = if (selectedLoginMethod == "OTP") Color(0xFFE0B4AA) else Color.White, // Light shade when selected
                                        contentColor = if (selectedLoginMethod == "OTP") Color(0xFFDD3825) else Color.Black,
                                    ),
                                border = BorderStroke(0.5.dp, Color(0xFFDD3825)),
                            ) {
                                Text(
                                    "OTP",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }

                        // Debug logging for fingerprint button condition
                        Log.d(
                            "LoginScreen",
                            "UI Debug: showBiometricButton=$showBiometricButton, firstTimeLogin=$firstTimeLogin, isDifferentUserMode=$isDifferentUserMode, sessionExpired=$sessionExpired, shouldTreatAsReturningUser=$shouldTreatAsReturningUser",
                        )
                        Log.d("LoginScreen", "Fingerprint condition check: showFingerprint=$showFingerprint")

                        // Show fingerprint button for returning users (including session expired) when biometric is available - third for returning users
                        if (showFingerprint && !isDifferentUserMode) {
                            Button(
                                onClick = {
                                    selectedLoginMethod = "Fingerprint"
                                    showOtpFields = false
                                },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(0.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = if (selectedLoginMethod == "Fingerprint") Color(0xFFE0B4AA) else Color.White,
                                        contentColor = if (selectedLoginMethod == "Fingerprint") Color(0xFFDD3825) else Color.Black,
                                    ),
                                border = BorderStroke(0.5.dp, Color(0xFFDD3825)),
                            ) {
                                Text(
                                    "Fingerprint",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Place this directly below the Row above
                if (!firstTimeLogin && selectedLoginMethod == "OTP" && !showOtpFields) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            when {
                                email.isBlank() -> {
                                    CustomToast.showErrorToast(context, "Email ID is required!")
                                }
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                    CustomToast.showErrorToast(context, "Please enter a valid Email ID!")
                                }
                                mobile.isBlank() -> {
                                    CustomToast.showErrorToast(context, "Mobile number is required!")
                                }
                                mobile.length != 10 -> {
                                    CustomToast.showErrorToast(context, "Mobile number must be 10 digits!")
                                }
                                employeeId.isBlank() -> {
                                    CustomToast.showErrorToast(context, "Employee ID is required!")
                                }
                                else -> {
                                    if (termsAccepted) {
                                        // Skip popup - directly send OTP
                                        isLoading = true
                                        controller.sendOtp(email, mobile, employeeId) { message, isError ->
                                            isLoading = false
                                            if (!isError) {
                                                navigator.navigateToOtpVerification(email, mobile, employeeId, stayLoggedIn)
                                            } else {
                                                errorMessage = message
                                            }
                                        }
                                    } else {
                                        // Show popup for first time
                                        showOtpTermsDialog = true
                                    }
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .height(62.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                disabledContainerColor = Color(0xFFDD3825),
                            ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.login), // <-- Your lock icon
                                contentDescription = "OTP",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with OTP",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                if (selectedLoginMethod == "OTP" && showOtpFields) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email ID") },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .padding(bottom = 16.dp),
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        textStyle =
                            TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                            ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions.Default,
                        shape = MaterialTheme.shapes.medium,
                    )

                    // Mobile Number Field
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                mobile = it
                            }
                        },
                        placeholder = { Text("Mobile No") },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .padding(bottom = 16.dp),
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        textStyle =
                            TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                            ),
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Number,
                            ),
                        keyboardActions = KeyboardActions.Default,
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = if (mobileVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mobileVisible = !mobileVisible }) {
                                Icon(
                                    painter =
                                        painterResource(
                                            id = if (mobileVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off,
                                        ),
                                    contentDescription = if (mobileVisible) "Hide mobile number" else "Show mobile number",
                                    tint = Color.Gray,
                                )
                            }
                        },
                    )

                    // Employee ID Field
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        placeholder = { Text("Employee ID") },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .padding(bottom = 32.dp),
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        textStyle =
                            TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                            ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions.Default,
                        shape = MaterialTheme.shapes.medium,
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.clickable {
                                    if (stayLoggedIn) {
                                        showDisableDialog = true
                                    } else {
                                        stayLoggedIn = true
                                    }
                                },
                        ) {
                            Checkbox(
                                checked = stayLoggedIn,
                                onCheckedChange = { checked ->
                                    if (!checked) {
                                        showDisableDialog = true
                                    } else {
                                        stayLoggedIn = true
                                    }
                                },
                                colors =
                                    CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFDD3825),
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White,
                                    ),
                                modifier =
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f), // Takes available space on the left
                            ) {
                                Text(
                                    "Stay logged in for faster access",
                                    modifier = Modifier.padding(bottom = 0.dp),
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                )
                                Text(
                                    "Your credentials will be securely stored",
                                    modifier = Modifier.padding(top = 0.dp),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dialog for disabling
                    if (showDisableDialog) {
                        Dialog(
                            onDismissRequest = { showDisableDialog = false },
                            properties =
                                DialogProperties(
                                    usePlatformDefaultWidth = false, // removes built-in margins
                                ),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFF6F4EE),
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.94f) // 98% of actual screen width
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(20.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.warning), // Use your document icon
                                        contentDescription = "Document",
                                        tint = Color(0xFFDD3825),
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        "Disable Stay Logged In?",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        "Disabling this option will require you to enter your credentials each time you log in. Are you sure?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp,
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))

                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Button(
                                            onClick = { showDisableDialog = false },
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .height(46.dp),
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor = Color(0x9ADED9D9),
                                                    contentColor = Color.Black,
                                                ),
                                            border = BorderStroke(1.dp, Color.LightGray),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Text(
                                                "Cancel",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color.Black,
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                stayLoggedIn = false
                                                showDisableDialog = false
                                            },
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .height(46.dp),
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFDD3825),
                                                    contentColor = Color.White,
                                                ),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Text(
                                                "Disable",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Login Button
                    Button(
                        onClick = {
                            when {
                                email.isBlank() -> {
                                    CustomToast.showErrorToast(context, "Email ID is required!")
                                }
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                    CustomToast.showErrorToast(context, "Please enter a valid email address")
                                }
                                mobile.isBlank() -> {
                                    CustomToast.showErrorToast(context, "Mobile number is required!")
                                }
                                mobile.length != 10 -> {
                                    CustomToast.showErrorToast(context, "Please enter a  10 digit Mobile Number!")
                                }
                                employeeId.isBlank() -> {
                                    CustomToast.showErrorToast(context, "Employee ID is required!")
                                }
                                else -> {
                                    if (termsAccepted) {
                                        // Skip popup - directly send OTP
                                        isLoading = true
                                        controller.sendOtp(email, mobile, employeeId) { message, isError ->
                                            isLoading = false
                                            if (!isError) {
                                                navigator.navigateToOtpVerification(email, mobile, employeeId, stayLoggedIn)
                                            } else {
                                                errorMessage = message
                                            }
                                        }
                                    } else {
                                        // Show popup for first time
                                        showOtpTermsDialog = true
                                    }
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .height(65.dp)
                                .padding(top = 6.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                disabledContainerColor = Color(0xFFDD3825), // Keep same color when disabled
                            ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.login), // <-- Your lock icon
                                contentDescription = "OTP",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with OTP",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                if (selectedLoginMethod == "MFA") {
                    Spacer(modifier = Modifier.height(10.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.clickable {
                                    if (stayLoggedIn) {
                                        showDisableDialog = true
                                    } else {
                                        stayLoggedIn = true
                                    }
                                },
                        ) {
                            Checkbox(
                                checked = stayLoggedIn,
                                onCheckedChange = { checked ->
                                    if (!checked) {
                                        showDisableDialog = true
                                    } else {
                                        stayLoggedIn = true
                                    }
                                },
                                colors =
                                    CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFDD3825),
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White,
                                    ),
                                modifier =
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f), // Takes available space on the left
                            ) {
                                Text(
                                    "Stay logged in for faster access",
                                    modifier = Modifier.padding(bottom = 0.dp),
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                )
                                Text(
                                    "Your credentials will be securely stored",
                                    modifier = Modifier.padding(top = 0.dp),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (showDisableDialog) {
                        Dialog(
                            onDismissRequest = { showDisableDialog = false },
                            properties =
                                DialogProperties(
                                    usePlatformDefaultWidth = false, // removes built-in margins
                                ),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFF6F4EE),
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.94f) // 98% of actual screen width
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(20.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.warning), // Use your document icon
                                        contentDescription = "Document",
                                        tint = Color(0xFFDD3825),
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        "Disable Stay Logged In?",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        "Disabling this option will require you to enter your credentials each time you log in. Are you sure?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp,
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))

                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Button(
                                            onClick = { showDisableDialog = false },
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .height(46.dp),
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor = Color(0x9ADED9D9),
                                                    contentColor = Color.Black,
                                                ),
                                            border = BorderStroke(1.dp, Color.LightGray),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Text(
                                                "Cancel",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color.Black,
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                stayLoggedIn = false
                                                showDisableDialog = false
                                            },
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .height(46.dp),
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFDD3825),
                                                    contentColor = Color.White,
                                                ),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Text(
                                                "Disable",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (termsAccepted) {
                                startMfaSignIn()
                            } else {
                                showMfaTermsDialog = true // Or handle terms
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .height(62.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                disabledContainerColor = Color(0xFFDD3825), // Keep same color when disabled
                            ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.mfa), // <-- Your MFA icon in drawable
                                contentDescription = "MFA",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with MFA",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Show fingerprint authentication for returning users (including session expired)
                if (selectedLoginMethod == "Fingerprint" && showFingerprint) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                biometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    onSuccess = {
                                        // Get stored biometric credentials with fallback to user data
                                        val credentials = biometricHelper.getStoredCredentialsWithToken()
                                        val bioEmail: String
                                        val bioMobile: String
                                        val bioEmployeeId: String

                                        if (credentials != null) {
                                            bioEmail = credentials.first
                                            bioMobile = credentials.second
                                            bioEmployeeId = credentials.third
                                        } else {
                                            // Enhanced fallback to session expired data if available
                                            bioEmail =
                                                when {
                                                    effectiveUserData?.email?.isNotEmpty() == true -> effectiveUserData.email!!
                                                    sessionExpired ->
                                                        preferencesManager.getString("session_expired_email", "")
                                                            ?: preferencesManager.getString("last_user_email", "")
                                                            ?: ""
                                                    else -> preferencesManager.getString("last_user_email", "") ?: ""
                                                }

                                            bioMobile =
                                                when {
                                                    effectiveUserData?.mobile?.isNotEmpty() == true -> effectiveUserData.mobile!!
                                                    sessionExpired ->
                                                        preferencesManager.getString("session_expired_mobile", "")
                                                            ?: preferencesManager.getString("last_user_mobile", "")
                                                            ?: ""
                                                    else -> preferencesManager.getString("last_user_mobile", "") ?: ""
                                                }

                                            bioEmployeeId =
                                                when {
                                                    effectiveUserData?.employeeId?.isNotEmpty() == true -> effectiveUserData.employeeId!!
                                                    sessionExpired ->
                                                        preferencesManager.getString("session_expired_employee_id", "")
                                                            ?: preferencesManager.getString("last_user_employee_id", "")
                                                            ?: ""
                                                    else -> preferencesManager.getString("last_user_employee_id", "") ?: ""
                                                }

                                            Log.d(
                                                "LoginScreen",
                                                "Biometric fallback credentials: email=$bioEmail, mobile=$bioMobile, employeeId=$bioEmployeeId, sessionExpired=$sessionExpired",
                                            )

                                            if (bioEmail.isBlank() || bioMobile.isBlank() || bioEmployeeId.isBlank()) {
                                                CustomToast.showErrorToast(
                                                    context,
                                                    "Biometric credentials not found. Please login with OTP.",
                                                )
                                                return@showBiometricPrompt
                                            }
                                        }

                                        // Validate credentials
                                        if (bioEmail.isBlank() || bioMobile.isBlank() || bioEmployeeId.isBlank()) {
                                            CustomToast.showErrorToast(context, "User credentials missing. Please use OTP login.")
                                            return@showBiometricPrompt
                                        }

                                        // Call OTP verify with isBiometric = true and empty OTP
                                        val otpController =
                                            OtpVerificationController(
                                                navigator = navigator,
                                                context = context,
                                            )

                                        val deviceInfo = DeviceInfoUtils.getAllDeviceInfo(context)
                                        otpController.verifyOtp(
                                            email = bioEmail,
                                            mobile = bioMobile,
                                            employeeId = bioEmployeeId,
                                            otpFromUser = "", // Empty OTP
                                            isBiometric = true,
                                            backgroundRefresh = false, // Normal login with navigation
                                            appVersion = deviceInfo.appVersion,
                                            deviceModel = deviceInfo.deviceModel,
                                            deviceId = deviceInfo.deviceId,
                                            platform = deviceInfo.platform,
                                            osVersion = deviceInfo.osVersion,
                                            stayLoggedIn = stayLoggedIn,
                                        ) { message, isError ->
                                            if (isError) {
                                                CustomToast.showErrorToast(context, message)
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        // Show error and prevent app bypass by staying on login screen
                                        CustomToast.showErrorToast(context, error)
                                        // Reset login method selection if needed
                                        if (error.contains("cancelled", ignoreCase = true)) {
                                            // User cancelled - they can try again or use another method
                                            Log.d("LoginScreen", "Biometric authentication cancelled by user")
                                        }
                                    },
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth(0.97f)
                                .height(62.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                disabledContainerColor = Color(0xFFDD3825), // Keep same color when disabled
                            ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fingerprint), // <-- Your fingerprint icon in drawable
                                contentDescription = "Fingerprint",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with Fingerprint",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }



                Spacer(modifier = Modifier.height(20.dp))

                if ((!isLoggedIn && hasLoggedIn) || (effectiveUserData != null && !isAutoRefreshing)) {
                    val annotatedText =
                        buildAnnotatedString {
                            withStyle(
                                style =
                                    SpanStyle(
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                    ),
                            ) {
                                append("Not a user?")
                            }
                            append("  ")
                            val start = length
                            append("Log in as different user")
                            addStyle(
                                style =
                                    SpanStyle(
                                        color = Color(0xFFDD3825),
                                        textDecoration = TextDecoration.Underline,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                    ),
                                start = start,
                                end = length,
                            )
                            addStringAnnotation(
                                tag = "login_different_user",
                                annotation = "login_different_user",
                                start = start,
                                end = length,
                            )
                        }

                    var forceUpdate by remember { mutableStateOf(false) }

                    ClickableText(
                        text = annotatedText,
                        onClick = { offset ->
                            annotatedText
                                .getStringAnnotations(tag = "login_different_user", start = offset, end = offset)
                                .firstOrNull()
                                ?.let {
                                    // Clear all login-related state and persistent user data
                                    UserDataManager.getInstance(context).clearUserData()
                                    UserDataManager.getInstance(context).setIsLoggedIn(false)
                                    UserDataManager.getInstance(context).setHasLoggedIn(false)
                                    setFirstTimeLogin(context, true)
                                    MpinManager.clearAllMpinData(context)
                                    BiometricHelper(context).disableBiometric() // Disable biometric

                                    // Clear preserved user data
                                    preferencesManager.setString("last_user_email", "")
                                    preferencesManager.setString("last_user_mobile", "")
                                    preferencesManager.setString("last_user_employee_id", "")
                                    preferencesManager.setString("last_user_name", "")
                                    preferencesManager.setString("session_expired_email", "")
                                    preferencesManager.setString("session_expired_mobile", "")
                                    preferencesManager.setString("session_expired_employee_id", "")
                                    preferencesManager.setString("session_expired_name", "")
                                    preferencesManager.setString("office_ip", "")

                                    // Restart LoginActivity with a clean back stack so HomeActivity
                                    // (and anything else above it) is destroyed. Without this, the
                                    // old user's HomeActivity stays in the task and the launcher
                                    // can resume it when the app is reopened.
                                    val intent = Intent(context, LoginActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putExtra("forceDifferentUserMode", true)
                                        putExtra("clearFields", true)
                                    }
                                    context.startActivity(intent)
                                    (context as? Activity)?.finish()
                                }
                        },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                // Privacy Policy
                Text(
                    text = "Privacy Policy",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier =
                        Modifier
                            .padding(top = 16.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://arche.global/arche-one-privacy-policy"))
                                context.startActivity(intent)
                            },
                    textDecoration = TextDecoration.Underline,
                )

                // Anti-Bribery and Employee Code of Conduct (side by side)
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.97f)
                            .padding(top = 10.dp)
                            .padding(bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Anti-Bribery and Anti- Corruption Policy",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable {
                                    val intent =
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://arche.global/anti-bribery-and-anti-corruption-policy"),
                                        )
                                    context.startActivity(intent)
                                }.padding(end = 8.dp),
                        textDecoration = TextDecoration.Underline,
                        maxLines = 2,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                    // Vertical divider
                    Box(
                        modifier =
                            Modifier
                                .height(30.dp)
                                .width(1.dp)
                                .background(Color.DarkGray),
                    )

                    Text(
                        text = "Employee code of conduct",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://arche.global/employee-code-of-conduct"))
                                    context.startActivity(intent)
                                }.padding(start = 10.dp),
                        textDecoration = TextDecoration.Underline,
                        maxLines = 2,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Policy WebView Dialog removed - now using external browser

            // Reset Password Button at the bottom
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth(0.9f)
                        .height(70.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            navigator.navigateToPasswordReset()
                        },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(
                    modifier =
                        Modifier
                            .padding(vertical = 10.dp, horizontal = 20.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.key),
                        contentDescription = "Reset Password Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reset Password",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                        )
                        Text(
                            text = "For Outlook, and more",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                        )
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                UniversalLoader(isLoading = true)
            }
        }

        if (showOtpTermsDialog || showMfaTermsDialog) {
            Dialog(
                onDismissRequest = {
                    if (showOtpTermsDialog) showOtpTermsDialog = false
                    if (showMfaTermsDialog) showMfaTermsDialog = false
                },
                properties =
                    DialogProperties(
                        usePlatformDefaultWidth = false, // removes built-in margins
                    ),
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF6F4EE),
                    modifier =
                        Modifier
                            .fillMaxWidth(0.94f) // 98% of actual screen width
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.busjust), // Use your document icon
                            contentDescription = "Document",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Terms and condition",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp, max = 400.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color.Gray,
                                        shape = RoundedCornerShape(8.dp),
                                    ).verticalScroll(rememberScrollState())
                                    .padding(12.dp),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .padding(6.dp),
                            ) {
                                Text(
                                    "Welcome to Arche's official application.\n\n" +
                                        "This application is the property of Arche Global Private Limited and is intended solely for authorized use by employees, contractors, or designated users. By accessing or using this application, you agree to the following terms:\n\n",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    lineHeight = 20.sp,
                                )
                                Text(
                                    "✅ Usage Terms\n\n",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    lineHeight = 20.sp,
                                )
                                Text(
                                    "- You acknowledge that this application is owned and managed by Arche Global Private Limited.\n" +
                                        "- You agree to use the application only for purposes permitted by your role and organizational policies.\n" +
                                        "- You agree not to share access credentials or sensitive information with unauthorized individuals.\n" +
                                        "- You consent to the collection and processing of usage data for operational, security, and compliance purposes.\n\n",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    lineHeight = 20.sp,
                                )
                                Text(
                                    "🔐 Privacy & Security\n\n",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    lineHeight = 20.sp,
                                )
                                Text(
                                    "- Your data is protected under applicable data protection laws and internal security protocols.\n" +
                                        "- Unauthorized access, misuse, or tampering with the application may result in disciplinary action or legal consequences.\n\n\n" +
                                        "By tapping \"Accept\", you confirm that you have read, understood, and agreed to abide by these terms and our Privacy Policy.\n",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    if (showOtpTermsDialog) showOtpTermsDialog = false
                                    if (showMfaTermsDialog) showMfaTermsDialog = false
                                    CustomToast.showErrorToast(context, "Please accept the terms and condition")
                                },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0x9ADED9D9),
                                        contentColor = Color.Black,
                                    ),
                                border = BorderStroke(1.dp, Color.LightGray),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    "Cancel",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                )
                            }
                            Button(
                                onClick = {
                                    termsAccepted = true
                                    if (showOtpTermsDialog) {
                                        showOtpTermsDialog = false
                                        isLoading = true
                                        controller.sendOtp(email, mobile, employeeId) { message, isError ->
                                            isLoading = false
                                            if (!isError) {
                                                navigator.navigateToOtpVerification(email, mobile, employeeId, stayLoggedIn)
                                            } else {
                                                errorMessage = message
                                            }
                                        }
                                    }
                                    if (showMfaTermsDialog) {
                                        showMfaTermsDialog = false
                                        startMfaSignIn()
                                    }
                                },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFDD3825),
                                        contentColor = Color.White,
                                    ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    "Accept",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
