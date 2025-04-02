package com.archeGlobal.one.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.SOSController
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.ui.components.FooterScaffold
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.activity.compose.BackHandler
@Composable
fun SOSScreen(
    controller: SOSController,
    onNavigateToRaiseConcern: () -> Unit,
    onBackPressed: () -> Unit,
    onSOSBlogClick: (SosBlogModel) -> Unit,
    onNavigateToEmergencyContact: () -> Unit = {},
    onFooterHomeClick: () -> Unit = {},
    onFooterChatClick: () -> Unit = {},
    onFooterSOSClick: () -> Unit = {},
    onFooterProfileClick: () -> Unit = {},
    showHeader: Boolean = false // Default to true
) {
    val sosBlogs by controller.sosBlogs.collectAsState()
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Create FooterNavigationModel with SOS selected
    val footerNavigation = FooterNavigationModel(
        showHome = false,
        showChat = false,
        showSOS = true,
        showProfile = false
    )

    // Wrap with FooterScaffold for bottom navigation
    if (showHeader) {
        // Disable back swipe gesture and back button
        BackHandler(enabled = true) {
            // Do nothing to prevent navigation
        }
        FooterScaffold(
            footerNavigation = footerNavigation,
            onFooterHomeClick = onFooterHomeClick,
            onFooterChatClick = onFooterChatClick,
            onFooterSOSClick = onFooterSOSClick,
            onFooterProfileClick = onFooterProfileClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                        )
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 16.dp), // Add bottom padding to ensure content is visible above the navigation bar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    // White Box for SOS Assistance & SOS Information
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Spacer(modifier = Modifier.height(5.dp))
                            // SOS Assistance
                            Text(
                                text = "SOS Assistance",
                                fontSize = 22.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            SOSButton(text = "SOS Call", onClick = { controller.makeSOSCall("1234567890") })
                            SOSButton(text = "Raise a Concern", onClick = { onNavigateToRaiseConcern() })
                            SOSButton(
                                text = "View Emergency Contact",
                                onClick = {
                                    Log.d("SOSScreen", "View Emergency Contact button clicked")
                                    onNavigateToEmergencyContact()
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // SOS Information Section
                            Text(
                                text = "SOS Information",
                                fontSize = 22.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Pager for blogs (Only one blog visible at a time)
                            HorizontalPager(
                                count = sosBlogs.size,
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                SOSBlogItem(sosBlogs[page], onClick = { onSOSBlogClick(sosBlogs[page]) })
                            }

                            // Pagination Dots
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                sosBlogs.forEachIndexed { index, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == pagerState.currentPage) 15.dp else 15.dp) // Active dot is bigger
                                            .padding(4.dp)
                                            .background(
                                                color = if (index == pagerState.currentPage) Color(0xFFDD3825) else Color.LightGray,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp), // Add bottom padding to ensure content is visible above the navigation bar
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with back button
//                if(showHeader) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    ) {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }

                        Text(
                            text = "SOS",
                            color = Color.Black,
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Add an invisible spacer with same size as back button for balance
                        Spacer(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.CenterEnd)
                        )
//                    }
                }

                // White Box for SOS Assistance & SOS Information
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.height(5.dp))
                        // SOS Assistance
                        Text(
                            text = "SOS Assistance",
                            fontSize = 22.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        SOSButton(text = "SOS Call", onClick = { controller.makeSOSCall("1234567890") })
                        SOSButton(text = "Raise a Concern", onClick = { onNavigateToRaiseConcern() })
                        SOSButton(
                            text = "View Emergency Contact",
                            onClick = {
                                Log.d("SOSScreen", "View Emergency Contact button clicked")
                                onNavigateToEmergencyContact()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // SOS Information Section
                        Text(
                            text = "SOS Information",
                            fontSize = 22.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pager for blogs (Only one blog visible at a time)
                        HorizontalPager(
                            count = sosBlogs.size,
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            SOSBlogItem(sosBlogs[page], onClick = { onSOSBlogClick(sosBlogs[page]) })
                        }

                        // Pagination Dots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            sosBlogs.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == pagerState.currentPage) 15.dp else 15.dp) // Active dot is bigger
                                        .padding(4.dp)
                                        .background(
                                            color = if (index == pagerState.currentPage) Color(0xFFDD3825) else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun SOSBlogItem(blog: SosBlogModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(320.dp) // Adjust width to match the design
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Replace Image with AsyncImage to add placeholder
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(blog.imageUrl)
                .crossfade(true)
                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .build(),
            contentDescription = blog.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(300.dp)
                .height(200.dp),
            error = painterResource(id = R.drawable.ic_image_placeholder),
            placeholder = painterResource(id = R.drawable.ic_image_placeholder)
        )
        Text(
            text = blog.name,
            fontSize = 17.sp,
            color = Color.Black, // Explicitly set to black for consistency
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = blog.description,
            fontSize = 12.sp,  // Increased readability
            color = Color.Gray,
            maxLines = 2,  // Restrict to 2 lines
            overflow = TextOverflow.Ellipsis,  // Show "..." if text is too long
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


@Composable
fun SOSButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp)
    ) {
        Text(text = text, color = Color.White)
    }
}
