package com.archeGlobal.one.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onItemNameChanged: (String) -> Unit,
    onItemNumberChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onTotalStockChanged: (String) -> Unit,
    onAddItem: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Inventory Item",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location Dropdown
                AddItemDropdown(
                    label = "Location",
                    selectedValue = model.selectedLocation,
                    options = model.locations,
                    onValueSelected = onLocationSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type Dropdown
                AddItemDropdown(
                    label = "Type",
                    selectedValue = model.selectedType,
                    options = model.types,
                    onValueSelected = onTypeSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name Field
                AddItemTextField(
                    label = "Item Name",
                    value = model.itemName,
                    onValueChange = onItemNameChanged,
                    placeholder = ""
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item Number Field
                AddItemTextField(
                    label = "Item Number",
                    value = model.itemNumber,
                    onValueChange = onItemNumberChanged,
                    placeholder = ""
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Unit Field
                AddItemTextField(
                    label = "Unit",
                    value = model.unit,
                    onValueChange = onUnitChanged,
                    placeholder = "NA"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Field
                AddItemTextField(
                    label = "Brand",
                    value = model.brand,
                    onValueChange = onBrandChanged,
                    placeholder = "NA"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Total Stock Field
                AddItemTextField(
                    label = "Total Stock",
                    value = model.totalStock,
                    onValueChange = onTotalStockChanged,
                    placeholder = ""
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Add Item Button
                Button(
                    onClick = onAddItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Add Item",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDropdown(
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
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
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
                        contentDescription = "Dropdown"
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
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
private fun AddItemTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = GraphikFontFamily,
                        color = Color.Gray
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
