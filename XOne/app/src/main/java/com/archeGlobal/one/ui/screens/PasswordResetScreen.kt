package com.archeGlobal.one.ui.screens

import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.PasswordResetController
import com.archeGlobal.one.model.PasswordResetResponse
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.components.CompanyLogo
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun PasswordResetScreen(navigator: Navigator) {
    val context = LocalContext.current
    val controller = remember { PasswordResetController(navigator, context) }

    var email by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordResetResponse by remember { mutableStateOf<PasswordResetResponse?>(null) }
    var showPasswordBox by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(120) } // 2 minutes in seconds

    // Set up countdown timer when password is shown
    val countDownTimer =
        remember(passwordResetResponse) {
            object : CountDownTimer(120000, 1000) { // 2 minutes, update every second
                override fun onTick(millisUntilFinished: Long) {
                    timeRemaining = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    showPasswordBox = false
                    passwordResetResponse = null
                }
            }
        }

    // Show password box and start timer when we get a successful response
    LaunchedEffect(passwordResetResponse) {
        if (passwordResetResponse != null && passwordResetResponse?.status == 200) {
            showPasswordBox = true
            timeRemaining = 120
            countDownTimer.start()
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
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFFE0DCD1), // Light Beige
                                    Color(0xFFC8C8CA), // Light Gray
                                    Color(0xFF474749), // Dark Gray
                                ),
                        ),
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    modifier =
                        Modifier
                            .fillMaxWidth(0.95f)
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

                // Employee ID Field
                OutlinedTextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    placeholder = { Text("Employee ID") },
                    modifier =
                        Modifier
                            .fillMaxWidth(0.95f)
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

                // Reset Password Button
                Button(
                    onClick = {
                        if (email.isBlank() || employeeId.isBlank()) {
                            errorMessage = "Please fill all the fields"
                            return@Button
                        }

                        isLoading = true
                        controller.resetPassword(email, employeeId) { response, error ->
                            isLoading = false
                            if (error != null) {
                                errorMessage = error
                                Log.e("PasswordResetScreen", "Error: $error")
                            } else if (response != null) {
                                if (response.status == 200) {
                                    passwordResetResponse = response
                                    // Successful reset - show temporary password
                                } else {
                                    // Error status
                                    errorMessage = response.message ?: "Password reset failed"
                                }
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp)
                            .padding(top = 10.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825), // Keep same color when disabled
                        ),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading,
                ) {
                    Text(
                        "Reset Password",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                val goBackText =
                    buildAnnotatedString {
                        val start = length
                        append("Go Back")
                        addStyle(
                            style =
                                SpanStyle(
                                    color = Color.Black,
                                    textDecoration = TextDecoration.Underline,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                ),
                            start = start,
                            end = length,
                        )
                        addStringAnnotation(
                            tag = "go_back",
                            annotation = "go_back",
                            start = start,
                            end = length,
                        )
                    }
                ClickableText(
                    text = goBackText,
                    style =
                        TextStyle(
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                        ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { offset ->
                        goBackText
                            .getStringAnnotations(
                                tag = "go_back",
                                start = offset,
                                end = offset,
                            ).firstOrNull()
                            ?.let {
                                val activity = context as? android.app.Activity
                                activity?.finish()
                            }
                    },
                )
            }

            // Temporary Password Display Box
            AnimatedVisibility(
                visible = showPasswordBox,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 450.dp), // Position further down below the reset button
            ) {
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color.White,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Hi ${
                                passwordResetResponse
                                    ?.name
                                    ?.split(' ')
                                    ?.firstOrNull() ?: "WebTestUser"
                            }",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GraphikFontFamily,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "This is your temporary password",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Display
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp),
                                    ),
                            color = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = passwordResetResponse?.newPassword ?: "T*S8Vhc1pM",
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Timer
                        Text(
                            text = "Time remaining: ${
                                String.format(
                                    "%02d:%02d",
                                    timeRemaining / 60,
                                    timeRemaining % 60,
                                )
                            }",
                            fontSize = 13.sp,
                            fontFamily = GraphikFontFamily,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { timeRemaining / 120f },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                            color = Color(0xFFDD3825), // Red color
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Info message
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color(0xFFDD3825),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You can use the temporary password to reset your password in Outlook and other services.",
                                fontSize = 11.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                lineHeight = 14.sp, // Reduce the line spacing
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                UniversalLoader(isLoading = true)
            }
        }
    }

    // Cleanup timer when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            countDownTimer.cancel()
        }
    }
}
