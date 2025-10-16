package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.MeetingRoomListActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.MeetSpaceController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.filled.History
import com.archeGlobal.one.MeetingHistoryActivity
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetSpaceScreen(
    controller: MeetSpaceController,
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val locations by controller.locations.collectAsState()
    val userRoles by controller.userRoles.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()

    var selectedLocation by remember { mutableStateOf("") }
    var showLocationDropdown by remember { mutableStateOf(false) }
    var selectedMeetingType by remember { mutableStateOf("") }
    var showMeetingTypeDropdown by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<Date>(Calendar.getInstance().time) } // Default to current date
    var isDefaultDate by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
    }
    val apiDateTimeFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
    }
    val dateFormatter = remember {
        SimpleDateFormat("yyyy MMM dd", java.util.Locale.getDefault())
    }
    val currentCalendar = remember { Calendar.getInstance() }
    val fromCalendar = remember { currentCalendar.clone() as Calendar }
    var fromTime by remember { mutableStateOf(timeFormatter.format(fromCalendar.time)) } // Set default to current time
    var showFromTimePicker by remember { mutableStateOf(false) }
    val toCalendar =
        remember {
            val cal = currentCalendar.clone() as Calendar
            cal.add(Calendar.HOUR_OF_DAY, 1)
            cal
        }
    var toTime by remember { mutableStateOf(timeFormatter.format(toCalendar.time)) }
    var showToTimePicker by remember { mutableStateOf(false) }

    var numberOfAttendees by remember { mutableStateOf("") }

//    val locations = listOf("Bengaluru", "New Delhi", "Mumbai", "Bangalore")
    val meetingTypes = listOf("Internal Meeting", "Meeting with Guest")

    fun parseTime(time: String): Triple<Int, Int, Boolean> {
        try {
            val date = timeFormatter.parse(time)
            val cal = Calendar.getInstance()
            cal.time = date
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val isAM = cal.get(Calendar.AM_PM) == Calendar.AM
            return Triple(hour % 12, minute, isAM)
        } catch (e: Exception) {
            Log.e("MeetSpace", "Error parsing time: $time", e)
            return Triple(0, 0, true) // Fallback to midnight AM
        }
    }

    @Composable
    fun CustomTimePicker(
        initialHour: Int,
        initialMinute: Int,
        initialIsAM: Boolean,
        onTimeSelected: (Int, Int) -> Unit,
        onDismiss: () -> Unit,
    ) {
        var selectedHour by remember { mutableStateOf(if (initialHour == 0) 12 else initialHour) }
        var selectedMinute by remember { mutableStateOf(initialMinute) }
        var isAM by remember { mutableStateOf(initialIsAM) }
        var isSelectingMinute by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier =
                    Modifier
                        .width(340.dp)
                        .padding(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Select Time",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isSelectingMinute) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier =
                                Modifier
                                    .size(260.dp)
                                    .background(Color(0xFFEBEBEB), shape = CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            with(LocalDensity.current) {
                                val radius = 105.dp.toPx()
                                for (i in 0 until 12) {
                                    val angle = Math.toRadians((i * 30 - 60).toDouble())
                                    val x = cos(angle) * radius
                                    val y = sin(angle) * radius
                                    val hour = if (i == 0) 12 else i
                                    Box(
                                        modifier =
                                            Modifier
                                                .offset {
                                                    IntOffset(
                                                        x = x.toInt(),
                                                        y = y.toInt(),
                                                    )
                                                }.size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selectedHour == hour) Color(0xFFDD3825) else Color.Transparent,
                                                ).border(
                                                    1.dp,
                                                    if (selectedHour == hour) Color(0xFFDD3825) else Color.Transparent,
                                                    CircleShape,
                                                ).clickable {
                                                    selectedHour = hour
                                                    isSelectingMinute = true // Switch to minute selection
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = hour.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = GraphikFontFamily,
                                            color = if (selectedHour == hour) Color.White else Color.Black,
                                        )
                                    }
                                }
                                // AM/PM toggle in center
                                Row(
                                    modifier = Modifier.align(Alignment.Center),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Button(
                                        onClick = { isAM = true },
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (isAM) Color(0xFFDD3825) else Color(0xFFF8F8F0),
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 4.dp),
                                    ) {
                                        Text(
                                            text = "AM",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = GraphikFontFamily,
                                            color = if (isAM) Color.White else Color(0xFFDD3825),
                                        )
                                    }
                                    Button(
                                        onClick = { isAM = false },
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (!isAM) Color(0xFFDD3825) else Color(0xFFF8F8F0),
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = "PM",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = GraphikFontFamily,
                                            color = if (!isAM) Color.White else Color(0xFFDD3825),
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier =
                                Modifier
                                    .size(260.dp)
                                    .background(Color(0xFFEBEBEB), shape = CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            with(LocalDensity.current) {
                                val radius = 100.dp.toPx()
                                for (i in 0 until 12) {
                                    val angle = Math.toRadians((i * 30 - 60).toDouble())
                                    val x = cos(angle) * radius
                                    val y = sin(angle) * radius
                                    val minute = (i * 5) % 60
                                    Box(
                                        modifier =
                                            Modifier
                                                .offset {
                                                    IntOffset(
                                                        x = x.toInt(),
                                                        y = y.toInt(),
                                                    )
                                                }.size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selectedMinute == minute) Color(0xFFDD3825) else Color.Transparent,
                                                ).border(
                                                    1.dp,
                                                    if (selectedMinute == minute) Color(0xFFDD3825) else Color.Transparent,
                                                    CircleShape,
                                                ).clickable {
                                                    selectedMinute = minute
                                                    val hour24 =
                                                        if (selectedHour == 12) {
                                                            if (isAM) 0 else 12
                                                        } else {
                                                            if (isAM) selectedHour else selectedHour + 12
                                                        }
                                                    onTimeSelected(hour24, selectedMinute)
                                                    onDismiss()
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "%02d".format(minute),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = GraphikFontFamily,
                                            color = if (selectedMinute == minute) Color.White else Color.Black,
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = { isSelectingMinute = false },
                                    modifier = Modifier.align(Alignment.Center),
                                ) {
                                    Text(
                                        text = "Back",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                        color = Color(0xFFDD3825),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = GraphikFontFamily,
                            color = Color(0xFFDD3825),
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFFE0DCD1), // Light Beige
                                    Color(0xFFC8C8CA), // Light Gray
                                    Color(0xFF474749), // Dark Gray
                                ),
                        ),
                    ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.offset(x = 24.dp),
                                text = "MeetSpace",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.Black,
                            )
                        }
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    actions = {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(context, MeetingHistoryActivity::class.java).apply {
                                        putExtra("source", "history")
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "History",
                                color = PrimaryRed,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Booking History",
                                tint = PrimaryRed
                            )
                        }
                    }
                )

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentHeight(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Admin Dashboard, Manager Approval, and CEO Approval Buttons
                        if (userRoles.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (userRoles.contains("admin")) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(context, MeetingHistoryActivity::class.java).apply {
                                                putExtra("source", "admin")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFDD3825),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text(
                                            text = "Admin Dashboard",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (userRoles.size > 1) Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (userRoles.contains("linemanager")) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(context, MeetingHistoryActivity::class.java).apply {
                                                putExtra("source", "linemanager")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1FC01F),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text(
                                            text = "Manager Approval",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (userRoles.size > 1 && userRoles.contains("ceo")) Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (userRoles.contains("ceo")) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(context, MeetingHistoryActivity::class.java).apply {
                                                putExtra("source", "ceo")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF14B8D5),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text(
                                            text = "CEO Approval",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(0.dp))

                        // Book Meeting Room Heading
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Book Meeting Room",
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            // Show button only if userRoles has exactly one role
                            if (userRoles.size == 1) {
                                Button(
                                    onClick = {
                                        val source = when {
                                            userRoles.contains("admin") -> "admin"
                                            userRoles.contains("linemanager") -> "linemanager"
                                            userRoles.contains("ceo") -> "ceo"
                                            else -> "unknown"
                                        }
                                        val intent = Intent(context, MeetingHistoryActivity::class.java).apply {
                                            putExtra("source", source)
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            userRoles.contains("admin") -> Color(0xFFDD3825) // Red for Admin
                                            userRoles.contains("linemanager") -> Color(0xFF1FC01F) // Green for Manager
                                            userRoles.contains("ceo") -> Color(0xFF14B8D5) // Cyan for CEO
                                            else -> Color(0xFFF6F4EE) // Fallback, though not expected
                                        },
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text(
                                        text = when {
                                            userRoles.contains("admin") -> "Admin Dashboard"
                                            userRoles.contains("linemanager") -> "Manager Approval"
                                            userRoles.contains("ceo") -> "CEO Approval"
                                            else -> "" // Fallback, though not expected
                                        },
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = showLocationDropdown,
                            onExpandedChange = { showLocationDropdown = !showLocationDropdown },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                    value = if (isLoading) "Loading locations..." else if (selectedLocation.isEmpty()) "Select Location" else selectedLocation,
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.dropdown),
                                            contentDescription = "Dropdown",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = if (selectedLocation.isEmpty()) Color.LightGray else Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    textStyle = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = if (selectedLocation.isEmpty()) Color.LightGray else Color.Black,
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()  // Required for positioning the menu
                                )
                            ExposedDropdownMenu(
                                expanded = showLocationDropdown,
                                onDismissRequest = { showLocationDropdown = false },
                                modifier =
                                    Modifier
                                        .exposedDropdownSize()
                                        .background(Color.White),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                locations.forEachIndexed { index, location ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = location,
                                                color = Color.Black,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        },
                                        onClick = {
                                            selectedLocation = location
                                            showLocationDropdown = false
                                        },
                                    )

                                    if (index < locations.size - 1) {
                                        HorizontalDivider(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp),
                                            thickness = 1.dp,
                                            color = Color.LightGray,
                                        )
                                    }
                                }
                            }
                        }

                        // Meeting Type Dropdown - Using ExposedDropdownMenuBox
                        ExposedDropdownMenuBox(
                            expanded = showMeetingTypeDropdown,
                            onExpandedChange = { showMeetingTypeDropdown = !showMeetingTypeDropdown },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = if (selectedMeetingType.isEmpty()) "Meeting Type" else selectedMeetingType,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.dropdown),
                                        contentDescription = "Dropdown",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(15.dp),
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
                                    fontSize = 18.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = if (selectedMeetingType.isEmpty()) Color.LightGray else Color.Black,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                            )
                            ExposedDropdownMenu(
                                expanded = showMeetingTypeDropdown,
                                onDismissRequest = { showMeetingTypeDropdown = false },
                                modifier =
                                    Modifier
                                        .exposedDropdownSize()
                                        .background(Color.White),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                meetingTypes.forEachIndexed { index, meetingType ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = meetingType,
                                                color = Color.Black,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        },
                                        onClick = {
                                            selectedMeetingType = meetingType
                                            showMeetingTypeDropdown = false
                                        },
                                    )

                                    if (index < meetingTypes.size - 1) {
                                        HorizontalDivider(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp),
                                            thickness = 1.dp,
                                            color = Color.LightGray,
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            // Adjust as needed
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // Meeting Date
                                Text(
                                    text = "Choose Meeting Slot",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                )

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                            .clickable {
                                                Log.d("MeetSpace", "Date field clicked, showDatePicker: $showDatePicker")
                                                showDatePicker = true
                                            }.padding(16.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = dateFormatter.format(selectedDate),
                                            color = if (isDefaultDate) Color.LightGray else Color.Black,
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Calendar",
                                            tint = Color.Gray,
                                        )
                                    }
                                }

                                if (showDatePicker) {
                                    val datePickerState =
                                        rememberDatePickerState(
                                            initialDisplayMode = DisplayMode.Picker,
                                            initialSelectedDateMillis = selectedDate.time,
                                            selectableDates =
                                                object : SelectableDates {
                                                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                                        val today =
                                                            Calendar
                                                                .getInstance()
                                                                .apply {
                                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                                    set(Calendar.MINUTE, 0)
                                                                    set(Calendar.SECOND, 0)
                                                                    set(Calendar.MILLISECOND, 0)
                                                                }.timeInMillis
                                                        return utcTimeMillis >= today
                                                    }
                                                },
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
                                        },
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "From",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        text = "To",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                // Time Fields
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 8.dp),
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                                                        .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp))
                                                        .padding(16.dp)
                                                        .clickable { showFromTimePicker = true },
                                                contentAlignment = Alignment.CenterStart,
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                ) {
                                                    Text(
                                                        text = fromTime, // Replace with your state variable
                                                        textAlign = TextAlign.Center,
                                                        fontSize = 18.sp,
                                                        fontFamily = GraphikFontFamily,
                                                        color = Color.Black,
                                                        modifier = Modifier.fillMaxWidth(),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // To Time
                                    Column(
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 8.dp),
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                                                        .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp))
                                                        .padding(16.dp)
                                                        .clickable { showToTimePicker = true },
                                                contentAlignment = Alignment.CenterStart,
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                ) {
                                                    Text(
                                                        text = toTime, // Replace with your state variable
                                                        textAlign = TextAlign.Center,
                                                        fontSize = 18.sp,
                                                        fontFamily = GraphikFontFamily,
                                                        color = Color.Black,
                                                        modifier = Modifier.fillMaxWidth(),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showFromTimePicker) {
                            val (hour, minute, isAM) = parseTime(fromTime)
                            CustomTimePicker(
                                initialHour = hour,
                                initialMinute = minute,
                                initialIsAM = isAM,
                                onTimeSelected = { hour24, minute ->
                                    val calendar =
                                        Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, hour24)
                                            set(Calendar.MINUTE, minute)
                                        }
                                    fromTime = timeFormatter.format(calendar.time)
                                },
                                onDismiss = { showFromTimePicker = false },
                            )
                        }

                        // Custom To Time Picker Dialog
                        if (showToTimePicker) {
                            val (hour, minute, isAM) = parseTime(toTime)
                            CustomTimePicker(
                                initialHour = hour,
                                initialMinute = minute,
                                initialIsAM = isAM,
                                onTimeSelected = { hour24, minute ->
                                    val calendar =
                                        Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, hour24)
                                            set(Calendar.MINUTE, minute)
                                        }
                                    toTime = timeFormatter.format(calendar.time)
                                },
                                onDismiss = { showToTimePicker = false },
                            )
                        }

                        // Number of Attendees
                        OutlinedTextField(
                            value = numberOfAttendees,
                            onValueChange = { numberOfAttendees = it },
                            placeholder = {
                                Text(
                                    "Number of Attendees",
                                    fontSize = 18.sp,
                                    color = Color.LightGray,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.LightGray,
                                    cursorColor = Color.Gray,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                ),
                            shape = RoundedCornerShape(12.dp),
                        )

                        // Note text
                        if (selectedMeetingType == "Meeting with Guest") {
                            val annotatedString =
                                buildAnnotatedString {
                                    append("* If you have priority meeting, Please reach out to Admin Team ")

                                    pushStringAnnotation(
                                        tag = "EMAIL",
                                        annotation = "mailto:adminemail@arche.global",
                                    )
                                    withStyle(
                                        style =
                                            SpanStyle(
                                                color = Color.Blue,
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                    ) {
                                        append("adminemail@arche.global")
                                    }
                                    pop()
                                }

                            ClickableText(
                                text = annotatedString,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { offset ->
                                    annotatedString
                                        .getStringAnnotations(
                                            tag = "EMAIL",
                                            start = offset,
                                            end = offset,
                                        ).firstOrNull()
                                        ?.let { stringAnnotation ->
                                            uriHandler.openUri(stringAnnotation.item)
                                        }
                                },
                                style =
                                    TextStyle(
                                        fontSize = 13.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Red,
                                        lineHeight = 18.sp,
                                    ),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Book Meeting Room Button
                        Button(
                            onClick = {
                                val missingFields = mutableListOf<String>()
                                if (selectedLocation.isEmpty()) missingFields.add("Location")
                                if (selectedMeetingType.isEmpty()) missingFields.add("Meeting Type")
                                if (isDefaultDate) missingFields.add("Date")
                                if (numberOfAttendees.isEmpty()) {
                                    missingFields.add("Number of Attendees")
                                } else if (numberOfAttendees.toIntOrNull() == null || numberOfAttendees.toInt() <= 0) {
                                    missingFields.add("Valid Number of Attendees")
                                }

                                if (missingFields.isEmpty()) {
                                    val startCal = Calendar.getInstance().apply { time = selectedDate }
                                    val startTimeCal = Calendar.getInstance().apply {
                                        time = timeFormatter.parse(fromTime)!!
                                    }
                                    startCal.set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY))
                                    startCal.set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE))

                                    val endCal = Calendar.getInstance().apply { time = selectedDate }
                                    val endTimeCal = Calendar.getInstance().apply {
                                        time = timeFormatter.parse(toTime)!!
                                    }
                                    endCal.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY))
                                    endCal.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE))

                                    val startDateStr = apiDateTimeFormatter.format(startCal.time)
                                    val endDateStr = apiDateTimeFormatter.format(endCal.time)
                                    val dateStr = dateFormatter.format(selectedDate)

                                    val intent = Intent(context, MeetingRoomListActivity::class.java).apply {
                                        putExtra("location", selectedLocation)
                                        putExtra("date", dateStr)
                                        putExtra("fromTime", fromTime)
                                        putExtra("toTime", toTime)
                                        putExtra("startDate", startDateStr)
                                        putExtra("endDate", endDateStr)
                                        putExtra("noOfAttendees", numberOfAttendees)
                                        putExtra("meetingType", selectedMeetingType)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "Please fill in all required fields",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                    contentColor = Color.White,
                                ),
                            shape = RoundedCornerShape(28.dp),
                        ) {
                            Text(
                                text = "Book Meeting Room",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        // Error Message Below Button
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage ?: "Unknown error",
                                    color = Color(0xFFDD3825),
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
