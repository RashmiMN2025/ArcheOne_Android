package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.controller.AssetInventoryDetailController
import com.archeGlobal.one.ui.components.UniversalLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetInventoryDetailScreen(
    assetName: String,
    onBackPressed: () -> Unit,
    controller: AssetInventoryDetailController,
    onAddClick: () -> Unit,
    onItemClick: (AssetInventoryDetailItem) -> Unit,
) {
    val model = controller.model
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf<AssetInventoryDetailItem?>(null) }

    val locationState by controller.locationState


    val filteredActive = model.activeItems.filter {
        it.model.contains(searchQuery, ignoreCase = true) ||
                it.serialNo.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true) ||
                it.configuration.contains(searchQuery, ignoreCase = true)
    }

    val filteredDecommissioned = model.decommissionedItems.filter {
        it.model.contains(searchQuery, ignoreCase = true) ||
                it.serialNo.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true) ||
                it.configuration.contains(searchQuery, ignoreCase = true)
    }

    val context = LocalContext.current

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
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = assetName,
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
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

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                color = Color(0x1ADD3825),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { /* Handle info click */ },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Tap an asset for Action",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = GraphikFontFamily,
                        textAlign = TextAlign.Left,
                        color = Color.Black,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search by asset name...",
                                color = Color.Gray,
                                fontFamily = GraphikFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(18.dp),
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (model.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            UniversalLoader(isLoading = true)
                        }
                    } else if (model.error != null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = model.error,
                                color = Color.Red,
                                fontSize = 18.sp
                            )
                        }
                    } else if (filteredActive.isEmpty() && filteredDecommissioned.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No assets found",
                                color = Color.Gray,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Scrollable asset list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 50.dp) // FAB space
                        ) {
                            if (filteredActive.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Active Assets",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                    )
                                }
                                items(filteredActive) { item ->
                                    AssetInventoryDetailCard(
                                        item = item,
                                        onClick = { showActionDialog = item }
                                    )
                                }
                            }

                            if (filteredDecommissioned.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Decommissioned Assets",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .padding(
                                                top = if (filteredActive.isNotEmpty()) 20.dp else 10.dp,
                                                bottom = 8.dp
                                            )
                                    )
                                }
                                items(filteredDecommissioned) { item ->
                                    AssetInventoryDetailCard(
                                        item = item,
                                        onClick = {  }
                                    )
                                }
                            }
                        }
                    }
                }

            }

            // Floating Add Button
            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 38.dp, end = 18.dp)
                    .width(180.dp)
                    .height(48.dp),
                containerColor = PrimaryRed,
                contentColor = Color(0xFFF6F4EE),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add $assetName",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }

        showActionDialog?.let { item ->
            AssetActionDialog(
                item = item,
                onDismiss = { showActionDialog = null },
                onEdit = { /* TODO */ },
                onDecommission = {
                    controller.updateCommissionStatus(item.serialNo, false, context)
                    showActionDialog = null
                },
                onDelete = {
                    controller.deleteAsset(item.serialNo, context)
                    showActionDialog = null
                }
            )
        }

        if (showAddSheet) {
            AddAssetBottomSheet(
                assetName = assetName,
                onDismiss = { showAddSheet = false },
                onAddAsset = { formData ->
                    controller.createAsset(formData, context)
                },
                locations = locationState.locations,
                isLoadingLocations = locationState.isLoading,
                locationError = locationState.error
            )
        }

    }
}

@Composable
fun AssetInventoryDetailCard(
    item: AssetInventoryDetailItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
//            .fillMaxWidth(0.9f)
            .clickable { onClick() }
            .alpha(if (item.isDecommissioned) 0.6f else 1f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Model (Bold, Black)
            DetailRow(
                icon = painterResource(R.drawable.item_name), // Add your icon
                label = "Model",
                value = item.model,
                isBold = true
            )

            // Other fields (Gray, Normal)
            DetailRow(
                icon = painterResource(R.drawable.item_no),
                label = "Serial No",
                value = item.serialNo
            )

            DetailRow(
                icon = painterResource(R.drawable.meetroomlocation),
                label = "Location",
                value = item.location
            )

            DetailRow(
                icon = painterResource(R.drawable.configuration),
                label = "Configuration",
                value = item.configuration
            )

            DetailRow(
                icon = painterResource(R.drawable.supplied_date),
                label = "Purchase Date",
                value = item.purchaseDate
            )

            DetailRow(
                icon = painterResource(R.drawable.updated_by),
                label = "Updated By",
                value = "N/A"
            )
        }
    }
}

// Dialog
@Composable
fun AssetActionDialog(
    item: AssetInventoryDetailItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDecommission: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(420.dp)
                .wrapContentHeight()
                .padding(20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                    ) {
                    Icon(
                        painter = painterResource(R.drawable.about_us),
                        contentDescription = "Asset",
                        tint = PrimaryRed,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Asset Actions",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(24.dp))

                InfoRow(icon = painterResource(R.drawable.item_name),"Model", item.model, true)
                InfoRow(icon = painterResource(R.drawable.item_no),"Serial No", item.serialNo)
                InfoRow(icon = painterResource(R.drawable.meetroomlocation),"Location", item.location)
                InfoRow(icon = painterResource(R.drawable.configuration),"Configuration", item.configuration)
                InfoRow(icon = painterResource(R.drawable.supplied_date),"Purchase Date", item.purchaseDate)
                InfoRow(icon = painterResource(R.drawable.warranty),"Warranty Start", item.warrantyStart)
                InfoRow(icon = painterResource(R.drawable.warranty),"Warranty End", item.warrantyEnd)
                InfoRow(icon = painterResource(R.drawable.warranty),"Warranty End", item.warrantyEnd)

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "Delete",
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = onDecommission,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF949494)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "Decommission",
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: Painter,
    label: String,
    value: String?,
    isBold: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            painter = icon,
            contentDescription = label,
            modifier = Modifier
                .size(16.dp),
            colorFilter = ColorFilter.tint(if (isBold) Color(0xFFDD3825) else Color.Gray)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Row {
            Text(
                text = "$label:",
                fontFamily = GraphikFontFamily,
                fontSize = if (isBold) 16.sp else 13.sp,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isBold) Color.Black else Color.Gray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value ?: "N/A",
                fontFamily = GraphikFontFamily,
                fontSize = if (isBold) 16.sp else 13.sp,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isBold) Color.Black else Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DetailRow(
    icon: Painter,
    label: String,
    value: String?,
    isBold: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = icon,
            contentDescription = label,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(if (isBold) Color(0xFFDD3825) else Color.Gray)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Row {
            Text(
                text = "$label:",
                fontFamily = GraphikFontFamily,
                fontSize = if (isBold) 16.sp else 15.sp,
                fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal,
                color = if (isBold) Color.Black else Color.Gray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value ?: "N/A",
                fontFamily = GraphikFontFamily,
                fontSize = if (isBold) 16.sp else 15.sp,
                fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal,
                color = if (isBold) Color.Black else Color.Gray
            )
        }
    }
}

// Static Data (same as before)
data class AssetInventoryDetailItem(
    val model: String,
    val serialNo: String,
    val location: String,
    val configuration: String,
    val purchaseDate: String,
    val warrantyStart: String? = null,
    val warrantyEnd: String? = null,
    val updatedBy: String? = null,
    val isDecommissioned: Boolean = false
)