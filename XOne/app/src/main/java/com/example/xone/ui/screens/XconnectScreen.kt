package com.example.xone.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.R
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XConnectScreen(
    onBackPressed: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Posts", "Case Studies", "Blogs", "Skill Support")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp)
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "XConnect",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 80.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.Black,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    // Empty indicator as we'll use custom tab styling
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = if (selectedTab == index) Color(0xFFDD3825) else Color.White,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Color.White else Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on selected tab
            when (selectedTab) {
                0 -> AllPostsContent()
                1 -> HorizontalSection("Case Studies", getCaseStudies())
                2 -> HorizontalSection("Blogs", getBlogs())
                3 -> HorizontalSection("Skill Support", getSkillSupport())
            }
        }
    }
}

@Composable
fun PostCard(imageRes: Int, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .padding(4.dp)
            .clickable { /* Handle card click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
}

@Composable
fun HorizontalSection(title: String, posts: List<Post>) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(posts) { post ->
                PostCard(
                    imageRes = post.imageRes,
                    title = post.title,
                    description = post.description
                )
            }
        }
    }
}

@Composable
fun AllPostsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalSection("Case Studies", getCaseStudies())
        HorizontalSection("Blogs", getBlogs())
        HorizontalSection("Skill Support", getSkillSupport())
        HorizontalSection("Jobs", getJobs())
    }
}

// **Dummy Post Data**
data class Post(val imageRes: Int, val title: String, val description: String)

fun getCaseStudies(): List<Post> {
    return listOf(
        Post(R.drawable.arche, "Bangalore Airport", "Third Largest Airport..."),
        Post(R.drawable.arche, "Hyderabad City", "Cutting-Edge Smart..."),
        Post(R.drawable.arche, "Hyderabad City", "Cutting-Edge Smart..."),
        Post(R.drawable.arche, "Hyderabad City", "Cutting-Edge Smart..."),
        Post(R.drawable.arche, "Hyderabad City", "Cutting-Edge Smart..."),
        )
}

fun getBlogs(): List<Post> {
    return listOf(
        Post(R.drawable.netcon, "Data Center", "Mastering Data Center..."),
        Post(R.drawable.netcon, "Cloud", "Exploring Hybrid Cloud...")
    )
}

fun getSkillSupport(): List<Post> {
    return listOf(
        Post(R.drawable.arche, "L1-L2 Cyber Security", "Cybersecurity Essentials..."),
        Post(R.drawable.arche, "Wireless Networks", "Introduction to Wireless...")
    )
}

fun getJobs(): List<Post> {
    return listOf(
        Post(R.drawable.netcon, "Software Engineer", "Openings in Development..."),
        Post(R.drawable.netcon, "Data Analyst", "Analyze Business Data...")
    )
}


