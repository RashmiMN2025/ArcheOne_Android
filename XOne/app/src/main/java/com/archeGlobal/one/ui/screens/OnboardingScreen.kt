package com.archeGlobal.one.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    // Onboarding pages data
    val pages = listOf(
        OnboardingPage(
            image = R.drawable.first,
            title = "Workplace, Simplified",
            description = "",
            showLogo = true,
            useBlackText = false
        ),
        OnboardingPage(
            image = R.drawable.second,
            title = "All-in-One Workforce Platform",
            description = "Unite Your Workforce, Seamlessly.",
            showLogo = false,
            useBlackText = false
        ),
        OnboardingPage(
            image = R.drawable.third, // Using third.jpeg as requested
            title = "AI Powered, Digital First",
            description = "Where Engagement Meets Impact.",
            showLogo = false,
            useBlackText = true // Use black text for third page
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Pager for onboarding screens
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(
                page = pages[page],
                isLastPage = page == pages.size - 1,
                onGetStartedClick = onGetStartedClick
            )
        }

        // Page indicator - moved higher up
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp), // Increased from 32dp to move indicators up
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Next button - aligned with dots
        AnimatedVisibility(
            visible = currentPage < pages.size - 1,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 60.dp) // Aligned with dots' bottom padding
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentPage + 1)
                    }
                },
                modifier = Modifier
                    .size(40.dp) // Smaller circle (was 48dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward, // Using Material icon for better arrow
                    contentDescription = "Next",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp) // Smaller icon
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(
    page: OnboardingPage,
    isLastPage: Boolean = false,
    onGetStartedClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = page.image),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Add appropriate spacing at the top based on whether it's the logo page
            Spacer(modifier = Modifier.height(if (page.showLogo) 70.dp else 0.dp))
            
            // For non-logo pages, we need to center the content but position slightly higher
            if (!page.showLogo) {
                Spacer(modifier = Modifier.weight(0.2f)) // Further reduced to move content even higher
            }

            // Arche Logo (if showLogo is true)
            if (page.showLogo) {
                Image(
                    painter = painterResource(id = R.drawable.arche2),
                    contentDescription = "Arche Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(bottom = 12.dp)
                )
            }
            
            // Title with custom formatting based on page
            Text(
                text = page.title,
                color = if (page.useBlackText) Color.Black else Color.White,
                fontSize = if (page.showLogo) 18.sp else if (page.useBlackText) 24.sp else 28.sp, // Smaller for third page
                fontFamily = GraphikFontFamily,
                fontWeight = if (page.showLogo) FontWeight.Normal else FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = if (page.title.contains("All-in-One")) 36.sp else 32.sp, // Increased line height for second page
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Description (if not empty)
            if (page.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = page.description,
                    color = if (page.useBlackText) Color.Black else Color.White,
                    fontSize = if (page.title.contains("All-in-One")) 16.sp else if (page.useBlackText) 14.sp else 18.sp, // Smaller text for third page
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Get Started button (only on last page)
            if (isLastPage) {
                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

data class OnboardingPage(
    val image: Int,
    val title: String,
    val description: String,
    val showLogo: Boolean = false,
    val useBlackText: Boolean = false
)
