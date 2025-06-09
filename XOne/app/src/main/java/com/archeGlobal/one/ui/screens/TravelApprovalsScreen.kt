package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            
            // Mock data for travel approval requests
            val mockApprovalRequests = listOf(
                TravelRequest(
                    id = "TRV028",
                    destination = "Delhi",
                    project = "Archeone",
                    approver = "Annamalai Kalyanasundaram",
                    createdDate = java.util.Date(),
                    status = com.archeGlobal.one.model.TravelStatus.APPROVED,
                    businessJustification = "Client meeting"
                ),
                TravelRequest(
                    id = "TRV027",
                    destination = "New York",
                    project = "Project Alpha",
                    approver = "Annamalai",
                    createdDate = java.util.Date(),
                    status = com.archeGlobal.one.model.TravelStatus.PENDING,
                    businessJustification = "Team training"
                ),
                TravelRequest(
                    id = "TRV026",
                    destination = "New York",
                    project = "Project Alpha",
                    approver = "JD",
                    createdDate = java.util.Date(),
                    status = com.archeGlobal.one.model.TravelStatus.PENDING,
                    businessJustification = "Conference"
                )
            )
            
            // Main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockApprovalRequests) { request ->
                    ApprovalRequestCard(
                        request = request,
                        onApprove = { controller.approveTravelRequest(request.id) },
                        onReject = { controller.rejectTravelRequest(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovalRequestCard(
    request: TravelRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
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
            
            // Request details
            DetailItem(icon = "👤", label = "Approver", value = request.approver)
            DetailItem(icon = "🌍", label = "Destination", value = request.destination)
            DetailItem(icon = "📁", label = "Project", value = request.project)
            DetailItem(icon = "📅", label = "Created", value = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(request.createdDate))
            
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
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}
