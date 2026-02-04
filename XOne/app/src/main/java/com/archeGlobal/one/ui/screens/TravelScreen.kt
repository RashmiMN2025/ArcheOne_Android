package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.archeGlobal.one.R
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
fun TravelScreen(controller: TravelController) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    // Handle system back gesture (swipe back)
    BackHandler {
        controller.onBackPressed()
    }

    // State for date picker dialogs
    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showArrivalDatePicker by remember { mutableStateOf(false) }

    // Date format for display - Using IST timezone to match Indian Standard Time
    val dateFormatter =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }

    // Wrap entire content with font scale adjustment
    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            WelcomeBackgroundTop, // Light Beige/Grey
                                            WelcomeBackgroundMiddle, // Light Grey
                                            WelcomeBackgroundBottom, // Dark Grey
                                        ),
                                ),
                        ),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    modifier = Modifier.offset(x = 24.dp),
                                    text = "TravelDesk",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { controller.onBackPressed() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black,
                                )
                            }
                        },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        actions = {
                            Row(
                                modifier =
                                    Modifier
                                        .clickable { controller.navigateToTravelHistory() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "History",
                                    color = PrimaryRed,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = "Travel History",
                                    tint = PrimaryRed,
                                )
                            }
                        },
                    )

                    // Main content
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 1.dp,
                        backgroundColor = Color(0xFFF6F4EE),
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(16.dp),
                        ) {
                            // Reload isAdmin status and approval count when screen resumes (initial load happens in HomeController before navigation)
                            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                                val observer =
                                    androidx.lifecycle.LifecycleEventObserver { _, event ->
                                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                            // Reload admin status and approval count when screen resumes
                                            controller.reloadApprovalHistoryCount()
                                        }
                                    }
                                lifecycleOwner.lifecycle.addObserver(observer)

                                onDispose {
                                    lifecycleOwner.lifecycle.removeObserver(observer)
                                }
                            }

                            // Add space above buttons
                            Spacer(modifier = Modifier.height(16.dp))

                            // Manager Approval and Admin Dashboard buttons with loading state
                            if (controller.isLoadingApprovalCount) {
                                // Show loader while fetching approval count
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            } else if (controller.totalApprovalCount > 0 || controller.isAdmin) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Manager Approval button - only show if user has approval history
                                    if (controller.totalApprovalCount > 0) {
                                        Box(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Button(
                                                onClick = { controller.navigateToTravelApprovals() },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PrimaryRed,
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Text(
                                                    text = "Manager Approval",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White,
                                                    fontFamily = GraphikFontFamily
                                                )
                                            }

                                            // Notification badge - shows pending count (positioned at top right corner)
                                            if (controller.pendingApprovalCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .offset(x = 8.dp, y = (-8).dp)
                                                        .size(24.dp)
                                                        .background(
                                                            color = Color.White,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = PrimaryRed,
                                                            shape = RoundedCornerShape(12.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = controller.pendingApprovalCount.toString(),
                                                        color = PrimaryRed,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = GraphikFontFamily
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Admin Dashboard button - only show if isAdmin is true
                                    if (controller.isAdmin) {
                                        Button(
                                            onClick = { controller.navigateToTravelAdminDashboard() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PrimaryRed,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                text = "Admin Dashboard",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White,
                                                fontFamily = GraphikFontFamily
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            // Booking Mode Selection (Self / On Behalf)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { controller.updateBookingMode(true) }
                                ) {
                                    androidx.compose.material.RadioButton(
                                        selected = controller.isBookingForSelf,
                                        onClick = { controller.updateBookingMode(true) },
                                        colors = androidx.compose.material.RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                                    )
                                    Text(
                                        text = "Self",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { controller.updateBookingMode(false) }
                                ) {
                                    androidx.compose.material.RadioButton(
                                        selected = !controller.isBookingForSelf,
                                        onClick = { controller.updateBookingMode(false) },
                                        colors = androidx.compose.material.RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                                    )
                                    Text(
                                        text = "On Behalf of Employee",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            // Search Bar for On Behalf
                            if (!controller.isBookingForSelf) {
                                var expanded by remember { mutableStateOf(false) }
                                val focusRequester = remember { FocusRequester() }
                                var isFocused by remember { mutableStateOf(false) }

                                // Update expanded state based on results
                                LaunchedEffect(controller.employeeSearchResults) {
                                    if (controller.employeeSearchResults.isNotEmpty()) {
                                        expanded = true
                                    }
                                }

                                androidx.compose.material3.ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { 
                                        if (isFocused) {
                                            expanded = true
                                        } else {
                                            expanded = it 
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = controller.attendeeSearchQuery,
                                        onValueChange = {
                                            controller.updateAttendeeSearchQuery(it)
                                            controller.searchEmployees(it)
                                            expanded = true
                                        },
                                        placeholder = { Text("Search Employee by Name", fontFamily = GraphikFontFamily) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .focusRequester(focusRequester)
                                            .onFocusChanged { focusState ->
                                                isFocused = focusState.isFocused
                                                if (focusState.isFocused && controller.employeeSearchResults.isNotEmpty()) {
                                                    expanded = true
                                                }
                                            }
                                            .onKeyEvent { keyEvent ->
                                                if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp) {
                                                    true
                                                } else {
                                                    false
                                                }
                                            },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = PrimaryRed,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                             androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        }
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded && controller.employeeSearchResults.isNotEmpty(),
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        controller.employeeSearchResults.forEach { employee ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(employee.name, fontFamily = GraphikFontFamily, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            text = if (employee.employeeId.isNotEmpty()) "${employee.employeeId} - ${employee.email}" else employee.email,
                                                            fontFamily = GraphikFontFamily,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    controller.selectOnBehalfEmployee(employee)
                                                    expanded = false
                                                    isFocused = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (controller.isBookingForSelf || controller.selectedOnBehalfEmployee != null) {
                            // Employee Details header
                            Text(
                                text = "Employee Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GraphikFontFamily,
                            )

                            // Add space between Employee Details header and employee rows
                            Spacer(modifier = Modifier.height(16.dp))

                            // Employee Details with proper alignment
                            EmployeeDetailRow(label = "Name:", value = controller.employeeName)
                            EmployeeDetailRow(label = "Employee ID:", value = controller.employeeId)
                            EmployeeDetailRow(label = "Mobile No:", value = controller.mobileNumber)
                            EmployeeDetailRow(
                                label = "Employee Grade:",
                                value = controller.employeeGrade,
                            )
                            EmployeeDetailRow(label = "Date of Birth:", value = controller.dateOfBirth)
                            EmployeeDetailRow(label = "Aadhar Number:", value = controller.aadharNumber)

                            // Add space below Employee Details section
                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                color = Color.LightGray,
                                thickness = 1.dp,
                            )

                            // Add space between divider and Travel Details
                            Spacer(modifier = Modifier.height(16.dp))

                            // Travel Details Section
                            Text(
                                text = "Travel Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GraphikFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )

                            // Project Name
                            OutlinedTextField(
                                value = controller.projectName,
                                onValueChange = { controller.updateProjectName(it) },
                                placeholder = {
                                    Text(
                                        "Project Name *",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                        focusedPlaceholderColor = Color(0xFFF6F4EE),
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )


                            // Project ID
                            OutlinedTextField(
                                value = controller.projectId,
                                onValueChange = { controller.updateProjectId(it) },
                                placeholder = {
                                    Text(
                                        "Project ID",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                        focusedPlaceholderColor = Color(0xFFF6F4EE),
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )

                            // Opportunity ID
                            OutlinedTextField(
                                value = controller.opportunityId,
                                onValueChange = { controller.updateOpportunityId(it) },
                                placeholder = {
                                    Text(
                                        "Opportunity ID",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                        focusedPlaceholderColor = Color(0xFFF6F4EE),
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )

                            // CRM ID
                            OutlinedTextField(
                                value = controller.crmId,
                                onValueChange = { controller.updateCrmId(it) },
                                placeholder = {
                                    Text(
                                        "CRM ID",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                        focusedPlaceholderColor = Color(0xFFF6F4EE),
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )

                            // Business Justification
                            OutlinedTextField(
                                value = controller.businessJustification,
                                onValueChange = { controller.updateBusinessJustification(it) },
                                placeholder = {
                                    Text(
                                        "Business Justification *",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                        focusedPlaceholderColor = Color(0xFFF6F4EE),
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )

                            // Mode of Transport
                            Box(modifier = Modifier.fillMaxWidth()) {
                                val interactionSource = remember { MutableInteractionSource() }

                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect { interaction ->
                                        if (interaction is PressInteraction.Release) {
                                            controller.toggleTransportDropdown()
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = controller.modeOfTransport,
                                    onValueChange = { },
                                    placeholder = {
                                        Text(
                                            "Mode of Transport *",
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = GraphikFontFamily,
                                        )
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                    colors =
                                        OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            cursorColor = Color.Black,
                                            unfocusedContainerColor = Color(0xFFF5F5F5),
                                            focusedContainerColor = Color(0xFFF5F5F5),
                                            unfocusedTextColor = Color.Black,
                                            focusedTextColor = Color.Black,
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.Gray,
                                        )
                                    },
                                    readOnly = true,
                                    interactionSource = interactionSource,
                                )

                                DropdownMenu(
                                    expanded = controller.isTransportDropdownExpanded,
                                    onDismissRequest = { controller.dismissTransportDropdown() },
                                    modifier =
                                        Modifier
                                            .width(with(LocalDensity.current) { 300.dp })
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp)),
                                ) {
                                    controller.transportOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option, fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium) },
                                            onClick = { controller.updateModeOfTransport(option) },
                                        )
                                    }
                                }
                            }

                            // Only show the rest of the form after Mode of Transport is selected
                            if (controller.modeOfTransport.isNotEmpty()) {
                                // Dynamic booking notice and flight options - only show if mode of transport is selected
                                if (controller.modeOfTransport == "Flight") {
                                // Booking notice appears immediately after Flight selection
                                val bookingNotice =
                                    when {
                                        controller.flightType == "International" -> "* Bookings must be made 2 weeks prior to departure."
                                        else -> "* Bookings must be made 1 week prior to departure."
                                    }

                                Text(
                                    text = bookingNotice,
                                    fontSize = 12.sp,
                                    fontFamily = GraphikFontFamily,
                                    color = PrimaryRed,
                                    modifier = Modifier.padding(bottom = 16.dp),
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
                                                fontFamily = GraphikFontFamily,
                                            )
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp)
                                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                .clickable(onClick = { controller.toggleFlightTypeDropdown() }),
                                        colors =
                                            OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedBorderColor = Color.Transparent,
                                                cursorColor = Color.Black,
                                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                                focusedContainerColor = Color(0xFFF5F5F5),
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                                focusedPlaceholderColor = Color(0xFFF6F4EE),
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray,
                                                modifier = Modifier.clickable { controller.toggleFlightTypeDropdown() },
                                            )
                                        },
                                        readOnly = true,
                                    )

                                    DropdownMenu(
                                        expanded = controller.isFlightTypeDropdownExpanded,
                                        onDismissRequest = { controller.dismissFlightTypeDropdown() },
                                        modifier =
                                            Modifier
                                                .width(with(LocalDensity.current) { 300.dp })
                                                .background(Color.White),
                                    ) {
                                        controller.flightTypeOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                                onClick = { controller.updateFlightType(option) },
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
                                            fontFamily = GraphikFontFamily,
                                        )
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                    colors =
                                        OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            cursorColor = Color.Black,
                                            unfocusedContainerColor = Color(0xFFF5F5F5),
                                            focusedContainerColor = Color(0xFFF5F5F5),
                                            unfocusedTextColor = Color.Black,
                                            focusedTextColor = Color.Black,
                                            unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                            focusedPlaceholderColor = Color(0xFFF6F4EE),
                                        ),
                                    shape = RoundedCornerShape(8.dp),
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
                                                    fontFamily = GraphikFontFamily,
                                                )
                                            },
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 16.dp)
                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                    .clickable(onClick = { controller.toggleSeatPrefDropdown() }),
                                            colors =
                                                OutlinedTextFieldDefaults.colors(
                                                    unfocusedBorderColor = Color.Transparent,
                                                    focusedBorderColor = Color.Transparent,
                                                    cursorColor = Color.Black,
                                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                                    focusedContainerColor = Color(0xFFF5F5F5),
                                                    unfocusedTextColor = Color.Black,
                                                    focusedTextColor = Color.Black,
                                                    unfocusedPlaceholderColor = Color(0xFFF6F4EE),
                                                    focusedPlaceholderColor = Color(0xFFF6F4EE),
                                                ),
                                            shape = RoundedCornerShape(8.dp),
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Dropdown",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.clickable { controller.toggleSeatPrefDropdown() },
                                                )
                                            },
                                            readOnly = true,
                                        )

                                        DropdownMenu(
                                            expanded = controller.isSeatPrefDropdownExpanded,
                                            onDismissRequest = { controller.dismissSeatPrefDropdown() },
                                            modifier =
                                                Modifier
                                                    .width(with(LocalDensity.current) { 300.dp })
                                                    .background(Color.White),
                                        ) {
                                            controller.seatPreferenceOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                                    onClick = { controller.updateSeatPreference(option) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Cab booking section - only show if mode of transport is "Cab"
                            if (controller.modeOfTransport == "Cab") {
                                CabBookingSection(controller)
                            }

                            // Tab buttons for Single/Multiple destinations (hidden for cab booking)
                            if (controller.modeOfTransport != "Cab") {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    // Single Destination Tab
                                    Button(
                                        onClick = { controller.toggleDestinationMode(false) },
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (!controller.isMultiDestination) PrimaryRed else Color.White,
                                                contentColor = if (!controller.isMultiDestination) Color.White else Color.Black,
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        border =
                                            BorderStroke(
                                                width = 1.dp,
                                                color = if (!controller.isMultiDestination) PrimaryRed else Color.LightGray,
                                            ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = "Single Destination",
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (!controller.isMultiDestination) Color.White else Color.Black,
                                        )
                                    }

                                    // Multiple Destinations Tab
                                    Button(
                                        onClick = { controller.toggleDestinationMode(true) },
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (controller.isMultiDestination) PrimaryRed else Color.White,
                                                contentColor = if (controller.isMultiDestination) Color.White else Color.Black,
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        border =
                                            BorderStroke(
                                                width = 1.dp,
                                                color = if (controller.isMultiDestination) PrimaryRed else Color.LightGray,
                                            ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = "Multiple Destinations",
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (controller.isMultiDestination) Color.White else Color.Black,
                                        )
                                    }
                                }
                            }

                            // Show destination fields based on mode (hidden for cab booking)
                            if (controller.modeOfTransport != "Cab") {
                                if (!controller.isMultiDestination) {
                                    // Single Destination Section
                                    // Destination header
                                    Text(
                                        text = "Destination",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                    )

                                    // White card containing destination fields
                                    Card(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = 2.dp,
                                        backgroundColor = Color.White,
                                    ) {
                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                        ) {
                                            // Origin City field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = controller.originCity,
                                                onValueChange = { controller.updateOriginCity(it) },
                                                placeholder = {
                                                    Text(
                                                        "Origin City *",
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Normal,
                                                        fontFamily = GraphikFontFamily,
                                                    )
                                                },
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 16.dp)
                                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                                colors =
                                                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                        unfocusedBorderColor = Color.Transparent,
                                                        focusedBorderColor = Color.Transparent,
                                                        cursorColor = Color.Black,
                                                        unfocusedContainerColor = Color.White,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedTextColor = Color.Black,
                                                        focusedTextColor = Color.Black,
                                                        unfocusedPlaceholderColor = Color.Gray,
                                                        focusedPlaceholderColor = Color.Gray,
                                                    ),
                                                shape = RoundedCornerShape(8.dp),
                                            )

                                            // Destination City field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = controller.destination,
                                                onValueChange = { controller.updateDestination(it) },
                                                placeholder = {
                                                    Text(
                                                        "Destination City *",
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Normal,
                                                        fontFamily = GraphikFontFamily,
                                                    )
                                                },
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 16.dp)
                                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                                colors =
                                                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                        unfocusedBorderColor = Color.Transparent,
                                                        focusedBorderColor = Color.Transparent,
                                                        cursorColor = Color.Black,
                                                        unfocusedContainerColor = Color.White,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedTextColor = Color.Black,
                                                        focusedTextColor = Color.Black,
                                                        unfocusedPlaceholderColor = Color.Gray,
                                                        focusedPlaceholderColor = Color.Gray,
                                                    ),
                                                shape = RoundedCornerShape(8.dp),
                                            )

                                            // Date Selection Row inside the card
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                // Departure Date
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    Text(
                                                        text = "Departure Date *",
                                                        fontSize = 14.sp,
                                                        fontFamily = GraphikFontFamily,
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(bottom = 4.dp),
                                                        fontWeight = FontWeight.SemiBold,
                                                    )

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
                                                                    .background(Color.White, RoundedCornerShape(8.dp))
                                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                                    .padding(16.dp)
                                                                    .clickable { showDepartureDatePicker = true },
                                                            contentAlignment = Alignment.CenterStart,
                                                        ) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .background(
                                                                            Color(0xFFEEEEEE),
                                                                            RoundedCornerShape(4.dp),
                                                                        ).padding(horizontal = 8.dp, vertical = 6.dp),
                                                            ) {
                                                                Text(
                                                                    text = controller.departureDate,
                                                                    color = Color.Black,
                                                                    fontSize = 15.sp,
                                                                    fontFamily = GraphikFontFamily,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Return Date
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    Text(
                                                        text = "Return Date *",
                                                        fontSize = 14.sp,
                                                        fontFamily = GraphikFontFamily,
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(bottom = 4.dp),
                                                        fontWeight = FontWeight.SemiBold,
                                                    )

                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 8.dp),
                                                    ) {
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .background(Color.White, RoundedCornerShape(8.dp))
                                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                                    .padding(16.dp)
                                                                    .clickable { showArrivalDatePicker = true },
                                                            contentAlignment = Alignment.CenterStart,
                                                        ) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .background(
                                                                            Color(0xFFEEEEEE),
                                                                            RoundedCornerShape(4.dp),
                                                                        ).padding(horizontal = 8.dp, vertical = 6.dp),
                                                            ) {
                                                                Text(
                                                                    text = controller.arrivalDate,
                                                                    color = Color.Black,
                                                                    fontSize = 15.sp,
                                                                    fontFamily = GraphikFontFamily,
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
                                                                fontFamily = GraphikFontFamily,
                                                            )
                                                        },
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(bottom = 16.dp)
                                                                .clickable(onClick = { controller.toggleFlightTimeDropdown() }),
                                                        colors =
                                                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                                unfocusedBorderColor = Color.LightGray,
                                                                focusedBorderColor = Color.LightGray,
                                                                cursorColor = Color.Black,
                                                                unfocusedContainerColor = Color.White,
                                                                focusedContainerColor = Color(0xFFF5F5F5),
                                                                unfocusedTextColor = Color.Black,
                                                                focusedTextColor = Color.Black,
                                                                unfocusedPlaceholderColor = Color.Gray,
                                                                focusedPlaceholderColor = Color.Gray,
                                                            ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        trailingIcon = {
                                                            Icon(
                                                                Icons.Default.KeyboardArrowDown,
                                                                contentDescription = "Dropdown",
                                                                tint = Color.Gray,
                                                                modifier = Modifier.clickable { controller.toggleFlightTimeDropdown() },
                                                            )
                                                        },
                                                        readOnly = true,
                                                    )

                                                    DropdownMenu(
                                                        expanded = controller.isFlightTimeDropdownExpanded,
                                                        onDismissRequest = { controller.dismissFlightTimeDropdown() },
                                                        modifier =
                                                            Modifier
                                                                .width(with(LocalDensity.current) { 300.dp })
                                                                .background(Color(0xCC000000)), // 80% opacity black background
                                                    ) {
                                                        controller.flightTimeOptions.forEach { option ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = option,
                                                                        color = Color.White,
                                                                        fontFamily = GraphikFontFamily,
                                                                    )
                                                                },
                                                                onClick = { controller.updateFlightTimePreference(option) },
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
                                                                fontFamily = GraphikFontFamily,
                                                            )
                                                        },
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clickable(onClick = { controller.toggleSeatPrefDropdown() }),
                                                        colors =
                                                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                                unfocusedBorderColor = Color.LightGray,
                                                                focusedBorderColor = Color.LightGray,
                                                                cursorColor = Color.Black,
                                                                unfocusedContainerColor = Color.White,
                                                                focusedContainerColor = Color(0xFFF5F5F5),
                                                                unfocusedTextColor = Color.Black,
                                                                focusedTextColor = Color.Black,
                                                                unfocusedPlaceholderColor = Color.Gray,
                                                                focusedPlaceholderColor = Color.Gray,
                                                            ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        trailingIcon = {
                                                            Icon(
                                                                Icons.Default.KeyboardArrowDown,
                                                                contentDescription = "Dropdown",
                                                                tint = Color.Gray,
                                                                modifier = Modifier.clickable { controller.toggleSeatPrefDropdown() },
                                                            )
                                                        },
                                                        readOnly = true,
                                                    )

                                                    DropdownMenu(
                                                        expanded = controller.isSeatPrefDropdownExpanded,
                                                        onDismissRequest = { controller.dismissSeatPrefDropdown() },
                                                        modifier =
                                                            Modifier
                                                                .width(with(LocalDensity.current) { 300.dp })
                                                                .background(Color(0xCC000000)), // 80% opacity black background
                                                    ) {
                                                        controller.seatPreferenceOptions.forEach { option ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = option,
                                                                        color = Color.White,
                                                                        fontFamily = GraphikFontFamily,
                                                                    )
                                                                },
                                                                onClick = { controller.updateSeatPreference(option) },
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

                                // Date Picker Dialogs for single destination (outside the card)
                                if (!controller.isMultiDestination) {
                                    // Departure Date Picker Dialog
                                    if (showDepartureDatePicker) {
                                        val datePickerState =
                                            rememberDatePickerState(
                                                initialDisplayMode = DisplayMode.Picker,
                                                initialSelectedDateMillis = System.currentTimeMillis(),
                                                selectableDates =
                                                    object : androidx.compose.material3.SelectableDates {
                                                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                                            // Only allow dates from today onwards
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
                                            },
                                        ) {
                                            DatePicker(state = datePickerState)
                                        }
                                    }

                                    // Arrival Date Picker Dialog
                                    if (showArrivalDatePicker) {
                                        val datePickerState =
                                            rememberDatePickerState(
                                                initialDisplayMode = DisplayMode.Picker,
                                                initialSelectedDateMillis = System.currentTimeMillis(),
                                                selectableDates =
                                                    object : androidx.compose.material3.SelectableDates {
                                                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                                            // Only allow dates from today onwards
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
                                            },
                                        ) {
                                            DatePicker(state = datePickerState)
                                        }
                                    }
                                }
                            }

                            // Meal Preference Toggle and Dropdown (only for Flight and Train)
                            if (controller.modeOfTransport == "Flight" || controller.modeOfTransport == "Train") {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "Select Meal Preference",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                    )

                                    // Toggle switch
                                    androidx.compose.material.Switch(
                                        checked = controller.mealPreferenceEnabled,
                                        onCheckedChange = { controller.toggleMealPreference(it) },
                                        modifier = Modifier.scale(1.2f),
                                        colors =
                                            androidx.compose.material.SwitchDefaults.colors(
                                                checkedThumbColor = Color(0xFFF6F4EE),
                                                checkedTrackColor = Color(0xFFADE1B6),
                                                uncheckedThumbColor = Color(0xFFF6F4EE),
                                                uncheckedTrackColor = Color.LightGray,
                                            ),
                                    )
                                }

                                // Show meal preference dropdown if enabled
                                if (controller.mealPreferenceEnabled) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = controller.mealPreference,
                                            onValueChange = { },
                                            label = { Text("Meal Preference", fontFamily = GraphikFontFamily) },
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 16.dp)
                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                    .clickable(onClick = { controller.toggleMealPrefDropdown() }),
                                            colors =
                                                OutlinedTextFieldDefaults.colors(
                                                    unfocusedBorderColor = Color.Transparent,
                                                    focusedBorderColor = Color.Transparent,
                                                    cursorColor = Color.Black,
                                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                                    focusedContainerColor = Color(0xFFF5F5F5),
                                                    unfocusedTextColor = Color.Black,
                                                    focusedTextColor = Color.Black,
                                                    unfocusedLabelColor = Color.Gray,
                                                    focusedLabelColor = Color.Gray,
                                                ),
                                            shape = RoundedCornerShape(8.dp),
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Dropdown",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.clickable { controller.toggleMealPrefDropdown() },
                                                )
                                            },
                                            readOnly = true,
                                        )

                                        DropdownMenu(
                                            expanded = controller.isMealPrefDropdownExpanded,
                                            onDismissRequest = { controller.dismissMealPrefDropdown() },
                                            modifier =
                                                Modifier
                                                    .width(with(LocalDensity.current) { 300.dp })
                                                    .background(Color.White),
                                        ) {
                                            controller.mealPreferenceOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(text = option, fontFamily = GraphikFontFamily) },
                                                    onClick = { controller.updateMealPreference(option) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Stay Required Toggle (only for non-cab transport modes)
                            if (controller.modeOfTransport != "Cab") {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "Stay Required",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                    )

                                    // Toggle switch
                                    androidx.compose.material.Switch(
                                        checked = controller.stayRequired,
                                        onCheckedChange = { controller.toggleStayRequired(it) },
                                        modifier = Modifier.scale(1.2f),
                                        colors =
                                            androidx.compose.material.SwitchDefaults.colors(
                                                checkedThumbColor = Color(0xFFF6F4EE),
                                                checkedTrackColor = Color(0xFFADE1B6),
                                                uncheckedThumbColor = Color(0xFFF6F4EE),
                                                uncheckedTrackColor = Color.LightGray,
                                            ),
                                    )
                                }

                                // Cab Required Toggle (only for non-cab transport modes)
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "Cab Required",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                    )

                                    // Toggle switch
                                    androidx.compose.material.Switch(
                                        checked = controller.cabRequired,
                                        onCheckedChange = { controller.toggleCabRequired(it) },
                                        modifier = Modifier.scale(1.2f),
                                        colors =
                                            androidx.compose.material.SwitchDefaults.colors(
                                                checkedThumbColor = Color(0xFFF6F4EE),
                                                checkedTrackColor = Color(0xFFADE1B6),
                                                uncheckedThumbColor = Color(0xFFF6F4EE),
                                                uncheckedTrackColor = Color.LightGray,
                                            ),
                                    )
                                }
                            }
                            } // Close modeOfTransport.isNotEmpty() conditional - destination fields only

                            // Approval Chain (always visible)
                            Text(
                                text = "Approval Chain",
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp),
                                fontWeight = FontWeight.SemiBold,
                            )

                            // Reporting Manager - custom layout with two-line label
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.width(140.dp),
                                ) {
                                    Text(
                                        text = "Reporting",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray,
                                    )
                                    Text(
                                        text = "Manager:",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray,
                                    )
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = controller.reportingManagerName,
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Error message if submission failed
                            controller.submissionError?.let { error ->
                                Text(
                                    text = error,
                                    color = PrimaryRed,
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }

                            // Submit Button - Different logic for cab vs other transport modes
                            Button(
                                onClick = {
                                    if (controller.modeOfTransport == "Cab") {
                                        controller.submitCabBookingRequest()
                                    } else {
                                        controller.submitTravelRequest()
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = PrimaryRed,
                                    ),
                                shape = RoundedCornerShape(30.dp),
                                enabled =
                                    if (controller.modeOfTransport ==
                                        "Cab"
                                    ) {
                                        !controller.isCabSubmitting
                                    } else {
                                        !controller.isSubmitting
                                    },
                            ) {
                                val isSubmitting =
                                    if (controller.modeOfTransport ==
                                        "Cab"
                                    ) {
                                        controller.isCabSubmitting
                                    } else {
                                        controller.isSubmitting
                                    }

                                if (isSubmitting) {
                                    androidx.compose.material.CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = if (controller.modeOfTransport == "Cab") "Submit Travel Request" else "Submit Travel Request",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
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
    } // Close FontScaleAdjusted block
}

/**
 * Composable for displaying employee detail rows with proper alignment
 */
@Composable
fun EmployeeDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(140.dp), // Increased fixed width for alignment
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDestinationSection(controller: TravelController) {
    // State for date picker dialogs
    val datePickerStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Destinations header with Add button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Destinations",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )

            Button(
                onClick = { controller.addDestination() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFB0B0B0),
                        disabledContentColor = Color(0xFF888888),
                    ),
                shape = RoundedCornerShape(10.dp),
                modifier =
                    Modifier
                        .height(40.dp)
                        .width(140.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp),
                enabled = controller.destinations.size < 3,
            ) {
                Text(
                    text = "Add Destination",
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                )
            }
        }

        // List of destinations
        controller.destinations.forEachIndexed { index, destination ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = 2.dp,
                backgroundColor = Color.White,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    // Destination header with delete button
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Destination ${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                        )

                        // Delete button (only show if there's more than one destination)
                        if (controller.destinations.size > 1) {
                            IconButton(
                                onClick = { controller.removeDestination(destination.id) },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bin),
                                    contentDescription = "Delete Destination",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    // Origin City field
                    androidx.compose.material3.OutlinedTextField(
                        value = destination.originCity,
                        onValueChange = { controller.updateDestinationOriginCity(destination.id, it) },
                        placeholder = {
                            Text(
                                "Origin City *",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedPlaceholderColor = Color.Gray,
                                focusedPlaceholderColor = Color.Gray,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    )

                    // Destination City field
                    androidx.compose.material3.OutlinedTextField(
                        value = destination.destination,
                        onValueChange = { controller.updateDestinationField(destination.id, it) },
                        placeholder = {
                            Text(
                                "Destination City *",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedPlaceholderColor = Color.Gray,
                                focusedPlaceholderColor = Color.Gray,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    )

                    // Date Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Departure Date
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Departure Date *",
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 4.dp),
                                fontWeight = FontWeight.SemiBold,
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(end = 8.dp),
                            ) {
                                val showDepartureDatePicker = datePickerStates["departure_${destination.id}"] ?: false

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                            .padding(16.dp)
                                            .clickable {
                                                datePickerStates["departure_${destination.id}"] = true
                                            },
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .background(
                                                    Color(0xFFEEEEEE),
                                                    RoundedCornerShape(4.dp),
                                                ).padding(horizontal = 8.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = destination.departureDate,
                                            color = Color.Black,
                                            fontSize = 15.sp,
                                            fontFamily = GraphikFontFamily,
                                        )
                                    }
                                }

                                // Departure Date Picker Dialog
                                if (showDepartureDatePicker) {
                                    val datePickerState =
                                        rememberDatePickerState(
                                            initialDisplayMode = DisplayMode.Picker,
                                            initialSelectedDateMillis = System.currentTimeMillis(),
                                            selectableDates =
                                                object : androidx.compose.material3.SelectableDates {
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
                                        },
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }
                        }

                        // Return Date
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Return Date *",
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 4.dp),
                                fontWeight = FontWeight.SemiBold,
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp),
                            ) {
                                val showReturnDatePicker = datePickerStates["return_${destination.id}"] ?: false

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                            .padding(16.dp)
                                            .clickable {
                                                datePickerStates["return_${destination.id}"] = true
                                            },
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .background(
                                                    Color(0xFFEEEEEE),
                                                    RoundedCornerShape(4.dp),
                                                ).padding(horizontal = 8.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = destination.returnDate,
                                            color = Color.Black,
                                            fontSize = 15.sp,
                                            fontFamily = GraphikFontFamily,
                                        )
                                    }
                                }

                                // Return Date Picker Dialog
                                if (showReturnDatePicker) {
                                    val datePickerState =
                                        rememberDatePickerState(
                                            initialDisplayMode = DisplayMode.Picker,
                                            initialSelectedDateMillis = System.currentTimeMillis(),
                                            selectableDates =
                                                object : androidx.compose.material3.SelectableDates {
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
                                        },
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
                            val isFlightTimeDropdownExpanded = datePickerStates["flightTime_${destination.id}"] ?: false

                            androidx.compose.material3.OutlinedTextField(
                                value = destination.flightTimePreference,
                                onValueChange = { },
                                placeholder = {
                                    Text(
                                        "Flight Time Preference",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                        .clickable(onClick = {
                                            datePickerStates["flightTime_${destination.id}"] = true
                                        }),
                                colors =
                                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color.Gray,
                                        focusedPlaceholderColor = Color.Gray,
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.Gray,
                                        modifier =
                                            Modifier.clickable {
                                                datePickerStates["flightTime_${destination.id}"] = true
                                            },
                                    )
                                },
                                readOnly = true,
                            )

                            DropdownMenu(
                                expanded = isFlightTimeDropdownExpanded,
                                onDismissRequest = { datePickerStates["flightTime_${destination.id}"] = false },
                                modifier =
                                    Modifier
                                        .width(with(LocalDensity.current) { 300.dp })
                                        .background(Color(0xCC000000)), // 80% opacity black background
                            ) {
                                controller.flightTimeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option, color = Color.White, fontFamily = GraphikFontFamily) },
                                        onClick = {
                                            controller.updateDestinationFlightTimePreference(destination.id, option)
                                            datePickerStates["flightTime_${destination.id}"] = false
                                        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabBookingSection(controller: TravelController) {
    // State for date picker dialog
    var showCabDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
    ) {

        // Travel Type Selection (Local Travel / Out of Local Station)
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { controller.toggleTravelTypeDropdown() }
            ) {
                OutlinedTextField(
                    value = if (controller.isLocalTravel) "Local Travel" else "Out of Local Station",
                    onValueChange = { },
                    placeholder = {
                        Text(
                            "Travel Type *",
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal,
                            fontFamily = GraphikFontFamily,
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = Color.Black,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Transparent,
                        ),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray,
                        )
                    },
                    readOnly = true,
                    enabled = false, // Disable text field interaction
                )
            }

            DropdownMenu(
                expanded = controller.isTravelTypeDropdownExpanded,
                onDismissRequest = { controller.dismissTravelTypeDropdown() },
                modifier =
                    Modifier
                        .width(with(LocalDensity.current) { 300.dp })
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
            ) {
                controller.travelTypeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium) },
                        onClick = { controller.updateTravelType(option) },
                    )
                }
            }
        }


        // Cab travel not allowed for single passenger note
        if (controller.isLocalTravel && controller.passengerCount == 1) {
            Text(
                text = "Note: Cab travel not allowed for single passenger.\nMinimum 2 members required.",
                color = PrimaryRed,
                fontSize = 12.sp,
                fontFamily = GraphikFontFamily,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }


        // Show cab details for all cab bookings
            // Cab Type Dropdown (enhanced with seat options)
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { controller.toggleCabTypeDropdown() }
                ) {
                    OutlinedTextField(
                        value = controller.cabType,
                        onValueChange = { },
                        placeholder = {
                            Text(
                                "Cab Type *",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Transparent,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = Color.Gray,
                            )
                        },
                        readOnly = true,
                        enabled = false, // Disable text field interaction
                    )
                }

                DropdownMenu(
                    expanded = controller.isCabTypeDropdownExpanded,
                    onDismissRequest = { controller.dismissCabTypeDropdown() },
                    modifier =
                        Modifier
                            .width(with(LocalDensity.current) { 300.dp })
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                ) {
                    controller.cabTypeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option, fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium) },
                            onClick = { controller.updateCabType(option) },
                        )
                    }
                }
            }

            // Search for Additional Attendees (only for local travel and after cab type is selected)
            if (controller.isLocalTravel && controller.cabType.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = controller.attendeeSearchQuery,
                        onValueChange = {
                            controller.updateAttendeeSearchQuery(it)
                            // Use suggested users search for cab booking
                            controller.searchSuggestedUsers(it)
                        },
                        placeholder = {
                            Text(
                                "Search Member to add",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_search_category_default),
                                contentDescription = "Search",
                                tint = Color.Gray,
                            )
                        },
                        trailingIcon = {
                            if (controller.attendeeSearchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    controller.updateAttendeeSearchQuery("")
                                    controller.searchSuggestedUsers("")
                                }) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        },
                    )
                }

                // Loading indicator for suggested users search
                if (controller.isSearchingSuggestedUsers) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF1976D2),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Searching members...",
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                }

                // Suggested Users Search Results
                if (controller.suggestedUsers.isNotEmpty()) {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp,
                        backgroundColor = Color.White,
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            controller.suggestedUsers.take(5).forEach { user ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { controller.addAttendeeFromSuggestedUser(user) }
                                            .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.displayName,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = user.mail,
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                        )
                                    }
                                }
                                if (user != controller.suggestedUsers.last()) {
                                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Added Attendees List
                if (controller.additionalAttendees.isNotEmpty()) {
                    Text(
                        text = "Additional Attendees",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    controller.additionalAttendees.forEachIndexed { index, attendee ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = attendee.name,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = attendee.email ?: if (attendee.employeeId != null) "${attendee.employeeId} • ${attendee.department}" else "Selected Member",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                )
                            }
                            IconButton(
                                onClick = { controller.removeAttendee(index) },
                                modifier = Modifier.size(20.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bin),
                                    contentDescription = "Remove",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Duration Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { controller.toggleCabDurationDropdown() }
                ) {
                    OutlinedTextField(
                        value = controller.cabDuration,
                        onValueChange = { },
                        placeholder = {
                            Text(
                                "Duration *",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Transparent,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = Color.Gray,
                            )
                        },
                        readOnly = true,
                        enabled = false, // Disable text field interaction
                    )
                }

                DropdownMenu(
                    expanded = controller.isCabDurationDropdownExpanded,
                    onDismissRequest = { controller.dismissCabDurationDropdown() },
                    modifier =
                        Modifier
                            .width(with(LocalDensity.current) { 300.dp })
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                ) {
                    controller.cabDurationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option, fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium) },
                            onClick = { controller.updateCabDuration(option) },
                        )
                    }
                }
            }

            // Pickup Location 1
            Text(
                text = "Pickup Location 1",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // White card containing Location and Map Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color.White,
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = controller.pickupLocation,
                        onValueChange = { controller.updatePickupLocation(it) },
                        placeholder = {
                            Text(
                                "Location *",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    )

                    OutlinedTextField(
                        value = controller.pickupMapDetails,
                        onValueChange = { controller.updatePickupMapDetails(it) },
                        placeholder = {
                            Text(
                                "Map Details",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }

            // Add Pickup Location Button (only for local travel, max 3 total pickup locations)
            if (controller.isLocalTravel) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { controller.addPickupLocation() },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFB0B0B0),
                                disabledContentColor = Color(0xFF888888),
                            ),
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .height(32.dp)
                                .width(160.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp),
                        enabled = controller.cabPickupLocations.size < 2,
                    ) {
                        Text(
                            text = "Add Pickup Location",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                        )
                    }
                }
            }

            // Additional Pickup Locations (only for local travel)
            if (controller.isLocalTravel) {
                controller.cabPickupLocations.forEachIndexed { index, location ->
                    // Header with title and delete button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pickup Location ${index + 2}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                        )

                        IconButton(
                            onClick = { controller.removePickupLocation(location.id) },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.bin),
                                contentDescription = "Remove Pickup Location",
                                tint = PrimaryRed,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // White card containing Location and Map Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color.White,
                        elevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = location.address,
                                onValueChange = { controller.updatePickupLocationAddress(location.id, it) },
                                placeholder = {
                                    Text(
                                        "Location *",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )

                            OutlinedTextField(
                                value = location.mapDetails,
                                onValueChange = { controller.updatePickupLocationMapDetails(location.id, it) },
                                placeholder = {
                                    Text(
                                        "Map Details",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        cursorColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            )
                        }
                    }
                }
            }

            // Drop Location
            Text(
                text = "Drop Location",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // White card containing Location and Map Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color.White,
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = controller.finalDropLocation,
                        onValueChange = { controller.updateFinalDropLocation(it) },
                        placeholder = {
                            Text(
                                "Location *",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    )

                    OutlinedTextField(
                        value = controller.dropMapDetails,
                        onValueChange = { controller.updateDropMapDetails(it) },
                        placeholder = {
                            Text(
                                "Map Details",
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                            ),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }

            // Date Selection
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .clickable { showCabDatePicker = true },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = controller.cabTravelDate,
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = Color.Gray,
                        )
                    }
                }
            }
        }

        // Date Picker Dialog
        if (showCabDatePicker) {
            val datePickerState =
                rememberDatePickerState(
                    initialDisplayMode = DisplayMode.Picker,
                    initialSelectedDateMillis = System.currentTimeMillis(),
                    selectableDates =
                        object : androidx.compose.material3.SelectableDates {
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
                onDismissRequest = { showCabDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            controller.updateCabTravelDateFromMillis(millis)
                        }
                        showCabDatePicker = false
                    }) {
                        Text("OK", color = Color.White, fontFamily = GraphikFontFamily)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCabDatePicker = false
                    }) {
                        Text("Cancel", color = Color.White, fontFamily = GraphikFontFamily)
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Error message if cab submission failed
        controller.cabSubmissionError?.let { error ->
            Text(
                text = error,
                color = PrimaryRed,
                fontSize = 14.sp,
                fontFamily = GraphikFontFamily,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
}
