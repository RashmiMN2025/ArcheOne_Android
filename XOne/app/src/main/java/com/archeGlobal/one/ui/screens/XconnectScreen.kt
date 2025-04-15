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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


@Composable
fun XConnectScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val socialController = remember { SocialController(context) }
    var selectedTab by remember { mutableStateOf("All Posts") }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                // Back button at the left edge
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Centered Title taking full width
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Connect",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Empty spacer for balance (same width as back button)
                Spacer(modifier = Modifier.size(48.dp))
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
            
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(width = 1.dp, color = Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search...",
                                            color = Color.Gray.copy(alpha = 0.6f),
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Content based on selected tab and search query
            when (selectedTab) {
                "All Posts" -> AllPostsContent(socialController, searchQuery)
                "Case Studies" -> CaseStudiesContent(socialController, searchQuery)
                "Blogs" -> BlogsContent(socialController, searchQuery)
                "Jobs" -> JobsContent(socialController, searchQuery)
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
fun AllPostsContent(socialController: SocialController, searchQuery: String) {
    // Filter case studies, blogs and jobs based on search query - only titles/headers, not content
    val filteredCaseStudies = socialController.getCaseStudies().filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }
    
    val filteredBlogs = socialController.getBlogs().filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }
    
    val filteredJobs = socialController.getJobPostings().filter {
        it.Title.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (filteredCaseStudies.isNotEmpty()) {
            HorizontalSection(title = "Case Studies", articles = filteredCaseStudies)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (filteredBlogs.isNotEmpty()) {
            HorizontalSection(title = "Blogs", articles = filteredBlogs)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (filteredJobs.isNotEmpty()) {
            HorizontalJobsSection(title = "Jobs", jobs = filteredJobs)
        }
        
        // If all sections are empty after filtering, show a message
        if (filteredCaseStudies.isEmpty() && filteredBlogs.isEmpty() && filteredJobs.isEmpty() && searchQuery.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results found for '$searchQuery'",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CaseStudiesContent(socialController: SocialController, searchQuery: String) {
    val caseStudies = socialController.getCaseStudies().filter {
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
    }
    
    if (caseStudies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchQuery.isEmpty()) "No case studies available" 
                       else "No case studies found for '$searchQuery'",
                color = Color.Gray
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Case Studies",
                fontSize = 18.sp,  // Reduced from 20sp to 18sp
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
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
fun BlogsContent(socialController: SocialController, searchQuery: String) {
    val blogs = socialController.getBlogs().filter {
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
    }
    
    if (blogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchQuery.isEmpty()) "No blogs available" 
                       else "No blogs found for '$searchQuery'",
                color = Color.Gray
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Blogs",
                fontSize = 18.sp,  // Reduced from 20sp to 18sp
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
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
fun JobsContent(socialController: SocialController, searchQuery: String) {
    val jobs = socialController.getJobPostings().filter {
        searchQuery.isEmpty() || it.Title.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Ensure uniform left & right padding
    ) {
        Text(
            text = "Jobs",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (jobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "No jobs available"
                    else "No jobs found for '$searchQuery'",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            jobs.forEach { job ->
                JobCard(job = job, socialController = socialController)
            }
        }
    }
}

@Composable
fun JobCard(job: Job, socialController: SocialController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth() // Ensures card takes full width of parent Column
            .padding(horizontal = 16.dp, vertical = 8.dp) // Uniform padding
            .clickable {
                socialController.openInBrowser("Jobs", job.Slug)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),  // Uniform padding inside the card
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(job.Image)
                    .crossfade(true)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = job.Title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_image_placeholder),
                placeholder = painterResource(id = R.drawable.ic_image_placeholder)
            )

            Text(
                text = job.Title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Experience: ${extractExperience(job.Description)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF505050),
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Button(
                onClick = { socialController.openInBrowser("Jobs", job.Slug) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Apply",
                    fontSize = 14.sp,
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
            fontSize = 18.sp,  // Reduced from 20sp to 18sp
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
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
            fontSize = 18.sp,  // Reduced from 20sp to 18sp
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp)
        ) {
            jobs.forEach { job ->
                JobPostCard(
                    title = job.Title,
                    description = "Experience: ${extractExperience(job.Description)}",
                    imageUrl = job.Image,
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
fun JobPostCard(
    title: String,
    description: String,
    imageUrl: String,
    slug: String,
    socialController: SocialController
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(200.dp)
            .height(240.dp)
            .padding(4.dp)
            .clickable {
                socialController.openInBrowser("Jobs", slug)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Taller image for job cards
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = title,
                modifier = Modifier
                    .height(160.dp) // Increased height from 120dp to 160dp
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_image_placeholder),
                placeholder = painterResource(id = R.drawable.ic_image_placeholder)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 8.dp, 8.dp, 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Ensures central alignment
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center, // Centers text
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    lineHeight = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center, // Centers text
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
fun ArticleCard(article: SocialArticle, type: String, socialController: SocialController) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { 
                socialController.openInBrowser(type, article.id)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image section with placeholder
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_image_placeholder),
                placeholder = painterResource(id = R.drawable.ic_image_placeholder)
            )
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Center align the content
            ) {
                Text(
                    text = article.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center // Center align the text
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Use AnnotatedString to style "Read More" differently
                Text(
                    text = buildAnnotatedString {
                        // Truncate description if needed
                        val truncatedDescription = if (article.description.length > 150) {
                            article.description.substring(0, 150)
                        } else {
                            article.description
                        }
                        
                        append(truncatedDescription)
                        append("... ")
                        withStyle(style = SpanStyle(
                            color = Color(0xFFDD3825),  // Red color
                            fontWeight = FontWeight.Bold  // Make it bold for better visibility
                        )) {
                            append("Read More")
                        }
                    },
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 4,  // Increased from 3 to 4 to ensure "Read More" is visible
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
        shape = RoundedCornerShape(10.dp),
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
            // Use AsyncImage with improved placeholder
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = title,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_image_placeholder),
                placeholder = painterResource(id = R.drawable.ic_image_placeholder)
            )
            
            Column(
                modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 4.dp), // Reduced bottom padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp)) // Reduced spacing
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    lineHeight = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


