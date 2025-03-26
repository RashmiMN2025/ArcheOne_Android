package com.archeGlobal.one.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.model.HomeModel
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.ui.theme.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import com.archeGlobal.one.model.WelcomeBackgroundModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.getColorForApp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.ui.components.EmptyFavorites
import com.archeGlobal.one.ui.components.FooterScaffold
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.CachePolicy
import android.util.Log
import com.archeGlobal.one.utils.ImageCache
import androidx.compose.runtime.collectAsState

@Composable
fun ProfileHeader(
    model: HomeModel,
    onShowProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Background image with clip
        Image(
            painter = painterResource(id = R.drawable.header_home),
            contentDescription = "Header Background",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content without overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 27.dp, end = 16.dp, top = 50.dp, bottom = 16.dp),  // Adjusted top padding from 60.dp to 50.dp and bottom from 24.dp to 16.dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Profile picture
                Surface(
                    modifier = Modifier
                        .size(64.dp, 70.dp)
                        .padding(top = 8.dp)
                        .clickable(onClick = onShowProfileClick),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    // If profile picture URL is available, display it using Coil
                    if (model.profilePicture != null && model.profilePicture.isNotEmpty()) {
                        Log.d("HomeScreen", "Loading profile picture: ${model.profilePicture}")
                        
                        // Use ImageCache version for recomposition
                        val context = LocalContext.current
                        val cacheVersion = ImageCache.profileImageVersion.collectAsState().value
                        key(model.profilePicture, cacheVersion) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageCache.createProfileImageRequest(
                                        context = context, 
                                        url = model.profilePicture
                                    ),
                                    onSuccess = { 
                                        Log.d("HomeScreen", "Profile image loaded successfully: ${model.profilePicture}") 
                                    }
                                ),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // Default profile icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(14.dp),
                            tint = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = model.userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                    
                    Text(
                        text = model.designation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )
                    
                    Text(
                        text = model.department,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )
                    
                    Text(
                        text = model.employeeId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
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

    // Wrap with FooterScaffold for bottom navigation
    FooterScaffold(
        footerNavigation = model.footerNavigation,
        onFooterHomeClick = onFooterHomeClick,
        onFooterChatClick = onFooterChatClick,
        onFooterSOSClick = onFooterSOSClick,
        onFooterProfileClick = onFooterProfileClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content with conditional blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (selectedApp != null) 10.dp else 0.dp)
            ) {
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

                    // Toggle Buttons
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content
                    Box(modifier = Modifier.weight(1f)) {
                        if (model.showAllApps) {
                            // All Apps View
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
                                                    isSelected = false, // Never set to true here
                                                    onLongPress = { position -> 
                                                        selectedApp = item
                                                        selectedPosition = position
                                                    }
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
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyFavorites()
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                                ) {
                                    model.favorites.forEach { (category, items) ->
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
                                                        isFavorite = true,
                                                        onClick = { onItemClick(item) },
                                                        onFavoriteClick = { onToggleFavorite(item) },
                                                        modifier = Modifier.weight(1f),
                                                        showFavoriteButton = true,
                                                        isSelected = false, // Never set to true here
                                                        onLongPress = { position -> 
                                                            selectedApp = item
                                                            selectedPosition = position
                                                        }
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
                        }
                    }
                }
            }

            // Semi-transparent overlay when an app is selected
            if (selectedApp != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .blur(radius = 8.dp)
                        .clickable(onClick = {
                            selectedApp = null
                            selectedPosition = null
                        })
                )
            }

            // Overlay the selected app
            if (selectedApp != null) {
                selectedPosition?.let { (x, y) ->
                    val density = LocalDensity.current
                    val itemSize = 80.dp
                    val scaleFactor = 1.2f  // Slightly bigger than original
                    val itemSizePx = with(density) { itemSize.toPx() }
                    
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (x - itemSizePx * scaleFactor / 2).toInt(),
                                    y = (y - itemSizePx - 15).toInt() // Position exactly above with exact pixel offset
                                )
                            }
                    ) {
                        val formattedTitle = formatServiceTitle(selectedApp!!.title)
                        
                        Card(
                            modifier = Modifier
                                .size(itemSize * scaleFactor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    // Icon at the top - slightly larger
                                    AppIcon(title = selectedApp!!.title, modifier = Modifier.size(46.dp))
                                    
                                    // Text at the bottom with more space
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 30.dp)
                                    ) {
                                        Text(
                                            text = formattedTitle,
                                            color = TextPrimary,
                                            fontSize = 10.sp, // Keep same as original
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            lineHeight = 13.sp,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Show favorite dialog
            if (selectedApp != null && selectedPosition != null) {
                selectedPosition?.let { (x, y) ->
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val dialogWidth = 160.dp  // Return to original width
                    val density = LocalDensity.current
                    
                    val dialogWidthPx = with(density) { dialogWidth.toPx() }
                    val screenWidthPx = with(density) { screenWidth.toPx() }
                    val itemSizePx = with(density) { 80.dp.toPx() }
                    val scaleFactor = 1.1f // Same as app scale factor
                    
                    // Calculate x position (centered with the app)
                    val xOffset = when {
                        x + (dialogWidthPx / 2) > screenWidthPx -> screenWidthPx - dialogWidthPx - 16f
                        x - (dialogWidthPx / 2) < 0 -> 16f
                        else -> x - (dialogWidthPx / 2)
                    }
                    
                    // Position dialog just a tiny bit above the enlarged app
                    val yOffset = y - itemSizePx - 235
                    
                    Card(
                        modifier = Modifier
                            .width(dialogWidth)
                            .offset {
                                IntOffset(
                                    x = xOffset.toInt(),
                                    y = yOffset.toInt()
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = if (selectedApp!!.isFavorite) "Remove from Favourites" else "Add to Favourites",
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onToggleFavorite(selectedApp!!)
                                    selectedApp = null
                                    selectedPosition = null
                                }
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Add this helper function to format long titles
private fun formatServiceTitle(title: String): String {
    // Special cases for specific long titles
    return when (title) {
        "Holiday Calendar" -> "Holiday\nCalendar"
        "New Onboarding" -> "New\nOnboarding"
        "Travel & Expenses" -> "Travel &\nExpenses"
        "Goal Setting/KPI" -> "Goal\nSetting/KPI"
        "Business Card" -> "Business\nCard"
        "My Documents" -> "My\nDocuments"
        else -> {
            // General rule for other multi-word titles longer than 10 characters
            if (title.contains(" ") && title.length > 10) {
                // Find the middle space to split approximately in half
                val spaces = title.indices.filter { title[it] == ' ' }
                if (spaces.isNotEmpty()) {
                    val middleSpaceIndex = spaces[spaces.size / 2]
                    title.substring(0, middleSpaceIndex) + "\n" + title.substring(middleSpaceIndex + 1)
                } else {
                    title
                }
            } else {
                title
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
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = true,
    isSelected: Boolean = false,
    onLongPress: (Pair<Float, Float>) -> Unit
) {
    var itemPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    // Format the title for better display
    val formattedTitle = formatServiceTitle(title)
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
                // Store the position when the component is laid out
                val position = coordinates.positionInRoot()
                itemPosition = Pair(
                    position.x + (coordinates.size.width / 2),
                    position.y + (coordinates.size.height / 2) // Store the center point
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { 
                        itemPosition?.let { pos -> onLongPress(pos) }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                
                // Icon at the top
                AppIcon(title = title, modifier = Modifier.size(38.dp))
                
                // Text at the bottom with more space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 30.dp)
                ) {
                    Text(
                        text = formattedTitle,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 13.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                            .align(Alignment.Center)
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
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
            "My Documents", "MyDocuments", "ID", "Asset", "Business Card", "Leave",
            "eLearning", "My Career", "Timesheet", "TimeSheet", "Goal Setting/KPI", "Admin",
            "Finance", "SAP", "SOS", "Holiday Calendar", "Greetings", "Medical", "Connect",
            "Locations", "Travel & Expenses", "Policy", "New Onboarding", "Profile", "Profile Connect"  -> {
                Surface(
                    modifier = Modifier.size(128.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(
                            id = when (title.lowercase().replace(" ", "")) {
                                "mydocuments" -> R.drawable.mydocuments
                                "id" -> R.drawable.id
                                "asset" -> R.drawable.asset
                                "businesscard" -> R.drawable.xcard
                                "leave" -> R.drawable.leave
                                "elearning" -> R.drawable.elearning
                                "mycareer" -> R.drawable.mycareer
                                "timesheet" -> R.drawable.timesheet
                                "goalsetting/kpi", "goal" -> R.drawable.goals
                                "admin" -> R.drawable.admin
                                "finance" -> R.drawable.finance
                                "sap" -> R.drawable.sap
                                "sos" -> R.drawable.sos
                                "holidaycalendar" -> R.drawable.holiday
                                "greetings" -> R.drawable.greetings
                                "medical" -> R.drawable.medical
                                "connect" -> R.drawable.xconnect
                                "locations" -> R.drawable.locations
                                "travel&expenses" -> R.drawable.travel
                                "policy" -> R.drawable.policy
                                "newonboarding" -> R.drawable.new_onboarding
                                "profileconnect" -> R.drawable.profile
                                else -> R.drawable.mydocuments
                            }
                        ),
                        contentDescription = title,
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            else -> {
                // Original placeholder design for non-default apps
                Surface(
                    modifier = Modifier.size(44.dp),
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
    Column(modifier = modifier) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
} 