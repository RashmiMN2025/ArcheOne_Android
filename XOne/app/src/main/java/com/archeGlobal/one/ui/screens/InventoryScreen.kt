package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.InventoryController
import com.archeGlobal.one.model.InventoryItem
import com.archeGlobal.one.model.InventoryModel
import com.archeGlobal.one.ui.components.AddItemDialog
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    model: InventoryModel,
    controller: InventoryController
) {
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                InventoryHeader(
                    onBackPressed = controller::onBackPressed
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Notification Banner
                    InventoryNotificationBanner()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar
                    InventorySearchBar(
                        searchQuery = model.searchQuery,
                        onSearchQueryChanged = controller::onSearchQueryChanged
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filters
                    InventoryFilters(
                        selectedLocation = model.selectedLocation,
                        selectedType = model.selectedType,
                        locations = model.locations,
                        types = model.types,
                        onLocationSelected = controller::onLocationSelected,
                        onTypeSelected = controller::onTypeSelected
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Items Grid
                    InventoryItemsGrid(
                        items = model.inventoryItems,
                        onItemClick = controller::onItemClick
                    )
                }
            }

            // Floating Add Button
            FloatingActionButton(
                onClick = controller::onAddItemClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 8.dp)
                    .width(140.dp)
                    .height(48.dp),
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Item",
                            tint = PrimaryRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Item",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Loading overlay
            if (model.isLoading) {
                UniversalLoader(isLoading = true)
            }
        }

        // Add Item Dialog
        if (controller.addItemModel.showDialog) {
            AddItemDialog(
                model = controller.addItemModel,
                onDismiss = controller::onAddItemDismiss,
                onLocationSelected = controller::onAddItemLocationSelected,
                onTypeSelected = controller::onAddItemTypeSelected,
                onAccessTypeSelected = controller::onAddItemAccessTypeSelected,
                onItemSelected = controller::onAddItemSelected,
                onQuantityUpdateTypeSelected = controller::onAddItemQuantityUpdateTypeSelected,
                onExistingStockChanged = controller::onAddItemExistingStockChanged,
                onUsedStockQuantityChanged = controller::onAddItemUsedStockQuantityChanged,
                onNewStockQuantityChanged = controller::onAddItemNewStockQuantityChanged,
                onUpdatedByChanged = controller::onAddItemUpdatedByChanged,
                onBrandChanged = controller::onAddItemBrandChanged,
                onUnitChanged = controller::onAddItemUnitChanged,
                onStockSuppliedDateChanged = controller::onAddItemStockSuppliedDateChanged,
                onStockSuppliedTimeChanged = controller::onAddItemStockSuppliedTimeChanged,
                onUpdateStock = controller::onUpdateStock
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryHeader(
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Inventory",
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
        actions = {
            Spacer(modifier = Modifier.width(48.dp)) // Balance the navigation icon
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventorySearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = {
            Text(
                text = "Search by item name...",
                fontFamily = GraphikFontFamily,
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        singleLine = true
    )
}

@Composable
fun InventoryNotificationBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = Color(0x1ADD3825),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { /* Handle info click */ },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color(0xFFDD3825),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Tap an item to update stock",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = GraphikFontFamily,
            textAlign = TextAlign.Left,
            color = Color.Black,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFilters(
    selectedLocation: String,
    selectedType: String,
    locations: List<String>,
    types: List<String>,
    onLocationSelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location Dropdown
        InventoryDropdown(
            label = "Location",
            selectedValue = selectedLocation,
            options = locations,
            onValueSelected = onLocationSelected,
            modifier = Modifier.weight(1f)
        )

        // Type Dropdown
        InventoryDropdown(
            label = "Type",
            selectedValue = selectedType,
            options = types,
            onValueSelected = onTypeSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = PrimaryRed
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = GraphikFontFamily
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItemsGrid(
    items: List<InventoryItem>,
    onItemClick: (InventoryItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(items) { item ->
            InventoryItemCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(310.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Header with red tag and item name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.item_name),
                    contentDescription = "Item",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(PrimaryRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Item Name: ${item.name}",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item details in single column
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                InventoryItemDetail(
                    icon = painterResource(id = R.drawable.item_no),
                    label = "Item No:",
                    value = item.itemNumber
                )

                Spacer(modifier = Modifier.height(12.dp))

                InventoryItemDetail(
                    icon = painterResource(id = R.drawable.unit),
                    label = "Unit:",
                    value = item.unit
                )

                Spacer(modifier = Modifier.height(12.dp))

                InventoryItemDetail(
                    icon = painterResource(id = R.drawable.closing_stock),
                    label = "Total Stock:",
                    value = item.totalStock.toString()
                )

                Spacer(modifier = Modifier.height(12.dp))

                InventoryItemDetail(
                    icon = painterResource(id = R.drawable.updated_by),
                    label = "Updated By:",
                    value = item.updatedBy
                )

                Spacer(modifier = Modifier.height(12.dp))

                InventoryItemDetail(
                    icon = painterResource(id = R.drawable.supplied_date),
                    label = "Supplied Date:",
                    value = item.suppliedDate
                )

                Spacer(modifier = Modifier.height(12.dp))

                InventoryItemDetail(
                    icon = painterResource(id = R.drawable.last_updated),
                    label = "Last Updated:",
                    value = item.lastUpdated
                )
            }
        }
    }
}

@Composable
fun InventoryItemDetail(
    icon: androidx.compose.ui.graphics.painter.Painter? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    value: String,
    iconTint: Color = Color.Gray
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            iconVector != null -> {
                Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
            icon != null -> {
                Image(
                    painter = icon,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(iconTint)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

