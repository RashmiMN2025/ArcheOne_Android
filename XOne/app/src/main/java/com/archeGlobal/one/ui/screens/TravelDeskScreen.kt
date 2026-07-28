package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
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
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

private data class ExpenseTile(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun TravelDeskScreen(controller: TravelController) {
    var openTravelRequestScreen by rememberSaveable { mutableStateOf(false) }
    var openExpenseExtractionScreen by rememberSaveable { mutableStateOf(false) }
    var openMileageCalculatorScreen by rememberSaveable { mutableStateOf(false) }
    var openExpenseApprovalScreen by rememberSaveable { mutableStateOf(false) }

    if (openTravelRequestScreen) {
        TravelExpenseRequestViewScreen(
            onBack = { openTravelRequestScreen = false }
        )
        return
    }

    if (openExpenseExtractionScreen) {
        ExpenseExtractionViewScreen(
            onBack = { openExpenseExtractionScreen = false }
        )
        return
    }

    if (openMileageCalculatorScreen) {
        MileageCalculatorViewScreen(
            onBack = { openMileageCalculatorScreen = false }
        )
        return
    }

    if (openExpenseApprovalScreen) {
        ExpenseApprovalViewScreen(
            onBack = { openExpenseApprovalScreen = false }
        )
        return
    }

    val userName = controller.employeeName.ifBlank { "Guest User" }
    val userEmployeeId = controller.employeeId.ifBlank { "--" }
    val userGrade = controller.employeeGrade.ifBlank { "--" }

    val tiles = buildList {
        add(
            ExpenseTile(
                title = "Travel Request",
                description = "Submit new travel expense requests",
                icon = Icons.Default.FlightTakeoff,
                onClick = { openTravelRequestScreen = true }
            )
        )
        add(
            ExpenseTile(
                title = "My Expense",
                description = "View and manage my expenses",
                icon = Icons.Default.Assignment,
                onClick = { openExpenseExtractionScreen = true }
            )
        )
        add(
            ExpenseTile(
                title = "Mileage Calculator",
                description = "Estimate trip reimbursement",
                icon = Icons.Default.DirectionsCar,
                onClick = { openMileageCalculatorScreen = true }
            )
        )
        if (controller.canViewExpenseApprovals) {
            add(
                ExpenseTile(
                    title = "Approvals",
                    description = "Review pending approvals",
                    icon = Icons.Default.CheckCircle,
                    onClick = { openExpenseApprovalScreen = true }
                )
            )
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
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Expense Desk",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
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
                actions = { Spacer(modifier = Modifier.size(48.dp)) }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                        Text(
                            text = "Expense Desk",
                            fontSize = 28.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Manage travel expenses, mileage, approvals and reports from one place.",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EmployeeIntroCard(
                            userName = userName,
                            employeeId = userEmployeeId,
                            grade = userGrade
                        )
                    }
                }

                items(tiles) { tile ->
                    ExpenseModuleTile(tile = tile)
                }
            }
        }
    }
}

@Composable
private fun EmployeeIntroCard(
    userName: String,
    employeeId: String,
    grade: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = userName,
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "Employee Id: $employeeId",
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Grade: $grade",
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Ready to file a new request?",
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ExpenseModuleTile(tile: ExpenseTile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { tile.onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(PrimaryRed.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.title,
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tile.title,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tile.description,
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
