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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.DisplayMode
import androidx.compose.ui.text.TextStyle
import com.archeGlobal.one.controller.MeetingHistoryController
import androidx.compose.foundation.lazy.items
import com.google.gson.Gson

private fun truncateBookingId(bookingId: String): String {
    return if (bookingId.length > 16) {
        "${bookingId.take(16)}..."
    } else {
        bookingId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingHistoryScreen(
    controller: MeetingHistoryController,
    onBackPressed: () -> Unit,
    source: String? = null
) {
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showDateFilterDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }

    // Initialize dates with current date range (last 30 days to today)
    val currentDate = remember { Calendar.getInstance() }
    val thirtyDaysAgo = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -30)
        }
    }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var fromDate by remember { mutableStateOf(dateFormatter.format(thirtyDaysAgo.time)) }
    var toDate by remember { mutableStateOf(dateFormatter.format(currentDate.time)) }

    // Selected date for date picker
    var selectedDate by remember { mutableStateOf(Date()) }
    var isDefaultDate by remember { mutableStateOf(true) }

    val bookings by controller.bookings.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()

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

                    // Filter section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search by Booking ID",
                                    color = Color.LightGray,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray,
                                cursorColor = Color.Gray,
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { /* TODO: Implement search */ }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Filters row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date Filter
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Date Filter",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded = showDateFilterDropdown,
                                    onExpandedChange = {
                                        showDateFilterDropdown = !showDateFilterDropdown
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = if (selectedDateFilter.isEmpty()) "All" else selectedDateFilter,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.dropdown),
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color.LightGray,
                                            cursorColor = Color.Gray,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = if (selectedDateFilter.isEmpty()) Color.LightGray else Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = showDateFilterDropdown,
                                        onDismissRequest = { showDateFilterDropdown = false }
                                    ) {
                                        listOf(
                                            "All",
                                            "1 Week",
                                            "1 Month",
                                            "Date Range"
                                        ).forEach { filter ->
                                            DropdownMenuItem(
                                                text = { Text(filter) },
                                                onClick = {
                                                    selectedDateFilter = filter
                                                    showDateFilterDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Status Filter
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Status Filter",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded = showCategoryDropdown,
                                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = if (selectedCategoryFilter.isEmpty()) "All" else selectedCategoryFilter,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.dropdown),
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color.LightGray,
                                            cursorColor = Color.Gray,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = if (selectedCategoryFilter.isEmpty()) Color.LightGray else Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = showCategoryDropdown,
                                        onDismissRequest = { showCategoryDropdown = false }
                                    ) {
                                        listOf(
                                            "All",
                                            "pending",
                                            "confirmed",
                                            "rejected",
                                            "canceled"
                                        ).forEach { filter ->
                                            DropdownMenuItem(
                                                text = { Text(filter) },
                                                onClick = {
                                                    selectedCategoryFilter = filter
                                                    showCategoryDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Date Range Fields (shown only when Date Range is selected)
                        if (selectedDateFilter == "Date Range") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // From Date Field
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "From Date",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                            .clickable {
                                                isFromDatePicker = true
                                                showDatePicker = true
                                                selectedDate = dateFormatter.parse(fromDate) ?: Date()
                                                Log.d("MeetSpace", "From Date field clicked")
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = fromDate,
                                            onValueChange = {},
                                            readOnly = true,
                                            placeholder = {
                                                Text(
                                                    "Select From Date",
                                                    color = Color.LightGray,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.LightGray,
                                                cursorColor = Color.Gray,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White
                                            ),
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Black
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = false // Disable direct interaction with the field
                                        )
                                    }
                                }

                                // To Date Field
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "To Date",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                            .clickable {
                                                isFromDatePicker = false
                                                showDatePicker = true
                                                selectedDate = dateFormatter.parse(toDate) ?: Date()
                                                Log.d("MeetSpace", "To Date field clicked")
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = toDate,
                                            onValueChange = {},
                                            readOnly = true,
                                            placeholder = {
                                                Text(
                                                    "Select To Date",
                                                    color = Color.LightGray,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.LightGray,
                                                cursorColor = Color.Gray,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White
                                            ),
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Black
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = false // Disable direct interaction with the field
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Original LazyColumn
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (errorMessage != null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = errorMessage ?: "Error loading bookings", color = Color.Red)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(bookings) { item ->
                                    MeetingBookingCard(
                                        bookingId = truncateBookingId(item.bookingId),
                                        roomName = item.roomName.trim(),
                                        host = item.hostEmail,
                                        meetingDate = dateFormate(item.meetingStarttime),
                                        meetingTime = "${formatTime(item.meetingStarttime)} - ${formatTime(item.meetingEndtime)}",
                                        pendingFrom = if (item.meetingType == "internal") "Admin" else "Manager/CEO",
                                        status = item.approvalStatus,
                                        onClick = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "view")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            context.startActivity(intent)
                                        },
                                        onApprove = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "approve")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            context.startActivity(intent)
                                        },
                                        onReject = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "reject")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            context.startActivity(intent)
                                        },
                                        showButtons = source != "history"
                                    )
                                }
                            }
                        }

                        // Custom Date Picker Dialog
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialDisplayMode = DisplayMode.Picker,
                                initialSelectedDateMillis = selectedDate.time,
                                selectableDates = object : SelectableDates {
                                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                        val today = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        return utcTimeMillis >= today
                                    }
                                }
                            )
                            DatePickerDialog(
                                onDismissRequest = {
                                    Log.d("MeetSpace", "DatePickerDialog dismissed")
                                    showDatePicker = false
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            selectedDate = Date(millis)
                                            isDefaultDate = false
                                            if (isFromDatePicker) {
                                                fromDate = dateFormatter.format(selectedDate)
                                            } else {
                                                toDate = dateFormatter.format(selectedDate)
                                            }
                                        }
                                        Log.d("MeetSpace", "DatePickerDialog OK clicked, selectedDate: $selectedDate")
                                        showDatePicker = false
                                    }) {
                                        Text("OK", color = Color(0xFFDD3825), fontFamily = GraphikFontFamily)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        Log.d("MeetSpace", "DatePickerDialog Cancel clicked")
                                        showDatePicker = false
                                    }) {
                                        Text("Cancel", color = Color.LightGray, fontFamily = GraphikFontFamily)
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun dateFormate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return ""
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
fun MeetingBookingCard(
    bookingId: String,
    roomName: String,
    host: String,
    meetingDate: String,
    meetingTime: String,
    pendingFrom: String,
    status: String,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    showButtons: Boolean = true
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
                        text = "Status: $status",
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
                value = "$roomName"
            )
            MeetingDetailItem(
                icon = R.drawable.ic_person,
                label = "Host",
                value = host
            )
            MeetingDetailItem(
                icon = R.drawable.ic_calendar,
                label = "Meeting date",
                value = meetingDate
            )
            MeetingDetailItem(
                icon = R.drawable.pending,
                label = "Meeting time",
                value = meetingTime
            )
            MeetingDetailItem(
                icon = R.drawable.pending,
                label = "Pending from",
                value = pendingFrom
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (showButtons) {

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
                            .background(color = Color(0xFF4CAF50))
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