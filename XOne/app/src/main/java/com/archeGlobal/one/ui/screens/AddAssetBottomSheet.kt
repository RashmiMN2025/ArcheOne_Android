package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetBottomSheet(
    assetName: String,
    onDismiss: () -> Unit,
    onAddAsset: (AssetFormData) -> Unit,
    locations: List<String>,
    isLoadingLocations: Boolean = false,
    locationError: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var makeModel by remember { mutableStateOf("") }
    var serialNo by remember { mutableStateOf("") }
    var configuration by remember { mutableStateOf("") }

    var purchaseDate by remember { mutableStateOf("") }
    var warrantyStart by remember { mutableStateOf("") }
    var warrantyEnd by remember { mutableStateOf("") }

    var locationExpanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }

    var showPurchasePicker by remember { mutableStateOf(false) }
    var showWarrantyStartPicker by remember { mutableStateOf(false) }
    var showWarrantyEndPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFFF6F4EE),
        dragHandle = null,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add $assetName to Inventory",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 30.dp)
                        .padding(end = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AssetTextField(
                label = "Make & Model",
                value = makeModel,
                placeholder = "Enter Make and Model *",
                onValueChange = { makeModel = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Serial No",
                value = serialNo,
                placeholder = "Enter serial number *",
                onValueChange = { serialNo = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Configuration",
                value = configuration,
                placeholder = "Enter configuration *",
                onValueChange = { configuration = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Purchase Date with DatePicker
            DateField(
                label = "Purchase Date",
                value = purchaseDate,
                placeholder = "Select purchase date *",
                onClick = { showPurchasePicker = true }
            )
            if (showPurchasePicker) {
                DatePickerModal(
                    onDateSelected = {
                        purchaseDate = it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        showPurchasePicker = false
                    },
                    onDismiss = { showPurchasePicker = false }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Warranty Start
            DateField(
                label = "Warranty Start",
                value = warrantyStart,
                placeholder = "Select start date *",
                onClick = { showWarrantyStartPicker = true }
            )
            if (showWarrantyStartPicker) {
                DatePickerModal(
                    onDateSelected = {
                        warrantyStart = it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        showWarrantyStartPicker = false
                    },
                    onDismiss = { showWarrantyStartPicker = false }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Warranty End
            DateField(
                label = "Warranty End",
                value = warrantyEnd,
                placeholder = "Enter end date *",
                onClick = { showWarrantyEndPicker = true }
            )
            if (showWarrantyEndPicker) {
                DatePickerModal(
                    onDateSelected = {
                        warrantyEnd = it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        showWarrantyEndPicker = false
                    },
                    onDismiss = { showWarrantyEndPicker = false }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = "Location",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = locationExpanded && !isLoadingLocations && locations.isNotEmpty(),
                    onExpandedChange = { if (!isLoadingLocations) locationExpanded = it }
                ) {
                    Box(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                            .clickable(enabled = !isLoadingLocations) { locationExpanded = true }
                    ) {
                        Text(
                            text = when {
                                isLoadingLocations -> "Loading locations..."
                                locationError != null -> "Failed to load"
                                selectedLocation.isEmpty() -> "Select location *"
                                else -> selectedLocation
                            },
                    color = when {
                        isLoadingLocations || locationError != null -> Color.Gray
                        selectedLocation.isEmpty() -> Color.Gray.copy(0.6f)
                        else -> Color.Black
                    },
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                    )
                }

                    ExposedDropdownMenu(
                        expanded = locationExpanded && locations.isNotEmpty(),
                        onDismissRequest = { locationExpanded = false },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .background(Color(0xFFF6F4EE))
                    ) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        location,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                        ) },
                                onClick = {
                                    selectedLocation = location
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color(0xFF949494)
                    )
                ) {
                    Text(
                        "Cancel",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        // Helper: Convert nullable string to non-nullable (empty if null)
                        fun String?.orEmpty() = this ?: ""

                        // Convert DD-MM-YYYY → YYYY-MM-DD
                        fun String.toIsoDate(): String {
                            if (isBlank()) return ""
                            return try {
                                val (d, m, y) = split("-").map { it.toInt() }
                                "%04d-%02d-%02d".format(y, m, d)
                            } catch (e: Exception) { "" }
                        }

                        onAddAsset(
                            AssetFormData(
                                makeModel = makeModel.trim(),
                                serialNo = serialNo.trim(),
                                configuration = configuration.trim(),
                                purchaseDate = purchaseDate.toIsoDate(),
                                warrantyStart = warrantyStart.toIsoDate(),
                                warrantyEnd = warrantyEnd.toIsoDate(),
                                location = selectedLocation
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Add Asset",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AssetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(horizontal = 0.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp), // Only internal text padding
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun DateField(
    label: String,
    placeholder: String,
    value: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = if (value.isEmpty()) placeholder else value,
                color = if (value.isEmpty()) Color.Gray.copy(alpha = 0.5f) else Color.Black,
                fontSize = 12.sp,
                fontFamily = GraphikFontFamily,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )

            Icon(
                painter = painterResource(id = R.drawable.calendert),
                contentDescription = "Select Date",
                tint = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
        }
    }
}

// Material3 Date Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val localDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(localDate)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Updated data class (removed updatedBy)
data class AssetFormData(
    val makeModel: String,
    val serialNo: String,
    val configuration: String,
    val purchaseDate: String,
    val warrantyStart: String,
    val warrantyEnd: String,
    val location: String
)