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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val leaveTypes = listOf(
    "Privilege Leave",
    "Casual Leave",
    "Sick Leave",
    "Paternity Leave",
    "Optional Holiday",
    "Work from Home",
)

private val leaveReasons = listOf("Personal", "Outdoor", "Not Well")

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApplyLeaveScreen(
    initialLeaveType: String = "",
    initialDate: LocalDate = LocalDate.now(),
    onBack: () -> Unit,
) {
    val primaryRed = Color(0xFFDD3825)

    var selectedLeaveType by remember { mutableStateOf(initialLeaveType) }
    var leaveTypeExpanded by remember { mutableStateOf(false) }

    var fromDate by remember { mutableStateOf(initialDate) }
    var toDate by remember { mutableStateOf(initialDate) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    var selectedReason by remember { mutableStateOf("") }
    var reasonExpanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val totalDays = (toDate.toEpochDay() - fromDate.toEpochDay() + 1).coerceAtLeast(1)

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
                            text = "Apply for Leave",
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
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
                                        fontSize = 14.sp,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = leaveTypeExpanded,
                                    onDismissRequest = { leaveTypeExpanded = false },
                                    modifier = Modifier.background(Color.White),
                                ) {
                                    leaveTypes.forEach { type ->
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
                                        color = Color(0xFF888888),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .clickable { showFromDatePicker = true }
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                    ) {
                                        Text(
                                            text = fromDate.format(dateFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 14.sp,
                                            color = Color.Black,
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
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                    ) {
                                        Text(
                                            text = toDate.format(dateFormatter),
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 14.sp,
                                            color = Color.Black,
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
                        }
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
                                        if (toDate.isBefore(selected)) toDate = selected
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = reasonExpanded,
                                onExpandedChange = { reasonExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedReason.ifEmpty { "Select Reason" },
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Description",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        text = "Enter additional details (optional)",
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
                                    focusedBorderColor = primaryRed,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = GraphikFontFamily,
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
                        onClick = { /* TODO: submit */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                    ) {
                        Text(
                            text = "Apply Leave",
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            color = Color(0xFF2196F3),
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
