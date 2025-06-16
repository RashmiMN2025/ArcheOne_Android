package com.archeGlobal.one.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.ui.preview.PreviewNavigator
import com.archeGlobal.one.ui.components.UniversalLoader
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.archeGlobal.one.R
import com.archeGlobal.one.utils.BiometricHelper
import androidx.fragment.app.FragmentActivity
import com.archeGlobal.one.ui.theme.GraphikFontFamily


@Composable
fun LoginScreen(controller: LoginController, navigator: Navigator) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mobileVisible by remember { mutableStateOf(false) }
    
    val biometricHelper = remember { BiometricHelper(context) }
    val showBiometricButton = remember { biometricHelper.canUseBiometric() && biometricHelper.isBiometricEnabled() }

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
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Company Logo
            CompanyLogo(modifier = Modifier.height(120.dp))

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email ID") },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
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
                    .fillMaxWidth(0.95f)
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
                    .fillMaxWidth(0.95f)
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
                            navigator.navigateToOtpVerification(email, mobile, employeeId)
                        } else {
                            errorMessage = message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(65.dp)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDD3825),
                    disabledContainerColor = Color(0xFFDD3825)  // Keep same color when disabled
                ),
                enabled = !isLoading
            ) {
                Text(
                    "Login",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }

            // OR Divider and Fingerprint only shown if biometric is enabled
            if (showBiometricButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                        color = Color.Gray
                    )
                    Text("OR", color = Color.Gray)
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        color = Color.Gray
                    )
                }

                // Fingerprint Icon Button
                IconButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            biometricHelper.showBiometricPrompt(
                                activity = activity,
                                onSuccess = {
                                    biometricHelper.getStoredCredentials()?.let { (savedEmail, savedMobile, savedEmployeeId) ->
                                        isLoading = true
                                        // Skip OTP verification completely for biometric login
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
                        .size(48.dp)
                        .background(Color(0xFF000000), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fingerprint),
                        contentDescription = "Login with fingerprint",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            UniversalLoader(isLoading = true)
        }
        
        // Reset Password Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable {
                        // Navigate to password reset screen
                        navigator.navigateToPasswordReset()
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 24.dp)
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
        }
    }
}
