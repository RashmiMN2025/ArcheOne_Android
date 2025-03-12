package com.example.xone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.xone.R
import com.example.xone.controller.HolidayCalendarController
import com.example.xone.model.Holiday
import com.example.xone.utils.NetworkResult
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
    
    var monthHolidays by remember { mutableStateOf<List<Holiday>>(emptyList()) }
    
    // Update holidays when LiveData changes
    LaunchedEffect(holidaysState.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    monthHolidays = calendarResponse.holidays
                        .filter { holiday ->
                            try {
                                val date = LocalDate.parse(holiday.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                date.monthValue == month && (holiday.holidayType == "Yes" || holiday.holidayType == "RH")
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
                            date.monthValue == month
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
                    text = YearMonth.of(2025, month)
                        .month
                        .getDisplayName(TextStyle.FULL, Locale.getDefault()),
                color = Color.Black,
                fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
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
            } else if (monthHolidays.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "No holidays in this month",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
                
                // Holiday list header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFDD3825), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Holiday",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "Date",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(monthHolidays) { holiday ->
                        HolidayDetailItem(holiday = holiday)
                    }
                }
            }
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
        date.format(DateTimeFormatter.ofPattern("MMMM d"))
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
