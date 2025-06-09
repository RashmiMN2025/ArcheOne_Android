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
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@Composable
fun TravelRequestDetailScreen(
    controller: TravelController,
    travelRequest: TravelRequest
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                    DetailRow(label = "Business Justification:", value = travelRequest.businessJustification ?: "N/A")
                    DetailRow(label = "Mode of Transport:", value = travelRequest.modeOfTransport ?: "N/A")
                    DetailRow(label = "Departure Date:", value = travelRequest.departureDate ?: "N/A")
                    DetailRow(label = "Arrival Date:", value = travelRequest.arrivalDate ?: "N/A")
                    // Format the created date for display
                    val formattedDate = try {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(travelRequest.createdDate)
                    } catch (e: Exception) {
                        "N/A"
                    }
                    DetailRow(label = "Request Date:", value = formattedDate)
                }
                
                // Approval Details Card
                DetailCard(title = "Approval Details") {
                    DetailRow(
                        label = "Status:",
                        value = travelRequest.status.name.capitalize(),
                        valueColor = when (travelRequest.status) {
                            TravelStatus.APPROVED -> Color.Black
                            TravelStatus.REJECTED -> Color.Black
                            else -> Color.Black
                        }
                    )
                    DetailRow(label = "Approver:", value = travelRequest.approver)
                    DetailRow(label = "Reporting Manager:", value = controller.reportingManagerName)
                    DetailRow(label = "Manager Email:", value = controller.reportingManagerEmail)
                }
            }
        }
    }
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
