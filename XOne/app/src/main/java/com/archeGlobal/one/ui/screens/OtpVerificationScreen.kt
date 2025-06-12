package com.archeGlobal.one.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.components.CompanyLogo
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@SuppressLint("DefaultLocale")
@Composable
fun OtpVerificationScreen(controller: OtpVerificationController, email: String, mobile: String, employeeId: String) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(60) }
    var timerStarted by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Timer logic: Decrease `timeLeft` every second
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                timeLeft -= 1
            }
        }
    }


    // Format time into MM:SS
    val formattedTime = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)

    // Show Toast message for verification feedback
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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

            // OTP Field
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                placeholder = { Text("Enter Your OTP") },
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

            // Timer Display (Aligned Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp) // Added padding to move it slightly left
            ) {
                Spacer(modifier = Modifier.weight(0.9f)) // Push text to the right
                if (timeLeft > 0) {
                    Text(
                        text = formattedTime,
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 18.sp, // Added font size
                            fontFamily = GraphikFontFamily, // Added font family
                            fontWeight = FontWeight.Medium // Added font weight
                        )
                    )
                }
            }

            // Verify OTP Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    if (otp.length != 6) {
                        Toast.makeText(context, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@Button
                    }
                    controller.verifyOtp(email, mobile, employeeId, otp) { message, isError ->
                        isLoading = false
                        if (isError) {
                            errorMessage = message  // This will trigger the Toast via LaunchedEffect
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.97f)
                    .height(65.dp)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDD3825),
                    disabledContainerColor = Color(0xFFDD3825)  // Keep same color when disabled
                ),
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading && otp.length == 6
            ) {
                Text(
                    "Verify OTP",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Only show Resend OTP when timer is expired
            if (timeLeft == 0) {
                Text(
                    text = "Resend OTP",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable {
                            controller.resendOtp(email, mobile, employeeId) { message ->
                                if (message.contains("success", ignoreCase = true)) {
                                    timeLeft = 60  // Reset timer properly
                                    Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = message  // This will trigger the Toast via LaunchedEffect
                                }
                            }
                        },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
        // Replace the existing loading indicator with UniversalLoader
        UniversalLoader(isLoading = isLoading)
    }
}