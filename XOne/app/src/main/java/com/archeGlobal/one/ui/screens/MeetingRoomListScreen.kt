package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.MeetingRoomListController
import com.archeGlobal.one.model.MeetingRoom
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRoomListScreen(
    controller: MeetingRoomListController,
    numberOfAttendees: String,
    meetingType: String,
    onBackPressed: () -> Unit,
    onRoomClick: (MeetingRoom) -> Unit = {}
) {
    val meetingRooms = listOf(
        MeetingRoom("KAIVALYA", 8, "Display Unit(55 Inch)"),
        MeetingRoom("ANANDA", 8, "Display Unit(55 Inch)"),
        MeetingRoom("OJAS", 8, "Display Unit(55 Inch)"),
        MeetingRoom("CONFERENCE ROOM A", 12, "Display Unit(65 Inch), Video Conferencing"),
        MeetingRoom("CONFERENCE ROOM B", 10, "Display Unit(55 Inch), Audio System"),
        MeetingRoom("BOARDROOM", 15, "Display Unit(75 Inch), Video Conferencing, Audio System")
    )

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

                val attendeesInt = numberOfAttendees.toIntOrNull() ?: 0
                val filteredRooms = meetingRooms.filter { it.capacity <= attendeesInt }

                // Meeting Rooms List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredRooms) { room ->
                        MeetingRoomCard(
                            room = room,
                            onClick = { onRoomClick(room) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingRoomCard(
    room: MeetingRoom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onClick() }
            .padding(bottom = 15.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background image placeholder
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
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // Room details overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = room.name,
                    fontSize = 24.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Meeting Room | Capacity: ${room.capacity}",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Equipment: ${room.equipment}",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}