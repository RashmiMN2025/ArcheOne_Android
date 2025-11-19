package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.network.CreatedPost
import com.archeGlobal.one.network.CreatedPostsRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.components.NewPostDialog
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostHistoryScreen(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val userDataManager = UserDataManager.getInstance(context)
    val userData = userDataManager.getUserData()
    val userEmail = userData?.email ?: ""
    val userAccess = userData?.userDetails?.access ?: ""

    var selectedTab by remember { mutableStateOf(0) }
    var posts by remember { mutableStateOf<List<CreatedPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<CreatedPost?>(null) }
    val scope = rememberCoroutineScope()

    // Function to load posts
    val loadPosts = suspend {
        isLoading = true
        try {
            val postType = if (selectedTab == 0) "headsUp" else "homeView"
            android.util.Log.d("PostHistoryScreen", "Loading posts for type: $postType")
            android.util.Log.d("PostHistoryScreen", "User email: $userEmail")

            val request = CreatedPostsRequest(email = userEmail)
            val response = RetrofitClient.apiService.getCreatedPosts(request)

            android.util.Log.d("PostHistoryScreen", "Response code: ${response.code()}")
            android.util.Log.d("PostHistoryScreen", "Response successful: ${response.isSuccessful}")
            android.util.Log.d("PostHistoryScreen", "Response body status: ${response.body()?.status}")

            if (response.isSuccessful && response.body()?.status == 200) {
                val allPosts = response.body()?.posts ?: emptyList()
                android.util.Log.d("PostHistoryScreen", "Total posts received: ${allPosts.size}")

                allPosts.forEachIndexed { index, post ->
                    android.util.Log.d("PostHistoryScreen", "Post $index: type=${post.post_type}, subject=${post.subject}, images=${post.image_urls?.size ?: 0}")
                    if (!post.image_urls.isNullOrEmpty()) {
                        post.image_urls.forEach { url ->
                            android.util.Log.d("PostHistoryScreen", "  Image URL: $url")
                        }
                    }
                }

                posts = allPosts.filter { it.post_type == postType }
                android.util.Log.d("PostHistoryScreen", "Filtered posts for $postType: ${posts.size}")

                posts.forEach { post ->
                    android.util.Log.d("PostHistoryScreen", "Displaying post: ${post.post_id}, images: ${post.image_urls?.size ?: 0}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PostHistoryScreen", "Error loading posts", e)
        } finally {
            isLoading = false
        }
    }

    // Load posts when tab changes or refresh is triggered
    LaunchedEffect(selectedTab, refreshTrigger) {
        android.util.Log.d("PostHistoryScreen", "LaunchedEffect triggered - selectedTab=$selectedTab, refreshTrigger=$refreshTrigger")
        scope.launch {
            loadPosts()
        }
    }

    // Delete post function
    val deletePost: (String) -> Unit = { postId ->
        scope.launch {
            try {
                android.util.Log.d("PostHistoryScreen", "Deleting post: $postId")
                val response = RetrofitClient.apiService.deletePost(postId)

                if (response.isSuccessful && response.body()?.status == 200) {
                    android.util.Log.d("PostHistoryScreen", "Post deleted successfully")
                    // Refresh the list
                    refreshTrigger++
                } else {
                    android.util.Log.e("PostHistoryScreen", "Failed to delete post: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("PostHistoryScreen", "Error deleting post", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4EE))
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Post History",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) Color(0xFFDD3825) else Color.White,
                        contentColor = if (selectedTab == 0) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "HeadsUp",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) Color(0xFFDD3825) else Color.White,
                        contentColor = if (selectedTab == 1) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Home Page",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Posts List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFDD3825))
                }
            } else if (posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No posts found",
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { post ->
                        if (post.post_type == "homeView") {
                            HomePagePostCard(
                                post = post,
                                onDelete = deletePost,
                                onEdit = { postData ->
                                    postToEdit = postData
                                    showEditDialog = true
                                }
                            )
                        } else {
                            PostHistoryCard(
                                post = post,
                                onDelete = deletePost,
                                onEdit = { postData ->
                                    postToEdit = postData
                                    showEditDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog && postToEdit != null) {
        NewPostDialog(
            profilePicUrl = postToEdit?.profile_pic,
            userName = postToEdit?.username ?: "",
            userAccess = userAccess,
            onDismiss = {
                showEditDialog = false
                postToEdit = null
            },
            onSubmit = { _: String, _: String, _: String ->
                // Refresh the list after edit
                android.util.Log.d("PostHistoryScreen", "onSubmit called - triggering refresh")
                refreshTrigger++
                android.util.Log.d("PostHistoryScreen", "refreshTrigger incremented to: $refreshTrigger")
                showEditDialog = false
                postToEdit = null
            },
            onHistoryClick = {},
            existingPost = postToEdit
        )
    }
}

@Composable
fun PostHistoryCard(post: CreatedPost, onDelete: (String) -> Unit, onEdit: (CreatedPost) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            // Header with profile, name, and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = post.username,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = formatPostDate(post.created_at),
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        // Tagged Employees or Target Departments
                        if (post.target_group == "DepartmentBased" && !post.target_department.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.item_name),
                                    contentDescription = "Target Departments",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Target Departments",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = post.target_department.joinToString(", "),
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        } else if (!post.target_employee.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.item_name),
                                    contentDescription = "Tagged Employees",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tagged Employees",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = post.target_employee.joinToString(", "),
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Priority badge and delete button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (post.priority) {
                                    "High" -> Color(0xFFD32F2F)
                                    "Medium" -> Color(0xFFFFA726)
                                    "Low" -> Color(0xFF66BB6A)
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

                    // Menu button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFDD3825), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit(post)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showMenu = false
                                    onDelete(post.post_id)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject
            if (post.subject.isNotEmpty()) {
                Text(
                    text = post.subject,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description
            Text(
                text = post.description,
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Images if available (displayed side-by-side)
            if (!post.image_urls.isNullOrEmpty() && post.image_urls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    post.image_urls.forEach { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Post image",
                            modifier = Modifier
                                .width(130.dp)
                                .height(130.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Support Details
            if (!post.support_channel.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Support Details :",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.support_channel,
                    fontFamily = GraphikFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Activity Duration
            if (!post.activity_start.isNullOrEmpty() && !post.activity_end.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Activity Duration :",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatActivityDateTime(post.activity_start)} - ${formatActivityDateTime(post.activity_end)}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Dates
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    if (!post.end_date.isNullOrEmpty()) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Post End Date",
                                fontFamily = GraphikFontFamily,
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatPostDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun formatPostDateShort(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun formatActivityDateTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun HomePagePostCard(post: CreatedPost, onDelete: (String) -> Unit, onEdit: (CreatedPost) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    android.util.Log.d("HomePagePostCard", "Rendering card for post: ${post.post_id}")
    android.util.Log.d("HomePagePostCard", "Post type: ${post.post_type}")
    android.util.Log.d("HomePagePostCard", "Subject: ${post.subject}")
    android.util.Log.d("HomePagePostCard", "Image URLs: ${post.image_urls}")
    android.util.Log.d("HomePagePostCard", "Image URLs null? ${post.image_urls == null}")
    android.util.Log.d("HomePagePostCard", "Image URLs empty? ${post.image_urls?.isEmpty()}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            // Header with profile, name, and edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = post.username,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = formatPostDate(post.created_at),
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Menu button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFDD3825), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit(post)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete(post.post_id)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Banner Image - Smaller and show full image
            if (!post.image_urls.isNullOrEmpty() && post.image_urls.isNotEmpty()) {
                android.util.Log.d("HomePagePostCard", "Loading image: ${post.image_urls[0]}")
                android.util.Log.d("HomePagePostCard", "Total images: ${post.image_urls.size}")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = post.image_urls[0],
                            onError = {
                                android.util.Log.e("HomePagePostCard", "Failed to load image: ${post.image_urls[0]}")
                                android.util.Log.e("HomePagePostCard", "Error: ${it.result.throwable}")
                            },
                            onSuccess = {
                                android.util.Log.d("HomePagePostCard", "Image loaded successfully: ${post.image_urls[0]}")
                            }
                        ),
                        contentDescription = "Post banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                android.util.Log.d("HomePagePostCard", "No images to display. image_urls: ${post.image_urls}")
                // Show placeholder when no image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Subject/Title - Centered and bold
            if (post.subject.isNotEmpty()) {
                Text(
                    text = post.subject,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Date - Centered and gray
            Text(
                text = formatPostDateShort(post.created_at),
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description - Centered
            Text(
                text = post.description,
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Dates section
            if (!post.start_date.isNullOrEmpty() || !post.end_date.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!post.start_date.isNullOrEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Post Start Date",
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    if (!post.end_date.isNullOrEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Post End Date",
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
