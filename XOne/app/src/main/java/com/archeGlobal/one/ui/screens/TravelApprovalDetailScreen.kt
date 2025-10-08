package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.*
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for approving or rejecting a travel request with detailed view
 */
@Composable
fun TravelApprovalDetailScreen(
    controller: TravelController,
    travelRequest: TravelRequest,
) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

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
                            Text(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .offset(x = (-24).dp),
                                text = "Travel Approvals Detail View",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { controller.onBackPressed() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black,
                                )
                            }
                        },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        actions = {},
                    )

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
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
                                    .padding(16.dp),
                        ) {
                            // Header with ID and Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "#${travelRequest.id}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black,
                                )

                                // Status Badge
                                TravelApprovalStatusBadge(status = travelRequest.status)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Divider line below ticket number/status
                            androidx.compose.material3.HorizontalDivider(
                                thickness = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.5f),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Employee Details
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.person_3x,
                                label = "Employee",
                                value = travelRequest.employeeName ?: "N/A",
                            )
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.person_badge_clock,
                                label = "Employee ID",
                                value = travelRequest.employeeId ?: "N/A",
                            )
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.envelope_3x,
                                label = "Email",
                                value = travelRequest.employeeEmail ?: "N/A",
                            )
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.phone_3x,
                                label = "Mobile",
                                value = travelRequest.employeeMobile ?: "N/A",
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Divider line
                            androidx.compose.material3.HorizontalDivider(
                                thickness = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.5f),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Project Details
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.folder_3x,
                                label = "Project",
                                value = travelRequest.project,
                            )
                            travelRequest.projectId?.let {
                                if (it.isNotBlank()) {
                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.folder_3x,
                                        label = "Project ID",
                                        value = it,
                                    )
                                }
                            }
                            travelRequest.opportunityId?.let {
                                if (it.isNotBlank()) {
                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.folder_3x,
                                        label = "Opportunity ID",
                                        value = it,
                                    )
                                }
                            }
                            travelRequest.crmId?.let {
                                if (it.isNotBlank()) {
                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.folder_3x,
                                        label = "CRM ID",
                                        value = it,
                                    )
                                }
                            }

                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.busjust,
                                label = "Business Justification",
                                value = travelRequest.businessJustification ?: "N/A",
                            )
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.car_3x,
                                label = "Mode of Transport",
                                value = travelRequest.modeOfTransport ?: "N/A",
                            )

                            // Show cab-specific fields if mode of transport is Cab
                            if (travelRequest.modeOfTransport?.lowercase() == "cab") {
                                travelRequest.travelType?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.car_3x,
                                            label = "Travel Type",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.cabType?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.car_3x,
                                            label = "Cab Type",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.duration?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.person_badge_clock,
                                            label = "Duration",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.pickupLocations?.let { locations ->
                                    if (locations.isNotEmpty()) {
                                        locations.forEachIndexed { index, location ->
                                            TravelDetailRowWithIcon(
                                                iconRes = R.drawable.mappin_and_ellipse,
                                                label = "Pickup Location ${index + 1}",
                                                value = location,
                                            )
                                        }
                                    }
                                }
                                travelRequest.dropLocation?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.mappin_and_ellipse,
                                            label = "Drop Location",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.additionalMembers?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.person_3x,
                                            label = "Additional Members",
                                            value = it,
                                        )
                                    }
                                }
                            } else {
                                // Show travel destination fields for non-cab bookings
                                val destinations = travelRequest.getAllDestinations()

                                if (destinations.isEmpty() || destinations.size == 1) {
                                    val destination = if (destinations.isNotEmpty()) destinations[0] else null

                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.mappin_and_ellipse,
                                        label = "Origin City",
                                        value = destination?.originCity?.takeIf { it.isNotEmpty() } ?: "N/A",
                                    )
                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.mappin_and_ellipse,
                                        label = "Destination City",
                                        value = destination?.destinationCity ?: travelRequest.destination,
                                    )

                                    val formattedDepartureDate = formatTravelDate(destination?.departureDate ?: travelRequest.departureDate)
                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.airplane_departure,
                                        label = "Date of Departure",
                                        value = formattedDepartureDate,
                                    )

                                    val formattedArrivalDate = formatTravelDate(destination?.arrivalDate ?: travelRequest.arrivalDate)
                                    TravelDetailRowWithIcon(
                                        iconRes = R.drawable.airplane_arrival,
                                        label = "Date of Arrival",
                                        value = formattedArrivalDate,
                                    )
                                } else {
                                    // Multi-destination
                                    destinations.forEachIndexed { index, destination ->
                                        if (index > 0) {
                                            androidx.compose.material3.HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                thickness = 1.dp,
                                                color = Color.Gray.copy(alpha = 0.3f),
                                            )
                                        }

                                        Text(
                                            text = "Trip ${index + 1}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            color = Color.Black,
                                            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
                                        )

                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.mappin_and_ellipse,
                                            label = "Origin City",
                                            value = destination.originCity?.takeIf { it.isNotEmpty() } ?: "N/A",
                                        )
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.mappin_and_ellipse,
                                            label = "Destination City",
                                            value = destination.destinationCity,
                                        )

                                        val formattedDepartureDate = formatTravelDate(destination.departureDate)
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.airplane_departure,
                                            label = "Date of Departure",
                                            value = formattedDepartureDate,
                                        )

                                        val formattedArrivalDate = formatTravelDate(destination.arrivalDate)
                                        TravelDetailRowWithIcon(
                                            iconRes = R.drawable.airplane_arrival,
                                            label = "Date of Arrival",
                                            value = formattedArrivalDate,
                                        )
                                    }
                                }
                            }

                            // Created Date
                            val formattedCreatedDate =
                                formatTravelDate(
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(travelRequest.createdDate),
                                )
                            TravelDetailRowWithIcon(
                                iconRes = R.drawable.calendar_3x,
                                label = "Created Date",
                                value = formattedCreatedDate,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action buttons - only show for pending requests
                            if (travelRequest.status == TravelStatus.PENDING) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Button(
                                        onClick = { controller.navigateToTravelApprove(travelRequest) },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                backgroundColor = Color(0xFF4CAF50),
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = "Approve",
                                            color = Color.White,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }

                                    Button(
                                        onClick = { controller.navigateToTravelReject(travelRequest) },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                backgroundColor = PrimaryRed,
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = "Reject",
                                            color = Color.White,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
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
private fun TravelApprovalStatusBadge(status: TravelStatus) {
    val (backgroundColor, textColor, text) =
        when (status) {
            TravelStatus.APPROVED -> Triple(Color(0xFFE6F4EA), Color(0xFF34A853), "Approved")
            TravelStatus.REJECTED -> Triple(Color(0xFFFCE8E6), Color(0xFFEA4335), "Rejected")
            TravelStatus.PENDING -> Triple(Color(0xFFFEF7E0), Color(0xFFFBBC05), "Pending")
            TravelStatus.CANCELLED -> Triple(Color(0xFFFCE8E6), Color(0xFFEA4335), "Cancelled")
        }

    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp,
    ) {
        Text(
            text = "Status: $text",
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun TravelDetailRowWithIcon(
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
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(130.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End,
        )
    }
}

// Helper function to format dates consistently
private fun formatTravelDate(dateString: String?): String {
    return try {
        if (dateString.isNullOrEmpty()) return "N/A"
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        if (date != null) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}
