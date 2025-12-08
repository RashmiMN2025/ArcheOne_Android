package com.archeGlobal.one.ui.screens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.archeGlobal.one.ui.components.UniversalLoader
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
    onHistoryClick: (initialTab: Int) -> Unit = {},
) {
    val context = LocalContext.current
    val userDataManager = UserDataManager.getInstance(context)
    val userData = userDataManager.getUserData()
    val profilePicUrl = userData?.profilePic
    val userName = userData?.name ?: "User"
    val userEmail = userData?.email ?: ""
    val userDepartment = userData?.department ?: ""
    val userLocation = userData?.location ?: ""
    val userAccess = userData?.userDetails?.access ?: ""
    val canCreatePost = userAccess.lowercase() in listOf("admin", "it", "hr")

    var showNewPostDialog by rememberSaveable { mutableStateOf(false) }
    var posts by remember { mutableStateOf<List<CreatedPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Handle back press when dialog is open - close dialog instead of leaving screen
    BackHandler(enabled = showNewPostDialog) {
        showNewPostDialog = false
    }

    // Function to load HeadsUp posts
    val loadPosts = suspend {
        isLoading = true
        try {
            android.util.Log.d("HeadsUpScreen", "Loading HeadsUp posts for email: $userEmail")
            android.util.Log.d("HeadsUpScreen", "User department: $userDepartment")
            android.util.Log.d("HeadsUpScreen", "User location: $userLocation")

            val request = HeadsUpPostsRequest(
                department = userDepartment,
                location = userLocation,
                email = userEmail
            )
            val response = RetrofitClient.apiService.getHeadsUpPosts(request)

            android.util.Log.d("HeadsUpScreen", "Response code: ${response.code()}")
            android.util.Log.d("HeadsUpScreen", "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body()?.status == 200) {
                posts = response.body()?.posts ?: emptyList()
                android.util.Log.d("HeadsUpScreen", "Loaded ${posts.size} HeadsUp posts")
                // Save HeadsUp count to UserDataManager
                userDataManager.saveHeadsUpCount(posts.size)
            } else {
                android.util.Log.e("HeadsUpScreen", "Failed to load posts: ${response.code()}")
                posts = emptyList()
                userDataManager.saveHeadsUpCount(0)
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
            headsUpCount = posts.size,
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
                    },
                    indicator = { _, _ -> } // Hide default indicator - use UniversalLoader instead
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
                                if (canCreatePost) {
                                    Text(
                                        text = "Create a post by clicking on New Post!",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF666666),
                                        textAlign = TextAlign.Center
                                    )
                                }

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
                            painter = painterResource(id = R.drawable.newpost),
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
                onSubmit = { _, _, formType ->
                    // Don't close dialog - keep it open when navigating to history
                    // so it reopens when user comes back from Post History screen
                    // Navigate to correct tab: 0 for HeadsUp, 1 for Home Page
                    val tab = if (formType == "Event") 1 else 0
                    onHistoryClick(tab)
                },
                onHistoryClick = { onHistoryClick(0) }
            )
        }

        // Universal loader
        if (isLoading) {
            UniversalLoader(isLoading = true)
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
    var showTargetDetailsDialog by remember { mutableStateOf(false) }
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
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
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
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                        
                        // Target audience logic
                        val isEveryone = post.target_group == "Everyone" || 
                                        (post.target_department.isNullOrEmpty() && 
                                         post.target_location.isNullOrEmpty() && 
                                         post.target_employee.isNullOrEmpty() &&
                                         post.target_group != "DepartmentBased" &&
                                         post.target_group != "LocationBased" &&
                                         post.target_group != "EmployeeBased" &&
                                         post.target_group != "ProjectBased")

                        // Only show label/icon if NOT everyone
                        if (!isEveryone) {
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
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF666666),
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        // Show actual target details (emails/departments/locations)
                        // Prepare the list of items
                        val targetItems = when {
                            isEveryone -> listOf("everyone@arche.global")
                            !post.target_department.isNullOrEmpty() -> post.target_department
                            !post.target_location.isNullOrEmpty() -> post.target_location
                            !post.target_employee.isNullOrEmpty() -> post.target_employee
                            else -> listOf("everyone@arche.global")
                        } ?: emptyList()

                        if (targetItems.isNotEmpty()) {
                            val displayText = if (targetItems.size > 3) {
                                val firstThree = targetItems.take(3).joinToString("\n")
                                "$firstThree\n+${targetItems.size - 3}"
                            } else {
                                targetItems.joinToString("\n")
                            }

                            Text(
                                text = displayText,
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF999999),
                                modifier = Modifier
                                    .padding(start = if (isEveryone) 0.dp else 16.dp)
                                    .clickable(enabled = targetItems.size > 3) {
                                        if (targetItems.size > 3) {
                                            showTargetDetailsDialog = true
                                        }
                                    },
                                softWrap = true,
                                overflow = TextOverflow.Visible,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
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
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.priority,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Only show dots menu for admin, IT, and HR users
                    if (canManagePost) {
                        Box {
                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = (-4).dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.dots),
                                    contentDescription = "More options",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF666666)
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
            if (!post.image_urls.isNullOrEmpty() && post.image_urls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.image_urls.forEach { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Post image",
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    zoomedImageUrl = imageUrl
                                    showImageZoom = true
                                },
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart
                        )
                    }
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
            if (post.activity_start != null && post.activity_end != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Acitivity Duration :",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "${formatActivityTime(post.activity_start ?: "")} - ${formatActivityTime(post.activity_end ?: "")}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            // Post Start Date and Post End Date
            if (!post.start_date.isNullOrEmpty() || !post.end_date.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!post.start_date.isNullOrEmpty()) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Post Start Date",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.green),
                                    contentDescription = "Start Date",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF66BB6A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatPostDateShort(post.start_date),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    if (!post.end_date.isNullOrEmpty()) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Post End Date",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.red),
                                    contentDescription = "End Date",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatPostDateShort(post.end_date),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
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
    
    // Target Details Dialog
    if (showTargetDetailsDialog) {
        Dialog(onDismissRequest = { showTargetDetailsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tagged Users",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val targetItems = when {
                        post.target_group == "Everyone" -> listOf("everyone@arche.global")
                        !post.target_department.isNullOrEmpty() -> post.target_department
                        !post.target_location.isNullOrEmpty() -> post.target_location
                        !post.target_employee.isNullOrEmpty() -> post.target_employee
                        else -> listOf("everyone@arche.global")
                    } ?: emptyList()

                    LazyColumn {
                        items(targetItems) { item ->
                            Text(
                                text = item,
                                fontFamily = GraphikFontFamily,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showTargetDetailsDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

// Helper function to format post time
private fun formatPostTime(createdAt: String): String {
    return try {
        // Parse the date from API format: "2025-11-19 18:04:44"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(createdAt)

        if (date != null) {
            android.text.format.DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                android.text.format.DateUtils.MINUTE_IN_MILLIS
            ).toString()
        } else {
            "Unknown"
        }
    } catch (e: Exception) {
        android.util.Log.e("HeadsUpScreen", "Error formatting date: $createdAt", e)
        "Unknown"
    }
}

// Helper function to format activity time
private fun formatActivityTime(isoTime: String): String {
    return try {
        // If isoTime is empty, use current date
        if (isoTime.isEmpty()) {
            val outputFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
            return outputFormat.format(Date())
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(isoTime)

        if (date != null) {
            val outputFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
            outputFormat.format(date)
        } else {
            isoTime
        }
    } catch (e: Exception) {
        isoTime
    }
}

// Helper function to format target audience label
private fun formatTargetAudience(post: CreatedPost): String {
    // Infer target group from which field is populated
    val inferredGroup = when {
        post.target_group == "Everyone" -> "Everyone"
        !post.target_department.isNullOrEmpty() -> "DepartmentBased"
        !post.target_location.isNullOrEmpty() -> "LocationBased"
        !post.target_employee.isNullOrEmpty() -> "EmployeeBased"
        post.target_group == "DepartmentBased" -> "DepartmentBased"
        post.target_group == "LocationBased" -> "LocationBased"
        post.target_group == "EmployeeBased" -> "EmployeeBased"
        post.target_group == "ProjectBased" -> "ProjectBased"
        else -> "Everyone"
    }

    val label = when (inferredGroup) {
        "Everyone" -> "Tagged Employees"
        "DepartmentBased" -> "Tagged Department"
        "LocationBased" -> "Tagged Location"
        "EmployeeBased" -> "Tagged Employees"
        "ProjectBased" -> "Tagged Project"
        else -> "Tagged Employees"
    }
    android.util.Log.d("HeadsUpScreen", "Original target_group: ${post.target_group}, Inferred: $inferredGroup, Label: $label")
    return label
}

// Helper function to format target details (actual emails/departments/locations)
private fun formatTargetDetails(post: CreatedPost): String {
    // Infer target group from which field is populated
    val details = when {
        post.target_group == "Everyone" -> "Everyone@arche.global"
        !post.target_department.isNullOrEmpty() -> post.target_department.joinToString("\n")
        !post.target_location.isNullOrEmpty() -> post.target_location.joinToString("\n")
        !post.target_employee.isNullOrEmpty() -> post.target_employee.joinToString("\n")
        post.target_group == "DepartmentBased" -> post.target_department?.joinToString("\n") ?: ""
        post.target_group == "LocationBased" -> post.target_location?.joinToString("\n") ?: ""
        post.target_group == "EmployeeBased" -> post.target_employee?.joinToString("\n") ?: ""
        else -> "Everyone@arche.global"
    }
    android.util.Log.d("HeadsUpScreen", "Target group: ${post.target_group}, Details: $details")
    android.util.Log.d("HeadsUpScreen", "Department: ${post.target_department}, Location: ${post.target_location}, Employee: ${post.target_employee}")
    return details
}
