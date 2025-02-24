package com.example.xone.ui.screens

import com.example.xone.ui.theme.XOneTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.xone.controller.LoginController
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.navigation.Navigator
import com.example.xone.ui.preview.PreviewNavigator
import com.example.xone.ui.components.UniversalLoader
import android.util.Log

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
                placeholder = { Text("Enter OTP") },
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

            // Error message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Verify OTP Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    if (otp.length != 6) {  // Add validation
                        errorMessage = "Please enter a valid 6-digit OTP"
                        isLoading = false
                        return@Button
                    }
                    controller.verifyOtp(email, mobile, employeeId, otp) { message, isError ->
                        isLoading = false
                        if (isError) {
                            Log.e("OtpVerification", "Error verifying OTP: $message")
                            errorMessage = message
                        } else {
                            Log.d("OtpVerification", "OTP verification successful")
                            errorMessage = null
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
                enabled = !isLoading && otp.length == 6
            ) {
                Text(
                    "Verify OTP",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend OTP Button (Removed Button Background Color, Added Underline)
            Text(
                text = "Resend OTP",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable(enabled = timeLeft == 0) {
                        controller.resendOtp(email, mobile, employeeId) { message ->
                            errorMessage = if (message.contains("success", ignoreCase = true)) null else message
                            if (message.contains("success", ignoreCase = true)) {
                                timeLeft = 60
                                timerStarted = true
                            }
                        }
                    },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (timeLeft == 0) Color.Black else Color.Gray,
                    textDecoration = if (timeLeft == 0) TextDecoration.Underline else TextDecoration.None
                ),
                textAlign = TextAlign.Center
            )

            // Replace the existing loading indicator with UniversalLoader
            UniversalLoader(isLoading = isLoading)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    val previewNavigator = PreviewNavigator()
    
    XOneTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            OtpVerificationScreen(
                controller = OtpVerificationController(
                    navigator = previewNavigator,
                    loginController = LoginController(
                        context = LocalContext.current,
                        navigator = previewNavigator
                    )
                ),
                email = "john.doe@company.com",
                mobile = "+91 9876543210",
                employeeId = "EMP123"
            )
        }
    }
}
