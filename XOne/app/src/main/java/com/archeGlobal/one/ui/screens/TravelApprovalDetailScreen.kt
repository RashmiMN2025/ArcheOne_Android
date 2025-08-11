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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                .systemBarsPadding()
        ) {
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(48.dp))

                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Travel Approval Details",
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp,
                        backgroundColor = Color(0xFFF6F4EE)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            // Title
                            Text(
                                text = "Approval Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GraphikFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Request details
                            DetailRow(label = "Request ID:", value = travelRequest.id)
                            DetailRow(label = "Employee:", value = travelRequest.approver)
                            // Show origin → destination format
                            val destinationValue = run {
                                val destinations = travelRequest.getAllDestinations()
                                if (destinations.isNotEmpty()) {
                                    val destination = destinations[0]
                                    if (!destination.originCity.isNullOrEmpty()) {
                                        "${destination.originCity} → ${destination.destinationCity}"
                                    } else {
                                        destination.destinationCity
                                    }
                                } else {
                                    travelRequest.destination // Fallback
                                }
                            }
                            DetailRow(label = "Origin → Destination:", value = destinationValue)
                            DetailRow(label = "Project:", value = travelRequest.project)
                            DetailRow(label = "Business Justification:", value = travelRequest.businessJustification ?: "N/A")
                            DetailRow(label = "Departure Date:", value = travelRequest.departureDate ?: "N/A")
                            DetailRow(label = "Return Date:", value = travelRequest.arrivalDate ?: "N/A")
                            DetailRow(label = "Transport Mode:", value = travelRequest.modeOfTransport ?: "N/A")
                            DetailRow(label = "Status:", value = travelRequest.status.name)
                            DetailRow(label = "Created Date:", value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(travelRequest.createdDate))

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action buttons
                            if (travelRequest.status == TravelStatus.PENDING) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { controller.navigateToTravelApprove(travelRequest) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Approve",
                                            color = Color.White,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Button(
                                        onClick = { controller.navigateToTravelReject(travelRequest) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = PrimaryRed
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Reject",
                                            color = Color.White,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
    }
}

private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

/**
 * Format date string to "d MMM yyyy" format (e.g., "10 Jun 2025")
 */
private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "N/A"

    try {
        // Parse the input date string (assuming it's in a standard format like yyyy-MM-dd)
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val date = inputFormat.parse(dateString)

        // Format to the desired output format
        val outputFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.US)
        return date?.let { outputFormat.format(it) } ?: "N/A"
    } catch (e: Exception) {
        // If parsing fails, return the original string
        return dateString
    }
}
