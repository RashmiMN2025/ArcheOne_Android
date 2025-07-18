package com.archeGlobal.one.ui.screens

import android.util.Log
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
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.model.Holiday
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.NetworkResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HolidayListScreen(
    controller: HolidayCalendarController,
    navigator: Navigator,
    onBackPressed: () -> Unit
) {
    val holidaysState = controller.holidays.observeAsState()
    val holidayFileUrl = controller.holidayFileUrl.observeAsState()

    var holidays by remember { mutableStateOf<List<Holiday>>(emptyList()) }

    // Update holidays when LiveData changes
    LaunchedEffect(holidaysState.value) {
        when (val result = holidaysState.value) {
            is NetworkResult.Success -> {
                result.data?.let { calendarResponse ->
                    holidays = calendarResponse.holidays.filter { it.isApplicable }
                }
            }
            is NetworkResult.Error -> {
                // If API fails, use default holidays
                holidays = controller.getDefaultHolidays()
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
                    .statusBarsPadding() // This adds top padding for the status bar
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
                    text = "Holiday List 2025",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )

                // View PDF button aligned to the right
                holidayFileUrl.value?.let { url ->
                    if (url.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (url.isNotBlank()) {
                                    Log.d("HolidayListScreen", "Opening PDF with URL: $url")
                                    navigator.navigateToPDFViewer(url, "Holiday List 2025")
                                } else {
                                    Log.e("HolidayListScreen", "Cannot open PDF: URL is empty")
                                    // Could show a toast here if needed
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825)
                            ),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                text = "PDF",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            } else if (holidays.isEmpty()) {
                // Empty state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "No holidays found",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "Date",
                        color = Color.White,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Holiday list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(holidays) { holiday ->
                        HolidayItem(holiday = holiday)
                    }
                }
            }
        }
    }
}

@Composable
fun HolidayItem(holiday: Holiday) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(2f)
            ) {
                // Color indicator based on holiday type
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

                Column {
                    Text(
                        text = holiday.name,
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = formatHolidayDate(holiday.date),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Holiday type badge
            Text(
                text = when (holiday.holidayType) {
                    "Yes" -> "Holiday"
                    "RH" -> "RH"
                    else -> "Other"
                },
                fontSize = 12.sp,
                color = when (holiday.holidayType) {
                    "Yes" -> Color(0xFFDD3825)
                    "RH" -> Color(0xFF2196F3)
                    else -> Color.Gray
                },
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

private fun formatHolidayDate(dateStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date = LocalDate.parse(dateStr, formatter)
        date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    } catch (e: Exception) {
        dateStr // Return original string if parsing fails
    }
}
