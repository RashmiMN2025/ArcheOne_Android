package com.archeGlobal.one.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelAdminDashboardScreen(controller: TravelController) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

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
                                text = "Admin Travel View",
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

                    // Load admin dashboard data when screen is first shown and on resume
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer =
                            LifecycleEventObserver { _, event ->
                                if (event == Lifecycle.Event.ON_RESUME) {
                                    // Load admin dashboard data when screen resumes
                                    controller.loadTravelAdminDashboard()
                                }
                            }
                        lifecycleOwner.lifecycle.addObserver(observer)

                        // Initial load when screen is first created
                        controller.loadTravelAdminDashboard()

                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
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
                                .height(48.dp), // Reduce height
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
                                    modifier = Modifier.size(20.dp) // Slightly smaller icon
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
                                        // Get unique user locations from the current state
                                        val locations = remember(controller.travelApprovalsState) {
                                            val currentState = controller.travelApprovalsState
                                            if (currentState is TravelController.TravelApprovalsState.Success) {
                                                listOf("All") + currentState.approvalRequests
                                                    .mapNotNull { it.userLocation }
                                                    .filter { it.isNotBlank() }
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
                                                Log.d("TravelAdminDashboard", "Transport modes found: $modes")
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
                                Log.d("TravelAdminDashboard", "Filtering ${state.approvalRequests.size} requests with: search='$searchQuery', status='$selectedStatusFilter', location='$selectedLocationFilter', transport='$selectedTransportFilter', category='$selectedCategoryFilter', date='$selectedDateFilter'")
                                state.approvalRequests.filter { request ->
                                // Apply search filter
                                val matchesSearch = searchQuery.isEmpty() ||
                                    request.employeeName?.contains(searchQuery, ignoreCase = true) == true ||
                                    request.id.contains(searchQuery, ignoreCase = true)

                                // Apply status filter
                                val matchesStatus = selectedStatusFilter == "All" ||
                                    request.status.name.equals(selectedStatusFilter, ignoreCase = true)

                                // Apply location filter (using userLocation field)
                                val matchesLocation = selectedLocationFilter == "All" ||
                                    request.userLocation?.equals(selectedLocationFilter, ignoreCase = true) == true

                                // Apply transport filter
                                val matchesTransport = selectedTransportFilter == "All" ||
                                    request.modeOfTransport?.equals(selectedTransportFilter, ignoreCase = true) == true

                                // Debug logging for transport filter
                                if (selectedCategoryFilter == "Mode of Transport" && selectedTransportFilter != "All") {
                                    Log.d("TravelAdminDashboard", "Request ${request.id}: modeOfTransport='${request.modeOfTransport}', selectedTransportFilter='$selectedTransportFilter', matchesTransport=$matchesTransport")
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
                                            Log.e("TravelAdminDashboard", "Date parsing error: ${e.message}")
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
                                    Log.d("TravelAdminDashboard", "Filtered to ${filteredList.size} requests")
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
                                            "No travel requests available"
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
                                // Show list of filtered requests
                                LazyColumn(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp), // Add bottom padding for fixed button
                                ) {
                                    items(filteredRequests) { request ->
                                        AdminRequestCard(
                                            request = request,
                                            onClick = {
                                                // Navigate to detail screen for all requests (including pending)
                                                controller.navigateToTravelApprovalDetails(request.id)
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
                                        text = "Error loading travel requests",
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

            // Fixed Download Report Button at bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp) // Move button up slightly
                    .clip(RoundedCornerShape(20.dp)) // More rounded edges
                    .background(PrimaryRed)
                    .clickable {
                        // Calculate date_range and dates based on date filter
                        val (dateRangeParam, calculatedStartDate, calculatedEndDate) = when (selectedDateFilter) {
                            "All" -> Triple("all", null, null)
                            "1 Week" -> Triple("1week", null, null)
                            "1 Month" -> Triple("1month", null, null)
                            "Date Range" -> {
                                try {
                                    val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    val apiFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val fromDateParsed = displayFormatter.parse(fromDate)
                                    val toDateParsed = displayFormatter.parse(toDate)
                                    if (fromDateParsed != null && toDateParsed != null) {
                                        Triple(
                                            null,
                                            apiFormatter.format(fromDateParsed),
                                            apiFormatter.format(toDateParsed)
                                        )
                                    } else {
                                        Triple(null, null, null)
                                    }
                                } catch (e: Exception) {
                                    Triple(null, null, null)
                                }
                            }
                            else -> Triple(null, null, null)
                        }

                        // Determine which filter values to pass based on category
                        val locationFilter = if (selectedCategoryFilter == "Location" || selectedCategoryFilter == "All") {
                            selectedLocationFilter
                        } else null

                        val transportFilter = if (selectedCategoryFilter == "Mode of Transport" || selectedCategoryFilter == "All") {
                            selectedTransportFilter
                        } else null

                        val statusFilter = if (selectedCategoryFilter == "Status" || selectedCategoryFilter == "All") {
                            selectedStatusFilter
                        } else null

                        controller.downloadAdminReport(
                            dateRange = dateRangeParam,
                            startDate = calculatedStartDate,
                            endDate = calculatedEndDate,
                            userLocation = locationFilter,
                            modeOfTransport = transportFilter,
                            status = statusFilter
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Download Report",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } // Close FontScaleAdjusted block
}

@Composable
fun AdminRequestCard(
    request: TravelRequest,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        backgroundColor = Color.White,
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

                // Status Badge with same colors as TravelApprovalsScreen
                AdminTravelStatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Employee Info
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
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.employeeName ?: "John Doe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Project Info
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
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.project ?: "Project X",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode of Transport Info
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
                    fontWeight = FontWeight.Normal,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Created date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_3x),
                    contentDescription = "Created Date",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Created Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
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

        }
    }
}

@Composable
fun AdminTravelStatusBadge(status: com.archeGlobal.one.model.TravelStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        com.archeGlobal.one.model.TravelStatus.APPROVED -> Triple(Color(0xFFD4EDDA), Color(0xFF28A745), "Approved")
        com.archeGlobal.one.model.TravelStatus.REJECTED -> Triple(Color(0xFFF8D7DA), Color(0xFFDC3545), "Rejected")
        com.archeGlobal.one.model.TravelStatus.PENDING -> Triple(Color(0xFFFFF3CD), Color(0xFFFF9800), "Pending")
        com.archeGlobal.one.model.TravelStatus.CANCELLED -> Triple(Color(0xFFF8D7DA), Color(0xFFDC3545), "Cancelled")
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