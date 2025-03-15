package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.model.SosBlogModel

@Composable
fun SOSDetailScreen(blog: SosBlogModel, onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF9EC))
    ) {
        Column {
            // 🔝 Fixed Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF9EC)) // Keeps the header color
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top =40.dp)
            ) {
                IconButton(onClick = onBackPressed) { // ⬅️ Navigates back to the previous screen
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = blog.name, // 🏷️ Use blog name as title
                    fontSize = 20.sp,
                    color = Color.Black,
                )
            }

            // 📜 Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 🖼️ Enlarged Blog Image
                Image(
                    painter = rememberAsyncImagePainter(blog.imageUrl),
                    contentDescription = blog.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Increased height for better visibility
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 📝 Blog Name
                Text(
                    text = blog.name,
                    fontSize = 25.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 📜 Blog Description
                Text(
                    text = blog.description,
                    fontSize = 15.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 📌 Displaying Blog Details with Proper Formatting
                blog.details.forEach { detail ->
                    Text(
                        text = detail.title, // 🏷️ Title in Black
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detail.description, // 📜 Description as a paragraph in Gray
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp) // Proper spacing for readability
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
