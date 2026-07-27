package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.TravelRequestItemUi
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TravelExpenseRequestViewScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val controller = remember { TravelExpenseController(context) }
    val isLoading by controller.isLoading
    val errorMessage by controller.errorMessage
    val requests by controller.trips
    val projectOptions by controller.projectOptions
    val projectOptionsLoading by controller.projectOptionsLoading
    val projectOptionsError by controller.projectOptionsError
    var selectedRequest by remember { mutableStateOf<TravelRequestItemUi?>(null) }
    var showDetailScreen by remember { mutableStateOf(false) }
    var isFetchingDetail by remember { mutableStateOf(false) }

    if (showDetailScreen && selectedRequest != null) {
        BackHandler {
            showDetailScreen = false
        }
        TravelExpenseRequestDetailViewScreen(
            controller = controller,
            request = selectedRequest!!,
            onBack = { showDetailScreen = false },
            projectOptions = projectOptions,
            projectOptionsLoading = projectOptionsLoading,
            projectOptionsError = projectOptionsError
        )
        return
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        controller.fetchTrips()
        controller.fetchProjectOptions()
    }

    var searchText by rememberSaveable { mutableStateOf("") }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    val filteredRequests = requests.filter { request ->
        searchText.isBlank() ||
            request.destination.contains(searchText, ignoreCase = true) ||
            request.requestId.contains(searchText, ignoreCase = true) ||
            request.tripCode.contains(searchText, ignoreCase = true) ||
            request.projectId.contains(searchText, ignoreCase = true) ||
            request.status.contains(searchText, ignoreCase = true)
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
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Travel Request",
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

            Text(
                text = "Travel Request",
                fontFamily = GraphikFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Create and manage travel requests with trip details and approvals.",
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                placeholder = {
                    Text(
                        text = "Search travel requests...",
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp
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

            Text(
                text = "All Travel Requests",
                fontFamily = GraphikFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to load travel requests",
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                        color = PrimaryRed,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No travel requests found",
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                if (isFetchingDetail) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryRed)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredRequests) { request ->
                            TravelHistoryCard(
                                request = request,
                                onViewDetails = {
                                    isFetchingDetail = true
                                    controller.fetchTrip(
                                        tripId = it.tripId,
                                        onSuccess = { tripDetail ->
                                            selectedRequest = tripDetail
                                            showDetailScreen = true
                                            isFetchingDetail = false
                                        },
                                        onError = { message ->
                                            isFetchingDetail = false
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                controller.fetchProjectOptions()
                showCreateDialog = true
            },
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
            Icon(Icons.Default.Add, contentDescription = "Add New", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add New",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showCreateDialog) {
        CreateTravelRequestBottomSheet(
            controller = controller,
            onDismiss = { showCreateDialog = false },
            onRequestCreated = { showCreateDialog = false },
            projectOptions = projectOptions,
            projectOptionsLoading = projectOptionsLoading,
            projectOptionsError = projectOptionsError
        )
    }
}

@Composable
private fun TravelHistoryCard(request: TravelRequestItemUi, onViewDetails: (TravelRequestItemUi) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails(request) },
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
                    text = "TR-${request.requestId}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = request.status.replaceFirstChar { it.uppercase() },
                    color = when (request.status.lowercase()) {
                        "approved" -> Color(0xFF2E7D32)
                        "pending", "submitted" -> Color(0xFF757575)
                        "rejected" -> PrimaryRed
                        else -> Color.Gray
                    },
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            when (request.status.lowercase()) {
                                "approved" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                "pending", "submitted" -> Color(0xFFEF6C00).copy(alpha = 0.15f)
                                "rejected" -> PrimaryRed.copy(alpha = 0.15f)
                                else -> Color.Gray.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Divider(color = Color(0xFFEAEAEA), thickness = 1.dp)

            InfoRow("Request ID", "TR-${request.requestId}")
            InfoRow("Trip ID", request.tripCode)
            InfoRow("Project ID", request.projectId)
            InfoRow("Destination", request.destination)
            InfoRow("Travel Dates", request.travelDates)
            InfoRow("Estimated Cost", request.estimatedCost)
            InfoRow("Mode of Travel", request.modeOfTravel)
            InfoRow("Hotel", request.hotelNeeded)
            InfoRow("Vehicle", request.vehicleNeeded)
            InfoRow("Advance", request.advanceNeeded)
            InfoRow("Requested Amount", request.firstAdvanceRequestedAmount)
            InfoRow("Approved Amount", request.approvedAmount)

            Divider(color = Color(0xFFEAEAEA), thickness = 1.dp)

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
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryRed
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTravelRequestBottomSheet(
    controller: TravelExpenseController,
    onDismiss: () -> Unit,
    onRequestCreated: () -> Unit,
    projectOptions: List<com.archeGlobal.one.network.ProjectOption>,
    projectOptionsLoading: Boolean,
    projectOptionsError: String?,
    isEditMode: Boolean = false,
    initialRequest: com.archeGlobal.one.network.TravelRequestItemUi? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displayDateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val apiDateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    var projectId by rememberSaveable { mutableStateOf(initialRequest?.projectId ?: "") }
    var description by rememberSaveable { mutableStateOf(initialRequest?.description?.takeIf { it != "-" } ?: "") }
    var destination by rememberSaveable { mutableStateOf(initialRequest?.destination ?: "") }
    var estimatedCost by rememberSaveable { mutableStateOf(initialRequest?.estimatedCost?.removePrefix("Rs ")?.replace(",", "")?.takeIf { it != "-" } ?: "") }
    var modeOfTravel by rememberSaveable { mutableStateOf(initialRequest?.modeOfTravel ?: "") }
    var startDateMillis by rememberSaveable { mutableStateOf<Long?>(initialRequest?.startDate?.let { parseDateString(it) }) }
    var endDateMillis by rememberSaveable { mutableStateOf<Long?>(initialRequest?.endDate?.let { parseDateString(it) }) }
    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }
    var advanceAmount by rememberSaveable { mutableStateOf(initialRequest?.firstAdvanceRequestedAmountRaw?.takeIf { it.isNotBlank() } ?: "") }
    var advanceType by rememberSaveable { mutableStateOf("") }
    var hotelNeeded by rememberSaveable { mutableStateOf(initialRequest?.hotelNeeded == "Yes") }
    var vehicleNeeded by rememberSaveable { mutableStateOf(initialRequest?.vehicleNeeded == "Yes") }
    var advanceNeeded by rememberSaveable { mutableStateOf(initialRequest?.advanceNeeded == "Yes") }
    val scrollState = rememberScrollState()

    LaunchedEffect(initialRequest?.tripId, initialRequest?.startDate, initialRequest?.endDate) {
        startDateMillis = initialRequest?.startDate?.let { parseDateString(it) }
        endDateMillis = initialRequest?.endDate?.let { parseDateString(it) }
        advanceAmount = initialRequest?.firstAdvanceRequestedAmountRaw?.takeIf { it.isNotBlank() } ?: ""
    }

    val startDateText = startDateMillis?.let { displayDateFormatter.format(Date(it)) } ?: "Select date"
    val endDateText = endDateMillis?.let { displayDateFormatter.format(Date(it)) } ?: "Select date"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFFF6F4EE),
        dragHandle = null,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                    text = if (isEditMode) "Update Travel Request" else "Create New Travel Request",
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
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Travel Request",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Submit a new travel request for approval and track your travel plans.",
                            fontFamily = GraphikFontFamily,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (projectOptionsLoading) {
                    Text(
                        text = "Loading project options...",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                if (!projectOptionsError.isNullOrBlank()) {
                    Text(
                        text = projectOptionsError,
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = PrimaryRed,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                DropdownField(
                    label = "Project ID *",
                    value = projectId,
                    options = if (projectOptions.isNotEmpty()) projectOptions.map { it.code } else listOf("No project options available"),
                    onValueChange = { projectId = it }
                )
                FormInput("Description *","Enter Description", description) { description = it }
                FormInput("Destination *","Enter destination", destination) { destination = it }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateSelectorField(
                        label = "Start Date *",
                        value = startDateText,
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    DateSelectorField(
                        label = "End Date *",
                        value = endDateText,
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                FormInput("Estimated Total Cost *","Estimated Total Cost *", estimatedCost) { estimatedCost = it }

                DropdownField(
                    label = "Mode Of Travel *",
                    value = modeOfTravel,
                    options = listOf("Flight", "Train", "Bus", "Own vehicle"),
                    onValueChange = { modeOfTravel = it }
                )
                SwitchRow("Hotel Accommodation Needed ?", hotelNeeded) { hotelNeeded = it }
                SwitchRow("Vehicle Needed ?", vehicleNeeded) { vehicleNeeded = it }
                SwitchRow("Advance Needed ?", advanceNeeded) { advanceNeeded = it }
                if (advanceNeeded) {
                    FormInput("Amount *","Enter advance amount", advanceAmount) { advanceAmount = it }
                    DropdownField(
                        label = "Advance Type *",
                        value = advanceType,
                        options = listOf("Cash"),
                        onValueChange = { advanceType = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val selectedProject = projectOptions.firstOrNull { it.code == projectId }
                    if (projectOptionsLoading || projectOptions.isEmpty()) {
                        Toast.makeText(context, "Project options are still loading. Please wait.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedProject == null) {
                        Toast.makeText(context, "Please select a valid project", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (destination.isBlank()) {
                        Toast.makeText(context, "Please enter destination", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (estimatedCost.isBlank() || estimatedCost.toDoubleOrNull() == null) {
                        Toast.makeText(context, "Please enter a valid estimated cost", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (startDateMillis == null || endDateMillis == null) {
                        Toast.makeText(context, "Please select travel dates", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (endDateMillis!! < startDateMillis!!) {
                        Toast.makeText(context, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (modeOfTravel.isBlank()) {
                        Toast.makeText(context, "Please select mode of travel", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val estimatedAmountValue = estimatedCost.toDoubleOrNull() ?: 0.0
                    val advanceAmountValue = if (advanceNeeded) {
                        advanceAmount.toDoubleOrNull()?.also {
                            if (it < 0) {
                                Toast.makeText(context, "Please enter a valid advance amount", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        0.0
                    }
                    if (advanceNeeded && advanceAmountValue == null) return@Button

                    val requestPayload = com.archeGlobal.one.network.CreateTripRequest(
                        destination = destination,
                        startDate = apiDateFormatter.format(Date(startDateMillis!!)),
                        endDate = apiDateFormatter.format(Date(endDateMillis!!)),
                        description = description.ifBlank { null },
                        hotelAccommodationNeeded = hotelNeeded,
                        modeOfTravel = modeOfTravel.lowercase(Locale.getDefault()),
                        vehicleNeeded = vehicleNeeded,
                        advanceNeeded = advanceNeeded,
                        advanceAmount = advanceAmountValue ?: 0.0,
                        estimatedAmount = estimatedAmountValue,
                        requestedAmount = advanceAmountValue ?: 0.0,
                        projectId = selectedProject.id
                    )

                    if (isEditMode && initialRequest != null) {
                        controller.updateTrip(
                            tripId = initialRequest.tripId,
                            request = requestPayload,
                            onSuccess = {
                                onRequestCreated()
                            },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        controller.createTrip(
                            request = requestPayload,
                            onSuccess = {
                                onRequestCreated()
                            },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isEditMode) "Update Request" else "Create Request", fontFamily = GraphikFontFamily, fontSize = 16.sp)
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = startDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDateMillis = millis
                        if (endDateMillis != null && endDateMillis!! < millis) {
                            endDateMillis = millis
                        }
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = endDateMillis ?: startDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        if (startDateMillis == null || millis >= startDateMillis!!) {
                            endDateMillis = millis
                        }
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun parseDateString(dateString: String): Long? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        formatter.parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun FormInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color(0xFFD4D4D4)
                )
            )
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
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
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    ) {
                        Text(
                            option,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelectorField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
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
