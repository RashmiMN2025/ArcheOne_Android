package com.archeGlobal.one.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for approving travel request with confirmation
 */
@Composable
fun TravelApproveScreen(
    controller: TravelController,
    travelRequest: TravelRequest,
) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    var remarks by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Date formatter for display
    val dateFormatter =
        remember {
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
                // Set result to indicate success and finish activity
                (context as? Activity)?.let { activity ->
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                }
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            WelcomeBackgroundTop,
                                            WelcomeBackgroundMiddle,
                                            WelcomeBackgroundBottom,
                                        ),
                                ),
                        ),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Approve Travel Request",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = (-24).dp),
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                (context as? Activity)?.finish()
                            }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black,
                                )
                            }
                        },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                    )

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp,
                        backgroundColor = Color(0xFFF6F4EE),
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(24.dp),
                        ) {
                            // Header with ID and Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "#${travelRequest.id}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black,
                                )

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    backgroundColor = Color(0xFFFFF3CD),
                                    elevation = 0.dp,
                                ) {
                                    Text(
                                        text = "Status: Pending",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF9800),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Created At
                            ApprovalDetailRow(
                                iconRes = R.drawable.calendar_3x,
                                label = "Created At",
                                value = dateFormatter.format(travelRequest.createdDate),
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Employee details
                            ApprovalDetailRow(
                                iconRes = R.drawable.person_3x,
                                label = "Employee",
                                value = travelRequest.employeeName ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.person_badge_clock,
                                label = "Employee ID",
                                value = travelRequest.employeeId ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.envelope_3x,
                                label = "Email",
                                value = travelRequest.employeeEmail ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.phone_3x,
                                label = "Mobile",
                                value = travelRequest.employeeMobile ?: "N/A",
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Project details
                            ApprovalDetailRow(
                                iconRes = R.drawable.folder_3x,
                                label = "Project",
                                value = travelRequest.project,
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.projectid,
                                label = "Project ID",
                                value = travelRequest.projectId ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.opportunityid,
                                label = "Opportunity ID",
                                value = travelRequest.opportunityId ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.crmid,
                                label = "CRM ID",
                                value = travelRequest.crmId ?: "N/A",
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Business details
                            ApprovalDetailRow(
                                iconRes = R.drawable.busjust,
                                label = "Business Justification",
                                value = travelRequest.businessJustification ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.modeoft,
                                label = "Mode of Transport",
                                value = travelRequest.modeOfTransport ?: "Flight",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.stayreq,
                                label = "Stay Required",
                                value = travelRequest.stayRequired ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.car_3x,
                                label = "Cab Required",
                                value = "No", // Default value, update based on actual field if available
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.meal,
                                label = "Meal Preference",
                                value = travelRequest.mealPreference ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.seat,
                                label = "Seat Preference",
                                value = travelRequest.seatPreference ?: "N/A",
                            )

                            ApprovalDetailRow(
                                iconRes = R.drawable.noimage,
                                label = "Frequent Flyer Number",
                                value = travelRequest.frequentFlyerNumber ?: "N/A",
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Travel Details - Handle multi-destination vs single destination
                            val destinations = travelRequest.getAllDestinations()

                            if (destinations.isEmpty() || destinations.size == 1) {
                                // Single destination display (no trip label)
                                val destination = destinations.firstOrNull()

                                ApprovalDetailRow(
                                    iconRes = R.drawable.mappin_and_ellipse,
                                    label = "Origin City",
                                    value = destination?.originCity ?: "N/A",
                                )

                                ApprovalDetailRow(
                                    iconRes = R.drawable.mappin_and_ellipse,
                                    label = "Destination City",
                                    value = destination?.destinationCity ?: travelRequest.destination,
                                )

                                ApprovalDetailRow(
                                    iconRes = R.drawable.airplane_departure,
                                    label = "Date of Departure",
                                    value = destination?.departureDate ?: travelRequest.departureDate ?: "N/A",
                                )

                                ApprovalDetailRow(
                                    iconRes = R.drawable.airplane_arrival,
                                    label = "Date of Arrival",
                                    value = destination?.arrivalDate ?: travelRequest.arrivalDate ?: "N/A",
                                )
                            } else {
                                // Multi-destination display (with trip labels)
                                destinations.forEachIndexed { index, destination ->
                                    if (index > 0) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Text(
                                        text = "Trip ${index + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )

                                    ApprovalDetailRow(
                                        iconRes = R.drawable.mappin_and_ellipse,
                                        label = "Origin City",
                                        value = destination.originCity?.takeIf { it.isNotEmpty() } ?: "N/A",
                                    )

                                    ApprovalDetailRow(
                                        iconRes = R.drawable.mappin_and_ellipse,
                                        label = "Destination City",
                                        value = destination.destinationCity,
                                    )

                                    ApprovalDetailRow(
                                        iconRes = R.drawable.airplane_departure,
                                        label = "Date of Departure",
                                        value = destination.departureDate,
                                    )

                                    ApprovalDetailRow(
                                        iconRes = R.drawable.airplane_arrival,
                                        label = "Date of Arrival",
                                        value = destination.arrivalDate,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Remarks input
                            OutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(bottom = 24.dp),
                                placeholder = {
                                    Text(
                                        "Enter remark (optional)",
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Gray,
                                    )
                                },
                                colors =
                                    TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFF4CAF50),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        textColor = Color.Black,
                                        placeholderColor = Color.Gray,
                                        backgroundColor = Color.White,
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )

                            // Submit Approval button
                            Button(
                                onClick = {
                                    isLoading = true
                                    controller.approveTravelRequest(travelRequest.id, remarks)
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF4CAF50),
                                        disabledBackgroundColor = Color.Gray,
                                    ),
                                shape = RoundedCornerShape(28.dp),
                                enabled = !isLoading,
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = "Submit Approval",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
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
                                    modifier = Modifier.fillMaxWidth(),
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
                                    modifier = Modifier.fillMaxWidth(),
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
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.Gray,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.width(130.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.End,
        )
    }
}
