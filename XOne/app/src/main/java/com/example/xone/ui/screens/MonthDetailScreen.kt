package com.example.xone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.controller.HolidayCalendarController
import com.example.xone.model.Holiday
import java.time.YearMonth
import java.time.format.TextStyle
import com.example.xone.R
import java.util.*

@Composable
fun MonthDetailScreen(
    month: Int,
    controller: HolidayCalendarController,
    onBackPressed: () -> Unit
) {
    val holidays = remember { controller.getHolidays().filter { it.month == month } }
    val monthName = YearMonth.of(2025, month).month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Back Button & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = monthName,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 70.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Holiday List
        LazyColumn {
            items(holidays.size) { index ->
                HolidayRow(holidays[index])
            }
        }
    }
}

@Composable
fun HolidayRow(holiday: Holiday) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = holiday.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "${holiday.day} ${holiday.month}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMonthDetailScreen() {
    MonthDetailScreen(
        month = 1,
        controller = HolidayCalendarController(),
        onBackPressed = {}
    )
}
