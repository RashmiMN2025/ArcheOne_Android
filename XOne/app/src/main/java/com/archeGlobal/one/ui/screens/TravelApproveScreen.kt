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
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import kotlinx.coroutines.delay

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
                                    textAlign = TextAlign.Center
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
                        elevation = 0.dp,
                        actions = {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp,
                        backgroundColor = Color.White
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(24.dp)
                        ) {
                            // Title
                            Text(
                                text = "Approve Travel Request",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Request ID
                            Text(
                                text = "Request ID: ${travelRequest.id}",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Employee name
                            Text(
                                text = "Employee: ${travelRequest.approver ?: "Nova O'Sullivan"}",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Origin → Destination
                            val destinations = travelRequest.getAllDestinations()
                            val destinationText = if (destinations.isNotEmpty()) {
                                val destination = destinations[0]
                                if (!destination.originCity.isNullOrEmpty()) {
                                    "${destination.originCity} → ${destination.destinationCity}"
                                } else {
                                    destination.destinationCity
                                }
                            } else {
                                "Hyd → BBI" // Fallback
                            }
                            
                            Text(
                                text = "Origin → Destination: $destinationText",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Horizontal divider
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                color = Color.LightGray,
                                thickness = 1.dp
                            )

                            // Remarks section
                            Text(
                                text = "Remarks (Optional)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

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
                                        "Enter approval remarks...",
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Gray
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    textColor = Color.Black,
                                    placeholderColor = Color.Gray,
                                    backgroundColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Approve Request button
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
                                        text = "Approve Request",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

