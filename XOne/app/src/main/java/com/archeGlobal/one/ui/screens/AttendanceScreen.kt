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
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
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
import com.archeGlobal.one.model.AttendanceDayData
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

private fun typeColor(type: String, requestStatus: String = ""): Color {
    val t = type.lowercase()
    val s = requestStatus.lowercase()

    // attendanceStatus overrides — checked first regardless of request status
    if (t.contains("present")) return Color(0xFF4CAF50)                   // green
    if (t.contains("absent") || t.contains("late")) return Color(0xFFDD3825) // red

    // request status gates
    if (s == "rejected") return Color.Transparent
    if (s == "pending")  return Color(0xFFCCCCCC)                          // light grey (weekend shade)

    // approved (or no request) — colour by type
    return when {
        t.contains("outdoor")                                                  -> Color(0xFFFFA500) // #FFA500
        t.contains("regularisation") || t.contains("regularize") ||
            t.contains("regularised")                                          -> Color(0xFF4CAF50) // green
        t.contains("wfh") || t.contains("work from home")                     -> Color(0xFF3EB489) // #3EB489
        t.contains("privilege")                                                -> Color(0xFFC7EA46) // #C7EA46
        t.contains("paternity") || t.contains("maternity")                    -> Color(0xFF8B4513) // #8B4513
        t.contains("casual")                                                   -> Color(0xFF20C997) // #20C997
        t.contains("sick")                                                     -> Color(0xFF4B0082) // #4B0082
        t.contains("optional")                                                 -> Color(0xFF007BFF) // #007BFF
        t.contains("probationary")                                             -> Color(0xFFFFD700) // #FFD700
        t.contains("knt women wellness leave")             -> Color(0xFFFF69B4) // #FF69B4
        else                                                                   -> Color(0xFFA0A0A0) // #A0A0A0
    }
}

// Used for leave balance cards only
private fun leaveColor(type: String): Color {
    val t = type.lowercase()
    return when {
        t.contains("privilege")                             -> Color(0xFFC7EA46) // #C7EA46
        t.contains("casual")                               -> Color(0xFF20C997) // #20C997
        t.contains("sick")                                 -> Color(0xFF4B0082) // #4B0082
        t.contains("optional")                             -> Color(0xFF007BFF) // #007BFF
        t.contains("paternity") || t.contains("maternity") -> Color(0xFF8B4513) // #8B4513
        t.contains("wfh") || t.contains("work from home")  -> Color(0xFF3EB489) // #3EB489
        t.contains("probationary")                         -> Color(0xFFFFD700) // #FFD700
        t.contains("knt women wellness leave")             -> Color(0xFFFF69B4) // #FF69B4
        else                                               -> Color(0xFF007BFF) // #007BFF
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
    val context = LocalContext.current
    var showAttendanceDialog by remember { mutableStateOf(false) }
    var longPressedDate by remember { mutableStateOf<LocalDate?>(null) }
    val isSelectedDateHoliday = controller.selectedDate?.let {
        controller.getHolidayNameForDate(it)
    } != null

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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
                ) {
                    // Attendance Summary
                    item {
                        AttendanceSummarySection(
                            attendanceMap = controller.attendanceMap,
                            attendanceTypeMap = controller.attendanceTypeMap,
                            holidayFileUrl = controller.holidayFileUrl,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Calendar card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
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
                                            modifier = Modifier.size(32.dp),
                                        )
                                    }
                                    Text(
                                        text = "${controller.currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())} ${controller.currentMonth.year}",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 21.sp,
                                        color = Color.Black,
                                    )
                                    IconButton(onClick = controller::nextMonth) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Next Month",
                                            tint = Color.Black,
                                            modifier = Modifier.size(32.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { label ->
                                        Text(
                                            text = label,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
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
                                    attendanceRequestStatusMap = controller.attendanceRequestStatusMap,
                                    calendarHolidayDays = controller.calendarHolidayDays,
                                    selectedDate = controller.selectedDate,
                                    onDateClick = { date ->
                                        if (date == controller.selectedDate) {
                                            // Second tap on the already-selected date → open details
                                            longPressedDate = date
                                            controller.fetchAttendanceForDate(date)
                                            showAttendanceDialog = true
                                        } else {
                                            // First tap on a new date → just select it
                                            controller.selectedDate = date
                                        }
                                    },
                                    onDateLongClick = { date ->
                                        longPressedDate = date
                                        controller.fetchAttendanceForDate(date)
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
                            color = Color.Black,
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
                                            .clickable {
                                                if (isSelectedDateHoliday) {
                                                    Toast.makeText(context, "Cannot apply leave on a holiday", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onLeaveCardClick(leave.type, controller.selectedDate ?: today)
                                                }
                                            },
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
                                onClick = {
                                    if (isSelectedDateHoliday) {
                                        Toast.makeText(context, "Cannot apply leave on a holiday", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onWfhClick(controller.selectedDate ?: today)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryRed),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = primaryRed,
                                ),
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "Work From Home",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = primaryRed,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 8.dp, end = 10.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF26C6B0)),
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { onOutdoorDutyClick(controller.selectedDate ?: today) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryRed),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = primaryRed,
                                ),
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "Outdoor Duty",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = primaryRed,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 8.dp, end = 10.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF9800)),
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (isSelectedDateHoliday) {
                                    Toast.makeText(context, "Cannot apply regularization on a holiday", Toast.LENGTH_SHORT).show()
                                } else {
                                    onRegularizeClick(controller.selectedDate ?: today)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                        ) {
                            Text(
                                text = "Apply For Regularization",
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
                records = controller.selectedDateRecords,
                isLoading = controller.isDetailLoading,
                holidayName = controller.getHolidayNameForDate(longPressedDate!!),
                onDismiss = { showAttendanceDialog = false }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AttendanceDetailsDialog(
    date: LocalDate,
    records: List<AttendanceDayData>,
    isLoading: Boolean,
    holidayName: String? = null,
    onDismiss: () -> Unit,
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
                    fontSize = 16.sp,
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
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFCCCCCC), thickness = 1.dp)
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = primaryRed,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(32.dp),
                    )
                }
            } else {
                val allRequests = records.flatMap { it.requests ?: emptyList() }.distinctBy { it.id }
                val punchRecords = records.filter { !it.punchIn.isNullOrEmpty() }.sortedBy { it.punchIn }
                val firstIn = formatTo12h(punchRecords.firstOrNull()?.punchIn ?: "None")
                val lastOut = formatTo12h(punchRecords.lastOrNull { !it.punchOut.isNullOrEmpty() }?.punchOut?.lastOrNull() ?: "None")
                val isWorkingDay = date.dayOfWeek != java.time.DayOfWeek.SATURDAY &&
                    date.dayOfWeek != java.time.DayOfWeek.SUNDAY
                val apiWorkingHours = records.firstOrNull { it.workingHours != null && it.workingHours != "00:00" }?.workingHours
                    ?: records.firstOrNull()?.workingHours
                val workingHours = when {
                    firstIn == "None" && isWorkingDay && holidayName == null -> "00:00 hrs"
                    apiWorkingHours != null -> apiWorkingHours
                    else -> "None"
                }
                val primaryRequest = allRequests.firstOrNull()
                val leaveType = primaryRequest?.requestType ?: "None"
                val leaveStatus = primaryRequest?.status ?: "None"
                val hasPendingRequest = allRequests.any { it.status?.equals("pending", ignoreCase = true) == true }
                val regularisation = allRequests.firstOrNull { it.requestType?.contains("Regularisation", ignoreCase = true) == true }
                val reason = allRequests.firstNotNullOfOrNull { it.reason?.ifEmpty { null } } ?: "None"
                val rawSwipes = punchRecords.flatMap { record ->
                    listOfNotNull(record.punchIn) + (record.punchOut ?: emptyList())
                }.joinToString(", ").ifEmpty { "None" }

                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    // Column 1
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AttendanceDetailRow(label = "Shift", value = if (date.dayOfWeek == java.time.DayOfWeek.SATURDAY || date.dayOfWeek == java.time.DayOfWeek.SUNDAY) "None" else "9:30 am to 6:30 pm")
                        AttendanceDetailRow(label = "Regularised", value = regularisation?.status ?: "None")
                        val attendanceStatusValue = when {
                            date == java.time.LocalDate.now() || hasPendingRequest -> "None"
                            isWorkingDay && holidayName == null && rawSwipes == "None" &&
                                (records.isEmpty() || records.firstOrNull()?.attendanceStatus.isNullOrBlank()) -> "Absent"
                            else -> records.firstOrNull()?.attendanceStatus?.takeIf { it.isNotBlank() } ?: "None"
                        }
                        AttendanceDetailRow(label = "Attendance Status", value = attendanceStatusValue)
                        AttendanceDetailRow(label = "Reason", value = reason)
                        AttendanceDetailRow(label = "Raw Swipes", value = rawSwipes)
                    }

                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFCCCCCC)),
                    )

                    // Column 2
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AttendanceDetailRow(label = "Actual", value = if (firstIn != "None") "$firstIn - $lastOut" else "None")
                        AttendanceDetailRow(label = "Hours", value = workingHours)
                        AttendanceDetailRow(label = "Leave type", value = leaveType)
                        AttendanceDetailRow(label = "Leave status", value = leaveStatus)
                        if (holidayName != null) {
                            AttendanceDetailRow(label = "Holiday", value = holidayName)
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
    )
}

private fun formatTo12h(time: String): String {
    if (time == "None" || time.isBlank()) return time
    val formats = listOf("HH:mm:ss", "HH:mm", "yyyy-MM-dd HH:mm:ss")
    val out = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    for (pattern in formats) {
        try {
            val parsed = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).parse(time)
            if (parsed != null) return out.format(parsed)
        } catch (_: Exception) {}
    }
    return time
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
            fontWeight = FontWeight.SemiBold,
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
    attendanceRequestStatusMap: Map<Int, String>,
    calendarHolidayDays: Set<Int>,
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
                                requestStatus = attendanceRequestStatusMap[day] ?: "",
                                isCalendarHoliday = day in calendarHolidayDays,
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
    requestStatus: String = "",
    isCalendarHoliday: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val todayBg = Color(0xFF64B5F6).copy(alpha = 0.5f)
    val selectedBg = Color(0xFFDD3825)
    val holidayBlue = Color(0xFF007BFF)
    val dotColor = when {
        isToday -> Color.Transparent
        status == AttendanceDayStatus.WEEKEND -> Color(0xFFAAAAAA)
        // Calendar holiday: blue unless the employee actually worked (PRESENT) or took leave (LEAVE)
        isCalendarHoliday && status != AttendanceDayStatus.PRESENT && status != AttendanceDayStatus.LEAVE -> holidayBlue
        status == null -> Color.Transparent
        // Rejected request: ignore the request type and show the actual attendance status colour
        requestStatus.lowercase() == "rejected" -> when (status) {
            AttendanceDayStatus.PRESENT -> Color(0xFF4CAF50)
            AttendanceDayStatus.ABSENT  -> primaryRed
            AttendanceDayStatus.HOLIDAY -> holidayBlue
            else -> primaryRed // LEAVE/LATE with rejected request → still absent
        }
        !rawType.isNullOrEmpty() -> typeColor(rawType, requestStatus)
        status == AttendanceDayStatus.PRESENT -> Color(0xFF4CAF50)
        status == AttendanceDayStatus.ABSENT  -> primaryRed
        status == AttendanceDayStatus.HOLIDAY -> holidayBlue
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
        status == AttendanceDayStatus.WEEKEND -> Color(0xFFAAAAAA)
        else -> Color.Black
    }
    val textWeight = FontWeight.SemiBold

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 4.8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(circleBg),
        ) {
            Text(
                text = day.toString(),
                fontFamily = GraphikFontFamily,
                fontWeight = textWeight,
                fontSize = 17.sp,
                color = textColor,
            )
        }
        Spacer(modifier = Modifier.height(9.6.dp))
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
    }
}

@Composable
private fun AttendanceSummarySection(
    attendanceMap: Map<Int, AttendanceDayStatus>,
    attendanceTypeMap: Map<Int, String>,
    holidayFileUrl: String?,
) {
    val absentsCount = attendanceMap.count { (day, status) ->
        status == AttendanceDayStatus.ABSENT &&
            attendanceTypeMap[day]?.lowercase()?.contains("late") != true
    }
    val lateInCount = attendanceTypeMap.count { (_, type) ->
        type.lowercase().contains("late")
    }
    val onLeaveCount = attendanceMap.count { (_, status) ->
        status == AttendanceDayStatus.LEAVE
    }
    val workDaysCount = attendanceMap.count { (_, status) ->
        status == AttendanceDayStatus.PRESENT
    }
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Attendance Summary",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
            )
            Text(
                text = "Holiday List",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = primaryRed,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val url = holidayFileUrl
                    if (url.isNullOrBlank()) {
                        Toast.makeText(context, "Holiday list not available", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = android.content.Intent(context, com.archeGlobal.one.WebViewActivity::class.java)
                        intent.putExtra("fileUrl", url)
                        intent.putExtra("title", "Holiday List")
                        context.startActivity(intent)
                    }
                },
            )
        }
        Text(
            text = "Monthly overview",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryCard(
                iconRes = R.drawable.absents,
                iconTint = Color(0xFFDD3825),
                iconBg = Color(0xFFFFEBEE),
                count = absentsCount,
                label = "Absents",
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                iconRes = R.drawable.latein,
                iconTint = Color(0xFFFF9800),
                iconBg = Color(0xFFFFF3E0),
                count = lateInCount,
                label = "Late In",
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                iconRes = R.drawable.onleave,
                iconTint = Color(0xFF26A69A),
                iconBg = Color(0xFFE0F2F1),
                count = onLeaveCount,
                label = "On Leave",
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                iconRes = R.drawable.workdays,
                iconTint = Color(0xFF388E3C),
                iconBg = Color(0xFFE8F5E9),
                count = workDaysCount,
                label = "Work Days",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    @androidx.annotation.DrawableRes iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
            )
            Text(
                text = label,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
            )
        }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.calender22),
                    contentDescription = null,
                    tint = primaryRed,
                    modifier = Modifier
                        .size(34.dp)
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
