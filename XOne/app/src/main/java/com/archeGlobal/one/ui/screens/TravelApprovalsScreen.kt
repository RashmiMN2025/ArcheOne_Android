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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.archeGlobal.one.ui.activities.TravelApproveActivity
import com.archeGlobal.one.ui.activities.TravelRejectActivity
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

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
            title = { Text("Rejection Reason", fontFamily = GraphikFontFamily, fontWeight = FontWeight.Bold) },
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
                    Text("Submit", color = if (rejectionRemarks.isNotBlank()) PrimaryRed else Color.Gray)
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,    // Light Beige/Grey
                        WelcomeBackgroundMiddle, // Light Grey
                        WelcomeBackgroundBottom  // Dark Grey
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                                    onApprove = { 
                                        // Navigate to dedicated approval screen instead of calling API directly
                                        val intent = Intent(context, TravelApproveActivity::class.java).apply {
                                            putExtra("travel_request", Gson().toJson(request))
                                        }
                                        context.startActivity(intent)
                                    },
                                    onReject = { 
                                        // Navigate to dedicated rejection screen instead of direct API call
                                        val intent = Intent(context, TravelRejectActivity::class.java).apply {
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
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${request.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GraphikFontFamily
                )
                
                val statusColor = when (request.status) {
                    com.archeGlobal.one.model.TravelStatus.APPROVED -> Color(0xFF4CAF50) // Green
                    com.archeGlobal.one.model.TravelStatus.REJECTED -> PrimaryRed
                    else -> Color(0xFFFFC107) // Amber/Yellow for pending
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = request.status.name.lowercase().capitalize(),
                        color = statusColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily
                    )
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )
            
            // Request details - only show the 4 required items
            DetailItem(iconRes = Icons.Default.Person, label = "Employee", value = request.approver)
            DetailItem(iconRes = Icons.Default.LocationOn, label = "Destination", value = request.destination)
            DetailItem(iconRes = Icons.Default.Info, label = "Project", value = request.project)
            DetailItem(iconRes = Icons.Default.DateRange, label = "Created", value = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).format(request.createdDate))
            
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
