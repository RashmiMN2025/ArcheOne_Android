package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val primaryRed = Color(0xFFDD3825)

private val leaveColors = listOf(
    Color(0xFF2196F3), // blue
    Color(0xFF4CAF50), // green
    Color(0xFF00BCD4), // teal
    Color(0xFF9C27B0), // purple
    Color(0xFF2196F3), // blue
    Color(0xFF00BCD4), // teal
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceScreen(
    controller: AttendanceController,
    onBack: () -> Unit,
    onLeaveCardClick: (String) -> Unit,
    onRegularizeClick: () -> Unit,
    onOutdoorDutyClick: () -> Unit,
) {
    val today = LocalDate.now()

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
                                        fontWeight = FontWeight.Bold,
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
                            fontWeight = FontWeight.Bold,
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

                    controller.leaveBalances.chunked(2).forEachIndexed { rowIndex, row ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                row.forEachIndexed { colIndex, leave ->
                                    val colorIndex = rowIndex * 2 + colIndex
                                    LeaveBalanceCard(
                                        leaveBalance = leave,
                                        dotColor = leaveColors.getOrElse(colorIndex) { Color(0xFF888888) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onLeaveCardClick(leave.type) },
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
                                onClick = onRegularizeClick,
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = primaryRed,
                                )
                            }
                            OutlinedButton(
                                onClick = onOutdoorDutyClick,
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = primaryRed,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    attendanceMap: Map<Int, AttendanceDayStatus>,
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
                            val isWeekend =
                                date.dayOfWeek == DayOfWeek.SATURDAY ||
                                    date.dayOfWeek == DayOfWeek.SUNDAY
                            val status = attendanceMap[day]
                                ?: if (isWeekend) AttendanceDayStatus.WEEKEND else null
                            DayCell(day = day, isToday = isToday, status = status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    status: AttendanceDayStatus?,
) {
    val todayBg = Color(0xFFDD3825)
    val dotColor = when (status) {
        AttendanceDayStatus.PRESENT -> Color(0xFF4CAF50)
        AttendanceDayStatus.ABSENT -> primaryRed
        AttendanceDayStatus.LATE -> Color(0xFFF5A623)
        AttendanceDayStatus.LEAVE -> Color(0xFF9C27B0)
        AttendanceDayStatus.HOLIDAY -> Color(0xFFFF9800)
        AttendanceDayStatus.WEEKEND -> Color(0xFFCCCCCC)
        null -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isToday) todayBg else Color.Transparent),
        ) {
            Text(
                text = day.toString(),
                fontFamily = GraphikFontFamily,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                color = if (isToday) Color.White else Color.Black,
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
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
        border = BorderStroke(1.dp, primaryRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    fontWeight = FontWeight.Bold,
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
