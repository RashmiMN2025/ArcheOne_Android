package com.example.xone.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.xone.controller.LoginController
import com.example.xone.ui.components.CompanyLogo
import androidx.compose.material3.Text
import com.example.xone.OtpVerificationActivity
import com.example.xone.navigation.AndroidNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(controller: LoginController, navigator: AndroidNavigator) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Show Toast when message updates
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

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter Email ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,  // White background
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,      // Black text
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,  // No underline
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions.Default,
                shape = MaterialTheme.shapes.medium
            )

            // Phone Number Field
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                placeholder = { Text("Enter Mobile Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,  // White background
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,      // Black text
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,  // No underline
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions.Default,
                shape = MaterialTheme.shapes.medium
            )

            // Employee ID Field
            OutlinedTextField(
                value = employeeId,
                onValueChange = { employeeId = it },
                placeholder = { Text("Enter Employee ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,  // White background
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,      // Black text
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,  // No underline
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions.Default,
                shape = MaterialTheme.shapes.medium
            )

            // Send OTP Button
            Button(
                onClick = {
                    controller.sendOtp(email) { responseMessage, isErrorResponse ->
                        message = responseMessage
                        isError = isErrorResponse

                        // On successful OTP sending, navigate to OTP verification screen
                        if (!isErrorResponse) {
                            val intent = Intent(context, OtpVerificationActivity::class.java).apply {
                                putExtra("email", email)
                                putExtra("mobile", mobile)
                                putExtra("employeeId", employeeId)
                            }
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, responseMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)) // Red Button
            ) {
                Text("Login", color = Color.White)
            }
        }
    }
}
