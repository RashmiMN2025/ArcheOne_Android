package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for approving travel request with confirmation
 */
@Composable
fun TravelApproveScreen(
    controller: TravelController,
    travelRequest: TravelRequest
) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    var remarks by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Date formatter for display
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    // Observe the approval action state from the controller
    val approvalActionState = controller.approvalActionState

    // Handle approval action state changes
    LaunchedEffect(approvalActionState) {
        when (approvalActionState) {
            is TravelController.TravelApprovalActionState.Loading -> {
                isLoading = true
                errorMessage = null
                successMessage = null
            }

            is TravelController.TravelApprovalActionState.Success -> {
                isLoading = false
                errorMessage = null
                successMessage = approvalActionState.message
                delay(1500) // Give user time to see the success state
                controller.navigateBack()
            }

            is TravelController.TravelApprovalActionState.Error -> {
                isLoading = false
                errorMessage = approvalActionState.message
                successMessage = null
            }

            else -> {
                isLoading = false
                errorMessage = null
                successMessage = null
            }
        }
    }

    // Wrap entire content with font scale adjustment
    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WelcomeBackgroundTop,
                                WelcomeBackgroundMiddle,
                                WelcomeBackgroundBottom
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(48.dp))

                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Approve Travel Request",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = (-24).dp)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { controller.onBackPressed() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp,
                        backgroundColor = Color(0xFFF6F4EE)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(24.dp)
                        ) {
                            // Header with ID and Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${travelRequest.id}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black
                                )
                                
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    backgroundColor = Color(0xFFFFF3CD),
                                    elevation = 0.dp
                                ) {
                                    Text(
                                        text = "Status: Pending",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF9800),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Employee details
                            ApprovalDetailRow(
                                iconRes = R.drawable.person_3x,
                                label = "Employee",
                                value = travelRequest.approver ?: "Nova O'Sullivan"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.person_badge_clock,
                                label = "Employee ID",
                                value = "NT9999"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.envelope_3x,
                                label = "Email",
                                value = "webtestuser@arche.global"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.phone_3x,
                                label = "Mobile",
                                value = "7397768656"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.mappin_and_ellipse,
                                label = "Origin City",
                                value = "Hyd"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.mappin_and_ellipse,
                                label = "Destination City",
                                value = "BBI"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.airplane_departure,
                                label = "Date of Departure",
                                value = travelRequest.departureDate ?: "11 Aug 2025"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.airplane_arrival,
                                label = "Date of Arrival",
                                value = travelRequest.arrivalDate ?: "11 Aug 2025"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.folder_3x,
                                label = "Project",
                                value = travelRequest.project
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.busjust,
                                label = "Business Justification",
                                value = travelRequest.businessJustification ?: "Test"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.car_3x,
                                label = "Mode of Transport",
                                value = travelRequest.modeOfTransport ?: "Flight"
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.calendar_3x,
                                label = "Created",
                                value = dateFormatter.format(travelRequest.createdDate)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Remarks input
                            OutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(bottom = 24.dp),
                                placeholder = {
                                    Text(
                                        "Enter remark (optional)",
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Gray
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    textColor = Color.Black,
                                    placeholderColor = Color.Gray,
                                    backgroundColor = Color(0xFFF6F4EE)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Submit Approval button
                            Button(
                                onClick = {
                                    isLoading = true
                                    controller.approveTravelRequest(travelRequest.id, remarks)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF4CAF50),
                                    disabledBackgroundColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(28.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Submit Approval",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Error message
                            errorMessage?.let { error ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = error,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Success message
                            successMessage?.let { success ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = success,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    } // Close FontScaleAdjusted block
}

@Composable
private fun ApprovalDetailRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
    }
}

