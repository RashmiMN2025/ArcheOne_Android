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
import androidx.compose.ui.graphics.Brush
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.model.GreetingSubcategory
import androidx.compose.foundation.text.BasicTextField

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
                model = imageUrl,
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
) {
    Card(
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
                model = imageUrl,
                contentDescription = "Greeting thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun SubcategoryGrid(
    subcategories: List<GreetingSubcategory>,
    onSubcategoryClick: (GreetingSubcategory) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(subcategories) { subcategory: GreetingSubcategory ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onSubcategoryClick(subcategory) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subcategory.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingsScreen(
    controller: GreetingsController,
    onBackPressed: () -> Unit
) {    
    // Get the status bar padding to avoid overlapping with front camera
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    
    // Get current model state
    val currentSelectedCategory = controller.model.selectedCategory
    val currentSelectedSubcategory = controller.model.selectedSubcategory
    
    // Add BackHandler to handle back swipe gesture
    BackHandler {
        if (currentSelectedSubcategory != null) {
            controller.clearSelectedSubcategory()
        } else if (currentSelectedCategory != null) {
            controller.onBackPressed()
        } else {
            onBackPressed()
        }
    }

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
            Spacer(modifier = Modifier.height(statusBarPadding.calculateTopPadding()))
            
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
                    IconButton(
                        onClick = {
                            if (currentSelectedSubcategory != null) {
                                controller.clearSelectedSubcategory()
                            } else if (currentSelectedCategory != null) {
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
                    
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                currentSelectedSubcategory != null -> currentSelectedSubcategory.name
                                currentSelectedCategory != null -> currentSelectedCategory
                                else -> "Greetings"
                            },
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Move searchQuery state to the top of the Composable
            var searchQuery by remember { mutableStateOf("") }
            LaunchedEffect(controller.model.searchQuery) {
                if (controller.model.searchQuery != searchQuery) {
                    searchQuery = controller.model.searchQuery
                }
            }
            // Only show search bar when not in a category or subcategory
            if (currentSelectedCategory == null && currentSelectedSubcategory == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(width = 1.dp, color = Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { value ->
                                    searchQuery = value
                                    controller.updateSearchQuery(value)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search greetings...",
                                                color = Color.Gray.copy(alpha = 0.6f),
                                                fontSize = 16.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // In the main categories grid, filter categories by local searchQuery
            when {
                currentSelectedCategory == null && currentSelectedSubcategory == null -> {
                    val filteredCategories = remember(searchQuery) {
                        controller.model.categories.keys.filter { it.contains(searchQuery.orEmpty(), ignoreCase = true) }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredCategories) { category ->
                            GreetingCategoryCard(
                                category = category,
                                imageUrl = controller.getCategoryThumbnail(category),
                                onClick = {
                                    controller.onCategoryClick(category)
                                }
                            )
                        }
                    }
                }
                else -> {
                    // Show greetings for selected category or subcategory
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val greetings = if (currentSelectedSubcategory != null) {
                            controller.getGreetingsForSubcategory(currentSelectedSubcategory)
                        } else {
                            controller.getGreetingsForCategory(currentSelectedCategory.toString())
                        }
                        items(greetings) { greetingUrl ->
                            GreetingCard(
                                imageUrl = greetingUrl,
                                onClick = {
                                    controller.onGreetingSelected(greetingUrl)
                                }
                            )
                        }
                    }
                }
            }
        }
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
            model = imageUrl,
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
                model = imageUrl,
                contentDescription = "Greeting detail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
