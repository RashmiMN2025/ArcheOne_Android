package com.archeGlobal.one.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.archeGlobal.one.ui.activities.TravelApproveActivity
import com.archeGlobal.one.ui.activities.TravelRejectActivity
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelApprovalsScreen(
    controller: TravelController
) {
    // State for rejection dialog
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionRemarks by remember { mutableStateOf("") }
    var selectedRequestId by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    // Rejection dialog
    if (showRejectionDialog) {
        AlertDialog(
            onDismissRequest = {
                showRejectionDialog = false
                rejectionRemarks = ""
            },
            title = {
                Text(
                    "Rejection Reason",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Please provide a reason for rejecting this travel request:",
                        fontFamily = GraphikFontFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionRemarks,
                        onValueChange = { rejectionRemarks = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Enter rejection reason") },
                        maxLines = 3
                    )

                    // Focus the text field when dialog appears
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        controller.rejectTravelRequest(selectedRequestId, rejectionRemarks)
                        showRejectionDialog = false
                        rejectionRemarks = ""
                    },
                    enabled = rejectionRemarks.isNotBlank()
                ) {
                    Text(
                        "Submit",
                        color = if (rejectionRemarks.isNotBlank()) PrimaryRed else Color.Gray
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectionDialog = false
                    rejectionRemarks = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Get context and font adjustment for consistent font scaling
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
                                    text = "Travel Approvals",
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

                    // Trigger loading of travel approval requests when the screen is shown
                    LaunchedEffect(Unit) {
                        controller.loadTravelApprovals()
                    }

                    // Main content based on state
                    when (val state = controller.travelApprovalsState) {
                        is TravelController.TravelApprovalsState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryRed)
                            }
                        }

                        is TravelController.TravelApprovalsState.Success -> {
                            if (state.approvalRequests.isEmpty()) {
                                // Empty state
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No travel requests to approve",
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // Show list of approval requests
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.approvalRequests) { request ->
                                        ApprovalRequestCard(
                                            request = request,
                                            onApprove = { // Navigate to dedicated approval screen instead of calling API directly
                                                val intent = Intent(
                                                    context,
                                                    TravelApproveActivity::class.java
                                                ).apply {
                                                    putExtra("travel_request", Gson().toJson(request))
                                                }
                                                context.startActivity(intent)
                                            },
                                            onReject = { // Navigate to dedicated rejection screen instead of direct API call
                                                val intent = Intent(
                                                    context,
                                                    TravelRejectActivity::class.java
                                                ).apply {
                                                    putExtra("travel_request", Gson().toJson(request))
                                                }
                                                context.startActivity(intent)
                                            },
                                            onClick = {
                                                // Only navigate to detail screen for non-pending requests
                                                if (request.status != com.archeGlobal.one.model.TravelStatus.PENDING) {
                                                    controller.navigateToTravelApprovalDetail(request)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        is TravelController.TravelApprovalsState.Error -> {
                            // Error state
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Error loading travel approvals",
                                        color = Color.Red,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.message,
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryRed)
                                            .clickable { controller.loadTravelApprovals() }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Retry",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily
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
fun ApprovalRequestCard(
    request: TravelRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "ID: ${request.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )

                // Status Badge matching the image design
                TravelStatusBadgeComponent(status = request.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Employee and Project Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Employee",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Employee",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.approver,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Project",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Project",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.project,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trip details based on single or multi destination
            if (request.isMultiDestination()) {
                MultiDestinationTripDetails(
                    travelRequest = request
                )
            } else {
                SingleDestinationTripDetails(
                    travelRequest = request
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Created date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Created",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Created",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(request.createdDate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }

            // Show action buttons only for pending requests
            if (request.status == com.archeGlobal.one.model.TravelStatus.PENDING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Approve button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(color = Color(0xFF4CAF50)) // Green color
                            .padding(vertical = 12.dp)
                            .clickable(onClick = onApprove),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Approve",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily
                        )
                    }

                    // Reject button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(color = PrimaryRed)
                            .padding(vertical = 12.dp)
                            .clickable(onClick = onReject),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reject",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SingleDestinationTripDetails(
    travelRequest: TravelRequest
) {
    Column {
        Text(
            text = "Trip 1",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GraphikFontFamily,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Get destinations for separate display
        val destinations = travelRequest.getAllDestinations()
        val destination = if (destinations.isNotEmpty()) destinations[0] else null
        
        // Origin City
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Origin City",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Origin City",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = destination?.originCity?.takeIf { it.isNotEmpty() } ?: "NA",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        
        // Destination City
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Destination City",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Destination City",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = destination?.destinationCity ?: travelRequest.destination,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Travel Dates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.airplane_departure),
                contentDescription = "Travel Dates",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Travel Dates",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${travelRequest.departureDate ?: "N/A"} - ${travelRequest.arrivalDate ?: "N/A"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MultiDestinationTripDetails(
    travelRequest: TravelRequest
) {
    val destinations = travelRequest.getAllDestinations()

    destinations.forEachIndexed { index, destination ->
        Column(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = "Trip ${index + 1}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Origin City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Origin City",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Origin City",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = destination.originCity?.takeIf { it.isNotEmpty() } ?: "NA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            // Destination City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Destination City",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Destination City",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = destination.destinationCity,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Travel Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.airplane_departure),
                    contentDescription = "Travel Dates",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Travel Dates",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${destination.departureDate} - ${destination.arrivalDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }

            if (index < destinations.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TravelStatusBadgeComponent(status: com.archeGlobal.one.model.TravelStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        com.archeGlobal.one.model.TravelStatus.APPROVED -> Triple(Color(0xFF4CAF50), Color.White, "Approved")
        com.archeGlobal.one.model.TravelStatus.REJECTED -> Triple(Color(0xFFF44336), Color.White, "Rejected")
        com.archeGlobal.one.model.TravelStatus.PENDING -> Triple(Color(0xFFFFC107), Color.Black, "Pending")
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GraphikFontFamily,
            color = textColor
        )
    }
}

@Composable
fun DetailItem(
    iconRes: ImageVector,
    label: String,
    value: String?
) {
    if (value == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconRes,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}
