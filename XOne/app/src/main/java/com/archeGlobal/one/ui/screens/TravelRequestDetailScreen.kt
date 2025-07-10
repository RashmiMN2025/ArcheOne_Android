package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import androidx.compose.runtime.remember

@Composable
fun TravelRequestDetailScreen(
    controller: TravelController,
    travelRequest: TravelRequest
) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    // Wrap entire content with font scale adjustment
    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WelcomeBackgroundTop, // Light Beige/Grey
                                WelcomeBackgroundMiddle, // Light Grey
                                WelcomeBackgroundBottom // Dark Grey
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Add space at the top to push everything down
                    Spacer(modifier = Modifier.height(48.dp))

                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Request Details",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { controller.onBackPressed(fromTravelDetail = true) }) {
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

                    // Main content
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Employee Details Card
                        DetailCard(title = "Employee Details") {
                            DetailRow(label = "Name:", value = controller.employeeName)
                            DetailRow(label = "Employee ID:", value = controller.employeeId)
                            DetailRow(label = "Email:", value = controller.employeeEmail)
                            DetailRow(label = "Mobile:", value = controller.mobileNumber)
                        }

                        // Travel Details Card
                        DetailCard(title = "Travel Details") {
                            DetailRow(label = "Destination:", value = travelRequest.destination)
                            DetailRow(label = "Project Name:", value = travelRequest.project)
                            // Use fields from the TravelRequest model
                            DetailRow(
                                label = "Business Justification:",
                                value = travelRequest.businessJustification ?: "N/A"
                            )
                            DetailRow(
                                label = "Mode of Transport:",
                                value = travelRequest.modeOfTransport ?: "N/A"
                            )
                            // Format departure date in the format: day Month year (e.g., 3 Jul 2025)
                            val formattedDepartureDate = try {
                                val date = java.text.SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    java.util.Locale.getDefault()
                                )
                                    .parse(travelRequest.departureDate ?: "")
                                if (date != null) {
                                    java.text.SimpleDateFormat(
                                        "d MMM yyyy",
                                        java.util.Locale.getDefault()
                                    )
                                        .format(date)
                                } else {
                                    "N/A"
                                }
                            } catch (e: Exception) {
                                "N/A"
                            }

                            // Format arrival date in the format: day Month year (e.g., 17 Jul 2025)
                            val formattedArrivalDate = try {
                                val date = java.text.SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    java.util.Locale.getDefault()
                                )
                                    .parse(travelRequest.arrivalDate ?: "")
                                if (date != null) {
                                    java.text.SimpleDateFormat(
                                        "d MMM yyyy",
                                        java.util.Locale.getDefault()
                                    )
                                        .format(date)
                                } else {
                                    "N/A"
                                }
                            } catch (e: Exception) {
                                "N/A"
                            }

                            DetailRow(label = "Departure Date:", value = formattedDepartureDate)
                            DetailRow(label = "Arrival Date:", value = formattedArrivalDate)
                            DetailRow(
                                label = "Stay Required:",
                                value = travelRequest.stayRequired ?: "N/A"
                            )
                            DetailRow(
                                label = "Meal Preference:",
                                value = travelRequest.mealPreference ?: "None"
                            )
                            DetailRow(
                                label = "Seat Preference:",
                                value = travelRequest.seatPreference ?: "None"
                            )
                            DetailRow(
                                label = "Flight Time:",
                                value = travelRequest.flightTime ?: "None"
                            )
                            DetailRow(
                                label = "Frequent\nFlyer Number:",
                                value = travelRequest.frequentFlyerNumber ?: "None"
                            )
                        }

                        // Approval Details Card
                        DetailCard(title = "Approval Details") {
                            DetailRow(
                                label = "Status:",
                                value = travelRequest.status.name.capitalize(),
                                valueColor = Color.Black // All status colors set to black
                            )
                            DetailRow(
                                label = "Reporting Manager:",
                                value = controller.reportingManagerName
                            )
                            DetailRow(
                                label = "Manager Email:",
                                value = controller.reportingManagerEmail
                            )

                            // Format the created date for display in the format: yyyy-MM-dd HH:mm:ss
                            val formattedCreatedAt = try {
                                java.text.SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss",
                                    java.util.Locale.getDefault()
                                )
                                    .format(travelRequest.createdDate)
                            } catch (e: Exception) {
                                "N/A"
                            }
                            DetailRow(label = "Created At:", value = formattedCreatedAt)
                        }
                    }
                }
            }
        }
    } // Close FontScaleAdjusted block
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GraphikFontFamily,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp, // Smaller font size
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            modifier = Modifier.width(140.dp) // Fixed width for alignment
        )
        Text(
            text = value,
            fontSize = 14.sp, // Smaller font size
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal, // Normal font weight
            color = valueColor
        )
    }
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
