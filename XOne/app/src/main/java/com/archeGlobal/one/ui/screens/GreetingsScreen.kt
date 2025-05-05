package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.GreetingsController
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import androidx.core.graphics.applyCanvas
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.launch

@Composable
fun GreetingsScreen(
    controller: GreetingsController
) {
    // Get the status bar padding to avoid overlapping with front camera
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    
    // Define cardBoxRef at this scope so it's accessible throughout the function
    val cardBoxRef = remember { mutableStateOf<View?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
            // Add status bar padding to push content down
            Spacer(modifier = Modifier.height(statusBarPadding.calculateTopPadding()))
            
            // Top App Bar without using ExperimentalMaterial3Api
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
                    IconButton(
                        onClick = { controller.onBackPressed() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    
                    // Title
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (controller.model.selectedCategory == null) "Greetings" 
                                   else controller.model.selectedCategory!!,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                    
                    // Empty space to balance the layout
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            if (controller.model.selectedCategory == null) {
                // Search Bar (only on main category screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TextField(
                        value = controller.model.searchQuery,
                        onValueChange = { controller.updateSearchQuery(it) },
                        placeholder = { Text("Search for a greeting...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )
                }

                // Show categories grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val filteredCategories = controller.getFilteredCategories()
                    items(filteredCategories) { category ->
                        GreetingCategoryCard(
                            category = category,
                            imageUrl = controller.model.categories[category]?.firstOrNull() ?: "",
                            onClick = { controller.onCategorySelected(category) }
                        )
                    }
                }
            } else {
                // Detailed greeting view layout that matches the reference image
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Horizontal row of smaller greeting thumbnails
                    val greetings = controller.model.categories[controller.model.selectedCategory] ?: emptyList()
                    val selectedGreeting = controller.model.selectedGreeting
                    
                    // Debug logging to verify selections are working
                    android.util.Log.d("GreetingsScreen", "Current category: ${controller.model.selectedCategory}")
                    android.util.Log.d("GreetingsScreen", "Selected greeting: $selectedGreeting")
                    android.util.Log.d("GreetingsScreen", "Available greetings: ${greetings.size}")
                    
                    // Force recomposition when selection changes by using a key
                    key(selectedGreeting) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(greetings) { greetingUrl ->
                                val isSelected = greetingUrl == selectedGreeting
                                // Debug each item
                                android.util.Log.d("GreetingsScreen", "Item URL: $greetingUrl, isSelected: $isSelected")
                                
                                GreetingThumbnailCard(
                                    imageUrl = greetingUrl,
                                    isSelected = isSelected,
                                    onClick = {
                                        android.util.Log.d("GreetingsScreen", "Thumbnail clicked: $greetingUrl")
                                        controller.onGreetingSelected(greetingUrl)
                                    }
                                )
                            }
                        }
                    }
                    
                    // Significantly increased spacing above the card
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Selected greeting card (larger view)
                    controller.model.selectedGreeting?.let { selectedGreeting ->
                        // Additional space above the card
                        Spacer(modifier = Modifier.height(24.dp))

                        // Force recomposition of the main card when the selected greeting changes
                        key(selectedGreeting) {
                            // --- BEGIN: Add key and ref for screenshot capture ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                // Create a state to track if the image is loaded
                                var isImageLoaded by remember { mutableStateOf(false) }
                                
                                AndroidView(
                                    factory = { ctx ->
                                        val imageView = android.widget.ImageView(ctx).apply {
                                            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                            setBackgroundColor(android.graphics.Color.WHITE) // Set white background
                                        }
                                        
                                        // Log the image loading attempt
                                        android.util.Log.d("GreetingsScreen", "Loading image: $selectedGreeting")
                                        
                                        coil.ImageLoader(ctx).enqueue(
                                            coil.request.ImageRequest.Builder(ctx)
                                                .data(selectedGreeting)
                                                .listener(
                                                    onStart = { 
                                                        isImageLoaded = false
                                                        android.util.Log.d("GreetingsScreen", "Started loading: $selectedGreeting")
                                                    },
                                                    onSuccess = { _, _ ->
                                                        isImageLoaded = true
                                                        android.util.Log.d("GreetingsScreen", "Successfully loaded: $selectedGreeting")
                                                    },
                                                    onError = { _, error ->
                                                        android.util.Log.e("GreetingsScreen", "Error loading: ${error.throwable.message}")
                                                    }
                                                )
                                                .target { drawable ->
                                                    imageView.setImageDrawable(drawable)
                                                    // Update reference after drawable is set
                                                    cardBoxRef.value = imageView
                                                    android.util.Log.d("GreetingsScreen", "Image set and reference updated")
                                                }
                                                .build()
                                        )
                                        imageView
                                    },
                                    // Use update callback to handle recompositions without recreating the view
                                    update = { view ->
                                        android.util.Log.d("GreetingsScreen", "AndroidView update callback with: $selectedGreeting")
                                        coil.ImageLoader(view.context).enqueue(
                                            coil.request.ImageRequest.Builder(view.context)
                                                .data(selectedGreeting)
                                                .target { drawable ->
                                                    view.setImageDrawable(drawable)
                                                    // Update reference after drawable is set
                                                    cardBoxRef.value = view
                                                    android.util.Log.d("GreetingsScreen", "Image updated in existing view")
                                                }
                                                .build()
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.55f)
                                        .aspectRatio(0.75f)
                                        .background(Color.White) // Add white background to Compose element too
                                )
                                
                                // Show loading indicator if the image is not loaded yet
                                if (!isImageLoaded) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = Color(0xFFDD3825)
                                    )
                                }
                            }
                        }
                        // --- END: Add key and ref for screenshot capture ---

                        // Increased space below the card
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Additional spacing before the "Add Message" section
                    Spacer(modifier = Modifier.height(34.dp))
                    
                    // Message input field
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Add Message",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Text field with rounded corners and proper styling like in the image
                        OutlinedTextField(
                            value = controller.model.message,
                            onValueChange = { controller.updateMessage(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                lineHeight = 24.sp
                            ),
                            minLines = 5
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    // When the Send Greeting button is clicked, capture the card as a bitmap and send
                                    val cardView = cardBoxRef.value
                                    if (cardView != null) {
                                        try {
                                            // Check if the view is an ImageView (which it should be)
                                            if (cardView is android.widget.ImageView && cardView.drawable != null) {
                                                // Get the drawable from the ImageView
                                                val drawable = cardView.drawable
                                                
                                                // Create a properly sized bitmap matching the drawable's intrinsic size
                                                // or the view size if intrinsic size is not available
                                                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: cardView.width
                                                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: cardView.height
                                                
                                                android.util.Log.d("GreetingsScreen", "Capturing image with size: $width x $height")
                                                
                                                if (width > 0 && height > 0) {
                                                    // Create bitmap with proper size
                                                    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                                                    
                                                    // Get canvas and set bounds
                                                    val canvas = android.graphics.Canvas(bitmap)
                                                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                                                    
                                                    // Draw the actual drawable content (not just the view)
                                                    drawable.draw(canvas)
                                                    
                                                    // Check if the bitmap contains actual content
                                                    var hasContent = false
                                                    for (x in 0 until width) {
                                                        for (y in 0 until height) {
                                                            if (bitmap.getPixel(x, y) != android.graphics.Color.TRANSPARENT && 
                                                                bitmap.getPixel(x, y) != android.graphics.Color.WHITE) {
                                                                hasContent = true
                                                                break
                                                            }
                                                        }
                                                        if (hasContent) break
                                                    }
                                                    
                                                    if (hasContent) {
                                                        android.util.Log.d("GreetingsScreen", "Bitmap has content, sending greeting")
                                                        controller.setCardScreenshot(bitmap)
                                                        controller.sendGreeting()
                                                    } else {
                                                        // Fallback: try to download the image directly
                                                        android.util.Log.d("GreetingsScreen", "Bitmap is empty, using direct download approach")
                                                        controller.downloadAndShareImage(controller.model.selectedGreeting ?: "")
                                                    }
                                                } else {
                                                    // Fallback for no dimensions
                                                    controller.downloadAndShareImage(controller.model.selectedGreeting ?: "")
                                                }
                                            } else {
                                                // Fallback for non-imageview or no drawable
                                                controller.downloadAndShareImage(controller.model.selectedGreeting ?: "")
                                            }
                                        } catch (e: Exception) {
                                            // Log exception and fall back to direct download approach
                                            android.util.Log.e("GreetingsScreen", "Error capturing card: ${e.message}")
                                            controller.downloadAndShareImage(controller.model.selectedGreeting ?: "")
                                        }
                                    } else {
                                        // No card view, fallback to direct URL
                                        controller.downloadAndShareImage(controller.model.selectedGreeting ?: "")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Send Greeting", 
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { 
                                    // When the Send in Outlook button is clicked, we share via Outlook
                                    controller.sendInOutlook()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Send in Outlook",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Add extra space at the bottom for better scrolling
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingCategoryCard(
    category: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Added padding to increase effective width slightly
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image card with adjusted height
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)  // Reduced height from 250dp to 200dp
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = category,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Text below the card
        Text(
            text = category,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GreetingCard(
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Greeting",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun GreetingThumbnailCard(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(80.dp, 107.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        border = if (isSelected) 
                    BorderStroke(2.dp, Color(0xFFDD3825)) 
                else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Greeting thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun GreetingDetailCard(
    imageUrl: String,
    modifier: Modifier = Modifier
            .fillMaxWidth(0.55f)
            .aspectRatio(0.75f)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Greeting detail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
