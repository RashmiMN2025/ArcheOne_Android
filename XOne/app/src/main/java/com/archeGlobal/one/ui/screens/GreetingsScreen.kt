package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.GreetingsController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveGreetingsScreen(
    controller: GreetingsController,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val (columns, cardWidth) = when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2 to 160.dp // Phone
        WindowWidthSizeClass.Medium -> 3 to 180.dp // Large phone/small tablet
        WindowWidthSizeClass.Expanded -> 4 to 220.dp // Tablet
        else -> 2 to 160.dp
    }
    GreetingsScreen(
        controller = controller,
        onBackPressed = onBackPressed,
        columns = columns,
        cardWidth = cardWidth
    )
}

@Composable
fun GreetingCategoryCard(
    category: String,
    imageUrl: String,
    onClick: () -> Unit,
    cardWidth: Dp = 160.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(cardWidth)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(0.8f)
        ) {
            Card(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = 2.dp, color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = category,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 14.sp,
            maxLines = 2,
            lineHeight = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun GreetingThumbnailCard(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = if (isSelected) RoundedCornerShape(12.dp) else RectangleShape
    val scale by animateFloatAsState(if (isSelected) 1.07f else 1f, label = "hover-scale")
    val borderWidth = if (isSelected) (2.dp / scale) else 0.dp // Thicker border, visually consistent

    Box(
        modifier = Modifier
            .size(80.dp, 100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = 0f // No shadow
                this.shape = shape
                clip = true
            }
            .border(
                width = borderWidth,
                color = if (isSelected) Color(0xFFDD3825) else Color.Transparent,
                shape = shape
            )
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            elevation = CardDefaults.cardElevation(0.dp), // No Card shadow
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
}

@Composable
fun GreetingsScreen(
    controller: GreetingsController,
    onBackPressed: () -> Unit,
    columns: Int = 2,
    cardWidth: Dp = 160.dp
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
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
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
                                fontWeight = FontWeight.Bold,
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
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.search11),
                                    contentDescription = "Search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
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
                                                    text = "Search for a greeting...",
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

                // Place this at the top of your Composable (inside GreetingsScreen)
                var selectedGreeting by remember { mutableStateOf<String?>(null) }

                // In the main categories grid, filter categories by local searchQuery
                when {
                    currentSelectedCategory == null && currentSelectedSubcategory == null -> {
                        val filteredCategories = remember(searchQuery) {
                            controller.model.categories.keys.filter {
                                it.contains(
                                    searchQuery.orEmpty(),
                                    ignoreCase = true
                                )
                            }
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
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
                                    },
                                    cardWidth = cardWidth
                                )
                            }
                        }
                    }

                    else -> {
                        // Show greetings for selected category or subcategory
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
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
                                    isSelected = selectedGreeting == greetingUrl,
                                    onClick = {
                                        selectedGreeting = greetingUrl
                                        controller.onGreetingSelected(greetingUrl)
                                    },
                                    cardWidth = cardWidth
                                )
                            }
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
    isSelected: Boolean,
    onClick: () -> Unit,
    cardWidth: Dp = 160.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(cardWidth)
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(0.8f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFFDD3825) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Greeting",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
