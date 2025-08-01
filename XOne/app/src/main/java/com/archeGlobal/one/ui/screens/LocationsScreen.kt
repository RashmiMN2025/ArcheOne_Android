package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.LocationsController
import com.archeGlobal.one.model.LocationInfo
import com.archeGlobal.one.model.StateInfo
import com.archeGlobal.one.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    navController: NavHostController,
    controller: LocationsController,
    isEmergencyContact: Boolean = false,
    showHeader: Boolean,
    onBackToHome: () -> Unit // <-- Add this
) {
    val context = LocalContext.current
    val locationController = controller ?: remember { LocationsController(context) }

    // Check both the passed parameter and the saved state handle
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val savedEmergencyContact = savedStateHandle?.get<Boolean>("isEmergencyContact") ?: false
    val isEmergencyContactActual = savedEmergencyContact || isEmergencyContact

    // Add BackHandler to handle back swipe gestures
    BackHandler {
        if (locationController.isInEmergencyContactMode()) {
            val stayInCurrentScreen = locationController.onEmergencyBackPressed()
            if (!stayInCurrentScreen) {
                navController.navigate("sos?showHeader=$showHeader") {
                    popUpTo("sos") { inclusive = true }
                }
            }
        } else {
            // If at top-level, go to home
            val state = locationController.getState()
            val atTopLevel = !state.showingStateList && !state.showingDetails
            if (atTopLevel) {
                onBackToHome() // <-- Call the callback
            } else {
                if (!locationController.onBackPressed()) {
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d(
            "LocationsScreen",
            "Screen initialized with isEmergencyContact=$isEmergencyContactActual"
        )
        locationController.resetState()

        // Set the value in the controller
        locationController.setEmergencyContactMode(isEmergencyContactActual)

        // Set the value in the saved state handle
        navController.currentBackStackEntry?.savedStateHandle?.set(
            "isEmergencyContact",
            isEmergencyContactActual
        )

        // Only auto-navigate to India location if in emergency contact mode
        if (isEmergencyContactActual) {
            Log.d("LocationsScreen", "Emergency contact mode enabled, looking for India location")
            val indiaLocation = locationController.getLocations().find { it.name == "India" }
            if (indiaLocation != null) {
                Log.d("LocationsScreen", "Found India location, selecting it")
                locationController.selectLocation(indiaLocation)
            } else {
                Log.e("LocationsScreen", "India location not found in locations list")
            }
        }
        // Otherwise, show the normal locations list without auto-navigation
    }

    val state = locationController.getState()

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
                            Color(0xFFE0DCD1), // Light Beige/Grey
                            Color(0xFFC8C8CA), // Light Grey
                            Color(0xFF474749) // Dark Grey
                        )
                    )
                )
        ) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 80.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = when {
                                        locationController.isInEmergencyContactMode() -> "Emergency Contacts"
                                        state.showingStateList -> "Regional Offices"
                                        state.showingDetails -> "Regional Offices"
                                        else -> "Locations"
                                    },
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    if (locationController.isInEmergencyContactMode()) {
                                        val stayInCurrentScreen =
                                            locationController.onEmergencyBackPressed()
                                        if (!stayInCurrentScreen) {
                                            navController.navigate("sos?showHeader=$showHeader") {
                                                popUpTo("sos") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        val state = locationController.getState()
                                        val atTopLevel =
                                            !state.showingStateList && !state.showingDetails
                                        if (atTopLevel) {
                                            onBackToHome()
                                        } else {
                                            if (!locationController.onBackPressed()) {
                                                navController.popBackStack()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { padding ->
                when {
                    state.showingStateList && state.selectedState?.name == "Tamil Nadu" -> {
                        // Show Tamil Nadu locations list
                        LazyColumn(
                            modifier = Modifier.padding(padding),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.selectedState.locations) { location ->
                                val context = LocalContext.current
                                LocationCard(
                                    location = location,
                                    onClick = { locationController.selectLocation(location) },
                                    onFloorMapClick = if (location.hasFloorMap && location.mapFileName != null) {
                                        {
                                            location.mapFileName?.let { mapFile ->
                                                locationController.showFloorMap(
                                                    mapFile
                                                )
                                            }
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }

                    state.showingStateList -> {
                        StateList(
                            states = state.selectedLocation?.states ?: emptyList(),
                            onStateClick = { state ->
                                locationController.selectStateLocation(state)
                            },
                            modifier = Modifier.padding(padding),
                            controller = locationController
                        )
                    }

                    state.showingDetails -> {
                        val selectedLocation = state.selectedLocation
                        if (selectedLocation != null) {
                            LocationDetails(
                                location = selectedLocation,
                                onShowFloorMap = {
                                    selectedLocation.mapFileName?.let { mapFile ->
                                        locationController.showFloorMap(mapFile)
                                    }
                                },
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }

                    else -> {
                        LocationList(
                            locations = locationController.getLocations(),
                            onLocationClick = locationController::selectLocation,
                            onShowFloorMap = { mapFile ->
                                locationController.showFloorMap(mapFile)
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCard(
    location: LocationInfo,
    onClick: () -> Unit,
    onFloorMapClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = location.name,
                fontSize = 18.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arche Global Private Limited",
                fontSize = 17.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = location.address,
                fontSize = 15.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Email:",
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
                Text(
                    text = location.email,
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${location.email}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send email"))
                        }
                    )
                )
            }

            if (location.hrName != null && location.hrNumber != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = PrimaryRed
                    )
                    Text(
                        text = "${location.hrName}: ${location.hrNumber}",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${location.hrNumber}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    )
                }
            }

            if (location.adminName != null && location.adminNumber != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = PrimaryRed
                    )
                    Text(
                        text = "${location.adminName}: ${location.adminNumber}",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${location.adminNumber}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        // Add debugging to see what's happening
                        Log.d("LocationsScreen", "View Location clicked for: ${location.name}, hasStates=${location.states != null}")

                        // For all locations except India, open in Google Maps
                        if (!location.name.equals("India", ignoreCase = true)) {
                            // Open Google Maps with the redirection link if available
                            val uri = if (location.redirection?.isNotEmpty() == true) {
                                Uri.parse(location.redirection)
                            } else {
                                // Fallback to searching for the address
                                Uri.parse("geo:0,0?q=${Uri.encode(location.address)}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } else {
                            // For India, use the provided onClick which shows internal details
                            Log.d("LocationsScreen", "Calling onClick() for India location")
                            onClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.width(180.dp)
                ) {
                    Text(
                        text = "View Location",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (location.hasFloorMap && location.mapFileName != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onFloorMapClick?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Floor Map",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StateList(
    states: List<StateInfo>,
    onStateClick: (StateInfo) -> Unit,
    modifier: Modifier = Modifier,
    controller: LocationsController
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display ALL states from the API response without filtering
                items(states) { state ->
                    val location = state.locations.firstOrNull() ?: return@items

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStateClick(state) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Office building icon in red circle
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.building),
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(45.dp)
                                )
                            }

                            // Office details
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = state.name,
                                    fontSize = 18.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = location.address,
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }

                            // Right arrow
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationDetails(
    location: LocationInfo,
    onShowFloorMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = location.name,
                fontSize = 19.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Address",
                fontSize = 16.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        .padding(4.dp)
                ) {
                    Text(
                        text = location.address,
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Add location icon in a circular red background
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                // Don't navigate to maps for Indian locations - we're already displaying details
                                if (!location.name.contains("India", ignoreCase = true)) {
                                    // Open Google Maps with the redirection link if available
                                    val uri = if (location.redirection?.isNotEmpty() == true) {
                                        Uri.parse(location.redirection)
                                    } else {
                                        // Fallback to searching for the address
                                        Uri.parse("geo:0,0?q=${Uri.encode(location.address)}")
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location), // Replace with your SVG resource
                        contentDescription = "Navigate to location",
                        tint = PrimaryRed,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.LightGray
            )

            // Only show Contact Information section if any contact info is available
            val hasContactInfo = location.email.isNotEmpty() || (location.hrName != null && location.hrNumber != null) || (location.adminName != null && location.adminNumber != null)

            if (hasContactInfo) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Contact Information",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Email
                if (location.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${location.email}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send email"))
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = location.email,
                            fontSize = 15.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // ADMIN Contact - only show if both name and number are available
                if (location.adminName != null && location.adminNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${location.adminNumber}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Admin Contact",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Row {
                                Text(
                                    text = location.adminName,
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "-",
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = location.adminNumber,
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // HR Contact - only show if both name and number are available
                if (location.hrName != null && location.hrNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${location.hrNumber}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "HR Contact",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Row {
                                Text(
                                    text = location.hrName,
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "-",
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = location.hrNumber,
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Floor Map Button
            if (location.hasFloorMap && location.mapFileName != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onShowFloorMap,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map), // Replace with your SVG resource
                            contentDescription = "Floor Map",
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Floor Map",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationList(
    locations: List<LocationInfo>,
    onLocationClick: (LocationInfo) -> Unit,
    onShowFloorMap: (String) -> Unit,
    modifier: Modifier = Modifier,
    isTamilNadu: Boolean = false
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(locations) { location ->
            LocationCard(
                location = location,
                onClick = { onLocationClick(location) },
                onFloorMapClick = if (location.hasFloorMap && location.mapFileName != null) {
                    { location.mapFileName?.let { mapFile -> onShowFloorMap(mapFile) } }
                } else {
                    null
                }
            )
        }
    }
}
