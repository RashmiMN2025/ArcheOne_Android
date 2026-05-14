package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.model.CreateLeaveRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.launch
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.archeGlobal.one.controller.AttendanceController

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApplyOutdoorDutyScreen(
    initialDate: LocalDate = LocalDate.now(),
    attendanceController: AttendanceController? = null,
    onBack: () -> Unit,
) {
    val primaryRed = Color(0xFFDD3825)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    var fromDate by remember { mutableStateOf(initialDate) }
    var toDate by remember { mutableStateOf(initialDate) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    var workType by remember { mutableStateOf("Outdoor Duty") }
    var workTypeExpanded by remember { mutableStateOf(false) }
    val workTypes = listOf("Outdoor Duty", "Short Leave")

    var inTime by remember { mutableStateOf(LocalTime.of(9, 30)) }
    var outTime by remember { mutableStateOf(LocalTime.of(18, 30)) }
    var showInTimePicker by remember { mutableStateOf(false) }
    var showOutTimePicker by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val totalDays = (toDate.toEpochDay() - fromDate.toEpochDay() + 1).coerceAtLeast(1)

    val reportingManagerName = OtpVerificationController.getUserData()?.userDetails?.reporting_manager ?: ""
    val reportingManagerEmail = OtpVerificationController.getUserData()?.userDetails?.reporting_manager_mail ?: ""
    val divisionalHeadName = OtpVerificationController.getUserData()?.userDetails?.divisional_head ?: ""
    val divisionalHeadEmail = OtpVerificationController.getUserData()?.userDetails?.divisional_head_mail ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WelcomeBackgroundTop,
                            WelcomeBackgroundMiddle,
                            WelcomeBackgroundBottom,
                        ),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (workType == "Short Leave") "Apply for Short Leave" else "Apply for Outdoor Duty",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.Black,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                    ),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Outdoor/Short Leave Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Outdoor Details",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Work Type",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = workTypeExpanded,
                                onExpandedChange = { workTypeExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = workType.ifEmpty { "Select Leave Type" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = workTypeExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFDDDDDD),
                                        focusedBorderColor = primaryRed,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = workTypeExpanded,
                                    onDismissRequest = { workTypeExpanded = false },
                                    modifier = Modifier.background(Color.White),
                                ) {
                                    workTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = type,
                                                    fontFamily = GraphikFontFamily,
                                                    color = Color.Black,
                                                    fontSize = 14.sp,
                                                )
                                            },
                                            onClick = {
                                                workType = type
                                                workTypeExpanded = false
                                                if (type == "Short Leave") {
                                                    toDate = fromDate
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // From / To / Total Days row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "From",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color(0xFF888888),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showFromDatePicker = true }
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = fromDate.format(dateFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "To",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color(0xFF888888),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showToDatePicker = true }
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = toDate.format(dateFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalDays",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 22.sp,
                                        color = Color.Black,
                                    )
                                    Text(
                                        text = "Total Day(s)",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFF888888),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // In Time / Out Time row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (workType == "Short Leave") "Start Time" else "In Time",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showInTimePicker = true }
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = inTime.format(timeFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (workType == "Short Leave") "End Time" else "Out Time",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showOutTimePicker = true }
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = outTime.format(timeFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (workType == "Short Leave") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Short leave allows maximum 2 hours and 2 requests per month.",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // In Time picker dialog
                    if (showInTimePicker) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = inTime.hour,
                            initialMinute = inTime.minute,
                            is24Hour = false,
                        )
                        AlertDialog(
                            onDismissRequest = { showInTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    inTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    showInTimePicker = false
                                }) {
                                    Text("OK", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showInTimePicker = false }) {
                                    Text("Cancel", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            text = { TimePicker(state = timePickerState) },
                        )
                    }

                    // Out Time picker dialog
                    if (showOutTimePicker) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = outTime.hour,
                            initialMinute = outTime.minute,
                            is24Hour = false,
                        )
                        AlertDialog(
                            onDismissRequest = { showOutTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    outTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    showOutTimePicker = false
                                }) {
                                    Text("OK", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showOutTimePicker = false }) {
                                    Text("Cancel", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            text = { TimePicker(state = timePickerState) },
                        )
                    }

                    // From date picker dialog
                    if (showFromDatePicker) {
                        val datePickerState = rememberDatePickerState(
                            initialDisplayMode = DisplayMode.Picker,
                            initialSelectedDateMillis = fromDate.toEpochDay() * 86400000L,
                        )
                        DatePickerDialog(
                            onDismissRequest = { showFromDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selected = LocalDate.ofEpochDay(millis / 86400000L)
                                        fromDate = selected
                                        if (toDate.isBefore(selected) || workType == "Short Leave") {
                                            toDate = selected
                                        }
                                    }
                                    showFromDatePicker = false
                                }) {
                                    Text("OK", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showFromDatePicker = false }) {
                                    Text("Cancel", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // To date picker dialog
                    if (showToDatePicker) {
                        val datePickerState = rememberDatePickerState(
                            initialDisplayMode = DisplayMode.Picker,
                            initialSelectedDateMillis = toDate.toEpochDay() * 86400000L,
                            selectableDates = object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    return utcTimeMillis >= fromDate.toEpochDay() * 86400000L
                                }
                            },
                        )
                        DatePickerDialog(
                            onDismissRequest = { showToDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selected = LocalDate.ofEpochDay(millis / 86400000L)
                                        if (workType == "Short Leave" && selected != fromDate) {
                                            Toast.makeText(context, "Short leave can only be applied for a single day", Toast.LENGTH_SHORT).show()
                                        } else {
                                            toDate = selected
                                        }
                                    }
                                    showToDatePicker = false
                                }) {
                                    Text("OK", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showToDatePicker = false }) {
                                    Text("Cancel", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // Reason & Description Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Reason & Description",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Description",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color(0xFF555555),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        text = "Enter your message here",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 13.sp,
                                        color = Color(0xFFAAAAAA),
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFDDDDDD),
                                    focusedBorderColor = Color(0xFFDDDDDD),
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                ),
                                maxLines = 5,
                            )
                        }
                    }

                    // Approver 1 — Reporting Manager
                    ApproverCard(
                        name = reportingManagerName,
                        email = reportingManagerEmail,
                        title = "Approver 1",
                        roleLabel = "Reporting\nManager",
                        roleBackground = Color(0xFFE8F5E9),
                        roleTextColor = Color(0xFF2E7D32),
                    )

                    // Approver 2 — Divisional Head
                    ApproverCard(
                        name = divisionalHeadName,
                        email = divisionalHeadEmail,
                        title = "Approver 2",
                        roleLabel = "Divisional\nHead",
                        roleBackground = Color(0xFFE3F2FD),
                        roleTextColor = Color(0xFF1565C0),
                    )

                    // Submit button
                    Button(
                        onClick = {
                            val duration = java.time.Duration.between(inTime, outTime)
                            val totalHours = duration.toMinutes() / 60.0

                            if (workType == "Outdoor Duty") {
                                if (!outTime.isAfter(inTime)) {
                                    Toast.makeText(context, "Out time must be after in time.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            } else if (workType == "Short Leave") {
                                if (fromDate == java.time.LocalDate.now()) {
                                    Toast.makeText(context, "Short Leave cannot be applied for today.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                // Validation: Max 2 hours
                                if (totalHours > 2.0 || totalHours <= 0) {
                                    Toast.makeText(context, "Short Leave can be applied for a maximum of 2 hours only", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            }

                            val userData = UserDataManager.getInstance(context).getUserData()
                            val employeeName = userData?.name ?: ""
                            val employeeCode = userData?.employeeId ?: ""
                            val userEmail = userData?.email ?: ""

                            isSubmitting = true
                            scope.launch {
                                try {
                                    // 1. Monthly validation for Short Leave (max 2 requests)
                                    if (workType == "Short Leave") {
                                        val monthStart = fromDate.withDayOfMonth(1).toString()
                                        val monthEnd = fromDate.withDayOfMonth(fromDate.lengthOfMonth()).toString()

                                        val checkRequest = com.archeGlobal.one.model.LeaveCheckRequest(
                                            email = userEmail,
                                            startDate = monthStart,
                                            endDate = monthEnd
                                        )
                                        val checkResponse = RetrofitClient.apiService.leaveCheck(checkRequest)

                                        if (checkResponse.isSuccessful) {
                                            val breakdown = checkResponse.body()?.data?.breakdown ?: emptyList()
                                            val shortLeaveCount = breakdown.count {
                                                it.requestType.contains("Short Leave", ignoreCase = true) &&
                                                (it.status.lowercase() == "pending" || it.status.lowercase() == "approved")
                                            }

                                            if (shortLeaveCount >= 2) {
                                                Toast.makeText(context, "You can apply only 2 Short Leaves per month.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }
                                        }
                                    }

                                    // 2. Pre-check for conflicts on specific dates
                                    val checkRequest = com.archeGlobal.one.model.LeaveCheckRequest(
                                        email = userEmail,
                                        startDate = fromDate.toString(),
                                        endDate = toDate.toString()
                                    )
                                    val checkResponse = RetrofitClient.apiService.leaveCheck(checkRequest)
                                    
                                    val conflicts = checkResponse.body()?.data?.breakdown?.filter { 
                                        it.status.lowercase() == "pending" || it.status.lowercase() == "approved"
                                    } ?: emptyList()

                                    if (checkResponse.isSuccessful && conflicts.isNotEmpty()) {
                                        Toast.makeText(context, "A request is already pending for the selected date.", Toast.LENGTH_LONG).show()
                                        isSubmitting = false
                                        return@launch
                                    }

                                    // 3. Final Submission
                                    val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")
                                    if (workType == "Short Leave") {
                                        val rangeReq = com.archeGlobal.one.model.AttendanceRequest(
                                            employeeEmail = userEmail,
                                            startDate = fromDate.toString(),
                                            endDate = fromDate.toString()
                                        )
                                        val rangeResp = try {
                                            RetrofitClient.apiService.getAttendanceRecords(rangeReq)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        val attendanceId = rangeResp?.body()?.data?.firstOrNull()?.id
                                        
                                        if (attendanceId == null) {
                                            Toast.makeText(context, "Short Leave cannot be applied as attendance is not available for the selected date.", Toast.LENGTH_LONG).show()
                                            isSubmitting = false
                                            return@launch
                                        }
                                        val shortRequest = com.archeGlobal.one.model.ShortLeaveCreateRequest(
                                            attendanceId = attendanceId,
                                            startTime = inTime.format(timeFmt),
                                            endTime = outTime.format(timeFmt),
                                            reason = description,
                                        )
                                        val response = RetrofitClient.apiService.createShortLeaveRequest(shortRequest)
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, response.body()?.message ?: "Short leave request submitted successfully!", Toast.LENGTH_LONG).show()
                                            attendanceController?.fetchLeaveBalances()
                                            onBack()
                                        } else {
                                            val errorMsg = try {
                                                response.errorBody()?.string()
                                                    ?.let { org.json.JSONObject(it).optString("message") }
                                                    ?.takeIf { it.isNotBlank() }
                                            } catch (_: Exception) { null }
                                            Toast.makeText(context, errorMsg ?: response.body()?.message ?: "Failed to submit Short Leave (${response.code()})", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        val apiRequest = CreateLeaveRequest(
                                            employeeName = employeeName,
                                            employeeCode = employeeCode,
                                            startDate = fromDate.toString(),
                                            endDate = toDate.toString(),
                                            requestType = "Outdoor",
                                            leaveDuration = "Full",
                                            description = description,
                                            reason = description,
                                            punchIn = inTime.format(timeFmt),
                                            punchOut = outTime.format(timeFmt),
                                        )
                                        val response = RetrofitClient.apiService.createLeaveRequest(apiRequest)
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, response.body()?.message ?: "Request submitted successfully", Toast.LENGTH_LONG).show()
                                            attendanceController?.fetchLeaveBalances()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, response.body()?.message ?: "Failed to submit request", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                    ) {
                            Text(
                                text = "Submit ${workType} Request",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
