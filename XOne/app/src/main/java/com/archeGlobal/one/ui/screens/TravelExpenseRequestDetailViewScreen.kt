package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.ProjectOption
import com.archeGlobal.one.network.TravelRequestItemUi
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@Composable
fun TravelExpenseRequestDetailViewScreen(
    controller: TravelExpenseController,
    request: TravelRequestItemUi,
    onBack: () -> Unit,
    projectOptions: List<ProjectOption>,
    projectOptionsLoading: Boolean,
    projectOptionsError: String?,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showEditBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(projectOptions, projectOptionsLoading) {
        if (projectOptions.isEmpty() && !projectOptionsLoading) {
            controller.fetchProjectOptions()
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
                        WelcomeBackgroundBottom,
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        text = "Travel Request Details",
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = {}
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TravelSummaryCard(
                    requestId = request.requestId,
                    destination = request.destination,
                    status = request.status,
                    travelDates = request.travelDates,
                )

                ExpenseRequestDetailCard(title = "Request Information") {
                    TravelDetailRow(label = "Request ID", value = "#TR-${request.requestId}")
                    TravelDetailRow(label = "Trip ID", value = request.tripCode)
                    TravelDetailRow(label = "Project Code", value = request.projectId)
                    TravelDetailRow(label = "Destination", value = request.destination)
                    TravelDetailRow(label = "Description", value = request.description)
                }

                ExpenseRequestDetailCard(title = "Employee") {
                    TravelDetailRow(label = "Employee", value = request.employeeName)
                    TravelDetailRow(label = "Email", value = request.employeeEmail)
                }

                ExpenseRequestDetailCard(title = "Travel Details") {
                    TravelDetailRow(label = "Travel Dates", value = request.travelDates)
                    TravelDetailRow(label = "Mode of Travel", value = request.modeOfTravel)
                    TravelDetailRow(label = "Hotel Needed", value = request.hotelNeeded)
                    TravelDetailRow(label = "Vehicle Needed", value = request.vehicleNeeded)
                }

                ExpenseRequestDetailCard(title = "Financials") {
                    TravelDetailRow(label = "Estimated Cost", value = request.estimatedCost)
                    TravelDetailRow(label = "Advance Needed", value = request.advanceNeeded)
                    TravelDetailRow(label = "Advance Amount", value = request.firstAdvanceRequestedAmount)
                    TravelDetailRow(label = "Approved Amount", value = request.approvedAmount)
                    TravelDetailRow(label = "Total Requested", value = request.totalRequestedAmount)
                    TravelDetailRow(label = "Total Approved", value = request.totalApprovedAmount)
                    TravelDetailRow(label = "Total Issued", value = request.totalIssuedAmount)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showEditBottomSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Update",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                    Button(
                        onClick = {
                            controller.deleteTrip(
                                request.tripId,
                                onSuccess = { onBack() },
                                onError = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFF6F4EE),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Delete",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    if (showEditBottomSheet) {
        CreateTravelRequestBottomSheet(
            controller = controller,
            onDismiss = { showEditBottomSheet = false },
            onRequestCreated = { showEditBottomSheet = false },
            projectOptions = projectOptions,
            projectOptionsLoading = projectOptionsLoading,
            projectOptionsError = projectOptionsError,
            isEditMode = true,
            initialRequest = request
        )
    }
}

@Composable
private fun TravelSummaryCard(
    requestId: String,
    destination: String,
    status: String,
    travelDates: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color(0xFFF6F4EE),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "#TR-$requestId",
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )
                }
                StatusBadge(status = status)
            }

            TravelDetailRow(label = "Travel Dates", value = travelDates)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    Text(
        text = status.replaceFirstChar { it.uppercase() },
        color = when (status.lowercase()) {
            "approved" -> Color(0xFF2E7D32)
            "pending", "submitted" -> Color(0xFFEF6C00)
            "rejected" -> PrimaryRed
            else -> Color.Gray
        },
        fontFamily = GraphikFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(
                when (status.lowercase()) {
                    "approved" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                    "pending", "submitted" -> Color(0xFFEF6C00).copy(alpha = 0.15f)
                    "rejected" -> PrimaryRed.copy(alpha = 0.15f)
                    else -> Color.Gray.copy(alpha = 0.15f)
                },
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun ExpenseRequestDetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = Color(0xFFF6F4EE),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            )
            content()
        }
    }
}

@Composable
private fun TravelDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}
