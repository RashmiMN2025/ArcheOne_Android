package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.MeetingRoomController
import com.archeGlobal.one.model.MeetingRoom
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.network.BookingRequest
import com.archeGlobal.one.network.BookingResponse
import com.archeGlobal.one.network.RetrofitClient
import retrofit2.Response
import androidx.compose.runtime.LaunchedEffect
import com.archeGlobal.one.utils.UserDataManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRoomScreen(
    controller: MeetingRoomController,
    room: MeetingRoom,
    location: String,
    date: String,
    fromTime: String,
    toTime: String,
    noOfAttendees: String,
    meetingType: String,
    startDate: String,
    endDate: String,
    onBackPressed: () -> Unit,
    onSubmit: () -> Unit = {},
) {
    val context = LocalContext.current  // Added for Toast
    val userDataManager = remember { UserDataManager.getInstance(context) }

    var clientName by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var meetingSubject by remember { mutableStateOf("") }
    var businessJustification by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var emailList by remember { mutableStateOf(listOf<String>()) }
    var archeAttendees by remember { mutableStateOf("") }
    var archeEmailList by remember { mutableStateOf(listOf<String>()) }
    var showAttendeesDropdown by remember { mutableStateOf(false) }
    var isSearchFieldFocused by remember { mutableStateOf(false) }

//    var meetingExtensionRequired by remember { mutableStateOf(false) }
    var refreshmentRequired by remember { mutableStateOf(false) }
    var additionalRequests by remember { mutableStateOf(false) }

    // States for conditional fields
    var extensionDuration by remember { mutableStateOf("") }
    var showExtensionDropdown by remember { mutableStateOf(false) }
    var refreshmentDetails by remember { mutableStateOf("") }
    var additionalDetails by remember { mutableStateOf("") }

    // Dropdown options for extension duration
    val extensionOptions = listOf("30 minutes", "1 hour", "2 hours", "Other")

    // Collect suggested users from controller
    val suggestedUsers by controller.suggestedUsers.collectAsState()
    val isLoadingSuggestions by controller.isLoadingSuggestions.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()

    // Coroutine scope for launching API calls
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val focusRequester = remember { FocusRequester() }

    var hostEmail by remember { mutableStateOf("") }
    var lineManagerEmail by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        var userData = userDataManager.getUserData()
        hostEmail = userData?.email ?: ""
        lineManagerEmail = userData?.userDetails?.reporting_manager_mail ?: ""
        userName = userData?.name ?: ""
    }

    // Dismiss dropdown when clicking outside
    val dismissDropdown = {
        showAttendeesDropdown = false
        isSearchFieldFocused = false
        focusRequester.freeFocus()
    }

    BackHandler {
        onBackPressed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .clickable(
                enabled = showAttendeesDropdown,
                onClick = { dismissDropdown() }
            )
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
                                        WelcomeBackgroundTop,
                                        WelcomeBackgroundMiddle,
                                        WelcomeBackgroundBottom,
                                    ),
                            ),
                    ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Header
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "MeetSpace",
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp)) // Balance the navigation icon
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )

                // Content
                Card(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                    ) {
                        // Room Image
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                            shape = RectangleShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                // Placeholder image
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = room.imageUrl,
                                        placeholder = painterResource(id = R.drawable.header_home)
                                    ),
                                    contentDescription = "${room.name} Meeting Room",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )

                                // Dark overlay for text readability
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize(),
                                )
                            }
                        }

                        Column(
                            modifier =
                                Modifier
                                    .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Text(
                                text = "Room Details",
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    // Room Name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.mroomname),
                                            contentDescription = "Room",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp),
                                        )
                                        Text(
                                            text = "Name: ${room.name}",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray,
                                        )
                                    }

                                    // Room Type
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.mroomtype),
                                            contentDescription = "Type",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp),
                                        )
                                        Text(
                                            text = "Type: ${room.room_type}",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray,
                                        )
                                    }

                                    // Equipment
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.equipment),
                                            contentDescription = "Equipment",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp),
                                        )
                                        Text(
                                            text = "Equipment: ${room.equipment}",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray,
                                        )
                                    }

                                    // Capacity
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.capacity),
                                            contentDescription = "Capacity",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp),
                                        )
                                        Text(
                                            text = "Capacity: ${room.capacity} Seats",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray,
                                        )
                                    }

                                    // Facilities Row
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                    ) {
                                        // Helpdesk
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(50.dp)
                                                        .background(Color(0xFFF6F4EE), CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.meethelpdesk),
                                                    contentDescription = "Helpdesk",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Helpdesk",
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }

                                        Divider(
                                            color = Color.LightGray,
                                            modifier =
                                                Modifier
                                                    .width(1.dp)
                                                    .height(50.dp),
                                        )

                                        // Pantry
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(50.dp)
                                                        .background(Color(0xFFF6F4EE), CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.panatry),
                                                    contentDescription = "Pantry",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Pantry",
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }

                                        Divider(
                                            color = Color.LightGray,
                                            modifier =
                                                Modifier
                                                    .width(1.dp)
                                                    .height(50.dp),
                                        )

                                        // Facility
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(50.dp)
                                                        .background(Color(0xFFF6F4EE), CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.facility),
                                                    contentDescription = "Facility",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Facility",
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Booking Details Section
                            Text(
                                text = "Booking Details",
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )

                            // Booking Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        // User info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.username),
                                                contentDescription = "User",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = userName,
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }

                                        // Location info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.meetroomlocation),
                                                contentDescription = "Location",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = "$location",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        // Date info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.meetcalender),
                                                contentDescription = "Date",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = "$date",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }

                                        // Time info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.meettime),
                                                contentDescription = "Time",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = "$fromTime \n$toTime",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray,
                                            )
                                        }
                                    }
                                }
                            }

                            if (meetingType == "Meeting with Guest") {
                                // Client Name
                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    placeholder = {
                                        Text(
                                            "Client Name",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Project Name
                                OutlinedTextField(
                                    value = projectName,
                                    onValueChange = { projectName = it },
                                    placeholder = {
                                        Text(
                                            "Project Name",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Meeting Subject
                                OutlinedTextField(
                                    value = meetingSubject,
                                    onValueChange = { meetingSubject = it },
                                    placeholder = {
                                        Text(
                                            "Meeting Subject",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Business Justification
                                OutlinedTextField(
                                value = businessJustification,
                                onValueChange = { businessJustification = it },
                                placeholder = {
                                    Text(
                                        "Business Justification",
                                        color = Color.LightGray,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                maxLines = 4,
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                    ),
                                shape = RoundedCornerShape(12.dp),
                            )

                                // Search for Attendees
                                Column {
                                    ExposedDropdownMenuBox(
                                        expanded = showAttendeesDropdown,
                                        onExpandedChange = { newExpanded ->
                                            if (isSearchFieldFocused) {
                                                showAttendeesDropdown = true // Keep open if focused
                                            } else {
                                                showAttendeesDropdown = newExpanded
                                            }
                                        }
                                    ) {
                                        OutlinedTextField(
                                            value = archeAttendees,
                                            onValueChange = { newValue ->
                                                archeAttendees = newValue
                                                // Cancel previous job to debounce
                                                searchJob?.cancel()
                                                searchJob = coroutineScope.launch {
                                                    delay(300) // Debounce for 300ms
                                                    controller.fetchSuggestedUsers(newValue)
                                                }
                                                // Keep dropdown open if focused or input is non-blank
                                                showAttendeesDropdown = isSearchFieldFocused || newValue.isNotBlank()
                                            },
                                            placeholder = {
                                                Text(
                                                    "Search for Attendees to Add",
                                                    color = Color.LightGray,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    tint = Color.Gray
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor()
                                                .focusRequester(focusRequester)
                                                .onFocusChanged { focusState ->
                                                    isSearchFieldFocused = focusState.isFocused
                                                    showAttendeesDropdown = focusState.isFocused || archeAttendees.isNotBlank()
                                                }
                                                .onKeyEvent { keyEvent ->
                                                    if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp) {
                                                        // Allow backspace to delete without closing dropdown
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.LightGray,
                                                cursorColor = Color.Gray,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = showAttendeesDropdown && (suggestedUsers.isNotEmpty() || isLoadingSuggestions),
                                            onDismissRequest = { dismissDropdown() },
                                            modifier = Modifier
                                                .exposedDropdownSize()
                                                .background(Color.White),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (isLoadingSuggestions) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = Color(0xFFDD3825),
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                            } else if (suggestedUsers.isNotEmpty()) {
                                                suggestedUsers.forEachIndexed { index, email ->
                                                    if (email != null) { // Skip null emails
                                                        Column {
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = email,
                                                                        color = Color.Black,
                                                                        fontFamily = GraphikFontFamily,
                                                                        fontWeight = FontWeight.Normal
                                                                    )
                                                                },
                                                                onClick = {
                                                                    if (email.isNotBlank() && email !in archeEmailList) {
                                                                        archeEmailList = archeEmailList + email
                                                                        archeAttendees = ""
                                                                    }
                                                                    showAttendeesDropdown = false
                                                                    dismissDropdown()
                                                                }
                                                            )
                                                            if (index < suggestedUsers.size - 1) {
                                                                Divider(
                                                                    color = Color.LightGray,
                                                                    thickness = 1.dp,
                                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Display Added Arche Emails
                                    if (archeEmailList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Arche Attendees",
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 0.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            archeEmailList.forEachIndexed { index, email ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(),
                                                    shape = RoundedCornerShape(20.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(2.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp, horizontal = 16.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = email,
                                                            fontSize = 14.sp,
                                                            fontFamily = GraphikFontFamily,
                                                            fontWeight = FontWeight.Normal,
                                                            color = Color.Black,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                archeEmailList = archeEmailList.filterIndexed { i, _ -> i != index }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .background(
                                                                        Color(0xFFDD3825),
                                                                        CircleShape
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Remove Email",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Enter Guest Email to Add
                                Column {
                                    OutlinedTextField(
                                        value = guestEmail,
                                        onValueChange = { guestEmail = it },
                                        placeholder = {
                                            Text(
                                                "Enter Guest Email to Add",
                                                color = Color.LightGray,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            Button(
                                                onClick = {
                                                    if (guestEmail.isNotBlank()) {
                                                        emailList = emailList + guestEmail
                                                        guestEmail = ""
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFDD3825),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .height(36.dp)
                                            ) {
                                                Text(
                                                    text = "Add",
                                                    fontSize = 16.sp,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color.LightGray,
                                            cursorColor = Color.Gray,
                                            unfocusedTextColor = Color.Black,
                                            focusedTextColor = Color.Black,
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Display Added Emails
                                    if (emailList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Guest Attendees",
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 0.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing between emails
                                        ) {
                                            emailList.forEachIndexed { index, email ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(),
                                                    shape = RoundedCornerShape(20.dp), // Smaller radius for individual emails
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(2.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp, horizontal = 16.dp), // Reduced vertical padding
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = email,
                                                            fontSize = 14.sp,
                                                            fontFamily = GraphikFontFamily,
                                                            fontWeight = FontWeight.Normal,
                                                            color = Color.Black,
                                                            modifier = Modifier.weight(1f) // Ensure text takes available space
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                emailList = emailList.filterIndexed { i, _ -> i != index }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .background(
                                                                        Color(0xFFDD3825),
                                                                        CircleShape
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Remove Email",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Meeting Subject
                                OutlinedTextField(
                                    value = meetingSubject,
                                    onValueChange = { meetingSubject = it },
                                    placeholder = {
                                        Text(
                                            "Meeting Subject",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Search for Attendees
                                Column {
                                    ExposedDropdownMenuBox(
                                        expanded = showAttendeesDropdown,
                                        onExpandedChange = { newExpanded ->
                                            if (isSearchFieldFocused) {
                                                showAttendeesDropdown = true // Keep open if focused
                                            } else {
                                                showAttendeesDropdown = newExpanded
                                            }
                                        }
                                    ) {
                                        OutlinedTextField(
                                            value = archeAttendees,
                                            onValueChange = { newValue ->
                                                archeAttendees = newValue
                                                // Cancel previous job to debounce
                                                searchJob?.cancel()
                                                searchJob = coroutineScope.launch {
                                                    delay(300) // Debounce for 300ms
                                                    controller.fetchSuggestedUsers(newValue)
                                                }
                                                // Keep dropdown open if focused or input is non-blank
                                                showAttendeesDropdown = isSearchFieldFocused || newValue.isNotBlank()
                                            },
                                            placeholder = {
                                                Text(
                                                    "Search for Attendees to Add",
                                                    color = Color.LightGray,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    tint = Color.Gray
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor()
                                                .focusRequester(focusRequester)
                                                .onFocusChanged { focusState ->
                                                    isSearchFieldFocused = focusState.isFocused
                                                    showAttendeesDropdown = focusState.isFocused || archeAttendees.isNotBlank()
                                                }
                                                .onKeyEvent { keyEvent ->
                                                    if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp) {
                                                        // Allow backspace to delete without closing dropdown
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.LightGray,
                                                cursorColor = Color.Gray,
                                                unfocusedTextColor = Color.Black,
                                                focusedTextColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = showAttendeesDropdown && (suggestedUsers.isNotEmpty() || isLoadingSuggestions),
                                            onDismissRequest = { dismissDropdown() },
                                            modifier = Modifier
                                                .exposedDropdownSize()
                                                .background(Color.White),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (isLoadingSuggestions) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = Color(0xFFDD3825),
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                            } else if (suggestedUsers.isNotEmpty()) {
                                                suggestedUsers.forEachIndexed { index, email ->
                                                    if (email != null) { // Skip null emails
                                                        Column {
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = email,
                                                                        color = Color.Black,
                                                                        fontFamily = GraphikFontFamily,
                                                                        fontWeight = FontWeight.Normal
                                                                    )
                                                                },
                                                                onClick = {
                                                                    if (email.isNotBlank() && email !in archeEmailList) {
                                                                        archeEmailList = archeEmailList + email
                                                                        archeAttendees = ""
                                                                    }
                                                                    showAttendeesDropdown = false
                                                                    dismissDropdown()
                                                                }
                                                            )
                                                            if (index < suggestedUsers.size - 1) {
                                                                Divider(
                                                                    color = Color.LightGray,
                                                                    thickness = 1.dp,
                                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Display Added Arche Emails
                                    if (archeEmailList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Arche Attendees",
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 0.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            archeEmailList.forEachIndexed { index, email ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(),
                                                    shape = RoundedCornerShape(20.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(2.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp, horizontal = 8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = email,
                                                            fontSize = 14.sp,
                                                            fontFamily = GraphikFontFamily,
                                                            fontWeight = FontWeight.Normal,
                                                            color = Color.Black,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                archeEmailList = archeEmailList.filterIndexed { i, _ -> i != index }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .background(
                                                                        Color(0xFFDD3825),
                                                                        CircleShape
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Remove Email",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Business Justification
                                OutlinedTextField(
                                    value = businessJustification,
                                    onValueChange = { businessJustification = it },
                                    placeholder = {
                                        Text(
                                            "Business Justification",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        ) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // Toggle Options
                        Column(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        )
                        {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Refreshment Required?",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                )
                                Switch(
                                    checked = refreshmentRequired,
                                    onCheckedChange = { refreshmentRequired = it },
                                    colors =
                                        SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFDD3825),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color.LightGray,
                                        ),
                                )
                            }
                            if (refreshmentRequired) {
                                OutlinedTextField(
                                    value = refreshmentDetails,
                                    onValueChange = { refreshmentDetails = it },
                                    placeholder = {
                                        Text(
                                            "Refreshment Details (e.g.,\nCoffee, Snacks)",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        ) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Additional Requests?",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                )
                                Switch(
                                    checked = additionalRequests,
                                    onCheckedChange = { additionalRequests = it },
                                    colors =
                                        SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFDD3825),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color.LightGray,
                                        ),
                                )
                            }
                            if (additionalRequests) {
                                OutlinedTextField(
                                    value = additionalDetails,
                                    onValueChange = { additionalDetails = it },
                                    placeholder = {
                                        Text(
                                            "Additional Requests (e.g.,\nSecurity, Parking)",
                                            color = Color.LightGray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                        ) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    val isValid = when (meetingType) {
                                        "Meeting with Guest" -> {
                                            clientName.isNotBlank() &&
                                                    projectName.isNotBlank() &&
                                                    meetingSubject.isNotBlank() &&
                                                    businessJustification.isNotBlank() &&
                                                    emailList.isNotEmpty() &&
                                                    archeEmailList.isNotEmpty() &&
                                                    (!refreshmentRequired || refreshmentDetails.isNotBlank()) &&
                                                    (!additionalRequests || additionalDetails.isNotBlank())
                                        }
                                        else -> { // Internal Meeting
                                            meetingSubject.isNotBlank() &&
                                                    archeEmailList.isNotEmpty() &&
                                                    businessJustification.isNotBlank() &&
                                                    (!refreshmentRequired || refreshmentDetails.isNotBlank()) &&
                                                    (!additionalRequests || additionalDetails.isNotBlank())
                                        }
                                    }

                                    if (!isValid) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Please fill in all required fields",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        val refreshmentReq =
                                            if (refreshmentRequired) "yes" else "no"

                                        var additional = additionalDetails
                                        if (refreshmentRequired && refreshmentDetails.isNotBlank()) {
                                            additional =
                                                "Refreshment: $refreshmentDetails\n$additional"
                                        }

                                        val lineManager =
                                            if (meetingType == "Meeting with Guest") lineManagerEmail else ""
                                        val client =
                                            if (meetingType == "Meeting with Guest") clientName else ""
                                        val project =
                                            if (meetingType == "Meeting with Guest") projectName else ""
                                        val business =
                                            if (meetingType == "Meeting with Guest") businessJustification else ""
                                        val guestAtt =
                                            if (meetingType == "Meeting with Guest") emailList else emptyList()

                                        val apiMeetingType = when (meetingType) {
                                            "Meeting with Guest" -> "external"
                                            "Internal Meeting" -> "internal"
                                            else -> meetingType // Fallback to original if unexpected
                                        }

                                        val request = BookingRequest(
                                            room_id = room.room_id,
                                            room_name = room.name,
                                            room_location = location,
                                            host_email = hostEmail,
                                            meeting_type = apiMeetingType,
                                            meeting_subject = meetingSubject,
                                            meeting_starttime = startDate,
                                            meeting_endtime = endDate,
                                            arche_attendees = archeEmailList,
                                            guest_attendees = guestAtt,
                                            business_justification = business,
                                            client_name = client,
                                            project_name = project,
//                                        meeting_extension = meetingExtension,
                                            refreshment_required = refreshmentReq,
                                            additional_request = additional,
                                            line_manager_email = lineManager
                                        )

                                        coroutineScope.launch {
                                            try {
                                                val response: Response<BookingResponse> =
                                                    RetrofitClient.apiService.requestBooking(request)
                                                if (response.isSuccessful) {
                                                    val body = response.body()
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        body?.message ?: "Booking successful",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                    // Clear all input fields
                                                    clientName = ""
                                                    projectName = ""
                                                    meetingSubject = ""
                                                    businessJustification = ""
                                                    guestEmail = ""
                                                    emailList = emptyList()
                                                    archeAttendees = ""
                                                    archeEmailList = emptyList()
                                                    showAttendeesDropdown = false
                                                    isSearchFieldFocused = false
//                                                meetingExtensionRequired = false
                                                    refreshmentRequired = false
                                                    additionalRequests = false
                                                    extensionDuration = ""
                                                    showExtensionDropdown = false
                                                    refreshmentDetails = ""
                                                    additionalDetails = ""
                                                    // Optional: Trigger onSubmit or navigate back
                                                    onSubmit()
                                                    // onBackPressed() // Uncomment if you want to navigate back
                                                } else {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Error: ${response.message()}",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Network error: ${e.message}",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "Submit",
                                    fontSize = 18.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = Color(0xFFDD3825),
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}
