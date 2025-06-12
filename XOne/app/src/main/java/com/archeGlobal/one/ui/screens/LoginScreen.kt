package com.archeGlobal.one.ui.screens

import MicrosoftLoginWebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.components.CompanyLogo
import androidx.compose.material3.Text
import com.archeGlobal.one.navigation.Navigator
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.components.UniversalLoader
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.archeGlobal.one.R
import com.archeGlobal.one.utils.BiometricHelper
import androidx.fragment.app.FragmentActivity
import com.archeGlobal.one.model.AuthResponse
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.isFirstTimeLogin
import com.archeGlobal.one.utils.setFirstTimeLogin
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoginScreen(controller: LoginController, navigator: Navigator) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mobileVisible by remember { mutableStateOf(false) }
    var firstTimeLogin by remember { mutableStateOf(isFirstTimeLogin(context)) }
    var showWebView by remember { mutableStateOf(false) }
    var authResponse by remember { mutableStateOf<AuthResponse?>(null) }
    var showOtpFields by remember { mutableStateOf(firstTimeLogin) }
    var selectedLoginMethod by remember { mutableStateOf("OTP") } // Track which button is selected

    val lastEmployeeName = UserDataManager.getInstance(context).getLastUsername()
    val isLoggedIn = UserDataManager.getInstance(context).isLoggedIn() 
    val hasLoggedIn = UserDataManager.getInstance(context).hasUserLoggedIn() 
    val biometricHelper = remember { BiometricHelper(context) }
    val showBiometricButton = remember { biometricHelper.canUseBiometric() && biometricHelper.isBiometricEnabled() }
    var showFingerprint by remember { mutableStateOf(showBiometricButton && !firstTimeLogin) }

    // Show Toast message for errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
            .padding(bottom = 32.dp), // Increased from 10.dp to 32.dp
            verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            // Company Logo
            CompanyLogo(modifier = Modifier.height(120.dp))

            Spacer(modifier = Modifier.height(20.dp))

            // ...inside your Column after the logo and Spacer...

            if (!lastEmployeeName.isNullOrEmpty()) {
                Text(
                    text = "Welcome, $lastEmployeeName",
                    fontSize = 28.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = "Log in with",
                fontSize = 18.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
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
                Button(
                    onClick = { selectedLoginMethod = "OTP"; showOtpFields = firstTimeLogin },
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

                Button(
                    onClick = { selectedLoginMethod = "MFA"; showOtpFields = false },
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

                if (showFingerprint && !firstTimeLogin && showBiometricButton) {
                    // Show fingerprint button if biometric is available and not first time login
                    if (showBiometricButton && !firstTimeLogin) {
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Place this directly below the Row above
            if (!firstTimeLogin && selectedLoginMethod == "OTP" && !showOtpFields) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showOtpFields = true },
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
                    onValueChange = { mobile = it },
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
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
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

                // Login Button
                Button(
                    onClick = {
                        isLoading = true
                        controller.sendOtp(email, mobile, employeeId) { message, isError ->
                            isLoading = false
                            if (!isError) {
                                setFirstTimeLogin(context, false)
                                firstTimeLogin = false
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
                        disabledContainerColor = Color(0xFFDD3825)  // Keep same color when disabled
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
                        showWebView = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.97f)
                        .height(62.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825),
                        disabledContainerColor = Color(0xFFDD3825)  // Keep same color when disabled
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

            // Biometric login button (only if selected)
            if (selectedLoginMethod == "Fingerprint" && showBiometricButton && !firstTimeLogin) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            biometricHelper.showBiometricPrompt(
                                activity = activity,
                                onSuccess = {
                                    biometricHelper.getStoredCredentials()?.let { (savedEmail, savedMobile, savedEmployeeId) ->
                                        isLoading = true
                                        val otpController = OtpVerificationController(navigator, context)
                                        otpController.verifyWithBiometric(savedEmail, savedMobile, savedEmployeeId) { message: String, isError: Boolean ->
                                            isLoading = false
                                            if (isError) {
                                                errorMessage = message
                                            }
                                        }
                                    }
                                },
                                onError = { error ->
                                    errorMessage = error
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.97f)
                        .height(62.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825),
                        disabledContainerColor = Color(0xFFDD3825)  // Keep same color when disabled
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
                            url = "https://login.microsoftonline.com/3865b44b-651f-4df8-a0c8-2625494f6198/oauth2/v2.0/authorize?client_id=b4cdff13-7b2f-4237-86bb-76cd7e6e3dcd&response_type=code&redirect_uri=https%3A%2F%2Fpulse.netcon.in%3A7000%2FmfaCallback&scope=openid%20profile%20User.Read&response_mode=query&prompt=login",
                            onReceiveAuth = { response ->
                                authResponse = response
                                isLoading = true
                                controller.loginWithToken(
                                    token = response.token,
                                    email = response.email,
                                    mobile = response.mobilePhone,
                                    employeeId = response.employeeId
                                ) {
                                message, isError ->
                                    isLoading = false
                                    if (!isError) {
                                        UserDataManager.getInstance(context).setHasLoggedIn(true) // <-- Place here
                                        setFirstTimeLogin(context, false)
                                        firstTimeLogin = false
                                        val biometricHelper = BiometricHelper(context)
                                        val canUse = biometricHelper.canUseBiometric()
                                        val isEnabled = biometricHelper.isBiometricEnabled()
                                        if (canUse && !isEnabled) {
                                            navigator.navigateToHome(false, true, response.email, response.mobilePhone, response.employeeId)
                                        } else {
                                            navigator.navigateToHome(false)
                                        }
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
                            fontWeight = FontWeight.Medium,
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
                                firstTimeLogin = true
                                showOtpFields = true
                                selectedLoginMethod = "OTP"
                                showFingerprint = false
                                email = ""
                                mobile = ""
                                employeeId = ""
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
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { /* Handle Privacy Policy click */ },
                textDecoration = TextDecoration.Underline
            )

            // Anti-Bribery and Employee Code of Conduct (side by side)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.97f)
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anti-Bribery and Anti- Corruption Policy",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* Handle Anti-Bribery click */ }
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
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* Handle Code of Conduct click */ }
                        .padding(start = 10.dp),
                    textDecoration = TextDecoration.Underline,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
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
}
