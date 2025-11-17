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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetBottomSheet(
    assetName: String,
    onDismiss: () -> Unit,
    onAddAsset: (AssetFormData) -> Unit
) {
    var makeModel by remember { mutableStateOf("") }
    var serialNo by remember { mutableStateOf("") }
    var configuration by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var warrantyStart by remember { mutableStateOf("") }
    var warrantyEnd by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var updatedBy by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFFF6F4EE),
        dragHandle = null
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
                    color = PrimaryRed,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 20.dp)
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
                onValueChange = { makeModel = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Serial No",
                value = serialNo,
                onValueChange = { serialNo = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Configuration",
                value = configuration,
                onValueChange = { configuration = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Purchase Date",
                value = purchaseDate,
                onValueChange = { purchaseDate = it },
                keyboardType = KeyboardType.Number,
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Warranty Start",
                value = warrantyStart,
                onValueChange = { warrantyStart = it },
                keyboardType = KeyboardType.Number,
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Warranty End",
                value = warrantyEnd,
                onValueChange = { warrantyEnd = it },
                keyboardType = KeyboardType.Number,
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Location",
                value = location,
                onValueChange = { location = it },
            )
            Spacer(modifier = Modifier.height(12.dp))

            AssetTextField(
                label = "Updated By",
                value = updatedBy,
                onValueChange = { updatedBy = it },
            )

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
                        fun formatDate(input: String): String? {
                            if (input.isBlank()) return null
                            return try {
                                val parts = input.split("-")
                                if (parts.size != 3) return null
                                val day = parts[0].padStart(2, '0')
                                val month = parts[1].padStart(2, '0')
                                val year = parts[2]
                                "$year-$month-$day"
                            } catch (e: Exception) {
                                null
                            }
                        }

                        val formattedPurchaseDate = formatDate(purchaseDate)
                        val formattedWarrantyStart = formatDate(warrantyStart)
                        val formattedWarrantyEnd = formatDate(warrantyEnd)

                        onAddAsset(
                            AssetFormData(
                                makeModel = makeModel.takeIf { it.isNotBlank() }.orEmpty(),
                                serialNo = serialNo.takeIf { it.isNotBlank() }.orEmpty(),
                                configuration = configuration.takeIf { it.isNotBlank() }.orEmpty(),
                                purchaseDate = formattedPurchaseDate.orEmpty(),
                                warrantyStart = formattedWarrantyStart.orEmpty(),
                                warrantyEnd = formattedWarrantyEnd.orEmpty(),
                                location = location.takeIf { it.isNotBlank() }.orEmpty(),
                                updatedBy = updatedBy.takeIf { it.isNotBlank() && it != "null" }.orEmpty()
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
                            text = "",
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

data class AssetFormData(
    val makeModel: String,
    val serialNo: String,
    val configuration: String,
    val purchaseDate: String,
    val warrantyStart: String,
    val warrantyEnd: String,
    val location: String,
    val updatedBy: String
)