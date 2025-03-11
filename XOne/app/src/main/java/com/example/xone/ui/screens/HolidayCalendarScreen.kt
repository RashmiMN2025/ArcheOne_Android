package com.example.xone.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    onBackPressed: () -> Unit
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
                // Back button aligned to the left
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
                
                // Title centered in the Box
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
                    onClick = { /* Handle holiday list click */ },
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
                                    MonthCard(monthIndex, holidays.filter { it.month == monthIndex })
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
            .width(110.dp)
            .height(135.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = YearMonth.of(2025, month)
                    .month
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(3.dp))
            
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
        verticalArrangement = Arrangement.spacedBy(2.dp)
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
            .size(15.dp)
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
                fontSize = 10.5.sp,
                color = if (isHoliday) Color.White else Color.Black,
                fontWeight = if (isHoliday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}


// Preview of Holiday Calendar
@Preview(showBackground = true)
@Composable
fun PreviewHolidayCalendarScreen() {
    HolidayCalendarScreen(controller = HolidayCalendarController(), onBackPressed = {})
}
