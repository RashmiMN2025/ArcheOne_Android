package com.archeGlobal.one.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
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
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: String) -> Unit,
) {
    // Form type switcher state
    var formType by remember { mutableStateOf("Post") } // "Post" or "Event"

    // Post form states
    var postType by remember { mutableStateOf("Planned") }
    var postSubject by remember { mutableStateOf("Select Post Subject") }
    var postPriority by remember { mutableStateOf("Select Post Priority") }
    var postGroup by remember { mutableStateOf("Employee-based") }
    var employee by remember { mutableStateOf("") }
    var announcementDescription by remember { mutableStateOf("") }
    var postStartDate by remember { mutableStateOf("") }
    var postEndDate by remember { mutableStateOf("") }
    var startDurationDate by remember { mutableStateOf("") }
    var startDurationTime by remember { mutableStateOf("") }
    var endDurationDate by remember { mutableStateOf("") }
    var endDurationTime by remember { mutableStateOf("") }
    var supportChannelDetails by remember { mutableStateOf("") }

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
    val showCloseButton by remember {
        derivedStateOf { scrollState.value <= 50 || scrollState.value > 100 }
    }
    val isScrolledDown by remember {
        derivedStateOf { scrollState.value > 100 }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F4EE))
                .systemBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                // Header spacing
                Spacer(modifier = Modifier.height(80.dp))

                // Title
                Text(
                    text = if (formType == "Post") "Create Post" else "Create Event",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 25.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                )

                // Form Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
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
                            text = "Create Post",
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
                            text = "Create Event",
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
                        .padding(horizontal = 20.dp),
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
                            employee = employee,
                            onEmployeeChange = { employee = it },
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

            // Floating close button - appears when scrolling
            if (showCloseButton) {
                // Horizontal background bar when scrolled down
                if (isScrolledDown) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(65.dp)
                            .background(
                                color = Color.Gray.copy(alpha = 0.7f),
                            ),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .size(35.dp)
                        .background(
                            color = if (isScrolledDown) Color.Gray.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isScrolledDown) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
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
    employee: String,
    onEmployeeChange: (String) -> Unit,
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
    val postGroups = listOf("Employee-based", "Department-based", "Location-based", "All")

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

    // Employee Search Field
    PostTextField(
        label = "Employee",
        value = employee,
        onValueChange = onEmployeeChange,
        placeholder = "Search and Select Employee",
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(24.dp))

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

    // Select images to attach button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            ),
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
                text = "Select images to attach",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                color = Color.Gray,
            )
        }
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

    // Start Duration
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Start Duration",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PostDateField(
                label = "",
                value = startDurationDate,
                onValueChange = onStartDurationDateChange,
                compact = true,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(28.dp))
            PostTimeField(
                label = "",
                value = startDurationTime,
                onValueChange = onStartDurationTimeChange,
                compact = true,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // End Duration
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "End Duration",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PostDateField(
                label = "",
                value = endDurationDate,
                onValueChange = onEndDurationDateChange,
                compact = true,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(28.dp))
            PostTimeField(
                label = "",
                value = endDurationTime,
                onValueChange = onEndDurationTimeChange,
                compact = true,
            )
        }
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

    // Preview Button
    OutlinedButton(
        onClick = { /* TODO: Preview functionality */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF0066FF),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_view_eye),
            contentDescription = "Preview",
            tint = Color(0xFF0066FF),
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

    Spacer(modifier = Modifier.height(16.dp))

    // Post Button
    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
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
                .width(120.dp)
                .height(180.dp)
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
            IconButton(
                onClick = {
                    onEventImageUriChange(null)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(
                        color = PrimaryRed,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Image",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
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

    // Preview Button
    OutlinedButton(
        onClick = onPreview,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF0066FF),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_view_eye),
            contentDescription = "Preview",
            tint = Color(0xFF0066FF),
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

    Spacer(modifier = Modifier.height(16.dp))

    // Create Event Button
    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
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
            fontSize = 18.sp,
        )
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

    Column {
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
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable { datePickerDialog.show() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else if (compact) "31 Oct 2025" else "Select date",
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.calendar_3x),
                    contentDescription = "Calendar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp),
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

    Column {
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
                    color = Color.White,
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
        0 -> "January"
        1 -> "February"
        2 -> "March"
        3 -> "April"
        4 -> "May"
        5 -> "June"
        6 -> "July"
        7 -> "August"
        8 -> "September"
        9 -> "October"
        10 -> "November"
        11 -> "December"
        else -> ""
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
