package com.example.xone.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.livedata.observeAsState
import com.example.xone.R
import com.example.xone.controller.HolidayCalendarController
import com.example.xone.model.Holiday
import com.example.xone.model.CalendarResponse
import com.example.xone.utils.NetworkResult
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun HolidayCalendarScreen(
    controller: HolidayCalendarController,
    onBackPressed: () -> Unit,
    onMonthClick: (Int) -> Unit,
    onHolidayListClick: (String) -> Unit
) {
    // Observe holidays from the controller
    val holidaysState = controller.holidays.observeAsState()
    
    // Local state for holidays
    val holidays = remember { mutableStateOf<List<Holiday>>(emptyList()) }
    val pdfUrl = remember { mutableStateOf<String?>(null) }
    
    // Update holidays when LiveData changes
    LaunchedEffect(holidaysState.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    holidays.value = calendarResponse.holidays
                    pdfUrl.value = calendarResponse.holidaysFile
                    
                    // Debug log to check the value
                    println("DEBUG: Holidays PDF URL set to: ${calendarResponse.holidaysFile}")
                }
            }
            is NetworkResult.Error -> {
                // If API fails, use default holidays
                holidays.value = controller.getDefaultHolidays()
                println("DEBUG: Error loading holidays: ${(result as NetworkResult.Error).message}")
            }
            is NetworkResult.Loading, null -> {
                // Show loading or use empty list
                println("DEBUG: Loading holidays...")
            }
        }
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
            // Top Bar with back button and title
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
                
                Text(
                    text = "Holiday Calendar",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Year heading and Holiday List Button in the same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Year 2025",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Button(
                    onClick = { 
                        pdfUrl.value?.let { url ->
                            onHolidayListClick(url)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Holiday List",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Holiday List",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator when fetching holidays
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
                // Calendar grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 3) {
                                val monthIndex = row * 3 + col + 1
                                if (monthIndex <= 12) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable { onMonthClick(monthIndex) }
                                    ) {
                                        MonthCard(
                                            month = monthIndex,
                                            holidays = holidays.value.filter { it.month == monthIndex && it.isApplicable }
                                        )
                                    }
                                }
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
fun MonthCard(month: Int, holidays: List<Holiday>) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(122.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = YearMonth.of(2025, month)
                    .month
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(1.dp))
            
            MonthDates(month, holidays)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthDates(month: Int, holidays: List<Holiday>) {
    val firstDayOfMonth = LocalDate.of(2025, month, 1).dayOfWeek.value % 7
    val totalDays = YearMonth.of(2025, month).lengthOfMonth()

    Column(
        verticalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        var dayCounter = 1
        for (week in 0 until 6) {
            if (dayCounter > totalDays) break
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (day in 0 until 7) {
                    val date = if (week == 0 && day < firstDayOfMonth) 0 
                              else if (dayCounter <= totalDays) dayCounter++ 
                              else 0
                    
                    // Check specifically for holidays with type = "Yes"
                    val isHoliday = holidays.any { 
                        it.day == date && it.holidayType == "Yes" 
                    }
                    
                    DateView(date, isHoliday)
                }
            }
        }
    }
}

@Composable
fun DateView(date: Int, isHoliday: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(12.dp)
            .then(
                if (isHoliday) 
                    Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFDD3825))
                else 
                    Modifier
            )
    ) {
        if (date > 0) {
            Text(
                text = date.toString(),
                fontSize = 8.sp,
                lineHeight = 8.sp,
                color = if (isHoliday) Color.White else Color.Black,
                fontWeight = if (isHoliday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHolidayCalendarScreen() {
    // For preview, create a simpler version that doesn't depend on mocking the entire API
    val previewHolidays = listOf(
        Holiday("New Year", "01-01-2025", "Yes"),
        Holiday("Republic Day", "26-01-2025", "Yes"),
        Holiday("Labor Day", "01-05-2025", "Yes"),
        Holiday("Independence Day", "15-08-2025", "Yes"),
        Holiday("Gandhi Jayanti", "02-10-2025", "Yes"),
        Holiday("Christmas", "25-12-2025", "Yes")
    )
    
    // Create a custom composable for preview instead of using the actual screen
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
            // Top Bar with back button and title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { },
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
                
                Text(
                    text = "Holiday Calendar",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Year 2025",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Holiday List",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Holiday List",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Calendar grid - only show if API level allows
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Calendar grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 3) {
                                val monthIndex = row * 3 + col + 1
                                if (monthIndex <= 12) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        MonthCard(
                                            month = monthIndex,
                                            holidays = previewHolidays.filter { it.month == monthIndex }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Show a message for older API levels
                Text(
                    text = "Calendar preview requires API level 26 or higher",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black
                )
            }
        }
    }
}