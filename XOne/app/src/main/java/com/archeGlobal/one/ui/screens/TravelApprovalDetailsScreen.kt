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
import androidx.compose.ui.platform.LocalContext
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
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment

/**
 * Read-only details screen for approved / rejected travel requests.
 */
@Composable
fun TravelApprovalDetailsScreen(
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
                    .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
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
                    // Spacer to account for status bar
                    Spacer(modifier = Modifier.height(48.dp))

                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Travel Approval Details",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { controller.navigateBack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black,
                                )
                            }
                        },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        actions = { Spacer(modifier = Modifier.width(48.dp)) },
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
                        // Card containing all details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 2.dp,
                            backgroundColor = Color.White,
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header row – ID and status badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "#${travelRequest.id}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GraphikFontFamily,
                                    )
                                    StatusBadge(status = travelRequest.status)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow(label = "Employee", value = travelRequest.approver)
                                DetailRow(label = "Mobile", value = controller.mobileNumber)
                                // Show origin → destination format
                                val destinationValue =
                                    run {
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
                                DetailRow(label = "Origin → Destination", value = destinationValue)
                                DetailRow(label = "Project", value = travelRequest.project)
                                DetailRow(
                                    label = "Business Justification",
                                    value = travelRequest.businessJustification ?: "N/A",
                                )
                                DetailRow(
                                    label = "Date of Departure",
                                    value = travelRequest.departureDate ?: "N/A",
                                )
                                DetailRow(
                                    label = "Date of Arrival",
                                    value = travelRequest.arrivalDate ?: "N/A",
                                )
                                DetailRow(
                                    label = "Mode of Transport",
                                    value = travelRequest.modeOfTransport ?: "N/A",
                                )
                            }
                        }
                    }
                }
            }
        }
    } // Close FontScaleAdjusted block
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatusBadge(status: TravelStatus) {
    val (text, bgColor) =
        when (status) {
            TravelStatus.APPROVED -> "Approved" to Color(0xFF4CD964)
            TravelStatus.REJECTED -> "Rejected" to PrimaryRed
            else -> status.name.capitalize() to Color.LightGray
        }
    Box(
        modifier =
            Modifier
                .background(color = bgColor, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "Status: $text",
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = GraphikFontFamily,
        )
    }
}
