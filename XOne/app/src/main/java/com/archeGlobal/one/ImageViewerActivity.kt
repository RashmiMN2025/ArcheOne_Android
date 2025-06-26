package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.archeGlobal.one.ui.components.UniversalLoader

class ImageViewerActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUrl = intent.getStringExtra("fileUrl") ?: ""
        val title = intent.getStringExtra("title") ?: "Image"
        var isLoading by mutableStateOf(true)
        var scale by mutableStateOf(1f)
        var offset by mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)

        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE0DCD1), // Light Grey/Beige
                                    Color(0xFFC8C8CA), // Medium Grey
                                    Color(0xFF474749) // Dark Grey
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top app bar
                        TopAppBar(
                            title = {
                                Text(
                                    text = title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.Black
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Image content in a Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {
                                                detectTransformGestures { centroid, pan, zoom, _ ->
                                                    // Calculate new scale
                                                    val newScale = (scale * zoom).coerceIn(1f, 3f)

                                                    // Only update if we're zooming in or if we're already zoomed in
                                                    if (newScale > 1f || scale > 1f) {
                                                        scale = newScale

                                                        // If we're zooming out completely, reset the offset
                                                        if (scale <= 1f) {
                                                            offset = androidx.compose.ui.geometry.Offset.Zero
                                                        } else {
                                                            // Apply pan only when zoomed in
                                                            offset += pan
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = title,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = scale,
                                                    scaleY = scale,
                                                    translationX = offset.x,
                                                    translationY = offset.y
                                                ),
                                            contentScale = ContentScale.Fit,
                                            onLoading = { isLoading = true },
                                            onSuccess = { isLoading = false },
                                            onError = { isLoading = false }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Show UniversalLoader while loading
                    if (isLoading) {
                        UniversalLoader(isLoading = true)
                    }
                }
            }
        }

        this.setTheme(R.style.Theme_XOne)
    }
}
