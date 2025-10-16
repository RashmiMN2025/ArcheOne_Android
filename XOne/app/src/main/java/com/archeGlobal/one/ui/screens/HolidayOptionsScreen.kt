package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HolidayOptionsController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@Composable
fun HolidayOptionsScreen(controller: HolidayOptionsController) {
    // Handle back button press
    BackHandler {
        controller.onBackPressed()
    }

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
                        brush =
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors =
                                    listOf(
                                        WelcomeBackgroundTop,
                                        WelcomeBackgroundMiddle,
                                        WelcomeBackgroundBottom,
                                    ),
                            ),
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(4.dp, 12.dp, 4.dp, 0.dp), // Minimized horizontal padding
            ) {
                // Top App Bar
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                ) {
                    // Back button aligned to start
                    IconButton(
                        onClick = { controller.onBackPressed() },
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                    // Title centered
                    Text(
                        text = "Calendar",
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }

            // Two option cards side by side
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                // Holiday Calendar Option
                OptionCard(
                    title = "Holiday Calendar", // Added line break to display on two lines
                    subtitle = "Company Holidays",
                    iconResId = R.drawable.holiday2,
                    onClick = { controller.navigateToHolidayCalendar() },
                    modifier =
                        Modifier
                            .width(200.dp)
                            .padding(top = 125.dp),
                )

                // Kudos Option
//                OptionCard(
//                    title = "Kudos",
//                    subtitle = "Celebrate Peers",
//                    iconResId = R.drawable.kudos,
//                    onClick = { controller.navigateToKudos() },
//                    modifier = Modifier.weight(1f)
//                )
            }
        }
    }
}

@Composable
fun OptionCard(
    title: String,
    subtitle: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .height(170.dp)
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            // Reduced padding from 16dp to 12dp
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon on top
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier =
                    Modifier
                        .size(50.dp) // Reduced from 60dp to 50dp
                        .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp)) // Reduced from 12dp to 8dp            // Title below icon
            Text(
                text = title,
                fontSize = 14.sp, // Further reduced from 15.sp to 14.sp
                fontWeight = FontWeight.SemiBold,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                maxLines = 2, // Changed from 1 to allow wrapping
                textAlign = TextAlign.Center,
                lineHeight = 18.sp, // Add line height to compress text vertically
            )

            Spacer(modifier = Modifier.height(1.dp)) // Further reduced from 2dp to 1dp

            // Subtitle below title
            Text(
                text = subtitle,
                fontSize = 11.sp, // Further reduced from 12.sp to 11.sp
                fontWeight = FontWeight.Normal,
                fontFamily = GraphikFontFamily,
                color = Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}
