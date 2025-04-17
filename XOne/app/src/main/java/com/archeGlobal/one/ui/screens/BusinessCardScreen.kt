package com.archeGlobal.one.ui.screens

import android.graphics.*
import android.util.TypedValue
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.BusinessCardController
import com.archeGlobal.one.model.BusinessCardModel
import androidx.compose.foundation.lazy.LazyColumn
import com.archeGlobal.one.ui.theme.TextPrimary
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalDensity
import com.archeGlobal.one.model.LocationInfo
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.network.Office
import com.archeGlobal.one.network.RegionalOffice

// Composable to display a QR code bitmap using Canvas
@Composable
private fun ComposeQRCodeImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun CustomTopAppBar(
    onBackPressed: () -> Unit,
    onShareClick: () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Column {
        Spacer(modifier = Modifier.height(statusBarPadding.calculateTopPadding()))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }

            // Title
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "My Business Card",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold

                )
            }

            // Share button
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessCardScreen(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    var showFrontSide by remember { mutableStateOf(true) }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val cardBounds = remember { mutableStateOf<android.graphics.Rect?>(null) }
    var newLocation by remember { mutableStateOf("") }

    // Create a LocationInfo object using the location string from businessCard
    // Use remember with businessCard.location as key to update when location changes
    val location = remember(businessCard.location) {
        // Try to find the full office address from the offices data
        val offices = OtpVerificationController.getOfficesData()
        val userLocation = businessCard.location.trim()

        // First check if there's an office with a matching country name
        val matchingOffice = offices?.find { office -> 
            office.country.equals(userLocation, ignoreCase = true) 
        }

        if (matchingOffice != null) {
            // Found a direct match with country
            LocationInfo(
                name = matchingOffice.country,
                companyName = matchingOffice.companyName ?: "Arche Global Pvt Ltd",
                address = matchingOffice.address,
                email = matchingOffice.email,
                hasMultipleLocations = false
            )
        } else {
            // Check if it's an Indian regional office
            val indiaOffice = offices?.find { office -> 
                office.country.equals("India", ignoreCase = true) 
            }
            val regionalOffice = indiaOffice?.regionaloffice?.find { office -> 
                office.region.contains(userLocation, ignoreCase = true) 
            }

            if (regionalOffice != null) {
                // Found a matching regional office
                LocationInfo(
                    name = regionalOffice.region,
                    companyName = regionalOffice.companyName ?: "Arche Global Pvt Ltd",
                    address = regionalOffice.address,
                    email = regionalOffice.email ?: indiaOffice.email,
                    hasMultipleLocations = false
                )
            } else {
                // Fallback to the original location string if no match found
                LocationInfo(
                    name = "",
                    companyName = "",
                    address = businessCard.location,
                    email = "",
                    hasMultipleLocations = false
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom
                    )
                )
            )
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CustomTopAppBar(
                onBackPressed = { controller.onBackPressed() },
                onShareClick = {
                    scope.launch {
                        cardBounds.value?.let { bounds ->
                            val bitmap = captureCardArea(view, bounds)
                            controller.onShareCard(bitmap)
                        }
                    }
                }
            )
        }

        item {
                // Business Card
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
                    .height(470.dp)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        cardBounds.value = android.graphics.Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        )
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                when {
                                    dragAmount < -50 && showFrontSide -> showFrontSide = false // Swipe left
                                    dragAmount > 50 && !showFrontSide -> showFrontSide = true  // Swipe right
                                }
                            }
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                    if (showFrontSide) {
                        // Front side
                Column(
                    modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Logo
                    Image(
                                painter = painterResource(id = R.drawable.arche_black),
                                contentDescription = "Arche Logo",
                                modifier = Modifier
                                    .size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(78.dp))

                            // Name and Designation
                    Text(
                        text = businessCard.name,
                                fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = businessCard.designation,
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Company Details and QR Code side by side
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Company Details
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                    Text(
                                        text = "Arche Global Pvt Ltd",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.email,
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Text(
                        text = businessCard.phone,
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = controller.businessCard.location,
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                // QR Code and arche text
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.width(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        businessCard.qrCode?.let { qrBitmap ->
                                            ComposeQRCodeImage(
                                                bitmap = qrBitmap,
                                                contentDescription = "QR Code",
                                                modifier = Modifier.size(70.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier.width(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "arche",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Back side
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Spacer(modifier = Modifier.height(0.dp))  // Small top spacing

                            // Text first (moved up)
                            Text(
                                text = "This could be the start of\nsomething great.",
                                fontSize = 15.sp,  // Smaller text size
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(80.dp))  // Reduced spacing

                            // Logo moved below text
                            Image(
                                painter = painterResource(id = R.drawable.arche_black),
                                contentDescription = "Arche Logo",
                                modifier = Modifier
                                    .size(82.dp)
                            )

                            Spacer(modifier = Modifier.height(80.dp))

                    Text(
                        text = businessCard.website,
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = location.address,
                                fontSize = 12.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                }
            }
        }

        item {
            Text(
                    text = if (showFrontSide) "Swipe to flip -->" else "<-- Swipe to flip",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold

            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            cardBounds.value?.let { bounds ->
                                // Capture both sides of the card and combine them
                                val combinedBitmap = captureBothSides(view, bounds, showFrontSide) { newShowFrontSide ->
                                    showFrontSide = newShowFrontSide
                                }
                                controller.onDownloadCard(combinedBitmap)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        "Download Card",
                        color = Color.White,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { controller.onEditLocation() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        "Edit Location",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Show Edit Location Dialog
    if (controller.showEditLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { controller.showEditLocationDialog.value = false },
            containerColor = Color(0xFFF5F5F5),
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Edit Location", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            text = {
                var isFocused by remember { mutableStateOf(false) }

                TextField(
                    value = newLocation,
                    onValueChange = { newLocation = it },
                    placeholder = { Text("Enter new location") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Black
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newLocation.isNotEmpty()) {
                            controller.onLocationUpdated(newLocation)
                            newLocation = ""
                        }
                        controller.showEditLocationDialog.value = false
                    }
                ) {
                    Text("Save", color = Color(0xFFDD3825))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { controller.showEditLocationDialog.value = false }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

private fun captureCardArea(view: View, cardBounds: android.graphics.Rect): Bitmap {
    // Take a screenshot of the entire view
    view.isDrawingCacheEnabled = true
    val fullBitmap = Bitmap.createBitmap(view.drawingCache)
    view.isDrawingCacheEnabled = false

    return try {
        // Create a bitmap with transparency support
        val result = Bitmap.createBitmap(
            cardBounds.width(),
            cardBounds.height(),
            Bitmap.Config.ARGB_8888
        )

        // Create a canvas to draw the cropped area
        val canvas = android.graphics.Canvas(result)

        // Create a paint object with anti-aliasing
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        // Create a path for rounded corners
        val path = android.graphics.Path().apply {
            // Add a rounded rectangle path
            val cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                view.resources.displayMetrics
            )
            addRoundRect(
                android.graphics.RectF(0f, 0f, cardBounds.width().toFloat(), cardBounds.height().toFloat()),
                cornerRadius,
                cornerRadius,
                android.graphics.Path.Direction.CW
            )
        }

        // Clip the canvas to the rounded rectangle path
        canvas.clipPath(path)

        // Draw the cropped portion of the original bitmap
        canvas.drawBitmap(
            fullBitmap,
            -cardBounds.left.toFloat(),
            -cardBounds.top.toFloat(),
            paint
        )

        result
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        fullBitmap
    }
}

private suspend fun captureBothSides(
    view: View, 
    cardBounds: android.graphics.Rect, 
    currentShowFrontSide: Boolean,
    updateShowFrontSide: (Boolean) -> Unit
): Bitmap {
    // Save the original state
    val originalShowFrontSide = currentShowFrontSide

    // Capture front side
    updateShowFrontSide(true)
    // Small delay to ensure UI updates
    kotlinx.coroutines.delay(300)
    val frontBitmap = captureCardArea(view, cardBounds)

    // Capture back side
    updateShowFrontSide(false)
    // Small delay to ensure UI updates
    kotlinx.coroutines.delay(300)
    val backBitmap = captureCardArea(view, cardBounds)

    // Restore original state
    updateShowFrontSide(originalShowFrontSide)

    // Define spacing between cards
    val spacingHeight = 100

    // Create a combined bitmap with front on top and back below, plus a separator
    val combinedHeight = frontBitmap.height + backBitmap.height + spacingHeight
    val combinedWidth = frontBitmap.width

    val combinedBitmap = Bitmap.createBitmap(
        combinedWidth,
        combinedHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(combinedBitmap)
    canvas.drawColor(android.graphics.Color.WHITE) // White background

    // Draw front card at the top
    canvas.drawBitmap(frontBitmap, 0f, 0f, null)

    // Draw a separator line between the cards
    val separatorPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.LTGRAY
        style = android.graphics.Paint.Style.FILL
    }

    // Draw a gray rectangle as separator
    val separatorRect = android.graphics.RectF(
        0f, 
        frontBitmap.height.toFloat(), 
        combinedWidth.toFloat(), 
        (frontBitmap.height + spacingHeight).toFloat()
    )
    canvas.drawRect(separatorRect, separatorPaint)

    // Draw back card below the separator
    canvas.drawBitmap(backBitmap, 0f, (frontBitmap.height + spacingHeight).toFloat(), null)

    return combinedBitmap
}
