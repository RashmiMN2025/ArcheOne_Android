package com.archeGlobal.one.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.MeetingHistoryDetailActivity
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingHistoryScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WelcomeBackgroundTop,
                                WelcomeBackgroundMiddle,
                                WelcomeBackgroundBottom
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .offset(x = (-24).dp),
                                text = "Booking History",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        actions = {}
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(3) { // Sample cards
                            MeetingBookingCard(
                                bookingId = "BOOKING12${it + 1}",
                                roomName = "Conference Room ${'A' + it}",
                                location = "Bengaluru",
                                host = "John Doe", // Sample host
                                meetingDate = "10 Oct 2025", // Sample date
                                meetingTime = "10:00 AM - 11:00 AM", // Sample time
                                pendingFrom = "Admin", // Sample pending from
                                status = listOf("Approved", "Pending", "Rejected")[it],
                                onClick = { },
                                onApprove = {
                                    val intent = Intent(context, MeetingHistoryDetailActivity::class.java)
                                    context.startActivity(intent)
                                },
                                onReject = {
                                    val intent = Intent(context, MeetingHistoryDetailActivity::class.java)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingBookingCard(
    bookingId: String,
    roomName: String,
    location: String,
    host: String,
    meetingDate: String,
    meetingTime: String,
    pendingFrom: String,
    status: String,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "approved" -> Pair(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000))
        "rejected" -> Pair(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000))
        else -> Pair(Color(0xFFFFA500).copy(alpha = 0.15f), Color(0xFFFFA500))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$bookingId",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "Status $status",
                        fontSize = 12.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            MeetingDetailItem(
                icon = R.drawable.ic_location,
                label = "Room",
                value = "$roomName ($location)"
            )
            MeetingDetailItem(
                icon = R.drawable.ic_person, // Assuming an icon for host exists; replace with actual if available
                label = "Host",
                value = host
            )
            MeetingDetailItem(
                icon = R.drawable.ic_calendar, // Assuming an icon for date exists; replace with actual if available
                label = "Meeting date",
                value = meetingDate
            )
            MeetingDetailItem(
                icon = R.drawable.pending, // Assuming an icon for time exists; replace with actual if available
                label = "Meeting time",
                value = meetingTime
            )
            MeetingDetailItem(
                icon = R.drawable.pending,
                label = "Pending from",
                value = pendingFrom
            )

            Spacer(modifier = Modifier.height(8.dp))


            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Approve button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(color = Color(0xFF4CAF50)) // Green color
                        .padding(vertical = 12.dp)
                        .clickable(onClick = onApprove),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Approve",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily
                    )
                }

                // Reject button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(color = PrimaryRed)
                        .padding(vertical = 12.dp)
                        .clickable(onClick = onReject),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reject",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun MeetingDetailItem(
    icon: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = GraphikFontFamily,
            color = Color.Black
        )
    }
}