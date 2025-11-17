package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.EmployeeAssetsController
import com.archeGlobal.one.model.ApiAssetDetail
import com.archeGlobal.one.model.SuggestedAssetUser
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaggedAssetsByEmployeeScreen(
    tagName: String,
    employeeName: String,
    onBackPressed: () -> Unit,
) {
    var showAddAssetSheet by remember { mutableStateOf(false) }

    val viewModelFactory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return EmployeeAssetsController(tagName) as T
            }
        }
    }
    val controller: EmployeeAssetsController = viewModel(factory = viewModelFactory)
    val model = controller.model
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Gradient Background (same as AssetScreen)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749), // Dark Gray
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // TopAppBar - Matches AssetScreen exactly
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.offset(x = 27.dp),
                            text = "Tagged Assets",
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
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Optional: Keep "Tag Asset" action if needed
                    TextButton(
                        onClick = { showAddAssetSheet = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Tag Asset",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDD3825),
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Main Content Padding
            Box(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
            ) {
                if (model.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        UniversalLoader(isLoading = true)
                    }
                } else if (model.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = model.error ?: "Unknown error",
                            color = Color.Red,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    val employee = model.employee!!
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    ) {
                        // Employee Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = employee.username,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRowUniform("Emp ID:", employee.employeeCode)
                                InfoRowUniform("Location:", employee.location)
                                InfoRowUniform(
                                    "Reporting To:",
                                    employee.assets.firstOrNull()?.reportingTo ?: "N/A"
                                )
                            }
                        }


                        if (employee.assets.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No assets tagged as \"$tagName\" found for this employee.",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            employee.assets.forEach { asset ->
                                EmployeeAssetCard(
                                    asset = asset,
                                    onDelete = { serialNumber ->
                                        controller.deleteAsset(serialNumber) { message ->
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }


                    if (showAddAssetSheet) {
                        val employee = model.employee!!
                        val preSelected = SuggestedAssetUser(
                            employeeCode = employee.employeeCode,
                            username = employee.username,
                            location = employee.location,
                            designation = employee.designation,
                            division = employee.division,
                            department = employee.department,
                            mobileNumber = employee.mobileNumber,
                            emailId = employee.mailId,  // Map mailId to emailId
                            divisionalHead = employee.assets.firstOrNull()?.divisionalHead ?: "",
                            reportingManager = employee.assets.firstOrNull()?.reportingTo ?: ""
                        )

                        AddTaggedAssetBottomSheet(
                            tagName = tagName,
                            employeeName = employeeName,
                            onDismiss = { showAddAssetSheet = false },
                            preSelectedUser = preSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeAssetCard(
    asset: ApiAssetDetail,
    onDelete: (String) -> Unit
) {
    val iconRes = when {
        asset.assetType.lowercase().contains("laptop") -> R.drawable.laptop
        asset.assetType.lowercase().contains("mobile") -> R.drawable.mobile // Assume you have a phone icon
        else -> R.drawable.profile // Default
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon + Device Model + Serial No
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Asset",
                    tint = PrimaryRed,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.modelNumber,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                } else {
                    IconButton(
                        onClick = { onDelete(asset.serialNumber) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Untag Asset",
                            tint = Color.Red,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Asset Details
            InfoRowCompact("Asset Type:", asset.assetType)
            InfoRowCompact("Serial No:", asset.serialNumber)
            InfoRowCompact("Old asset ID:", asset.oldAssetId ?: "")
            InfoRowCompact("New Asset ID:", asset.newAssetId ?: "")
            InfoRowCompact("Purchase Date:", asset.purchaseDate)
            InfoRowCompact("Configuration:", asset.configuration)
            InfoRowCompact("Warranty Start:", asset.warrantyStart ?: "")
            InfoRowCompact("Warranty End:", asset.warrantyEnd ?: "")
            InfoRowCompact("Date Of Issue:", asset.dateOfIssue)
            if (asset.isTagged == 0) {
                InfoRowCompact("Tagging status", "Pending from IT Team")
            }
        }
    }
}

// Reusable Info Rows - copied from AssetScreen for consistency
@Composable
private fun InfoRowCompact(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
            fontWeight = FontWeight.Medium,
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
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = value.ifEmpty { "N/A" },
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}