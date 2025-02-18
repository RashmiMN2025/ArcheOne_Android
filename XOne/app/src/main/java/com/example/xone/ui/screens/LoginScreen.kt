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
import com.example.xone.navigation.Navigator
import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import android.app.Activity
import com.example.xone.R
import com.example.xone.ui.theme.XOneTheme
import com.example.xone.ui.preview.PreviewNavigator
import com.example.xone.ui.components.UniversalLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(controller: LoginController, navigator: Navigator) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

            // Login Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    
                    controller.sendOtp(
                        email = email,
                        mobile = mobile,
                        employeeId = employeeId
                    ) { message, isError ->
                        isLoading = false
                        if (!isError) {
                            Log.d("LoginScreen", "OTP sent successfully")
                            navigator.navigateToOtpVerification(email, mobile, employeeId)
                        } else {
                            Log.e("LoginScreen", "Error sending OTP: $message")
                            errorMessage = message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                enabled = !isLoading
            ) {
                Text("Login", color = Color.White)
            }

            // Error message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Replace the existing loading indicator with UniversalLoader
        UniversalLoader(isLoading = isLoading)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val previewNavigator = PreviewNavigator()
    
    XOneTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen(
                controller = LoginController(
                    context = LocalContext.current,
                    navigator = previewNavigator
                ),
                navigator = previewNavigator
            )
        }
    }
}
