package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.model.SupportTicket
import com.archeGlobal.one.model.TicketStatus
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTrackingScreen(
    controller: HelpDeskController
) {
    val model by controller.model.collectAsState()

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
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Track Your Tickets",
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { controller.navigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "View the status of your raised concerns",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when {
                        model.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        model.error != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error loading tickets",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Red
                                )
                                Text(
                                    text = model.error!!,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    fontFamily = GraphikFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Button(
                                    onClick = { controller.refreshTickets() },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text(
                                        text = "Retry",
                                        fontFamily = GraphikFontFamily
                                    )
                                }
                            }
                        }
                        model.tickets.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tickets found",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.Gray
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(model.tickets) { ticket ->
                                    TicketCard(ticket = ticket)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCard(
    ticket: SupportTicket
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Ticket ${ticket.ticketNumber}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black
                    )
                    Text(
                        text = ticket.title,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = ticket.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.Gray
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Created: ${ticket.createdDate}",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hello Team,",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ticket.description,
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = GraphikFontFamily,
                        lineHeight = 20.sp
                    )

                    ticket.details?.let { details ->
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Category: ${ticket.category}",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                        if (details.rating != null) {
                            Text(
                                text = "Rating: ${details.rating}",
                                fontSize = 12.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "Platform: ${details.platform}",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                        Text(
                            text = "Device: ${details.deviceInfo}",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                        Text(
                            text = "Android Version: ${details.appVersion}",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Best regards,",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                        Text(
                            text = "ArcheOne Team",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: TicketStatus) {
    val (statusText, backgroundColor) = when (status) {
        TicketStatus.OPEN -> "Open" to Color(0xFFFF9800)
        TicketStatus.IN_PROGRESS -> "In Progress" to Color(0xFF2196F3)
        TicketStatus.CLOSED -> "Closed" to Color(0xFF4CAF50)
        TicketStatus.PENDING -> "Pending" to Color(0xFFFFC107)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = statusText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GraphikFontFamily,
            color = Color.White
        )
    }
}
