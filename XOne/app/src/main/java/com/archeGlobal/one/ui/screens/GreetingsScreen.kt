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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.core.graphics.applyCanvas
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.launch
import coil.ImageLoader

fun processCardView(
    cardView: android.view.View?,
    controller: GreetingsController,
    onError: () -> Unit
) {
    if (cardView == null) {
        onError()
        return
    }
    try {
        if (cardView is android.widget.ImageView && cardView.drawable != null) {
            val drawable = cardView.drawable
            val width = drawable.intrinsicWidth.takeIf { value -> value > 0 } ?: cardView.width
            val height = drawable.intrinsicHeight.takeIf { value -> value > 0 } ?: cardView.height
            android.util.Log.d("GreetingsScreen", "Capturing image with size: $width x $height")
            if (width > 0 && height > 0) {
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
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
                    return
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("GreetingsScreen", "Error capturing card: ${e.message}")
    }
    onError()
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
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
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
        Text(
            text = category,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GreetingThumbnailCard(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {    Card(
        modifier = Modifier
            .size(80.dp, 107.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFFDD3825) else Color.Transparent, // Changed to red
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = if (isSelected) CardDefaults.cardElevation(6.dp) else CardDefaults.cardElevation(2.dp)
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
fun GreetingsScreen(
    controller: GreetingsController,
    onBackPressed: () -> Unit
) {    // Get the status bar padding to avoid overlapping with front camera
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    
    // Define cardBoxRef at this scope so it's accessible throughout the function
    val cardBoxRef = remember { mutableStateOf<View?>(null) }
    
    // Add a key to force recomposition when the selected category changes
    val currentSelectedCategory = controller.model.selectedCategory
    key(currentSelectedCategory) {
      // Default messages are now handled by the controller based on category selection
    
    // Add BackHandler to handle back swipe gesture
    BackHandler {
        // Use the same back navigation logic as the back button
        if (controller.model.selectedCategory != null) {
            controller.onBackPressed()
        } else {
            onBackPressed()
        }
    }
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
                        onClick = { 
                            android.util.Log.d("GreetingsScreen", "Back button pressed")
                            // Check if we're in a category view. If yes, go back to main categories,
                            // otherwise navigate to home screen
                            if (controller.model.selectedCategory != null) {
                                controller.onBackPressed()
                            } else {
                                onBackPressed()
                            }
                        }
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
                }                // Show categories grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val filteredCategories = controller.getFilteredCategories()
                    items(items = filteredCategories) { category ->
                        // Wrap with key to force recomposition when selection changes
                        key(category) {                            val context = LocalContext.current
                            GreetingCategoryCard(
                                category = category,
                                imageUrl = controller.model.categories[category]?.firstOrNull() ?: "",
                                onClick = { 
                                    android.util.Log.d("GreetingsScreen", "Category card clicked: $category")
                                    // Check if we have greeting data for this category
                                    val greetingsList = controller.model.categories[category]
                                    if (greetingsList.isNullOrEmpty()) {
                                        android.util.Log.e("GreetingsScreen", "No greetings found for category: $category")
                                        // Store context safely before the lambda
                                        val appContext = context.applicationContext
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {                                            android.widget.Toast.makeText(
                                                appContext,
                                                "No greeting cards available for this category", 
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        android.util.Log.d("GreetingsScreen", "Category has ${greetingsList.size} greetings")
                                          // Call onCategorySelected with navigateToDetail=true to immediately show detail view
                                        controller.onCategorySelected(category, navigateToDetail = true)
                                        
                                        // Force a UI update immediately after selection
                                        controller.updateMessage(controller.model.message)
                                        
                                        // Double-check after a small delay to ensure UI updated
                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                            if (controller.model.selectedCategory == category) {
                                                android.util.Log.d("GreetingsScreen", "Category selected successfully: ${controller.model.selectedCategory}")
                                                android.util.Log.d("GreetingsScreen", "Selected greeting: ${controller.model.selectedGreeting}")
                                            } else {
                                                android.util.Log.e("GreetingsScreen", "Selection failed! Forcing reselection.")
                                                controller.onCategorySelected(category, navigateToDetail = true)
                                            }
                                        }, 100)
                                    }
                                }
                            )                        }
                    }
                }
            } else {
                // Detailed greeting view layout that matches the reference image
                // Added verticalScroll to make the content scrollable
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Horizontal row of smaller greeting thumbnails
                    val greetings = controller.model.categories[controller.model.selectedCategory] ?: emptyList()
                    val currentSelectedGreeting = controller.model.selectedGreeting // Renamed from selectedGreeting to avoid shadowing

                    // Debug logging to verify selections are working
                    android.util.Log.d("GreetingsScreen", "Current category: ${controller.model.selectedCategory}")
                    android.util.Log.d("GreetingsScreen", "Selected greeting: $currentSelectedGreeting")
                    android.util.Log.d("GreetingsScreen", "Available greetings: ${greetings.size}")

                    // Force recomposition when selection changes by using a key
                    key(currentSelectedGreeting) { // Use the renamed variable
                        val appContext = LocalContext.current.applicationContext
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(greetings) { greetingUrl ->
                                val isSelected = greetingUrl == currentSelectedGreeting // Use the renamed variable                                // Debug each item
                                android.util.Log.d("GreetingsScreen", "Item URL: $greetingUrl, isSelected: $isSelected")
                                GreetingThumbnailCard(
                                    imageUrl = greetingUrl,
                                    isSelected = isSelected,
                                    onClick = {
                                        android.util.Log.d("GreetingsScreen", "Thumbnail clicked: $greetingUrl")
                                        // Skip if already selected
                                        if (!isSelected) {
                                            // Use the captured context in the handler
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                try {
                                                    if (android.os.Build.VERSION.SDK_INT >= 31) { // Android 12+
                                                        val vibratorManager = appContext.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                                                        val vibrator = vibratorManager?.defaultVibrator
                                                        if (vibrator != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                            // vibrator.vibrate(android.os.VibrationEffect.createOneShot(10, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                                        }
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        val vibrator = appContext.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                                        if (vibrator != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                            // vibrator.vibrate(android.os.VibrationEffect.createOneShot(10, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    // Ignore vibration errors
                                                }
                                            }
                                            // Select the greeting
                                            controller.onGreetingSelected(greetingUrl)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                // Reduced spacing between selector and card since we have scrolling now
                Spacer(modifier = Modifier.height(16.dp))                // Selected greeting card (larger view)
                controller.model.selectedGreeting?.let { greeting ->
                    // Force recomposition of the main card when the selected greeting changes
                    key(greeting) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable {
                                    // Navigate to detail view when the main card is clicked
                                    android.util.Log.d("GreetingsScreen", "Main card clicked: $greeting")
                                    controller.onGreetingSelected(greeting)
                                }
                        ) {
                            val context = LocalContext.current
                            val greetingState = remember(greeting) { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
                            AndroidView(
                                factory = { ctx ->
                                    android.widget.ImageView(ctx).apply {
                                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setImageResource(android.R.drawable.stat_notify_sync)
                                    }
                                },
                                update = { view ->
                                    greetingState.value?.let { view.setImageDrawable(it) }
                                    cardBoxRef.value = view
                                }
                            )
                            LaunchedEffect(greeting) {
                                coil.ImageLoader(context).enqueue(
                                    coil.request.ImageRequest.Builder(context)
                                        .data(greeting)
                                        .target(
                                            onStart = {
                                                greetingState.value = null
                                            },
                                            onSuccess = { result ->
                                                greetingState.value = result
                                            }
                                        )
                                        .build()
                                )
                            }
                        }
                    }
                    // Increased space below the card
                    Spacer(modifier = Modifier.height(40.dp))
                }
                // Reduced spacing before the "Add Message" section since we have scrolling now
                Spacer(modifier = Modifier.height(100.dp))
                
                // Message input field
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {                        // Simple header for message section - Templates dropdown removed
                    Text(
                        text = "Add Message",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
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
                                processCardView(cardView, controller) {
                                    // Fallback to direct URL
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
    } // Close key block for currentSelectedCategory




@Composable
fun GreetingCard(
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        // onClick = onClick, // Removed this line
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Added .clickable modifier here
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
}
