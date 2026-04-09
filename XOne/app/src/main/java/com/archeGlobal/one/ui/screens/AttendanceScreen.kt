package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.AttendanceController
import com.archeGlobal.one.model.AttendanceDayStatus
import com.archeGlobal.one.model.LeaveBalance
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val primaryRed = Color(0xFFDD3825)

private fun typeColor(type: String): Color {
    val t = type.lowercase()
    return when {
        t.contains("outdoor")                                              -> Color(0xFFFF9800) // orange
        t.contains("regularisation") || t.contains("regularize") ||
            t.contains("regularised")                                      -> Color(0xFF9C27B0) // purple
        t.contains("wfh") || t.contains("work from home")                 -> Color(0xFF26C6B0) // mint
        t.contains("privilege")                                            -> Color(0xFFEC407A) // pink
        t.contains("paternity") || t.contains("maternity")                -> Color(0xFF8D6E63) // brown
        t.contains("casual")                                               -> Color(0xFF26A69A) // teal
        t.contains("sick")                                                 -> Color(0xFF5C6BC0) // indigo
        t.contains("optional")                                             -> Color(0xFF2196F3) // blue
        t.contains("probationary")                                         -> Color(0xFFFFCA28) // yellow
        t.contains("present")                                              -> Color(0xFF4CAF50) // green
        t.contains("absent") || t.contains("late")                        -> Color(0xFFDD3825) // red
        else                                                               -> Color(0xFF888888) // grey
    }
}

// Used for leave balance cards only
private fun leaveColor(type: String): Color {
    val t = type.lowercase()
    return when {
        t.contains("privilege")                        -> Color(0xFFEC407A) // pink
        t.contains("casual")                           -> Color(0xFF26A69A) // teal
        t.contains("sick")                             -> Color(0xFF5C6BC0) // indigo
        t.contains("optional")                         -> Color(0xFF2196F3) // blue
        t.contains("paternity") || t.contains("maternity") -> Color(0xFF8D6E63) // brown
        t.contains("wfh") || t.contains("work from home")  -> Color(0xFF26C6B0) // mint
        t.contains("probationary")                     -> Color(0xFFFFCA28) // yellow
        else                                           -> Color(0xFF2196F3) // blue default
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceScreen(
    controller: AttendanceController,
    onBack: () -> Unit,
    onLeaveCardClick: (String, LocalDate) -> Unit,
    onRegularizeClick: (LocalDate) -> Unit,
    onOutdoorDutyClick: (LocalDate) -> Unit,
    onWfhClick: (LocalDate) -> Unit = {},
    onHistoryClick: () -> Unit = {},
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAttendanceDialog by remember { mutableStateOf(false) }
    var longPressedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WelcomeBackgroundTop,
                            WelcomeBackgroundMiddle,
                            WelcomeBackgroundBottom,
                        ),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Attendance",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.Black,
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
                    actions = {
                        Row(
                            modifier = Modifier
                                .clickable { onHistoryClick() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "History",
                                color = primaryRed,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Attendance History",
                                tint = primaryRed,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                    ),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                ) {
                    // Calendar card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(onClick = controller::previousMonth) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowLeft,
                                            contentDescription = "Previous Month",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                    Text(
                                        text = "${controller.currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())} ${controller.currentMonth.year}",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = Color.Black,
                                    )
                                    IconButton(onClick = controller::nextMonth) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Next Month",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { label ->
                                        Text(
                                            text = label,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = Color(0xFF888888),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                CalendarGrid(
                                    yearMonth = controller.currentMonth,
                                    today = today,
                                    attendanceMap = controller.attendanceMap,
                                    attendanceTypeMap = controller.attendanceTypeMap,
                                    selectedDate = selectedDate,
                                    onDateClick = { date -> selectedDate = date },
                                    onDateLongClick = { date ->
                                        longPressedDate = date
                                        showAttendanceDialog = true
                                    },
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item {
                        Text(
                            text = "Leave Balance",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap on any leave type to apply",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    controller.leaveBalances.chunked(2).forEach { row ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                row.forEach { leave ->
                                    LeaveBalanceCard(
                                        leaveBalance = leave,
                                        dotColor = leaveColor(leave.type),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onLeaveCardClick(leave.type, selectedDate ?: today) },
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onRegularizeClick(selectedDate ?: today) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryRed),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = primaryRed,
                                ),
                            ) {
                                Text(
                                    text = "Regularize",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = primaryRed,
                                )
                            }
                            OutlinedButton(
                                onClick = { onOutdoorDutyClick(selectedDate ?: today) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryRed),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = primaryRed,
                                ),
                            ) {
                                Text(
                                    text = "Outdoor Duty",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = primaryRed,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onWfhClick(selectedDate ?: today) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                        ) {
                            Text(
                                text = "Work From Home",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
        UniversalLoader(isLoading = controller.isPageLoading)

        if (showAttendanceDialog && longPressedDate != null) {
            AttendanceDetailsDialog(
                date = longPressedDate!!,
                onDismiss = { showAttendanceDialog = false }
            )
        }
    }
}

@Composable
private fun AttendanceDetailsDialog(
    date: LocalDate,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
            ) {
                Text(
                    text = "Okay",
                    color = Color.White,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Attendance details",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Column 1
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AttendanceDetailRow(label = "Shift", value = "none")
                    AttendanceDetailRow(label = "Regularised", value = "none")
                    AttendanceDetailRow(label = "Deficit", value = "none")
                    AttendanceDetailRow(label = "Reason", value = "none")
                    AttendanceDetailRow(label = "Raw Swipes", value = "none")
                }

                // Column 2
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AttendanceDetailRow(label = "Actual", value = "none")
                    AttendanceDetailRow(label = "Hours", value = "00:00 hrs(00:00 hrs extra)")
                    AttendanceDetailRow(label = "Leave status", value = "none")
                    AttendanceDetailRow(label = "Application", value = "none")
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
private fun AttendanceDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    attendanceMap: Map<Int, AttendanceDayStatus>,
    attendanceTypeMap: Map<Int, String>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit,
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek
    val startOffset = when (firstDayOfMonth) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
    val totalRows = (startOffset + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until totalRows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - startOffset + 1
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (day in 1..daysInMonth) {
                            val date = yearMonth.atDay(day)
                            val isToday = date == today
                            val isSelected = date == selectedDate
                            val isWeekend =
                                date.dayOfWeek == DayOfWeek.SATURDAY ||
                                    date.dayOfWeek == DayOfWeek.SUNDAY
                            val status = attendanceMap[day]
                                ?: if (isWeekend) AttendanceDayStatus.WEEKEND else null
                            DayCell(
                                day = day,
                                isToday = isToday,
                                isSelected = isSelected,
                                status = status,
                                rawType = attendanceTypeMap[day],
                                onClick = { onDateClick(date) },
                                onLongClick = { onDateLongClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    status: AttendanceDayStatus?,
    rawType: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val todayBg = Color(0xFF64B5F6).copy(alpha = 0.5f)
    val selectedBg = Color(0xFFDD3825)
    val dotColor = when {
        status == AttendanceDayStatus.WEEKEND -> Color(0xFFCCCCCC)
        status == null -> Color.Transparent
        !rawType.isNullOrEmpty() -> typeColor(rawType)
        status == AttendanceDayStatus.PRESENT -> Color(0xFF4CAF50)
        status == AttendanceDayStatus.ABSENT  -> primaryRed
        status == AttendanceDayStatus.HOLIDAY -> Color(0xFFFF9800)
        else -> Color.Transparent
    }
    val circleBg = when {
        isToday && isSelected -> selectedBg
        isToday -> todayBg
        isSelected -> selectedBg
        else -> Color.Transparent
    }
    val textColor = when {
        isToday || isSelected -> Color.White
        status == AttendanceDayStatus.WEEKEND -> Color(0xFF999999)
        else -> Color.Black
    }
    val textWeight = FontWeight.SemiBold

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(circleBg),
        ) {
            Text(
                text = day.toString(),
                fontFamily = GraphikFontFamily,
                fontWeight = textWeight,
                fontSize = 14.sp,
                color = textColor,
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
    }
}

@Composable
private fun LeaveBalanceCard(
    leaveBalance: LeaveBalance,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = primaryRed,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomStart),
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .align(Alignment.TopEnd),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = String.format("%.2f", leaveBalance.balance),
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black,
                )
                Text(
                    text = leaveBalance.type,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                    lineHeight = 14.sp,
                )
            }
        }
    }
}
