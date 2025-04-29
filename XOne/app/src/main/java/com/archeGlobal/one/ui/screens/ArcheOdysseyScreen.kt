package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ArcheOdysseyController
import com.archeGlobal.one.VisionActivity
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcheOdysseyScreen(
    controller: ArcheOdysseyController,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
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
                .padding(16.dp)
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp) // Moved heading and back arrow slightly down
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "Arche Odyssey",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Add tiles in a 2x2 matrix
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Tile(
    heading = "Core Values",
    text = "Our guiding principles",
    icon = painterResource(id = R.drawable.core_values),
    modifier = Modifier.weight(1f),
    onClick = { controller.onCoreValuesClick() } // Trigger navigation
)
                    Tile(
                        heading = "Vision",
                        text = "Future aspirations",
                        icon = painterResource(id = R.drawable.vision),
                        modifier = Modifier.weight(1f),
                        onClick = { controller.onVisionClick() }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Tile(
                        heading = "Communique",
                        text = "Latest updates",
                        icon = painterResource(id = R.drawable.communique),
                        modifier = Modifier.weight(1f),
                        onClick = { controller.onCommuniqueClick() }
                    )
                    Tile(
                        heading = "About Us",
                        text = "Who we are & what we stand for",
                        icon = painterResource(id = R.drawable.about_us),
                        modifier = Modifier.weight(1f),
                        onClick = { controller.onAboutUs() }
                    )
                }
            }
        }
    }
}

@Composable
fun Tile(heading: String, text: String, icon: Painter, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .size(width = 120.dp, height = 200.dp) // Explicit width and height for tiles
            .shadow(4.dp, shape = MaterialTheme.shapes.medium, clip = false) // Adjusted shadow to appear outside
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .clickable(enabled = onClick != null) {
                Log.d("Tile", "Tile clicked: $heading") // Added logging for click event
                onClick?.invoke()
            }, // Added clickable behavior
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(4.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = "$heading Icon",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = heading,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 0.dp).fillMaxWidth(), // Removed space between text lines
                lineHeight = 16.sp // Ensures compact line spacing
            )
        }
    }
}
