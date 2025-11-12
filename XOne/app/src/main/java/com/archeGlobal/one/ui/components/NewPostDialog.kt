package com.archeGlobal.one.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostDialog(
    profilePicUrl: String? = null,
    userName: String = "User",
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: String) -> Unit,
    onHistoryClick: () -> Unit = {},
) {
    // Form type switcher state
    var formType by remember { mutableStateOf("Post") } // "Post" or "Event"

    // Fetched locations and departments
    var fetchedLocations by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchedDepartments by remember { mutableStateOf<List<String>>(emptyList()) }

    // Post form states
    var postType by remember { mutableStateOf("Planned") }
    var postSubject by remember { mutableStateOf("Select Post Subject") }
    var postPriority by remember { mutableStateOf("Select Post Priority") }
    var postGroup by remember { mutableStateOf("Employee-based") }
    var employeeSearchQuery by remember { mutableStateOf("") }
    var selectedEmployee by remember { mutableStateOf<com.archeGlobal.one.model.SuggestedUser?>(null) }
    var suggestedEmployees by remember { mutableStateOf<List<com.archeGlobal.one.model.SuggestedUser>>(emptyList()) }
    var isSearchingEmployees by remember { mutableStateOf(false) }
    var department by remember { mutableStateOf("Select Department") }
    var location by remember { mutableStateOf("Select Location") }
    var announcementDescription by remember { mutableStateOf("") }

    // Fetch locations and departments from API
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getLocationsAndDepartments()
            if (response.isSuccessful && response.body()?.status == 200) {
                val data = response.body()?.data
                fetchedLocations = data?.cities ?: emptyList()
                fetchedDepartments = data?.departments ?: emptyList()
            }
        } catch (e: Exception) {
            // Handle error silently, keep empty lists
        }
    }

    // Search employees function
    fun searchEmployees(query: String) {
        employeeSearchQuery = query
        if (query.length < 2) {
            suggestedEmployees = emptyList()
            return
        }

        isSearchingEmployees = true
        val call = RetrofitClient.apiService.suggestUsers(query)
        call.enqueue(object : retrofit2.Callback<List<com.archeGlobal.one.model.SuggestedUser>> {
            override fun onResponse(
                call: retrofit2.Call<List<com.archeGlobal.one.model.SuggestedUser>>,
                response: retrofit2.Response<List<com.archeGlobal.one.model.SuggestedUser>>
            ) {
                isSearchingEmployees = false
                if (response.isSuccessful) {
                    suggestedEmployees = response.body() ?: emptyList()
                }
            }

            override fun onFailure(
                call: retrofit2.Call<List<com.archeGlobal.one.model.SuggestedUser>>,
                t: Throwable
            ) {
                isSearchingEmployees = false
                suggestedEmployees = emptyList()
            }
        })
    }
    var postStartDate by remember { mutableStateOf("") }
    var postEndDate by remember { mutableStateOf("") }
    var startDurationDate by remember { mutableStateOf("") }
    var startDurationTime by remember { mutableStateOf("") }
    var endDurationDate by remember { mutableStateOf("") }
    var endDurationTime by remember { mutableStateOf("") }
    var supportChannelDetails by remember { mutableStateOf("") }
    var postImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showPostPreview by remember { mutableStateOf(false) }

    // Event form states
    var eventSubject by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventImageUri by remember { mutableStateOf<Uri?>(null) }
    var eventDate by remember { mutableStateOf("") }
    var eventStartDate by remember { mutableStateOf("") }
    var eventEndDate by remember { mutableStateOf("") }
    var showEventPreview by remember { mutableStateOf(false) }

    val postTypes = listOf("Planned", "Unplanned/Emergency")

    val plannedSubjects = listOf(
        "Housekeeping schedule",
        "Pest control or deep cleaning activities",
        "Pantry & cafeteria updates",
        "Air conditioning or lighting maintenance",
        "Fire drills or emergency activities",
        "Lost & found notifications",
        "Security protocol reminders",
        "Access restriction or badge issues",
        "Lift/escalator maintenance",
        "Parking space updates",
        "Delivery or courier notifications",
        "Clean desk policy reminders",
        "Power outage or generator testing",
        "Visitor on floor alerts",
        "Noise level reminders",
        "Seating arrangement changes"
    )

    val unplannedSubjects = listOf(
        "Air conditioning/lighting maintenance",
        "Pantry & cafeteria update",
        "Fire drills/emergency",
        "Lost & found",
        "Lift/escalator maintenance",
        "Delivery or courier notification",
        "Power outage/generator testing",
        "Noise level reminder"
    )

    val postSubjects = if (postType == "Planned") plannedSubjects else unplannedSubjects
    val postPriorities = listOf("High", "Medium", "Low")
    val postGroups = listOf("Employee-based", "Department-based", "Location-based", "All")

    // Reset post subject when post type changes
    LaunchedEffect(postType) {
        postSubject = "Select Post Subject"
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4EE))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (formType == "Post") "Create Post" else "Create Event",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(x = 24.dp) // Standard offset for proper centering
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clickable { onHistoryClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "History",
                            color = PrimaryRed,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp, // Matching DeskCartScreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp) // Matching DeskCartScreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Form Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { formType = "Post" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formType == "Post") PrimaryRed else Color.White,
                            contentColor = if (formType == "Post") Color.White else Color.Black,
                        ),
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                    ) {
                        Text(
                            text = "HeadsUp",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                        )
                    }

                    Button(
                        onClick = { formType = "Event" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formType == "Event") PrimaryRed else Color.White,
                            contentColor = if (formType == "Event") Color.White else Color.Black,
                        ),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp),
                    ) {
                        Text(
                            text = "Home Page",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Show Post Form or Event Form based on formType
                    if (formType == "Post") {
                        // POST FORM CONTENT
                        PostFormContent(
                            postType = postType,
                            onPostTypeChange = { postType = it },
                            postSubject = postSubject,
                            onPostSubjectChange = { postSubject = it },
                            postPriority = postPriority,
                            onPostPriorityChange = { postPriority = it },
                            postGroup = postGroup,
                            onPostGroupChange = { postGroup = it },
                            employeeSearchQuery = employeeSearchQuery,
                            onEmployeeSearchQueryChange = { searchEmployees(it) },
                            selectedEmployee = selectedEmployee,
                            onEmployeeSelected = { selectedEmployee = it; suggestedEmployees = emptyList() },
                            suggestedEmployees = suggestedEmployees,
                            isSearchingEmployees = isSearchingEmployees,
                            department = department,
                            onDepartmentChange = { department = it },
                            location = location,
                            onLocationChange = { location = it },
                            fetchedDepartments = fetchedDepartments,
                            fetchedLocations = fetchedLocations,
                            announcementDescription = announcementDescription,
                            onAnnouncementDescriptionChange = { announcementDescription = it },
                            postStartDate = postStartDate,
                            onPostStartDateChange = { postStartDate = it },
                            postEndDate = postEndDate,
                            onPostEndDateChange = { postEndDate = it },
                            startDurationDate = startDurationDate,
                            onStartDurationDateChange = { startDurationDate = it },
                            startDurationTime = startDurationTime,
                            onStartDurationTimeChange = { startDurationTime = it },
                            endDurationDate = endDurationDate,
                            onEndDurationDateChange = { endDurationDate = it },
                            endDurationTime = endDurationTime,
                            onEndDurationTimeChange = { endDurationTime = it },
                            supportChannelDetails = supportChannelDetails,
                            onSupportChannelDetailsChange = { supportChannelDetails = it },
                            postImageUris = postImageUris,
                            onPostImageUrisChange = { postImageUris = it },
                            onPreview = { showPostPreview = true },
                            onSubmit = {
                                if (announcementDescription.isNotBlank()) {
                                    onSubmit("", announcementDescription, "post")
                                }
                            },
                        )
                    } else {
                        // EVENT FORM CONTENT
                        EventFormContent(
                            eventSubject = eventSubject,
                            onEventSubjectChange = { eventSubject = it },
                            eventDescription = eventDescription,
                            onEventDescriptionChange = { eventDescription = it },
                            eventImageUri = eventImageUri,
                            onEventImageUriChange = { eventImageUri = it },
                            eventDate = eventDate,
                            onEventDateChange = { eventDate = it },
                            eventStartDate = eventStartDate,
                            onEventStartDateChange = { eventStartDate = it },
                            eventEndDate = eventEndDate,
                            onEventEndDateChange = { eventEndDate = it },
                            onPreview = { showEventPreview = true },
                            onSubmit = {
                                if (eventSubject.isNotBlank() && eventDescription.isNotBlank()) {
                                    onSubmit(eventSubject, eventDescription, "event")
                                }
                            },
                        )
                    }
                }
            }
        }

        // Show Post Preview Dialog
        if (showPostPreview) {
            PostPreviewDialog(
                profilePicUrl = profilePicUrl,
                userName = userName,
                postType = postType,
                postSubject = postSubject,
                postPriority = postPriority,
                postGroup = postGroup,
                employee = selectedEmployee?.displayName ?: "",
                announcementDescription = announcementDescription,
                postStartDate = postStartDate,
                postEndDate = postEndDate,
                startDurationDate = startDurationDate,
                startDurationTime = startDurationTime,
                endDurationDate = endDurationDate,
                endDurationTime = endDurationTime,
                supportChannelDetails = supportChannelDetails,
                postImageUris = postImageUris,
                onDismiss = { showPostPreview = false }
            )
        }

        // Show Event Preview Dialog
        if (showEventPreview) {
            EventPreviewDialog(
                eventSubject = eventSubject,
                eventDescription = eventDescription,
                eventImageUri = eventImageUri,
                eventDate = eventDate,
                eventStartDate = eventStartDate,
                eventEndDate = eventEndDate,
                onDismiss = { showEventPreview = false }
            )
        }
    }
}

@Composable
private fun PostFormContent(
    postType: String,
    onPostTypeChange: (String) -> Unit,
    postSubject: String,
    onPostSubjectChange: (String) -> Unit,
    postPriority: String,
    onPostPriorityChange: (String) -> Unit,
    postGroup: String,
    onPostGroupChange: (String) -> Unit,
    employeeSearchQuery: String,
    onEmployeeSearchQueryChange: (String) -> Unit,
    selectedEmployee: com.archeGlobal.one.model.SuggestedUser?,
    onEmployeeSelected: (com.archeGlobal.one.model.SuggestedUser) -> Unit,
    suggestedEmployees: List<com.archeGlobal.one.model.SuggestedUser>,
    isSearchingEmployees: Boolean,
    department: String,
    onDepartmentChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    fetchedDepartments: List<String>,
    fetchedLocations: List<String>,
    announcementDescription: String,
    onAnnouncementDescriptionChange: (String) -> Unit,
    postStartDate: String,
    onPostStartDateChange: (String) -> Unit,
    postEndDate: String,
    onPostEndDateChange: (String) -> Unit,
    startDurationDate: String,
    onStartDurationDateChange: (String) -> Unit,
    startDurationTime: String,
    onStartDurationTimeChange: (String) -> Unit,
    endDurationDate: String,
    onEndDurationDateChange: (String) -> Unit,
    endDurationTime: String,
    onEndDurationTimeChange: (String) -> Unit,
    supportChannelDetails: String,
    onSupportChannelDetailsChange: (String) -> Unit,
    postImageUris: List<Uri>,
    onPostImageUrisChange: (List<Uri>) -> Unit,
    onPreview: () -> Unit,
    onSubmit: () -> Unit,
) {
    val postTypes = listOf("Planned", "Unplanned/Emergency")

    val plannedSubjects = listOf(
        "Housekeeping schedule",
        "Pest control or deep cleaning activities",
        "Pantry & cafeteria updates",
        "Air conditioning or lighting maintenance",
        "Fire drills or emergency activities",
        "Lost & found notifications",
        "Security protocol reminders",
        "Access restriction or badge issues",
        "Lift/escalator maintenance",
        "Parking space updates",
        "Delivery or courier notifications",
        "Clean desk policy reminders",
        "Power outage or generator testing",
        "Visitor on floor alerts",
        "Noise level reminders",
        "Seating arrangement changes"
    )

    val unplannedSubjects = listOf(
        "Air conditioning/lighting maintenance",
        "Pantry & cafeteria update",
        "Fire drills/emergency",
        "Lost & found",
        "Lift/escalator maintenance",
        "Delivery or courier notification",
        "Power outage/generator testing",
        "Noise level reminder"
    )

    val postSubjects = if (postType == "Planned") plannedSubjects else unplannedSubjects
    val postPriorities = listOf("High", "Medium", "Low")
    val postGroups = listOf("Employee-based", "Department-based", "Location-based", "Everyone.global")

    // Use fetched data, fallback to empty if not loaded yet
    val departments = fetchedDepartments
    val locations = fetchedLocations

    // Image picker launcher for multiple images (max 3)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (postImageUris.size < 3) {
                onPostImageUrisChange(postImageUris + it)
            }
        }
    }

    // Post Type Dropdown
    PostDropdown(
        label = "Post Type",
        selectedValue = postType,
        options = postTypes,
        onValueSelected = onPostTypeChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Subject Dropdown
    PostDropdown(
        label = "Post Subject",
        selectedValue = postSubject,
        options = postSubjects,
        onValueSelected = onPostSubjectChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Priority Dropdown
    PostDropdown(
        label = "Post Priority",
        selectedValue = postPriority,
        options = postPriorities,
        onValueSelected = onPostPriorityChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Group Dropdown
    PostDropdown(
        label = "Post Group",
        selectedValue = postGroup,
        options = postGroups,
        onValueSelected = onPostGroupChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Conditional field based on Post Group selection
    when (postGroup) {
        "Employee-based" -> {
            // Employee Search Field with suggestions
            Column {
                // Search field
                OutlinedTextField(
                    value = employeeSearchQuery,
                    onValueChange = { query ->
                        onEmployeeSearchQueryChange(query)
                    },
                    placeholder = {
                        Text(
                            text = "Search Member to add",
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    ),
                    textStyle = TextStyle(
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    trailingIcon = {
                        if (employeeSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onEmployeeSearchQueryChange("")
                            }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Loading indicator
                if (isSearchingEmployees) {
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

                // Suggestions list
                if (suggestedEmployees.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            suggestedEmployees.take(5).forEach { employee ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEmployeeSelected(employee)
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = employee.displayName,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = employee.mail,
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                        )
                                    }
                                }
                                if (employee != suggestedEmployees.take(5).last()) {
                                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Display selected employee
                if (selectedEmployee != null && selectedEmployee?.mail?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selected Employee",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedEmployee?.displayName ?: "",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                            Text(
                                text = selectedEmployee?.mail ?: "",
                                fontFamily = GraphikFontFamily,
                                fontSize = 10.sp,
                                color = Color.Gray,
                            )
                        }
                        IconButton(
                            onClick = {
                                onEmployeeSelected(com.archeGlobal.one.model.SuggestedUser("", ""))
                            },
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.bin),
                                contentDescription = "Remove",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Department-based" -> {
            // Department Dropdown
            PostDropdown(
                label = "Department",
                selectedValue = department,
                options = departments,
                onValueSelected = onDepartmentChange,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Location-based" -> {
            // Location Dropdown
            PostDropdown(
                label = "Location",
                selectedValue = location,
                options = locations,
                onValueSelected = onLocationChange,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Everyone.global" -> {
            // No field for Everyone.global
        }
    }

    // Announcement Description (multiline)
    PostTextField(
        label = "Announcement Description",
        value = announcementDescription,
        onValueChange = onAnnouncementDescriptionChange,
        placeholder = "Announcement Description...",
        singleLine = false,
        minLines = 5,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Select images to attach button (max 3 images)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = if (postImageUris.size < 3) Color.White else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = postImageUris.size < 3) {
                if (postImageUris.size < 3) {
                    imagePickerLauncher.launch("image/*")
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_gallery),
                contentDescription = "Gallery",
                tint = if (postImageUris.size < 3) Color.Gray else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (postImageUris.size < 3)
                    "Select images to attach (${postImageUris.size}/3)"
                else
                    "Maximum 3 images reached",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                color = if (postImageUris.size < 3) Color.Gray else Color.Gray.copy(alpha = 0.5f),
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Attached Photos Section
    if (postImageUris.isNotEmpty()) {
        Text(
            text = "Attached Photos",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Horizontal scrollable row of images
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            postImageUris.forEach { uri ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Attached Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Remove image button (X)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(20.dp)
                            .background(
                                color = PrimaryRed,
                                shape = CircleShape
                            )
                            .clickable {
                                onPostImageUrisChange(postImageUris.filter { it != uri })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Post Start Date
    PostDateField(
        label = "Post Start Date",
        value = postStartDate,
        onValueChange = onPostStartDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post End Date
    PostDateField(
        label = "Post End Date",
        value = postEndDate,
        onValueChange = onPostEndDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Activity Duration Section
    Text(
        text = "Activity Duration",
        fontFamily = GraphikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp),
    )

    // Start Duration Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Start\nDuration",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.width(80.dp),
            lineHeight = 18.sp
        )
        PostDateField(
            label = "",
            value = startDurationDate,
            onValueChange = onStartDurationDateChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
        PostTimeField(
            label = "",
            value = startDurationTime,
            onValueChange = onStartDurationTimeChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // End Duration Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "End\nDuration",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.width(80.dp),
            lineHeight = 18.sp
        )
        PostDateField(
            label = "",
            value = endDurationDate,
            onValueChange = onEndDurationDateChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
        PostTimeField(
            label = "",
            value = endDurationTime,
            onValueChange = onEndDurationTimeChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Support Channel Details (optional)
    PostTextField(
        label = "Support Channel Details (optional)",
        value = supportChannelDetails,
        onValueChange = onSupportChannelDetailsChange,
        placeholder = "Support Channel Details (optional)",
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Preview and Post Buttons Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Preview Button
        Button(
            onClick = onPreview,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_view_eye),
                contentDescription = "Preview",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Preview",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
        }

        // Post Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White,
                disabledContainerColor = PrimaryRed,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = announcementDescription.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Post",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Post",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
    }

    Spacer(modifier = Modifier.height(48.dp))
}

@Composable
private fun EventFormContent(
    eventSubject: String,
    onEventSubjectChange: (String) -> Unit,
    eventDescription: String,
    onEventDescriptionChange: (String) -> Unit,
    eventImageUri: Uri?,
    onEventImageUriChange: (Uri?) -> Unit,
    eventDate: String,
    onEventDateChange: (String) -> Unit,
    eventStartDate: String,
    onEventStartDateChange: (String) -> Unit,
    eventEndDate: String,
    onEventEndDateChange: (String) -> Unit,
    onPreview: () -> Unit,
    onSubmit: () -> Unit,
) {
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onEventImageUriChange(it)
        }
    }
    // Post Subject (Event Subject)
    PostTextField(
        label = "Post Subject",
        value = eventSubject,
        onValueChange = onEventSubjectChange,
        placeholder = "Enter Subject",
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Note about home screen display
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "*",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Red,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "This will be displayed on the home screen",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Gray,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Post Description (Event Description)
    PostTextField(
        label = "Post Description",
        value = eventDescription,
        onValueChange = onEventDescriptionChange,
        placeholder = "Enter post description...",
        singleLine = false,
        minLines = 5,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Select image to attach button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { imagePickerLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_gallery),
                contentDescription = "Gallery",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select image to attach",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                color = Color.Gray,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Attached Photo Section
    eventImageUri?.let { uri ->
        Text(
            text = "Attached Photo",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Single image preview
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(150.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Attached Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Remove image button (X)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(20.dp)
                    .background(
                        color = PrimaryRed,
                        shape = CircleShape
                    )
                    .clickable {
                        onEventImageUriChange(null)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Image",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Event Date
    PostDateField(
        label = "Event Date",
        value = eventDate,
        onValueChange = onEventDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Start Date
    PostDateField(
        label = "Post Start Date",
        value = eventStartDate,
        onValueChange = onEventStartDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post End Date
    PostDateField(
        label = "Post End Date",
        value = eventEndDate,
        onValueChange = onEventEndDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Preview and Create Event Buttons Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Preview Button
        Button(
            onClick = onPreview,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_view_eye),
                contentDescription = "Preview",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Preview",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
        }

        // Create Event Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = eventSubject.isNotBlank() && eventDescription.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Create Event",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Event",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }

    Spacer(modifier = Modifier.height(48.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = PrimaryRed,
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                ),
                textStyle = TextStyle(
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = GraphikFontFamily,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PostTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotEmpty()) {
                {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                    )
                }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) {
                        Modifier.height(56.dp)
                    } else {
                        Modifier.heightIn(min = (56 * minLines).dp)
                    }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
            ),
            textStyle = TextStyle(
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            singleLine = singleLine,
            minLines = if (!singleLine) minLines else 1,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        )
    }
}

@Composable
private fun PostDateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d %s %04d", selectedDay,
                getMonthName(selectedMonth), selectedYear)
            onValueChange(formattedDate)
        },
        year,
        month,
        day,
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = if (compact) Color(0xFFE8E8E8) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable { datePickerDialog.show() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else if (compact) "31 Oct 2025" else "Select date",
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun PostTimeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d %s",
                if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour,
                selectedMinute,
                if (selectedHour >= 12) "PM" else "AM"
            )
            onValueChange(formattedTime)
        },
        hour,
        minute,
        false, // 12-hour format
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = if (compact) Color(0xFFE8E8E8) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable { timePickerDialog.show() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else "5:08 PM",
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                )
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "Jan"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Aug"
        8 -> "Sep"
        9 -> "Oct"
        10 -> "Nov"
        11 -> "Dec"
        else -> ""
    }
}

@Composable
private fun PostPreviewDialog(
    profilePicUrl: String?,
    userName: String,
    postType: String,
    postSubject: String,
    postPriority: String,
    postGroup: String,
    employee: String,
    announcementDescription: String,
    postStartDate: String,
    postEndDate: String,
    startDurationDate: String,
    startDurationTime: String,
    endDurationDate: String,
    endDurationTime: String,
    supportChannelDetails: String,
    postImageUris: List<Uri>,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    // Title
                    Text(
                        text = "HeadsUp Preview",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // White content card (includes profile header)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header with profile and priority
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Profile picture
                                    Card(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(containerColor = Color.Gray)
                                    ) {
                                        if (profilePicUrl != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(profilePicUrl),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Gray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = userName.take(1).uppercase(),
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = userName,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = Color.Black,
                                        )
                                        Text(
                                            text = "12 November 2025",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                        )
                                    }
                                }

                                // Priority badge - only show if selected
                                if (postPriority != "Select Post Priority") {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (postPriority) {
                                                    "High" -> Color(0xFFD32F2F)
                                                    "Medium" -> Color(0xFFFFA726)
                                                    "Low" -> Color(0xFF66BB6A)
                                                    else -> Color.Gray
                                                },
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = postPriority,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            // Post Subject - only show if not default selection
                            if (postSubject != "Select Post Subject") {
                                Text(
                                    text = postSubject,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Announcement Description - only show if not empty
                            if (announcementDescription.isNotEmpty()) {
                                Text(
                                    text = announcementDescription,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Support and Duration labels (always show)
                            Text(
                                text = "Support:",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                            Text(
                                text = "Duration:",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )

                            // Attached Images - display in grid if images exist
                            if (postImageUris.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))

                                // Grid layout for images (2 columns)
                                val rows = (postImageUris.size + 1) / 2
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (rowIndex in 0 until rows) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (colIndex in 0 until 2) {
                                                val imageIndex = rowIndex * 2 + colIndex
                                                if (imageIndex < postImageUris.size) {
                                                    Card(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(140.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                    ) {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(postImageUris[imageIndex]),
                                                            contentDescription = "Attached Image",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start and End dates - only show if at least one is filled
                    if (postStartDate.isNotEmpty() || postEndDate.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Post Start Date - only if filled
                            if (postStartDate.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Post Start Date",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color(0xFF66BB6A), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = postStartDate,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                        )
                                    }
                                }
                            }

                            // Post End Date - only if filled
                            if (postEndDate.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Post End Date",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color(0xFFD32F2F), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = postEndDate,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(140.dp)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventPreviewDialog(
    eventSubject: String,
    eventDescription: String,
    eventImageUri: Uri?,
    eventDate: String,
    eventStartDate: String,
    eventEndDate: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Event Image
                    eventImageUri?.let { uri ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Event Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Event Title/Subject
                    Text(
                        text = eventSubject.ifEmpty { "Event Title" },
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Event Date
                    if (eventDate.isNotEmpty()) {
                        Text(
                            text = eventDate,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Start and End Dates
                    if (eventStartDate.isNotEmpty() && eventEndDate.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "Start: $eventStartDate",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "End: $eventEndDate",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Event Description
                    if (eventDescription.isNotEmpty()) {
                        Text(
                            text = eventDescription,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
