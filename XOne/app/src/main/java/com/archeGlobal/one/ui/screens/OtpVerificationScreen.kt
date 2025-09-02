package com.archeGlobal.one.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.components.CompanyLogo
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.CustomToast
import com.archeGlobal.one.utils.DeviceInfoUtils
import kotlinx.coroutines.delay

@SuppressLint("DefaultLocale")
@Composable
fun OtpVerificationScreen(
    controller: OtpVerificationController,
    email: String,
    mobile: String,
    employeeId: String,
    stayLoggedIn: Boolean
) {
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = List(6) { remember { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    var focusedIndex by remember { mutableStateOf(-1) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(60) }
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

    // Show Toast message for errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // Format long messages with line breaks for better visibility
            val formattedMessage = if (message.length > 50) {
                // Split long messages into multiple lines
                val words = message.split(" ")
                val lines = mutableListOf<String>()
                var currentLine = ""

                for (word in words) {
                    if ((currentLine + word).length > 35) {
                        if (currentLine.isNotEmpty()) {
                            lines.add(currentLine.trim())
                            currentLine = word + " "
                        } else {
                            lines.add(word)
                        }
                    } else {
                        currentLine += "$word "
                    }
                }

                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.trim())
                }

                // Join with newlines to create multi-line toast
                lines.joinToString("\n")
            } else {
                message
            }

            CustomToast.showErrorToast(context, formattedMessage)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749) // Dark Gray
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp, 16.dp, 32.dp, 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                                        timeLeft = 60 // Restart timer
                                        CustomToast.showErrorToast(context, "OTP sent successfully")
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
                            containerColor = Color(0xFFEFE0DF),
                            contentColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFEFE0DF),
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

                        val otp = otpDigits.joinToString("")

                        // ✅ Check if all OTP boxes are empty
                        if (otp.isBlank()) {
                            CustomToast.showErrorToast(context, "Please enter an OTP!")
                            isLoading = false
                            return@Button
                        }

                        if (otp.length < 6) {
                            CustomToast.showErrorToast(context, "Please enter a valid OTP!")
                            isLoading = false
                            return@Button
                        }

                        val deviceInfo = DeviceInfoUtils.getAllDeviceInfo(context)
                        val appVersion = deviceInfo.appVersion
                        val deviceModel = deviceInfo.deviceModel
                        val osVersion = deviceInfo.osVersion
                        val platform = deviceInfo.platform
                        val deviceId = deviceInfo.deviceId

                        controller.verifyOtp(
                            email,
                            mobile,
                            employeeId,
                            otp,
                            isBiometric = false,
                            backgroundRefresh = false,
                            appVersion = appVersion,
                            deviceModel = deviceModel,
                            deviceId = deviceId,
                            platform = platform,
                            osVersion = osVersion,
                            stayLoggedIn = stayLoggedIn,
                        ) { message, isError ->
                            isLoading = false
                            if (isError) {
                                errorMessage = message // This will trigger the Toast via LaunchedEffect
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.97f)
                        .height(65.dp)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825),
                        disabledContainerColor = Color(0xFFDD3825) // Keep same color when disabled
                    ),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading
                ) {
                    Text(
                        "Verify OTP",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                val goBackText = buildAnnotatedString {
                    val start = length
                    append("Go Back")
                    addStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            textDecoration = TextDecoration.Underline,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        start = start,
                        end = length
                    )
                    addStringAnnotation(
                        tag = "go_back",
                        annotation = "go_back",
                        start = start,
                        end = length
                    )
                }
                ClickableText(
                    text = goBackText,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { offset ->
                        goBackText.getStringAnnotations(
                            tag = "go_back",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let {
                                val activity = context as? android.app.Activity
                                activity?.finish()
                            }
                    }
                )
            }
            // Replace the existing loading indicator with UniversalLoader
            UniversalLoader(isLoading = isLoading)
        }
    }
}
