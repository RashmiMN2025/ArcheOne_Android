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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.model.SupportTicket
import com.archeGlobal.one.model.TicketStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTrackingScreen(
    controller: HelpDeskController
) {
    val model by controller.model.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5E6F0))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Track Your Tickets",
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
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
                containerColor = Color(0xFFF5E6F0)
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
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

@Composable
fun TicketCard(
    ticket: SupportTicket
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        color = Color.Black
                    )
                    Text(
                        text = ticket.title,
                        fontSize = 14.sp,
                        color = Color.Gray,
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

            if (isExpanded && ticket.details != null) {
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )

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
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ticket.description,
                        fontSize = 14.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )

                    ticket.details?.let { details ->
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Category: ${ticket.category}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        if (details.rating != null) {
                            Text(
                                text = "Rating: ${details.rating}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "Platform: ${details.platform}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Device: ${details.deviceInfo}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Android Version: ${details.appVersion}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        details.additionalNotes?.let { notes ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = notes,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: TicketStatus) {
    val (backgroundColor, textColor) = when (status) {
        TicketStatus.OPEN -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        TicketStatus.IN_PROGRESS -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        TicketStatus.CLOSED -> Color(0xFFE8F5E8) to Color(0xFF388E3C)
        TicketStatus.PENDING -> Color(0xFFFFE8E6) to Color(0xFFD32F2F)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = when (status) {
                TicketStatus.OPEN -> "Open"
                TicketStatus.IN_PROGRESS -> "In Progress"
                TicketStatus.CLOSED -> "Closed"
                TicketStatus.PENDING -> "Pending"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}