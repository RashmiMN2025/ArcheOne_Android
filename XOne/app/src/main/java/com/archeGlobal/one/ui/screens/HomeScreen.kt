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
import com.archeGlobal.one.ui.components.UniversalLoader
import coil.compose.rememberAsyncImagePainter
import android.util.Log
import android.widget.Toast
import com.archeGlobal.one.utils.ImageCache
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
            .height(240.dp)
            .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
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
                .padding(start = 27.dp, end = 16.dp, top = 70.dp, bottom = 16.dp),  // Adjusted top padding from 60.dp to 50.dp and bottom from 24.dp to 16.dp
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
                        .size(90.dp,95.dp)
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
                        fontSize = 23.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Text(
                        text = model.designation,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )

                    Text(
                        text = model.department,
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )

                    Text(
                        text = model.employeeId,
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
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

    // State to track the current view (All Apps or Favorites)
    var currentView by remember { mutableStateOf("All Apps") }

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
    )  {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content with conditional blur and pull-to-refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (isAuthenticating) 10.dp else 0.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { /* Handle drag end */ },
                            onHorizontalDrag = { _, dragAmount ->
                                if (dragAmount > 50) {
                                    // Swiped from left to right
                                    currentView = "All Apps"
                                } else if (dragAmount < -50) {
                                    // Swiped from right to left
                                    currentView = "Favorites"
                                }
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
                        onClick = {
                            currentView = "All Apps"
                            onAllAppsClick()
                        },
                        modifier = Modifier.width(150.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentView == "All Apps")
                                Color(0xFFDD3825) else CardBackground,
                            contentColor = if (currentView == "All Apps")
                                Color.White else Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (currentView == "All Apps") Color(0xFFDD3825) else DividerColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "All Apps",
                                fontSize = 16.sp, // Added font size
                                fontFamily = GraphikFontFamily, // Added font family
                                fontWeight = FontWeight.Medium, // Added font weight
                            )
                        }

                        Spacer(modifier = Modifier.width(30.dp))

                        Button(
                        onClick = {
                            currentView = "Favorites"
                            onFavoritesClick()
                        },
                        modifier = Modifier.width(150.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentView == "Favorites")
                                Color(0xFFDD3825) else CardBackground,
                            contentColor = if (currentView == "Favorites")
                                Color.White else Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (currentView == "Favorites") Color(0xFFDD3825) else DividerColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Favorites",
                                fontSize = 16.sp, // Added font size
                                fontFamily = GraphikFontFamily, // Added font family
                                fontWeight = FontWeight.Medium, // Added font weight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content
                    Box(modifier = Modifier.weight(1f)) {
                        if (currentView == "All Apps") {
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
                        } else if (currentView == "Favorites") {
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
                                            color = Color.Black,
                                            fontSize = 10.sp, // Keep same as original
                                            fontFamily = GraphikFontFamily,
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
        "DeskCart" -> "DeskCart"
        "Vision" -> "Vision"
        "Core Values" -> "CoreValues"
        "Communique" -> "Communique"
        "About Us" -> "AboutUs"
        "Calendar" -> "Calendar"
        "Holiday Calendar" -> "Holiday\nCalendar"
        "New Onboarding" -> "New\nOnboarding"
        "Travel & Expenses" -> "Travel &\nExpenses"
        "Goal Setting/KPI" -> "Goal\nSetting/KPI"
        "Business Card" -> "Business\nCard"
        "My Documents" -> "My\nDocuments"
        "MyDocuments" -> "My\nDocuments"
        "Checkmate" -> "Checkmate"
        "My Career" -> "My\nCareer"
        "Admin" -> "Admin" 
        "Medical" -> "Medical"
        "ID" -> "ID"
        "Finance" -> "Finance"
        "SAP" -> "SAP"
        "Ample" -> "Ample"
        "SOS" -> "SOS"
        "Connect" -> "Connect"
        "Locations" -> "Locations"
        "Policy" -> "Policy"
        "Profile" -> "Profile"
        "TimeSheet", "Timesheet" -> "TimeSheet"
        "Leave" -> "Leave"
        "eLearning" -> "eLearning"
        "Asset" -> "Asset"
        "Greetings" -> "Greetings"
        "Profile Connect" -> "Profile\nConnect"
        "Arche Odyssey" -> "Arche\nOdyssey"
        "Idea Vault", "IdeaVault" -> "IdeaVault"
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
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(12.dp))
                // Icon at the top
                AppIcon(title = title, modifier = Modifier.size(50.dp))

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                        text = formattedTitle,
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Visible, // Changed from Ellipsis to make sure text is visible
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
            "My Documents", "MyDocuments", "ID", "Asset", "Business Card", "Leave", "DeskCart",
            "eLearning", "My Career", "Timesheet", "TimeSheet", "Goal Setting/KPI", "Admin", "Vision",
            "Finance", "SAP", "Ample", "SOS", "Holiday Calendar", "Calendar", "About Us", "Communique", "Core Values", "CoreValues", "Greetings", "Medical", "Blogs",
            "Locations", "Travel & Expenses", "Policy", "New Onboarding", "Profile", "Profile Connect", "Checkmate" ,"Password Reset" ,"Know Your Org" ,"Arche Odyssey","ZingHR", "IdeaVault" ,"Pulse" -> {
                Surface(
                    modifier = Modifier.size(128.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(
                            id = when (title.lowercase().replace(" ", "")) {
                                "deskcart" -> R.drawable.deskcart
                                "vision" -> R.drawable.vision
                                "corevalues" -> R.drawable.core_values
                                "communique" -> R.drawable.communique
                                "aboutus" -> R.drawable.about_us
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
                                "ample" -> R.drawable.ample
                                "sos" -> R.drawable.sos
                                "calendar" -> R.drawable.holiday
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
                                "checkmate" -> R.drawable.todo
                                "passwordreset" -> R.drawable.password_reset
                                "knowyourog" -> R.drawable.know_your_org
                                "archeodyssey" -> R.drawable.arche_odyssey
                                "zinghr" -> R.drawable.zinghr
                                "ideavault" -> R.drawable.idea_vault
                                "pulse" -> R.drawable.pulse
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
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
