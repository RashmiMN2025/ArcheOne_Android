package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.*

/**
 * Screen for approving or rejecting a travel request with detailed view
 */
@Composable
fun TravelApprovalDetailScreen(
    controller: TravelController,
    travelRequest: TravelRequest
) {
    val selectedRequest = travelRequest
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
                // Show loading state
                isLoading = true
                errorMessage = null
                successMessage = null
            }
            is TravelController.TravelApprovalActionState.Success -> {
                // Show success and navigate back after a delay
                isLoading = false
                errorMessage = null
                successMessage = approvalActionState.message
                delay(1500) // Give user time to see the success state
                controller.navigateBack()
            }
            is TravelController.TravelApprovalActionState.Error -> {
                // Show error message
                isLoading = false
                errorMessage = approvalActionState.message
                successMessage = null
            }
            else -> {
                // Reset state
                isLoading = false
                errorMessage = null
                successMessage = null
            }
        }
    }
    
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
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80FFFFFF))
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing request...")
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text("Approve Travel Request", fontFamily = GraphikFontFamily) },
                navigationIcon = {
                    IconButton(onClick = { controller.navigateBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color.White
            )
            
            // Main content
            if (selectedRequest != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Request card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Request ID and Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ID: ${selectedRequest.id}",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                
                                StatusBadge(status = selectedRequest.status)
                            }
                            
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            
                            // Request details
                            DetailRow("Employee", selectedRequest.approver)
                            DetailRow("Mobile", selectedRequest.businessJustification ?: "N/A") // Using justification as mobile for demo
                            DetailRow("Destination", selectedRequest.destination)
                            DetailRow("Project", selectedRequest.project)
                            DetailRow("Business Justification", selectedRequest.businessJustification ?: "N/A")
                            DetailRow("Date of Departure", selectedRequest.departureDate ?: "N/A")
                            DetailRow("Date of Arrival", selectedRequest.arrivalDate ?: "N/A")
                            DetailRow("Mode of Transport", selectedRequest.modeOfTransport ?: "N/A")
                        }
                    }
                    
                    // Remarks input field
                    if (selectedRequest.status == TravelStatus.PENDING) {
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 16.dp),
                            label = { Text("Enter remark (optional)") },
                            placeholder = { Text("Enter remark (optional)") }
                        )
                        
                        // Error message display
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = PrimaryRed,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Success message display
                        successMessage?.let {
                            Text(
                                text = it,
                                color = Color(0xFF4CD964), // Green color
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Approve button
                        Button(
                            onClick = { 
                                // Navigate to the approval confirmation screen instead of making the API call directly
                                controller.navigateToTravelApprovalConfirm(selectedRequest)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF4CD964), // Green color as shown in the image
                                disabledBackgroundColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
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
                                    "Submit Approval",
                                    color = Color.White,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reject button
                        OutlinedButton(
                            onClick = { 
                                controller.rejectTravelRequest(selectedRequest.id, remarks)
                                // State management and navigation will happen via the LaunchedEffect
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
                            border = ButtonDefaults.outlinedBorder.copy(
                                brush = SolidColor(if (isLoading) Color.Gray else PrimaryRed)
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PrimaryRed,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Reject Request",
                                    color = PrimaryRed,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // No selected request
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No travel request selected",
                        fontFamily = GraphikFontFamily,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TravelStatus) {
    val (backgroundColor, textColor) = when (status) {
        TravelStatus.APPROVED -> Color(0xFF4CD964) to Color.White // Green
        TravelStatus.REJECTED -> PrimaryRed to Color.White // Red
        TravelStatus.PENDING -> Color(0xFFFFCC00) to Color.Black // Yellow
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
