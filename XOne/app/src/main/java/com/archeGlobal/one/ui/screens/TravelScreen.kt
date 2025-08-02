package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    controller: TravelController
) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    // State for date picker dialogs
    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showArrivalDatePicker by remember { mutableStateOf(false) }

    // Date format for display - Using IST timezone to match Indian Standard Time
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    // Wrap entire content with font scale adjustment
    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
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
                                    text = "TravelDesk",
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
                            // Load travel approvals when the screen is shown to get the latest count
                            LaunchedEffect(Unit) {
                                controller.loadTravelApprovals()
                            }

                            // Add space above Employee Details section
                            Spacer(modifier = Modifier.height(24.dp))

                            // Header row with Employee Details and Approval button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Employee Details",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily
                                )

                                // Show approval button only if there is approval history
                                if (controller.hasApprovalHistory()) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0xFF4CAF50),
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
                            }

                            // Add space between Employee Details header and employee rows
                            Spacer(modifier = Modifier.height(16.dp))

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

                            // Add space below Employee Details section
                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                color = Color.LightGray,
                                thickness = 1.dp
                            )

                            // Add space between divider and Travel Details
                            Spacer(modifier = Modifier.height(16.dp))

                            // Travel Details Section
                            Text(
                                text = "Travel Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GraphikFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Project Name
                            OutlinedTextField(
                                value = controller.projectName,
                                onValueChange = { controller.updateProjectName(it) },
                                placeholder = {
                                    Text(
                                        "Project Name",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily
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
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily
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
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = GraphikFontFamily
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
                                            text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                            onClick = { controller.updateModeOfTransport(option) }
                                        )
                                    }
                                }
                            }

                            // Dynamic booking notice and flight options - only show if mode of transport is selected
                            if (controller.modeOfTransport == "Flight") {
                                // Booking notice appears immediately after Flight selection
                                val bookingNotice = when {
                                    controller.flightType == "International" -> "* Bookings must be made 2 weeks prior to departure."
                                    else -> "* Bookings must be made 1 week prior to departure."
                                }

                                Text(
                                    text = bookingNotice,
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    color = PrimaryRed,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // International/Domestic Dropdown
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = controller.flightType,
                                        onValueChange = { },
                                        placeholder = {
                                            Text(
                                                "International",
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Normal,
                                                fontFamily = GraphikFontFamily
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .clickable(onClick = { controller.toggleFlightTypeDropdown() }),
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
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray,
                                                modifier = Modifier.clickable { controller.toggleFlightTypeDropdown() }
                                            )
                                        },
                                        readOnly = true
                                    )

                                    DropdownMenu(
                                        expanded = controller.isFlightTypeDropdownExpanded,
                                        onDismissRequest = { controller.dismissFlightTypeDropdown() },
                                        modifier = Modifier
                                            .width(with(LocalDensity.current) { 300.dp })
                                            .background(Color.White)
                                    ) {
                                        controller.flightTypeOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                                onClick = { controller.updateFlightType(option) }
                                            )
                                        }
                                    }
                                }

                                // Frequent Flyer Number Input
                                OutlinedTextField(
                                    value = controller.frequentFlyerNumber.let { if (it == "0") "" else it },
                                    onValueChange = { controller.updateFrequentFlyerNumber(it) },
                                    placeholder = {
                                        Text(
                                            "Frequent Flyer Number",
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = GraphikFontFamily
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

                                // Seat Preference Dropdown - only for multi-destination mode (below frequent flyer number and above single/multi destination buttons)
                                if (controller.isMultiDestination) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = controller.seatPreference,
                                            onValueChange = { },
                                            placeholder = {
                                                Text(
                                                    "Seat Preference",
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = GraphikFontFamily
                                                )
                                            },
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
                                                unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                                focusedPlaceholderColor = Color(0xFFF6F4EE)
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
                                                .background(Color.White)
                                        ) {
                                            controller.seatPreferenceOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                                    onClick = { controller.updateSeatPreference(option) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Tab buttons for Single/Multiple destinations
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Single Destination Tab
                                Button(
                                    onClick = { controller.toggleDestinationMode(false) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!controller.isMultiDestination) PrimaryRed else Color.White,
                                        contentColor = if (!controller.isMultiDestination) Color.White else Color.Black
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (!controller.isMultiDestination) PrimaryRed else Color.LightGray
                                    )
                                ) {
                                    Text(
                                        text = "Single Destination",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (!controller.isMultiDestination) Color.White else Color.Black
                                    )
                                }

                                // Multiple Destinations Tab
                                Button(
                                    onClick = { controller.toggleDestinationMode(true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (controller.isMultiDestination) PrimaryRed else Color.White,
                                        contentColor = if (controller.isMultiDestination) Color.White else Color.Black
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (controller.isMultiDestination) PrimaryRed else Color.LightGray
                                    )
                                ) {
                                    Text(
                                        text = "Multiple Destinations",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (controller.isMultiDestination) Color.White else Color.Black
                                    )
                                }
                            }

                            // Show destination fields based on mode
                            if (!controller.isMultiDestination) {
                                // Single Destination Section
                                // Destination header
                                Text(
                                    text = "Destination",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // White card containing destination fields
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = 2.dp,
                                    backgroundColor = Color.White
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // Origin City field
                                        androidx.compose.material3.OutlinedTextField(
                                            value = controller.originCity,
                                            onValueChange = { controller.updateOriginCity(it) },
                                            placeholder = {
                                                Text(
                                                    "Origin City",
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = GraphikFontFamily
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.Gray,
                                                cursorColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedPlaceholderColor = Color.Gray,
                                                focusedPlaceholderColor = Color.Gray
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        // Destination City field
                                        androidx.compose.material3.OutlinedTextField(
                                            value = controller.destination,
                                            onValueChange = { controller.updateDestination(it) },
                                            placeholder = {
                                                Text(
                                                    "Destination City",
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = GraphikFontFamily
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.Gray,
                                                cursorColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedPlaceholderColor = Color.Gray,
                                                focusedPlaceholderColor = Color.Gray
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        // Date Selection Row inside the card
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White, RoundedCornerShape(8.dp))
                                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                            .padding(16.dp)
                                                            .clickable { showDepartureDatePicker = true },
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    Color(0xFFEEEEEE),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = controller.departureDate,
                                                                color = Color.Black,
                                                                fontSize = 15.sp,
                                                                fontFamily = GraphikFontFamily
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Return Date
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
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White, RoundedCornerShape(8.dp))
                                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                            .padding(16.dp)
                                                            .clickable { showArrivalDatePicker = true },
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    Color(0xFFEEEEEE),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = controller.arrivalDate,
                                                                color = Color.Black,
                                                                fontSize = 15.sp,
                                                                fontFamily = GraphikFontFamily
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Flight additional options - show flight time and seat preference inside the card for flights
                                        if (controller.modeOfTransport == "Flight") {
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Flight Time Preference Dropdown
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                androidx.compose.material3.OutlinedTextField(
                                                    value = controller.flightTimePreference,
                                                    onValueChange = { },
                                                    placeholder = {
                                                        Text(
                                                            "Flight Time Preference",
                                                            color = Color.Gray,
                                                            fontWeight = FontWeight.Normal,
                                                            fontFamily = GraphikFontFamily
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 16.dp)
                                                        .clickable(onClick = { controller.toggleFlightTimeDropdown() }),
                                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                        unfocusedBorderColor = Color.LightGray,
                                                        focusedBorderColor = Color.Gray,
                                                        cursorColor = Color.Black,
                                                        unfocusedContainerColor = Color.White,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedTextColor = Color.Black,
                                                        focusedTextColor = Color.Black,
                                                        unfocusedPlaceholderColor = Color.Gray,
                                                        focusedPlaceholderColor = Color.Gray
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
                                                            text = { Text(text = option, color = Color.White, fontFamily = GraphikFontFamily) },
                                                            onClick = { controller.updateFlightTimePreference(option) }
                                                        )
                                                    }
                                                }
                                            }

                                            // Seat Preference Dropdown
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                androidx.compose.material3.OutlinedTextField(
                                                    value = controller.seatPreference,
                                                    onValueChange = { },
                                                    placeholder = {
                                                        Text(
                                                            "Seat Preference",
                                                            color = Color.Gray,
                                                            fontWeight = FontWeight.Normal,
                                                            fontFamily = GraphikFontFamily
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable(onClick = { controller.toggleSeatPrefDropdown() }),
                                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                        unfocusedBorderColor = Color.LightGray,
                                                        focusedBorderColor = Color.Gray,
                                                        cursorColor = Color.Black,
                                                        unfocusedContainerColor = Color.White,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedTextColor = Color.Black,
                                                        focusedTextColor = Color.Black,
                                                        unfocusedPlaceholderColor = Color.Gray,
                                                        focusedPlaceholderColor = Color.Gray
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
                                                            text = { Text(text = option, color = Color.White, fontFamily = GraphikFontFamily) },
                                                            onClick = { controller.updateSeatPreference(option) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Multiple Destinations
                                MultiDestinationSection(controller)
                            }

                            // Stay Required Toggle (outside destination cards)
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
                                    modifier = Modifier.scale(1.2f),
                                    colors = androidx.compose.material.SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFF6F4EE),
                                        checkedTrackColor = Color(0xFFADE1B6),
                                        uncheckedThumbColor =  Color(0xFFF6F4EE),
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )
                            }

                            // Date Picker Dialogs for single destination (outside the card)
                            if (!controller.isMultiDestination) {
                                // Departure Date Picker Dialog
                                if (showDepartureDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialDisplayMode = DisplayMode.Picker,
                                        initialSelectedDateMillis = System.currentTimeMillis(),
                                        selectableDates = object : androidx.compose.material3.SelectableDates {
                                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                                // Only allow dates from today onwards
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
                                        onDismissRequest = { showDepartureDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    controller.updateDepartureDateFromMillis(millis)
                                                }
                                                showDepartureDatePicker = false
                                            }) {
                                                Text("OK", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                showDepartureDatePicker = false
                                            }) {
                                                Text("Cancel", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }

                                // Arrival Date Picker Dialog
                                if (showArrivalDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialDisplayMode = DisplayMode.Picker,
                                        initialSelectedDateMillis = System.currentTimeMillis(),
                                        selectableDates = object : androidx.compose.material3.SelectableDates {
                                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                                // Only allow dates from today onwards
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
                                        onDismissRequest = { showArrivalDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    controller.updateArrivalDateFromMillis(millis)
                                                }
                                                showArrivalDatePicker = false
                                            }) {
                                                Text("OK", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                showArrivalDatePicker = false
                                            }) {
                                                Text("Cancel", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }

                            // Meal Preference Toggle and Dropdown (only for Flight and Train)
                            if (controller.modeOfTransport == "Flight" || controller.modeOfTransport == "Train") {
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
                                        modifier = Modifier.scale(1.2f),
                                        colors = androidx.compose.material.SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFF6F4EE),
                                            checkedTrackColor = Color(0xFFADE1B6),
                                            uncheckedThumbColor = Color(0xFFF6F4EE),
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
                                            label = { Text("Meal Preference", fontFamily = GraphikFontFamily) },
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
                                                    text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                                    onClick = { controller.updateMealPreference(option) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Approval Chain
                            Text(
                                text = "Approval Chain",
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp),
                                fontWeight = FontWeight.SemiBold
                            )

                            // Reporting Manager - custom layout with two-line label
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.width(140.dp)
                                ) {
                                    Text(
                                        text = "Reporting",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Manager:",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = controller.reportingManagerName,
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black
                                )
                            }

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
    } // Close FontScaleAdjusted block
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
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(140.dp) // Increased fixed width for alignment
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDestinationSection(controller: TravelController) {
    // State for date picker dialogs
    val datePickerStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Destinations header with Add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Destinations",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GraphikFontFamily,
                color = Color.Black
            )

            Button(
                onClick = { controller.addDestination() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "Add Destination",
                    fontSize = 12.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.White

                )
            }
        }

        // List of destinations
        controller.destinations.forEachIndexed { index, destination ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = 2.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Destination header with delete button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Destination ${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black
                        )

                        // Delete button (only show if there's more than one destination)
                        if (controller.destinations.size > 1) {
                            IconButton(
                                onClick = { controller.removeDestination(destination.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Destination",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Origin City field
                    androidx.compose.material3.OutlinedTextField(
                        value = controller.originCity,
                        onValueChange = { controller.updateOriginCity(it) },
                        placeholder = {
                            Text(
                                "Origin City",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Gray,
                            cursorColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Destination City field
                    androidx.compose.material3.OutlinedTextField(
                        value = destination.destination,
                        onValueChange = { controller.updateDestinationField(destination.id, it) },
                        placeholder = {
                            Text(
                                "Destination City",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Gray,
                            cursorColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Date Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                val showDepartureDatePicker = datePickerStates["departure_${destination.id}"] ?: false

                                OutlinedTextField(
                                    value = destination.departureDate,
                                    onValueChange = { },
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
                                        fontSize = 15.sp,
                                        fontFamily = GraphikFontFamily
                                    )
                                )

                                // Add invisible clickable overlay
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            datePickerStates["departure_${destination.id}"] = true
                                        }
                                        .background(Color.Transparent)
                                )

                                // Departure Date Picker Dialog
                                if (showDepartureDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialDisplayMode = DisplayMode.Picker,
                                        initialSelectedDateMillis = System.currentTimeMillis(),
                                        selectableDates = object : androidx.compose.material3.SelectableDates {
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
                                        onDismissRequest = { datePickerStates["departure_${destination.id}"] = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    controller.updateDestinationDepartureDateFromMillis(destination.id, millis)
                                                }
                                                datePickerStates["departure_${destination.id}"] = false
                                            }) {
                                                Text("OK", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                datePickerStates["departure_${destination.id}"] = false
                                            }) {
                                                Text("Cancel", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }
                        }

                        // Return Date
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
                                val showReturnDatePicker = datePickerStates["return_${destination.id}"] ?: false

                                OutlinedTextField(
                                    value = destination.returnDate,
                                    onValueChange = { },
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
                                        fontSize = 15.sp,
                                        fontFamily = GraphikFontFamily
                                    )
                                )

                                // Add invisible clickable overlay
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            datePickerStates["return_${destination.id}"] = true
                                        }
                                        .background(Color.Transparent)
                                )

                                // Return Date Picker Dialog
                                if (showReturnDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialDisplayMode = DisplayMode.Picker,
                                        initialSelectedDateMillis = System.currentTimeMillis(),
                                        selectableDates = object : androidx.compose.material3.SelectableDates {
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
                                        onDismissRequest = { datePickerStates["return_${destination.id}"] = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    controller.updateDestinationReturnDateFromMillis(destination.id, millis)
                                                }
                                                datePickerStates["return_${destination.id}"] = false
                                            }) {
                                                Text("OK", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                datePickerStates["return_${destination.id}"] = false
                                            }) {
                                                Text("Cancel", color = Color.White, fontFamily = GraphikFontFamily)
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }
                        }
                    }

                    // Flight additional options - show flight time and seat preference inside the card for flights
                    if (controller.modeOfTransport == "Flight") {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Flight Time Preference Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.material3.OutlinedTextField(
                                value = controller.flightTimePreference,
                                onValueChange = { },
                                placeholder = {
                                    Text(
                                        "Flight Time Preference",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .clickable(onClick = { controller.toggleFlightTimeDropdown() }),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Gray,
                                    cursorColor = Color.Black,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedPlaceholderColor = Color.Gray,
                                    focusedPlaceholderColor = Color.Gray
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
                                        text = { Text(text = option, color = Color.White, fontFamily = GraphikFontFamily) },
                                        onClick = { controller.updateFlightTimePreference(option) }
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
