package com.example.xone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.model.HomeModel
import com.example.xone.model.HomeItem
import com.example.xone.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ProfileHeader(
    model: HomeModel,
    onShowProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFDD3825),
                        Color(0xFFB82D1C)
                    )
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(11.dp),
                        tint = Color(0xFFDD3825)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = model.userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = model.designation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = model.department,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    model: HomeModel,
    onItemClick: (String) -> Unit,
    onAllAppsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onShowProfileClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        ProfileHeader(
            model = model,
            onShowProfileClick = onShowProfileClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            color = SearchBarBackground
        ) {
            OutlinedTextField(
                value = model.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                placeholder = { 
                    Text(
                        "Search apps",
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    cursorColor = PrimaryBlue
                ),
                singleLine = true
            )
        }

        // Toggle Buttons
        if (model.searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAllAppsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (model.showAllApps) 
                            Color(0xFFDD3825) else CardBackground,
                        contentColor = if (model.showAllApps) 
                            Color.White else TextSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (model.showAllApps) Color(0xFFDD3825) else DividerColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("All Apps")
                }
                Button(
                    onClick = onFavoritesClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (model.viewFavorites) 
                            Color(0xFFDD3825) else CardBackground,
                        contentColor = if (model.viewFavorites) 
                            Color.White else TextSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (model.viewFavorites) Color(0xFFDD3825) else DividerColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Favorites")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.weight(1f)) {
            if (model.searchQuery.isNotEmpty()) {
                // Search Results
                if (model.filteredApps.isEmpty()) {
                    // Show "No results found" message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps found",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(model.filteredApps.chunked(3)) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    AppItem(
                                        title = item.title,
                                        isFavorite = item.isFavorite,
                                        onClick = { onItemClick(item.title) },
                                        onFavoriteClick = { onToggleFavorite(item.title) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } else if (model.showAllApps) {
                // All Apps View with Categories
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    model.categories.forEach { (category, items) ->
                        item {
                            CategoryHeader(
                                title = category,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(items.chunked(3)) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    AppItem(
                                        title = item.title,
                                        isFavorite = item.isFavorite,
                                        onClick = { onItemClick(item.title) },
                                        onFavoriteClick = { onToggleFavorite(item.title) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } else if (model.viewFavorites) {
                // Favorites View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(model.favorites.chunked(3)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                AppItem(
                                    title = item.title,
                                    isFavorite = item.isFavorite,
                                    onClick = { onItemClick(item.title) },
                                    onFavoriteClick = { onToggleFavorite(item.title) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Default Apps View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(model.defaultApps.chunked(3)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                AppItem(
                                    title = item.title,
                                    isFavorite = item.isFavorite,
                                    onClick = { onItemClick(item.title) },
                                    onFavoriteClick = { onToggleFavorite(item.title) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItem(
    title: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = if (isPressed) 0.95f else 1f
                scaleY = if (isPressed) 0.95f else 1f
            }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // App content in center
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppIcon(title = title, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Replace heart icon with plus/check icon in a circle
            Surface(
                shape = CircleShape,
                color = if (isFavorite) Color(0xFFDD3825).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(19.dp)
                    .clickable(onClick = onFavoriteClick)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFDD3825) else Color.Gray,
                    modifier = Modifier
                        .padding(3.dp)
                        .size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun AppIcon(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = getColorForApp(title).copy(alpha = 0.1f)
        ) {
            Box {
                // Pattern overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pattern = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, size.height)
                        moveTo(size.width, 0f)
                        lineTo(0f, size.height)
                    }
                    drawPath(
                        path = pattern,
                        color = Color.White.copy(alpha = 0.1f),
                        style = Stroke(width = 1f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = getColorForApp(title),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                thickness = 2.dp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun getColorForApp(title: String): Color {
    return when (title) {
        "ID" -> Color(0xFF1c5c89)
        "Asset" -> Color(0xFFb33d1f)
        "Timesheet" -> Color(0xFF999900)
        "Leave" -> Color(0xFFb67f3e)
        "MyDocuments" -> Color(0xFF7c4c91)
        "My Career" -> Color(0xFFC71585)
        "eLearning" -> Color(0xFF2a7aad)
        "Goal Setting/KPI" -> Color(0xFFa34200)
        "XCard" -> Color(0xFF007A78)
        "Medical" -> Color(0xFFc67817)
        "Finance" -> Color(0xFF2a3a4b)
        "Admin" -> Color(0xFF7c4c91)
        "HR" -> Color(0xFF4a8c38)
        "Holiday Calendar" -> Color(0xFFcc4629)
        "Client Calendar" -> Color(0xFF1c5c89)
        "Greetings" -> Color(0xFF696969)
        "XConnect" -> Color(0xFF2981cc)
        "Locations" -> Color(0xFF800020)
        "Helpdesk" -> Color(0xFF12806a)
        "Announcements" -> Color(0xFFff6347)
        "XProfile" -> Color(0xFF00bfff)
        "Password Reset" -> Color(0xFF8B008B)
        "Policy" -> Color(0xFF123456)
        "SOS" -> Color(0xFFFF0000)
        "Travel & Expenses" -> Color(0xFF4B0082)
        "SAP" -> Color(0xFF0000CD)
        else -> Color(0xFF091857)
    }
} 