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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.window.Dialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
                "Approved" -> normalizedStatus.contains("approv") || normalizedStatus == "confirmed"
                "Rejected" -> normalizedStatus == "rejected" || normalizedStatus == "canceled"
                "Pending" -> normalizedStatus == "requested"
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
                                            "Approved",
                                            "Rejected",
                                            "Pending"
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
                                items(filteredBookings) { item ->
                                    val normalizedStatus = item.approvalStatus.lowercase()
                                    MeetingBookingCard(
                                        bookingId = truncateBookingId(item.bookingId),
                                        roomName = item.roomName.trim(),
                                        host = item.hostEmail,
                                        meetingDate = dateFormate(item.meetingStarttime),
                                        meetingTime = "${formatTime(item.meetingStarttime)} - ${formatTime(item.meetingEndtime)}",
                                        pendingFrom = if (item.meetingType == "internal") "Admin" else "Manager/CEO",
                                        status = item.meetingStatus,
                                        remark = item.remark,
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
                                        onCheckIn = {
                                            if (ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.CAMERA
                                                ) == PackageManager.PERMISSION_GRANTED
                                            ) {
                                                selectedBookingId = item.bookingId
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
                                            context.startActivity(intent)
                                        },
                                        source = source ?: "history",
                                        meetingType = item.meetingType
                                    )
                                }
                            }
                        }

                        // QR Scanner Dialog
                        if (showQrScanner) {
                            QrScannerDialog(
                                bookingId = selectedBookingId,
                                onDismiss = { showQrScanner = false },
                                onQrCodeScanned = { scannedQrCode ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            val request =
                                                VerifyCheckInRequest(qr_code = scannedQrCode)
                                            val response: Response<VerifyCheckInResponse> = withContext(Dispatchers.IO) {
                                                RetrofitClient.apiService.verifyAndCheckIn(selectedBookingId, request)
                                            }
                                            if (response.isSuccessful) {
                                                val body = response.body()
                                                if (body?.status == 200 && body.data?.is_valid == true) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Check-in successful: ${body.data.message}",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                    showQrScanner = false
                                                } else {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Check-in failed: ${body?.data?.message ?: "Invalid QR code"}",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            } else {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "API error: HTTP ${response.code()} - ${response.message()}",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Network error: ${e.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
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

@Composable
fun QrScannerDialog(
    bookingId: String,
    onDismiss: () -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

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
                        factory = {
                            PreviewView(context).apply {
                                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
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

        // Setup CameraX and QR scanning
        LaunchedEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val value = barcode.rawValue
                                    if (value != null) {
                                        onQrCodeScanned(value)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("QrScanner", "QR scan failed: ${e.message}")
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("QrScanner", "Camera binding failed: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context))
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
    remark: String,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCheckIn: () -> Unit,
    onCancel: () -> Unit,
    source: String,
    meetingType: String
) {
    val normalizedStatus = status.lowercase()
    val normalizedType = meetingType.lowercase()
    val isInternal = normalizedType == "internal"
    val isPending = normalizedStatus == "requested"
    val isStatus = normalizedStatus == "approved by line manager"

    val showApproveReject = when (source) {
        "admin" -> isInternal && isPending
        "linemanager" -> !isInternal && isPending
        "ceo" -> !isInternal && isStatus
        else -> false
    }

    val showCheckInCancel = source == "history" &&
            (normalizedStatus == "approve" || normalizedStatus == "approved" || normalizedStatus == "approved by admin")

    val (backgroundColor, textColor) = when (normalizedStatus) {
        "confirmed" -> Pair(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000))
        "rejected" , "canceled" -> Pair(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000))
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
                icon = R.drawable.mroomtype,
                label = "Room",
                value = "$roomName"
            )
            MeetingDetailItem(
                icon = R.drawable.person_3x,
                label = "Host",
                value = host
            )
            MeetingDetailItem(
                icon = R.drawable.meetcalender,
                label = "Meeting date",
                value = meetingDate
            )
            MeetingDetailItem(
                icon = R.drawable.pending,
                label = "Meeting time",
                value = meetingTime
            )
            MeetingDetailItem(
                icon = R.drawable.mrrompending,
                label = "Pending from",
                value = pendingFrom
            )
            MeetingDetailItem(
                icon = R.drawable.justification,
                label = "Remark",
                value = remark
            )
            MeetingDetailItem(
                icon = R.drawable.checkinstatus,
                label = "Check-in Status",
                value = remark
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (showApproveReject || showCheckInCancel) {
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(color = Color(0xFF14B8D5))
                                .padding(vertical = 12.dp)
                                .clickable(onClick = onCheckIn),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Check-in",
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
                                .clickable(onClick = onCancel),
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