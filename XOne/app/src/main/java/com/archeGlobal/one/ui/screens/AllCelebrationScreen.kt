package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.HomeController
import com.archeGlobal.one.model.WelcomeBackgroundModel
import com.archeGlobal.one.ui.components.CelebrationItem
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCelebrationScreen(
    controller: HomeController,
    onBackPressed: () -> Unit,
) {
    val celebrationData = controller.celebrationData.collectAsState().value
    var selectedTab by remember { mutableStateOf("Today") }
    val backgroundModel = remember { WelcomeBackgroundModel() }

    // Status bar padding
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    backgroundModel.topColor,
                                    backgroundModel.middleColor,
                                    backgroundModel.bottomColor,
                                ),
                        ),
                ).padding(statusBarPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                    )
                }

                Text(
                    text = "All Cheers For Peers!",
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )

                // Empty space for balance
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Tab selector
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TabButton(
                    text = "Today",
                    isSelected = selectedTab == "Today",
                    onClick = { selectedTab = "Today" },
                )

                TabButton(
                    text = "Tomorrow",
                    isSelected = selectedTab == "Tomorrow",
                    onClick = { selectedTab = "Tomorrow" },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = { /* Handle drag end */ },
                                onHorizontalDrag = { _, dragAmount ->
                                    if (dragAmount > 50) {
                                        // Swiped from left to right - go to Today
                                        selectedTab = "Today"
                                    } else if (dragAmount < -50) {
                                        // Swiped from right to left - go to Tomorrow
                                        selectedTab = "Tomorrow"
                                    }
                                },
                            )
                        },
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                val itemsToShow =
                    if (selectedTab == "Today") {
                        celebrationData?.today ?: emptyList()
                    } else {
                        celebrationData?.tomorrow ?: emptyList()
                    }

                items(itemsToShow) { item ->
                    CelebrationItem(
                        item = item,
                        onWishesClick = { controller.onCelebrationWishesClick(item.email, item.employeeName, item.celebrationType) },
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
            }

            // Page indicator
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                // Today indicator
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .background(
                                color = if (selectedTab == "Today") Color(0xFFDD3825) else Color.Gray.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.CircleShape,
                            ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Tomorrow indicator
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .background(
                                color = if (selectedTab == "Tomorrow") Color(0xFFDD3825) else Color.Gray.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.CircleShape,
                            ),
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isSelected) Color(0xFFFF6B6B) else Color.White,
                contentColor = if (isSelected) Color.White else Color.Gray,
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
