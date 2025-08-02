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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.model.SupportTicket
import com.archeGlobal.one.model.TicketStatus
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTrackingScreen(
    controller: HelpDeskController
) {
    val model by controller.model.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    // Handle back press gesture to navigate to proper source screen
    BackHandler {
        controller.navigateBack()
    }

    // Update isRefreshing based on model.isLoading
    LaunchedEffect(model.isLoading) {
        isRefreshing = model.isLoading
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
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.offset(x = (-24).dp),
                                text = "Track Your Tickets",
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
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
                    // Show different text based on navigation source
                    val statusText = when (controller.getNavigationSource()) {
                        "asset" -> "Check the status of your tickets"
                        else -> "Check the status of your tickets"
                    }

                    Text(
                        text = statusText,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { controller.refreshTickets() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
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
                            model.tickets.isEmpty() && !model.isLoading -> {
                                EmptyTicketsState(
                                    navigationSource = controller.getNavigationSource(),
                                    onRaiseTicket = { controller.raiseTicket("", "") },
                                    onRaiseConcern = { controller.raiseConcern("", "") }
                                )
                            }
                            else -> {
                                TicketsList(tickets = model.tickets)
                            }
                        }
                    }
                }
            }
        }

        // Show UniversalLoader for programmatic refresh (not swipe refresh)
        if (isRefreshing && !swipeRefreshState.isRefreshing) {
            UniversalLoader(isLoading = true)
        }
    }
}

@Composable
fun TicketsList(
    tickets: List<SupportTicket>
) {
    var expandedTicketId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(tickets) { ticket ->
            TicketCard(
                ticket = ticket,
                isExpanded = expandedTicketId == ticket.ticketNumber,
                onExpandToggle = { ticketId ->
                    expandedTicketId = if (expandedTicketId == ticketId) null else ticketId
                }
            )
        }
    }
}

@Composable
fun TicketCard(
    ticket: SupportTicket,
    isExpanded: Boolean,
    onExpandToggle: (String) -> Unit
) {
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
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle(ticket.ticketNumber) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Ticket ${ticket.ticketNumber}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black
                    )
                    Text(
                        text = ticket.title,
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = GraphikFontFamily,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.offset(y = (-4).dp)) {
                        StatusChip(status = ticket.status)
                    }
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
                    // Add separator line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Created Date",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Created: ${ticket.createdDate}",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.description),
                                contentDescription = "Description",
                                modifier = Modifier.size(12.dp),
                                colorFilter = ColorFilter.tint(Color.Gray)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ticket.description,
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontFamily = GraphikFontFamily,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

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
        TicketStatus.OPEN -> "Open" to Color(0xFFD32F2F) // Changed to red
        TicketStatus.IN_PROGRESS -> "In Progress" to Color(0xFF2196F3)
        TicketStatus.CLOSED -> "Closed" to Color(0xFF4CAF50)
        TicketStatus.PENDING -> "Pending" to Color(0xFFFFC107)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 1.dp)
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

@Composable
fun EmptyTicketsState(
    navigationSource: String?,
    onRaiseTicket: () -> Unit,
    onRaiseConcern: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.noimage),
                contentDescription = "No tickets",
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No tickets raised yet.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Raise a issue to start tracking.",
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
