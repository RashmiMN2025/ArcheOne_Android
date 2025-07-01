package com.archeGlobal.one.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.model.GlobalEvent
import com.archeGlobal.one.model.Holiday
import com.archeGlobal.one.model.Milestone
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.NetworkResult
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun MonthDetailScreen(
    month: Int,
    controller: HolidayCalendarController,
    onBackPressed: () -> Unit
) {
    val holidaysState = controller.holidays.observeAsState()
    val hiddenHolidaysUpdated = controller.hiddenHolidaysUpdated.observeAsState()
    val milestonesState = controller.milestones.observeAsState()

    var selectedMonth by remember { mutableStateOf(month) }
    var monthHolidays by remember { mutableStateOf<List<Holiday>>(emptyList()) }
    var selectedHoliday by remember { mutableStateOf<Holiday?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedMilestones by remember { mutableStateOf<List<Milestone>>(emptyList()) }
    var milestoneDates by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    // Track whether a date was explicitly selected by user (vs. automatically selected on init)
    var isUserSelectedDate by remember { mutableStateOf(false) }
    // Global events state
    var monthGlobalEvents by remember { mutableStateOf<List<GlobalEvent>>(emptyList()) }
    var selectedGlobalEvents by remember { mutableStateOf<List<GlobalEvent>>(emptyList()) }
    var globalEventDates by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Update holidays and global events when LiveData changes, month changes, or hidden holidays are updated
    LaunchedEffect(holidaysState.value, selectedMonth, hiddenHolidaysUpdated.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    // Filter holidays by month and hide status
                    val allMonthHolidays = calendarResponse.holidays
                        .filter { holiday ->
                            try {
                                val date = LocalDate.parse(
                                    holiday.date,
                                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                )
                                date.monthValue == selectedMonth && (holiday.holidayType == "Yes" || holiday.holidayType == "RH")
                            } catch (e: Exception) {
                                false
                            }
                        }
                    // Filter out hidden holidays
                    monthHolidays = controller.getVisibleHolidays(allMonthHolidays)

                    // Get global events for the month
                    monthGlobalEvents = controller.getGlobalEventsForMonth(selectedMonth)
                }
            }

            is NetworkResult.Error -> {
                // If API fails, use default holidays filtered by month
                val allMonthHolidays = controller.getDefaultHolidays()
                    .filter { holiday ->
                        try {
                            val date = LocalDate.parse(
                                holiday.date,
                                DateTimeFormatter.ofPattern("dd-MM-yyyy")
                            )
                            date.monthValue == selectedMonth
                        } catch (e: Exception) {
                            false
                        }
                    }
                // Filter out hidden holidays
                monthHolidays = controller.getVisibleHolidays(allMonthHolidays)

                // Get global events for the month
                monthGlobalEvents = controller.getGlobalEventsForMonth(selectedMonth)
            }

            is NetworkResult.Loading, null -> {
                // Show loading state
            }
        }

        // If the selected holiday is now hidden, clear the selection
        if (selectedHoliday != null && controller.isHolidayHidden(selectedHoliday!!)) {
            selectedHoliday = null
        }
    }

    // Update milestones and global events when selected date changes
    LaunchedEffect(selectedDate, milestonesState.value) {
        if (selectedDate != null) {
            Log.d("MonthDetailScreen", "Selected date: $selectedDate")

            val milestones = when (val result = holidaysState.value) {
                is NetworkResult.Success -> {
                    result.data?.let { calendarResponse ->
                        // Get milestones that match the selected date
                        val allMilestones = calendarResponse.milestones
                        Log.d(
                            "MonthDetailScreen",
                            "Total milestones to filter: ${allMilestones.size}"
                        )

                        val filtered = allMilestones.filter { milestone ->
                            try {
                                // Get the selected date parts (selectedDate will never be null here)
                                val selectedDateStr = selectedDate ?: return@filter false
                                val selectedDateParts = selectedDateStr.split("-")
                                val dayFromSelection = selectedDateParts[0].toInt()
                                val monthFromSelection = selectedDateParts[1].toInt()

                                // Parse milestone date (always in dd-MM-yyyy format)
                                val milestoneParts = milestone.poDate.split("-")

                                if (milestoneParts.size == 3) {
                                    // ALL milestone dates are in DD-MM-YYYY format
                                    val milestoneDay = milestoneParts[0].toInt()
                                    val milestoneMonth = milestoneParts[1].toInt()

                                    // Compare ONLY day and month, ignoring year completely
                                    val isMatch =
                                        milestoneDay == dayFromSelection && milestoneMonth == monthFromSelection

                                    Log.d(
                                        "MonthDetailScreen",
                                        "Comparing milestone ${milestone.poDate} " +
                                                "(day=$milestoneDay, month=$milestoneMonth) with " +
                                                "selected date $selectedDate (day=$dayFromSelection, month=$monthFromSelection) " +
                                                "= $isMatch"
                                    )

                                    isMatch
                                } else {
                                    Log.e(
                                        "MonthDetailScreen",
                                        "Invalid milestone date format: ${milestone.poDate}"
                                    )
                                    false
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "MonthDetailScreen",
                                    "Error comparing dates: ${milestone.poDate} vs $selectedDate",
                                    e
                                )
                                false
                            }
                        }

                        filtered
                    } ?: emptyList()
                }

                else -> emptyList()
            }
            selectedMilestones = milestones

            // Get global events for the selected date
            if (selectedDate != null) {
                selectedGlobalEvents = controller.getGlobalEventsForDate(selectedDate ?: "")
                Log.d(
                    "MonthDetailScreen",
                    "Global events for $selectedDate: ${selectedGlobalEvents.size}"
                )
            }
        } else {
            selectedMilestones = emptyList()
            selectedGlobalEvents = emptyList()
        }
    }

    // Update milestones and global events dates when month changes or data changes
    LaunchedEffect(selectedMonth, milestonesState.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    // Get milestone dates for the selected month
                    val milestones = calendarResponse.milestones
                    Log.d("MonthDetailScreen", "Total milestones: ${milestones.size}")

                    milestoneDates = milestones
                        .filter { milestone ->
                            try {
                                // ALL milestone dates are in DD-MM-YYYY format
                                val parts = milestone.poDate.split("-")

                                if (parts.size == 3) {
                                    // Parse as DD-MM-YYYY
                                    val day = parts[0].toInt()
                                    val monthValue = parts[1].toInt() // Renamed to avoid shadowing

                                    // Check if this milestone is for the current month
                                    val isMatch = monthValue == selectedMonth
                                    Log.d(
                                        "MonthDetailScreen",
                                        "Milestone ${milestone.poDate}: Month=$monthValue, Day=$day, Current Month=$selectedMonth, Match=$isMatch"
                                    )
                                    isMatch
                                } else {
                                    Log.e(
                                        "MonthDetailScreen",
                                        "Invalid date format: ${milestone.poDate}"
                                    )
                                    false
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "MonthDetailScreen",
                                    "Error parsing date: ${milestone.poDate}",
                                    e
                                )
                                false
                            }
                        }
                        .map {
                            try {
                                // Extract day from DD-MM-YYYY date format
                                val parts = it.poDate.split("-")

                                if (parts.size == 3) {
                                    // Always use first part as day (DD-MM-YYYY format)
                                    val day = parts[0].toInt()

                                    Log.d(
                                        "MonthDetailScreen",
                                        "Milestone day: $day from ${it.poDate}"
                                    )
                                    day
                                } else {
                                    Log.e("MonthDetailScreen", "Invalid date format: ${it.poDate}")
                                    0
                                }
                            } catch (e: Exception) {
                                Log.e("MonthDetailScreen", "Error getting day: ${it.poDate}", e)
                                0
                            }
                        }
                        .filter { it > 0 }
                        .toSet()

                    Log.d(
                        "MonthDetailScreen",
                        "Milestone dates for month $selectedMonth: $milestoneDates"
                    )

                    // Get global event dates for the selected month
                    globalEventDates = controller.getGlobalEventsForMonth(selectedMonth)
                        .map { event ->
                            try {
                                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val localDate = LocalDate.parse(event.date, formatter)
                                if (localDate.monthValue == selectedMonth) localDate.dayOfMonth else 0
                            } catch (e: Exception) {
                                Log.e(
                                    "MonthDetailScreen",
                                    "Error parsing global event date: ${e.message}"
                                )
                                0
                            }
                        }
                        .filter { it > 0 }
                        .toSet()

                    Log.d(
                        "MonthDetailScreen",
                        "Global event dates for month $selectedMonth: $globalEventDates"
                    )
                }
            }

            else -> {
                Log.d("MonthDetailScreen", "No calendar data available for milestone dates")
            }
        }
    } // Initialize with today's date if we're in current month, otherwise first day of month
    LaunchedEffect(selectedMonth) {
        val currentDate = LocalDate.now()
        if (currentDate.monthValue == selectedMonth && currentDate.year == 2025) {
            // If it's current month, select today's date
            val todayStr =
                String.format("%02d-%02d-%04d", currentDate.dayOfMonth, selectedMonth, 2025)
            selectedDate = todayStr
            selectedDay = currentDate.dayOfMonth

            // Find holiday for today
            val todayHoliday = monthHolidays.find { holiday ->
                try {
                    val holidayDate =
                        LocalDate.parse(holiday.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    holidayDate.dayOfMonth == currentDate.dayOfMonth
                } catch (e: Exception) {
                    false
                }
            }
            selectedHoliday = todayHoliday
            // Reset user selection flag since this is automatic selection
            isUserSelectedDate = false
        } else {
            // For other months, select the first day
            val firstDayStr = String.format("%02d-%02d-%04d", 1, selectedMonth, 2025)
            selectedDate = firstDayStr
            selectedDay = 1

            // Find holiday for first day if any
            val firstDayHoliday = monthHolidays.find { holiday ->
                try {
                    val holidayDate =
                        LocalDate.parse(holiday.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    holidayDate.dayOfMonth == 1
                } catch (e: Exception) {
                    false
                }
            }
            selectedHoliday = firstDayHoliday
            // Reset user selection flag since this is automatic selection
            isUserSelectedDate = false
        }
    }

    // Reset to current date when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // This code runs when leaving the screen
            isUserSelectedDate = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Clear selections when background is clicked
                    selectedHoliday = null
                    selectedDate = null
                    selectedMilestones = emptyList()
                    selectedDay = null
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp, 12.dp, 4.dp, 0.dp) // Minimized horizontal padding
                    .clickable(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume click to prevent it from reaching the background */ }
            ) {
                // Top Bar with back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Add this for proper top spacing
                        .padding(bottom = 10.dp)
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    // Header now shows "Holiday Calendar" text
                    Text(
                        text = "Holiday Calendar",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (holidaysState.value is NetworkResult.Loading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFDD3825)
                        )
                    }
                } else {
                    // Month Calendar with white background Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(horizontal = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Month Header with navigation arrows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Month Button - only show if not January
                                if (selectedMonth > 1) {
                                    IconButton(
                                        onClick = {
                                            selectedMonth--
                                            // Clear current selections
                                            selectedHoliday = null
                                            selectedMilestones = emptyList()
                                            // Set first day of new month
                                            val firstDayStr = String.format(
                                                "%02d-%02d-%04d",
                                                1,
                                                selectedMonth - 1,
                                                2025
                                            )
                                            selectedDate = firstDayStr
                                            selectedDay = 1
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_back),
                                            contentDescription = "Previous Month",
                                            tint = Color.Black
                                        )
                                    }
                                } else {
                                    // Empty spacer for alignment when button is hidden
                                    Spacer(modifier = Modifier.size(48.dp))
                                }

                                // Month and Year
                                Text(
                                    text = YearMonth.of(2025, selectedMonth)
                                        .month
                                        .getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        ) + " 2025",
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold
                                )

                                // Next Month Button - only show if not December
                                if (selectedMonth < 12) {
                                    IconButton(
                                        onClick = {
                                            selectedMonth++
                                            // Clear current selections
                                            selectedHoliday = null
                                            selectedMilestones = emptyList()
                                            // Set first day of new month
                                            val firstDayStr = String.format(
                                                "%02d-%02d-%04d",
                                                1,
                                                selectedMonth + 1,
                                                2025
                                            )
                                            selectedDate = firstDayStr
                                            selectedDay = 1
                                        }
                                    ) {
                                        // Using the same icon as back but rotated 180 degrees
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_back),
                                            contentDescription = "Next Month",
                                            tint = Color.Black,
                                            modifier = Modifier.rotate(180f)
                                        )
                                    }
                                } else {
                                    // Empty spacer for alignment when button is hidden
                                    Spacer(modifier = Modifier.size(48.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp)) // Month Calendar
                            MonthCalendarView(
                                selectedMonth = selectedMonth,
                                holidays = monthHolidays,
                                milestoneDates = milestoneDates,
                                globalEventDates = globalEventDates,
                                selectedDay = selectedDay,
                                isUserSelectedDate = isUserSelectedDate,
                                onDateClick = { holiday, dateStr, day ->
                                    // Save selected holiday and date
                                    selectedHoliday = holiday
                                    selectedDate = dateStr
                                    selectedDay = day

                                    // Force a direct check against known global event dates
                                    // This handles the case where the API's date format might be different
                                    val knownGlobalEventDates = mapOf(
                                        "08-03-2025" to "International Women's Day",
                                        "22-04-2025" to "Earth Day",
                                        "07-04-2025" to "World Health Day",
                                        "01-05-2025" to "International Workers' day",
                                        "05-06-2025" to "World Environment Day",
                                        "21-09-2025" to "World Peace Day",
                                        "19-11-2025" to "International Men's Day",
                                        "11-05-2025" to "International Mother's Day",
                                        "15-06-2025" to "International Father's Day",
                                        "11-04-2025" to "International Pets Day",
                                        "28-06-2025" to "LGBT Pride Day"
                                    )

                                    // Update milestone data for selected date
                                    selectedMilestones = controller.getMilestonesForDate(dateStr)
                                    Log.d(
                                        "MonthDetailScreen",
                                        "Date clicked: $dateStr, Milestones: ${selectedMilestones.size}"
                                    )

                                    // Update global events for selected date
                                    selectedGlobalEvents =
                                        controller.getGlobalEventsForDate(dateStr)

                                    // Direct check against known global event dates
                                    if (knownGlobalEventDates.containsKey(dateStr) && selectedGlobalEvents.isEmpty()) {
                                        Log.d(
                                            "MonthDetailScreen",
                                            "Known global event date detected: $dateStr - ${knownGlobalEventDates[dateStr]}"
                                        )
                                        // Manually find matching global events by exact date string
                                        val allGlobalEvents = controller.getAllGlobalEvents()
                                        val matchingEvents =
                                            allGlobalEvents.filter { it.date == dateStr }

                                        if (matchingEvents.isNotEmpty()) {
                                            selectedGlobalEvents = matchingEvents
                                            Log.d(
                                                "MonthDetailScreen",
                                                "Manually added ${matchingEvents.size} global events"
                                            )
                                            matchingEvents.forEach { event ->
                                                Log.d(
                                                    "MonthDetailScreen",
                                                    "  - Added: ${event.name} on ${event.date}"
                                                )
                                            }
                                        } else {
                                            // As a fallback, create a synthetic event based on the known date
                                            Log.d(
                                                "MonthDetailScreen",
                                                "No direct match found, adding synthetic event"
                                            )
                                            val syntheticEvent = GlobalEvent(
                                                name = knownGlobalEventDates[dateStr]
                                                    ?: "Global Event",
                                                date = dateStr,
                                                image = null,
                                                description = "Global celebration day"
                                            )
                                            selectedGlobalEvents = listOf(syntheticEvent)
                                            Log.d(
                                                "MonthDetailScreen",
                                                "Added synthetic event: ${syntheticEvent.name}"
                                            )
                                        }
                                    }

                                    Log.d(
                                        "MonthDetailScreen",
                                        "Global Events for $dateStr: ${selectedGlobalEvents.size}"
                                    )
                                    selectedGlobalEvents.forEach { event ->
                                        Log.d(
                                            "MonthDetailScreen",
                                            "  - ${event.name}: ${event.date}"
                                        )
                                    }

                                    isUserSelectedDate = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Always proceed to show details if any data is available
                    if (selectedHoliday == null && selectedMilestones.isEmpty() && selectedGlobalEvents.isEmpty()) {
                        // No data available for selected date - show message
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                        }
                    } else {
                        // Show details boxes in order: Holiday, Global Events, Milestones
                        // Each section will display if content is available

                        // 1. Holiday Details Box
                        if (selectedHoliday != null) {
                            HolidayDetailsBox(
                                holiday = selectedHoliday!!
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 2. Global Events Box
                        if (selectedDate != null && selectedGlobalEvents.isNotEmpty()) {
                            GlobalEventDetailsBox(
                                date = selectedDate!!,
                                globalEvents = selectedGlobalEvents,
                                controller = controller
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 3. Milestones Box
                        if (selectedDate != null && selectedMilestones.isNotEmpty()) {
                            MilestoneDetailsBox(
                                date = selectedDate!!,
                                milestones = selectedMilestones
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarView(
    selectedMonth: Int,
    holidays: List<Holiday>,
    milestoneDates: Set<Int> = emptySet(),
    globalEventDates: Set<Int> = emptySet(),
    selectedDay: Int? = null,
    isUserSelectedDate: Boolean = false,
    onDateClick: (Holiday?, String, Int) -> Unit
) {
    // Log milestone dates for debugging
    LaunchedEffect(milestoneDates) {
        Log.d("MonthCalendarView", "Milestone dates: $milestoneDates")
    }

    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val yearMonth = YearMonth.of(2025, selectedMonth)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7

    // Get current date to correctly mark today
    val currentDate = LocalDate.now()
    val isCurrentMonth = currentDate.monthValue == selectedMonth && currentDate.year == 2025

    Column {
        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend for holiday types - placed between week names and calendar dates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFFDD3825), text = "Holiday")
            Spacer(modifier = Modifier.width(6.dp))
            LegendItem(color = Color(0xFF2196F3), text = "RH")
            Spacer(modifier = Modifier.width(6.dp))
            LegendItem(color = Color(0xFF4CAF50), text = "Global Event")
            Spacer(modifier = Modifier.width(6.dp))
            LegendItem(color = Color(0xFFF5A623), text = "Milestone")
            // Spacer(modifier = Modifier.width(16.dp))
            // LegendItem(color = Color(0x), text = "Global Event")
        }

        Spacer(modifier = Modifier.height(11.dp))

        // Dates
        val weeks = (daysInMonth + firstDayOfMonth + 6) / 7
        repeat(weeks) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { dayOfWeekIndex ->
                    val day = weekIndex * 7 + dayOfWeekIndex + 1 - firstDayOfMonth
                    if (day in 1..daysInMonth) {
                        // Format the date to "dd-MM-yyyy" for consistency
                        val dateStr = String.format("%02d-%02d-%04d", day, selectedMonth, 2025)

                        // Only mark as today if it's the current date
                        val isToday = isCurrentMonth && day == currentDate.dayOfMonth

                        // Find if this day has a holiday
                        val holiday = holidays.find {
                            try {
                                val holidayDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                holidayDate.dayOfMonth == day
                            } catch (e: Exception) {
                                false
                            }
                        }

                        val isMandatoryHoliday = holiday?.holidayType == "Yes"
                        val isRegionalHoliday = holiday?.holidayType == "RH"

                        // Check if this day has milestones or global events
                        val hasMilestone = milestoneDates.contains(day)
                        val hasGlobalEvent = globalEventDates.contains(day)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape).background(
                                    when {
                                        isMandatoryHoliday -> Color(0xFFDD3825) // Solid red for holidays
                                        isRegionalHoliday -> Color(0xFF2196F3) // Solid blue for RH
                                        hasGlobalEvent -> Color(0xFF4CAF50) // Solid green for global events
                                        // Show blue for today if no user selection, or for selected day if user made a selection
                                        (isToday && !isUserSelectedDate) || (day == selectedDay && isUserSelectedDate) -> Color(0xFF2196F3).copy(alpha = 0.3f) // Blue with 30% opacity
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (day == selectedDay) 1.dp else 0.dp,
                                    color = if (day == selectedDay) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onDateClick(holiday, dateStr, day)
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.toString(),
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    fontSize = 14.sp,
                                    color = when {
                                        isMandatoryHoliday || isRegionalHoliday -> Color.White
                                        hasGlobalEvent -> Color.White
                                        (isToday && !isUserSelectedDate) || (day == selectedDay && isUserSelectedDate) -> Color.White
                                        else -> Color.Black
                                    },
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = when {
                                        isMandatoryHoliday || isRegionalHoliday -> FontWeight.Bold
                                        hasGlobalEvent -> FontWeight.Bold
                                        (isToday && !isUserSelectedDate) || (day == selectedDay && isUserSelectedDate) -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    }
                                )

                                // Add indicator dots for special dates
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    // Add yellow dot for milestone dates
                                    if (hasMilestone) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF5A623))
                                        )
                                    }

                                    // Add purple dot for global event dates
                                    if (hasGlobalEvent) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4CAF50))
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty space for days outside the month
                        Box(
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HolidayDetailsBox(
    holiday: Holiday
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            // Keep the clickable to prevent clicks from propagating to parent
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Prevent click from reaching background */ }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Replace the header row containing title and close button with just the title
            Text(
                text = "Holiday Details",
                fontSize = 14.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Icon and holiday name in a row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                // Load holiday icon from URL
                if (holiday.icon != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(holiday.icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${holiday.name} icon",
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFDD3825),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            // Fallback to Indian flag if icon fails to load
                            DefaultIndianFlag()
                        }
                    )
                } else {
                    // Fallback to Indian flag if no icon URL is available
                    Box(
                        modifier = Modifier.size(45.dp)
                    ) {
                        DefaultIndianFlag()
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Holiday name and date in a column
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = holiday.name,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = formatDetailDate(holiday.date),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultIndianFlag() {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFFF9933))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF000080))
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF138808))
            )
        }
    }
}

@Composable
fun HolidayDetailItem(
    holiday: Holiday,
    onItemClick: (Holiday) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(holiday) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(2f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (holiday.holidayType) {
                                "Yes" -> Color(0xFFDD3825) // Red for mandatory holidays
                                "RH" -> Color(0xFF2196F3) // Blue for RH holidays
                                else -> Color.Gray // Gray for others
                            }
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = holiday.name,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }

            Text(
                text = formatDetailDate(holiday.date),
                color = Color.DarkGray,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

private fun formatDetailDate(dateStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date = LocalDate.parse(dateStr, formatter)
        date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Black
        )
    }
}

@Composable
fun MilestoneDetailsBox(
    date: String,
    milestones: List<Milestone>
) {
    // Format date for display (e.g., "12 January" from "12-01-2025")
    val formattedDate = try {
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        "${localDate.dayOfMonth} ${localDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}"
    } catch (e: Exception) {
        date
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Prevent click from reaching background */ }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with formatted date
            Text(
                text = "Milestones of $formattedDate",
                fontSize = 14.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display milestone information
            milestones.forEach { milestone ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    // Milestone bullet point in orange color
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFF5A623), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = milestone.event,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Customer information with left padding to align with the event text
                    Row(
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "Customer: ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = milestone.customer,
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Project information
                    Row(
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "Project: ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = milestone.project,
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Show original date (includes year)
                    Row(
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "Original Date: ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatMilestoneDate(milestone.poDate),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (milestone != milestones.last()) {
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format milestone dates
private fun formatMilestoneDate(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")

        if (parts.size != 3) {
            // Invalid format, return as is
            return dateStr
        }

        // Parse DD-MM-YYYY format
        val day = parts[0].toInt()
        val monthNum = parts[1].toInt() // Renamed to avoid shadowing
        val year = parts[2].toInt()

        // Format as "Month DD, YYYY"
        val monthName = when (monthNum) {
            1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"; 5 -> "May"; 6 -> "June"
            7 -> "July"; 8 -> "August"; 9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
            else -> "Unknown"
        }

        "$monthName $day, $year"
    } catch (e: Exception) {
        // On any error, return the original string
        dateStr
    }
}

@Composable
fun GlobalEventDetailsBox(
    date: String,
    globalEvents: List<GlobalEvent>,
    controller: HolidayCalendarController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Events list
        globalEvents.forEach { event ->
            GlobalEventItem(event, controller)

            // Add divider between events
            if (event != globalEvents.last()) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
fun GlobalEventItem(
    event: GlobalEvent,
    controller: HolidayCalendarController
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event image on the left
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                if (!event.image.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(event.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${event.name} image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(color = Color(0xFF4CAF50))
                            }
                        },
                        error = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Show a placeholder icon for missing image
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Event Icon",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    )
                } else {
                    // Default icon if no image
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Event Icon",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Event information on the right
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                // Event name
                Text(
                    text = event.name,
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Event date
                Text(
                    text = formatDetailDate(event.date),
                    fontSize = 12.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Event description
                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
