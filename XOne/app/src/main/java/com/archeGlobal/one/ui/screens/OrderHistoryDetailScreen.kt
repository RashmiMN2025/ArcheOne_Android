package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.OrderHistoryController
import com.archeGlobal.one.model.DeskCartOrderHistory
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryDetailScreen(
    controller: OrderHistoryController,
    orderItem: DeskCartOrderHistory
) {
    // Handle back gesture navigation - always go to Order History
    BackHandler {
        // Use popBackStack to go back in navigation stack (will go to Order History)
        controller.popBackStack()
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
                // Header
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Order Details",
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.offset(x = (-24).dp) // Center accounting for back button
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { controller.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Order Status Card at top
                    OrderHistoryStatusCard(
                        orderId = orderItem.order_Id,
                        orderDate = formatOrderHistoryDateDetails(orderItem.Order_Placed_Time),
                        orderStatus = orderItem.Order_Status
                    )
                    
                    // Employee Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Employee Details",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OrderHistoryDetailRow(label = "Name:", value = orderItem.Emp_Name)
                            OrderHistoryDetailRow(label = "Employee ID:", value = orderItem.Emp_ID)
                            OrderHistoryDetailRow(label = "Department:", value = orderItem.Dept)
                            OrderHistoryDetailRow(label = "Location:", value = orderItem.Location)
                            OrderHistoryDetailRow(
                                label = "Email:", 
                                value = com.archeGlobal.one.utils.UserDataManager.getInstance(
                                    androidx.compose.ui.platform.LocalContext.current
                                ).getUserData()?.email ?: ""
                            )

                        }
                    }

                    // Order Items Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Order Items",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // List each item
                            orderItem.items.filter { it.count > 0 }.forEach { item ->
                                OrderHistoryItemRow(
                                    label = item.name.replace("_", " "),
                                    value = "Qty: ${item.count}"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Total items with divider
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OrderHistoryItemRow(
                                label = "Total Items",
                                value = "Qty: ${orderItem.Total_Items_in_Order}",
                                labelWeight = FontWeight.Normal,
                                valueWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Remarks Card - Show if remarks exist
                    if (orderItem.Remarks.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Remarks",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 19.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                    text = orderItem.Remarks,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun OrderHistoryStatusCard(
    orderId: String,
    orderDate: String,
    orderStatus: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "#$orderId",
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = orderDate,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Status Badge
                val (backgroundColor, textColor, borderColor) = when (orderStatus.lowercase()) {
                    "pending" -> Triple(Color(0xFFFFA500).copy(alpha = 0.15f), Color(0xFFFFA500), Color(0xFFFFA500)) // Orange
                    "approved" -> Triple(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000), Color(0xFF008000)) // Green
                    "rejected", "cancelled" -> Triple(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000), Color(0xFFFF0000)) // Red
                    "closed" -> Triple(Color(0xFF808080).copy(alpha = 0.15f), Color(0xFF808080), Color(0xFF808080)) // Gray
                    else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, Color.Gray) // Fallback
                }
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = orderStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        fontSize = 13.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryDetailRow(
    label: String,
    value: String,
    labelWeight: FontWeight = FontWeight.Medium,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = labelWeight,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = valueWeight,
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun OrderHistoryItemRow(
    label: String,
    value: String,
    labelWeight: FontWeight = FontWeight.Normal,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = labelWeight,
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = valueWeight,
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

// Date formatting function for order history details
private fun formatOrderHistoryDateDetails(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        outputFormat.format(date ?: return dateTimeString.substringBefore(" "))
    } catch (e: Exception) {
        dateTimeString.substringBefore(" ")
    }
}