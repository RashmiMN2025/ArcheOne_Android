package com.archeGlobal.one.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.model.AddItemModel
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    model: AddItemModel,
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onItemSelected: (String) -> Unit,
    onExistingStockChanged: (String) -> Unit,
    onNewStockQuantityChanged: (String) -> Unit,
    onUpdatedByChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onStockSuppliedDateChanged: (String) -> Unit,
    onStockSuppliedTimeChanged: (String) -> Unit,
    onUpdateStock: () -> Unit
) {
    val scrollState = rememberScrollState()
    val showCloseButton by remember {
        derivedStateOf { scrollState.value <= 50 || scrollState.value > 100 }
    }
    val isScrolledDown by remember {
        derivedStateOf { scrollState.value > 100 }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F4EE))
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header spacing
                Spacer(modifier = Modifier.height(80.dp))

                // Update Inventory Title
                Text(
                    text = "Update Inventory",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Location Dropdown
                    UpdateInventoryDropdown(
                        label = "Location",
                        selectedValue = model.selectedLocation,
                        options = model.locations,
                        onValueSelected = onLocationSelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Type Dropdown
                    UpdateInventoryDropdown(
                        label = "Type",
                        selectedValue = model.selectedType,
                        options = model.types,
                        onValueSelected = onTypeSelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Item Dropdown
                    UpdateInventoryDropdown(
                        label = "Item",
                        selectedValue = model.selectedItem,
                        options = model.items,
                        onValueSelected = onItemSelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Existing Stock Field
                    UpdateInventoryTextField(
                        label = "Existing Stock",
                        value = model.existingStock,
                        onValueChange = onExistingStockChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // New Stock Quantity Field
                    UpdateInventoryTextField(
                        label = "New Stock Quantity",
                        value = model.newStockQuantity,
                        onValueChange = onNewStockQuantityChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Updated By Field
                    UpdateInventoryTextField(
                        label = "Updated By",
                        value = model.updatedBy,
                        onValueChange = onUpdatedByChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Brand Field
                    UpdateInventoryTextField(
                        label = "Brand",
                        value = model.brand,
                        onValueChange = onBrandChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Unit Field
                    UpdateInventoryTextField(
                        label = "Unit",
                        value = model.unit,
                        onValueChange = onUnitChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stock Supplied Date Section
                    Text(
                        text = "Stock Supplied Date",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = model.stockSuppliedDate,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }

                            // Time Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = model.stockSuppliedTime,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Update Stock Button
                    Button(
                        onClick = onUpdateStock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Update Stock",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Floating close button - appears when scrolling
            if (showCloseButton) {
                // Horizontal background bar when scrolled down
                if (isScrolledDown) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(65.dp)
                            .background(
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .size(35.dp)
                        .background(
                            color = if (isScrolledDown) Color.Gray.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isScrolledDown) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateInventoryDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
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
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = TextStyle(
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
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
                                fontFamily = GraphikFontFamily,
                                fontSize = 16.sp
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
private fun UpdateInventoryTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            textStyle = TextStyle(
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true
        )
    }
}
