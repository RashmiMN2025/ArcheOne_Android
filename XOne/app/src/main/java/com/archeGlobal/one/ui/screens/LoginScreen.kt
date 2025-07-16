package com.archeGlobal.one.ui.screens

import MicrosoftLoginWebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.model.AuthResponse
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.components.CompanyLogo
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.BiometricHelper
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.isFirstTimeLogin
import com.archeGlobal.one.utils.setFirstTimeLogin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveLoginScreen(
    controller: LoginController,
    navigator: Navigator,
    forceOriginalLogin: Boolean = false,
    forceDifferentUserMode: Boolean = false
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val contentPadding = when (windowSizeClass?.widthSizeClass) {
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
        contentPadding = contentPadding
    )
}

@Composable
fun LoginScreen(
    controller: LoginController,
    navigator: Navigator,
    forceOriginalLogin: Boolean = false,
    forceDifferentUserMode: Boolean = false,
    contentPadding: Dp = 16.dp // <-- Add this parameter
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mobileVisible by remember { mutableStateOf(false) }
    var firstTimeLogin by remember { mutableStateOf(forceOriginalLogin || isFirstTimeLogin(context)) }
    
    // Update firstTimeLogin when the screen is recreated after logout
    LaunchedEffect(Unit) {
        firstTimeLogin = forceOriginalLogin || isFirstTimeLogin(context)
        android.util.Log.d("LoginScreen", "Screen Created: firstTimeLogin=$firstTimeLogin, forceOriginalLogin=$forceOriginalLogin, isFirstTimeLogin=${isFirstTimeLogin(context)}")
    }
    var showWebView by remember { mutableStateOf(false) }
    var authResponse by remember { mutableStateOf<AuthResponse?>(null) }
    var showMfaTermsDialog by remember { mutableStateOf(false) }

    val lastEmployeeName = UserDataManager.getInstance(context).getLastUsername()
    val isLoggedIn = UserDataManager.getInstance(context).isLoggedIn()
    val hasLoggedIn = UserDataManager.getInstance(context).hasUserLoggedIn()
    val biometricHelper = remember { BiometricHelper(context) }
    var isDifferentUserMode by remember { mutableStateOf(forceDifferentUserMode) }
    
    // Make biometric button state reactive - don't use remember so it re-evaluates
    val showBiometricButton = biometricHelper.canUseBiometric() && biometricHelper.isBiometricEnabled()
    var showFingerprint by remember { mutableStateOf(false) }
    
    // Update showFingerprint when relevant conditions change
    LaunchedEffect(firstTimeLogin, showBiometricButton, forceDifferentUserMode) {
        showFingerprint = showBiometricButton && !firstTimeLogin
        // Reset isDifferentUserMode after normal logout (when it's not forced)
        if (!forceDifferentUserMode && !firstTimeLogin) {
            isDifferentUserMode = false
        }
        
        // Debug logging
        android.util.Log.d("LoginScreen", "Biometric Debug: showBiometricButton=$showBiometricButton, firstTimeLogin=$firstTimeLogin, isDifferentUserMode=$isDifferentUserMode")
        android.util.Log.d("LoginScreen", "Biometric Debug: canUseBiometric=${biometricHelper.canUseBiometric()}, isBiometricEnabled=${biometricHelper.isBiometricEnabled()}")
        android.util.Log.d("LoginScreen", "Biometric Debug: showFingerprint=$showFingerprint")
    }

    var showPolicyWebView by remember { mutableStateOf(false) }
    var policyUrl by remember { mutableStateOf("") }
    var policyTitle by remember { mutableStateOf("") }

    val mpinController = remember { com.archeGlobal.one.controller.MpinController(context) }
    // Make hasMpin reactive to changes - don't use remember so it re-evaluates
    val hasMpin = mpinController.isMpinSet()
    var selectedLoginMethod by remember { mutableStateOf("OTP") }
    var showOtpFields by remember { mutableStateOf(forceOriginalLogin || firstTimeLogin) }
    var enteredMpin by remember { mutableStateOf("") }
    var mpinError by remember { mutableStateOf<String?>(null) }
    val focusRequesters = List(4) { remember { androidx.compose.ui.focus.FocusRequester() } }
    var focusedIndex by remember { mutableStateOf(-1) }
    var isVerifyingMpin by remember { mutableStateOf(false) }

    var showOtpButton by remember { mutableStateOf(forceOriginalLogin || firstTimeLogin) }

    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    var enteredMpinDigits by remember { mutableStateOf(List(4) { "" }) }

    val userData = UserDataManager.getInstance(context).getUserData()
    LaunchedEffect(Unit) {
        if (userData != null) {
            if (email.isEmpty()) email = userData.email ?: ""
            if (mobile.isEmpty()) mobile = userData.mobile ?: ""
            if (employeeId.isEmpty()) employeeId = userData.employeeId ?: ""
        }
    }

    // Re-evaluate the login method whenever firstTimeLogin or hasMpin changes
    LaunchedEffect(firstTimeLogin, hasMpin, isDifferentUserMode, showBiometricButton) {
        // If forceOriginalLogin is true, always show original login form
        if (forceOriginalLogin) {
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
            } else if (hasMpin) {
                // For existing users, default to MPIN if available
                selectedLoginMethod = "MPIN"
                showOtpFields = false
            } else if (showBiometricButton) {
                // If biometric is available, default to Fingerprint
                selectedLoginMethod = "Fingerprint"
                showOtpFields = false
            } else {
                // Default to MFA for existing users without MPIN or biometric
                selectedLoginMethod = "MFA"
                showOtpFields = false
            }
        }
    }

    // Show Toast message for errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749) // Dark Gray
                        )
                    )
                )
                .padding(bottom = 32.dp, start = contentPadding, end = contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "Log in with",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ...inside your Column after the "Log in with" Text...

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.97f)
                        .height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isDifferentUserMode && hasMpin && !firstTimeLogin) {
                        Button(
                            onClick = { selectedLoginMethod = "MPIN" },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLoginMethod == "MPIN") Color(0xFFE0B4AA) else Color.White,
                                contentColor = if (selectedLoginMethod == "MPIN") Color(0xFFDD3825) else Color.Black
                            ),
                            border = BorderStroke(0.5.dp, Color(0xFFDD3825))
                        ) {
                            Text(
                                "MPIN",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (showOtpButton) {
                        Button(
                            onClick = { selectedLoginMethod = "OTP"; showOtpFields = true },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLoginMethod == "OTP") Color(0xFFE0B4AA) else Color.White, // Light shade when selected
                                contentColor = if (selectedLoginMethod == "OTP") Color(0xFFDD3825) else Color.Black
                            ),
                            border = BorderStroke(0.5.dp, Color(0xFFDD3825))
                        ) {
                            Text(
                                "OTP",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Button(
                        onClick = { selectedLoginMethod = "MFA" },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedLoginMethod == "MFA") Color(0xFFE0B4AA) else Color.White, // Light shade when selected
                            contentColor = if (selectedLoginMethod == "MFA") Color(0xFFDD3825) else Color.Black
                        ),
                        border = BorderStroke(0.5.dp, Color(0xFFDD3825))
                    ) {
                        Text(
                            "MFA",
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Debug logging for fingerprint button condition
                    android.util.Log.d("LoginScreen", "UI Debug: showBiometricButton=$showBiometricButton, firstTimeLogin=$firstTimeLogin, isDifferentUserMode=$isDifferentUserMode")
                    
                    if (showBiometricButton && !firstTimeLogin && !isDifferentUserMode) {
                        Button(
                            onClick = { selectedLoginMethod = "Fingerprint"; showOtpFields = false },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLoginMethod == "Fingerprint") Color(0xFFE0B4AA) else Color.White,
                                contentColor = if (selectedLoginMethod == "Fingerprint") Color(0xFFDD3825) else Color.Black
                            ),
                            border = BorderStroke(0.5.dp, Color(0xFFDD3825))
                        ) {
                            Text(
                                "Fingerprint",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Place this directly below the Row above
                if (!firstTimeLogin && selectedLoginMethod == "OTP" && !showOtpFields) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            showOtpFields = true
                            if (!termsAccepted) {
                                Toast.makeText(context, "Please accept the terms and condition", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            controller.sendOtp(email, mobile, employeeId) { message, isError ->
                                isLoading = false
                                if (!isError) {
                                    // Don't set firstTimeLogin to false here - user hasn't logged in yet
                                    navigator.navigateToOtpVerification(email, mobile, employeeId)
                                } else {
                                    errorMessage = message
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(62.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock), // <-- Your lock icon
                                contentDescription = "OTP",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with OTP",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
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
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .padding(bottom = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions.Default,
                        shape = MaterialTheme.shapes.medium
                    )

                    // Mobile Number Field
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                mobile = it
                            }
                        },
                        placeholder = { Text("Mobile No") },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .padding(bottom = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions.Default,
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = if (mobileVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mobileVisible = !mobileVisible }) {
                                Icon(
                                    painter = painterResource(id = if (mobileVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                                    contentDescription = if (mobileVisible) "Hide mobile number" else "Show mobile number",
                                    tint = Color.Gray
                                )
                            }
                        }
                    )

                    // Employee ID Field
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        placeholder = { Text("Employee ID") },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .padding(bottom = 32.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions.Default,
                        shape = MaterialTheme.shapes.medium
                    )

                    // Entire row is clickable to show dialog or uncheck if already checked
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp, 0.dp, 8.dp)
                            .clickable {
                                if (termsAccepted) {
                                    // If already checked, allow unchecking directly
                                    termsAccepted = false
                                } else {
                                    // If not checked, show dialog to read terms first
                                    showTermsDialog = true
                                }
                            }
                    ) {
                        // Checkbox with no onCheckedChange - handled by row click
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = null, // Disable default behavior
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFDD3825),
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // Text with no clickable - handled by row click
                        Text(
                            text = "I agree to the terms and condition",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Login Button
                    Button(
                        onClick = {
                            if (!termsAccepted) {
                                Toast.makeText(context, "Please accept the terms and condition", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            controller.sendOtp(email, mobile, employeeId) { message, isError ->
                                isLoading = false
                                if (!isError) {
                                    // Don't set firstTimeLogin to false here - user hasn't logged in yet
                                    navigator.navigateToOtpVerification(email, mobile, employeeId)
                                } else {
                                    errorMessage = message
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp)
                            .padding(top = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825) // Keep same color when disabled
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock), // <-- Your lock icon
                                contentDescription = "OTP",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with OTP",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (selectedLoginMethod == "MFA") {
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showMfaTermsDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(62.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825) // Keep same color when disabled
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.mfa), // <-- Your MFA icon in drawable
                                contentDescription = "MFA",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with MFA",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (selectedLoginMethod == "Fingerprint" && showBiometricButton && !firstTimeLogin) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                biometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    onSuccess = {
                                        // Get credentials from PreferencesManager
                                        val prefs = com.archeGlobal.one.utils.PreferencesManager(context)
                                        val email = prefs.getString("biometric_email", "") ?: ""
                                        val mobile = prefs.getString("biometric_mobile", "") ?: ""
                                        val employeeId = prefs.getString("biometric_employee_id", "") ?: ""

                                        if (email.isBlank() || mobile.isBlank() || employeeId.isBlank()) {
                                            Toast.makeText(context, "Biometric credentials not found. Please login with MPIN or OTP.", Toast.LENGTH_SHORT).show()
                                            return@showBiometricPrompt
                                        }

                                        // Call OTP verify with isBiometric = true and empty OTP
                                        val otpController = com.archeGlobal.one.controller.OtpVerificationController(
                                            navigator = navigator,
                                            context = context
                                        )
                                        otpController.verifyOtp(
                                            email = email,
                                            mobile = mobile,
                                            employeeId = employeeId,
                                            otpFromUser = "", // Empty OTP
                                            isBiometric = true
                                        ) { message, isError ->
                                            if (isError) {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(62.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825) // Keep same color when disabled
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fingerprint), // <-- Your fingerprint icon in drawable
                                contentDescription = "Fingerprint",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Login with Fingerprint",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (selectedLoginMethod == "MPIN" && hasMpin) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Enter MPIN",
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 4) {
                            OutlinedTextField(
                                value = enteredMpinDigits[i],
                                onValueChange = { value ->
                                    if (value.length <= 1 && value.all { it.isDigit() }) {
                                        enteredMpinDigits = enteredMpinDigits.toMutableList().also { it[i] = value }
                                        if (value.isNotEmpty() && i < 3) {
                                            focusRequesters[i + 1].requestFocus()
                                        }
                                    }
                                    if (value.isEmpty() && i > 0) {
                                        enteredMpinDigits = enteredMpinDigits.toMutableList().also { it[i] = "" }
                                        focusRequesters[i - 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .width(65.dp)
                                    .height(65.dp)
                                    .focusRequester(focusRequesters[i])
                                    .padding(horizontal = 4.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) focusedIndex = i
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
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = if (i == 3) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
                                ),
                                enabled = !isVerifyingMpin,
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
                                visualTransformation = PasswordVisualTransformation()
                            )
                            if (i < 3) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isVerifyingMpin = true
                            mpinError = null
                            val enteredMpin = enteredMpinDigits.joinToString("")
                            val mpinController = com.archeGlobal.one.controller.MpinController(context)
                            if (!mpinController.validateMpin(enteredMpin)) {
                                mpinError = "Invalid MPIN"
                                isVerifyingMpin = false
                                return@Button
                            }
                            // Call OTP verify with isBiometric = true and empty OTP
                            val otpController = com.archeGlobal.one.controller.OtpVerificationController(
                                navigator = navigator,
                                context = context
                            )
                            otpController.verifyOtp(
                                email = email,
                                mobile = mobile,
                                employeeId = employeeId,
                                otpFromUser = "", // Empty OTP
                                isBiometric = true
                            ) { message, isError ->
                                isVerifyingMpin = false
                                if (isError) {
                                    mpinError = message
                                } else {
                                    // On success, get the new token and call loginWithToken
                                    val token = com.archeGlobal.one.utils.UserDataManager.getInstance(context).getAuthToken() ?: ""
                                    otpController.loginWithToken(
                                        token = token,
                                        email = email,
                                        mobile = mobile,
                                        employeeId = employeeId,
                                        fromHome = false,
                                        fromOtp = false,
                                        shouldNavigateToHome = true
                                    ) { loginMsg, loginError ->
                                        if (loginError) {
                                            mpinError = loginMsg
                                        } else {
                                            // Success: Home navigation handled in controller
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isVerifyingMpin
                    ) {
                        Icon(
                            painter = painterResource(id = com.archeGlobal.one.R.drawable.ic_lock),
                            contentDescription = "Lock",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Login with MPIN",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (mpinError != null) {
                        Text(
                            text = mpinError ?: "",
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Reset MPIN
                    ClickableText(
                        text = buildAnnotatedString {
                            append("Reset MPIN")
                            addStyle(
                                style = androidx.compose.ui.text.SpanStyle(
                                    color = Color(0xFFDD3825),
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Normal
                                ),
                                start = 0,
                                end = "Reset MPIN".length
                            )
                            addStringAnnotation(
                                tag = "reset_mpin",
                                annotation = "reset_mpin",
                                start = 0,
                                end = "Reset MPIN".length
                            )
                        },
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFDD3825)
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { offset ->
                            // Navigate to MPIN reset screen
                            val intent = android.content.Intent(context, com.archeGlobal.one.ui.screens.MpinActivity::class.java)
                            intent.putExtra("resetMpin", true)
                            context.startActivity(intent)
                        }
                    )
                }

                if (showWebView) {
                    Dialog(
                        onDismissRequest = { showWebView = false },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            usePlatformDefaultWidth = false // Fullscreen
                        )
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight() // Almost full screen, adjust as needed
                        ) {
                            MicrosoftLoginWebView(
                                url = "https://login.microsoftonline.com/3865b44b-651f-4df8-a0c8-2625494f6198/oauth2/v2.0/authorize?client_id=b4cdff13-7b2f-4237-86bb-76cd7e6e3dcd&response_type=code&redirect_uri=https%3A%2F%2Farcheone.arche.global%2FmfaCallback&scope=openid%20profile%20User.Read&response_mode=query&prompt=login",
                                onReceiveAuth = { response ->
                                    android.util.Log.d("LoginScreen", "MFA onReceiveAuth called with token: ${response.token}")
                                    android.util.Log.d("LoginScreen", "Email: ${response.email}, EmployeeId: ${response.employeeId}")
                                    authResponse = response
                                    isLoading = true
                                    controller.loginWithToken(
                                        token = response.token,
                                        email = response.email,
                                        mobile = response.mobilePhone,
                                        employeeId = response.employeeId
                                    ) { message, isError ->
                                        android.util.Log.d("LoginScreen", "loginWithToken callback: message=$message, isError=$isError")
                                        isLoading = false
                                        if (!isError) {
                                            UserDataManager.getInstance(context).setHasLoggedIn(true)
                                            setFirstTimeLogin(context, false)
                                            firstTimeLogin = false
                                            // Navigation is now handled by LoginController.loginWithToken()
                                        }
                                    }
                                },
                                onClose = {
                                    showWebView = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isLoggedIn && hasLoggedIn) {
                    val annotatedText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        ) {
                            append("Not a user?")
                        }
                        append("  ")
                        val start = length
                        append("Log in as different user")
                        addStyle(
                            style = SpanStyle(
                                color = Color(0xFFDD3825),
                                textDecoration = TextDecoration.Underline,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            ),
                            start = start,
                            end = length
                        )
                        addStringAnnotation(
                            tag = "login_different_user",
                            annotation = "login_different_user",
                            start = start,
                            end = length
                        )
                    }

                    var forceUpdate by remember { mutableStateOf(false) }

                    ClickableText(
                        text = annotatedText,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "login_different_user", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    // Clear all login-related state and persistent user data
                                    UserDataManager.getInstance(context).clearUserData()
                                    UserDataManager.getInstance(context).setIsLoggedIn(false)
                                    UserDataManager.getInstance(context).setHasLoggedIn(false)
                                    setFirstTimeLogin(context, true)
                                    com.archeGlobal.one.utils.MpinManager.clearAllMpinData(context)
                                    BiometricHelper(context).disableBiometric() // Disable biometric

                                    // Reset all UI state
                                    firstTimeLogin = true
                                    showOtpButton = true
                                    isDifferentUserMode = true
                                    selectedLoginMethod = "OTP"
                                    showOtpFields = true
                                    showFingerprint = false
                                    email = ""
                                    mobile = ""
                                    employeeId = ""
                                    enteredMpin = ""
                                    mpinError = null
                                    showMfaTermsDialog = false
                                    termsAccepted = false

                                    // Force a UI refresh
                                    forceUpdate = !forceUpdate
                                }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Privacy Policy
                Text(
                    text = "Privacy Policy",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable {
                            policyUrl = "https://arche.global/arche-one-privacy-policy"
                            policyTitle = "Privacy Policy"
                            showPolicyWebView = true
                        },
                    textDecoration = TextDecoration.Underline
                )

                // Anti-Bribery and Employee Code of Conduct (side by side)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.97f)
                        .padding(top = 10.dp)
                        .padding(bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anti-Bribery and Anti- Corruption Policy",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                policyUrl = "https://arche.global/anti-bribery-and-anti-corruption-policy"
                                policyTitle = "Anti-Bribery Policy"
                                showPolicyWebView = true
                            }
                            .padding(end = 8.dp),
                        textDecoration = TextDecoration.Underline,
                        maxLines = 2,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(Color.DarkGray)
                    )

                    Text(
                        text = "Employee code of conduct",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                policyUrl = "https://arche.global/employee-code-of-conduct"
                                policyTitle = "Employee Code of Conduct"
                                showPolicyWebView = true
                            }
                            .padding(start = 10.dp),
                        textDecoration = TextDecoration.Underline,
                        maxLines = 2,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Policy WebView Dialog
            if (showPolicyWebView) {
                Dialog(
                    onDismissRequest = { showPolicyWebView = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        MicrosoftLoginWebView(
                            url = policyUrl,
                            onReceiveAuth = { /* Not needed for policy pages */ },
                            onClose = { showPolicyWebView = false }
                        )
                    }
                }
            }

            // Reset Password Button at the bottom
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(70.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        navigator.navigateToPasswordReset()
                    },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_key),
                        contentDescription = "Reset Password Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reset Password",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily
                        )
                        Text(
                            text = "For Outlook, and more",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily
                        )
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                UniversalLoader(isLoading = true)
            }
        }

        if (showTermsDialog || showMfaTermsDialog) {
            Dialog(
                onDismissRequest = {
                    if (showTermsDialog) showTermsDialog = false
                    if (showMfaTermsDialog) showMfaTermsDialog = false
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth(1f) // Increase width to 98% of the screen
                        .padding(horizontal = 0.dp, vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_policy_default), // Use your document icon
                            contentDescription = "Document",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Terms and condition",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .heightIn(min = 120.dp, max = 260.dp)
                                .verticalScroll(rememberScrollState())
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    "Welcome to Arche's official application.\n\n" +
                                        "This application is the property of Arche Global Private Limited and is intended solely for authorized use by employees, contractors, or designated users. By accessing or using this application, you agree to the following terms:\n\n",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    "✅ Usage Terms\n\n",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    lineHeight = 18.sp
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
                                    lineHeight = 18.sp
                                )
                                Text(
                                    "🔐 Privacy & Security\n\n",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    "- Your data is protected under applicable data protection laws and internal security protocols.\n" +
                                        "- Unauthorized access, misuse, or tampering with the application may result in disciplinary action or legal consequences.\n\n\n" +
                                        "By tapping \"Accept\", you confirm that you have read, understood, and agreed to abide by these terms and our Privacy Policy.\n",
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (showTermsDialog) showTermsDialog = false
                                    if (showMfaTermsDialog) showMfaTermsDialog = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.LightGray,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Cancel",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            }
                            Button(
                                onClick = {
                                    termsAccepted = true
                                    if (showTermsDialog) {
                                        showTermsDialog = false
                                    }
                                    if (showMfaTermsDialog) {
                                        showMfaTermsDialog = false
                                        showWebView = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Accept",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
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
