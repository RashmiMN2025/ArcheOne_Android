package com.archeGlobal.one.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.ui.activities.TravelApproveActivity
import com.archeGlobal.one.ui.activities.TravelRejectActivity
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelApprovalsScreen(controller: TravelController) {
    // State for rejection dialog
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionRemarks by remember { mutableStateOf("") }
    var selectedRequestId by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedLocationFilter by remember { mutableStateOf("All") }
    var selectedTransportFilter by remember { mutableStateOf("All") }

    // Initialize dates with current date range (last 30 days to today)
    val currentDate = remember { Calendar.getInstance() }
    val thirtyDaysAgo = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -30)
        }
    }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var fromDate by remember { mutableStateOf(dateFormatter.format(thirtyDaysAgo.time)) }
    var toDate by remember { mutableStateOf(dateFormatter.format(currentDate.time)) }
    var showDateFilterDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showLocationDropdown by remember { mutableStateOf(false) }
    var showTransportDropdown by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }

    // Calendar state
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    // Rejection dialog
    if (showRejectionDialog) {
        AlertDialog(
            onDismissRequest = {
                showRejectionDialog = false
                rejectionRemarks = ""
            },
            title = {
                Text(
                    "Rejection Reason",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column {
                    Text(
                        "Please provide a reason for rejecting this travel request:",
                        fontFamily = GraphikFontFamily,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionRemarks,
                        onValueChange = { rejectionRemarks = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                        placeholder = { Text("Enter rejection reason") },
                        maxLines = 3,
                    )

                    // Focus the text field when dialog appears
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        controller.rejectTravelRequest(selectedRequestId, rejectionRemarks)
                        showRejectionDialog = false
                        rejectionRemarks = ""
                    },
                    enabled = rejectionRemarks.isNotBlank(),
                ) {
                    Text(
                        "Submit",
                        color = if (rejectionRemarks.isNotBlank()) PrimaryRed else Color.Gray,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectionDialog = false
                    rejectionRemarks = ""
                }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Get context and font adjustment for consistent font scaling
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

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
                            Text(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .offset(x = (-24).dp),
                                text = "Travel Approvals",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
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
                        actions = {},
                    )

                    // Load travel approvals only on initial screen load
                    // The state is already updated optimistically by the controller after approve/reject
                    // No need to reload on resume - the local state changes will be reflected automatically
                    LaunchedEffect(Unit) {
                        // Only load if we don't have data yet (Idle state)
                        if (controller.travelApprovalsState is TravelController.TravelApprovalsState.Idle) {
                            controller.loadTravelApprovals()
                        }
                    }

                    // Filter Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            placeholder = {
                                Text(
                                    text = "Search by Employee Name",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = Color.White,
                                focusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    // Trigger search - filtering is already reactive via remember
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date Filter Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    Text(
                                        text = "Date Filter",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontFamily = GraphikFontFamily,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showDateFilterDropdown = !showDateFilterDropdown }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedDateFilter,
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.Black
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }

                                // Date Filter Dropdown Menu
                                DropdownMenu(
                                    expanded = showDateFilterDropdown,
                                    onDismissRequest = { showDateFilterDropdown = false },
                                    modifier = Modifier
                                        .background(Color(0xFF424242))
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    listOf("All", "1 Week", "1 Month", "Date Range").forEach { option ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedDateFilter = option
                                                showDateFilterDropdown = false
                                                if (option == "Date Range") {
                                                    showCalendar = true
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = option,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Category Filter Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    Text(
                                        text = "Category Filter",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontFamily = GraphikFontFamily,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showCategoryDropdown = !showCategoryDropdown }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedCategoryFilter,
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.Black
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }

                                // Category Filter Dropdown Menu
                                DropdownMenu(
                                    expanded = showCategoryDropdown,
                                    onDismissRequest = { showCategoryDropdown = false },
                                    modifier = Modifier
                                        .background(Color(0xFF424242))
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    listOf("All", "Status", "Location", "Mode of Transport").forEach { option ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedCategoryFilter = option
                                                showCategoryDropdown = false
                                            }
                                        ) {
                                            Text(
                                                text = option,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Filter Row (centered) - Only show when Status category is selected
                        if (selectedCategoryFilter == "Status") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Status Filter",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontFamily = GraphikFontFamily,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Box(modifier = Modifier.width(200.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showStatusDropdown = !showStatusDropdown }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedStatusFilter,
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.Black
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray
                                            )
                                        }
                                    }

                                    // Status Filter Dropdown Menu
                                    DropdownMenu(
                                        expanded = showStatusDropdown,
                                        onDismissRequest = { showStatusDropdown = false },
                                        modifier = Modifier
                                            .background(Color(0xFF424242))
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        listOf("All", "Pending", "Approved", "Rejected", "Cancelled").forEach { option ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    selectedStatusFilter = option
                                                    showStatusDropdown = false
                                                }
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = GraphikFontFamily,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Location Filter Row (centered) - Only show when Location category is selected
                        if (selectedCategoryFilter == "Location") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Location Filter",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontFamily = GraphikFontFamily,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Box(modifier = Modifier.width(200.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showLocationDropdown = !showLocationDropdown }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedLocationFilter,
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.Black
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray
                                            )
                                        }
                                    }

                                    // Location Filter Dropdown Menu
                                    DropdownMenu(
                                        expanded = showLocationDropdown,
                                        onDismissRequest = { showLocationDropdown = false },
                                        modifier = Modifier
                                            .background(Color(0xFF424242))
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        // Get unique locations from the current state
                                        val locations = remember(controller.travelApprovalsState) {
                                            val currentState = controller.travelApprovalsState
                                            if (currentState is TravelController.TravelApprovalsState.Success) {
                                                listOf("All") + currentState.approvalRequests
                                                    .mapNotNull { it.destination }
                                                    .distinct()
                                                    .sorted()
                                            } else {
                                                listOf("All")
                                            }
                                        }

                                        locations.forEach { option ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    selectedLocationFilter = option
                                                    showLocationDropdown = false
                                                }
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = GraphikFontFamily,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Mode of Transport Filter Row (centered) - Only show when Mode of Transport category is selected
                        if (selectedCategoryFilter == "Mode of Transport") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Mode of Transport Filter",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontFamily = GraphikFontFamily,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Box(modifier = Modifier.width(200.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showTransportDropdown = !showTransportDropdown }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedTransportFilter,
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                color = Color.Black
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray
                                            )
                                        }
                                    }

                                    // Mode of Transport Filter Dropdown Menu
                                    DropdownMenu(
                                        expanded = showTransportDropdown,
                                        onDismissRequest = { showTransportDropdown = false },
                                        modifier = Modifier
                                            .background(Color(0xFF424242))
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        // Get unique modes of transport from the current state
                                        val transportModes = remember(controller.travelApprovalsState) {
                                            val currentState = controller.travelApprovalsState
                                            if (currentState is TravelController.TravelApprovalsState.Success) {
                                                val modes = currentState.approvalRequests
                                                    .mapNotNull { it.modeOfTransport }
                                                    .distinct()
                                                    .sorted()
                                                Log.d("TravelApprovalsScreen", "Transport modes found: $modes")
                                                listOf("All") + modes
                                            } else {
                                                listOf("All", "Flight", "Bus", "Train", "Cab")
                                            }
                                        }

                                        transportModes.forEach { option ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    selectedTransportFilter = option
                                                    showTransportDropdown = false
                                                }
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = GraphikFontFamily,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Date Range Display (only show when Date Range is selected)
                        if (selectedDateFilter == "Date Range") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DateDisplayCard(
                                    label = "From:",
                                    date = fromDate,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    isFromDatePicker = true
                                    showDatePicker = true
                                }

                                DateDisplayCard(
                                    label = "To:",
                                    date = toDate,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    isFromDatePicker = false
                                    showDatePicker = true
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main content based on state
                    when (val state = controller.travelApprovalsState) {
                        is TravelController.TravelApprovalsState.Idle -> {
                            // Show nothing initially
                        }

                        is TravelController.TravelApprovalsState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = PrimaryRed)
                            }
                        }

                        is TravelController.TravelApprovalsState.Success -> {
                            // Apply filters to the requests - remember to trigger recomposition when filters change
                            val filteredRequests = remember(
                                state.approvalRequests,
                                searchQuery,
                                selectedStatusFilter,
                                selectedLocationFilter,
                                selectedTransportFilter,
                                selectedCategoryFilter,
                                selectedDateFilter,
                                fromDate,
                                toDate
                            ) {
                                Log.d("TravelApprovalsScreen", "Filtering ${state.approvalRequests.size} requests with: search='$searchQuery', status='$selectedStatusFilter', location='$selectedLocationFilter', transport='$selectedTransportFilter', category='$selectedCategoryFilter', date='$selectedDateFilter'")
                                state.approvalRequests.filter { request ->
                                    // Apply search filter
                                    val matchesSearch = searchQuery.isEmpty() ||
                                        request.employeeName?.contains(searchQuery, ignoreCase = true) == true ||
                                        request.id.contains(searchQuery, ignoreCase = true)

                                    // Apply status filter
                                    val matchesStatus = selectedStatusFilter == "All" ||
                                        request.status.name.equals(selectedStatusFilter, ignoreCase = true)

                                    // Apply location filter
                                    val matchesLocation = selectedLocationFilter == "All" ||
                                        request.destination?.equals(selectedLocationFilter, ignoreCase = true) == true

                                    // Apply transport filter
                                    val matchesTransport = selectedTransportFilter == "All" ||
                                        request.modeOfTransport?.equals(selectedTransportFilter, ignoreCase = true) == true

                                    // Debug logging for transport filter
                                    if (selectedCategoryFilter == "Mode of Transport" && selectedTransportFilter != "All") {
                                        Log.d("TravelApprovalsScreen", "Request ${request.id}: modeOfTransport='${request.modeOfTransport}', selectedTransportFilter='$selectedTransportFilter', matchesTransport=$matchesTransport")
                                    }

                                    // Apply category filter (this is a meta-filter that affects what to show)
                                    val matchesCategory = when (selectedCategoryFilter) {
                                        "All" -> true
                                        "Status" -> true // Show all when filtering by status
                                        "Location" -> matchesLocation // Filter by destination when location category is selected
                                        "Mode of Transport" -> matchesTransport // Filter by transport mode when transport category is selected
                                        else -> true
                                    }

                                    // Apply date filter with proper boundary handling
                                    val matchesDate = when (selectedDateFilter) {
                                        "All" -> true
                                        "1 Week" -> {
                                            val oneWeekAgo = Calendar.getInstance().apply {
                                                add(Calendar.WEEK_OF_YEAR, -1)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }.time
                                            !request.createdDate.before(oneWeekAgo)
                                        }
                                        "1 Month" -> {
                                            val oneMonthAgo = Calendar.getInstance().apply {
                                                add(Calendar.MONTH, -1)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }.time
                                            !request.createdDate.before(oneMonthAgo)
                                        }
                                        "Date Range" -> {
                                            try {
                                                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                val fromDateParsed = formatter.parse(fromDate)
                                                val toDateParsed = formatter.parse(toDate)

                                                if (fromDateParsed != null && toDateParsed != null) {
                                                    // Normalize dates to start of day for proper comparison
                                                    val fromCal = Calendar.getInstance().apply {
                                                        time = fromDateParsed
                                                        set(Calendar.HOUR_OF_DAY, 0)
                                                        set(Calendar.MINUTE, 0)
                                                        set(Calendar.SECOND, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    val toCal = Calendar.getInstance().apply {
                                                        time = toDateParsed
                                                        set(Calendar.HOUR_OF_DAY, 23)
                                                        set(Calendar.MINUTE, 59)
                                                        set(Calendar.SECOND, 59)
                                                        set(Calendar.MILLISECOND, 999)
                                                    }

                                                    !request.createdDate.before(fromCal.time) && !request.createdDate.after(toCal.time)
                                                } else {
                                                    true
                                                }
                                            } catch (e: Exception) {
                                                Log.e("TravelApprovalsScreen", "Date parsing error: ${e.message}")
                                                true // If date parsing fails, don't filter
                                            }
                                        }
                                        else -> true
                                    }

                                    // Combine all filters based on selected category
                                    val finalResult = when (selectedCategoryFilter) {
                                        "All" -> matchesSearch && matchesDate // No category-specific filter when "All" is selected
                                        "Status" -> matchesSearch && matchesStatus && matchesDate
                                        "Location" -> matchesSearch && matchesLocation && matchesDate
                                        "Mode of Transport" -> matchesSearch && matchesTransport && matchesDate
                                        else -> matchesSearch && matchesDate
                                    }

                                    finalResult
                                }.also { filteredList ->
                                    Log.d("TravelApprovalsScreen", "Filtered to ${filteredList.size} requests")
                                }
                            }

                            if (filteredRequests.isEmpty()) {
                                // Empty state
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (state.approvalRequests.isEmpty()) {
                                            "No travel requests to approve"
                                        } else {
                                            "No requests match your filters"
                                        },
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            } else {
                                // Show list of filtered approval requests
                                LazyColumn(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    items(filteredRequests) { request ->
                                        ApprovalRequestCard(
                                            request = request,
                                            onApprove = {
                                                // Set shared controller instance before navigating
                                                TravelApproveActivity.sharedTravelController = controller

                                                // Navigate to dedicated approval screen instead of calling API directly
                                                val intent =
                                                    Intent(
                                                        context,
                                                        TravelApproveActivity::class.java,
                                                    ).apply {
                                                        putExtra("travel_request", Gson().toJson(request))
                                                    }
                                                context.startActivity(intent)
                                            },
                                            onReject = {
                                                // Set shared controller instance before navigating
                                                TravelRejectActivity.sharedTravelController = controller

                                                // Navigate to dedicated rejection screen instead of direct API call
                                                val intent =
                                                    Intent(
                                                        context,
                                                        TravelRejectActivity::class.java,
                                                    ).apply {
                                                        putExtra("travel_request", Gson().toJson(request))
                                                    }
                                                context.startActivity(intent)
                                            },
                                            onClick = {
                                                // Only navigate to detail screen for non-pending requests
                                                if (request.status != com.archeGlobal.one.model.TravelStatus.PENDING) {
                                                    controller.navigateToTravelApprovalDetail(request)
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        is TravelController.TravelApprovalsState.Error -> {
                            // Error state
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Error loading travel approvals",
                                        color = Color.Red,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.message,
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier =
                                            Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PrimaryRed)
                                                .clickable { controller.loadTravelApprovals() }
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = "Retry",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Calendar Overlay
                if (showCalendar) {
                    CalendarOverlay(
                        currentMonth = currentMonth,
                        onMonthChange = { currentMonth = it },
                        onDateSelected = { selectedDate ->
                            // Handle date selection
                            showCalendar = false
                        },
                        onDismiss = { showCalendar = false }
                    )
                }

                // Date Picker Dialog
                if (showDatePicker) {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val formattedDate = formatter.format(selectedCalendar.time)

                            if (isFromDatePicker) {
                                fromDate = formattedDate
                            } else {
                                toDate = formattedDate
                            }
                            showDatePicker = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            }
        }
    } // Close FontScaleAdjusted block
}

@Composable
fun ApprovalRequestCard(
    request: TravelRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF6F4EE),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Header with ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "#${request.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )

                // Status Badge matching the image design
                TravelStatusBadgeComponent(status = request.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Light divider line below ID/status section
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Employee and Project Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.person_3x),
                    contentDescription = "Employee",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Employee",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.employeeName ?: "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.folder_3x),
                    contentDescription = "Project",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Project",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.project,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mode of Transport
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.modeoft),
                    contentDescription = "Mode of Transport",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Mode of Transport",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.modeOfTransport ?: "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Show different details based on mode of transport
            if (request.modeOfTransport?.lowercase() == "cab") {
                // Show cab-specific fields
                CabTripDetails(travelRequest = request)
            } else {
                // Show trip details for non-cab bookings
                if (request.isMultiDestination()) {
                    MultiDestinationTripDetails(
                        travelRequest = request,
                    )
                } else {
                    SingleDestinationTripDetails(
                        travelRequest = request,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Created date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_3x),
                    contentDescription = "Created",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Created",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(request.createdDate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            // Show action buttons only for pending requests
            if (request.status == com.archeGlobal.one.model.TravelStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))

                // Light divider line above approve/reject buttons
                Divider(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Approve button
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(color = Color(0xFF4CAF50)) // Green color
                                .padding(vertical = 12.dp)
                                .clickable(onClick = onApprove),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Approve",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                        )
                    }

                    // Reject button
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(color = PrimaryRed)
                                .padding(vertical = 12.dp)
                                .clickable(onClick = onReject),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Reject",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SingleDestinationTripDetails(travelRequest: TravelRequest) {
    Column {
        // Get destinations for separate display
        val destinations = travelRequest.getAllDestinations()
        val destination = if (destinations.isNotEmpty()) destinations[0] else null

        // Origin City
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mappin_and_ellipse),
                contentDescription = "Origin City",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Origin City",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = destination?.originCity?.takeIf { it.isNotEmpty() } ?: "NA",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Destination City
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mappin_and_ellipse),
                contentDescription = "Destination City",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Destination City",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = destination?.destinationCity ?: travelRequest.destination,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Travel Dates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.airplane_departure),
                contentDescription = "Travel Dates",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Travel Dates",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${destination?.departureDate ?: travelRequest.departureDate ?: "N/A"} - ${destination?.arrivalDate ?: travelRequest.arrivalDate ?: "N/A"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }
    }
}

@Composable
fun MultiDestinationTripDetails(travelRequest: TravelRequest) {
    val destinations = travelRequest.getAllDestinations()

    destinations.forEachIndexed { index, destination ->
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text(
                text = "Trip ${index + 1}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            // Origin City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Origin City",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Origin City",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = destination.originCity?.takeIf { it.isNotEmpty() } ?: "NA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Destination City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Destination City",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Destination City",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = destination.destinationCity,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Travel Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.airplane_departure),
                    contentDescription = "Travel Dates",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Travel Dates",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${destination.departureDate} - ${destination.arrivalDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            if (index < destinations.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CabTripDetails(travelRequest: TravelRequest) {
    Column {
        // Travel Type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.traveltype),
                contentDescription = "Travel Type",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Travel Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (travelRequest.travelType.isNullOrBlank()) "N/A" else travelRequest.travelType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Cab Type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cabtype),
                contentDescription = "Cab Type",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Cab Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (travelRequest.cabType.isNullOrBlank()) "N/A" else travelRequest.cabType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.duration),
                contentDescription = "Duration",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Duration",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (travelRequest.duration.isNullOrBlank()) "N/A" else travelRequest.duration,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Pickup Locations
        val pickupLocations = travelRequest.pickupLocations
        if (pickupLocations != null && pickupLocations.isNotEmpty() && pickupLocations.any { it.isNotBlank() }) {
            pickupLocations.forEachIndexed { index, location ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pickupl),
                        contentDescription = "Pickup Location ${index + 1}",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Pickup Location ${index + 1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (location.isBlank()) "N/A" else location,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black,
                    )
                }
                if (index < pickupLocations.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pickupl),
                    contentDescription = "Pickup Location 1",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Pickup Location 1",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Drop Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.dropl),
                contentDescription = "Drop Location",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Drop Location",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (travelRequest.dropLocation.isNullOrBlank()) "N/A" else travelRequest.dropLocation,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Travel Dates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar_3x),
                contentDescription = "Travel Date",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Travel Date",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (travelRequest.travelDate.isNullOrBlank()) "N/A" else travelRequest.travelDate,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
            )
        }
    }
}

@Composable
fun TravelStatusBadgeComponent(status: com.archeGlobal.one.model.TravelStatus) {
    val (backgroundColor, textColor, text) =
        when (status) {
            com.archeGlobal.one.model.TravelStatus.APPROVED -> Triple(Color(0xFFD4EDDA), Color(0xFF28A745), "Approved") // Lighter green
            com.archeGlobal.one.model.TravelStatus.REJECTED -> Triple(Color(0xFFF8D7DA), Color(0xFFDC3545), "Rejected") // Lighter red
            com.archeGlobal.one.model.TravelStatus.PENDING -> Triple(Color(0xFFFFF3CD), Color(0xFFFF9800), "Pending")
            com.archeGlobal.one.model.TravelStatus.CANCELLED -> Triple(Color(0xFFF8D7DA), Color(0xFFDC3545), "Cancelled") // Same as rejected
        }

    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp,
    ) {
        Text(
            text = "Status: $text",
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun DetailItem(
    iconRes: ImageVector,
    label: String,
    value: String?,
) {
    if (value == null) return
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = iconRes,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

// Shared composables for date filtering
@Composable
fun DateDisplayCard(
    label: String,
    date: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GraphikFontFamily,
                color = Color.Black
            )
            Text(
                text = date,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = PrimaryRed
            )
        }
    }
}

@Composable
fun CalendarOverlay(
    currentMonth: Calendar,
    onMonthChange: (Calendar) -> Unit,
    onDateSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(350.dp)
                .clickable { /* Prevent dismiss when clicking calendar */ },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color(0xFF424242),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, -1)
                            }
                            onMonthChange(newMonth)
                        }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = PrimaryRed
                        )
                    }

                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GraphikFontFamily,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            val newMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, 1)
                            }
                            onMonthChange(newMonth)
                        }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = PrimaryRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar days grid
                val firstDayOfMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
                val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

                for (week in 0..5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            val dayNumber = week * 7 + dayOfWeek - startDayOfWeek + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable(enabled = dayNumber in 1..daysInMonth) {
                                        if (dayNumber in 1..daysInMonth) {
                                            onDateSelected(dayNumber)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val isSelected = dayNumber == 26 // Highlight selected date
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isSelected) PrimaryRed else Color.Transparent,
                                                RoundedCornerShape(16.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 14.sp,
                                            fontFamily = GraphikFontFamily,
                                            color = if (isSelected) Color.White else Color.White
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
}
