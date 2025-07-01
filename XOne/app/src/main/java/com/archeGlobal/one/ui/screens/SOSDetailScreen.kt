package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.archeGlobal.one.R
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun SOSDetailScreen(
    blog: SosBlogModel,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF9EC))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 🔝 Fixed Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF9EC)) // Keeps the header color
                        .padding(top = 40.dp, bottom = 12.dp)
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) { // ⬅️ Navigates back to the previous screen
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Text(
                        text = blog.name, // 🏷️ Use blog name as title
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Add an invisible spacer with same size as back button for balance
                    Spacer(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterEnd)
                    )
                }

                // 📜 Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp) // Add extra padding at the bottom to prevent content from being hidden
                ) {
                    Spacer(modifier = Modifier.height(5.dp))

                    // 🖼️ Enlarged Blog Image with placeholder
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(blog.imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = blog.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) // Increased height for better visibility
                            .clip(RoundedCornerShape(16.dp)),
                        error = painterResource(id = R.drawable.ic_image_placeholder),
                        placeholder = painterResource(id = R.drawable.ic_image_placeholder)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 📝 Blog Name
                    Text(
                        text = blog.name,
                        fontSize = 24.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 📜 Blog Description - Changed to Black
                    Text(
                        text = blog.description,
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black, // Changed from Gray to Black
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 📌 Displaying Blog Details with Proper Formatting
                    blog.details.forEach { detail ->
                        Text(
                            text = detail.title, // 🏷️ Title in Black
                            fontSize = 20.sp,
                            color = Color.Black,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detail.description, // 📜 Description as a paragraph - Changed to Black
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray, // Changed from Gray to Black
                            modifier = Modifier.padding(bottom = 16.dp) // Proper spacing for readability
                        )
                    }

                    // Add extra space at the bottom to ensure content is not hidden
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
