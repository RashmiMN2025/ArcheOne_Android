package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.AssetITAdminActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.AssetController
import com.archeGlobal.one.model.AssetModel
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetScreen(
    model: AssetModel,
    controller: AssetController,
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
    ) {
        var showIssueDialog by remember { mutableStateOf(false) }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFFE0DCD1), // Light Beige
                                    Color(0xFFC8C8CA), // Light Gray
                                    Color(0xFF474749), // Dark Gray
                                ),
                        ),
                    ),
        ) {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            modifier = Modifier.offset(x = 27.dp),
                            text = "Asset Information",
                            color = Color.Black,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { controller.navigateToTrackTickets() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                    ) {
                        Text(
                            text = "Track Tickets",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDD3825),
                            fontSize = 14.sp
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
            ) {
                when {
                    model.isLoading -> {
                        UniversalLoader(isLoading = model.isLoading)
                    }
                    model.error != null -> {
                        Text(
                            text = model.error,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // User Name and Admin Button Block
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Name + Admin Dashboard button side-by-side
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = model.name,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = {
                                                val intent = Intent(context, AssetITAdminActivity::class.java)
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                            shape = RoundedCornerShape(18.dp),
                                            modifier = Modifier.height(35.dp)
                                        ) {
                                            Text(
                                                text = "Admin Dashboard",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { controller.showSelfTagDialog() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)), // Grey
                                            shape = RoundedCornerShape(18.dp),
                                            modifier = Modifier.height(35.dp)
                                        ) {
                                            Text(
                                                "Tag Asset",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }

                                    }

                                    // Employee ID, Location, Reporting To — same style
                                    InfoRowUniform("Employee ID:", model.employeeId)
                                    InfoRowUniform("Location:", model.location)
                                    InfoRowUniform("Reporting To:", model.reportingTo)
                                }
                            }

                            // ASSET BLOCKS (per asset)
                            model.assetDetails.forEach { asset ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // Icon + Model Number + Serial Number
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val iconId = when (asset.assetType.trim().uppercase()) {
                                                "LAPTOP" -> R.drawable.laptop
                                                "MOBILE" -> R.drawable.mobile // Add your mobile icon
                                                else -> R.drawable.asset // Fallback icon
                                            }
                                            Icon(
                                                painter = painterResource(id = iconId),
                                                contentDescription = "Asset",
                                                tint = PrimaryRed,
                                                modifier = Modifier.size(50.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = asset.modelNumber,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 18.sp,
                                                    color = Color.Black
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = asset.serialNumber,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            if (asset.isTagged == 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .background(
                                                            color = Color(0xFFFFAA00).copy(alpha = 0.10f),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.duration), // Warning/pending icon
                                                        contentDescription = "Pending",
                                                        tint = Color(0xFFFFA500),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Approval Pending",
                                                        fontFamily = GraphikFontFamily,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFFFA500)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Updated key-value pairs with new fields
                                        InfoRowCompact("Asset Type", asset.assetType)
                                        InfoRowCompact("Asset ID/HostName", asset.newAssetId)
                                        InfoRowCompact("Serial No", asset.serialNumber)
                                        InfoRowCompact("Date Of Issue", asset.dateOfIssue)
                                        InfoRowCompact("Configuration", asset.configuration)
                                        if (asset.isTagged == 0) {
                                            InfoRowCompact("Tagging status", "Pending from IT Team")
                                        }
                                    }
                                }
                            }

                            // Reporting Any Issue Block with Icon
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.info), // Assume icon exists
                                        contentDescription = "Report Issue",
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Report any issues you may have with your assigned asset using \"Raise a Ticket\" bar below.",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        lineHeight = 22.sp
                                    )
                                }
                            }

                            // Raise an Issue Button
                            Button(
                                onClick = {
                                    // Navigate to RaiseConcernActivity with Asset prefilled
                                    val intent =
                                        android.content.Intent(context, com.archeGlobal.one.RaiseConcernActivity::class.java).apply {
                                            putExtra("source", "asset")
                                            putExtra("prefilledCategory", "Asset") // asset category pre-selected
                                        }
                                    context.startActivity(intent)
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = PrimaryRed,
                                    ),
                                shape = RoundedCornerShape(25.dp),
                            ) {
                                Text(
                                    "Raise a Ticket",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        if (controller.showSelfTagDialog) {
            SelfTagAssetDialog(
                controller = controller,
                onDismiss = { controller.hideSelfTagDialog() }
            )
        }
    }
}

@Composable
private fun InfoRowCompact(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF8D8D8D)
        )
        Text(
            text = value.ifEmpty { "N/A" },
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun InfoRowUniform(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color.Black,
//            modifier = Modifier.width(120.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = value.ifEmpty { "N/A" },
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelfTagAssetDialog(
    controller: AssetController,
    onDismiss: () -> Unit
) {
    val assetTypes = listOf("Laptop", "Mobile")
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.item_name),
                        contentDescription = "Tag",
                        tint = PrimaryRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Self Tag Asset",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Asset Type Dropdown with placeholder
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = controller.selectedAssetType,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            if (controller.selectedAssetType.isEmpty()) {
                                Text(
                                    "Select Asset Type",
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        )
                    )

                    // This is the dropdown list — background color applied here
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFFF6F4EE)) // Background for dropdown list
                    ) {
                        assetTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    controller.selectedAssetType = type
                                    expanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF6F4EE))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Model Name with placeholder
                OutlinedTextField(
                    value = controller.modelNumberInput,
                    onValueChange = { controller.modelNumberInput = it },
                    placeholder = { Text(
                        "Enter Model Name",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Serial Number with placeholder
                OutlinedTextField(
                    value = controller.serialNumberInput,
                    onValueChange = { controller.serialNumberInput = it },
                    placeholder = { Text(
                        "Enter Serial Number",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel & Submit Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF949494)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            "Cancel",
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { controller.submitSelfTagAsset() },
                        enabled = !controller.selfTagSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            "Submit",
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium)
                    }
                }

                // Result Message
                controller.selfTagResult?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
