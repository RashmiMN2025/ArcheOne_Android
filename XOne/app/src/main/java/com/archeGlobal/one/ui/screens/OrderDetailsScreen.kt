package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.OrderController
import com.archeGlobal.one.model.OrderDetails
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
                    // Header
                    OrderDetailsHeader(
                        orderId = orderDetails.orderId,
                        onBackPressed = { controller.onBackPressed() }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Order Details Section Title
                        Text(
                            text = "Order Details",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Order Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                OrderDetailRow(
                                    label = "Order ID:",
                                    value = orderDetails.orderId
                                )

                                OrderDetailRow(
                                    label = "Employee ID:",
                                    value = orderDetails.employeeId
                                )

                                OrderDetailRow(
                                    label = "Name:",
                                    value = orderDetails.employeeName
                                )

                                OrderDetailRow(
                                    label = "Department:",
                                    value = orderDetails.department
                                )

                                OrderDetailRow(
                                    label = "Email:",
                                    value = orderDetails.email
                                )

                                OrderDetailRow(
                                    label = "Order Status:",
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
                                    label = "Order Date:",
                                    value = orderDetails.orderDate
                                )

                                OrderDetailRow(
                                    label = "Order Time:",
                                    value = orderDetails.orderTime
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Order Items Section Title
                        Text(
                            text = "Order Items",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Order Items Cards
                        orderDetails.orderItems.forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.itemName,
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Qty: ${item.quantity}",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

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
                                        containerColor = PrimaryRed,
                                        disabledContainerColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(12.dp),
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
                                        containerColor = Color(0xFF4CAF50),
                                        disabledContainerColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(12.dp),
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
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailsHeader(
    orderId: String,
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order $orderId",
                    color = Color.Black,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = (-12).dp)
                )
            }
        },
        navigationIcon = {
            TextButton(
                onClick = onBackPressed,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF007AFF))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Back",
                        fontSize = 17.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF007AFF)
                    )
                }
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

private fun getItemIcon(itemName: String): Int {
    return when (itemName.lowercase()) {
        "pen" -> R.drawable.ic_file
        "notepad" -> R.drawable.ic_file "marker" -> R.drawable.ic_file
        "envelope dl" -> R.drawable.ic_file
        "scissor" -> R.drawable.ic_file
        "tape" -> R.drawable.ic_file
        "glue" -> R.drawable.ic_file
        "stapler" -> R.drawable.ic_file
        "pencil" -> R.drawable.ic_file
        else -> R.drawable.ic_file
    }
}
