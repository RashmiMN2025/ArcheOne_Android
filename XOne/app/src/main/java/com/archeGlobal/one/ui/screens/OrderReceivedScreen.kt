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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.OrderReceivedController
import com.archeGlobal.one.model.Order
import com.archeGlobal.one.model.OrderReceivedModel
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderReceivedScreen(
    model: OrderReceivedModel,
    controller: OrderReceivedController
) {
    // Handle back gesture navigation
    BackHandler {
        controller.onBackPressed()
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
                OrderReceivedHeader(
                    onBackPressed = controller::onBackPressed,
                    onRefresh = controller::refreshOrders
                )

                // Content
                when {
                    model.isLoading -> {
                        // Loading will be handled by UniversalLoader overlay
                    }
                    model.error != null -> {
                        ErrorContent(
                            error = model.error,
                            onRetry = controller::refreshOrders,
                            onDismiss = controller::clearError
                        )
                    }
                    model.orders.isEmpty() -> {
                        EmptyOrdersContent()
                    }
                    else -> {
                        OrdersList(
                            orders = model.orders,
                            onOrderClick = controller::onOrderClick
                        )
                    }
                }
            }

            // Loading overlay
            if (model.isLoading) {
                UniversalLoader(isLoading = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderReceivedHeader(
    onBackPressed: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order Received",
                    color = Color.Black,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
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
}

@Composable
fun OrdersList(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(orders) { order ->
            OrderCard(
                order = order,
                onClick = { onOrderClick(order) }
            )
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // First Row: Order ID, New Badge, and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Order ID and New Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order ID: ${order.orderId}",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    if (order.isNew) {
                        NewBadge()
                    }
                }

                // Right side: Status
                Text(
                    text = "Status: ${order.status.displayName}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(android.graphics.Color.parseColor(order.status.colorHex))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Second Row: User and Employee ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "User: ${order.userName}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Emp ID: ${order.employeeId}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Third Row: Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date: ${order.date}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Time: ${order.time}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun OrderInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun NewBadge() {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(
            text = "New",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = Color.White
        )
    }
}

@Composable
fun EmptyOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.order_received),
                contentDescription = "No orders",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.Gray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Orders Received",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "New stationary orders will appear here",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PrimaryRed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = error,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Dismiss",
                        fontFamily = GraphikFontFamily,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    )
                ) {
                    Text(
                        text = "Retry",
                        fontFamily = GraphikFontFamily,
                        color = Color.White
                    )
                }
            }
        }
    }
}
