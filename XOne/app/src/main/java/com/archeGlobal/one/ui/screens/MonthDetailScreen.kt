package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.model.Holiday
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
    var selectedMonth by remember { mutableStateOf(month) }
    
    var monthHolidays by remember { mutableStateOf<List<Holiday>>(emptyList()) }
    var selectedHoliday by remember { mutableStateOf<Holiday?>(null) }
    
    // Update holidays when LiveData changes or when month changes
    LaunchedEffect(holidaysState.value, selectedMonth) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    monthHolidays = calendarResponse.holidays
                        .filter { holiday ->
                            try {
                                val date = LocalDate.parse(holiday.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                date.monthValue == selectedMonth && (holiday.holidayType == "Yes" || holiday.holidayType == "RH")
                            } catch (e: Exception) {
                                false
                            }
                        }
                }
            }
            is NetworkResult.Error -> {
                // If API fails, use default holidays filtered by month
                monthHolidays = controller.getDefaultHolidays()
                    .filter { holiday ->
                        try {
                            val date = LocalDate.parse(holiday.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            date.monthValue == selectedMonth
                        } catch (e: Exception) {
                            false
                        }
                    }
            }
            is NetworkResult.Loading, null -> {
                // Show loading state
            }
        }
    }

    // Reset selected holiday when changing months
    LaunchedEffect(selectedMonth) {
        selectedHoliday = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
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
                        .getDisplayName(TextStyle.FULL, Locale.getDefault()) + " 2025",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Next Month Button - only show if not December
                if (selectedMonth < 12) {
                    IconButton(
                        onClick = {
                            selectedMonth++
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
                // Month Calendar (without white box) - always show calendar
                MonthCalendarView(selectedMonth, monthHolidays) { holiday ->
                    selectedHoliday = holiday
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show "No holidays" message only if there's no selected holiday and no holidays in the month
                if (selectedHoliday == null && monthHolidays.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "No holidays in this month",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Holiday Details Box (shows when a holiday is selected)
                    selectedHoliday?.let { holiday ->
                        HolidayDetailsBox(holiday = holiday)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarView(month: Int, holidays: List<Holiday>, onDateClick: (Holiday) -> Unit) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val yearMonth = YearMonth.of(2025, month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7
    
    // Get current date to correctly mark today
    val currentDate = LocalDate.now()
    val isCurrentMonth = currentDate.monthValue == month && currentDate.year == 2025
    
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
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color(0xFF2196F3), text = "RH")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color(0xFF4CAF50), text = "Today")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isMandatoryHoliday -> Color(0xFFDD3825) // Red for holidays
                                        isRegionalHoliday -> Color(0xFF2196F3) // Blue for RH
                                        isToday -> Color(0xFF4CAF50) // Green for today
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    holiday?.let { onDateClick(it) }
                                }
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 16.sp,
                                color = when {
                                    isMandatoryHoliday || isRegionalHoliday || isToday -> Color.White
                                    else -> Color.Black
                                },
                                fontWeight = when {
                                    isMandatoryHoliday || isRegionalHoliday || isToday -> FontWeight.Bold
                                    else -> FontWeight.Normal
                                }
                            )
                        }
                    } else {
                        // Empty space for days outside the month
                        Box(
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HolidayDetailsBox(holiday: Holiday) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Holiday Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Icon and holiday name in a row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
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
                            .size(60.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
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
                        modifier = Modifier.size(60.dp)
                    ) {
                        DefaultIndianFlag()
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Holiday name and date in a column
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = holiday.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatDetailDate(holiday.date),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Start
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
fun HolidayDetailItem(holiday: Holiday) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                                "Yes" -> Color(0xFFDD3825)  // Red for mandatory holidays
                                "RH" -> Color(0xFF2196F3)   // Blue for RH holidays
                                else -> Color.Gray          // Gray for others
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
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}
