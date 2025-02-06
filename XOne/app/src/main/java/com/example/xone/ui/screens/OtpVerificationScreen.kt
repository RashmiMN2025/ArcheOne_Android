package com.example.xone.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import com.example.xone.HomeDashboardActivity
import com.example.xone.controller.OtpVerificationController
import com.example.xone.ui.components.CompanyLogo

@SuppressLint("DefaultLocale")
@Composable
fun OtpVerificationScreen(controller: OtpVerificationController, email: String, mobile: String, employeeId: String) {
    var otp by remember { mutableStateOf("") }
    var timerStarted by remember { mutableStateOf(true) }
    var timeLeft by remember { mutableStateOf(60) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }  // Track error state
    val context = LocalContext.current

    // Timer logic: Decrease `timeLeft` every second
    LaunchedEffect(timerStarted) {
        if (timerStarted) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                timeLeft -= 1
            }
        }
    }

    // Format time into MM:SS
    val formattedTime = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)

    // Show Toast message for verification feedback
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
            Spacer(modifier = Modifier.height(40.dp))

            // Company Logo
            CompanyLogo(modifier = Modifier.height(120.dp))

            Spacer(modifier = Modifier.height(20.dp))

            // OTP Field
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                placeholder = { Text("Enter OTP") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions.Default,
                shape = MaterialTheme.shapes.medium
            )

            // Timer Display (Aligned Right)
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f)) // Push text to the right
                if (timeLeft > 0) {
                    Text(
                        text = formattedTime,
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Verify OTP Button
            Button(
                onClick = {
                    controller.verifyOtp(email, mobile, employeeId, otp) { responseMessage, error ->
                        message = responseMessage
                        isError = error

                        if (!error) {
                            val intent = Intent(context, HomeDashboardActivity::class.java)
                            context.startActivity(intent)
                        }
                        else {
                            // Handle OTP validation failure
                            Toast.makeText(context, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)), // Red Button
                enabled = true // Always enabled
            ) {
                Text("Verify OTP", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend OTP Button (Removed Button Background Color, Added Underline)
            Text(
                text = "Resend OTP",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable {
                        controller.resendOtp(email) { responseMessage ->
                            message = responseMessage
                            timeLeft = 60 // Restart the timer when OTP is resent
                            timerStarted = true // Start the timer again
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
}
