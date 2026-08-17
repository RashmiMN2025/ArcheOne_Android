package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.ExpenseController
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.ExpenseDetailUi
import com.archeGlobal.one.network.MileageExpenseItemUi
import com.archeGlobal.one.network.SubmittedExpenseUi
import com.archeGlobal.one.network.TeamMileageDashboardMetricsResponse
import com.archeGlobal.one.network.TravelRequestItemUi
import com.archeGlobal.one.network.isTravelExpenseDraft
import com.archeGlobal.one.network.isTravelExpenseSubmitted
import com.archeGlobal.one.network.travelExpenseStatusLabel
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.text.SimpleDateFormat
import java.util.Locale

private enum class ApprovalTab { TravelRequest, Expenses, Mileage }

@Composable
fun ExpenseApprovalViewScreen(onBack: () -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(ApprovalTab.TravelRequest) }
    val context = LocalContext.current
    val travelExpenseController = remember { TravelExpenseController(context) }
    val expenseController = remember { ExpenseController(context) }

    var selectedTravelRequest by rememberSaveable { mutableStateOf<TravelRequestItemUi?>(null) }
    var showTravelDetail by rememberSaveable { mutableStateOf(false) }
    var approvalAmountText by rememberSaveable { mutableStateOf("") }
    var reviewComment by rememberSaveable { mutableStateOf("") }
    var isReviewSubmitting by rememberSaveable { mutableStateOf(false) }
    var selectedExpenseDetail by remember { mutableStateOf<ExpenseDetailUi?>(null) }
    var selectedMileageExpenseId by rememberSaveable { mutableStateOf<Int?>(null) }

    if (selectedMileageExpenseId != null) {
        MileageApprovalDetailScreen(
            expenseId = selectedMileageExpenseId!!,
            onBack = {
                selectedMileageExpenseId = null
                travelExpenseController.fetchTeamMileageDashboardMetrics()
                travelExpenseController.fetchTeamMileageExpenses()
            },
        )
        return
    }

    if (selectedExpenseDetail != null) {
        BackHandler {
            selectedExpenseDetail = null
            expenseController.fetchApprovalExpenses()
        }
        ExpenseExtractionDetailViewScreen(
            expense = selectedExpenseDetail!!,
            isReadOnly = true,
            approvalMode = true,
            onBack = {
                selectedExpenseDetail = null
                expenseController.fetchApprovalExpenses()
            },
        )
        return
    }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            ApprovalTab.TravelRequest -> travelExpenseController.fetchTeamTrips()
            ApprovalTab.Expenses -> expenseController.fetchApprovalExpenses()
            ApprovalTab.Mileage -> {
                travelExpenseController.fetchTeamMileageDashboardMetrics()
                travelExpenseController.fetchTeamMileageExpenses()
            }
        }
    }

    val teamTrips = travelExpenseController.teamTrips.value
    val approvalExpenses = expenseController.approvalExpenses.value
    val teamMileageMetrics = travelExpenseController.teamMileageMetrics.value
    val mileageExpenses = travelExpenseController.teamMileageExpenses.value
    val isLoading = travelExpenseController.teamTripsLoading.value ||
        travelExpenseController.teamMileageMetricsLoading.value ||
        travelExpenseController.teamMileageExpensesLoading.value ||
        expenseController.approvalExpensesLoading.value

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Approvals",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = { Spacer(modifier = Modifier.size(48.dp)) }
            )

            Text(
                text = "Approvals",
                fontFamily = GraphikFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "All your pending tasks, reviews, and financial approvals in one place.",
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApprovalTabButton(
                    title = "Travel Request",
                    isSelected = selectedTab == ApprovalTab.TravelRequest,
                    onClick = { selectedTab = ApprovalTab.TravelRequest }
                )
                ApprovalTabButton(
                    title = "Expenses",
                    isSelected = selectedTab == ApprovalTab.Expenses,
                    onClick = { selectedTab = ApprovalTab.Expenses }
                )
                ApprovalTabButton(
                    title = "Mileage Expenses",
                    isSelected = selectedTab == ApprovalTab.Mileage,
                    onClick = { selectedTab = ApprovalTab.Mileage }
                )
            }

            when (selectedTab) {
                ApprovalTab.Expenses -> ExpenseApprovalList(
                    expenses = approvalExpenses,
                    isLoading = expenseController.approvalExpensesLoading.value,
                    errorMessage = expenseController.approvalExpensesError.value,
                    onViewDetails = { expense ->
                        val userExpenseId = expense.userExpenseId
                        if (userExpenseId == null) {
                            Toast.makeText(context, "Unable to identify this expense", Toast.LENGTH_LONG).show()
                        } else {
                            expenseController.fetchExpenseDetail(
                                expenseId = userExpenseId.toString(),
                                onSuccess = { detail -> selectedExpenseDetail = detail },
                                onError = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                },
                            )
                        }
                    },
                    onDownload = { expense ->
                        expenseController.downloadExpenseFile(
                            expenseId = expense.userExpenseId?.toString() ?: expense.id,
                            fallbackFileName = expense.name,
                            onSuccess = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() },
                        )
                    },
                )
                ApprovalTab.TravelRequest -> TravelApprovalList(
                    travels = teamTrips,
                    isLoading = travelExpenseController.teamTripsLoading.value,
                    errorMessage = travelExpenseController.teamTripsError.value,
                    onRequestClick = { request ->
                        selectedTravelRequest = request
                        approvalAmountText = request.firstAdvanceRequestedAmountRaw.takeIf { it.isNotBlank() } ?: ""
                        reviewComment = ""
                        isReviewSubmitting = false
                        showTravelDetail = true
                    }
                )
                ApprovalTab.Mileage -> MileageApprovalList(
                    metrics = teamMileageMetrics,
                    mileages = mileageExpenses,
                    isLoading = travelExpenseController.teamMileageExpensesLoading.value,
                    errorMessage = travelExpenseController.teamMileageExpensesError.value,
                    onViewMileage = { mileage -> selectedMileageExpenseId = mileage.expenseId }
                )
            }
        }

        if (isLoading && teamTrips.isEmpty() && approvalExpenses.isEmpty() && mileageExpenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        }

        if (expenseController.detailLoading.value || expenseController.downloadLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        }
    }

    if (showTravelDetail && selectedTravelRequest != null) {
        val request = selectedTravelRequest!!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showTravelDetail = false },
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 100.dp, bottom = 0.dp)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Review Travel Request TR-${request.requestId}",
                                fontFamily = GraphikFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Review the details of the travel request and provide your approval or rejection.",
                                fontFamily = GraphikFontFamily,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Divider(color = Color(0xFFEAEAEA))

                    ApprovalDetailPairRow(
                        leftTitle = "Employee",
                        leftValue = request.employeeName,
                        rightTitle = "Reporting Manager",
                        rightValue = request.reportingManager
                    )
                    ApprovalDetailPairRow(
                        leftTitle = "Destination",
                        leftValue = request.destination,
                        rightTitle = "Duration",
                        rightValue = formatDuration(request.startDate, request.endDate)
                    )
                    ApprovalDetailPairRow(
                        leftTitle = "Travel Dates",
                        leftValue = "${request.startDate} - ${request.endDate}",
                        rightTitle = "Submitted Date",
                        rightValue = formatDisplayDate(request.startDate)
                    )

                    ApprovalDetailPairRow(
                        leftTitle = "Purpose of Travel",
                        leftValue = request.description,
                        rightTitle = "",
                        rightValue = ""
                    )
                    ApprovalDetailPairRow(
                        leftTitle = "Estimated Total Cost",
                        leftValue = request.estimatedCost,
                        rightTitle = "Advance Requested",
                        rightValue = request.firstAdvanceRequestedAmount,
                        isBoldLeft = true,
                        isBoldRight = false
                    )
                    ApprovalDetailPairRow(
                        leftTitle ="Advance Type",
                        leftValue = if (request.advanceNeeded.equals("Yes", ignoreCase = true)) "Cash" else "N/A",
                        rightTitle = "",
                        rightValue = ""
                        )

                    Text(
                        text = "Approval Amount *",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    OutlinedTextField(
                        value = approvalAmountText,
                        onValueChange = { approvalAmountText = it.filter { char -> char.isDigit() || char == '.' || char == ',' } },
                        placeholder = { Text("Enter approved amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Comments",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("Comments (Required on Rejection)") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFDF2F2), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Note: Upon approval, a Trip ID will be generated automatically. The advance will need to be issued separately from the Advance Management section.",
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showTravelDetail = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF6F4EE), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isReviewSubmitting
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (reviewComment.trim().isBlank()) {
                                    Toast.makeText(context, "Comments is required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val parsedAmount = approvalAmountText.toDoubleOrNull()
                                isReviewSubmitting = true
                                travelExpenseController.updateTripStatus(
                                    tripId = request.tripId,
                                    approvedAmount = parsedAmount,
                                    comment = reviewComment.trim(),
                                    status = "rejected",
                                    onSuccess = {
                                        isReviewSubmitting = false
                                        showTravelDetail = false
                                        travelExpenseController.fetchTeamTrips()
                                        Toast.makeText(context, "Travel request rejected", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { message ->
                                        isReviewSubmitting = false
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF3E8E8), contentColor = PrimaryRed),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isReviewSubmitting
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = {
                                val parsedAmount = approvalAmountText.toDoubleOrNull()
                                isReviewSubmitting = true
                                travelExpenseController.updateTripStatus(
                                    tripId = request.tripId,
                                    approvedAmount = parsedAmount,
                                    comment = reviewComment.trim(),
                                    status = "approved",
                                    onSuccess = {
                                        isReviewSubmitting = false
                                        showTravelDetail = false
                                        travelExpenseController.fetchTeamTrips()
                                        Toast.makeText(context, "Travel request approved", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { message ->
                                        isReviewSubmitting = false
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryRed, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isReviewSubmitting
                        ) {
                            Text("Approve")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovalDetailPairRow(
    leftTitle: String,
    leftValue: String,
    rightTitle: String,
    rightValue: String,
    isBoldLeft: Boolean = false,
    isBoldRight: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leftTitle,
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = leftValue.takeIf { it.isNotBlank() } ?: "-",
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                fontWeight = if (isBoldLeft) FontWeight.SemiBold else FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rightTitle,
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = rightValue.takeIf { it.isNotBlank() } ?: "-",
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                fontWeight = if (isBoldRight) FontWeight.SemiBold else FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
}

private fun formatDuration(startDate: String, endDate: String): String {
    if (startDate.isBlank() || endDate.isBlank()) return "-"
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val start = formatter.parse(startDate)
        val end = formatter.parse(endDate)
        if (start != null && end != null) {
            val diffDays = ((end.time - start.time) / 86_400_000L) + 1
            if (diffDays <= 1L) "1 day" else "$diffDays days"
        } else {
            "-"
        }
    } catch (_: Exception) {
        "-"
    }
}

private fun formatDisplayDate(value: String): String {
    if (value.isBlank()) return "-"
    return try {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val parsedDate = inputFormatter.parse(value)
        parsedDate?.let { outputFormatter.format(it) } ?: value
    } catch (_: Exception) {
        value
    }
}

@Composable
private fun ApprovalTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = title,
        fontFamily = GraphikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = if (isSelected) Color.White else Color.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.9f))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun ExpenseApprovalList(
    expenses: List<SubmittedExpenseUi>,
    onViewDetails: (SubmittedExpenseUi) -> Unit,
    onDownload: (SubmittedExpenseUi) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Team expenses",
                fontFamily = GraphikFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (isLoading && expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        } else if (expenses.isEmpty()) {
            item {
                ApprovalEmptyState(
                    icon = Icons.Default.Description,
                    title = if (errorMessage.isNullOrBlank()) "No expense requests" else "Unable to load expenses",
                    message = errorMessage ?: "No approval requests were returned for the selected view."
                )
            }
        } else {
            items(expenses) { expense ->
                ExpenseApprovalCard(
                    expense = expense,
                    onViewDetails = { onViewDetails(expense) },
                    onDownload = { onDownload(expense) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TravelApprovalList(
    travels: List<TravelRequestItemUi>,
    isLoading: Boolean,
    errorMessage: String?,
    onRequestClick: (TravelRequestItemUi) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Team travel requests",
                fontFamily = GraphikFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (isLoading && travels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        } else if (travels.isEmpty()) {
            item {
                ApprovalEmptyState(
                    icon = Icons.Default.FlightTakeoff,
                    title = if (errorMessage.isNullOrBlank()) "No travel requests" else "Unable to load travel requests",
                    message = errorMessage ?: "No travel requests were returned for your team."
                )
            }
        } else {
            items(travels) { travel ->
                TravelApprovalCard(
                    travel = travel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onRequestClick = onRequestClick
                )
            }
        }
    }
}

@Composable
private fun MileageApprovalList(
    metrics: TeamMileageDashboardMetricsResponse?,
    mileages: List<MileageExpenseItemUi>,
    isLoading: Boolean,
    errorMessage: String?,
    onViewMileage: (MileageExpenseItemUi) -> Unit,
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    val filteredMileages = mileages.filter { mileage ->
        searchText.isBlank() ||
            mileage.id.contains(searchText, ignoreCase = true) ||
            mileage.customerName.contains(searchText, ignoreCase = true) ||
            mileage.startPoint.contains(searchText, ignoreCase = true) ||
            mileage.endPoint.contains(searchText, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MileageApprovalStatCard(
                    title = "Pending claims",
                    value = (metrics?.pendingCount ?: filteredMileages.size).toString(),
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                MileageApprovalStatCard(
                    title = "Total distance logged",
                    value = "${formatMetricValue(metrics?.totalDistance)} km",
                    icon = Icons.Default.Eco,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                MileageApprovalStatCard(
                    title = "Total claim amount",
                    value = "Rs ${formatMetricValue(metrics?.totalClaimAmount)}",
                    icon = Icons.Default.Description,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Team mileage submissions",
                    fontFamily = GraphikFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                    },
                    placeholder = {
                        Text(
                            text = "Search claims...",
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontSize = 15.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color(0xFFD4D4D4)
                    )
                )
            }
        }

        if (isLoading && mileages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        } else if (filteredMileages.isEmpty()) {
            item {
                ApprovalEmptyState(
                    icon = Icons.Default.DirectionsCar,
                    title = when {
                        !errorMessage.isNullOrBlank() -> "Unable to load mileage claims"
                        mileages.isNotEmpty() -> "No matching claims"
                        else -> "No mileage claims"
                    },
                    message = errorMessage
                        ?: if (mileages.isNotEmpty()) {
                            "No claims match \"$searchText\"."
                        } else {
                            "No mileage claims were returned for the team."
                        }
                )
            }
        } else {
            items(filteredMileages) { mileage ->
                MileageApprovalCard(
                    mileage = mileage,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onView = { onViewMileage(mileage) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseApprovalCard(
    expense: SubmittedExpenseUi,
    onViewDetails: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    ApprovalCardContainer(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EXP-${expense.userExpenseId}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            TravelStatusBadge(status = expense.status)
        }

        Divider(color = Color(0xFFEAEAEA))

        ApprovalDetailRow("Employee Name", expense.employeeName)
        ApprovalDetailRow("Employee ID", expense.employeeCode)
        ApprovalDetailRow("Category", expense.category)
        ApprovalDetailRow("Bill Date", formatDisplayDate(expense.billDate))
        ApprovalDetailRow("Description", expense.vendorName)
        ApprovalDetailRow("Amount", expense.amount, isBold = true)
        ApprovalDetailRow("Submission Date", formatDisplayDate(expense.submittedAt))

        Divider(color = Color(0xFFEAEAEA))

        ExpenseReviewActionRow(onViewDetails = onViewDetails, onDownload = onDownload)
    }
}

@Composable
private fun TravelApprovalCard(
    travel: TravelRequestItemUi,
    modifier: Modifier = Modifier,
    onRequestClick: (TravelRequestItemUi) -> Unit,
) {
    val shouldShowDetailAction = shouldShowTravelDetailAction(travel.status)
    val cardModifier = if (shouldShowDetailAction) {
        modifier.clickable { onRequestClick(travel) }
    } else {
        modifier

    }

    ApprovalCardContainer(modifier = cardModifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TR-${travel.requestId}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (travel.status.equals("approved", ignoreCase = true)) {
                    Text(
                        text = "Trip ID: ${travel.tripCode}",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            TravelStatusBadge(status = travel.status)
        }

        Divider(color = Color(0xFFEAEAEA), thickness = 1.dp)

        ApprovalDetailRow("Request ID", travel.requestId)
        if (travel.status.equals("approved", ignoreCase = true)) {
            ApprovalDetailRow("Trip ID", travel.tripCode)
        }
        ApprovalDetailRow("Project", travel.projectId)
        ApprovalDetailRow("Submitted By", travel.employeeName)
        ApprovalDetailRow("Destination", travel.destination)
        ApprovalDetailRow("Travel Dates", "${travel.startDate} - ${travel.endDate}")
        ApprovalDetailRow("Estimated Cost", travel.estimatedCost, isBold = true)
        ApprovalDetailRow("Mode of Travel", travel.modeOfTravel)
        ApprovalDetailRow("Hotel", travel.hotelNeeded)
        ApprovalDetailRow("Vehicle", travel.vehicleNeeded)
        ApprovalDetailRow("Advance", travel.advanceAmount)
        ApprovalDetailRow("Requested Amount", travel.firstAdvanceRequestedAmount)
        ApprovalDetailRow("Approved Amount", travel.approvedAmount)

        if (shouldShowDetailAction) {
            Divider(color = Color(0xFFEAEAEA), thickness = 1.dp)
            ReviewActionRow(actionText = "View", onClick = { onRequestClick(travel) })
        }
    }
}

fun shouldShowTravelDetailAction(status: String?): Boolean {
    return status?.equals("approved", ignoreCase = true) != true &&
        status?.equals("rejected", ignoreCase = true) != true
}

/** Mirrors the trip card on the mileage overview screen, with review as the only action. */
@Composable
private fun MileageApprovalCard(
    mileage: MileageExpenseItemUi,
    modifier: Modifier = Modifier,
    onView: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mileage.id,
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                MileageApprovalStatusBadge(status = mileage.status)
            }

            Divider(color = Color(0xFFEAEAEA))

            MileageApprovalDetailRow("Customer Name", mileage.customerName)
            MileageApprovalDetailRow("From", mileage.fromDate)
            MileageApprovalDetailRow("To", mileage.toDate)
            MileageApprovalDetailRow("Start", mileage.startPoint)
            MileageApprovalDetailRow("End", mileage.endPoint)
            MileageApprovalDetailRow("Type", mileage.type)
            MileageApprovalDetailRow("Vehicle", mileage.vehicle)
            MileageApprovalDetailRow("Amount", mileage.amount)
            MileageApprovalDetailRow("Distance", mileage.distance)

            Divider(color = Color(0xFFEAEAEA))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MileageApprovalActionButton(
                    icon = Icons.Default.Visibility,
                    label = "Review",
                    onClick = onView
                )
            }
        }
    }
}

@Composable
private fun MileageApprovalDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MileageApprovalActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = Color(0xFFF8F8F8)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = PrimaryRed,
                fontFamily = GraphikFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MileageApprovalStatusBadge(status: String) {
    val label = travelExpenseStatusLabel(status.takeIf { it.isNotBlank() } ?: "pending")
    val normalized = label.lowercase(Locale.getDefault())
    val (background, textColor) = when {
        isTravelExpenseDraft(normalized) -> Pair(Color(0xFFFFF9C4), Color(0xFF827717))
        isTravelExpenseSubmitted(normalized) -> Pair(Color(0xFFF5F5F5), Color(0xFF616161))
        normalized.contains("reject") -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }

    Text(
        text = label,
        color = textColor,
        fontFamily = GraphikFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun MileageApprovalStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 125.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                text = value,
                fontFamily = GraphikFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ApprovalCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ApprovalDetailRow(
    title: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReviewActionRow(actionText: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            tint = PrimaryRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = actionText,
            color = PrimaryRed,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ExpenseReviewActionRow(
    onViewDetails: () -> Unit,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "View Details",
            tint = PrimaryRed,
            modifier = Modifier
                .size(22.dp)
                .clickable { onViewDetails() },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download",
            tint = PrimaryRed,
            modifier = Modifier
                .size(22.dp)
                .clickable { onDownload() },
        )
    }
}

private fun getStatusColor(status: String): Color {
    val normalizedStatus = status.trim().ifBlank { "Pending" }
    return when {
        normalizedStatus.equals("Approved", ignoreCase = true) -> Color(0xFF2E7D32)
        normalizedStatus.equals("Rejected", ignoreCase = true) -> PrimaryRed
        else -> Color(0xFF757575)
    }
}

@Composable
private fun TravelStatusBadge(status: String) {
    val normalizedStatus = status.takeIf { it.isNotBlank() } ?: "Pending"
    val color = getStatusColor(normalizedStatus)

    Text(
        text = normalizedStatus.replaceFirstChar { it.uppercase() },
        color = color,
        fontFamily = GraphikFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

private fun formatMetricValue(value: String?): String {
    return value?.takeIf { it.isNotBlank() } ?: "0"
}

@Composable
private fun ApprovalEmptyState(
    icon: ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = title,
            fontFamily = GraphikFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = message,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
