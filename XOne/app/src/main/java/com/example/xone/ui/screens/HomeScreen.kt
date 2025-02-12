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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Email
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
import com.example.xone.model.FooterNavigationModel
import com.example.xone.ui.theme.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.xone.model.WelcomeBackgroundModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.xone.R
import com.example.xone.ui.theme.getColorForApp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ProfileHeader(
    model: HomeModel,
    onShowProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF808080),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(start = 27.dp, end = 16.dp, top = 72.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = Color(0xFF808080)
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Text(
                        text = model.userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = model.designation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = model.department,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = model.employeeId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.Medium
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
    onItemClick: (HomeItem) -> Unit,
    onAllAppsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onShowProfileClick: () -> Unit,
    onToggleFavorite: (HomeItem) -> Unit,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit,
    onXCardClick: () -> Unit
) {
    val backgroundModel = remember { WelcomeBackgroundModel() }
    var selectedApp by remember { mutableStateOf<HomeItem?>(null) }
    var selectedPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundModel.topColor,
                            backgroundModel.middleColor,
                            backgroundModel.bottomColor
                        )
                    )
                )
        ) {
            ProfileHeader(
                model = model,
                onShowProfileClick = onShowProfileClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Only show search bar when in All Apps or Favorites view
            if (model.showAllApps || model.viewFavorites) {
                // Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    color = SearchBarBackground
                ) {
                    OutlinedTextField(
                        value = model.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .padding(horizontal = 4.dp),
                        placeholder = { 
                            Text(
                                "Search apps",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = PrimaryBlue
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp
                        ),
                        singleLine = true
                    )
                }
            }

            // Toggle Buttons
            if (model.searchQuery.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onAllAppsClick,
                        modifier = Modifier.width(120.dp),
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
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = onFavoritesClick,
                        modifier = Modifier.width(120.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

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
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
                        ) {
                            items(model.filteredApps.chunked(3)) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        AppItem(
                                            title = item.title,
                                            isFavorite = item.isFavorite,
                                            onClick = { onItemClick(item) },
                                            onFavoriteClick = { onToggleFavorite(item) },
                                            modifier = Modifier.weight(1f),
                                            showFavoriteButton = model.showAllApps || model.viewFavorites,
                                            isSelected = item == selectedApp,
                                            onLongPress = { position -> 
                                                selectedApp = item
                                                selectedPosition = position
                                            },
                                            shouldBlur = selectedApp != null && item != selectedApp
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else if (model.showAllApps) {
                    // All Apps View with Categories
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    ) {
                        model.categories.forEach { (category, items) ->
                            item {
                                CategoryHeader(
                                    title = category,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                            items(items.chunked(3)) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        AppItem(
                                            title = item.title,
                                            isFavorite = item.isFavorite,
                                            onClick = { onItemClick(item) },
                                            onFavoriteClick = { onToggleFavorite(item) },
                                            modifier = Modifier.weight(1f),
                                            showFavoriteButton = model.showAllApps || model.viewFavorites,
                                            isSelected = item == selectedApp,
                                            onLongPress = { position -> 
                                                selectedApp = item
                                                selectedPosition = position
                                            },
                                            shouldBlur = selectedApp != null && item != selectedApp
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else if (model.viewFavorites) {
                    // Favorites View
                    if (model.favorites.isEmpty()) {
                        // Show empty state message when no favorites
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No favorite apps yet",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                        ) {
                            items(model.favorites.chunked(3)) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        AppItem(
                                            title = item.title,
                                            isFavorite = true, // Always true in favorites view
                                            onClick = { onItemClick(item) },
                                            onFavoriteClick = { onToggleFavorite(item) },
                                            modifier = Modifier.weight(1f),
                                            showFavoriteButton = true,
                                            isSelected = item == selectedApp,
                                            onLongPress = { position -> 
                                                selectedApp = item
                                                selectedPosition = position
                                            },
                                            shouldBlur = selectedApp != null && item != selectedApp
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    // Default Apps View
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                    ) {
                        items(model.defaultApps.chunked(3)) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(25.dp)
                            ) {
                                rowItems.forEach { item ->
                                    AppItem(
                                        title = item.title,
                                        isFavorite = item.isFavorite,
                                        onClick = { onItemClick(item) },
                                        onFavoriteClick = { onToggleFavorite(item) },
                                        modifier = Modifier.weight(1f),
                                        showFavoriteButton = false,
                                        isSelected = item == selectedApp,
                                        onLongPress = { position -> 
                                            selectedApp = item
                                            selectedPosition = position
                                        },
                                        shouldBlur = selectedApp != null && item != selectedApp
                                    )
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            
            // Add footer navigation
            FooterNavigation(
                model = model.footerNavigation,
                onHomeClick = onFooterHomeClick,
                onChatClick = onFooterChatClick,
                onSOSClick = onFooterSOSClick,
                onProfileClick = onFooterProfileClick
            )
        }

        // Show favorite dialog when an app is selected
        if (selectedApp != null && selectedPosition != null) {
            FavoriteDialog(
                title = selectedApp!!.title,
                isFavorite = selectedApp!!.isFavorite,
                onConfirm = {
                    onToggleFavorite(selectedApp!!)
                    selectedApp = null
                    selectedPosition = null
                },
                onDismiss = {
                    selectedApp = null
                    selectedPosition = null
                },
                position = selectedPosition
            )
        }
    }
}

@Composable
private fun AppItem(
    title: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = true,
    isSelected: Boolean = false,
    onLongPress: (Pair<Float, Float>) -> Unit,
    shouldBlur: Boolean = false
) {
    var itemPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
                // Store the position when the component is laid out
                val position = coordinates.positionInRoot()
                itemPosition = Pair(
                    position.x + (coordinates.size.width / 2),
                    position.y + coordinates.size.height
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { 
                        // Use the stored position when long pressed
                        itemPosition?.let { pos -> onLongPress(pos) }
                    }
                )
            }
            .blur(radius = if (shouldBlur) 10.dp else 0.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppIcon(title = title, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
        // Check if it's a default app
        when (title) {
            "My Documents", "ID", "Asset", "XCard", "Leave", 
            "eLearning", "My Career", "TimeSheet", "Goal" -> {
                Surface(
                    modifier = Modifier.size(120.dp),  // Significantly increased from 90.dp to 120.dp
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(
                            id = when (title) {
                                "My Documents" -> R.drawable.mydocuments
                                "ID" -> R.drawable.id
                                "Asset" -> R.drawable.asset
                                "XCard" -> R.drawable.xcard
                                "Leave" -> R.drawable.leave
                                "eLearning" -> R.drawable.elearning
                                "My Career" -> R.drawable.mycareer
                                "TimeSheet" -> R.drawable.timesheet
                                "Goal" -> R.drawable.goal
                                else -> R.drawable.mydocuments
                            }
                        ),
                        contentDescription = title,
                        modifier = Modifier
                            .padding(4.dp)  // Reduced padding even more to maximize icon size
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            else -> {
                // Original placeholder design for non-default apps
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = getColorForApp(title).copy(alpha = 0.1f)
                ) {
                    Box {
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
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier
                    .width(120.dp)
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = title,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Divider(
                modifier = Modifier.weight(1f),
                color = PrimaryBlue.copy(alpha = 0.1f),
                thickness = 2.dp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun FooterNavigation(
    model: FooterNavigationModel,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onSOSClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FooterItem(
            icon = Icons.Default.Home,
            title = "Home",
            isSelected = model.showHome,
            onClick = onHomeClick
        )
        FooterItem(
            icon = Icons.Default.Email,
            title = "Chat",
            isSelected = model.showChat,
            onClick = onChatClick
        )
        FooterItem(
            icon = Icons.Default.Warning,
            title = "SOS",
            isSelected = model.showSOS,
            onClick = onSOSClick
        )
        FooterItem(
            icon = Icons.Default.Person,
            title = "Profile",
            isSelected = model.showProfile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun FooterItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) Color(0xFF808080) else Color(0xFFBDBDBD),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color(0xFF808080) else Color(0xFFBDBDBD),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FavoriteDialog(
    title: String,
    isFavorite: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    position: Pair<Float, Float>? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        position?.let { (x, y) ->
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .offset {
                        IntOffset(
                            x = (x - 100).toInt(),
                            y = y.toInt() + 8
                        )
                    }
                    .clickable(enabled = false) { }, // Prevent click propagation
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                TextButton(
                    onClick = {
                        onConfirm()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        color = Color(0xFFDD3825),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 