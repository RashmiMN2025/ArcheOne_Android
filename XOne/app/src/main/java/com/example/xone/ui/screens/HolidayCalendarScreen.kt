package com.example.xone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.xone.R
import com.example.xone.controller.HolidayCalendarController
import com.example.xone.model.Holiday
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun HolidayCalendarScreen(
    controller: HolidayCalendarController,
    onBackPressed: () -> Unit,
    onMonthClick: (Int) -> Unit
) {
    val holidays = remember { controller.getHolidays() }

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
            modifier = Modifier.fillMaxSize().padding(8.dp) // Reduced padding
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 25.dp) // Adjusted top padding
            ) {
                IconButton(
                    onClick = onBackPressed
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
                    fontSize = 20.sp, // Slightly reduced font size
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 70.dp).align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(20.dp)) // Reduced space

            // 4x3 Grid Layout for months
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp) // Adjusted space between month rows
            ) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 3) {
                            val monthIndex = row * 3 + col + 1
                            if (monthIndex <= 12) {
                                MonthView(
                                    monthIndex,
                                    holidays.filter { it.month == monthIndex },
                                    onMonthClick = onMonthClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthView(month: Int, holidays: List<Holiday>, onMonthClick: (Int) -> Unit) {
    val monthName = YearMonth.of(2025, month)
        .month
        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) // Get month name (e.g., "Jan")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(115.dp) // Adjusted width
            .padding(2.dp)
            .clickable { onMonthClick(month)}
    ) {
        Text(
            text = monthName,
            fontSize = 14.sp, // Adjusted font size for better fit
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp)) // Reduced space

        MonthDates(month, holidays)
    }
}

@Composable
fun MonthDates(month: Int, holidays: List<Holiday>) {
    val firstDayOfMonth = LocalDate.of(2025, month, 1).dayOfWeek.value % 7 // 0 = Sunday
    val totalDays = YearMonth.of(2025, month).lengthOfMonth()

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp) // Further reduced row spacing
    ) {
        var dayCounter = 1
        for (week in 0 until 6) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp), // Reduced column spacing
                modifier = Modifier.fillMaxWidth()
            ) {
                for (day in 0 until 7) {
                    val date = if (week == 0 && day < firstDayOfMonth) 0 else if (dayCounter <= totalDays) dayCounter++ else 0
                    DateView(date, holidays.any { it.day == date })
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
            .size(16.dp) // Adjusted size
//            .padding(2.dp)
            .then(if (isHoliday) Modifier.clip(CircleShape).background(Color(0xFFDD3825)) else Modifier) // Apply circle only for holidays
    ) {
        Text(
            text = if (date > 0) date.toString() else " ", // Hide empty dates
            fontSize = 8.sp, // Adjusted font size
            color = if (isHoliday) Color.White else Color.Black, // White text for holidays
            fontWeight = if (isHoliday) FontWeight.Bold else FontWeight.Normal
        )
    }
}


// Preview of Holiday Calendar
//@Preview(showBackground = true)
//@Composable
//fun PreviewHolidayCalendarScreen() {
//    HolidayCalendarScreen(
//        controller = HolidayCalendarController(),
//        onBackPressed = {},
//        onMonthClick = ()
//    )
//}
