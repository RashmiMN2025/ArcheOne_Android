package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.OrderController
import com.archeGlobal.one.model.OrderDetails
import com.archeGlobal.one.model.OrderItem
import com.archeGlobal.one.model.OrderStatus
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.utils.FontScaleAdjusted
import com.archeGlobal.one.utils.getDeviceSpecificFontAdjustment
import kotlinx.coroutines.delay

@Composable
fun OrderDetailsScreen(
    controller: OrderController,
    orderDetails: OrderDetails
) {
    val context = LocalContext.current
    val fontAdjustment = remember { getDeviceSpecificFontAdjustment(context) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Observe the approval action state from the controller
    val approvalActionState = controller.approvalActionState

    // Handle approval action state changes
    LaunchedEffect(approvalActionState) {
        when (approvalActionState) {
            is com.archeGlobal.one.model.OrderApprovalActionState.Loading -> {
                isLoading = true
                errorMessage = null
                successMessage = null
            }
            is com.archeGlobal.one.model.OrderApprovalActionState.Success -> {
                isLoading = false
                errorMessage = null
                successMessage = approvalActionState.message
                delay(1500) // Give user time to see the success state
                controller.navigateBack()
            }
            is com.archeGlobal.one.model.OrderApprovalActionState.Error -> {
                isLoading = false
                errorMessage = approvalActionState.message
                successMessage = null
            }
            else -> {
                isLoading = false
                errorMessage = null
                successMessage = null
            }
        }
    }

    FontScaleAdjusted(fontScaleAdjustment = fontAdjustment) {
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
                    Spacer(modifier = Modifier.height(48.dp))

                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Order ${orderDetails.orderId}",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = (-24).dp) // Offset for header centering without actions
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { controller.onBackPressed() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        
                        // Main content card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 2.dp,
                            backgroundColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .padding(16.dp)
                            ) {
                                // Order Details Section
                                OrderDetailsSectionHeader(title = "Order Details")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.folder_3x,
                                    label = "Order ID",
                                    value = orderDetails.orderId
                                )
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.person_badge_clock,
                                    label = "Employee ID",
                                    value = orderDetails.employeeId
                                )
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.person_3x,
                                    label = "Name",
                                    value = orderDetails.employeeName
                                )
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.building1,
                                    label = "Department",
                                    value = orderDetails.department
                                )
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.envelope_3x,
                                    label = "Email",
                                    value = orderDetails.email
                                )
                                
                                OrderDetailRow(
                                    iconRes = when (orderDetails.orderStatus) {
                                        OrderStatus.PENDING -> R.drawable.pending
                                        OrderStatus.APPROVED -> R.drawable.approved
                                        OrderStatus.REJECTED -> R.drawable.rejected
                                        OrderStatus.PROCESSING -> R.drawable.pending
                                        OrderStatus.COMPLETED -> R.drawable.approved
                                        OrderStatus.CANCELLED -> R.drawable.rejected
                                    },
                                    label = "Order Status",
                                    value = when (orderDetails.orderStatus) {
                                        OrderStatus.PENDING -> "Pending"
                                        OrderStatus.APPROVED -> "Approved"
                                        OrderStatus.REJECTED -> "Rejected"
                                        OrderStatus.PROCESSING -> "Processing"
                                        OrderStatus.COMPLETED -> "Completed"
                                        OrderStatus.CANCELLED -> "Cancelled"
                                    }
                                )
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.calendar_3x,
                                    label = "Order Date",
                                    value = orderDetails.orderDate
                                )
                                
                                OrderDetailRow(
                                    iconRes = R.drawable.calendar_3x, // Using calendar icon for time as well
                                    label = "Order Time",
                                    value = orderDetails.orderTime
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Order Items Section
                                OrderDetailsSectionHeader(title = "Order Items")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                orderDetails.orderItems.forEach { item ->
                                    OrderItemRow(item = item)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Error/Success Messages
                                errorMessage?.let { error ->
                                    Text(
                                        text = error,
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }

                                successMessage?.let { success ->
                                    Text(
                                        text = success,
                                        color = Color(0xFF4CAF50),
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action Buttons - Only show for pending orders
                        if (orderDetails.orderStatus == OrderStatus.PENDING) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Reject Button
                                Button(
                                    onClick = {
                                        controller.rejectOrder(orderDetails.orderId, "")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = PrimaryRed,
                                        disabledBackgroundColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Reject",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                // Approve Button
                                Button(
                                    onClick = {
                                        controller.approveOrder(orderDetails.orderId, "")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF4CAF50),
                                        disabledBackgroundColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Approve",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = GraphikFontFamily,
        color = Color.Black
    )
}

@Composable
private fun OrderDetailRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = getItemIcon(item.itemName)),
            contentDescription = item.itemName,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.itemName,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Black,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${item.quantity}",
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

private fun getItemIcon(itemName: String): Int {
    return when (itemName.lowercase()) {
        "pen" -> R.drawable.ic_file
        "notepad" -> R.drawable.ic_file  
        "marker" -> R.drawable.ic_file
        "envelope dl" -> R.drawable.ic_file
        "scissor" -> R.drawable.ic_file
        "tape" -> R.drawable.ic_file
        "glue" -> R.drawable.ic_file
        "stapler" -> R.drawable.ic_file
        "pencil" -> R.drawable.ic_file
        else -> R.drawable.ic_file
    }
}