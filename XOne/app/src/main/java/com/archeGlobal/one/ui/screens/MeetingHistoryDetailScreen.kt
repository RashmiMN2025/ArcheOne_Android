package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.BookingHistoryItem
import com.archeGlobal.one.model.MeetingApprovalRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private fun truncateBookingId(bookingId: String): String {
    return if (bookingId.length > 16) {
        "${bookingId.take(16)}..."
    } else {
        bookingId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingHistoryDetailScreen(
    onBackPressed: () -> Unit,
    action: String? = null,
    booking: BookingHistoryItem? = null,
    source: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }
    var enterRemark by remember { mutableStateOf("") }

    val pendingFrom = when (booking?.pendingFrom) {
        "admin" -> "Admin"
        "linemanager" -> "Reporting Manager"
        "ceo" -> "CEO"
        else -> "Unknown"
    }

    val alreadyCheckedIn   = booking?.checkedIn?.lowercase() == "true"

    val showRemarkAndButton = action == "approve" || action == "reject" || action == "cancel"
    val buttonText = if (action == "approve") "Approve Booking" else if (action == "reject") "Reject Booking" else "Cancel Booking"
    val buttonColor = if (action == "approve") Color(0xFF4CAF50) else if (action == "reject") Color(0xFFDD3825) else Color(0xFFDD3825)

    val archeAttendeesList = remember(booking) {
        if (booking != null && !booking.archeAttendees.isNullOrBlank()) {
            try {
                // Try parsing as JSON array
                Gson().fromJson(booking.archeAttendees, Array<String>::class.java).toList()
            } catch (e: JsonSyntaxException) {
                // If JSON parsing fails, treat as comma-separated string
                booking.archeAttendees.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        } else {
            emptyList()
        }
    }

    val guestAttendeesList = remember(booking) {
        if (booking != null && !booking.guestAttendees.isNullOrBlank()) {
            try {
                // Try parsing as JSON array
                Gson().fromJson(booking.guestAttendees, Array<String>::class.java).toList()
            } catch (e: JsonSyntaxException) {
                // If JSON parsing fails, treat as comma-separated string
                booking.guestAttendees.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        } else {
            emptyList()
        }
    }

    val status = booking?.meetingStatus ?: "Unknown"
    val titleCaseStatus = status.split(" ")
        .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    val (statusBackgroundColor, statusTextColor) = when (status.lowercase()) {
        "booked" -> Color(0xFF008000).copy(alpha = 0.15f) to Color(0xFF008000)
        "rejected" -> Color(0xFFFF0000).copy(alpha = 0.15f) to Color(0xFFFF0000)
        "cancelled" -> Color.Gray.copy(alpha = 0.15f) to Color.Gray
        else -> Color(0xFFFFA500).copy(alpha = 0.15f) to Color(0xFFFFA500)
    }

    val normalizedStatus = status.lowercase()
    val showPendingFrom = normalizedStatus == "requested"
    val showRemark      = normalizedStatus in listOf("booked","requested","cancelled","rejected")
    val showCheckIn     = normalizedStatus == "booked"
    val remarkLabel     = when (normalizedStatus) {
        "cancelled" -> "Cancellation reason"
        "rejected"  -> "Rejection reason"
        else        -> "Remark"
    }

    val remarkIcon = when (normalizedStatus) {
        "cancelled" -> R.drawable.remark1   // ← same drawable as above
        "rejected"  -> R.drawable.remark1      // ← same drawable as above
        else        -> R.drawable.justification
    }

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
                                text = "Booking Details",
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

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Booking ID and Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${truncateBookingId(booking?.bookingId ?: "Unknown")}",
                                        fontSize = 18.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = statusBackgroundColor
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Text(
                                            text = "Status: $titleCaseStatus",
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = statusTextColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    thickness = 1.dp,
                                    color = Color.LightGray
                                )

                                // Booking Details
                                Text(
                                    text = "Booking Details",
                                    fontSize = 18.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Column(
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    MeetingDetailRow(
                                        icon = R.drawable.mroomtype,
                                        label = "Room",
                                        value = booking?.roomName ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.host,
                                        label = "Host Email",
                                        value = booking?.hostEmail ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.meetcalender,
                                        label = "Meeting Date",
                                        value = dateFormate(booking?.meetingStarttime ?: "")
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.meettime,
                                        label = "Meeting Time",
                                        value = "${formatTime(booking?.meetingStarttime ?: "")} - ${formatTime(booking?.meetingEndtime ?: "")}"
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.meetingtype,
                                        label = "Meeting Type",
                                        value = booking?.meetingType ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.archeattendees,
                                        label = "Arche Attendees",
                                        value = archeAttendeesList.joinToString(", ")
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.guestattendees,
                                        label = "Guest Attendees",
                                        value = guestAttendeesList.joinToString(", ")
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.justification,
                                        label = "Justification",
                                        value = booking?.businessJustification ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.panatry,
                                        label = "Refreshment",
                                        value = booking?.refreshmentRequired ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.additionalrequest,
                                        label = "Additional Request",
                                        value = booking?.additionalRequest ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.mappin_and_ellipse,
                                        label = "Subject",
                                        value = booking?.meetingSubject ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.mappin_and_ellipse,
                                        label = "Client Name",
                                        value = booking?.clientName ?: ""
                                    )
                                    MeetingDetailRow(
                                        icon = R.drawable.mappin_and_ellipse,
                                        label = "Project Name",
                                        value = booking?.projectName ?: ""
                                    )
                                    if (showPendingFrom) {
                                        MeetingDetailRow(
                                            icon = R.drawable.mrrompending,  // Adjust icon
                                            label = "Pending from",
                                            value = pendingFrom
                                        )
                                    }
                                    if (showRemark) {
                                        MeetingDetailRow(
                                            icon = remarkIcon,
                                            label = remarkLabel,
                                            value = booking?.remark ?: ""
                                        )
                                    }
                                    if (showCheckIn) {
                                        MeetingDetailRow(
                                            icon = R.drawable.checkinstatus,  // Adjust icon
                                            label = "Check-in Status",
                                            value = if (alreadyCheckedIn) "Done" else "Pending"
                                        )
                                    }
                                }

                                if (showRemarkAndButton) {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(),
                                        thickness = 1.dp,
                                        color = Color.LightGray
                                    )
                                    // Remark Text Field
                                    OutlinedTextField(
                                        value = enterRemark,
                                        onValueChange = { enterRemark = it },
                                        placeholder = {
                                            Text(
                                                "Enter remark(optional)",
                                                color = Color.LightGray,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color.LightGray,
                                            cursorColor = Color.Gray,
                                            unfocusedTextColor = Color.Black,
                                            focusedTextColor = Color.Black,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Submit Button
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    val userRole = when (source) {
                                                        "history" -> "host"
                                                        "admin" -> "admin"
                                                        "linemanager" -> "linemanager"
                                                        "ceo" -> "ceo"
                                                        else -> "user"
                                                    }
                                                    val responseStr = when (buttonText) {
                                                        "Approve Booking" -> "approved"
                                                        "Reject Booking" -> "rejected"
                                                        "Cancel Booking" -> "rejected"
                                                        else -> return@launch
                                                    }
                                                    val request = MeetingApprovalRequest(
                                                        designation = userRole,
                                                        response = responseStr,
                                                        remark = if (enterRemark.isNotBlank()) enterRemark else null
                                                    )
                                                    val apiResponse = RetrofitClient.apiService.updateMeetingApprovalStatus(
                                                        booking?.bookingId ?: return@launch,
                                                        request
                                                    )
                                                    if (apiResponse.isSuccessful) {
                                                        Toast.makeText(context, "$responseStr successful", Toast.LENGTH_SHORT).show()
                                                        onBackPressed()
                                                    } else {
                                                        Toast.makeText(context, "Error: ${apiResponse.message()}", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = buttonColor,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text(
                                            text = buttonText,
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
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

private fun dateFormate(timeStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timeStr) ?: return ""
        val outputFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}

private fun formatTime(timeStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timeStr) ?: return ""
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun MeetingDetailRow(
    icon: Int,
    label: String,
    value: String
) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(0.4f)
                    .padding(top = 2.dp)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.End,
                maxLines = 20,
                modifier = Modifier
                    .weight(0.6f)
                    .padding(end = 8.dp)
            )
        }
    }
}