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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
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
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalDensity
import com.archeGlobal.one.model.LocationInfo
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.network.Office
import com.archeGlobal.one.network.RegionalOffice
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import com.archeGlobal.one.ui.theme.GraphikFontFamily

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
                    text = "Business Card",
                    color = Color.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = GraphikFontFamily,
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
    var newLocation by remember(businessCard.location) { mutableStateOf(businessCard.location) }
    val context = LocalContext.current

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
                            val combinedBitmap = captureBothSides(view, bounds, showFrontSide) { newShowFrontSide ->
                                showFrontSide = newShowFrontSide
                            }
                            controller.onShareCard(combinedBitmap)
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
                    .fillMaxWidth(0.82f)
                    .height(420.dp)
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Logo
                    Image(
                                painter = painterResource(id = R.drawable.arche_black),
                                contentDescription = "Arche Logo",
                                modifier = Modifier
                                    .size(39.dp)
                            )

                            Spacer(modifier = Modifier.height(135.dp))

                            // Name and Designation
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black)) {
                                append(businessCard.name)
                                append("\n")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontSize = 9.sp, color = Color.Black)) {
                                append(businessCard.designation)
                            }
                        },
                        lineHeight = 15.sp
                    )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Company Details and QR Code side by side
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 45.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Company Details
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, color = Color.Black)) {
                                                append("Arche Global Pvt Ltd\n")
                                            }
                                            withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontSize = 10.sp, color = Color.Black)) {
                                                append(businessCard.email)
                                                append("\n")
                                                append(businessCard.phone)
                                                append("\n")
                                                append(controller.businessCard.location)
                                            }
                                        },
                                        lineHeight = 15.sp
                                    )
                                }

                                // QR Code and arche text
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .offset(x = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(72.dp)
                                            .offset(y = (-6).dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        businessCard.qrCode?.let { qrBitmap ->
                                            ComposeQRCodeImage(
                                                bitmap = qrBitmap,
                                                contentDescription = "QR Code",
                                                modifier = Modifier.size(76.dp),

                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier.width(54.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "arche",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            modifier = Modifier.offset(x = (-7).dp, y = (-8).dp)
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
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Spacer(modifier = Modifier.height(0.dp))  // Small top spacing

                            // Text first (moved up)
                            Text(
                                text = "This could be the start of something great.",
                                fontSize = 9.sp,
                                fontFamily = FontFamily(Font(R.font.canela_regular)),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(100.dp))  // Reduced spacing

                            // Logo moved below text
                            Image(
                                painter = painterResource(id = R.drawable.arche_black),
                                contentDescription = "Arche Logo",
                                modifier = Modifier
                                    .size(84.dp)
                            )

                            Spacer(modifier = Modifier.height(100.dp))

                    Text(
                        text = businessCard.website,
                        fontSize = 9.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = location.address,
                        fontSize = 8.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
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

                OutlinedTextField(
                    value = newLocation,
                    onValueChange = { newLocation = it },
                    placeholder = { Text("Enter new location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Black,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newLocation.isNotEmpty()) {
                            val offices = OtpVerificationController.getOfficesData()
                            val trimmedLocation = newLocation.trim()
                            
                            // Validate against office locations
                            val isValid = offices?.any { office ->
                                office.country.equals(trimmedLocation, true) || 
                                office.regionaloffice?.any { regional ->
                                    regional.region.contains(trimmedLocation, true)
                                } == true
                            } ?: false

                            if (!isValid) {
                                // Set default to Bangalore and show toast
                                val defaultLocation = "Bangalore"
                                Toast.makeText(
                                    context, 
                                    "Invalid location, defaulting to $defaultLocation",
                                    Toast.LENGTH_SHORT
                                ).show()
                                controller.onLocationUpdated(defaultLocation)
                            } else {
                                controller.onLocationUpdated(trimmedLocation)
                            }
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
    kotlinx.coroutines.delay(300)
    val frontBitmap = captureCardArea(view, cardBounds)

    // Capture back side
    updateShowFrontSide(false)
    kotlinx.coroutines.delay(300)
    val backBitmap = captureCardArea(view, cardBounds)

    // Restore original state
    updateShowFrontSide(originalShowFrontSide)

    // Define much larger spacing between cards for complete separation
    val spacingHeight = 180 // Much larger gap between cards
    val shadowSize = 15f // Shadow size for cards
    
    // Calculate dimensions for the combined bitmap with extra space for shadows
    val cardWidth = frontBitmap.width
    val cardHeight = frontBitmap.height
    val combinedHeight = (cardHeight * 2) + spacingHeight + (shadowSize * 4).toInt()
    val combinedWidth = cardWidth + (shadowSize * 4).toInt()
    
    // Create the combined bitmap with white background
    val combinedBitmap = Bitmap.createBitmap(
        combinedWidth,
        combinedHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(combinedBitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    
    // Calculate corner radius in pixels
    val cornerRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        16f,
        view.resources.displayMetrics
    )
    
    // Create a function to draw a card with shadow
    fun drawCardWithShadow(bitmap: Bitmap, x: Float, y: Float) {
        // Draw shadow first
        val shadowPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(40, 0, 0, 0)
            style = android.graphics.Paint.Style.FILL
            setShadowLayer(shadowSize, 0f, 6f, android.graphics.Color.argb(80, 0, 0, 0))
        }
        
        // Create rectangle for card with shadow
        val cardRect = android.graphics.RectF(
            x + shadowSize,
            y + shadowSize,
            x + cardWidth - shadowSize,
            y + cardHeight - shadowSize
        )
        
        // Draw shadow with rounded corners
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, shadowPaint)
        
        // Create rectangle for the actual card
        val cardRealRect = android.graphics.RectF(
            x + shadowSize,
            y + shadowSize,
            x + cardWidth - shadowSize,
            y + cardHeight - shadowSize
        )
        
        // Draw card background
        val cardPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(242, 242, 237) // Cream white like in image
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRealRect, cornerRadius, cornerRadius, cardPaint)
        
        // Create a clip path for the card content
        val clipPath = android.graphics.Path()
        clipPath.addRoundRect(cardRealRect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
        
        // Save canvas state and apply clip
        canvas.save()
        canvas.clipPath(clipPath)
        
        // Draw the actual card bitmap
        canvas.drawBitmap(
            bitmap,
            x + shadowSize,
            y + shadowSize,
            null
        )
        
        // Restore canvas state
        canvas.restore()
    }
    
    // Draw front card at the top with shadow
    drawCardWithShadow(frontBitmap, shadowSize * 2, shadowSize * 2)
    
    // Draw back card below with shadow
    drawCardWithShadow(backBitmap, shadowSize * 2, (cardHeight + spacingHeight).toFloat())
    
    return combinedBitmap
}
