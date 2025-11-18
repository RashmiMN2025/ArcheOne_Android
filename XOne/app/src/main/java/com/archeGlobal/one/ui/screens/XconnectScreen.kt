package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.SocialController
import com.archeGlobal.one.model.SocialArticle
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveArticleGrid(
    articles: List<SocialArticle>,
    type: String,
    socialController: SocialController,
    showArticleDetail: (SocialArticle, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val columns =
        when (windowSizeClass?.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 2 // Phone: single column
            WindowWidthSizeClass.Medium -> 3 // Large phone/small tablet
            WindowWidthSizeClass.Expanded -> 4 // Tablet: 3 columns
            else -> 2
        }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        items(articles) { article ->
            ArticleCard(
                article = article,
                type = type,
                socialController = socialController,
                showArticleDetail = showArticleDetail,
            )
        }
    }
}

@Composable
fun XConnectScreen(
    onBackPressed: () -> Unit,
    onArticleSelected: (SocialArticle, String) -> Unit = { _, _ -> },
    initialTab: String = "All Posts",
) {
    val context = LocalContext.current
    val socialController = remember { SocialController(context) }
    var selectedTab by remember { mutableStateOf("All Posts") }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Variables for article detail dialog
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedArticle by remember { mutableStateOf<SocialArticle?>(null) }
    var selectedArticleType by remember { mutableStateOf("") }

    // Function to show article detail
    val showArticleDetail: (SocialArticle, String) -> Unit = { article, type ->
        // For non-job articles, use the new navigation flow
        if (type != "Jobs") {
            onArticleSelected(article, type)
        } else {
            // For jobs, keep the existing behavior (direct to WebView)
            socialController.openInBrowser(type, article.id)
        }
    }

    // Updated tab options
    val tabs = listOf("All Posts", "Case Studies", "Blogs")

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFFE0DCD1), // Light Beige
                                    Color(0xFFC8C8CA), // Light Gray
                                    Color(0xFF474749), // Dark Gray
                                ),
                        ),
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(bottom = 10.dp),
                ) {
                    // Back button at the left edge
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }

                    // Centered Title taking full width
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Blogs & Case Studies",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    // Empty spacer for balance (same width as back button)
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Tabs Row (Scrollable)
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center, // Center align the tabs
                ) {
                    tabs.forEach { tab ->
                        TabItem(
                            text = tab,
                            isSelected = selectedTab == tab,
                            onTabSelected = { selectedTab = tab },
                        )
                    }
                }

                // Search Bar
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                ),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp),
                            )

                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp),
                                singleLine = true,
                                textStyle =
                                    TextStyle( // Added textStyle for innerTextField
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Search,
                                    ),
                                keyboardActions =
                                    KeyboardActions(
                                        onSearch = {
                                            keyboardController?.hide()
                                        },
                                    ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search...",
                                                color = Color.Gray.copy(alpha = 0.6f),
                                                fontSize = 16.sp,
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )
                        }
                    }
                }

                // Content based on selected tab and search query
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        "All Posts" -> AllPostsContent(socialController, searchQuery, showArticleDetail)
                        "Case Studies" ->
                            CaseStudiesContent(
                                socialController,
                                searchQuery,
                                showArticleDetail,
                            )

                        "Blogs" -> BlogsContent(socialController, searchQuery, showArticleDetail)
                    }
                }
            }
        }
    }

    // Show article detail dialog if needed
    if (showDetailDialog && selectedArticle != null) {
        ArticleDetailDialog(
            article = selectedArticle!!,
            type = selectedArticleType,
            onDismiss = { showDetailDialog = false },
            onReadMore = {
                socialController.openInBrowser(selectedArticleType, selectedArticle!!.id)
                showDetailDialog = false
            },
        )
    }
}

@Composable
fun ArticleDetailDialog(
    article: SocialArticle,
    type: String,
    onDismiss: () -> Unit,
    onReadMore: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                // Header with close button
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDD3825))
                            .padding(16.dp),
                ) {
                    Text(
                        text = type,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                        )
                    }
                }

                // Image
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(article.imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                    contentDescription = article.title,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_image_placeholder),
                    placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                )

                // Content
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        text = article.title,
                        fontSize = 28.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    Text(
                        text = article.description,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        lineHeight = 24.sp,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Read More button
                    Button(
                        onClick = onReadMore,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(
                            text = "Read More",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onTabSelected: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .padding(end = 8.dp)
                .background(
                    color = if (isSelected) Color(0xFFDD3825) else Color.White, // Red background when selected
                    shape = RoundedCornerShape(8.dp), // Changed from 24.dp to 8.dp for less rounded corners
                ).clickable { onTabSelected() }
                .padding(horizontal = 16.dp, vertical = 8.dp), // Reduced padding for a more compact look
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 16.sp, // Slightly smaller font size
            fontFamily = GraphikFontFamily,
            fontWeight = if (isSelected) FontWeight.Normal else FontWeight.Medium,
        )
    }
}

@Composable
fun AllPostsContent(
    socialController: SocialController,
    searchQuery: String,
    showArticleDetail: (SocialArticle, String) -> Unit = { _, _ -> },
) {
    // Filter case studies, blogs and jobs based on search query - only titles/headers, not content
    val filteredCaseStudies =
        socialController.getCaseStudies().filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }

    val filteredBlogs =
        socialController.getBlogs().filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Added space after the search bar

        if (filteredCaseStudies.isNotEmpty()) {
            Text(
                text = "Case Studies",
                fontSize = 20.sp, // Increased font size
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(filteredCaseStudies) { caseStudy ->
                    PostTile(
                        post = caseStudy,
                        onClick = { showArticleDetail(caseStudy, "Case Studies") },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filteredBlogs.isNotEmpty()) {
            Text(
                text = "Blogs",
                fontSize = 20.sp, // Increased font size
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(filteredBlogs) { blog ->
                    PostTile(
                        post = blog,
                        onClick = { showArticleDetail(blog, "Blogs") },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // If all sections are empty after filtering, show a message
        if (filteredCaseStudies.isEmpty() && filteredBlogs.isEmpty() && searchQuery.isNotEmpty()) {
            NoResultsCard(
                searchQuery = searchQuery,
                message = "No results found",
            )
        }
    }
}

@Composable
fun PostTile(
    post: SocialArticle,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .width(200.dp) // Increased tile width
                .height(250.dp) // Increased tile height
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                contentDescription = post.title,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Adjusted image height
                        .clip(RoundedCornerShape(12.dp)),
                // Added rounded corners for the image
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                error = painterResource(id = R.drawable.ic_image_placeholder),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 12.dp, 12.dp, 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = post.title,
                    fontSize = 14.sp, // Increased font size for titles
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 3,
                    overflow = TextOverflow.Visible,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun CaseStudiesContent(
    socialController: SocialController,
    searchQuery: String,
    showArticleDetail: (SocialArticle, String) -> Unit = { _, _ -> },
) {
    val caseStudies =
        socialController.getCaseStudies().filter {
            searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
        }

    if (caseStudies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NoResultsCard(
                searchQuery = if (searchQuery.isEmpty()) "" else searchQuery,
                message =
                    if (searchQuery.isEmpty()) {
                        "No case studies available"
                    } else {
                        "No case studies found"
                    },
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Case Studies",
                fontSize = 20.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            ResponsiveArticleGrid(
                articles = caseStudies,
                type = "Case Studies",
                socialController = socialController,
                showArticleDetail = showArticleDetail,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun BlogsContent(
    socialController: SocialController,
    searchQuery: String,
    showArticleDetail: (SocialArticle, String) -> Unit = { _, _ -> },
) {
    val blogs =
        socialController.getBlogs().filter {
            searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
        }

    if (blogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NoResultsCard(
                searchQuery = if (searchQuery.isEmpty()) "" else searchQuery,
                message =
                    if (searchQuery.isEmpty()) {
                        "No blogs available"
                    } else {
                        "No blogs found"
                    },
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Blogs",
                fontSize = 20.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            ResponsiveArticleGrid(
                articles = blogs,
                type = "Blogs",
                socialController = socialController,
                showArticleDetail = showArticleDetail,
            )
        }
    }
}

@Composable
fun NoResultsCard(
    searchQuery: String = "",
    message: String = "No results found",
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Large magnifying glass icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "No results",
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main message
            Text(
                text = message,
                fontSize = 20.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
            )

            // Subtitle with search query if provided
            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We couldn't find any matches for '$searchQuery'",
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: SocialArticle,
    type: String,
    socialController: SocialController,
    showArticleDetail: (SocialArticle, String) -> Unit = { _, _ -> },
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier =
            Modifier
                .height(210.dp)
                .padding(8.dp)
                .clickable { showArticleDetail(article, type) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(article.imageUrl)
                        .crossfade(true)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                contentDescription = article.title,
                modifier =
                    Modifier
                        .height(125.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                // Added rounded corners for the image
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_image_placeholder),
                placeholder = painterResource(id = R.drawable.ic_image_placeholder),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 12.dp, 12.dp, 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = article.title,
                    fontSize = 11.sp, // slightly smaller
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 3,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
