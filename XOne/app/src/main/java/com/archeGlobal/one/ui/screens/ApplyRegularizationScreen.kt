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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val regularizationActions = listOf(
    "Regularization",
    "Forgot to Punch",
    "Early Leave",
    "Late Arrival",
    "Work from Home",
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApplyRegularizationScreen(
    date: LocalDate = LocalDate.now(),
    scheduledIn: String = "",
    scheduledOut: String = "",
    actualIn: String = "",
    actualOut: String = "",
    prePostTime: String = "",
    shiftName: String = "",
    totalHours: String = "",
    onBack: () -> Unit,
) {
    val primaryRed = Color(0xFFDD3825)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var selectedAction by remember { mutableStateOf("") }
    var actionExpanded by remember { mutableStateOf(false) }
    var inTime by remember { mutableStateOf(LocalTime.now()) }
    var outTime by remember { mutableStateOf(LocalTime.now()) }
    var showInTimePicker by remember { mutableStateOf(false) }
    var showOutTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    var description by remember { mutableStateOf("") }

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
                            text = "Apply Regularization",
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
                    // Regularization Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Regularization of - ${date.format(dateFormatter)}",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Scheduled",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888),
                                )
                                Text(
                                    text = "Actual",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (scheduledIn.isNotEmpty() && scheduledOut.isNotEmpty()) "$scheduledIn to $scheduledOut" else "—",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                )
                                Text(
                                    text = if (actualIn.isNotEmpty() && actualOut.isNotEmpty()) "$actualIn to $actualOut" else "—",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Pre-post time",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888),
                                )
                                Text(
                                    text = "Shift Name",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = prePostTime.ifEmpty { "—" },
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                )
                                Text(
                                    text = shiftName.ifEmpty { "—" },
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Total Hours",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFF888888),
                                )
                                Text(
                                    text = totalHours.ifEmpty { "—" },
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.Black,
                                )
                            }
                        }
                    }

                    // Action & Description Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Action & Description",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Select Action",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = actionExpanded,
                                onExpandedChange = { actionExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedAction.ifEmpty { "Select Action" },
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
                                        unfocusedTextColor = if (selectedAction.isEmpty()) Color(0xFF888888) else Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = actionExpanded,
                                    onDismissRequest = { actionExpanded = false },
                                    modifier = Modifier.background(Color.White),
                                ) {
                                    regularizationActions.forEach { action ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = action,
                                                    fontFamily = GraphikFontFamily,
                                                    fontSize = 14.sp,
                                                    color = Color.Black,
                                                )
                                            },
                                            onClick = {
                                                selectedAction = action
                                                actionExpanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // In Time / Out Time row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.width(130.dp)) {
                                    Text(
                                        text = "In Time",
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
                                Column(modifier = Modifier.width(130.dp)) {
                                    Text(
                                        text = "Out Time",
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
                                    focusedBorderColor = primaryRed,
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

                    // Approver Card
                    ApproverCard(
                        name = reportingManagerName,
                        email = reportingManagerEmail,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Submit button
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
                            text = "Submit Regularization",
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
