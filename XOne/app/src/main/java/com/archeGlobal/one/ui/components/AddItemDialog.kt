package com.archeGlobal.one.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.R
import com.archeGlobal.one.model.AddItemModel
import com.archeGlobal.one.model.DialogMode
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    model: AddItemModel,
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onAccessTypeSelected: (String) -> Unit,
    onItemSelected: (String) -> Unit,
    onQuantityUpdateTypeSelected: (String) -> Unit,
    onExistingStockChanged: (String) -> Unit,
    onUsedStockQuantityChanged: (String) -> Unit,
    onNewStockQuantityChanged: (String) -> Unit,
    onUpdatedByChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onStockSuppliedDateChanged: (String) -> Unit,
    onStockSuppliedTimeChanged: (String) -> Unit,
    onUpdateStock: () -> Unit,
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
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F4EE))
                    .systemBarsPadding(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
            ) {
                // Header spacing
                Spacer(modifier = Modifier.height(80.dp))

                // Title based on mode
                Text(
                    text =
                        when (model.mode) {
                            DialogMode.ADD -> "Add New Inventory Item"
                            DialogMode.UPDATE -> "Update Inventory"
                        },
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 25.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                )

                // Content
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    when (model.mode) {
                        DialogMode.ADD -> {
                            // ADD MODE FIELDS in requested order:
                            // 1. Location
                            UpdateInventoryDropdown(
                                label = "Location",
                                selectedValue = model.selectedLocation,
                                options = model.locations,
                                onValueSelected = onLocationSelected,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Category Type
                            UpdateInventoryDropdown(
                                label = "Category Type",
                                selectedValue = model.selectedType,
                                options = model.types,
                                onValueSelected = onTypeSelected,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. Access Type
                            UpdateInventoryDropdown(
                                label = "Access Type",
                                selectedValue = model.selectedAccessType,
                                options = listOf("Admin", "User"),
                                onValueSelected = onAccessTypeSelected,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. Item Name
                            UpdateInventoryTextField(
                                label = "Item Name",
                                value = model.selectedItem,
                                onValueChange = onItemSelected,
                                placeholder = "Enter item name",
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 5. Unit
                            UpdateInventoryTextField(
                                label = "Unit",
                                value = model.unit,
                                onValueChange = onUnitChanged,
                                placeholder = "Enter unit",
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 6. Brand
                            UpdateInventoryTextField(
                                label = "Brand",
                                value = model.brand,
                                onValueChange = onBrandChanged,
                                placeholder = "Enter brand name",
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 7. Opening Stock
                            UpdateInventoryTextField(
                                label = "Opening Stock",
                                value = model.existingStock,
                                onValueChange = onExistingStockChanged,
                                placeholder = "Enter opening stock quantity",
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 8. Added By
                            UpdateInventoryTextField(
                                label = "Added By",
                                value = model.updatedBy,
                                onValueChange = onUpdatedByChanged,
                                placeholder = "Enter your name",
                            )
                        }
                        DialogMode.UPDATE -> {
                            // UPDATE MODE FIELDS in specified order:
                            // 1. Location (read-only)
                            UpdateInventoryTextField(
                                label = "Location",
                                value = model.selectedLocation,
                                onValueChange = { },
                                readOnly = true,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Type (read-only)
                            UpdateInventoryTextField(
                                label = "Type",
                                value = model.selectedType,
                                onValueChange = { },
                                readOnly = true,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. Item (read-only)
                            UpdateInventoryTextField(
                                label = "Item",
                                value = model.selectedItem,
                                onValueChange = { },
                                readOnly = true,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. Quantity Update Type Dropdown
                            UpdateInventoryDropdown(
                                label = "Quantity Update Type",
                                selectedValue = model.quantityUpdateType,
                                options = model.quantityUpdateTypes,
                                onValueSelected = onQuantityUpdateTypeSelected,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 5. Existing Stock (read-only)
                            UpdateInventoryTextField(
                                label = "Existing Stock",
                                value = model.existingStock,
                                onValueChange = { },
                                readOnly = true,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 6. Dynamic Stock Quantity (editable)
                            val stockQuantityLabel =
                                if (model.quantityUpdateType == "Update Used Quantity") {
                                    "Used Stock Quantity"
                                } else {
                                    "New Stock Quantity"
                                }

                            UpdateInventoryTextField(
                                label = stockQuantityLabel,
                                value = model.newStockQuantity,
                                onValueChange = onNewStockQuantityChanged,
                                placeholder =
                                    if (model.quantityUpdateType == "Update Used Quantity") {
                                        "Enter quantity used"
                                    } else {
                                        "Enter stock to add"
                                    },
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 7. Brand (read-only)
                            UpdateInventoryTextField(
                                label = "Brand",
                                value = model.brand,
                                onValueChange = { },
                                readOnly = true,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 8. Unit (read-only)
                            UpdateInventoryTextField(
                                label = "Unit",
                                value = model.unit,
                                onValueChange = { },
                                readOnly = true,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 9. Stock Supplied Date (calendar selector)
                            StockSuppliedDateField(
                                label = "Stock Supplied Date",
                                value = model.stockSuppliedDate,
                                onValueChange = onStockSuppliedDateChanged,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 10. Updated By (editable)
                            UpdateInventoryTextField(
                                label = "Updated By",
                                value = model.updatedBy,
                                onValueChange = onUpdatedByChanged,
                                placeholder = "Enter updated by",
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Side by side buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Action Button (left) - text changes based on mode
                        Button(
                            onClick = onUpdateStock,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(52.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = PrimaryRed,
                                    contentColor = Color.White,
                                ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text =
                                    when (model.mode) {
                                        DialogMode.ADD -> "Add Item"
                                        DialogMode.UPDATE -> "Update Stock"
                                    },
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                            )
                        }

                        // Cancel Button (right)
                        Button(
                            onClick = onDismiss,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(52.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray,
                                    contentColor = Color.White,
                                ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Floating close button - appears when scrolling
            if (showCloseButton) {
                // Horizontal background bar when scrolled down
                if (isScrolledDown) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(65.dp)
                                .background(
                                    color = Color.Gray.copy(alpha = 0.7f),
                                ),
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp)
                            .size(35.dp)
                            .background(
                                color = if (isScrolledDown) Color.Gray.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isScrolledDown) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp),
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
    onValueSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = PrimaryRed,
                    )
                },
                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    ),
                textStyle =
                    TextStyle(
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = GraphikFontFamily,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        },
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
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    placeholder: String = "",
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder =
                if (placeholder.isNotEmpty()) {
                    {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontSize = 16.sp,
                        )
                    }
                } else {
                    null
                },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                ),
            textStyle =
                TextStyle(
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            singleLine = true,
        )
    }
}

@Composable
private fun StockSuppliedDateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

    val datePickerDialog =
        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                onValueChange(formattedDate)
            },
            year,
            month,
            day,
        )
    // Set minimum date to today (only present/future dates allowed)
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    ).clickable {
                        datePickerDialog.show()
                    },
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else "Select date",
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    painter = painterResource(id = R.drawable.calendar_3x),
                    contentDescription = "Calendar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
