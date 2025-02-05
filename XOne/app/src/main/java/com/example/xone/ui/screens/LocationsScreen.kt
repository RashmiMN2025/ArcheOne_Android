package com.example.xone.ui.screens

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
import com.example.xone.controller.LocationsController
import com.example.xone.model.LocationInfo
import com.example.xone.model.StateInfo
import com.example.xone.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    navController: NavHostController,
    controller: LocationsController? = null
) {
    val context = LocalContext.current
    val locationController = controller ?: remember { LocationsController(context) }
    
    val state = locationController.getState()
    
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
                    LocationList(
                        locations = state.selectedState.locations,
                        onLocationClick = { location ->
                            locationController.selectLocation(location)
                        },
                        onShowFloorMap = { mapFile ->
                            locationController.showFloorMap(mapFile)
                        },
                        modifier = Modifier.padding(padding),
                        isTamilNadu = true
                    )
                }
                state.showingStateList -> {
                    StateList(
                        states = state.selectedLocation?.states ?: emptyList(),
                        onStateClick = { state ->
                            if (state.name == "Tamil Nadu") {
                                // Show Tamil Nadu locations directly
                                locationController.showTamilNaduLocations(state)
                            } else {
                                // For other states, show single location directly
                                locationController.selectStateLocation(state)
                            }
                        },
                        modifier = Modifier.padding(padding)
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
    onFloorMapClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = location.name,
                fontSize = if (location.hasMultipleLocations) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = location.companyName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Address:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    text = location.address.substringBefore(","),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = location.address.substringAfter(",").trim(),
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Email:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    text = location.email,
                    fontSize = 14.sp,
                    color = PrimaryRed,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${location.email}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send email"))
                    }
                )
            }

            if (location.hrName != null) {
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
                        color = PrimaryRed,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.hrNumber}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            if (location.adminName != null) {
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
                        color = PrimaryRed,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.adminNumber}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            if (location.hasMultipleLocations) {
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
                            text = "View Locations",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
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
                        Text("View Floor Map")
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(states) { state ->
            StateCard(
                state = state,
                onClick = { onStateClick(state) }
            )
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    fontSize = if (location.hasMultipleLocations) 16.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = location.companyName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Address:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = location.address.substringBefore(","),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    text = location.address.substringAfter(",").trim(),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Email:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = location.email,
                        fontSize = 14.sp,
                        color = PrimaryRed,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${location.email}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send email"))
                        }
                    )
                }

                if (location.hrName != null) {
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
                            color = PrimaryRed,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${location.hrNumber}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                if (location.adminName != null) {
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
                            color = PrimaryRed,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${location.adminNumber}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
                
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
                                color = Color.White
                            )
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StateCard(
    state: StateInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = state.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${state.locations.size} location${if (state.locations.size != 1) "s" else ""}",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
} 