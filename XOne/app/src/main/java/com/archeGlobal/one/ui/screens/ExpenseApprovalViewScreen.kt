package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

private enum class ApprovalTab { TravelRequest, Expenses, Mileage }

private data class ExpenseApprovalItem(
    val id: String,
    val category: String,
    val description: String,
    val payment: String,
    val amount: String,
    val date: String
)

private data class TravelApprovalItem(
    val id: String,
    val tripId: String,
    val projectId: String,
    val destination: String,
    val travelDates: String,
    val estimatedCost: String,
    val modeOfTravel: String,
    val hotelNeeded: String,
    val vehicleNeeded: String,
    val advanceAmount: String,
    val approvedAmount: String,
    val status: String,
    val action: String
)

private data class MileageApprovalItem(
    val id: String,
    val customerName: String,
    val date: String,
    val startPoint: String,
    val endPoint: String,
    val type: String,
    val vehicle: String,
    val amount: String,
    val distance: String,
    val status: String,
    val action: String
)

@Composable
fun ExpenseApprovalViewScreen(onBack: () -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(ApprovalTab.TravelRequest) }

    val expenseApprovals = remember {
        listOf(
            ExpenseApprovalItem(
                id = "EXP-3601",
                category = "Accommodation",
                description = "Hotel stay - Bangalore trip",
                payment = "Bank Transfer",
                amount = "Rs 12,500",
                date = "Jun 21, 2026"
            ),
            ExpenseApprovalItem(
                id = "EXP-3598",
                category = "Travel",
                description = "Flight - Mumbai to Delhi",
                payment = "Corporate Card",
                amount = "Rs 8,750",
                date = "Jun 19, 2026"
            )
        )
    }

    val travelApprovals = remember {
        listOf(
            TravelApprovalItem(
                id = "TRP-001",
                tripId = "TRP-001",
                projectId = "PROJ-001",
                destination = "Bangalore",
                travelDates = "12-15 Apr 2026",
                estimatedCost = "Rs 28,500",
                modeOfTravel = "Flight",
                hotelNeeded = "Yes",
                vehicleNeeded = "No",
                advanceAmount = "Rs 5,000",
                approvedAmount = "Rs 5,000",
                status = "Approved",
                action = "Review & Approve"
            ),
            TravelApprovalItem(
                id = "TRP-002",
                tripId = "TRP-002",
                projectId = "PROJ-002",
                destination = "Mumbai",
                travelDates = "28-30 Mar 2026",
                estimatedCost = "Rs 12,400",
                modeOfTravel = "Train",
                hotelNeeded = "Yes",
                vehicleNeeded = "Yes",
                advanceAmount = "-",
                approvedAmount = "-",
                status = "Pending",
                action = "Review Request"
            )
        )
    }

    val mileageApprovals = remember {
        listOf(
            MileageApprovalItem(
                id = "MLG-1002",
                customerName = "Priya Nair",
                date = "Jun 25, 2026",
                startPoint = "Hyderabad",
                endPoint = "Secunderabad",
                type = "Personal",
                vehicle = "Bike",
                amount = "Rs 480",
                distance = "28 km",
                status = "Pending",
                action = "Review Claim"
            )
        )
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
                ApprovalTab.Expenses -> ExpenseApprovalList(expenses = expenseApprovals)
                ApprovalTab.TravelRequest -> TravelApprovalList(travels = travelApprovals)
                ApprovalTab.Mileage -> MileageApprovalList(mileages = mileageApprovals)
            }
        }
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
private fun ExpenseApprovalList(expenses: List<ExpenseApprovalItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Teams expenses",
                fontFamily = GraphikFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (expenses.isEmpty()) {
            item {
                ApprovalEmptyState(
                    icon = Icons.Default.Description,
                    title = "Expense not found",
                    message = "We couldn't find any expense document for this entry."
                )
            }
        } else {
            items(expenses) { expense ->
                ExpenseApprovalCard(
                    expense = expense,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TravelApprovalList(travels: List<TravelApprovalItem>) {
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

        if (travels.isEmpty()) {
            item {
                ApprovalEmptyState(
                    icon = Icons.Default.FlightTakeoff,
                    title = "No travel request found",
                    message = "No travel request found for the current filters."
                )
            }
        } else {
            items(travels) { travel ->
                TravelApprovalCard(
                    travel = travel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MileageApprovalList(mileages: List<MileageApprovalItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ApprovalStatCard(
                    title = "Team Distance Traveled",
                    value = "0.00 km",
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier.weight(1f)
                )
                ApprovalStatCard(
                    title = "Carbon Emissions",
                    value = "0.00 Kg CO₂e",
                    icon = Icons.Default.Eco,
                    modifier = Modifier.weight(1f)
                )
                ApprovalStatCard(
                    title = "Total Claim Amount",
                    value = "Rs 0.00",
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Team mileage expenses",
                fontFamily = GraphikFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (mileages.isEmpty()) {
            item {
                ApprovalEmptyState(
                    icon = Icons.Default.DirectionsCar,
                    title = "No data",
                    message = "You haven't spent anything yet"
                )
            }
        } else {
            items(mileages) { mileage ->
                MileageApprovalCard(
                    mileage = mileage,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpenseApprovalCard(
    expense: ExpenseApprovalItem,
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
                    text = expense.description,
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "${expense.category} • ${expense.date}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            PendingStatusBadge()
        }

        Divider(color = Color(0xFFEAEAEA))

        ApprovalDetailRow("Payment", expense.payment)
        ApprovalDetailRow("Amount", expense.amount, isBold = true)
        ApprovalDetailRow("ID", expense.id)

        ReviewActionRow(actionText = "Review")
    }
}

@Composable
private fun TravelApprovalCard(
    travel: TravelApprovalItem,
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
                    text = "${travel.destination} Trip",
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "TRP-${travel.tripId} • ${travel.travelDates}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            TravelStatusBadge(status = travel.status)
        }

        Divider(color = Color(0xFFEAEAEA))

        ApprovalDetailRow("Project", travel.projectId)
        ApprovalDetailRow("Mode", travel.modeOfTravel)
        ApprovalDetailRow("Estimated Cost", travel.estimatedCost, isBold = true)
        ApprovalDetailRow("Hotel Needed", travel.hotelNeeded)
        ApprovalDetailRow("Vehicle Needed", travel.vehicleNeeded)
        ApprovalDetailRow("Advance", travel.advanceAmount)
        ApprovalDetailRow("Approved Amount", travel.approvedAmount)
        ApprovalDetailRow("Action", travel.action)

        ReviewActionRow(actionText = travel.action)
    }
}

@Composable
private fun MileageApprovalCard(
    mileage: MileageApprovalItem,
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
                    text = mileage.customerName,
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "${mileage.startPoint} → ${mileage.endPoint}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            PendingStatusBadge()
        }

        Divider(color = Color(0xFFEAEAEA))

        ApprovalDetailRow("Date", mileage.date)
        ApprovalDetailRow("Distance", mileage.distance)
        ApprovalDetailRow("Vehicle", mileage.vehicle)
        ApprovalDetailRow("Type", mileage.type)
        ApprovalDetailRow("Amount", mileage.amount, isBold = true)
        ApprovalDetailRow("Action", mileage.action)

        ReviewActionRow(actionText = mileage.action)
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
    isBold: Boolean = false
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
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReviewActionRow(actionText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
private fun PendingStatusBadge() {
    Text(
        text = "Pending",
        color = Color(0xFFEF6C00),
        fontFamily = GraphikFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(Color(0xFFEF6C00).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun TravelStatusBadge(status: String) {
    val isApproved = status.equals("Approved", ignoreCase = true)
    val color = if (isApproved) Color(0xFF2E7D32) else Color(0xFFEF6C00)

    Text(
        text = status,
        color = color,
        fontFamily = GraphikFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun ApprovalStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                fontFamily = GraphikFontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
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
