package com.archeGlobal.one.ui.screens

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ArcheOdysseyController
import com.archeGlobal.one.ui.theme.GraphikFontFamily

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
                .padding(10.dp)
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 10.dp) // Moved heading and back arrow slightly down
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
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
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Centered Tiles
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter // Center the tiles in the available space
            ) {
                // Add tiles in a 2x2 matrix
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
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
}

@Composable
fun Tile(heading: String, text: String, icon: Painter, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .size(width = 80.dp, height = 180.dp) // Explicit width and height for tiles
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
            modifier = modifier.padding(6.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = "$heading Icon",
                tint = Color.Black,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = heading,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 0.dp).fillMaxWidth(0.9f), // Removed space between text lines
                lineHeight = 16.sp // Ensures compact line spacing
            )
        }
    }
}
