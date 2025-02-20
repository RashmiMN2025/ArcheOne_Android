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

@Composable
fun XConnectScreen(onBackPressed: () -> Unit) {
    var selectedTab by remember { mutableStateOf("All Posts") }

    // Tab Options
    val tabs = listOf("All Posts", "Case Studies", "Blogs", "Skill Support", "Jobs")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
            .verticalScroll(rememberScrollState()) // Make the whole screen scrollable
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
            ) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back), // Add menu icon in drawable folder
                        contentDescription = "Menu",
                        tint = Color.Black
                    )
                }

                // **Centered Title**
                Text(
                    text = "XConnect",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 100.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            // **Tabs Row (Scrollable)**
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .horizontalScroll(rememberScrollState()) // Make tabs scrollable
            ) {
                tabs.forEach { tab ->
                    Button(
                        onClick = { selectedTab = tab },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == tab) Color(0xFFFF5C5C) else Color(0xFFE0E0E0),
                            contentColor = if (selectedTab == tab) Color.White else Color.Black
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(text = tab)
                    }
                }
            }

            // **Content Section**
            when (selectedTab) {
                "All Posts" -> AllPostsContent()
                "Case Studies" -> HorizontalSection(title = "Case Studies", posts = getCaseStudies())
                "Blogs" -> HorizontalSection(title = "Blogs", posts = getBlogs())
                "Skill Support" -> HorizontalSection(title = "Skill Support", posts = getSkillSupport())
                "Jobs" -> HorizontalSection(title = "Jobs", posts = getJobs())
            }
        }
    }
}

@Composable
fun AllPostsContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalSection(title = "Case Studies", posts = getCaseStudies())
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalSection(title = "Blogs", posts = getBlogs())
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalSection(title = "Skill Support", posts = getSkillSupport())
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalSection(title = "Jobs", posts = getJobs())
    }
}

@Composable
fun HorizontalSection(title: String, posts: List<Post>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()) // Make each section horizontally scrollable
        ) {
            posts.forEach { post ->
                PostCard(
                    imageRes = post.imageRes,
                    title = post.title,
                    description = post.description
                )
            }
        }
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

@Composable
fun PostCard(imageRes: Int, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(20.dp), // Increased rounded corners
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .padding(4.dp) // Added space between cards
            .clickable { /* Handle card click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center, // Align content in the center
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth() // Ensure image fills the card width
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp)) // Increased rounded corners for image
            )
            Spacer(modifier = Modifier.height(4.dp)) // Reduced space between image and text
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}


