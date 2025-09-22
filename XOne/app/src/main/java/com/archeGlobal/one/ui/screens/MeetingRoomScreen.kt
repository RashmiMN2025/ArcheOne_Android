package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
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
import com.archeGlobal.one.model.MeetingRoom
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRoomScreen(
    room: MeetingRoom,
    numberOfAttendees: String,
    meetingType: String,
    onBackPressed: () -> Unit,
    onSubmit: () -> Unit = {}
) {
    var businessJustification by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var emailList by remember { mutableStateOf(listOf<String>()) }
    var archeAttendees by remember { mutableStateOf("") }

    var meetingExtensionRequired by remember { mutableStateOf(false) }
    var refreshmentRequired by remember { mutableStateOf(false) }
    var additionalRequests by remember { mutableStateOf(false) }

    BackHandler {
        onBackPressed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WelcomeBackgroundTop,
                            WelcomeBackgroundMiddle,
                            WelcomeBackgroundBottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MeetSpace",
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp)) // Balance the navigation icon
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Content
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Room Image
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            shape = RectangleShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Placeholder image
                                Image(
                                    painter = painterResource(id = room.imageRes),
                                    contentDescription = "${room.name} Meeting Room",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Dark overlay for text readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Room Details",
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Room Name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                                            contentDescription = "Room",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Text(
                                            text = "Name: ${room.name}",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )
                                    }

                                    // Room Type
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = android.R.drawable.ic_menu_mapmode),
                                            contentDescription = "Type",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Text(
                                            text = "Type: Meeting Room",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )
                                    }

                                    // Equipment
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                            contentDescription = "Equipment",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Text(
                                            text = "Equipment: ${room.equipment}",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )
                                    }

                                    // Capacity
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Capacity",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Text(
                                            text = "Capacity: ${room.capacity} Seats",
                                            fontSize = 18.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )
                                    }


                                    // Facilities Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Helpdesk
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .background(Color(0xFFF6F4EE), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = android.R.drawable.ic_menu_help),
                                                    contentDescription = "Helpdesk",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Helpdesk",
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
                                            )
                                        }

                                        Divider(
                                            color = Color.LightGray,
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(50.dp)
                                        )

                                        // Pantry
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .background(Color(0xFFF6F4EE), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = android.R.drawable.ic_menu_set_as),
                                                    contentDescription = "Pantry",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Pantry",
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
                                            )
                                        }

                                        Divider(
                                            color = Color.LightGray,
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(50.dp)
                                        )

                                        // Facility
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .background(Color(0xFFF6F4EE), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = android.R.drawable.ic_menu_manage),
                                                    contentDescription = "Facility",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Facility",
                                                fontSize = 14.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
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
                                color = Color.Black
                            )

                            // Booking Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // User info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "User",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "Biswajit",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
                                            )
                                        }

                                        // Location info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Location",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "Bangalore",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Date info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Date",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "21 August 2025",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
                                            )
                                        }

                                        // Time info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = "Time",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "6:54 PM -\n 6:54 PM",
                                                fontSize = 15.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            // Client Name
                            OutlinedTextField(
                                value = businessJustification,
                                onValueChange = { businessJustification = it },
                                placeholder = {
                                    Text(
                                        "Client Name",
                                        color = Color.LightGray,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                    ) },
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
                                value = businessJustification,
                                onValueChange = { businessJustification = it },
                                placeholder = {
                                    Text(
                                        "Project Name",
                                        color = Color.LightGray,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                    ) },
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

                            // Search for Attendees
                            OutlinedTextField(
                                value = archeAttendees,
                                onValueChange = { archeAttendees = it },
                                placeholder = {
                                    Text(
                                        "Search for Attendees to Add",
                                        color = Color.LightGray,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                    ) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color.Gray
                                    )
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
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        shape = RoundedCornerShape(28.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                        ) {
                                            emailList.forEachIndexed { index, email ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = email,
                                                        fontSize = 14.sp,
                                                        fontFamily = GraphikFontFamily,
                                                        fontWeight = FontWeight.Normal,
                                                        color = Color.Black,
                                                        modifier = Modifier
                                                            .padding(start = 30.dp)
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            emailList = emailList.filterIndexed { i, _ -> i != index }
                                                        }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .background(Color(0xFFDD3825), CircleShape),
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
                        }

                        // Toggle Options
                        Column(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Meeting Extension Required?",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Switch(
                                    checked = meetingExtensionRequired,
                                    onCheckedChange = { meetingExtensionRequired = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFDD3825),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Refreshment Required?",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Switch(
                                    checked = refreshmentRequired,
                                    onCheckedChange = { refreshmentRequired = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFDD3825),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Additional Requests?",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Switch(
                                    checked = additionalRequests,
                                    onCheckedChange = { additionalRequests = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFDD3825),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Button
                            Button(
                                onClick = onSubmit,
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
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}