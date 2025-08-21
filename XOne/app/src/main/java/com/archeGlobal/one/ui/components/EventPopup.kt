package com.archeGlobal.one.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun EventPopup(
    event: EventResponse,
    onDismiss: () -> Unit
) {
    android.util.Log.d("EventPopup", "Rendering EventPopup with data: Title=${event.title}, Image=${event.image}")
    android.util.Log.d("EventPopup", "Event details: Date=${event.date}, Description=${event.description?.take(50) ?: "N/A"}...")
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF6F4EE)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(top = 20.dp)
                ) {
                    val painter = rememberAsyncImagePainter(event.image)
                    val state = painter.state

                    Image(
                        painter = painter,
                        contentDescription = event.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Fit
                    )

                    // Log image loading state
                    when (state) {
                        is coil.compose.AsyncImagePainter.State.Loading -> {
                            android.util.Log.d("EventPopup", "Image loading: ${event.image}")
                        }
                        is coil.compose.AsyncImagePainter.State.Success -> {
                            android.util.Log.d("EventPopup", "Image loaded successfully: ${event.image}")
                        }
                        is coil.compose.AsyncImagePainter.State.Error -> {
                            val error = (state as coil.compose.AsyncImagePainter.State.Error).result.throwable
                            android.util.Log.e("EventPopup", "Image loading failed: ${event.image}", error)
                        }
                        else -> {
                            android.util.Log.d("EventPopup", "Image in unknown state: ${event.image}")
                        }
                    }
                }

                // Title
                Text(
                    text = event.title ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                // Date
                Text(
                    text = event.date ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = event.description ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
                    fontSize = 15.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(46.dp)
                        .width(110.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Close",
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }
    }
}
