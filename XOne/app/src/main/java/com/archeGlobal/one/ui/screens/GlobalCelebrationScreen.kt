package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.archeGlobal.one.controller.GlobalCelebrationController
import com.archeGlobal.one.model.GreetingSubcategory
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveGlobalCelebrationScreen(
    controller: GlobalCelebrationController,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val (columns, cardWidth) = when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2 to 160.dp   // Phone
        WindowWidthSizeClass.Medium -> 3 to 180.dp    // Large phone/small tablet
        WindowWidthSizeClass.Expanded -> 4 to 220.dp  // Tablet
        else -> 2 to 160.dp
    }
    GlobalCelebrationScreen(
        controller = controller,
        onBackPressed = onBackPressed,
        columns = columns,
        cardWidth = cardWidth
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalCelebrationScreen(
    controller: GlobalCelebrationController,
    onBackPressed: () -> Unit,
    columns: Int = 2,
    cardWidth: Dp = 160.dp
) {
    // Status bar padding to avoid overlapping with front camera
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    // Handle back button press
    BackHandler {
        controller.onBackPressed()
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

            // Top App Bar with Back Button and Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Global Celebration",
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

            // Search Bar (updated logic: local state, update controller on change)
            var searchQuery by remember { mutableStateOf("") }
            LaunchedEffect(controller.model.searchQuery) {
                if (controller.model.searchQuery != searchQuery) {
                    searchQuery = controller.model.searchQuery
                }
            }
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
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                            text = "Search celebration...",
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

            // Filter subcategories locally using searchQuery
            val filteredSubcategories = remember(searchQuery) {
                controller.model.subcategories.filter {
                    it.name.contains(searchQuery.orEmpty(), ignoreCase = true)
                }
            }
            GlobalCelebrationSubcategoriesGrid(
                subcategories = filteredSubcategories,
                onSubcategoryClick = { subcategory ->
                    controller.onSubcategorySelected(subcategory)
                },
                columns = columns,
                cardWidth = cardWidth
            )
        }
    }
}

@Composable
fun GlobalCelebrationSubcategoriesGrid(
    subcategories: List<GreetingSubcategory>,
    onSubcategoryClick: (GreetingSubcategory) -> Unit,
    columns: Int = 2,
    cardWidth: Dp = 160.dp
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(subcategories) { subcategory ->
            GlobalCelebrationSubcategoryCard(
                subcategory = subcategory,
                onClick = { onSubcategoryClick(subcategory) },
                cardWidth = cardWidth
            )
        }
    }
}

@Composable
fun GlobalCelebrationSubcategoryCard(
    subcategory: GreetingSubcategory,
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
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                if (subcategory.files.isNotEmpty()) {
                    AsyncImage(
                        model = subcategory.files.first(),
                        contentDescription = subcategory.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subcategory.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subcategory.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
