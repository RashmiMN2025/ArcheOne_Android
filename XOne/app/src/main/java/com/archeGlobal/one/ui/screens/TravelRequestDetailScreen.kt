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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
                                    text = "Travel Request Details",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                .padding(16.dp)
                        ) {
                            // Header with ID and Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ID: ${travelRequest.id}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black
                                )

                                // Status Badge matching reject screen style
                                TravelRequestStatusBadge(status = travelRequest.status)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Employee Details
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.person_3x,
                                label = "Employee",
                                value = controller.employeeName
                            )
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.person_badge_clock,
                                label = "Employee ID",
                                value = controller.employeeId
                            )
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.envelope_3x,
                                label = "Email",
                                value = controller.employeeEmail
                            )
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.phone_3x,
                                label = "Mobile",
                                value = controller.mobileNumber
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Travel Details - Handle multi-destination vs single destination
                            val destinations = travelRequest.getAllDestinations()

                            if (destinations.isEmpty() || destinations.size == 1) {
                                // Single destination - show Trip 1
                                Text(
                                    text = "Trip 1",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                                )

                                // Show separate origin and destination fields
                                val destination = if (destinations.isNotEmpty()) destinations[0] else null

                                // Origin City
                                DetailRowWithDrawableIcon(
                                    iconRes = R.drawable.mappin_and_ellipse,
                                    label = "Origin City",
                                    value = destination?.originCity?.takeIf { it.isNotEmpty() } ?: "N/A"
                                )

                                // Destination City
                                DetailRowWithDrawableIcon(
                                    iconRes = R.drawable.mappin_and_ellipse,
                                    label = "Destination City",
                                    value = destination?.destinationCity ?: travelRequest.destination
                                )

                                // Format departure date
                                val formattedDepartureDate = formatDate(travelRequest.departureDate)
                                DetailRowWithDrawableIcon(
                                    iconRes = R.drawable.airplane_departure,
                                    label = "Date of Departure",
                                    value = formattedDepartureDate
                                )

                                // Format arrival date
                                val formattedArrivalDate = formatDate(travelRequest.arrivalDate)
                                DetailRowWithDrawableIcon(
                                    iconRes = R.drawable.airplane_arrival,
                                    label = "Date of Arrival",
                                    value = formattedArrivalDate
                                )
                            } else {
                                // Multi-destination - show Trip 1, Trip 2, etc.
                                destinations.forEachIndexed { index, destination ->
                                    if (index > 0) {
                                        // Add light divider line between trips
                                        androidx.compose.material3.HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            thickness = 1.dp,
                                            color = Color.Gray.copy(alpha = 0.3f)
                                        )
                                    }

                                    Text(
                                        text = "Trip ${index + 1}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                                    )

                                    // Origin City
                                    DetailRowWithDrawableIcon(
                                        iconRes = R.drawable.mappin_and_ellipse,
                                        label = "Origin City",
                                        value = destination.originCity?.takeIf { it.isNotEmpty() } ?: "N/A"
                                    )

                                    // Destination City
                                    DetailRowWithDrawableIcon(
                                        iconRes = R.drawable.mappin_and_ellipse,
                                        label = "Destination City",
                                        value = destination.destinationCity
                                    )

                                    val formattedDepartureDate = formatDate(destination.departureDate)
                                    DetailRowWithDrawableIcon(
                                        iconRes = R.drawable.airplane_departure,
                                        label = "Date of Departure",
                                        value = formattedDepartureDate
                                    )

                                    val formattedArrivalDate = formatDate(destination.arrivalDate)
                                    DetailRowWithDrawableIcon(
                                        iconRes = R.drawable.airplane_arrival,
                                        label = "Date of Arrival",
                                        value = formattedArrivalDate
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Project and other details
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.folder_3x,
                                label = "Project",
                                value = travelRequest.project
                            )
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.busjust,
                                label = "Business Justification",
                                value = travelRequest.businessJustification ?: "N/A"
                            )
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.car_3x,
                                label = "Mode of Transport",
                                value = travelRequest.modeOfTransport ?: "N/A"
                            )

                            // Format created date
                            val formattedCreatedDate = formatDate(
                                java.text.SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    java.util.Locale.getDefault()
                                ).format(travelRequest.createdDate)
                            )
                            DetailRowWithDrawableIcon(
                                iconRes = R.drawable.calendar_3x,
                                label = "Created",
                                value = formattedCreatedDate
                            )
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
            fontWeight = FontWeight.Medium,
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

@Composable
fun DetailRowWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(130.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun DetailRowWithDrawableIcon(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(130.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

// Helper function to format dates consistently
private fun formatDate(dateString: String?): String {
    return try {
        if (dateString.isNullOrEmpty()) return "N/A"
        val date = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).parse(dateString)
        if (date != null) {
            java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(date)
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

@Composable
fun TravelRequestStatusBadge(status: com.archeGlobal.one.model.TravelStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        com.archeGlobal.one.model.TravelStatus.APPROVED -> Triple(Color(0xFFD4EDDA), Color(0xFF155724), "Approved")
        com.archeGlobal.one.model.TravelStatus.REJECTED -> Triple(Color(0xFFF8D7DA), Color(0xFF721C24), "Rejected")
        com.archeGlobal.one.model.TravelStatus.PENDING -> Triple(Color(0xFFFFF3CD), Color(0xFFFF9800), "Pending")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
