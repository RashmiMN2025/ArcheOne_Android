package com.archeGlobal.one.ui.screens

import android.graphics.*
import android.util.TypedValue
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.BusinessCardController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.model.BusinessCardModel
import com.archeGlobal.one.model.LocationInfo
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.TextPrimary
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import kotlinx.coroutines.launch

// Function to crop the white border from the QR code bitmap
private fun cropQRCodeBitmap(bitmap: Bitmap, borderFraction: Float = 0.075f): Bitmap {
    val borderSize = (bitmap.width * borderFraction).toInt()
    val clippedSize = bitmap.width - (2 * borderSize)
    try {
        // Create a new bitmap with ARGB_8888 for transparency
        val croppedBitmap = Bitmap.createBitmap(
            clippedSize,
            clippedSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(croppedBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        // Draw the cropped portion
        canvas.drawBitmap(
            bitmap,
            Rect(borderSize, borderSize, bitmap.width - borderSize, bitmap.height - borderSize),
            Rect(0, 0, clippedSize, clippedSize),
            paint
        )
        return croppedBitmap
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        return bitmap // Return original if cropping fails
    }
}

// Composable to display a QR code bitmap with clipped borders
@Composable
private fun ComposeQRCodeImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .background(Color.Transparent) // Ensure transparent background
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = cropQRCodeBitmap(bitmap).asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTopAppBar(
    onBackPressed: () -> Unit,
    onShareClick: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
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
        },
        navigationIcon = {
            IconButton(
                onClick = onBackPressed
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 5.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.share),
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
        val matchingOffice =
            offices?.find { office -> office.country.equals(userLocation, ignoreCase = true) }

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
            val indiaOffice =
                offices?.find { office -> office.country.equals("India", ignoreCase = true) }
            val regionalOffice = indiaOffice?.regionaloffice?.find { office ->
                office.region.contains(
                    userLocation,
                    ignoreCase = true
                )
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
                // Custom location - use Bangalore as fallback address for the back side
                val bangaloreOffice = indiaOffice?.regionaloffice?.find { office ->
                    office.region.contains("Bangalore", ignoreCase = true)
                }

                LocationInfo(
                    name = userLocation, // Keep custom location name for front side
                    companyName = bangaloreOffice?.companyName ?: "Arche Global Pvt Ltd",
                    address = bangaloreOffice?.address ?: "Bangalore", // Use Bangalore address for back side
                    email = bangaloreOffice?.email ?: indiaOffice?.email ?: "",
                    hasMultipleLocations = false
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
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
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    CustomTopAppBar(
                        onBackPressed = { controller.onBackPressed() },
                        onShareClick = {
                            scope.launch {
                                cardBounds.value?.let { bounds ->
                                    val combinedBitmap = captureBothSides(
                                        view,
                                        bounds,
                                        showFrontSide
                                    ) { newShowFrontSide ->
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
                            .width(280.dp)
                            .height(450.dp)
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
                                        dragAmount < -50 && showFrontSide ->
                                            showFrontSide =
                                                false // Swipe left
                                        dragAmount > 50 && !showFrontSide ->
                                            showFrontSide =
                                                true // Swipe right
                                    }
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
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
                                // Logo
                                Image(
                                    painter = painterResource(id = R.drawable.arche_black2),
                                    contentDescription = "Arche Logo",
                                    modifier = Modifier
                                        .size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(90.dp))

                                // Name and Designation section
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Name
                                    Text(
                                        text = businessCard.name,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp,
                                        color = Color.Black
                                    )

                                    // Designation
                                    Text(
                                        text = businessCard.designation,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = Color.Gray
                                    )
                                }

                                // Add spacing between designation and contact info
                                Spacer(modifier = Modifier.height(25.dp))

                                // Contact information section
                                Column {
                                    // Email
                                    Text(
                                        text = businessCard.email,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )

                                    // Reduced spacing between email and phone
                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Phone
                                    Text(
                                        text = businessCard.phone,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )

                                    // Normal spacing between phone and location
                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Location
                                    Text(
                                        text = controller.businessCard.location,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                }

                                // Push content to bottom of card
                                Spacer(modifier = Modifier.weight(1f))

                                // Bottom row with arche text and QR code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF6F4EE)),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // Arche text at bottom left
                                    Text(
                                        text = "arche",
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.offset(y = (-10).dp) // Move up slightly while keeping in the row
                                    )

                                    // QR Code at bottom right
                                    businessCard.qrCode?.let { qrBitmap ->
                                        ComposeQRCodeImage(
                                            bitmap = qrBitmap,
                                            contentDescription = "QR Code",
                                            modifier = Modifier
                                                .size(75.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Back side
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top quote
                                Text(
                                    text = "This could be the start of something great.",
                                    fontSize = 12.5.sp,
                                    fontFamily = FontFamily(Font(R.font.canela_regular)),
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .padding(top = 20.dp)
                                )

                                // Logo in the middle
                                Image(
                                    painter = painterResource(id = R.drawable.arche_black2),
                                    contentDescription = "Arche Logo",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .aspectRatio(9f / 8f)
                                )

                                // Bottom section with company name, address, and website
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Arche Global Private Limited",
                                        fontSize = 15.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    // Use the location data already resolved in the front side
                                    val officeAddress = location.address

                                    Text(
                                        text = officeAddress,
                                        fontSize = 9.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 11.sp,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 20.dp)
                                    )

                                    Text(
                                        text = "www.arche.global",
                                        fontSize = 15.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (showFrontSide) "Swipe to flip" else "Swipe to flip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp
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
                                        val combinedBitmap = captureBothSides(
                                            view,
                                            bounds,
                                            showFrontSide
                                        ) { newShowFrontSide ->
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
                                fontSize = 12.5.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { controller.onEditCard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF6F4EE),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Text(
                                "Edit Card",
                                fontSize = 12.5.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Show Edit Card Dialog
        if (controller.showEditCardDialog.value) {
            var newPhone by remember(businessCard.phone) { mutableStateOf(businessCard.phone) }
            // Split phone number into country code and number
            var countryCode by remember(businessCard.phone) {
                mutableStateOf(
                    businessCard.phone.split(" - ").firstOrNull()?.take(4) ?: "+91"
                )
            }
            var phoneNumber by remember(businessCard.phone) {
                mutableStateOf(
                    businessCard.phone.split(" - ").getOrNull(1)?.take(10) ?: ""
                )
            }

            // Define keywords for designation check
            val keywords = listOf(
                "sales",
                "lead",
                "practice",
                "head",
                "ceo",
                "managing",
                "director",
                "management",
                "manager",
                "senior"
            )

            // Check if user has permission to edit phone number based on designation
            val canEditPhone = businessCard.designation.lowercase().split(" ").any { word ->
                keywords.any { keyword -> word.contains(keyword) }
            }

            AlertDialog(
                onDismissRequest = { controller.showEditCardDialog.value = false },
                containerColor = Color(0xFFF6F4EE),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit Card",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            color = Color.Black
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Location field with dropdown
                        val offices = OtpVerificationController.getOfficesData()
                        var expanded by remember { mutableStateOf(false) }

                        // Get all available locations
                        val locations = mutableListOf<String>()
                        offices?.forEach { office ->
                            locations.add(office.country)
                            office.regionaloffice?.forEach { regional ->
                                locations.add(regional.region)
                            }
                        }

                        // Add "Other" option
                        locations.add("Other")

                        // Check if current location is a custom location (not in predefined list)
                        val isCurrentLocationCustom = !locations.contains(businessCard.location)

                        var selectedLocation by remember {
                            mutableStateOf(
                                if (isCurrentLocationCustom) "Other" else businessCard.location
                            )
                        }
                        var isOtherSelected by remember { mutableStateOf(isCurrentLocationCustom) }

                        // Initialize custom location with current location if it's custom
                        var customLocation by remember {
                            mutableStateOf(
                                if (isCurrentLocationCustom) businessCard.location else ""
                            )
                        }

                        Box {
                            OutlinedTextField(
                                value = selectedLocation,
                                onValueChange = {
                                    selectedLocation = it
                                    if (!isOtherSelected) {
                                        newLocation = it
                                    }
                                },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = if (expanded) {
                                                androidx.compose.material.icons.Icons.Default.KeyboardArrowUp
                                            } else {
                                                androidx.compose.material.icons.Icons.Default.KeyboardArrowDown
                                            },
                                            contentDescription = if (expanded) "Collapse" else "Expand"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedIndicatorColor = Color.Black,
                                    unfocusedIndicatorColor = Color.Black
                                ),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .width(260.dp)
                                    .heightIn(max = 360.dp),
                                // Override the container color to make it transparent black
                                properties = PopupProperties(focusable = true),
                                shape = RoundedCornerShape(12.dp),
                                containerColor = Color(0xFFF6F4EE) // 80% transparent black
                            ) {
                                locations.forEachIndexed { index, location ->
                                    Column {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = location,
                                                    color = Color.Black,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            },
                                            onClick = {
                                                selectedLocation = location
                                                if (location == "Other") {
                                                    isOtherSelected = true
                                                } else {
                                                    isOtherSelected = false
                                                    newLocation = location
                                                }
                                                expanded = false
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = Color.White,
                                                leadingIconColor = Color.White,
                                                trailingIconColor = Color.White,
                                                disabledTextColor = Color.White.copy(alpha = 0.5f),
                                                disabledLeadingIconColor = Color.White.copy(alpha = 0.5f),
                                                disabledTrailingIconColor = Color.White.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier.height(30.dp)
                                        )
                                        if (index < locations.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                thickness = 1.dp,
                                                color = Color.Gray.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Show custom location field when "Other" is selected
                        if (isOtherSelected) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customLocation,
                                onValueChange = {
                                    // Allow only letters, numbers, and spaces
                                    val filtered = it.filter { it.isLetterOrDigit() || it.isWhitespace() }
                                    customLocation = filtered
                                    newLocation = filtered
                                },
                                placeholder = {
                                    Text(
                                        "Enter custom location",
                                        color = Color.Gray,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
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
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Only show phone number field if user has permission
                        if (canEditPhone) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Phone number fields (Country Code + Number)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Country Code field
                                OutlinedTextField(
                                    value = countryCode,
                                    onValueChange = { newValue ->
                                        // Allow only digits and optional leading '+'
                                        val filtered = newValue.filter { it.isDigit() || it == '+' }
                                        // Limit to 4 characters, ensure '+' is only at start
                                        if (filtered.length <= 4 && (filtered.startsWith("+") || filtered.all { it.isDigit() })) {
                                            countryCode = filtered
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Code",
                                            color = Color.Gray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .height(52.dp),
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
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Text(
                                    text = "-",
                                    color = Color.Black,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                )

                                // Phone Number field
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { newValue ->
                                        // Allow only digits, limit to 10
                                        val filtered = newValue.filter { it.isDigit() }
                                        if (filtered.length <= 10) {
                                            phoneNumber = filtered
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Enter Phone Number",
                                            color = Color.Gray,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .height(52.dp),
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
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { controller.showEditCardDialog.value = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9E9E9E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = GraphikFontFamily
                            )
                        }

                        Button(
                            onClick = {
                                if (canEditPhone) {
                                    if (phoneNumber.length == 10 && countryCode.isNotEmpty()) {
                                        controller.onCardUpdated(newLocation, countryCode, phoneNumber)
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (countryCode.isEmpty()) "Country code cannot be empty!" else "Phone number must be 10 digits!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    controller.onCardUpdated(newLocation, businessCard.phone.split(" - ").firstOrNull() ?: "+91", businessCard.phone.split(" - ").getOrNull(1) ?: "")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "Save",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = GraphikFontFamily
                            )
                        }
                    }
                },
                dismissButton = null
            )
        }
    }
}

private fun captureCardArea(view: View, cardBounds: android.graphics.Rect): Bitmap {
    view.isDrawingCacheEnabled = true
    val fullBitmap = Bitmap.createBitmap(view.drawingCache)
    view.isDrawingCacheEnabled = false

    return try {
        val result = Bitmap.createBitmap(
            cardBounds.width(),
            cardBounds.height(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        val cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            view.resources.displayMetrics
        )
        val path = Path().apply {
            addRoundRect(
                RectF(0f, 0f, cardBounds.width().toFloat(), cardBounds.height().toFloat()),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
        }
        canvas.clipPath(path)
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
