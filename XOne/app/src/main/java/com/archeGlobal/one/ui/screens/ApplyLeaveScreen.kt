package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.launch
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.AttendanceController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val leaveTypes = listOf(
    "Privilege Leave",
    "Casual Leave",
    "Sick Leave",
    "Paternity Leave",
    "Optional Holiday",
)

private val leaveReasons = listOf("Personal", "Outdoor", "Not Well")

private val dayOptions = listOf("Full Day", "First Half", "Second Half")

private val leaveTypesWithStartEndDay = setOf(
    "Casual Leave",
    "Sick Leave",
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApplyLeaveScreen(
    initialLeaveType: String = "",
    initialDate: LocalDate = LocalDate.now(),
    attendanceController: AttendanceController? = null,
    onBack: () -> Unit,
) {
    val primaryRed = Color(0xFFDD3825)

    var selectedLeaveType by remember { mutableStateOf(initialLeaveType) }
    var leaveTypeExpanded by remember { mutableStateOf(false) }

    var fromDate by remember { mutableStateOf(initialDate) }
    var toDate by remember { mutableStateOf(initialDate) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    var startDay by remember { mutableStateOf("Full Day") }
    var startDayExpanded by remember { mutableStateOf(false) }
    var endDay by remember { mutableStateOf("Full Day") }
    var endDayExpanded by remember { mutableStateOf(false) }

    var selectedHoliday by remember { mutableStateOf("") }
    var holidayExpanded by remember { mutableStateOf(false) }

    var selectedReason by remember { mutableStateOf("") }
    var reasonExpanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val totalDays = (toDate.toEpochDay() - fromDate.toEpochDay() + 1).coerceAtLeast(1)
    val isShortLeave = selectedLeaveType.contains("Short Leave", ignoreCase = true)

    LaunchedEffect(selectedLeaveType) {
        if (isShortLeave) {
            toDate = fromDate
            startTime = null
            endTime = null
        }
    }

    // Use available leave balances from controller if provided, otherwise fallback to default list
    val availableLeaveTypes = if (attendanceController != null && attendanceController.leaveBalances.isNotEmpty()) {
        attendanceController.leaveBalances.map { it.type }
    } else {
        leaveTypes
    }

    val showStartEndDay = !isShortLeave &&
                         selectedLeaveType != "Privilege Leave" &&
                         selectedLeaveType != "Optional Holiday" &&
                         (selectedLeaveType in leaveTypesWithStartEndDay ||
                         (attendanceController?.leaveBalances?.any { it.type == selectedLeaveType } == true))

    val leaveBalance = attendanceController?.leaveBalances?.find {
        it.type.equals(selectedLeaveType, ignoreCase = true)
    }?.balance

    val screenTitle = when {
        selectedLeaveType.isNotEmpty() -> "Apply for $selectedLeaveType"
        else -> "Apply for Leave"
    }

    val reportingManagerName = OtpVerificationController.getUserData()?.userDetails?.reporting_manager ?: ""
    val reportingManagerEmail = OtpVerificationController.getUserData()?.userDetails?.reporting_manager_mail ?: ""

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
                            text = screenTitle,
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

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Leave Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Leave Details",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Leave Type",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = leaveTypeExpanded,
                                onExpandedChange = { leaveTypeExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedLeaveType.ifEmpty { "Select Leave Type" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFF888888),
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFDDDDDD),
                                        focusedBorderColor = primaryRed,
                                        unfocusedTextColor = if (selectedLeaveType.isEmpty()) Color(0xFF888888) else Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = leaveTypeExpanded,
                                    onDismissRequest = { leaveTypeExpanded = false },
                                    modifier = Modifier.background(Color.White),
                                ) {
                                    availableLeaveTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = type,
                                                    fontFamily = GraphikFontFamily,
                                                    fontSize = 14.sp,
                                                    color = Color.Black,
                                                )
                                            },
                                            onClick = {
                                                selectedLeaveType = type
                                                leaveTypeExpanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                        color = if (isShortLeave) Color(0xFF888888) else Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(
                                                if (isShortLeave) Color(0xFFF0F0F0) else Color.White,
                                                RoundedCornerShape(10.dp),
                                            )
                                            .then(
                                                if (!isShortLeave) Modifier.clickable { showToDatePicker = true }
                                                else Modifier
                                            )
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = toDate.format(dateFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (isShortLeave) Color(0xFF888888) else Color.Black,
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

                            // Optional Holiday dropdown (only for Optional Holiday)
                            if (selectedLeaveType == "Optional Holiday") {
                                LaunchedEffect(Unit) {
                                    attendanceController?.fetchOptionalHolidays()
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Select Holidays",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                ExposedDropdownMenuBox(
                                    expanded = holidayExpanded,
                                    onExpandedChange = { holidayExpanded = it },
                                ) {
                                    OutlinedTextField(
                                        value = selectedHoliday.ifEmpty { "Select Optional Holiday" },
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Color(0xFF888888),
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFDDDDDD),
                                            focusedBorderColor = primaryRed,
                                            unfocusedTextColor = if (selectedHoliday.isEmpty()) Color(0xFF888888) else Color.Black,
                                            focusedTextColor = Color.Black,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White,
                                        ),
                                        textStyle = LocalTextStyle.current.copy(
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                        ),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = holidayExpanded,
                                        onDismissRequest = { holidayExpanded = false },
                                        modifier = Modifier.background(Color.White),
                                    ) {
                                        attendanceController?.optionalHolidays?.forEach { holiday ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = holiday,
                                                        fontFamily = GraphikFontFamily,
                                                        fontSize = 14.sp,
                                                        color = Color.Black,
                                                    )
                                                },
                                                onClick = {
                                                    selectedHoliday = holiday
                                                    holidayExpanded = false
                                                    // Parse date from "Name (DD-MM-YYYY)" and set from/to
                                                    val dateStr = holiday.substringAfterLast("(").removeSuffix(")")
                                                    try {
                                                        val holidayDate = java.time.LocalDate.parse(
                                                            dateStr,
                                                            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                                        )
                                                        fromDate = holidayDate
                                                        toDate = holidayDate
                                                    } catch (_: Exception) { }
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            // Start Day / End Day dropdowns (only for certain leave types)
                            if (showStartEndDay) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // Start Day dropdown
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Start Day",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ExposedDropdownMenuBox(
                                            expanded = startDayExpanded,
                                            onExpandedChange = { startDayExpanded = it },
                                        ) {
                                            OutlinedTextField(
                                                value = startDay,
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = Color(0xFF888888),
                                                        modifier = Modifier.size(20.dp),
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    unfocusedBorderColor = Color(0xFFDDDDDD),
                                                    focusedBorderColor = primaryRed,
                                                    unfocusedTextColor = Color.Black,
                                                    focusedTextColor = Color.Black,
                                                    unfocusedContainerColor = Color.White,
                                                    focusedContainerColor = Color.White,
                                                ),
                                                textStyle = LocalTextStyle.current.copy(
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                ),
                                                singleLine = true,
                                            )
                                            ExposedDropdownMenu(
                                                expanded = startDayExpanded,
                                                onDismissRequest = { startDayExpanded = false },
                                                modifier = Modifier.background(Color.White),
                                            ) {
                                                dayOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = option,
                                                                fontFamily = GraphikFontFamily,
                                                                fontSize = 13.sp,
                                                                color = Color.Black,
                                                            )
                                                        },
                                                        onClick = {
                                                            startDay = option
                                                            startDayExpanded = false
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // End Day dropdown
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "End Day",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ExposedDropdownMenuBox(
                                            expanded = endDayExpanded,
                                            onExpandedChange = { endDayExpanded = it },
                                        ) {
                                            OutlinedTextField(
                                                value = endDay,
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = Color(0xFF888888),
                                                        modifier = Modifier.size(20.dp),
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    unfocusedBorderColor = Color(0xFFDDDDDD),
                                                    focusedBorderColor = primaryRed,
                                                    unfocusedTextColor = Color.Black,
                                                    focusedTextColor = Color.Black,
                                                    unfocusedContainerColor = Color.White,
                                                    focusedContainerColor = Color.White,
                                                ),
                                                textStyle = LocalTextStyle.current.copy(
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                ),
                                                singleLine = true,
                                            )
                                            ExposedDropdownMenu(
                                                expanded = endDayExpanded,
                                                onDismissRequest = { endDayExpanded = false },
                                                modifier = Modifier.background(Color.White),
                                            ) {
                                                dayOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = option,
                                                                fontFamily = GraphikFontFamily,
                                                                fontSize = 13.sp,
                                                                color = Color.Black,
                                                            )
                                                        },
                                                        onClick = {
                                                            endDay = option
                                                            endDayExpanded = false
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Short Leave: time pickers
                        if (isShortLeave) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Start Time",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showStartTimePicker = true }
                                            .padding(horizontal = 12.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = startTime?.format(timeFormatter) ?: "Select",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (startTime == null) Color(0xFF888888) else Color.Black,
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "End Time",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showEndTimePicker = true }
                                            .padding(horizontal = 12.dp, vertical = 16.dp),
                                    ) {
                                        Text(
                                            text = endTime?.format(timeFormatter) ?: "Select",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (endTime == null) Color(0xFF888888) else Color.Black,
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Max duration: 2 hours",
                                fontFamily = GraphikFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF888888),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    // Start time picker dialog
                    if (showStartTimePicker) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = startTime?.hour ?: LocalTime.now().hour,
                            initialMinute = startTime?.minute ?: 0,
                            is24Hour = false,
                        )
                        AlertDialog(
                            onDismissRequest = { showStartTimePicker = false },
                            title = {
                                Text("Select Start Time", fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold)
                            },
                            text = { TimePicker(state = timePickerState) },
                            confirmButton = {
                                TextButton(onClick = {
                                    startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    if (endTime != null && !endTime!!.isAfter(startTime)) endTime = null
                                    showStartTimePicker = false
                                }) {
                                    Text("OK", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showStartTimePicker = false }) {
                                    Text("Cancel", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                        )
                    }

                    // End time picker dialog
                    if (showEndTimePicker) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = endTime?.hour ?: (startTime?.hour?.plus(1) ?: LocalTime.now().hour),
                            initialMinute = endTime?.minute ?: (startTime?.minute ?: 0),
                            is24Hour = false,
                        )
                        AlertDialog(
                            onDismissRequest = { showEndTimePicker = false },
                            title = {
                                Text("Select End Time", fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold)
                            },
                            text = { TimePicker(state = timePickerState) },
                            confirmButton = {
                                TextButton(onClick = {
                                    endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    showEndTimePicker = false
                                }) {
                                    Text("OK", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEndTimePicker = false }) {
                                    Text("Cancel", color = primaryRed, fontFamily = GraphikFontFamily)
                                }
                            },
                        )
                    }

                    // From date picker dialog
                    if (showFromDatePicker) {
                        val datePickerState = rememberDatePickerState(
                            initialDisplayMode = DisplayMode.Picker,
                            initialSelectedDateMillis = System.currentTimeMillis(),
                        )
                        DatePickerDialog(
                            onDismissRequest = { showFromDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selected = LocalDate.ofEpochDay(millis / 86400000L)
                                        fromDate = selected
                                        if (isShortLeave) toDate = selected
                                        else if (toDate.isBefore(selected)) toDate = selected
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
                            initialSelectedDateMillis = System.currentTimeMillis(),
                            selectableDates = object : androidx.compose.material3.SelectableDates {
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
                                        toDate = LocalDate.ofEpochDay(millis / 86400000L)
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
                                text = "Leave Reason",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = reasonExpanded,
                                    onExpandedChange = { reasonExpanded = it },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    OutlinedTextField(
                                        value = selectedReason.ifEmpty { "Select" },
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Color(0xFF888888),
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFDDDDDD),
                                            focusedBorderColor = primaryRed,
                                            unfocusedTextColor = if (selectedReason.isEmpty()) Color(0xFF888888) else Color.Black,
                                            focusedTextColor = Color.Black,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White,
                                        ),
                                        textStyle = LocalTextStyle.current.copy(
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                        ),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = reasonExpanded,
                                        onDismissRequest = { reasonExpanded = false },
                                        modifier = Modifier.background(Color.White),
                                    ) {
                                        leaveReasons.forEach { reason ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = reason,
                                                        fontFamily = GraphikFontFamily,
                                                        fontSize = 14.sp,
                                                        color = Color.Black,
                                                    )
                                                },
                                                onClick = {
                                                    selectedReason = reason
                                                    reasonExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }

                                // Leave Balance indicator
                                if (leaveBalance != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.calender22),
                                                contentDescription = null,
                                                tint = Color(0xFF555555),
                                                modifier = Modifier.size(20.dp),
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = leaveBalance.toInt().toString(),
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 20.sp,
                                                color = Color.Black,
                                            )
                                        }
                                        Text(
                                            text = "Leave Balance",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF888888),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Description",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black,
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

                    // Approver Card
                    ApproverCard(
                        name = reportingManagerName,
                        email = reportingManagerEmail,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Apply Leave button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Button(
                        onClick = {
                            val userData = UserDataManager.getInstance(context).getUserData()
                            val employeeName = userData?.name ?: ""
                            val employeeCode = userData?.employeeId ?: ""
                            val userEmail = userData?.email ?: ""

                            // 1. Basic Selection Validations
                            if (selectedLeaveType.isEmpty()) {
                                Toast.makeText(context, "Please select leave type", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedLeaveType == "Optional Holiday" && selectedHoliday.isEmpty()) {
                                Toast.makeText(context, "Please select an optional holiday", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedReason.isEmpty()) {
                                Toast.makeText(context, "Please select leave reason", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 2. Day of Week Validations
                            val fromDayOfWeek = fromDate.dayOfWeek
                            val toDayOfWeek = toDate.dayOfWeek
                            val isWeekend = fromDayOfWeek == java.time.DayOfWeek.SATURDAY || fromDayOfWeek == java.time.DayOfWeek.SUNDAY ||
                                            toDayOfWeek == java.time.DayOfWeek.SATURDAY || toDayOfWeek == java.time.DayOfWeek.SUNDAY

                            // Weekend Restriction (except Outdoor)
                            if (!selectedLeaveType.contains("Outdoor", ignoreCase = true)) {
                                if (isWeekend) {
                                    Toast.makeText(context, "Leaves cannot be applied on weekends.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            }

                            // 3. Single Day Validation for restricted leaves
                            val isWellness = selectedLeaveType.contains("Women Wellness", ignoreCase = true)
                            val isSickLeave = selectedLeaveType.equals("Sick Leave", ignoreCase = true)
                            val isCasualLeave = selectedLeaveType.equals("Casual Leave", ignoreCase = true)
                            val isProbationary = selectedLeaveType.equals("Probationary Leave", ignoreCase = true)
                            val isShortLeaveSubmit = selectedLeaveType.contains("Short Leave", ignoreCase = true)

                            if (isShortLeaveSubmit) {
                                if (startTime == null || endTime == null) {
                                    Toast.makeText(context, "Please select start and end time for Short Leave.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!endTime!!.isAfter(startTime)) {
                                    Toast.makeText(context, "End time must be after start time.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes()
                                if (durationMinutes > 120) {
                                    Toast.makeText(context, "Short Leave cannot exceed 2 hours.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            } else if (isSickLeave) {
                                if (totalDays > 2) {
                                    Toast.makeText(context, "Sick Leave can only be applied for a maximum of 2 consecutive days.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            } else if (isCasualLeave || isProbationary || isWellness) {
                                if (fromDate != toDate) {
                                    Toast.makeText(context, "$selectedLeaveType can only be applied for a single day.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            }

                            // 4. WFH Specific Day Restriction (Only Tue/Wed/Thu)
                            if (selectedLeaveType.contains("Work From Home", ignoreCase = true) || 
                                selectedLeaveType.contains("WFH", ignoreCase = true)) {
                                val allowedWfhDays = listOf(java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY)
                                if (fromDayOfWeek !in allowedWfhDays || toDayOfWeek !in allowedWfhDays) {
                                    Toast.makeText(context, "Work From Home can only be applied on Tuesday, Wednesday, or Thursday.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            }

                            // 5. Complex Async Validations
                            isSubmitting = true
                            scope.launch {
                                try {
                                    // A. Short Leave Checks (2 per day, no other requests on same day)
                                    if (isShortLeaveSubmit) {
                                        val checkRequest = com.archeGlobal.one.model.LeaveCheckRequest(
                                            email = userEmail,
                                            startDate = fromDate.toString(),
                                            endDate = fromDate.toString()
                                        )
                                        val checkResponse = RetrofitClient.apiService.leaveCheck(checkRequest)
                                        if (checkResponse.isSuccessful) {
                                            val breakdown = checkResponse.body()?.data?.breakdown ?: emptyList()
                                            val activeEntries = breakdown.filter {
                                                it.status.lowercase() == "pending" || it.status.lowercase() == "approved"
                                            }
                                            val shortLeaveCount = activeEntries.count {
                                                it.requestType.contains("Short Leave", ignoreCase = true)
                                            }
                                            if (shortLeaveCount >= 2) {
                                                Toast.makeText(context, "You can apply only 2 Short Leaves per day.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }
                                            val hasOtherRequest = activeEntries.any {
                                                !it.requestType.contains("Short Leave", ignoreCase = true)
                                            }
                                            if (hasOtherRequest) {
                                                Toast.makeText(context, "You already have a pending or approved request on this day. Short Leave cannot be applied.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }
                                        }
                                    }

                                    // B. WFH Weekly Limit Check
                                    if (selectedLeaveType.contains("Work From Home", ignoreCase = true) || 
                                        selectedLeaveType.contains("WFH", ignoreCase = true)) {
                                        
                                        val weekStart = fromDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
                                        val weekEnd = fromDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).toString()
                                        
                                        val checkRequest = com.archeGlobal.one.model.LeaveCheckRequest(
                                            email = userEmail,
                                            startDate = weekStart,
                                            endDate = weekEnd
                                        )
                                        val checkResponse = RetrofitClient.apiService.leaveCheck(checkRequest)
                                        if (checkResponse.isSuccessful) {
                                            val breakdown = checkResponse.body()?.data?.breakdown ?: emptyList()
                                            val wfhCount = breakdown.count {
                                                (it.requestType.contains("Work From Home", ignoreCase = true) ||
                                                 it.requestType.contains("WFH", ignoreCase = true)) &&
                                                (it.status.lowercase() == "pending" || it.status.lowercase() == "approved")
                                            }
                                            if (wfhCount >= 1) {
                                                Toast.makeText(context, "You can apply only one Work From Home per week.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }
                                        }
                                    }

                                    // B. Women Wellness Monthly Limit Check
                                    if (selectedLeaveType.contains("KNT Women Wellness Leave", ignoreCase = true) ||
                                        selectedLeaveType.contains("Women Wellness", ignoreCase = true)) {
                                        
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
                                            val wellnessConflict = breakdown.any {
                                                (it.requestType.contains("KNT Women Wellness Leave", ignoreCase = true) ||
                                                 it.requestType.contains("Women Wellness", ignoreCase = true)) &&
                                                (it.status.lowercase() == "pending" || it.status.lowercase() == "approved")
                                            }
                                            if (wellnessConflict) {
                                                Toast.makeText(context, "You can apply only one KNT Women Wellness Leave per month.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }
                                        }
                                    }

                                    // C. Monthly Limit and Adjacent Leave Check (No Clubbing)
                                    val restrictedTypes = listOf(
                                        "Casual Leave", "Sick Leave", "Privilege Leave", 
                                        "Maternity Leaves", "Paternity Leaves", "Optional Holiday",
                                        "Probationary Leave"
                                    )
                                    
                                    if (restrictedTypes.any { it.equals(selectedLeaveType, ignoreCase = true) }) {
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
                                            
                                            // 1. Rule: Monthly Limits (2 CL and 2 SL allowed per month)
                                            if (selectedLeaveType == "Casual Leave" || selectedLeaveType == "Sick Leave" || selectedLeaveType == "Probationary Leave") {
                                                val monthlyCount = breakdown.count {
                                                    it.requestType.equals(selectedLeaveType, ignoreCase = true) &&
                                                    (it.status.lowercase() == "pending" || it.status.lowercase() == "approved")
                                                }
                                                
                                                val limit = if (selectedLeaveType == "Probationary Leave") 1 else 2
                                                
                                                if (monthlyCount >= limit) {
                                                    Toast.makeText(context, "$selectedLeaveType limit reached ($limit per month).", Toast.LENGTH_LONG).show()
                                                    isSubmitting = false
                                                    return@launch
                                                }
                                            }

                                            // 2. Rule: No Clubbing (Adjacent Leave Check)
                                            val prevDay = fromDate.minusDays(1).toString()
                                            val nextDay = toDate.plusDays(1).toString()
                                            
                                            val adjacentConflict = breakdown.any { entry ->
                                                val entryStatus = entry.status.lowercase()
                                                val entryType = entry.requestType ?: ""
                                                val isRestrictedEntry = restrictedTypes.any { entryType.contains(it, ignoreCase = true) }
                                                val isAdjacentDate = entry.requestDate == prevDay || entry.requestDate == nextDay
                                                
                                                val isApprovedOrPending = entryStatus == "pending" || entryStatus == "approved"
                                                
                                                if (isRestrictedEntry && isAdjacentDate && isApprovedOrPending) {
                                                    // SPECIAL CASE: 2 consecutive SL are ALLOWED (SL + SL)
                                                    val isCurrentSick = selectedLeaveType.contains("Sick", ignoreCase = true)
                                                    val isEntrySick = entryType.contains("Sick", ignoreCase = true)
                                                    
                                                    if (isCurrentSick && isEntrySick) {
                                                        false // Allowed
                                                    } else {
                                                        // All other restricted adjacencies are blocked
                                                        // This includes CL + CL, CL + SL, PL + CL, etc.
                                                        true // Conflict
                                                    }
                                                } else {
                                                    false
                                                }
                                            }

                                            if (adjacentConflict) {
                                                Toast.makeText(context, "No clubbing allowed: $selectedLeaveType cannot be adjacent to another restricted leave.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }

                                            // 3. Rule: Check for existing request on same dates
                                            val dateConflict = breakdown.any { entry ->
                                                val entryDate = java.time.LocalDate.parse(entry.requestDate)
                                                val isOverlap = !entryDate.isBefore(fromDate) && !entryDate.isAfter(toDate)
                                                isOverlap && (entry.status.lowercase() == "pending" || entry.status.lowercase() == "approved")
                                            }
                                            if (dateConflict) {
                                                Toast.makeText(context, "A leave request already exists for these dates.", Toast.LENGTH_LONG).show()
                                                isSubmitting = false
                                                return@launch
                                            }
                                        }
                                    } else if (!selectedLeaveType.contains("Work From Home", ignoreCase = true) && 
                                               !selectedLeaveType.contains("WFH", ignoreCase = true) &&
                                               !selectedLeaveType.contains("Women Wellness", ignoreCase = true)) {
                                        
                                        // Standard check for other leave types (not already checked above)
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
                                            Toast.makeText(context, "A request already exists for these dates. Please change the date.", Toast.LENGTH_LONG).show()
                                            isSubmitting = false
                                            return@launch
                                        }
                                    }

                                    // 6. Final Submission
                                    if (isShortLeaveSubmit) {
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
                                            Toast.makeText(context, "Attendance record not found. Please punch in before applying Short Leave.", Toast.LENGTH_LONG).show()
                                            isSubmitting = false
                                            return@launch
                                        }
                                        val timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                                        val shortRequest = com.archeGlobal.one.model.ShortLeaveCreateRequest(
                                            attendanceId = attendanceId,
                                            startTime = startTime!!.format(timeFmt),
                                            endTime = endTime!!.format(timeFmt),
                                            reason = selectedReason,
                                        )
                                        android.util.Log.d("ShortLeave", "Request: attendanceId=$attendanceId startTime=${shortRequest.startTime} endTime=${shortRequest.endTime} reason=${shortRequest.reason}")
                                        val response = RetrofitClient.apiService.createShortLeaveRequest(shortRequest)
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, response.body()?.message ?: "Short Leave request created successfully", Toast.LENGTH_LONG).show()
                                            attendanceController?.fetchLeaveBalances()
                                            onBack()
                                        } else {
                                            val errorMsg = try {
                                                response.errorBody()?.string()
                                                    ?.let { org.json.JSONObject(it).optString("message") }
                                                    ?.takeIf { it.isNotBlank() }
                                            } catch (_: Exception) { null }
                                            Toast.makeText(context, errorMsg ?: response.body()?.message ?: "Failed to create Short Leave request (${response.code()})", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        val apiRequest = com.archeGlobal.one.model.CreateLeaveRequest(
                                            employeeName = employeeName,
                                            employeeCode = employeeCode,
                                            startDate = fromDate.toString(),
                                            endDate = toDate.toString(),
                                            requestType = selectedLeaveType,
                                            leaveDuration = when (startDay) {
                                                "First Half" -> "First Half"
                                                "Second Half" -> "Second Half"
                                                else -> "Full"
                                            },
                                            description = description,
                                            reason = selectedReason,
                                        )
                                        val response = RetrofitClient.apiService.createLeaveRequest(apiRequest)
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, response.body()?.message ?: "Request created successfully", Toast.LENGTH_LONG).show()
                                            attendanceController?.fetchLeaveBalances()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, response.body()?.message ?: "Failed to create request", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ShortLeave", "Submit error", e)
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
                        val buttonText = if (selectedLeaveType.isNotEmpty()) "Submit $selectedLeaveType Request" else "Submit Leave"
                        Text(
                            text = buttonText,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun ApproverCard(
    name: String,
    email: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Approver",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDD3825)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifEmpty { "—" },
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color.Black,
                    )
                    if (email.isNotEmpty()) {
                        Text(
                            text = email,
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Reporting\nManager",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp,
                    )
                }
            }
        }
    }
}
