package com.archeGlobal.one.ui.screens

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
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import androidx.activity.compose.BackHandler

@Composable
fun HolidayOptionsScreen(
    controller: HolidayOptionsController
) {
    // Handle back button press
    BackHandler {
        controller.onBackPressed()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {            // Add top padding to push everything down
            Spacer(modifier = Modifier.height(30.dp))
            
            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = { controller.onBackPressed() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                              // Title
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Calendar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                      // Empty space to balance the layout
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Two option cards side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {                // Holiday Calendar Option
                OptionCard(
                    title = "Holiday\nCalendar", // Added line break to display on two lines
                    subtitle = "Company Holidays",
                    iconResId = R.drawable.holiday2,
                    onClick = { controller.navigateToHolidayCalendar() },
                    modifier = Modifier.weight(1f)
                )
                
                // Kudos Option
                OptionCard(
                    title = "Kudos",
                    subtitle = "Celebrate Peers",
                    iconResId = R.drawable.kudos,
                    onClick = { controller.navigateToKudos() },
                    modifier = Modifier.weight(1f)
                )
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp), // Reduced padding from 16dp to 12dp
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon on top
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier
                    .size(50.dp) // Reduced from 60dp to 50dp
                    .clip(RoundedCornerShape(8.dp))
            )
              Spacer(modifier = Modifier.height(8.dp)) // Reduced from 12dp to 8dp            // Title below icon
            Text(
                text = title,
                fontSize = 14.sp, // Further reduced from 15.sp to 14.sp
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 2, // Changed from 1 to allow wrapping
                textAlign = TextAlign.Center,
                lineHeight = 18.sp // Add line height to compress text vertically
            )
            
            Spacer(modifier = Modifier.height(1.dp)) // Further reduced from 2dp to 1dp
            
            // Subtitle below title
            Text(
                text = subtitle,
                fontSize = 11.sp, // Further reduced from 12.sp to 11.sp
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
