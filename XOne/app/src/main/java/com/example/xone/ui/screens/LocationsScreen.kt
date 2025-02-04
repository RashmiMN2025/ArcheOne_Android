package com.example.xone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.xone.controller.LocationsController
import com.example.xone.model.Location
import com.example.xone.model.Country
import com.example.xone.model.State
import com.example.xone.model.getCountryFlag
import com.example.xone.ui.theme.*
import android.content.Intent
import android.net.Uri
import com.example.xone.model.WelcomeBackgroundModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    navController: NavHostController,
    name: String,
    department: String,
    designation: String
) {
    val controller = remember { LocationsController() }
    val state = controller.getCurrentState()
    val backgroundModel = remember { WelcomeBackgroundModel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundModel.topColor,
                        backgroundModel.middleColor,
                        backgroundModel.bottomColor
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar - Modified to blend with background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        when {
                            state.selectedLocation != null && state.selectedState != null -> 
                                // If we're in Tamil Nadu location, go back to state
                                controller.selectLocation(null)
                            state.selectedLocation != null -> 
                                // For other states' locations, go back to India page
                                controller.selectState(null)  // This will clear location and go back to India page
                            state.selectedState != null -> 
                                controller.selectState(null)
                            state.selectedCountry != null -> 
                                controller.selectCountry(null)
                            else -> 
                                navController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = when {
                        state.selectedLocation != null -> state.selectedLocation.name
                        state.selectedState != null -> state.selectedState.name
                        state.selectedCountry != null -> state.selectedCountry.name
                        else -> "Locations"
                    },
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(modifier = Modifier.width(48.dp))
            }

            // Content
            when {
                state.selectedLocation != null -> {
                    LocationDetailsContent(
                        location = state.selectedLocation,
                        onBack = { controller.selectLocation(null) }
                    )
                }
                state.selectedState != null -> {
                    LocationsGrid(
                        locations = state.selectedState.locations,
                        onLocationClick = { controller.selectLocation(it) }
                    )
                }
                state.selectedCountry != null -> {
                    if (state.selectedCountry.states != null) {
                        StatesGrid(
                            states = state.selectedCountry.states,
                            onStateClick = { controller.selectState(it) }
                        )
                    } else if (state.selectedCountry.location != null) {
                        LocationDetailsContent(
                            location = state.selectedCountry.location,
                            onBack = { controller.selectCountry(null) }
                        )
                    }
                }
                else -> {
                    CountriesGrid(
                        countries = controller.getCountries(),
                        onCountryClick = { controller.selectCountry(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CountriesGrid(
    countries: List<Country>,
    onCountryClick: (Country) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(countries) { country ->
            CountryCard(
                country = country,
                onClick = { onCountryClick(country) }
            )
        }
    }
}

@Composable
private fun CountryCard(
    country: Country,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFDD3825).copy(alpha = 0.05f),
                            CardBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Country Flag
                Text(
                    text = getCountryFlag(country.name),
                    fontSize = 40.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Country Name
                Text(
                    text = country.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Location Count
                val locationCount = when {
                    country.states != null -> country.states.sumOf { it.locations.size }
                    country.location != null -> 1
                    else -> 0
                }
                
                Text(
                    text = "$locationCount ${if (locationCount == 1) "Location" else "Locations"}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatesGrid(
    states: List<State>,
    onStateClick: (State) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(states) { state ->
            LocationCard(
                title = state.name,
                onClick = { onStateClick(state) }
            )
        }
    }
}

@Composable
private fun LocationsGrid(
    locations: List<Location>,
    onLocationClick: (Location) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(locations) { location ->
            LocationCard(
                title = location.name,
                onClick = { onLocationClick(location) }
            )
        }
    }
}

@Composable
private fun LocationCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFDD3825)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun LocationDetailsContent(
    location: Location,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = location.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    text = location.address
                )

                if (location.email != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        icon = Icons.Default.Email,
                        text = location.email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${location.email}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send email using"))
                        }
                    )
                }

                if (location.hrNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        icon = Icons.Default.Phone,
                        text = "${location.hrName ?: "HR"}: ${location.hrNumber}",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.hrNumber}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                if (location.adminNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        icon = Icons.Default.Phone,
                        text = "${location.adminName ?: "Admin"}: ${location.adminNumber}",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${location.adminNumber}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFDD3825),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) Color(0xFFDD3825) else TextSecondary
        )
    }
} 