package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.archeGlobal.one.controller.LocationsController
import com.archeGlobal.one.model.LocationInfo
import com.archeGlobal.one.model.StateInfo
import com.archeGlobal.one.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    navController: NavHostController,
    controller: LocationsController,
) {
    val context = LocalContext.current
    val locationController = controller ?: remember { LocationsController(context) }
    
    LaunchedEffect(Unit) {
        locationController.resetState()
    }
    
    val state = locationController.getState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1),  // Light Beige/Grey
                        Color(0xFFC8C8CA),  // Light Grey
                        Color(0xFF474749)   // Dark Grey
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                when {
                                    state.showingStateList -> "${state.selectedLocation?.name} Locations"
                                    state.showingDetails -> {
                                        when {
                                            state.selectedState?.name == "Tamil Nadu" -> state.selectedLocation?.name ?: ""
                                            else -> state.selectedLocation?.name?.split(",")?.firstOrNull() ?: ""
                                        }
                                    }
                                    else -> "Locations"
                                },
                                fontSize = 18.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (!locationController.onBackPressed()) {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryBlue
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
                            LocationCard(
                                location = location,
                                onClick = { locationController.selectLocation(location) },
                                onFloorMapClick = if (location.hasFloorMap && location.mapFileName != null) {
                                    { location.mapFileName?.let { mapFile -> locationController.showFloorMap(mapFile) } }
                                } else null
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

@OptIn(ExperimentalMaterial3Api::class)
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
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = location.address.substringBefore(","),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = location.address.substringAfter(",").trim(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Email:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = location.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${location.email}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send email"))
                    }
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
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.hrNumber}")
                            }
                            context.startActivity(intent)
                        }
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
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.adminNumber}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.width(160.dp)
                ) {
                    Text(
                        text = "View Location",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
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
                            fontWeight = FontWeight.Bold,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "India",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display ALL states from the API response without filtering
                    items(states) { state ->
                        Button(
                            onClick = { onStateClick(state) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRed
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(
                                text = state.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = location.address,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                
                // Add location icon in a circular red background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFDD3825),
                            shape = CircleShape
                        )
                        .clickable {
                            // Open Google Maps with the redirection link if available
                            val uri = if (location.redirection?.isNotEmpty() == true) {
                                Uri.parse(location.redirection)
                            } else {
                                // Fallback to searching for the address
                                Uri.parse("geo:0,0?q=${Uri.encode(location.address)}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Navigate to location",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Only show Contact Information section if any contact info is available
            val hasContactInfo = location.email.isNotEmpty() || 
                                (location.hrName != null && location.hrNumber != null) || 
                                (location.adminName != null && location.adminNumber != null)
                                
            if (hasContactInfo) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Contact Information",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                // Email
                if (location.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${location.email}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send email"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = location.email,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
                
                // HR Contact - only show if both name and number are available
                if (location.hrName != null && location.hrNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.hrNumber}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "HR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "${location.hrName} : ${location.hrNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
                
                // Admin Contact - only show if both name and number are available
                if (location.adminName != null && location.adminNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.adminNumber}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "IT Admin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "${location.adminName} : ${location.adminNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }
            
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Floor Map",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
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
                onClick = { if (!isTamilNadu) onLocationClick(location) },
                onFloorMapClick = if (location.hasFloorMap && location.mapFileName != null) {
                    { location.mapFileName?.let { mapFile -> onShowFloorMap(mapFile) } }
                } else null
            )
        }
    }
} 