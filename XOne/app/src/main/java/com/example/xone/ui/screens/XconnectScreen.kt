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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import com.example.xone.R
import com.example.xone.controller.SocialController
import com.example.xone.model.Job
import com.example.xone.model.SocialArticle

@Composable
fun XConnectScreen(onBackPressed: () -> Unit) {
    var selectedTab by remember { mutableStateOf("All Posts") }
    val socialController = remember { SocialController() }

    // Tab Options
    val tabs = listOf("All Posts", "Case Studies", "Blogs", "Jobs")

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
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Centered Title
                Text(
                    text = "XConnect",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 100.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            // Tabs Row (Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                tabs.forEach { tab ->
                    TabItem(
                        text = tab,
                        isSelected = selectedTab == tab,
                        onTabSelected = { selectedTab = tab }
                    )
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                "All Posts" -> AllPostsContent(socialController)
                "Case Studies" -> CaseStudiesContent(socialController)
                "Blogs" -> BlogsContent(socialController)
                "Jobs" -> JobsContent(socialController)
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onTabSelected: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF474749) else Color.White)
            .clickable { onTabSelected() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF474749),
            fontSize = 16.sp
        )
    }
}

@Composable
fun AllPostsContent(socialController: SocialController) {
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalSection(title = "Case Studies", articles = socialController.getCaseStudies())
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalSection(title = "Blogs", articles = socialController.getBlogs())
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalJobsSection(title = "Jobs", jobs = socialController.getJobPostings())
    }
}

@Composable
fun CaseStudiesContent(socialController: SocialController) {
    val caseStudies = socialController.getCaseStudies()
    if (caseStudies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No case studies available", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            caseStudies.forEach { article ->
                ArticleCard(article)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BlogsContent(socialController: SocialController) {
    val blogs = socialController.getBlogs()
    if (blogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No blogs available", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            blogs.forEach { article ->
                ArticleCard(article)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun JobsContent(socialController: SocialController) {
    val jobs = socialController.getJobPostings()
    if (jobs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No jobs available", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            jobs.forEach { job ->
                JobCard(job)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HorizontalSection(title: String, articles: List<SocialArticle>) {
    if (articles.isEmpty()) return
    
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
            articles.forEach { article ->
                PostCard(
                    title = article.title,
                    description = article.description,
                    imageUrl = article.imageUrl
                )
            }
        }
    }
}

@Composable
fun HorizontalJobsSection(title: String, jobs: List<Job>) {
    if (jobs.isEmpty()) return
    
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
            jobs.forEach { job ->
                PostCard(
                    title = job.Title,
                    description = job.Description,
                    imageUrl = job.Image
                )
            }
        }
    }
}

@Composable
fun ArticleCard(article: SocialArticle) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Handle card click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Use AsyncImage with error and loading states
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                // Show a placeholder while loading or if error
                error = painterResource(id = R.drawable.ic_back),
                placeholder = painterResource(id = R.drawable.ic_back)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun JobCard(job: Job) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Handle job click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Use AsyncImage with error and loading states
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(job.Image)
                    .crossfade(true)
                    .build(),
                contentDescription = job.Title,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                // Show a placeholder while loading or if error
                error = painterResource(id = R.drawable.ic_back),
                placeholder = painterResource(id = R.drawable.ic_back)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = job.Title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.Description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PostCard(title: String, description: String, imageUrl: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .padding(4.dp)
            .clickable { /* Handle card click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Use AsyncImage with error and loading states
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop,
                // Show a placeholder while loading or if error
                error = painterResource(id = R.drawable.ic_back),
                placeholder = painterResource(id = R.drawable.ic_back)
            )
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


