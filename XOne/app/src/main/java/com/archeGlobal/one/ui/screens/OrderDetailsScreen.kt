package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.OrderHistoryDetailsController
import com.archeGlobal.one.model.OrderHistoryItem
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    controller: OrderHistoryDetailsController,
    orderItem: OrderHistoryItem
) {
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
                                modifier = Modifier.offset(x = (-24).dp) // Center align properly
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { controller.onBackPressed() }) {
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
                    OrderStatusCard(
                        orderId = orderItem.orderId,
                        orderDate = formatOrderDateDetails(orderItem.orderPlacedTime),
                        orderStatus = orderItem.orderStatus
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
                                fontSize = 20.sp, // Increased from 18sp
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            DetailRow(label = "Name:", value = orderItem.empName)
                            DetailRow(label = "Employee ID:", value = orderItem.empId)
                            DetailRow(label = "Department:", value = orderItem.dept)
                            DetailRow(label = "Location:", value = orderItem.location)
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
                                fontSize = 20.sp, // Increased from 18sp
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // List each item
                            orderItem.items.filter { it.count > 0 }.forEach { item ->
                                ItemRow(
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
                            
                            ItemRow(
                                label = "Total Items",
                                value = "Qty: ${orderItem.totalItemsInOrder}",
                                labelWeight = FontWeight.Normal,
                                valueWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Remarks Card - Show for pending orders, approved orders, or if not empty
                    if (orderItem.orderStatus.lowercase() == "pending" || 
                        orderItem.orderStatus.lowercase() == "approved" || 
                        orderItem.remarks.isNotEmpty()) {
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
                                    fontSize = 20.sp, // Increased from 18sp
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                when (orderItem.orderStatus.lowercase()) {
                                    "pending" -> {
                                        // Show text field for pending orders
                                        OutlinedTextField(
                                            value = controller.remarks,
                                            onValueChange = { controller.updateRemarks(it) },
                                            placeholder = {
                                                Text(
                                                    "Enter remarks (Mandatory)",
                                                    color = Color.Gray,
                                                    fontFamily = GraphikFontFamily
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryRed,
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            maxLines = 4
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Action buttons for pending orders
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Button(
                                                onClick = { controller.onRejectOrder(orderItem.orderId) },
                                                modifier = Modifier.weight(1f),
                                                enabled = !controller.isLoading,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFDC3545),
                                                    disabledContainerColor = Color(0xFFDC3545).copy(alpha = 0.6f)
                                                ),
                                                shape = RoundedCornerShape(28.dp)
                                            ) {
                                                Text(
                                                    text = "Reject",
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                            }

                                            Button(
                                                onClick = { controller.onApproveOrder(orderItem.orderId) },
                                                modifier = Modifier.weight(1f),
                                                enabled = !controller.isLoading,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF28A745),
                                                    disabledContainerColor = Color(0xFF28A745).copy(alpha = 0.6f)
                                                ),
                                                shape = RoundedCornerShape(28.dp)
                                            ) {
                                                Text(
                                                    text = "Approve",
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    
                                    "approved" -> {
                                        // Show text field for approved orders (mandatory remarks)
                                        OutlinedTextField(
                                            value = controller.remarks,
                                            onValueChange = { controller.updateRemarks(it) },
                                            placeholder = {
                                                Text(
                                                    "Enter remarks (mandatory)",
                                                    color = Color.Gray,
                                                    fontFamily = GraphikFontFamily
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryRed,
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            maxLines = 4
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Switch for Collected/Cancelled
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Row {
                                                CollectedCancelledTabButton(
                                                    text = "Collected",
                                                    isSelected = controller.selectedAction == "collected",
                                                    onClick = { controller.updateSelectedAction("collected") },
                                                    isFirst = true
                                                )
                                                CollectedCancelledTabButton(
                                                    text = "Cancelled",
                                                    isSelected = controller.selectedAction == "cancelled",
                                                    onClick = { controller.updateSelectedAction("cancelled") },
                                                    isLast = true
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Close Order button
                                        Button(
                                            onClick = { controller.onCloseOrder(orderItem.orderId) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp), // Increased height
                                            enabled = !controller.isLoading,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PrimaryRed,
                                                disabledContainerColor = PrimaryRed.copy(alpha = 0.6f)
                                            ),
                                            shape = RoundedCornerShape(28.dp)
                                        ) {
                                            Text(
                                                text = if (controller.selectedAction == "collected") "Close Order" else "Cancel Order",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    
                                    else -> {
                                        // Show remarks text for other orders (rejected, closed, cancelled, etc.)
                                        Text(
                                            text = orderItem.remarks.ifEmpty { "No remarks provided" },
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp,
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

        // Loading overlay
        if (controller.isLoading) {
            UniversalLoader(isLoading = true)
        }
    }
}

@Composable
private fun OrderStatusCard(
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
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "Order Date: ",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = orderDate,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    }
                }
                
                // Status Badge
                val (backgroundColor, textColor) = when (orderStatus.lowercase()) {
                    "pending" -> Pair(Color(0xFFFFA500).copy(alpha = 0.15f), Color(0xFFFFA500)) // Orange
                    "approved" -> Pair(Color(0xFF008000).copy(alpha = 0.15f), Color(0xFF008000)) // Green
                    "rejected", "cancelled" -> Pair(Color(0xFFFF0000).copy(alpha = 0.15f), Color(0xFFFF0000)) // Red
                    "closed" -> Pair(Color(0xFF808080).copy(alpha = 0.15f), Color(0xFF808080)) // Gray
                    else -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray) // Fallback
                }
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Status: $orderStatus",
                        fontSize = 13.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
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
            fontSize = 16.sp, // Increased from 14sp
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = valueWeight,
            fontSize = 16.sp, // Increased from 14sp
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ItemRow(
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
            fontSize = 16.sp, // Increased from 14sp
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = valueWeight,
            fontSize = 16.sp, // Increased from 14sp
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CollectedCancelledTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val shape = when {
        isFirst -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
        isLast -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
        else -> RoundedCornerShape(0.dp)
    }
    
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) Color.White else Color(0xFFE0E0E0)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = GraphikFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

// Date formatting function
private fun formatOrderDateDetails(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        outputFormat.format(date ?: return dateTimeString.substringBefore(" "))
    } catch (e: Exception) {
        dateTimeString.substringBefore(" ")
    }
}