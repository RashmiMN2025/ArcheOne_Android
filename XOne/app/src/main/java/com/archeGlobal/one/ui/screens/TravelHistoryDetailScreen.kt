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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun TravelHistoryDetailScreen(
    controller: TravelController,
    travelRequest: TravelRequest,
) {
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

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
                                text = "Travel History Details",
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

                    val scrollState = rememberScrollState()
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        val formattedCreatedDate =
                            formatDate(
                                java.text
                                    .SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        java.util.Locale.getDefault(),
                                    ).format(travelRequest.createdDate),
                            )
                        TravelSummaryCard(
                            id = travelRequest.id,
                            createdDate = formattedCreatedDate,
                            status =
                                when (travelRequest.status) {
                                    com.archeGlobal.one.model.TravelStatus.APPROVED -> "Approved"
                                    com.archeGlobal.one.model.TravelStatus.REJECTED -> "Rejected"
                                    com.archeGlobal.one.model.TravelStatus.PENDING -> "Pending"
                                    com.archeGlobal.one.model.TravelStatus.CANCELLED -> "Cancelled"
                                },
                        )

                        // Employee Details Card
                        TravelDetailCard(
                            title = "Employee Details",
                        ) {
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.person_3x,
                                label = "Employee",
                                value = controller.employeeName,
                            )
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.person_badge_clock,
                                label = "Employee ID",
                                value = controller.employeeId,
                            )
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.envelope_3x,
                                label = "Email",
                                value = controller.employeeEmail,
                            )
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.phone_3x,
                                label = "Mobile",
                                value = controller.mobileNumber,
                            )
                        }

                        // Travel Details Card
                        TravelDetailCard(
                            title = "Travel Details",
                        ) {
                            // Check if cab details and travel destinations are present
                            val hasCabDetails = (travelRequest.travelType != null && travelRequest.travelType.isNotBlank()) ||
                                               (travelRequest.cabType != null && travelRequest.cabType.isNotBlank()) ||
                                               (travelRequest.duration != null && travelRequest.duration.isNotBlank()) ||
                                               (travelRequest.pickupLocations?.isNotEmpty() == true && travelRequest.pickupLocations.any { it.isNotBlank() }) ||
                                               (travelRequest.dropLocation != null && travelRequest.dropLocation.isNotBlank()) ||
                                               (travelRequest.additionalMembers != null && travelRequest.additionalMembers.isNotBlank())

                            val destinations = travelRequest.getAllDestinations()
                            val hasTravelDestinations = destinations.isNotEmpty() &&
                                                       destinations.any { !it.destinationCity.isNullOrBlank() }

                            // Show destination/date fields if present
                            if (hasTravelDestinations) {

                                if (destinations.isEmpty() || destinations.size == 1) {
                                    // Single destination - show origin city and destination city separately
                                    if (destinations.isNotEmpty()) {
                                        val destination = destinations[0]

                                        // Show origin city if available
                                        if (!destination.originCity.isNullOrEmpty()) {
                                            TravelDetailRowWithDrawableIcon(
                                                iconRes = R.drawable.mappin_and_ellipse,
                                                label = "Origin City",
                                                value = destination.originCity,
                                            )
                                        }

                                        // Show destination city
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.mappin_and_ellipse,
                                            label = "Destination City",
                                            value = destination.destinationCity,
                                        )
                                    } else {
                                        // Fallback for cases without travel details
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.mappin_and_ellipse,
                                            label = "Destination",
                                            value = travelRequest.destination,
                                        )
                                    }

                                    val formattedDepartureDate = formatDate(travelRequest.departureDate)
                                    TravelDetailRowWithDrawableIcon(
                                        iconRes = R.drawable.airplane_departure,
                                        label = "Departure Date",
                                        value = formattedDepartureDate,
                                    )

                                    val formattedArrivalDate = formatDate(travelRequest.arrivalDate)
                                    TravelDetailRowWithDrawableIcon(
                                        iconRes = R.drawable.airplane_arrival,
                                        label = "Return Date",
                                        value = formattedArrivalDate,
                                    )
                                } else {
                                    // Multi-destination
                                    destinations.forEachIndexed { index, destination ->
                                        if (index > 0) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        Text(
                                            text = "Trip ${index + 1}",
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            color = Color.Black,
                                            modifier = Modifier.padding(bottom = 8.dp),
                                        )

                                        // Show origin city if available
                                        if (!destination.originCity.isNullOrEmpty()) {
                                            TravelDetailRowWithDrawableIcon(
                                                iconRes = R.drawable.mappin_and_ellipse,
                                                label = "Origin City",
                                                value = destination.originCity,
                                            )
                                        }

                                        // Show destination city
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.mappin_and_ellipse,
                                            label = "Destination City",
                                            value = destination.destinationCity,
                                        )

                                        val formattedDepartureDate = formatDate(destination.departureDate)
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.airplane_departure,
                                            label = "Departure Date",
                                            value = formattedDepartureDate,
                                        )

                                        val formattedArrivalDate = formatDate(destination.arrivalDate)
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.airplane_arrival,
                                            label = "Return Date",
                                            value = formattedArrivalDate,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.folder_3x,
                                label = "Project Name",
                                value = travelRequest.project,
                            )
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.busjust,
                                label = "Business Justification",
                                value = travelRequest.businessJustification ?: "N/A",
                            )
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.modeoft,
                                label = "Mode of Transport",
                                value = travelRequest.modeOfTransport ?: "N/A",
                            )

                            // Show cab-specific fields if cab details are present
                            if (hasCabDetails) {
                                // Add divider if we also showed travel destinations
                                if (hasTravelDestinations) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    androidx.compose.material3.HorizontalDivider(
                                        thickness = 1.dp,
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                travelRequest.travelType?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.traveltype,
                                            label = "Travel Type",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.cabType?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.cabtype,
                                            label = "Cab Type",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.duration?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.duration,
                                            label = "Duration",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.pickupLocations?.let { locations ->
                                    if (locations.isNotEmpty()) {
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.pickupl,
                                            label = "Pickup Location 1",
                                            value = locations[0],
                                        )
                                    }
                                }
                                travelRequest.dropLocation?.let {
                                    if (it.isNotBlank()) {
                                        TravelDetailRowWithDrawableIcon(
                                            iconRes = R.drawable.dropl,
                                            label = "Drop Location",
                                            value = it,
                                        )
                                    }
                                }
                                travelRequest.additionalMembers?.let { members ->
                                    if (members.isNotBlank()) {
                                        // Split members by comma and display each on a new line
                                        val membersList = members.split(",").map { it.trim() }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.additional),
                                                    contentDescription = "Additional Members",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = "Additional Members",
                                                    fontSize = 14.sp,
                                                    fontFamily = GraphikFontFamily,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }

                                            // Display each member on a new line with indent
                                            membersList.forEach { member ->
                                                if (member.isNotBlank()) {
                                                    Text(
                                                        text = "• $member",
                                                        fontSize = 14.sp,
                                                        fontFamily = GraphikFontFamily,
                                                        fontWeight = FontWeight.Normal,
                                                        color = Color.Black,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 36.dp, top = 4.dp),
                                                        textAlign = TextAlign.Start,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Show flight/train specific fields only for non-cab bookings
                                TravelDetailRowWithDrawableIcon(
                                    iconRes = R.drawable.stayreq,
                                    label = "Stay Required",
                                    value = "Yes", // This could be dynamic based on your data model
                                )
                                TravelDetailRowWithDrawableIcon(
                                    iconRes = R.drawable.meal,
                                    label = "Meal Preference",
                                    value = travelRequest.mealPreference ?: "N/A",
                                )
                                TravelDetailRowWithDrawableIcon(
                                    iconRes = R.drawable.seat,
                                    label = "Seat Preference",
                                    value = travelRequest.seatPreference ?: "N/A",
                                )
                                TravelDetailRowWithDrawableIcon(
                                    iconRes = R.drawable.noimage,
                                    label = "Frequent Flyer Number",
                                    value = travelRequest.frequentFlyerNumber ?: "N/A",
                                )
                            }
                        }

                        // Approval Details Card
                        TravelDetailCard(
                            title = "Approver Details",
                        ) {
//                            TravelDetailRowWithDrawableIcon(
//                                iconRes = when (travelRequest.status) {
//                                    com.archeGlobal.one.model.TravelStatus.APPROVED -> R.drawable.approved
//                                    com.archeGlobal.one.model.TravelStatus.REJECTED -> R.drawable.rejected
//                                    com.archeGlobal.one.model.TravelStatus.PENDING -> R.drawable.pending
//                                },
//                                label = "Status",
//                                value = travelRequest.status.name.lowercase().replaceFirstChar { it.uppercase() }
//                            )

                            // Show rejection reason if the status is rejected and reason is available
                            if (travelRequest.status == com.archeGlobal.one.model.TravelStatus.REJECTED &&
                                !travelRequest.rejectionReason.isNullOrEmpty()
                            ) {
                                TravelDetailRowWithDrawableIcon(
                                    iconRes = R.drawable.rejectionreason,
                                    label = "Rejection Reason",
                                    value = travelRequest.rejectionReason,
                                )
                            }
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.manager,
                                label = "Reporting Manager",
                                value = travelRequest.approver,
                            )
                            TravelDetailRowWithDrawableIcon(
                                iconRes = R.drawable.envelope_3x,
                                label = "Manager Email",
                                value = travelRequest.approverEmail ?: "N/A",
                            )

//                            val formattedCreatedDate = formatDate(
//                                java.text.SimpleDateFormat(
//                                    "yyyy-MM-dd",
//                                    java.util.Locale.getDefault()
//                                ).format(travelRequest.createdDate)
//                            )
//                            TravelDetailRowWithDrawableIcon(
//                                iconRes = R.drawable.calendar_3x,
//                                label = "Created At",
//                                value = formattedCreatedDate
//                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TravelSummaryCard(
    id: String,
    createdDate: String,
    status: String,
) {
    val (backgroundColor, textColor, borderColor) =
        when (status.lowercase()) {
            "pending" -> Triple(Color(0xFFFFA500).copy(alpha = 0.15f), Color(0xFFFFA500), Color(0xFFFFA500))
            "approved" -> Triple(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000), Color(0xFF008000))
            "rejected", "cancelled" -> Triple(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000), Color(0xFFFF0000))
            "closed" -> Triple(Color(0xFF808080).copy(alpha = 0.15f), Color(0xFF808080), Color(0xFF808080))
            else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, Color.Gray)
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "#$id",
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )
                    Row(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Created At: ",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                        )
                        Text(
                            text = createdDate,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        text = "Status: $status",
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TravelDetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = Color(0xFFF6F4EE),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            content()
        }
    }
}

@Composable
fun TravelDetailRowWithDrawableIcon(
    iconRes: Int,
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

// Helper function to format dates consistently
private fun formatDate(dateString: String?): String {
    return try {
        if (dateString.isNullOrEmpty()) return "N/A"
        val date =
            java.text
                .SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault(),
                ).parse(dateString)
        if (date != null) {
            java.text
                .SimpleDateFormat(
                    "dd MMM yyyy",
                    java.util.Locale.getDefault(),
                ).format(date)
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}
