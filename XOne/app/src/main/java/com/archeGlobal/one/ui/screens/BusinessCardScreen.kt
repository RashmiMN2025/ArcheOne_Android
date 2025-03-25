package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalView
import android.view.View
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import android.util.TypedValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun BusinessCardScreen(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    var isPortraitView by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,    // Color(0xFFE0DCD1)
                        WelcomeBackgroundMiddle, // Color(0xFFC8C8CA)
                        WelcomeBackgroundBottom  // Color(0xFF474749)
                    )
                )
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    when {
                        dragAmount < -50 && isPortraitView -> isPortraitView = false // Swipe left
                        dragAmount > 50 && !isPortraitView -> isPortraitView = true  // Swipe right
                    }
                }
            }
    ) {
        if (isPortraitView) {
            PortraitBusinessCard(businessCard, controller)
        } else {
            LandscapeBusinessCard(businessCard, controller)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitBusinessCard(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val cardBounds = remember { mutableStateOf<android.graphics.Rect?>(null) }
    var newLocation by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        "My Business Card",
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { controller.onBackPressed() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                cardBounds.value?.let { bounds ->
                                    val bitmap = captureCardArea(view, bounds)
                                    controller.onShareCard(bitmap)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Share",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }

        item {
            // Card Content
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
                    .height(500.dp)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        cardBounds.value = android.graphics.Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        )
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 70.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = businessCard.companyLogo),
                        contentDescription = "Company Logo",
                        modifier = Modifier.height(30.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = businessCard.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.designation,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Arche Global Pvt. Ltd.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.email,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.phone,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = Color.Black
                    )

                    Text(
                        text = controller.businessCard.location,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.website,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = Color.Black
                    )
                }
            }
        }

        item {
            Text(
                text = "Swipe right to change View -->",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            // Bottom Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        scope.launch {
                            cardBounds.value?.let { bounds ->
                                val bitmap = captureCardArea(view, bounds)
                                controller.onDownloadCard(bitmap)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "Download Business Card",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }

                Button(
                    onClick = { controller.onEditLocation() }, // Updated to just open dialog
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "Edit Location",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Show Edit Location Dialog
    if (controller.showEditLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { controller.showEditLocationDialog.value = false },
            containerColor = Color(0xFFF5F5F5), // Light gray background for the dialog
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
                    placeholder = { Text("Enter new location") }, // ✅ Placeholder disappears on input
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedContainerColor = Color.White, // ✅ Background white
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }, // ✅ Tracks focus
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions(
                        onDone = { isFocused = false }
                    )
                )

            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly // Center-align buttons
                ) {
                    Button(
                        onClick = { controller.showEditLocationDialog.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.weight(0.6f) // Equal width
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = { controller.onLocationUpdated(newLocation) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)), // Red color
                        modifier = Modifier.weight(0.6f) // Equal width
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeBusinessCard(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val cardBounds = remember { mutableStateOf<android.graphics.Rect?>(null) }
    var newLocation by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        "My Business Card",
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { controller.onBackPressed() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                cardBounds.value?.let { bounds ->
                                    val bitmap = captureCardArea(view, bounds)
                                    controller.onShareCard(bitmap)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Share",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }

        item {
            // Main Card
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.95f)
                    .height(300.dp)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        cardBounds.value = android.graphics.Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        )
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left section - Logo
                    Image(
                        painter = painterResource(id = businessCard.companyLogo),
                        contentDescription = "Company Logo",
                        modifier = Modifier
                            .weight(0.3f)
                            .height(35.dp)
                    )

                    // Right section with text content
                    Column(
                        modifier = Modifier
                            .weight(0.7f)
                            .padding(start = 24.dp, top = 2.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = businessCard.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = businessCard.designation,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = businessCard.email,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = Color.Black
                        )

                        Text(
                            text = businessCard.phone,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = Color.Black
                        )

                        Text(
                            text = controller.businessCard.location,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Spacer(modifier = Modifier.weight(2f))

                        Text(
                            text = businessCard.website,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "<-- Swipe left to change View",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            cardBounds.value?.let { bounds ->
                                val bitmap = captureCardArea(view, bounds)
                                controller.onDownloadCard(bitmap)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "Download Business Card",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }

                Button(
                    onClick = { controller.onEditLocation() }, // Updated to just open dialog
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "Edit Location",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Show Edit Location Dialog
    if (controller.showEditLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { controller.showEditLocationDialog.value = false },
            containerColor = Color(0xFFF5F5F5), // Light gray background for the dialog
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
                    placeholder = { Text("Enter new location") }, // ✅ Placeholder disappears on input
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedContainerColor = Color.White, // ✅ Background white
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }, // ✅ Tracks focus
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions(
                        onDone = { isFocused = false }
                    )
                )

            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly // Center-align buttons
                ) {
                    Button(
                        onClick = { controller.showEditLocationDialog.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.weight(0.6f) // Equal width
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = { controller.onLocationUpdated(newLocation) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)), // Red color
                        modifier = Modifier.weight(0.6f) // Equal width
                    ) {
                        Text("Save", color = Color.White)
                    }
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