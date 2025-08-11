package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.DeskCartController
import com.archeGlobal.one.model.DeskCartModel
import com.archeGlobal.one.model.StationaryItem
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeskCartScreen(
    model: DeskCartModel,
    controller: DeskCartController
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                DeskCartHeader(
                    onBackPressed = controller::onBackPressed
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Employee Details Section
                    EmployeeDetailsSection(
                        model = model,
                        onAdminDashboardClick = controller::onAdminDashboardClick
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Store Front Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        StoreFrontSection(
                            items = model.stationaryItems,
                            onIncreaseQuantity = controller::onIncreaseQuantity,
                            onDecreaseQuantity = controller::onDecreaseQuantity,
                            onPlaceOrder = controller::onPlaceOrder,
                            isPlaceOrderEnabled = controller.getTotalItemsSelected() > 0
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Spacer(modifier = Modifier.height(20.dp))
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
fun DeskCartHeader(
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Stationary Order",
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
            IconButton(onClick = { /* Handle history */ }) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "History",
                    tint = PrimaryRed,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun EmployeeDetailsSection(
    model: DeskCartModel,
    onAdminDashboardClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Employee Details",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Employee Info Rows
            EmployeeInfoRow(label = "Email ID:", value = model.employeeDetails.emailId)
            EmployeeInfoRow(label = "Employee ID:", value = model.employeeDetails.employeeId)
            EmployeeInfoRow(label = "Department:", value = model.employeeDetails.department)

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Dashboard Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onAdminDashboardClick,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Admin Dashboard",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
fun StoreFrontSection(
    items: List<StationaryItem>,
    onIncreaseQuantity: (StationaryItem) -> Unit,
    onDecreaseQuantity: (StationaryItem) -> Unit,
    onPlaceOrder: () -> Unit,
    isPlaceOrderEnabled: Boolean
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Store Front",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(600.dp) // Fixed height to prevent scrolling issues
        ) {
            items(items) { item ->
                StationaryItemCard(
                    item = item,
                    onIncreaseQuantity = { onIncreaseQuantity(item) },
                    onDecreaseQuantity = { onDecreaseQuantity(item) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Place Order Button inside the card
        PlaceOrderButton(
            onClick = onPlaceOrder,
            enabled = isPlaceOrderEnabled
        )
    }
}

@Composable
fun StationaryItemCard(
    item: StationaryItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Item Icon
            Image(
                painter = painterResource(id = getStationaryIcon(item.iconName)),
                contentDescription = item.name,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            // Item Name
            Text(
                text = item.name,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Quantity Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease button (disabled when quantity is 0)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color.Gray.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                        .clickable(enabled = item.currentQuantity > 0) {
                            onDecreaseQuantity()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quantity Display
                Text(
                    text = "${item.currentQuantity}/${item.maxQuantity}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.Black
                )

                // Increase button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = PrimaryRed,
                            shape = CircleShape
                        )
                        .clickable(enabled = item.currentQuantity < item.maxQuantity) {
                            onIncreaseQuantity()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceOrderButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryRed,
            disabledContainerColor = PrimaryRed
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = "Place Order",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun getStationaryIcon(iconName: String): Int {
    return when (iconName) {
        "ic_pen" -> R.drawable.ic_file
        "ic_pencil" -> R.drawable.ic_file
        "ic_notepad" -> R.drawable.ic_pdf_document
        "ic_marker" -> R.drawable.ic_file
        "ic_envelope_dl" -> R.drawable.ic_doc
        "ic_envelope_a4" -> R.drawable.ic_doc
        "ic_stapler" -> R.drawable.ic_file
        "ic_glue" -> R.drawable.ic_file
        "ic_scissor" -> R.drawable.ic_file
        "ic_tape" -> R.drawable.ic_file
        "ic_punching_machine" -> R.drawable.ic_it_asset
        else -> R.drawable.ic_file
    }
}
