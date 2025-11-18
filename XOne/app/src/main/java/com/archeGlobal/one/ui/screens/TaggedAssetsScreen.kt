package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TaggedAssetsController
import com.archeGlobal.one.model.AssetV2Request
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaggedAssetsScreen(
    onBackPressed: () -> Unit,
    navController: NavController
) {
    val controller: TaggedAssetsController = viewModel()
    val locationModel = controller.model
    var searchQuery by remember { mutableStateOf("") }
    var employeeResults by remember { mutableStateOf<List<AssetInLocation>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var showAddAssetSheet by remember { mutableStateOf(false) }

    // Debounce + Real-time Search (starts after 3 chars)
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 1) {
            isSearching = true
            searchError = null

            withContext(Dispatchers.IO) {
                try {
                    val request = AssetV2Request(employeeCode = searchQuery.trim())
                    val response = RetrofitClient.apiService.getAssetsV2(request)

                    withContext(Dispatchers.Main) {
                        if (response.success && response.data.isNotEmpty()) {
                            employeeResults = response.data.map { employee ->
                                AssetInLocation(
                                    name = employee.username,
                                    empId = employee.employeeCode,
                                    location = employee.location,
                                    assetCount = employee.assets.size
                                )
                            }
                        } else {
                            employeeResults = emptyList()
                            searchError = if (response.data.isEmpty()) "No employee found" else response.message
                        }
                        isSearching = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        searchError = "Network error. Try again."
                        employeeResults = emptyList()
                        isSearching = false
                    }
                }
            }
        } else {
            employeeResults = emptyList()
            searchError = null
            isSearching = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Tagged Asset",
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = { Spacer(modifier = Modifier.width(48.dp)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Employee Id...", color = Color.Gray, fontFamily = GraphikFontFamily) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF6F4EE),
                        unfocusedContainerColor = Color(0xFFF6F4EE),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

// Main Content Logic
                when {
                    locationModel.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            UniversalLoader(isLoading = true)
                        }
                    }

                    locationModel.error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = locationModel.error ?: "Error", color = Color.Red, fontSize = 18.sp)
                        }
                    }

                    searchQuery.length >= 1 -> {
                        // Show Employee Search Results
                        if (searchError != null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = searchError!!, color = Color.Red, fontSize = 16.sp)
                            }
                        } else if (employeeResults.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No employee found", color = Color.Gray, fontSize = 16.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(employeeResults) { asset ->
                                    TaggedAssetInLocationCard(
                                        asset = asset,
                                        navController = navController
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Default: Show Location Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(locationModel.items) { item ->
                                TaggedAssetCard(
                                    item = item,
                                    onClick = { navController.navigate("assets_in_location/${item.location}") }
                                )
                            }
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { showAddAssetSheet = true },
                containerColor = Color(0xFFDD3825),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = PrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tag to new user",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp
                )
            }

            // BottomSheet
            if (showAddAssetSheet) {
                AddTaggedAssetBottomSheet(
                    tagName = "",
                    employeeName = "",
                    onDismiss = { showAddAssetSheet = false }
                )
            }
        }
    }
}

@Composable
fun TaggedAssetCard(
    item: TaggedAssetItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(Color(0xFFDD3825)),
            width = 0.5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Fixed Icon for all
            Image(
                painter = painterResource(id = R.drawable.asset_location),
                contentDescription = item.location,
                modifier = Modifier.size(48.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFDD3825))
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Number of Assets
            Text(
                text = item.assetCount.toString(),
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                color = Color(0xFFDD3825)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Location
            Text(
                text = item.location,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Updated Data Class (removed tag)
data class TaggedAssetItem(
    val location: String,
    val assetCount: Int
)

@Composable
fun TaggedAssetInLocationCard(
    asset: AssetInLocation,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("tagged_assets_employee/${asset.empId}/${asset.name}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Row 1: Icon + Name + Dropdown (right-facing)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Asset",
                    modifier = Modifier.size(34.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFDD3825))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = asset.name,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.dropdown),
                    contentDescription = "Expand",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(270f) // Already right-facing
                )
            }

            // Row 2: Keys — Emp ID (left) | Location (right, but text left-aligned)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(44.dp)) // Align with icon + gap
                Text(
                    text = "Emp ID",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(105.dp))
                Text(
                    text = "Location",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(24.dp)) // Balance dropdown
            }

            // Row 3: Values — Same alignment as keys
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(44.dp))
                Text(
                    text = asset.empId,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(75.dp))
                Text(
                    text = asset.location,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(24.dp))
            }

            // Row 4: Tagged Assets
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 44.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.item_name),
                    contentDescription = "Tag",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFDD3825)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tagged Assets: ",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = asset.assetCount.toString(),
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFFDD3825)
                )
            }
        }
    }
}

data class AssetInLocation(
    val name: String,
    val empId: String,
    val location: String,
    val assetCount: Int
)