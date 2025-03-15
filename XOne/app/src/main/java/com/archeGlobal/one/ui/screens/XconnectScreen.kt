package com.archeGlobal.one.ui.screens

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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.SocialController
import com.archeGlobal.one.model.Job
import com.archeGlobal.one.model.SocialArticle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun XConnectScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val socialController = remember { SocialController(context) }
    var selectedTab by remember { mutableStateOf("All Posts") }

    // Updated tab options
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
                    text = "Connect",
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
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            .background(
                color = if (isSelected) Color(0xFFDD3825) else Color.White,  // Red background when selected
                shape = RoundedCornerShape(8.dp)  // Changed from 24.dp to 8.dp for less rounded corners
            )
            .clickable { onTabSelected() }
            .padding(horizontal = 16.dp, vertical = 8.dp)  // Reduced padding for a more compact look
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,  // Slightly smaller font size
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
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
                ArticleCard(
                    article = article,
                    type = "Case Studies",
                    socialController = socialController
                )
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
                ArticleCard(
                    article = article,
                    type = "Blogs",
                    socialController = socialController
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun JobsContent(socialController: SocialController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "Jobs",
            fontSize = 22.sp,  // Slightly smaller from 24.sp
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start  // Left align the title
        )
        
        val jobs = socialController.getJobPostings()
        if (jobs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No jobs available", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                jobs.forEach { job ->
                    JobCard(job = job, socialController = socialController)
                }
            }
        }
    }
}

@Composable
fun JobCard(job: Job, socialController: SocialController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 8.dp)
            .clickable { 
                socialController.openInBrowser("Jobs", job.Slug)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Job Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(job.Image)
                    .crossfade(true)
                    .build(),
                contentDescription = job.Title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_back),
                placeholder = painterResource(id = R.drawable.ic_back)
            )
            
            // Job Title
            Text(
                text = job.Title,
                fontSize = 18.sp,  // Smaller from 20.sp
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
            
            // Experience
            Text(
                text = "Experience : ${extractExperience(job.Description)}",
                fontSize = 14.sp,  // Smaller from 16.sp
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
            
            // Apply Button
            Button(
                onClick = { socialController.openInBrowser("Jobs", job.Slug) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)  // Wider button (from 0.8f)
                    .padding(top = 16.dp),  // Increased top padding from 12.dp
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Apply",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// Helper function to extract experience from description
private fun extractExperience(description: String): String {
    // Try to find experience mention in the description
    val experiencePattern = "(\\d+[-]\\d+\\s*(?:years|yrs))".toRegex(RegexOption.IGNORE_CASE)
    val match = experiencePattern.find(description)
    return match?.value ?: "Not specified"
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
                .horizontalScroll(rememberScrollState())
        ) {
            articles.forEach { article ->
                PostCard(
                    title = article.title,
                    description = article.description,
                    imageUrl = article.imageUrl,
                    type = title,
                    slug = article.id,
                    socialController = LocalContext.current.let { 
                        remember { SocialController(it) }
                    }
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
                .horizontalScroll(rememberScrollState())
        ) {
            jobs.forEach { job ->
                PostCard(
                    title = job.Title,
                    description = job.Description,
                    imageUrl = job.Image,
                    type = "Jobs",
                    slug = job.Slug,
                    socialController = LocalContext.current.let { 
                        remember { SocialController(it) }
                    }
                )
            }
        }
    }
}

@Composable
fun ArticleCard(article: SocialArticle, type: String, socialController: SocialController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { 
                socialController.openInBrowser(type, article.id)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image section
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_back),
                placeholder = painterResource(id = R.drawable.ic_back)
            )
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = article.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 4,  // Show 4 lines
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Overlay "Read More" at the bottom right with background
                    Text(
                        text = "Read More",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDD3825),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(top = 54.dp)  // Position at fourth line (3 lines × 18sp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0f),
                                        Color.White
                                    ),
                                    startX = -40f
                                )
                            )
                            .padding(start = 40.dp, end = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    title: String, 
    description: String, 
    imageUrl: String,
    type: String,
    slug: String,
    socialController: SocialController
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .padding(4.dp)
            .clickable { 
                socialController.openInBrowser(type, slug)
            },
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


