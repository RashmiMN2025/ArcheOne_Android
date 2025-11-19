package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    val controller: EmployeeAssetsController = viewModel(factory = EmployeeAssetsControllerFactory(tagName))
    val model = controller.model
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Track which asset is being deleted (for immediate UI feedback)
    var deletingSerial by remember { mutableStateOf<String?>(null) }
    var deleteSuccessMessage by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Build full employee data for the bottom sheet (works even when assets.isEmpty())
    fun buildPreSelectedUser(): SuggestedAssetUser {
        val emp = model.employee!!   // always exists after load
        val anyAsset = emp.assets.firstOrNull()

        return SuggestedAssetUser(
            employeeCode = emp.employeeCode,
            username = emp.username,
            location = emp.location ?: anyAsset?.location ?: "",
            designation = emp.designation ?: "",
            division = emp.division ?: anyAsset?.division ?: "",
            department = emp.department ?: "",
            mobileNumber = emp.mobileNumber ?: "",
            emailId = emp.mailId ?: "",
            divisionalHead = anyAsset?.divisionalHead ?: "",
            reportingManager = anyAsset?.reportingTo ?: ""
        )
    }
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
                when {
                    model.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            UniversalLoader(isLoading = true)
                        }
                    }

                    model.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(model.error!!, color = Color.Red, fontSize = 18.sp)
                        }
                    }

                    else -> {
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
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFF6F4EE
                                        )
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No assets tagged yet",
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 16.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                employee.assets.forEach { asset ->
                                    val isDeleting = deletingSerial == asset.serialNumber

                                    EmployeeAssetCard(
                                        asset = asset,
                                        isDeleting = isDeleting,
                                        deleteMessage = deleteSuccessMessage[asset.serialNumber],
                                        onDelete = { serialNumber ->
                                            deletingSerial = serialNumber

                                            controller.deleteAsset(serialNumber) { message ->
                                                // We are already on Main thread here (because deleteAsset switches to Main before calling onResult)
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                                                deleteSuccessMessage = deleteSuccessMessage + (serialNumber to message)
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }

        if (showAddAssetSheet) {
            AddTaggedAssetBottomSheet(
                tagName = tagName,
                employeeName = employeeName,
                preSelectedUser = buildPreSelectedUser(),
                onDismiss = { showAddAssetSheet = false },
                onSave = {
                    controller.refresh()          // reload after successful tagging
                    showAddAssetSheet = false
                }
            )
        }
    }
}

class EmployeeAssetsControllerFactory(private val employeeCode: String) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmployeeAssetsController(employeeCode) as T
    }
}

@Composable
fun EmployeeAssetCard(
    asset: ApiAssetDetail,
    onDelete: (String) -> Unit,
    isDeleting: Boolean = false,           // New state
    deleteMessage: String? = null
) {
    val iconRes = when {
        asset.assetType.lowercase().contains("laptop") -> R.drawable.laptop
        asset.assetType.lowercase().contains("mobile") -> R.drawable.mobile // Assume you have a phone icon
        else -> R.drawable.profile // Default
    }

    val backgroundColor = if (isDeleting) Color(0xFFF0F0F0).copy(alpha = 0.7f) else Color(0xFFF6F4EE)
    val textAlpha = if (isDeleting) 0.5f else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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

                if (asset.isTagged == 0 && !isDeleting) {
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
                }  else if (isDeleting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(
                        Color(0xFF4CAF50).copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Deleted",
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (asset.isTagged == 1) {
                IconButton(onClick = { onDelete(asset.serialNumber) }, enabled = !isDeleting) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = "Untag",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            }

            Spacer(modifier = Modifier.height(12.dp))

            deleteMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFF2E7D32),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Asset Details
            val textColor = if (isDeleting) Color.Gray else Color(0xFF8D8D8D)
            val valueColor = if (isDeleting) Color.Gray else Color.Black

            InfoRowCompact("Asset Type:", asset.assetType, textColor, valueColor)
            InfoRowCompact("Serial No:", asset.serialNumber, textColor, valueColor)
            InfoRowCompact("Old asset ID:", asset.oldAssetId ?: "N/A", textColor, valueColor)
            InfoRowCompact("New Asset ID:", asset.newAssetId ?: "N/A", textColor, valueColor)
            InfoRowCompact("Purchase Date:", asset.purchaseDate, textColor, valueColor)
            InfoRowCompact("Configuration:", asset.configuration, textColor, valueColor)
            InfoRowCompact("Warranty Start:", asset.warrantyStart ?: "N/A", textColor, valueColor)
            InfoRowCompact("Warranty End:", asset.warrantyEnd ?: "N/A", textColor, valueColor)
            InfoRowCompact("Date Of Issue:", asset.dateOfIssue, textColor, valueColor)
            if (asset.isTagged == 0) {
                InfoRowCompact("Tagging status", "Pending from IT Team",textColor, valueColor)
            }
        }
    }
}

// Reusable Info Rows - copied from AssetScreen for consistency
@Composable
private fun InfoRowCompact(
    label: String,
    value: String,
    labelColor: Color = Color(0xFF8D8D8D),
    valueColor: Color = Color.Black
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
            color = labelColor
        )
        Text(
            text = value.ifEmpty { "N/A" },
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = valueColor,
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