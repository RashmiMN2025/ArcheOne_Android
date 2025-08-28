package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.archeGlobal.one.controller.TravelController.TravelHistoryState
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.DateFormatter
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment

@Composable
fun TravelHistoryScreen(
    controller: TravelController
) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    val state = controller.travelHistoryState

    // Wrap entire content with font scale adjustment
    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WelcomeBackgroundTop, // Light Beige/Grey (0xFFE0DCD1)
                                WelcomeBackgroundMiddle, // Light Grey (0xFFC8C8CA)
                                WelcomeBackgroundBottom // Dark Grey (0xFF474749)
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
                                    text = "Travel History",
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

                    // Add more space after the TopAppBar
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Render UI based on current travel history state
                        when (val currentState = state) {
                            is TravelHistoryState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }

                            is TravelHistoryState.Success -> {
                                if (currentState.historyItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "No travel history found", fontFamily = GraphikFontFamily)
                                    }
                                } else {
                                    TravelHistoryList(
                                        travelRequests = currentState.historyItems,
                                        onTravelRequestClick = { requestId ->
                                            controller.navigateToTravelDetails(requestId)
                                        }
                                    )
                                }
                            }

                            is TravelHistoryState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = currentState.message, fontFamily = GraphikFontFamily)
                                    Button(
                                        onClick = { controller.loadCombinedTravelHistory() },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("Retry", fontFamily = GraphikFontFamily)
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
fun TravelHistoryList(
    travelRequests: List<TravelRequest>,
    onTravelRequestClick: (String) -> Unit
) {
    if (travelRequests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No items found", fontFamily = GraphikFontFamily)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(travelRequests) { request ->
                TravelRequestCard(
                    travelRequest = request,
                    onClick = { onTravelRequestClick(request.id) }
                )
            }
        }
    }
}

@Composable
fun TravelRequestCard(
    travelRequest: TravelRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = Color(0xFFF6F4EE)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // ID and Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${travelRequest.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GraphikFontFamily
                )
                StatusTag(status = travelRequest.status)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())

            // Project
            DetailItem(
                icon = R.drawable.folder_3x,
                label = "Project",
                value = travelRequest.project
            )

            // Handle single vs multi-destination display
            val destinations = travelRequest.getAllDestinations()

            if (destinations.isEmpty() || destinations.size == 1) {
                // Single destination - show origin city and destination city separately
                if (destinations.isNotEmpty()) {
                    val destination = destinations[0]

                    // Show origin city if available
                    if (!destination.originCity.isNullOrEmpty()) {
                        DetailItem(
                            icon = R.drawable.mappin_and_ellipse,
                            label = "Origin City",
                            value = destination.originCity
                        )
                    }

                    // Show destination city
                    DetailItem(
                        icon = R.drawable.mappin_and_ellipse,
                        label = "Destination City", value = destination.destinationCity
                    )

                    // Show travel dates for single destination
                    if (!destination.departureDate.isNullOrEmpty() && !destination.arrivalDate.isNullOrEmpty()) {
                        DetailItem(
                            icon = R.drawable.ic_calendar,
                            label = "Travel Dates",
                            value = DateFormatter.formatTravelDateRange(destination.departureDate, destination.arrivalDate)
                        )
                    }
                } else {
                    // Fallback for cases without travel details
                    DetailItem(
                        icon = R.drawable.mappin_and_ellipse,
                        label = "Destination",
                        value = travelRequest.destination
                    )
                }
            } else {
                // Multi-destination - show Trip 1, Trip 2, etc.
                destinations.forEachIndexed { index, destination ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trip ${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Show origin city if available
                    if (!destination.originCity.isNullOrEmpty()) {
                        DetailItem(
                            icon = R.drawable.mappin_and_ellipse,
                            label = "Origin City",
                            value = destination.originCity
                        )
                    }

                    // Show destination city
                    DetailItem(
                        icon = R.drawable.mappin_and_ellipse,
                        label = "Destination City",
                        value = destination.destinationCity
                    )

                    DetailItem(
                        icon = R.drawable.airplane_departure,
                        label = "Travel Dates",
                        value = DateFormatter.formatTravelDateRange(destination.departureDate, destination.arrivalDate)
                    )
                }
            }

            // Approver
            DetailItem(
                icon = R.drawable.approver,
                label = "Approver",
                value = travelRequest.approver
            )

            // Add divider line before Created date
            Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())

            // Created date
            DetailItem(
                icon = R.drawable.ic_calendar,
                label = "Created",
                value = DateFormatter.formatDisplayDate(travelRequest.createdDate)
            )
        }
    }
}

@Composable
fun StatusTag(status: TravelStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TravelStatus.APPROVED -> Triple(Color(0xFFE6F4EA), Color(0xFF34A853), "Approved")
        TravelStatus.REJECTED -> Triple(Color(0xFFFCE8E6), Color(0xFFEA4335), "Rejected")
        TravelStatus.PENDING -> Triple(Color(0xFFFEF7E0), Color(0xFFFBBC05), "Pending")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = "Status: $text",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = GraphikFontFamily,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetailItem(
    icon: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = GraphikFontFamily
        )
    }
}
