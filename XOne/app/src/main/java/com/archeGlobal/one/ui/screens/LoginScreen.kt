package com.archeGlobal.one.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.MSALAuthenticationController
import com.archeGlobal.one.model.AuthResponse
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.components.CompanyLogo
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.CustomToast
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
    var showMfaTermsDialog by remember { mutableStateOf(false) }
    // Inside your LoginScreen composable:
    var stayLoggedIn by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }

    // Get session expired status from intent
    val currentActivity = context as? Activity
    val sessionExpired = currentActivity?.intent?.getBooleanExtra("session_expired", false) ?: false

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

    // Removed policy WebView state variables - now using external browser

    var termsAccepted by remember { mutableStateOf(false) }
    var showMsalConsentDialog by remember { mutableStateOf(false) }

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

        val currentActivity = context as? Activity
        if (currentActivity == null) {
            errorMessage = "Unable to start authentication"
            return
        }
        val authController = msalAuthenticationController
        if (authController == null) {
            errorMessage = "Authentication controller unavailable"
            return
        }

        isLoading = true
        errorMessage = null

        authController.performSignIn(currentActivity) { success, msg ->
            isLoading = false
            if (success) {
                if (authController.isAuthenticated.value) {
                    val email = authController.userEmail.value
                    val storedMobile = preferencesManager.getString("last_user_mobile", "") ?: ""
                    val storedEmployeeId = preferencesManager.getString("last_user_employee_id", "") ?: ""
                    navigator.navigateToHome(true, false, email, storedMobile, storedEmployeeId)
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
            if (email.isEmpty()) email = effectiveUserData.email
            if (mobile.isEmpty()) mobile = effectiveUserData.mobile
            if (employeeId.isEmpty()) employeeId = effectiveUserData.employeeId
        }
    }

    // Auto-refresh token using stored credentials is disabled because OTP login has been removed.
    var isAutoRefreshing by remember { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.height(70.dp))

                CompanyLogo(modifier = Modifier.height(130.dp))

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Welcome to Arche One",
                    color = Color.Black,
                    fontSize = 30.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Sign in with your Microsoft account",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (termsAccepted) {
                            startMfaSignIn()
                        } else {
                            showMfaTermsDialog = true
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth(0.97f)
                            .height(64.dp),
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
                            painter = painterResource(id = R.drawable.lock),
                            contentDescription = "MFA",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Sign in with Microsoft",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "MFA",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Secured by Microsoft Azure AD",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

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

        if (showMfaTermsDialog) {
            Dialog(
                onDismissRequest = {
                    showMfaTermsDialog = false
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
                                    showMfaTermsDialog = false
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
                                    showMfaTermsDialog = false
                                    showMsalConsentDialog = true
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

        if (showMsalConsentDialog) {
            Dialog(
                onDismissRequest = { showMsalConsentDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF6F4EE),
                    modifier =
                        Modifier
                            .fillMaxWidth(0.94f)
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = " \"ArcheOne\" wants to use \"microsoftonline.com\" to sign in.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This allows the app and website to share information about you.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = { showMsalConsentDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(
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
                                    showMsalConsentDialog = false
                                    startMfaSignIn()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                    contentColor = Color.White,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    "Continue",
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
