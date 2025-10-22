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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.window.Dialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.archeGlobal.one.model.VerifyCheckInRequest
import com.archeGlobal.one.model.VerifyCheckInResponse
import com.archeGlobal.one.network.RetrofitClient
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlinx.coroutines.withContext
import retrofit2.Response
import androidx.compose.runtime.derivedStateOf
import com.archeGlobal.one.utils.UserDataManager
import java.util.Calendar
import kotlinx.coroutines.delay

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

    // QR Scanner state
    var showQrScanner by remember { mutableStateOf(false) }
    var selectedBookingId by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf("") }

    val bookings by controller.bookings.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()

    // Filtering logic
    val filteredBookings by remember { derivedStateOf {
        bookings.filter { booking ->
            // Search filter by Booking ID
            val matchesSearch = searchQuery.isBlank() || booking.bookingId.contains(searchQuery, ignoreCase = true)

            // Status filter based on meetingStatus
            val normalizedStatus = booking.meetingStatus.lowercase()
            val matchesStatus = when (selectedCategoryFilter) {
                "All" -> true
                "Booked" -> normalizedStatus.contains("approv") || normalizedStatus == "confirmed" || normalizedStatus == "booked"
                "Rejected" -> normalizedStatus == "rejected"
                "Cancelled" -> normalizedStatus == "cancelled"
                "Requested" -> normalizedStatus == "requested"
                else -> true
            }

            // Date filter based on meetingStarttime
            var matchesDate = true
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val meetingDate = inputFormat.parse(booking.meetingStarttime)
                if (meetingDate != null) {
                    val calMeeting = Calendar.getInstance().apply { time = meetingDate }
                    val today = Calendar.getInstance()
                    when (selectedDateFilter) {
                        "All" -> {}
                        "1 Week" -> {
                            val oneWeekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }
                            matchesDate = calMeeting.after(oneWeekAgo) && calMeeting.before(today)
                        }
                        "1 Month" -> {
                            val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                            matchesDate = calMeeting.after(oneMonthAgo) && calMeeting.before(today)
                        }
                        "Date Range" -> {
                            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val from = dateFormatter.parse(fromDate)
                            val to = dateFormatter.parse(toDate)
                            if (from != null && to != null) {
                                val calFrom = Calendar.getInstance().apply {
                                    time = from
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val calTo = Calendar.getInstance().apply {
                                    time = to
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                matchesDate = !calMeeting.before(calFrom) && !calMeeting.after(calTo)
                            } else {
                                matchesDate = false
                            }
                        }
                        else -> {}
                    }
                } else {
                    matchesDate = false
                }
            } catch (e: Exception) {
                matchesDate = false
            }

            matchesSearch && matchesStatus && matchesDate
        }
    } }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showQrScanner = true
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission is required for QR scanning",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    val detailLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            controller.fetchBookingHistory()
        }
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
                            keyboardActions = KeyboardActions(onSearch = { /* Search triggered */ }),
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
                                        onDismissRequest = { showDateFilterDropdown = false },
                                        modifier = Modifier
                                            .background(Color.White)  // Dropdown background color
                                            .clip(RoundedCornerShape(18.dp))  // Rounded corners
                                    ) {
                                        listOf(
                                            "All",
                                            "1 Week",
                                            "1 Month",
                                            "Date Range"
                                        ).forEach { filter ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = filter,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = Color.Black,
                                                        fontFamily = GraphikFontFamily
                                                    )
                                                },
                                                onClick = {
                                                    selectedDateFilter = filter
                                                    showDateFilterDropdown = false
                                                    if (filter == "Date Range") {
                                                        showDatePicker = true
                                                        isFromDatePicker = true
                                                    }
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
                                        onDismissRequest = { showCategoryDropdown = false },
                                        modifier = Modifier
                                            .background(Color.White)  // Dropdown background color
                                            .clip(RoundedCornerShape(18.dp))  // Rounded corners
                                    ) {
                                        listOf(
                                            "All",
                                            "Booked",
                                            "Cancelled",
                                            "Rejected",
                                            "Requested"
                                        ).forEach { filter ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = filter,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = Color.Black,
                                                        fontFamily = GraphikFontFamily
                                                    )
                                                },
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

                        if (selectedDateFilter == "Date Range") {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Date Range Fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // From Date
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "From",
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
                                            }
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = fromDate,
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Black
                                        )
                                    }
                                }

                                // To Date
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "To",
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
                                            }
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = toDate,
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // LazyColumn with filtered bookings
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (errorMessage != null) {
                            Text(errorMessage ?: "", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (filteredBookings.isEmpty()) {
                            Text("No bookings found", modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(filteredBookings) { item ->
                                    val normalizedStatus = item.approvalStatus.lowercase()
                                    val userEmail = remember { UserDataManager.getInstance(context).getUserData()?.email ?: "" }
                                    MeetingBookingCard(
                                        bookingId = truncateBookingId(item.bookingId),
                                        roomName = item.roomName.trim(),
                                        host = item.hostEmail,
                                        meetingDate = dateFormate(item.meetingStarttime),
                                        meetingTime = "${formatTime(item.meetingStarttime)} - ${formatTime(item.meetingEndtime)}",
                                        pendingFrom = item.pendingFrom,
                                        status = item.meetingStatus,
                                        remark = item.remark,
                                        checkIn = item.checkedIn,
                                        created = dateFormate(item.createdAt),
                                        userEmail = userEmail,
                                        subject = item.meetingSubject,
                                        onClick = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "view")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            detailLauncher.launch(intent)
                                        },
                                        onApprove = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "approve")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            detailLauncher.launch(intent)
                                        },
                                        onReject = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "reject")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            detailLauncher.launch(intent)
                                        },
                                        onCheckIn = {
                                            selectedBookingId = item.bookingId
                                            selectedRoomId = item.roomId
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                showQrScanner = true
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        onCancel = {
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "cancel")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            detailLauncher.launch(intent)
                                        },
                                        onOwnerCancel = { // NEW: Handler for owner-specific cancel
                                            val intent = Intent(context, MeetingHistoryDetailActivity::class.java).apply {
                                                putExtra("source", source)
                                                putExtra("action", "cancel")
                                                putExtra("booking_json", Gson().toJson(item))
                                            }
                                            detailLauncher.launch(intent)
                                        },
                                        source = source ?: "history",
                                        meetingType = item.meetingType,
                                        meetingStarttime = item.meetingStarttime,
                                    )
                                }
                            }
                        }

                        // QR Scanner Dialog
                        if (showQrScanner) {
                            QrScannerDialog(
                                bookingId = selectedBookingId,
                                onDismiss = { showQrScanner = false },
                                onQrCodeScanned = { qrCode ->
                                    if (qrCode == selectedRoomId) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            performCheckIn(selectedBookingId, context, controller) {
                                                showQrScanner = false
                                            }
                                        }
                                    } else {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            android.widget.Toast.makeText(context, "QR code does not match the room", android.widget.Toast.LENGTH_SHORT).show()
                                            showQrScanner = false
                                        }
                                    }
                                }
                            )
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
                                        return utcTimeMillis <= today
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

private suspend fun performCheckIn(bookingId: String, context: android.content.Context, controller: MeetingHistoryController, onSuccess: () -> Unit) {
    try {
        val response = RetrofitClient.apiService.verifyAndCheckIn(bookingId)
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.status == 200 && body.data?.is_valid == true) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, body.data.message, android.widget.Toast.LENGTH_SHORT).show()
                    onSuccess()
                    controller.fetchBookingHistory()
                }
            } else {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, body?.data?.message ?: "Check-in failed", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "API error: ${response.message()}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            android.widget.Toast.makeText(context, "Network error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun QrScannerDialog(
    bookingId: String,
    onDismiss: () -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasScanned by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Scan QR Code For Check-in",
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "for Meeting ID",
                    fontSize = 12.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = bookingId,
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // QR Scanner Preview with matching border
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                // Ensure the view is properly initialized
                                this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                        },
                        update = { view ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(view.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also {
                                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null && !hasScanned) {
                                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                                val scanner = BarcodeScanning.getClient()
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            val qrCode = barcode.displayValue ?: barcode.rawValue
                                                            if (qrCode != null) {
                                                                hasScanned = true
                                                                onQrCodeScanned(qrCode)
                                                                break
                                                            }
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("QRScanner", "Scan failed", e)
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (exc: Exception) {
                                    Log.e("QRScanner", "Use case binding failed", exc)
                                }
                            }, context.mainExecutor)
                        }
                    )
                }

                DisposableEffect(Unit) {
                    onDispose {
                        cameraExecutor.shutdown()
                    }
                }
                // Cancel Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Cancel",
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

private fun dateFormate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return ""
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
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
    remark: String,
    checkIn: String,
    meetingStarttime: String,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCheckIn: () -> Unit,
    onCancel: () -> Unit,
    onOwnerCancel: () -> Unit, // NEW: Callback for owner-specific cancel (no time restriction)
    source: String,
    meetingType: String,
    created: String,
    subject: String,
    userEmail: String // NEW: Logged-in user's email for owner check
) {
    val normalizedStatus = status.lowercase()
    val normalizedType = meetingType.lowercase()
    val normalizePending = pendingFrom.lowercase()

    val isInternal = normalizedType == "internal"
    val isStatus = normalizedStatus == "requested"
    val isPending = normalizePending == "linemanager"
    val isPendingCeo = normalizePending == "ceo"

    val showApproveReject = when (source) {
        "admin" -> isInternal && isStatus
        "linemanager" -> !isInternal && isStatus && isPending
        "ceo" -> !isInternal && isStatus && isPendingCeo
        else -> false
    }

    val pendingFrom = when (pendingFrom) {
        "admin" -> "Admin"
        "linemanager" -> "Reporting Manager"
        "ceo" -> "CEO"
        else -> "Unknown"
    }

    // Existing: Show check-in/cancel for history source
    val showCheckInCancel = source == "history" && normalizedStatus == "booked"

    // NEW: Owner can cancel booked/requested meetings without time restriction (if not in history source)
    val isOwner = userEmail.isNotBlank() && userEmail.equals(host, ignoreCase = true)
    val canOwnerCancel = isOwner && (normalizedStatus == "booked" || normalizedStatus == "requested")
    val showOwnerCancel = source == "history" && canOwnerCancel

    val (backgroundColor, textColor) = when (normalizedStatus) {
        "booked" -> Pair(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000))
        "rejected" -> Pair(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000))
        "cancelled" -> Color.Gray.copy(alpha = 0.15f) to Color.Gray
        else -> Pair(Color(0xFFFFA500).copy(alpha = 0.15f), Color(0xFFFFA500))
    }

    val titleCaseStatus = status.split(" ")
        .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

    val startCalendar = remember(meetingStarttime) {
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            fmt.parse(meetingStarttime)?.let {
                Calendar.getInstance().apply { time = it }
            }
        } catch (e: Exception) { null }
    }

    val checkInWindowEnd = remember(startCalendar) {
        startCalendar?.let { start ->
            Calendar.getInstance().apply {
                timeInMillis = start.timeInMillis + 15 * 60 * 1000
            }
        }
    }

    var minuteTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            minuteTick++
        }
    }

    val now = Calendar.getInstance()

    // Time logic — now safe from Compose compiler confusion
    val isBeforeStart = startCalendar != null && now.before(startCalendar)
    val isWithinWindow = startCalendar != null && checkInWindowEnd != null &&
            now.after(startCalendar) && now.before(checkInWindowEnd)
    val isAfterWindow = checkInWindowEnd != null && now.after(checkInWindowEnd)

    val alreadyCheckedIn = checkIn.lowercase() == "true"
    val showCancelButton = isBeforeStart
    val showCheckInButton = !isBeforeStart
    val enableCheckInButton = isWithinWindow && !alreadyCheckedIn
    val buttonText = when {
        alreadyCheckedIn -> "Checked In"
        isWithinWindow -> "Scan to Check-in"
        else -> "Check-in Expired"
    }

    val showPendingFrom = normalizedStatus == "requested"
    val showRemark = normalizedStatus in listOf("booked", "requested", "cancelled", "rejected")
    val showCheckIn = normalizedStatus == "booked"
    val remarkLabel = when (normalizedStatus) {
        "cancelled" -> "Cancellation reason"
        "rejected" -> "Rejection reason"
        else -> "Remark"
    }
    val remarkIcon = when (normalizedStatus) {
        "cancelled", "rejected" -> R.drawable.remark1
        else -> R.drawable.justification
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
        Column(modifier = Modifier.padding(16.dp)) {
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Status: $titleCaseStatus",
                        fontSize = 12.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.Top) {
                MeetingDetailItem(icon = R.drawable.meetcalender, label = "Created Date", value = created)
                MeetingDetailItem(icon = R.drawable.person_3x, label = "Host Email", value = host)
                MeetingDetailItem(icon = R.drawable.subject, label = "Subject", value = subject)
                MeetingDetailItem(icon = R.drawable.mroomtype, label = "Room", value = roomName)
                MeetingDetailItem(icon = R.drawable.meetcalender, label = "Meeting date", value = meetingDate)
                MeetingDetailItem(icon = R.drawable.pending, label = "Meeting time", value = meetingTime)

                if (showPendingFrom) MeetingDetailItem(icon = R.drawable.mrrompending, label = "Pending from", value = pendingFrom)
                if (showRemark) MeetingDetailItem(icon = remarkIcon, label = remarkLabel, value = remark)

                if (showCheckIn) {
                    MeetingDetailItem(
                        icon = R.drawable.checkinstatus,
                        label = "Check-in Status",
                        value = when {
                            alreadyCheckedIn -> "Checked-in"
                            isAfterWindow -> "Not checked-in"
                            else -> "Pending"
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // MODIFIED: Show buttons if any of approve/reject, check-in/cancel, or owner-cancel is visible
            if (showApproveReject || showCheckInCancel || showOwnerCancel) {
                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showApproveReject) {
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
                    } else if (showCheckInCancel) {
                        if (showCheckInButton) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color = if (enableCheckInButton) Color(0xFF007AFF) else Color.Gray)
                                    .clickable(enabled = enableCheckInButton, onClick = onCheckIn)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.checkin),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = buttonText,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GraphikFontFamily
                                    )
                                }
                            }
                        }

                        if (showCancelButton) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color = PrimaryRed)
                                    .clickable(onClick = onCancel)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily
                                )
                            }
                        }
                    } else if (showOwnerCancel) { // NEW: Render owner cancel button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(color = PrimaryRed)
                                .clickable(onClick = onOwnerCancel)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
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
}

@Composable
fun MeetingDetailItem(
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
                fontSize = 13.sp,
                color = Color.Gray,
                fontFamily = GraphikFontFamily,
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