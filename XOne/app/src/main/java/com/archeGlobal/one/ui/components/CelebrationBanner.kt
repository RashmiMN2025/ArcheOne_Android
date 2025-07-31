package com.archeGlobal.one.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.R
import com.archeGlobal.one.model.CelebrationItem
import com.archeGlobal.one.model.CelebrationResponse
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.XOneTheme

@Composable
fun CelebrationBanner(
    celebrationData: CelebrationResponse?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCelebrations = (celebrationData?.today?.size ?: 0) + (celebrationData?.tomorrow?.size ?: 0)

    if (totalCelebrations > 0) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = Color(0x1ADD3825),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cele),
                    contentDescription = "Celebration Pin",
                    tint = Color(0xFFDD3825),
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Cheers to our peers' birthdays and work anniversaries—here's to celebrating your joy and achievements!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.rightpin),
                    contentDescription = "Celebration Pin",
                    tint = Color(0xFFDD3825),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CelebrationDialog(
    celebrationData: CelebrationResponse?,
    onDismiss: () -> Unit,
    onWishesClick: (String, String, String) -> Unit,
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Today") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Empty space for balance
                    Spacer(modifier = Modifier.width(48.dp))

                    Text(
                        text = "Cheers For Peers!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        text = "Today",
                        isSelected = selectedTab == "Today",
                        onClick = { selectedTab = "Today" }
                    )

                    TabButton(
                        text = "Tomorrow",
                        isSelected = selectedTab == "Tomorrow",
                        onClick = { selectedTab = "Tomorrow" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Get items for current tab
                val allItemsForTab = if (selectedTab == "Today") {
                    celebrationData?.today ?: emptyList()
                } else {
                    celebrationData?.tomorrow ?: emptyList()
                }

                // Content
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    // Show only first 3 items
                    val itemsToShow = allItemsForTab.take(3)

                    items(itemsToShow) { item ->
                        CelebrationItem(
                            item = item,
                            onWishesClick = { onWishesClick(item.email, item.employeeName, item.celebrationType) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // View All button - only show if current tab has more than 3 items
                if (allItemsForTab.size > 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = onViewAllClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFDD3825),
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, Color(0xFFDD3825)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "View All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = GraphikFontFamily,
                                color = Color(0xFFDD3825)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Page indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Today indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (selectedTab == "Today") Color(0xFFDD3825) else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tomorrow indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (selectedTab == "Tomorrow") Color(0xFFDD3825) else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFF6B6B) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = GraphikFontFamily
        )
    }
}

@Composable
fun CelebrationItem(
    item: CelebrationItem,
    onWishesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Gray, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.profilePic.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Show default icon as background
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )

                            // Load actual profile image on top
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.profilePic)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.employeeName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        color = Color.Black
                    )

                    Text(
                        text = item.celebrationType,
                        fontSize = 12.sp,
                        fontFamily = GraphikFontFamily,
                        color = Color(0xFF007AFF)
                    )
                }
            }

            // Wishes button
            Button(
                onClick = onWishesClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "WISHES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GraphikFontFamily,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CelebrationBannerPreview() {
    XOneTheme {
        CelebrationBanner(
            celebrationData = null,
            onClick = {}
        )
    }
}
