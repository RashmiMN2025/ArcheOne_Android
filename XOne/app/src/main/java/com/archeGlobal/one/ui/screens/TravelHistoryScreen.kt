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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.controller.TravelController.TravelHistoryState
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.DateFormatter
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelHistoryScreen(controller: TravelController) {
    // Get context and font adjustment for consistent font scaling
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }

    val state = controller.travelHistoryState

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
                                            WelcomeBackgroundTop, // Light Beige/Grey (0xFFE0DCD1)
                                            WelcomeBackgroundMiddle, // Light Grey (0xFFC8C8CA)
                                            WelcomeBackgroundBottom, // Dark Grey (0xFF474749)
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
                                text = "Travel History",
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
                                    text = "Search by ID or Project",
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
                                    // Filtering is already reactive via remember
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
                                        val locations = remember(state) {
                                            if (state is TravelHistoryState.Success) {
                                                listOf("All") + state.historyItems
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
                                        val transportModes = remember(state) {
                                            if (state is TravelHistoryState.Success) {
                                                val modes = state.historyItems
                                                    .mapNotNull { it.modeOfTransport }
                                                    .distinct()
                                                    .sorted()
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
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                    ) {
                        when (val currentState = state) {
                            is TravelHistoryState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }

                            is TravelHistoryState.Success -> {
                                // Apply filters to the history items
                                val filteredRequests = remember(
                                    currentState.historyItems,
                                    searchQuery,
                                    selectedStatusFilter,
                                    selectedLocationFilter,
                                    selectedTransportFilter,
                                    selectedCategoryFilter,
                                    selectedDateFilter,
                                    fromDate,
                                    toDate
                                ) {
                                    currentState.historyItems.filter { request ->
                                        // Apply search filter
                                        val matchesSearch = searchQuery.isEmpty() ||
                                            request.id.contains(searchQuery, ignoreCase = true) ||
                                            request.project.contains(searchQuery, ignoreCase = true) ||
                                            request.employeeName?.contains(searchQuery, ignoreCase = true) == true

                                        // Apply status filter
                                        val matchesStatus = selectedStatusFilter == "All" ||
                                            request.status.name.equals(selectedStatusFilter, ignoreCase = true)

                                        // Apply location filter (using userLocation field)
                                        val matchesLocation = selectedLocationFilter == "All" ||
                                            request.userLocation?.equals(selectedLocationFilter, ignoreCase = true) == true

                                        // Apply transport filter
                                        val matchesTransport = selectedTransportFilter == "All" ||
                                            request.modeOfTransport?.equals(selectedTransportFilter, ignoreCase = true) == true

                                        // Apply category filter
                                        val matchesCategory = when (selectedCategoryFilter) {
                                            "All" -> true
                                            "Status" -> true
                                            "Location" -> matchesLocation
                                            "Mode of Transport" -> matchesTransport
                                            else -> true
                                        }

                                        // Apply date filter
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
                                                    true
                                                }
                                            }
                                            else -> true
                                        }

                                        // Combine all filters based on selected category
                                        when (selectedCategoryFilter) {
                                            "All" -> matchesSearch && matchesDate
                                            "Status" -> matchesSearch && matchesStatus && matchesDate
                                            "Location" -> matchesSearch && matchesLocation && matchesDate
                                            "Mode of Transport" -> matchesSearch && matchesTransport && matchesDate
                                            else -> matchesSearch && matchesDate
                                        }
                                    }
                                }

                                if (filteredRequests.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (currentState.historyItems.isEmpty()) {
                                                "No travel history found"
                                            } else {
                                                "No requests match your filters"
                                            },
                                            fontFamily = GraphikFontFamily,
                                            color = Color.Gray,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                } else {
                                    TravelHistoryList(
                                        travelRequests = filteredRequests,
                                        onTravelRequestClick = { requestId ->
                                            controller.navigateToTravelDetails(requestId)
                                        },
                                    )
                                }
                            }

                            is TravelHistoryState.Error -> {
                                Column(
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(text = currentState.message, fontFamily = GraphikFontFamily)
                                    Button(
                                        onClick = { controller.loadCombinedTravelHistory() },
                                        modifier = Modifier.padding(top = 8.dp),
                                    ) {
                                        Text("Retry", fontFamily = GraphikFontFamily)
                                    }
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
    } // Close FontScaleAdjusted block
}

@Composable
fun TravelHistoryList(
    travelRequests: List<TravelRequest>,
    onTravelRequestClick: (String) -> Unit,
) {
    if (travelRequests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "No items found", fontFamily = GraphikFontFamily)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(travelRequests) { request ->
                TravelRequestCard(
                    travelRequest = request,
                    onClick = { onTravelRequestClick(request.id) },
                )
            }
        }
    }
}

@Composable
fun TravelRequestCard(
    travelRequest: TravelRequest,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = Color(0xFFF6F4EE),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
        ) {
            // ID and Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${travelRequest.id}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                )
                StatusTag(status = travelRequest.status)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth())

            // Employee
            DetailItem(
                icon = R.drawable.person_3x,
                label = "Employee",
                value = travelRequest.employeeName ?: "N/A",
            )

            // Project
            DetailItem(
                icon = R.drawable.folder_3x,
                label = "Project",
                value = travelRequest.project,
            )

            // Mode of Transport
            DetailItem(
                icon = R.drawable.modeoft,
                label = "Mode of Transport",
                value = travelRequest.modeOfTransport ?: "N/A",
            )

            // Show trip details only for non-cab bookings
            if (travelRequest.modeOfTransport?.lowercase() != "cab") {
                // Handle single vs multi-destination display
                val destinations = travelRequest.getAllDestinations()

                if (destinations.isEmpty() || destinations.size == 1) {
                    // Single destination - show origin city and destination city separately
                    if (destinations.isNotEmpty()) {
                        val destination = destinations[0]

                        // Show origin city if available
                        if (!destination.originCity.isNullOrEmpty()) {
                            DetailItem(
                                icon = R.drawable.mappin_and_ellipse,
                                label = "Origin City",
                                value = destination.originCity,
                            )
                        }

                        // Show destination city
                        DetailItem(
                            icon = R.drawable.mappin_and_ellipse,
                            label = "Destination City",
                            value = destination.destinationCity,
                        )

                        // Show travel dates for single destination
                        if (!destination.departureDate.isNullOrEmpty() && !destination.arrivalDate.isNullOrEmpty()) {
                            DetailItem(
                                icon = R.drawable.ic_calendar,
                                label = "Travel Dates",
                                value = DateFormatter.formatTravelDateRange(destination.departureDate, destination.arrivalDate),
                            )
                        }
                    } else {
                        // Fallback for cases without travel details
                        DetailItem(
                            icon = R.drawable.mappin_and_ellipse,
                            label = "Destination",
                            value = travelRequest.destination,
                        )
                    }
                } else {
                    // Multi-destination - show Trip 1, Trip 2, etc.
                    destinations.forEachIndexed { index, destination ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Trip ${index + 1}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )

                        // Show origin city if available
                        if (!destination.originCity.isNullOrEmpty()) {
                            DetailItem(
                                icon = R.drawable.mappin_and_ellipse,
                                label = "Origin City",
                                value = destination.originCity,
                            )
                        }

                        // Show destination city
                        DetailItem(
                            icon = R.drawable.mappin_and_ellipse,
                            label = "Destination City",
                            value = destination.destinationCity,
                        )

                        DetailItem(
                            icon = R.drawable.airplane_departure,
                            label = "Travel Dates",
                            value = DateFormatter.formatTravelDateRange(destination.departureDate, destination.arrivalDate),
                        )
                    }
                }

                // Approver - only show for non-cab bookings
                DetailItem(
                    icon = R.drawable.approver,
                    label = "Approver",
                    value = travelRequest.approver,
                )
            }

            // Add divider line before Created date
            Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())

            // Created date
            DetailItem(
                icon = R.drawable.ic_calendar,
                label = "Created",
                value = DateFormatter.formatDisplayDate(travelRequest.createdDate),
            )
        }
    }
}

@Composable
fun StatusTag(status: TravelStatus) {
    val (backgroundColor, textColor, text) =
        when (status) {
            TravelStatus.APPROVED -> Triple(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000), "Approved")
            TravelStatus.REJECTED -> Triple(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000), "Rejected")
            TravelStatus.PENDING -> Triple(Color(0xFFFFA500).copy(alpha = 0.15f), Color(0xFFFFA500), "Pending")
            TravelStatus.CANCELLED -> Triple(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000), "Cancelled")
        }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = "Status: $text",
            fontSize = 15.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun DetailItem(
    icon: Int,
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.Gray,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = GraphikFontFamily,
            color = Color.Black,
        )
    }
}
