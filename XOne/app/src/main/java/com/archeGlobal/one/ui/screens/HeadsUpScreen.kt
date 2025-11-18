package com.archeGlobal.one.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.network.CreatedPost
import com.archeGlobal.one.network.HeadsUpPostsRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.components.FooterScaffold
import com.archeGlobal.one.ui.components.NewPostDialog
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.UserDataManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadsUpScreen(
    footerNavigation: FooterNavigationModel,
    isUsingPrideIcon: Boolean = false,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterHeadsUpClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit,
    onHistoryClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val userDataManager = UserDataManager.getInstance(context)
    val userData = userDataManager.getUserData()
    val profilePicUrl = userData?.profilePic
    val userName = userData?.name ?: "User"
    val userEmail = userData?.email ?: ""
    val userAccess = userData?.userDetails?.access ?: ""
    val canCreatePost = userAccess.lowercase() in listOf("admin", "it", "hr")

    var showNewPostDialog by remember { mutableStateOf(false) }
    var posts by remember { mutableStateOf<List<CreatedPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Function to load HeadsUp posts
    val loadPosts = suspend {
        isLoading = true
        try {
            android.util.Log.d("HeadsUpScreen", "Loading HeadsUp posts for email: $userEmail")

            val request = HeadsUpPostsRequest(
                department = "",
                location = "",
                email = userEmail
            )
            val response = RetrofitClient.apiService.getHeadsUpPosts(request)

            android.util.Log.d("HeadsUpScreen", "Response code: ${response.code()}")
            android.util.Log.d("HeadsUpScreen", "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body()?.status == 200) {
                posts = response.body()?.posts ?: emptyList()
                android.util.Log.d("HeadsUpScreen", "Loaded ${posts.size} HeadsUp posts")
            } else {
                android.util.Log.e("HeadsUpScreen", "Failed to load posts: ${response.code()}")
                posts = emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("HeadsUpScreen", "Error loading posts", e)
            posts = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Load posts on screen launch and when refresh is triggered
    LaunchedEffect(refreshTrigger) {
        scope.launch {
            loadPosts()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        FooterScaffold(
            footerNavigation = footerNavigation.copy(
                showHome = false,
                showChat = false,
                showHeadsUp = true,
                showSOS = false,
                showProfile = false
            ),
            isUsingPrideIcon = isUsingPrideIcon,
            onFooterHomeClick = onFooterHomeClick,
            onFooterChatClick = onFooterChatClick,
            onFooterHeadsUpClick = onFooterHeadsUpClick,
            onFooterSOSClick = onFooterSOSClick,
            onFooterProfileClick = onFooterProfileClick,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE0DCD1), // Light grey at top
                                Color(0xFFC8C8CA), // Medium grey in middle
                                Color(0xFF474749), // Dark grey at bottom
                            )
                        )
                    )
            ) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "HeadsUp",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {},
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                SwipeRefresh(
                    state = rememberSwipeRefreshState(isLoading),
                    onRefresh = {
                        scope.launch {
                            loadPosts()
                        }
                    }
                ) {
                    if (posts.isEmpty() && !isLoading) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                // Icon
                                Surface(
                                    modifier = Modifier.size(120.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color(0xFFE8E3D9)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.headsup),
                                            contentDescription = "No Posts",
                                            modifier = Modifier.size(64.dp),
                                            tint = Color(0xFF999999)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Title
                                Text(
                                    text = "No Posts Found",
                                    fontSize = 24.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Subtitle
                                Text(
                                    text = "Create a post by clicking on New Post!",
                                    fontSize = 16.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF666666),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(200.dp))
                            }
                        }
                    } else {
                        // Posts list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(posts) { post ->
                                HeadsUpPostCard(
                                    post = post,
                                    canManagePost = canCreatePost,
                                    userEmail = userEmail
                                )
                            }
                        }
                    }
                }
            }

            // Floating Action Button at bottom right - For admin, IT, and HR users
            if (canCreatePost) {
                FloatingActionButton(
                    onClick = { showNewPostDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 40.dp, end = 24.dp),
                    containerColor = Color(0xFFDD3825),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "New Post",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Post",
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Show New Post Dialog
        if (showNewPostDialog) {
            NewPostDialog(
                profilePicUrl = profilePicUrl,
                userName = userName,
                userAccess = userAccess,
                onDismiss = { showNewPostDialog = false },
                onSubmit = { _, _, _ ->
                    showNewPostDialog = false
                    // Refresh posts after creating new post
                    refreshTrigger++
                },
                onHistoryClick = onHistoryClick
            )
        }
    }
}

@Composable
fun HeadsUpPostCard(
    post: CreatedPost,
    canManagePost: Boolean,
    userEmail: String
) {
    var expanded by remember { mutableStateOf(false) }
    var showImageZoom by remember { mutableStateOf(false) }
    var zoomedImageUrl by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with profile, name, and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Profile picture
                    if (!post.profile_pic.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(post.profile_pic),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.username.take(1).uppercase(),
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = post.username,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = formatPostTime(post.created_at),
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        // Target audience with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.item_name),
                                contentDescription = "Target Audience",
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTargetAudience(post),
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp) // Added start padding to move right
                ) {
                    // Priority badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (post.priority.lowercase()) {
                                    "high" -> Color(0xFFD32F2F)
                                    "medium" -> Color(0xFFFFA726)
                                    "low" -> Color(0xFF66BB6A)
                                    else -> Color.Gray
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.priority,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Only show dots menu for admin, IT, and HR users
                    if (canManagePost) {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.dots),
                                    contentDescription = "More options",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report Spam") },
                                    onClick = {
                                        expanded = false
                                        // Call report spam API
                                        scope.launch {
                                            try {
                                                val response = RetrofitClient.apiService.reportSpam(
                                                    com.archeGlobal.one.network.ReportSpamRequest(
                                                        email = userEmail,
                                                        post_id = post.post_id
                                                    )
                                                )
                                                if (response.isSuccessful && response.body()?.status == 200) {
                                                    android.util.Log.d("HeadsUpScreen", "Report spam successful")
                                                    // Show success message
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        response.body()?.message ?: "Report sent successfully",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    android.util.Log.e("HeadsUpScreen", "Report spam failed: ${response.code()}")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Failed to report spam",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("HeadsUpScreen", "Error reporting spam", e)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Error: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject
            Text(
                text = post.subject,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = post.description,
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )

            // Images if available with zoom functionality
            if (!post.image_urls.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(post.image_urls[0]),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .width(130.dp)
                            .height(130.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                zoomedImageUrl = post.image_urls[0]
                                showImageZoom = true
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Support Details if available
            if (!post.support_channel.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Support Details :",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = post.support_channel,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            // Activity Duration if available
            if (!post.activity_start.isNullOrEmpty() && !post.activity_end.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Acitivity Duration :",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "${formatActivityTime(post.activity_start)} - ${formatActivityTime(post.activity_end)}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }

    // Image zoom dialog
    if (showImageZoom) {
        Dialog(
            onDismissRequest = { showImageZoom = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showImageZoom = false }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(zoomedImageUrl),
                    contentDescription = "Zoomed Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { showImageZoom = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// Helper function to format post time
private fun formatPostTime(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(createdAt)

        if (date != null) {
            val now = Calendar.getInstance().time
            val diff = now.time - date.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        } else {
            "Unknown"
        }
    } catch (e: Exception) {
        "Unknown"
    }
}

// Helper function to format activity time
private fun formatActivityTime(isoTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoTime)

        if (date != null) {
            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            outputFormat.format(date)
        } else {
            isoTime
        }
    } catch (e: Exception) {
        isoTime
    }
}

// Helper function to format target audience
private fun formatTargetAudience(post: CreatedPost): String {
    return when (post.target_group) {
        "Everyone" -> "Everyone@arche.global"
        "DepartmentBased" -> post.target_department?.joinToString(", ") ?: "Department"
        "LocationBased" -> post.target_location?.joinToString(", ") ?: "Location"
        "EmployeeBased" -> post.target_employee?.firstOrNull() ?: "Specific Employee"
        else -> "Everyone@arche.global"
    }
}
