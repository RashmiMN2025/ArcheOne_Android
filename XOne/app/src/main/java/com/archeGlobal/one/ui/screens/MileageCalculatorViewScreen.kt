package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class MileageTripUi(
    val id: String,
    val customerName: String,
    val date: String,
    val startPoint: String,
    val endPoint: String,
    val type: String,
    val vehicle: String,
    val amount: String,
    val distance: String,
    val status: String
)

@Composable
fun MileageCalculatorViewScreen(onBack: () -> Unit) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var showAddMileageSheet by rememberSaveable { mutableStateOf(false) }

    val trips = remember {
        listOf(
            MileageTripUi(
                id = "MLG-1001",
                customerName = "Rahul Sharma",
                date = "Jun 28, 2026",
                startPoint = "Bangalore",
                endPoint = "Mysore",
                type = "Business",
                vehicle = "Car",
                amount = "Rs 1,250",
                distance = "142 km",
                status = "Approved"
            ),
            MileageTripUi(
                id = "MLG-1002",
                customerName = "Priya Nair",
                date = "Jun 25, 2026",
                startPoint = "Hyderabad",
                endPoint = "Secunderabad",
                type = "Personal",
                vehicle = "Bike",
                amount = "Rs 480",
                distance = "28 km",
                status = "Pending"
            )
        )
    }

    val filteredTrips = trips.filter { trip ->
        searchText.isBlank() ||
            trip.id.contains(searchText, ignoreCase = true) ||
            trip.customerName.contains(searchText, ignoreCase = true) ||
            trip.startPoint.contains(searchText, ignoreCase = true) ||
            trip.endPoint.contains(searchText, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Mileage Overview",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    actions = { Spacer(modifier = Modifier.size(48.dp)) }
                )
            }

            item {
                Text(
                    text = "Mileage Overview",
                    fontFamily = GraphikFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Track and review all travel-based mileage submissions in one place.",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MileageStatCard(
                        title = "Total distance logged",
                        value = "0.00 km",
                        icon = Icons.Default.DirectionsCar,
                        modifier = Modifier.weight(1f)
                    )
                    MileageStatCard(
                        title = "Total Carbon Emission",
                        value = "0.00 Kg CO₂e",
                        icon = Icons.Default.Eco,
                        modifier = Modifier.weight(1f)
                    )
                    MileageStatCard(
                        title = "Total claim amount",
                        value = "Rs 0.00",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "My recent trips",
                        fontFamily = GraphikFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        },
                        placeholder = {
                            Text(
                                text = "Search trips...",
                                color = Color.Gray,
                                fontFamily = GraphikFontFamily,
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4)
                        )
                    )
                }
            }

            if (filteredTrips.isEmpty()) {
                item {
                    MileageEmptyState(message = "You haven't spent anything yet")
                }
            } else {
                items(filteredTrips) { trip ->
                    MileageTripCard(
                        trip = trip,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Button(
            onClick = { showAddMileageSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = PrimaryRed,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Expense",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showAddMileageSheet) {
        AddMileageExpenseBottomSheet(onDismiss = { showAddMileageSheet = false })
    }
}

@Composable
private fun MileageStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(125.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                text = value,
                fontFamily = GraphikFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun MileageTripCard(
    trip: MileageTripUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.id,
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = trip.status,
                    color = Color(0xFF2E7D32),
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Divider(color = Color(0xFFEAEAEA))

            MileageDetailRow("Customer Name", trip.customerName)
            MileageDetailRow("Date", trip.date)
            MileageDetailRow("Route", "${trip.startPoint} → ${trip.endPoint}")
            MileageDetailRow("Type", trip.type)
            MileageDetailRow("Vehicle", trip.vehicle)
            MileageDetailRow("Amount", trip.amount)
            MileageDetailRow("Distance", trip.distance)

            Divider(color = Color(0xFFEAEAEA))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View Details",
                    color = PrimaryRed,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MileageDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MileageEmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No data",
            fontFamily = GraphikFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = message,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMileageExpenseBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var customerName by rememberSaveable { mutableStateOf("") }
    var projectId by rememberSaveable { mutableStateOf("") }
    var fromDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var toDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showFromDatePicker by rememberSaveable { mutableStateOf(false) }
    var showToDatePicker by rememberSaveable { mutableStateOf(false) }
    var startLocation by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var vehicleType by rememberSaveable { mutableStateOf("") }
    var vehicle by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val fromDateText = fromDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select date"
    val toDateText = toDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select date"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = null,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Mileage Expense",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Cancel",
                    color = PrimaryRed,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Mileage Expense",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add and manage mileage expenses for travel reimbursements.",
                            fontFamily = GraphikFontFamily,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                MileageFormInput("Enter customer name", customerName) { customerName = it }
                MileageDropdownField(
                    label = "Project ID",
                    value = projectId,
                    options = listOf("PROJ-001", "PROJ-002", "PROJ-003"),
                    onValueChange = { projectId = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MileageDateField(
                        label = "From Date",
                        value = fromDateText,
                        onClick = { showFromDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    MileageDateField(
                        label = "To Date",
                        value = toDateText,
                        onClick = { showToDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                MileageFormInput("Select starting location", startLocation) { startLocation = it }
                MileageFormInput("Select destination", destination) { destination = it }
                MileageFormInput("Distance will be calculated automatically", "", enabled = false) { }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MileageDropdownField(
                        label = "Vehicle Type",
                        value = vehicleType,
                        options = listOf("PERSONAL", "COMPANY"),
                        onValueChange = { vehicleType = it },
                        modifier = Modifier.weight(1f)
                    )
                    MileageDropdownField(
                        label = "Vehicle",
                        value = vehicle,
                        options = listOf("CAR", "BIKE"),
                        onValueChange = { vehicle = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                MileageFormInput("Price will be calculated automatically", "", enabled = false) { }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Expense", fontFamily = GraphikFontFamily, fontSize = 16.sp)
            }
        }
    }

    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = fromDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fromDateMillis = millis
                        if (toDateMillis != null && toDateMillis!! < millis) {
                            toDateMillis = millis
                        }
                    }
                    showFromDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = toDateMillis ?: fromDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        if (fromDateMillis == null || millis >= fromDateMillis!!) {
                            toDateMillis = millis
                        }
                    }
                    showToDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun MileageFormInput(
    placeholder: String,
    value: String,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray,
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = Color.LightGray,
            unfocusedBorderColor = Color(0xFFD4D4D4),
            disabledBorderColor = Color(0xFFD4D4D4),
            disabledTextColor = Color.Gray
        )
    )
}

@Composable
private fun MileageDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value.ifBlank { "Select" },
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    disabledTextColor = if (value.isBlank()) Color.Gray else Color.Black,
                    disabledBorderColor = Color(0xFFD4D4D4),
                    disabledTrailingIconColor = Color.Gray
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onValueChange(option)
                        expanded = false
                    }) {
                        Text(option, fontFamily = GraphikFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun MileageDateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    disabledTextColor = if (value == "Select date") Color.Gray else Color.Black,
                    disabledBorderColor = Color(0xFFD4D4D4)
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            )
        }
    }
}
