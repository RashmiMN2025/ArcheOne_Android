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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.archeGlobal.one.ui.components.UniversalLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.CachePolicy
import android.util.Log
import android.widget.Toast
import com.archeGlobal.one.utils.ImageCache
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.BackHandler
import android.app.Activity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.foundation.gestures.detectVerticalDragGestures

@Composable
fun ProfileHeader(
    model: HomeModel,
    onShowProfileClick: () -> Unit
) {

    val context = LocalContext.current
    val activity = remember { context as? Activity }

    // Handle back swipe or back button press
    BackHandler(enabled = true) {
        activity?.finishAffinity() // Exit the app and go to the mobile home screen
    }
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
                Spacer(modifier = Modifier.width(5.dp))
                // Profile picture
                Surface(
                    modifier = Modifier
                        .size(80.dp,85.dp)
                        .padding(top = 8.dp)
                        .clickable(onClick = onShowProfileClick),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    // If profile picture URL is available, display it using Coil
                    if (model.profilePicture != null && model.profilePicture.isNotEmpty()) {
                        Log.d("HomeScreen", "Loading profile picture: ${model.profilePicture}")

                        // Use ImageCache version for recomposition
                        val context = LocalContext.current
                        val cacheVersion = ImageCache.profileImageVersion.collectAsState().value
                        key(model.profilePicture, cacheVersion) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Always show the person icon first as a placeholder
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.DarkGray
                                )

                                // Load the actual profile image on top
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
                        }
                    } else {
                        // Default profile icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.DarkGray
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
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = model.designation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = model.department,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = model.employeeId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontFamily = GraphikFontFamily,
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
    onXCardClick: () -> Unit,
    isAuthenticating: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val backgroundModel = remember { WelcomeBackgroundModel() }
    var selectedApp by remember { mutableStateOf<HomeItem?>(null) }
    var selectedPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Effect to handle refresh completion
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            onRefresh()
            // Add a small delay to ensure the refresh operation has time to complete
            kotlinx.coroutines.delay(1000)
            isRefreshing = false
        }
    }

    // Wrap with FooterScaffold for bottom navigation
    FooterScaffold(
        footerNavigation = model.footerNavigation,
        onFooterHomeClick = onFooterHomeClick,
        onFooterChatClick = onFooterChatClick,
        onFooterSOSClick = onFooterSOSClick,
        onFooterProfileClick = onFooterProfileClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content with conditional blur and pull-to-refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (isAuthenticating) 10.dp else 0.dp)
                    .pointerInput(Unit) {
                        var dragStart = 0f
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                dragStart = offset.y
                            },
                            onDragEnd = {
                                if (dragStart > 50f && !isRefreshing) { // Only trigger if not already refreshing
                                    isRefreshing = true
                                }
                            },
                            onDragCancel = {},
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                            }
                        )
                    }
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
                            Text(
                                "All Apps",
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(30.dp))

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
                            Text(
                                "Favorites",
                                fontWeight = FontWeight.Medium
                            )
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowItems.forEach { item ->
                                                AppItem(
                                                    title = item.title,
                                                    isFavorite = item.isFavorite,
                                                    onClick = { onItemClick(item) },
                                                    onFavoriteClick = { onToggleFavorite(item) },
                                                    modifier = Modifier.weight(1f),
                                                    showFavoriteButton = false,
                                                    isSelected = false,
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
                                        Spacer(modifier = Modifier.height(10.dp))
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
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowItems.forEach { item ->
                                                    AppItem(
                                                        title = item.title,
                                                        isFavorite = true,
                                                        onClick = { onItemClick(item) },
                                                        onFavoriteClick = { onToggleFavorite(item) },
                                                        modifier = Modifier.weight(1f),
                                                        showFavoriteButton = false,
                                                        isSelected = false,
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
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Show universal loader while refreshing
            UniversalLoader(isLoading = isRefreshing)

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

                    // Reduce the yOffset to decrease the space between the service card and the dialog
                    val yOffset = y - itemSizePx - 180 // Reduced from 235 to 200

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

            // Show authentication overlay if authenticating
            if (isAuthenticating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Optional: Add a fingerprint icon or loading indicator here
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

// Update this helper function to better format long titles
private fun formatServiceTitle(title: String): String {
    // Special cases for specific long titles - forcing proper line breaks
    return when (title) {
        "Holiday Calendar" -> "Holiday\nCalendar"
        "New Onboarding" -> "New\nOnboarding"
        "Travel & Expenses" -> "Travel &\nExpenses"
        "Goal Setting/KPI" -> "Goal\nSetting/KPI"
        "Business Card" -> "Business\nCard"
        "My Documents" -> "My\nDocuments"
        "MyDocuments" -> "My\nDocuments"
        "To Do" -> "To Do"
        "My Career" -> "My\nCareer"
        "Admin" -> "Admin" 
        "Medical" -> "Medical"
        "ID" -> "ID"
        "Finance" -> "Finance"
        "SAP" -> "SAP"
        "SOS" -> "SOS"
        "Connect" -> "Connect"
        "Locations" -> "Locations"
        "Policy" -> "Policy"
        "Profile" -> "Profile"
        "TimeSheet", "Timesheet" -> "Time\nSheet"
        "Leave" -> "Leave"
        "eLearning" -> "e-\nLearning"
        "Asset" -> "Asset"
        "Greetings" -> "Greetings"
        "Profile Connect" -> "Profile\nConnect"
        "Arche Odyssey" -> "Arche\nOdyssey"
        else -> {
            // For any other multi-word titles, always split at a space
            if (title.contains(" ")) {
                val words = title.split(" ")
                if (words.size >= 2) {
                    // If there are multiple words, split appropriately
                    if (words.size == 2) {
                        // Just two words, simple split
                        "${words[0]}\n${words[1]}"
                    } else {
                        // More than two words, balance the lines
                        val midpoint = words.size / 2
                        val firstPart = words.take(midpoint).joinToString(" ")
                        val secondPart = words.drop(midpoint).joinToString(" ")
                        "$firstPart\n$secondPart"
                    }
                } else {
                    title
                }
            } else if (title.length > 7) {
                // For long single words, split in half
                val mid = title.length / 2
                title.substring(0, mid) + "\n" + title.substring(mid)
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
    showFavoriteButton: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: (Pair<Float, Float>) -> Unit
) {
    var itemPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    val context = LocalContext.current
    // Format the title for better display
    val formattedTitle = formatServiceTitle(title)

    Card(
        modifier = modifier
            .aspectRatio(0.95f)
            .padding(3.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                itemPosition = Pair(
                    position.x + (coordinates.size.width / 2),
                    position.y + (coordinates.size.height / 2)
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { 
                        onClick()
                    },
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
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Icon at the top
                AppIcon(title = title, modifier = Modifier.size(48.dp))
                
                Spacer(modifier = Modifier.height(1.dp))
                
                // Text area with more space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 35.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedTitle,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,  // Changed from Medium to SemiBold
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Visible, // Changed from Ellipsis to make sure text is visible
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    )
                }
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
            "Finance", "SAP", "SOS", "Holiday Calendar", "Greetings", "Medical", "Blogs",
            "Locations", "Travel & Expenses", "Policy", "New Onboarding", "Profile", "Profile Connect", "To Do" ,"Password Reset" ,"Know Your Org" ,"Arche Odyssey","ZingHR" -> {
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
                                "blogs" -> R.drawable.xconnect
                                "locations" -> R.drawable.locations
                                "travel&expenses" -> R.drawable.travel
                                "policy" -> R.drawable.policy
                                "newonboarding" -> R.drawable.new_onboarding
                                "profile" -> R.drawable.profile
                                "profileconnect" -> R.drawable.profile
                                "todo" -> R.drawable.todo
                                "passwordreset" -> R.drawable.password_reset
                                "knowyourog" -> R.drawable.know_your_org
                                "archeodyssey" -> R.drawable.arche_odyssey
                                "zinghr" -> R.drawable.zinghr
                                else -> R.drawable.mydocuments
                            }
                        ),
                        contentDescription = title,
                        modifier = Modifier
                            .padding(5.dp) // Increase padding if needed

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
