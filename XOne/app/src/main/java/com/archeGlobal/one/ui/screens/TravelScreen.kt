package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    controller: TravelController
) {
    // State for date picker dialogs
    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showArrivalDatePicker by remember { mutableStateOf(false) }

    // Date format for display - Using IST timezone to match Indian Standard Time
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WelcomeBackgroundTop, // Light Beige/Grey
                            WelcomeBackgroundMiddle, // Light Grey
                            WelcomeBackgroundBottom // Dark Grey
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Add space at the top to push everything down
                Spacer(modifier = Modifier.height(48.dp))

                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.offset(x = 24.dp),
                                text = "Travel",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { controller.onBackPressed() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    actions = {
                        Row(
                            modifier = Modifier
                                .clickable { controller.navigateToTravelHistory() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "History",
                                color = PrimaryRed,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Travel History",
                                tint = PrimaryRed
                            )
                        }
                    }
                )

                // Main content
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 1.dp,
                    backgroundColor = Color(0xFFF6F4EE)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Employee Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GraphikFontFamily
                        )

                        // Load travel approvals when the screen is shown to get the latest count
                        LaunchedEffect(Unit) {
                            controller.loadTravelApprovals()
                        }

                        // Show approval button only if there is approval history
                        if (controller.hasApprovalHistory()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (controller.hasPendingApprovals()) PrimaryRed else Color(0xFF4CAF50),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { controller.navigateToTravelApprovals() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Approval",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GraphikFontFamily
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Pending approvals",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )

                                        val count = controller.pendingApprovalCount
                                        if (count > 0) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = if (count > 99) "99+" else count.toString(),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = GraphikFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Employee Details with proper alignment
                        EmployeeDetailRow(label = "Name:", value = controller.employeeName)
                        EmployeeDetailRow(label = "Employee ID:", value = controller.employeeId)
                        EmployeeDetailRow(label = "Mobile No:", value = controller.mobileNumber)
                        EmployeeDetailRow(
                            label = "Employee Grade:",
                            value = controller.employeeGrade
                        )
                        EmployeeDetailRow(label = "Date of Birth:", value = controller.dateOfBirth)
                        EmployeeDetailRow(label = "Aadhar Number:", value = controller.aadharNumber)

                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            color = Color.LightGray,
                            thickness = 1.dp
                        )

                        // Travel Details Section
                        Text(
                            text = "Travel Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GraphikFontFamily,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Travel Destination
                        OutlinedTextField(
                            value = controller.destination,
                            onValueChange = { controller.updateDestination(it) },
                            placeholder = {
                                Text(
                                    "Travel Destination",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.Gray,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                focusedPlaceholderColor = Color(0xFFF6F4EE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Project Name
                        OutlinedTextField(
                            value = controller.projectName,
                            onValueChange = { controller.updateProjectName(it) },
                            placeholder = {
                                Text(
                                    "Project Name",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.Gray,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                focusedPlaceholderColor = Color(0xFFF6F4EE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Business Justification
                        OutlinedTextField(
                            value = controller.businessJustification,
                            onValueChange = { controller.updateBusinessJustification(it) },
                            placeholder = {
                                Text(
                                    "Business Justification",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.Gray,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                focusedPlaceholderColor = Color(0xFFF6F4EE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Mode of Transport
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = controller.modeOfTransport,
                                onValueChange = { },
                                placeholder = {
                                    Text(
                                        "Mode of Transport",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .clickable(onClick = { controller.toggleTransportDropdown() }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Gray,
                                    cursorColor = Color.Black,
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    focusedContainerColor = Color.White,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.Gray,
                                        modifier = Modifier.clickable { controller.toggleTransportDropdown() }
                                    )
                                },
                                readOnly = true
                            )

                            DropdownMenu(
                                expanded = controller.isTransportDropdownExpanded,
                                onDismissRequest = { controller.dismissTransportDropdown() },
                                modifier = Modifier
                                    .width(with(LocalDensity.current) { 300.dp })
                                    .background(Color.White)
                            ) {
                                controller.transportOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option) },
                                        onClick = { controller.updateModeOfTransport(option) }
                                    )
                                }
                            }
                        }

                        // Date Selection Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Departure Date
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Departure Date",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = controller.departureDate,
                                        onValueChange = { showDepartureDatePicker = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color.Gray,
                                            cursorColor = Color.Black,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White,
                                            unfocusedTextColor = Color.Black,
                                            focusedTextColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        readOnly = true,
                                        enabled = true,
                                        textStyle = TextStyle(
                                            background = Color(0xFFEEEEEE),
                                            color = Color.Black,
                                            fontSize = 15.sp
                                        )
                                    )

                                    // Add invisible clickable overlay
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showDepartureDatePicker = true }
                                            .background(Color.Transparent)
                                    )
                                }

                                // Departure Date Picker Dialog
                                if (showDepartureDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialDisplayMode = DisplayMode.Picker,
                                        initialSelectedDateMillis = System.currentTimeMillis()
                                    )
                                    DatePickerDialog(
                                        onDismissRequest = { showDepartureDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    controller.updateDepartureDateFromMillis(millis)
                                                }
                                                showDepartureDatePicker = false
                                            }) {
                                                Text("OK")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                showDepartureDatePicker = false
                                            }) {
                                                Text("Cancel")
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }

                            // Arrival Date
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Return Date",
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = controller.arrivalDate,
                                        onValueChange = { showArrivalDatePicker = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color.Gray,
                                            cursorColor = Color.Black,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White,
                                            unfocusedTextColor = Color.Black,
                                            focusedTextColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        readOnly = true,
                                        enabled = true,
                                        textStyle = TextStyle(
                                            background = Color(0xFFEEEEEE),
                                            color = Color.Black,
                                            fontSize = 15.sp
                                        )
                                    )

                                    // Add invisible clickable overlay
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showArrivalDatePicker = true }
                                            .background(Color.Transparent)
                                    )
                                }

                                // Arrival Date Picker Dialog
                                if (showArrivalDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialDisplayMode = DisplayMode.Picker,
                                        initialSelectedDateMillis = System.currentTimeMillis()
                                    )
                                    DatePickerDialog(
                                        onDismissRequest = { showArrivalDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    controller.updateArrivalDateFromMillis(millis)
                                                }
                                                showArrivalDatePicker = false
                                            }) {
                                                Text("OK")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                showArrivalDatePicker = false
                                            }) {
                                                Text("Cancel")
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }
                        }

                        // Flight Time Preference - only show if mode of transport is Flight
                        if (controller.modeOfTransport == "Flight") {
                            // Note about flight booking
                            Text(
                                text = "Note: Flight bookings must be made 1 week prior to departure.",
                                fontSize = 12.sp,
                                fontFamily = GraphikFontFamily,
                                color = PrimaryRed,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Flight Time Preference Dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = controller.flightTimePreference,
                                    onValueChange = { },
                                    label = { Text("Flight Time Preference") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .clickable(onClick = { controller.toggleFlightTimeDropdown() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.Gray,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color.White,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedLabelColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.Gray,
                                            modifier = Modifier.clickable { controller.toggleFlightTimeDropdown() }
                                        )
                                    },
                                    readOnly = true
                                )

                                DropdownMenu(
                                    expanded = controller.isFlightTimeDropdownExpanded,
                                    onDismissRequest = { controller.dismissFlightTimeDropdown() },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { 300.dp })
                                        .background(Color(0xCC000000)) // 80% opacity black background
                                ) {
                                    controller.flightTimeOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option, color = Color.White) },
                                            onClick = { controller.updateFlightTimePreference(option) }
                                        )
                                    }
                                }
                            }

                            // Seat Preference Dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = controller.seatPreference,
                                    onValueChange = { },
                                    label = { Text("Seat Preference") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .clickable(onClick = { controller.toggleSeatPrefDropdown() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.Gray,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color.White,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedLabelColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.Gray,
                                            modifier = Modifier.clickable { controller.toggleSeatPrefDropdown() }
                                        )
                                    },
                                    readOnly = true
                                )

                                DropdownMenu(
                                    expanded = controller.isSeatPrefDropdownExpanded,
                                    onDismissRequest = { controller.dismissSeatPrefDropdown() },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { 300.dp })
                                        .background(Color(0xCC000000)) // 80% opacity black background
                                ) {
                                    controller.seatPreferenceOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option, color = Color.White) },
                                            onClick = { controller.updateSeatPreference(option) }
                                        )
                                    }
                                }
                            }

                            // Frequent Flyer Number Button
                            Button(
                                onClick = { controller.showFrequentFlyerNumberDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                border = BorderStroke(1.dp, Color.LightGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Enter Frequent Flyer Number",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            // Frequent Flyer Number Dialog
                            if (controller.showFrequentFlyerDialog) {
                                androidx.compose.material.AlertDialog(
                                    onDismissRequest = { controller.dismissFrequentFlyerNumberDialog() },
                                    title = { Text(text = "Enter Frequent Flyer Number") },
                                    text = {
                                        OutlinedTextField(
                                            value = controller.frequentFlyerNumber,
                                            onValueChange = {
                                                controller.updateFrequentFlyerNumber(
                                                    it
                                                )
                                            },
                                            label = { Text("Frequent Flyer Number") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.Gray,
                                                cursorColor = Color.Black,
                                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                                focusedContainerColor = Color.White,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedLabelColor = Color.Gray,
                                                focusedLabelColor = Color.Gray
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = { controller.dismissFrequentFlyerNumberDialog() },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                                        ) {
                                            Text("Submit")
                                        }
                                    },
                                    backgroundColor = Color.White,
                                    contentColor = Color.Black
                                )
                            }
                        }

                        // Meal Preference Toggle and Dropdown
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Select Meal Preference",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black
                            )

                            // Toggle switch
                            androidx.compose.material.Switch(
                                checked = controller.mealPreferenceEnabled,
                                onCheckedChange = { controller.toggleMealPreference(it) },
                                colors = androidx.compose.material.SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFFADE1B6),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        // Show meal preference dropdown if enabled
                        if (controller.mealPreferenceEnabled) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = controller.mealPreference,
                                    onValueChange = { },
                                    label = { Text("Meal Preference") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .clickable(onClick = { controller.toggleMealPrefDropdown() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.Gray,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color.White,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedLabelColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.Gray,
                                            modifier = Modifier.clickable { controller.toggleMealPrefDropdown() }
                                        )
                                    },
                                    readOnly = true
                                )

                                DropdownMenu(
                                    expanded = controller.isMealPrefDropdownExpanded,
                                    onDismissRequest = { controller.dismissMealPrefDropdown() },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { 300.dp })
                                        .background(Color.White)
                                ) {
                                    controller.mealPreferenceOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option) },
                                            onClick = { controller.updateMealPreference(option) }
                                        )
                                    }
                                }
                            }
                        }

                        // Stay Required Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stay Required",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black
                            )

                            // Toggle switch
                            androidx.compose.material.Switch(
                                checked = controller.stayRequired,
                                onCheckedChange = { controller.toggleStayRequired(it) },
                                colors = androidx.compose.material.SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFFADE1B6),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        // Approval Chain
                        Text(
                            text = "Approval Chain",
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp),
                            fontWeight = FontWeight.Bold
                        )

                        // Reporting Manager - using the same EmployeeDetailRow component for consistent alignment
                        EmployeeDetailRow(
                            label = "Reporting Manager:",
                            value = controller.reportingManagerName
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Error message if submission failed
                        controller.submissionError?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = { controller.submitTravelRequest() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRed
                            ),
                            shape = RoundedCornerShape(30.dp),
                            enabled = !controller.isSubmitting
                        ) {
                            if (controller.isSubmitting) {
                                androidx.compose.material.CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Submit Travel Request",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }

                        // Spacer at the bottom for better padding
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Composable for displaying employee detail rows with proper alignment
 */
@Composable
fun EmployeeDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            modifier = Modifier.width(140.dp) // Increased fixed width for alignment
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
    }
}
