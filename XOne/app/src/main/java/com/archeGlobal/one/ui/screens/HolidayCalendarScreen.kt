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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.model.GlobalEvent
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
    val globalEvents = remember { mutableStateOf<List<GlobalEvent>>(emptyList()) }
    val pdfUrl = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(holidaysState.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    holidays.value = calendarResponse.holidays
                    globalEvents.value = calendarResponse.globalEvents ?: emptyList()
                    pdfUrl.value = calendarResponse.holidaysFile
                }
            }

            is NetworkResult.Error -> {
                holidays.value = controller.getDefaultHolidays()
                globalEvents.value = emptyList()
            }

            is NetworkResult.Loading, null -> { /* Show loading */
            }
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
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp, 12.dp, 6.dp, 0.dp) // Minimized horizontal padding
            ) {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
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
                                controller.onViewClick(
                                    context = context,
                                    documentName = "Holiday Calendar PDF",
                                    filePath = url
                                )
                                // onHolidayListClick(url)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.holiday_list),
                                contentDescription = "Holiday List",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                "Holiday List",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Legend for holiday types
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFDD3825), text = "Holidays")
                    Spacer(modifier = Modifier.width(11.dp))
                    LegendItem(color = Color(0xFF2196F3), text = "RH")
                    Spacer(modifier = Modifier.width(11.dp))
                    LegendItem(color = Color(0xFF4CAF50), text = "Global Event")
                }

                Spacer(modifier = Modifier.height(2.dp)) // Responsive Calendar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // Changed to 3 columns to match the image
                    verticalArrangement = Arrangement.spacedBy(20.dp), // Adjusted vertical spacing
                    horizontalArrangement = Arrangement.spacedBy(4.dp), // Further reduced horizontal spacing
                    contentPadding = PaddingValues(
                        start = 2.dp,
                        top = 8.dp,
                        end = 2.dp,
                        bottom = 100.dp
                    ), // Minimized side padding
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(12) { monthIndex ->
                        val currentMonth = monthIndex + 1
                        MonthCard(
                            month = currentMonth,
                            holidays = holidays.value.filter { it.month == currentMonth && it.isApplicable },
                            globalEvents = globalEvents.value.filter {
                                try {
                                    val parts = it.date.split("-")
                                    parts.size == 3 && parts[1].toInt() == currentMonth
                                } catch (e: Exception) {
                                    false
                                }
                            },
                            onMonthClick = onMonthClick
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthCard(month: Int, holidays: List<Holiday>, globalEvents: List<GlobalEvent> = emptyList(), onMonthClick: (Int) -> Unit) {
    // Get screen width
    val screenWidth = LocalConfiguration.current.screenWidthDp

    // Adjust height based on screen width
    val cardHeight = if (screenWidth <= 360) 160.dp else 150.dp // Increase height for small screens

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Maintained height
            .padding(horizontal = 0.5.dp) // Minimized padding
            .clickable { onMonthClick(month) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp), // Reduced vertical padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = YearMonth.of(2025, month)
                    .month
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    .take(3), // Just first 3 characters to match image (Jan, Feb, etc.)
                fontSize = 18.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 1.dp) // Reduced bottom padding
            )

            // Removed spacer to bring date numbers closer to month name

            MonthDates(month, holidays, globalEvents)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthDates(month: Int, holidays: List<Holiday>, globalEvents: List<GlobalEvent> = emptyList()) {
    val firstDayOfMonth = LocalDate.of(2025, month, 1).dayOfWeek.value % 7
    val totalDays = YearMonth.of(2025, month).lengthOfMonth()

    // Get current date to check if today should be highlighted
    val currentDate = LocalDate.now()
    val isCurrentMonth = currentDate.monthValue == month && currentDate.year == 2025

    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp), // Slightly reduced spacing between rows
        modifier = Modifier.padding(top = 2.dp) // Small top padding to adjust position
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
                    val date = if (week == 0 && day < firstDayOfMonth) {
                        0
                    } else if (dayCounter <= totalDays) {
                        dayCounter++
                    } else {
                        0
                    }

                    // Check if this is today
                    val isToday = isCurrentMonth && date == currentDate.dayOfMonth

                    // Check for holidays and global events
                    val mandatoryHoliday = holidays.any {
                        it.day == date && it.holidayType == "Yes"
                    }

                    val regionalHoliday = holidays.any {
                        it.day == date && it.holidayType == "RH"
                    }

                    val hasGlobalEvent = globalEvents.any {
                        try {
                            val parts = it.date.split("-")
                            parts.size == 3 && parts[0].toInt() == date
                        } catch (e: Exception) {
                            false
                        }
                    }

                    DateView(date, mandatoryHoliday, regionalHoliday, hasGlobalEvent, isToday)
                }
            }
        }
    }
}

@Composable
fun DateView(date: Int, isMandatoryHoliday: Boolean, isRegionalHoliday: Boolean, hasGlobalEvent: Boolean = false, isToday: Boolean = false) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(14.dp) // Slightly reduced size
            .then(
                when {
                    // Priority: If date has both holiday and global event, show green
                    (isMandatoryHoliday || isRegionalHoliday) && hasGlobalEvent ->
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)) // Green for both holiday and global event
                    isMandatoryHoliday ->
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFDD3825)) // Red for mandatory holidays
                    isRegionalHoliday ->
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3)) // Blue for RH holidays
                    hasGlobalEvent ->
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)) // Green for global events
                    isToday ->
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.3f)) // Light blue for today
                    else -> Modifier
                }
            )
    ) {
        if (date > 0) {
            Text(
                text = date.toString(),
                fontSize = 8.sp, // Smaller text size
                lineHeight = 8.sp,
                color = if (isMandatoryHoliday || isRegionalHoliday || hasGlobalEvent) Color.White else Color.Black,
                fontFamily = GraphikFontFamily,
                fontWeight = if (isMandatoryHoliday || isRegionalHoliday || hasGlobalEvent) FontWeight.Normal else FontWeight.Normal
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
        Holiday("Pongal", "15-01-2025", "RH"), // Regional holiday
        Holiday("Labor Day", "01-05-2025", "Yes"),
        Holiday("Good Friday", "18-04-2025", "RH"), // Regional holiday
        Holiday("Independence Day", "15-08-2025", "Yes"),
        Holiday("Janmashtami", "16-08-2025", "RH"), // Regional holiday
        Holiday("Gandhi Jayanti", "02-10-2025", "Yes"),
        Holiday("Christmas", "25-12-2025", "Yes")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
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
                        text = "Holiday Calendar PDF",
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
                        fontWeight = FontWeight.SemiBold,
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
