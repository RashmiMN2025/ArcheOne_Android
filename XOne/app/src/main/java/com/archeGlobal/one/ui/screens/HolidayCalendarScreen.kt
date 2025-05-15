package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.model.Holiday
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.NetworkResult
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
    val holidaysState = controller.holidays.observeAsState()
    val holidays = remember { mutableStateOf<List<Holiday>>(emptyList()) }
    val pdfUrl = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(holidaysState.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    holidays.value = calendarResponse.holidays
                    pdfUrl.value = calendarResponse.holidaysFile
                }
            }
            is NetworkResult.Error -> {
                holidays.value = controller.getDefaultHolidays()
            }
            is NetworkResult.Loading, null -> { /* Show loading */ }
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
                .padding(12.dp, 12.dp, 12.dp, 0.dp)
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 10.dp)
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
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Year heading and Holiday List Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Year 2025",
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Button(
                    onClick = {
                        pdfUrl.value?.let { url ->
                            controller.onViewClick(context = context, documentName = "Holiday List 2025", filePath = url)
                           // onHolidayListClick(url)
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
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))            // Legend for holiday types
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFFDD3825), text = "Holidays")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = Color(0xFF2196F3), text = "RH")
            }
            Spacer(modifier = Modifier.height(2.dp))            // Responsive Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 115.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp), // Increased vertical spacing to 30dp
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Kept same horizontal spacing
                modifier = Modifier.fillMaxSize()
            ) {
                items(12) { monthIndex ->
                    MonthCard(
                        month = monthIndex + 1,
                        holidays = holidays.value.filter { it.month == monthIndex + 1 && it.isApplicable },
                        onMonthClick = onMonthClick
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthCard(month: Int, holidays: List<Holiday>, onMonthClick: (Int) -> Unit) {
    // Get screen width
    val screenWidth = LocalConfiguration.current.screenWidthDp

    // Adjust height based on screen width
    val cardHeight = if (screenWidth <= 360) 160.dp else 150.dp // Increase height for small screens

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .aspectRatio(1f) // Ensures the card is square
            .clickable { onMonthClick(month) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp), // Added equal padding inside the card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = YearMonth.of(2025, month)
                    .month
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                fontSize = 16.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                for (day in 0 until 7) {
                    val date = if (week == 0 && day < firstDayOfMonth) 0 
                              else if (dayCounter <= totalDays) dayCounter++ 
                              else 0
                    
                    // Check for both mandatory and regional holidays
                    val mandatoryHoliday = holidays.any { 
                        it.day == date && it.holidayType == "Yes" 
                    }
                    
                    val regionalHoliday = holidays.any { 
                        it.day == date && it.holidayType == "RH" 
                    }
                    
                    DateView(date, mandatoryHoliday, regionalHoliday)
                }
            }
        }
    }
}

@Composable
fun DateView(date: Int, isMandatoryHoliday: Boolean, isRegionalHoliday: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(12.dp)
            .then(
                when {
                    isMandatoryHoliday -> Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFDD3825)) // Red for mandatory holidays
                    isRegionalHoliday -> Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)) // Blue for RH holidays
                    else -> Modifier
                }
            )
    ) {
        if (date > 0) {
        Text(
                text = date.toString(),
                fontSize = 8.sp,
                lineHeight = 8.sp,
                color = if (isMandatoryHoliday || isRegionalHoliday) Color.White else Color.Black,
                fontFamily = GraphikFontFamily,
                fontWeight = if (isMandatoryHoliday || isRegionalHoliday) FontWeight.Bold else FontWeight.Normal
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
        Holiday("Pongal", "15-01-2025", "RH"),  // Regional holiday
        Holiday("Labor Day", "01-05-2025", "Yes"),
        Holiday("Good Friday", "18-04-2025", "RH"),  // Regional holiday
        Holiday("Independence Day", "15-08-2025", "Yes"),
        Holiday("Janmashtami", "16-08-2025", "RH"),  // Regional holiday
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
                    fontFamily = GraphikFontFamily,
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
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
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
                // Legend for holiday types
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFDD3825), text = "Holidays")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = Color(0xFF2196F3), text = "RH")
                }
                
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
                                            holidays = previewHolidays.filter { it.month == monthIndex },
                                            onMonthClick = { /* Handle month click */ }
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

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
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