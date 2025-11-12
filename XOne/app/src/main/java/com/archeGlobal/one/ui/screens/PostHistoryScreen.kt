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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.network.CreatedPost
import com.archeGlobal.one.network.CreatedPostsRequest
import com.archeGlobal.one.network.RetrofitClient
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

    var selectedTab by remember { mutableStateOf(0) }
    var posts by remember { mutableStateOf<List<CreatedPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load posts
    LaunchedEffect(selectedTab) {
        scope.launch {
            isLoading = true
            try {
                val postType = if (selectedTab == 0) "headsUp" else "homePage"
                val request = CreatedPostsRequest(email = userEmail)
                val response = RetrofitClient.apiService.getCreatedPosts(request)
                if (response.isSuccessful && response.body()?.status == 200) {
                    posts = response.body()?.posts?.filter { it.post_type == postType } ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
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
                        PostHistoryCard(post = post)
                    }
                }
            }
        }
    }
}

@Composable
fun PostHistoryCard(post: CreatedPost) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatPostDate(post.created_at),
                                fontFamily = GraphikFontFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = " • ${if (post.post_type == "headsUp") "headsUp" else "homePage"}",
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
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = post.priority,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = { /* TODO: Handle delete */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bin),
                            contentDescription = "Delete",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
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

            // Support channel
            if (!post.support_channel.isNullOrEmpty()) {
                Text(
                    text = "Support: ${post.support_channel}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Images if available (displayed side-by-side)
            if (!post.image_urls.isNullOrEmpty() && post.image_urls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    post.image_urls.forEach { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Post image",
                            modifier = Modifier
                                .width(130.dp)
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Inside
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Dates
            if (!post.start_date.isNullOrEmpty() || !post.end_date.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
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
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF66BB6A), CircleShape)
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
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFD32F2F), CircleShape)
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

private fun formatPostDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun formatPostDateShort(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
