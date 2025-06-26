package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingDetailScreen(
    imageUrl: String,
    categoryGreetings: List<String>,
    selectedGreetingUrl: String,
    message: String,
    category: String,
    onMessageChanged: (String) -> Unit,
    onGreetingSelected: (String) -> Unit,
    onBackPressed: () -> Unit,
    onSendGreeting: () -> Unit,
    onSendInOutlook: (String, String) -> Unit
) {
    // Track the currently selected greeting
    var currentSelectedGreeting by remember { mutableStateOf(selectedGreetingUrl) }
    // Status bar padding to avoid overlapping with front camera
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Add status bar padding to push content down
            Spacer(modifier = Modifier.height(statusBarPadding.calculateTopPadding()))

            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    // Title
                    Text(
                        text = category,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    // Empty space for balance
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Horizontal row of greeting thumbnails
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoryGreetings) { greetingUrl ->
                    GreetingThumbnailCard(
                        imageUrl = greetingUrl,
                        isSelected = greetingUrl == currentSelectedGreeting,
                        onClick = {
                            currentSelectedGreeting = greetingUrl
                            onGreetingSelected(greetingUrl)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            // Main greeting card display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) { // Use a Box with AsyncImage directly instead of Card with white background
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f) // Made even smaller (reduced from 0.85f)
                        .aspectRatio(0.75f) // Keep the same aspect ratio
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentSelectedGreeting)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Greeting detail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Message section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Add Message",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Editable message field
                val messageScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(150.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(6.dp)
                        .align(Alignment.CenterHorizontally) // <-- Add this line
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = onMessageChanged,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(messageScroll),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = GraphikFontFamily,
                            fontSize = 14.sp,
                            color = Color.Black
                        ),
                        maxLines = Int.MAX_VALUE,
                        singleLine = false,
                        decorationBox = { innerTextField ->
                            if (message.isEmpty()) {
                                Text(
                                    text = "Enter your message...",
                                    color = Color.LightGray,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Send Greeting button
                    Button(
                        onClick = onSendGreeting,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Send Greeting",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Send in Outlook button
                    Button(
                        onClick = { onSendInOutlook(currentSelectedGreeting, message) },
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Send in Outlook",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}
