package com.archeGlobal.one.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.border
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveOtpVerificationScreen(
    controller: OtpVerificationController,
    email: String,
    mobile: String,
    employeeId: String
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val contentPadding = when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16.dp   // Phone
        WindowWidthSizeClass.Medium -> 48.dp    // Large phone/small tablet
        WindowWidthSizeClass.Expanded -> 120.dp // Tablet
        else -> 16.dp
    }
    OtpVerificationScreen(
        controller = controller,
        email = email,
        mobile = mobile,
        employeeId = employeeId,
        contentPadding = contentPadding
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun OtpVerificationScreen(
    controller: OtpVerificationController,
    email: String,
    mobile: String,
    employeeId: String,
    contentPadding: Dp = 16.dp
) {
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = List(6) { remember { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    var focusedIndex by remember { mutableStateOf(-1) }
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
            .padding(horizontal = contentPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Company Logo
            CompanyLogo(modifier = Modifier.height(120.dp))

            Spacer(modifier = Modifier.height(28.dp))

            // Row with email icon and "Enter OTP" text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = Color(0xFFDD3825),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Enter OTP",
                    fontSize = 22.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 16.dp, 32.dp, 32.dp)
            ) {
                Text(
                    text = "Enter the 6-digit OTP sent to your registered email",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            // OTP code label (left aligned)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "OTP code",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            // 6-digit OTP input boxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0 until 6) {
                    OutlinedTextField(
                        value = otpDigits[i],
                        onValueChange = { value ->
                            if (value.length <= 1 && value.all { it.isDigit() }) {
                                otpDigits[i] = value
                                if (value.isNotEmpty() && i < 5) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            }
                            if (value.isEmpty() && i > 0) {
                                otpDigits[i] = ""
                                focusRequesters[i - 1].requestFocus()
                            }
                        },
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .focusRequester(focusRequesters[i])
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    focusedIndex = i
                                }
                            }
                            .border(
                                width = 1.5.dp,
                                color = if (focusedIndex == i) Color(0xFFDD3825) else Color.White,
                                shape = MaterialTheme.shapes.medium
                            ),
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = if (i == 5) ImeAction.Done else ImeAction.Next
                        ),
                        enabled = !isLoading,
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
                        )
                    )
                }
            }

            val otp = otpDigits.joinToString("")

            Spacer(modifier = Modifier.height(8.dp))

            // Timer and Resend OTP Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Resend OTP in $formattedTime",
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                // Resend OTP button (enabled only when timer is 0)
                Button(
                    onClick = {
                        if (timeLeft == 0) {
                            controller.resendOtp(email, mobile, employeeId) { message ->
                                if (message.contains("success", ignoreCase = true)) {
                                    timeLeft = 60  // Restart timer
                                    Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = message
                                }
                            }
                        }
                    },
                    enabled = timeLeft == 0,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0B4AA),
                        contentColor = Color(0xFFDD3825),
                        disabledContainerColor = Color(0xFFE0B4AA),
                        disabledContentColor = Color(0xFFDD3825)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFDD3825)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Resend OTP",
                        color = Color(0xFFDD3825),
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
        }
        // Replace the existing loading indicator with UniversalLoader
        UniversalLoader(isLoading = isLoading)
    }
}