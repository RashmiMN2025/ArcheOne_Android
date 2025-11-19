package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.SelfTagController
import com.archeGlobal.one.model.SelfTagItem
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfTagRequestScreen(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val controller = remember { SelfTagController(context) }
    val model by controller.model.collectAsState()

    LaunchedEffect(Unit) {
        controller.loadSelfTagRequests()
    }

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
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1),
                            Color(0xFFC8C8CA),
                            Color(0xFF474749)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.offset(x = 5.dp),
                                text = "Self-Tag Approvals",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackPressed() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                if (model.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        UniversalLoader(isLoading = true)
                    }
                } else if (model.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${model.error}",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                } else if (model.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No self-tag requests found",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(model.items) { item ->
                            SelfTagRequestCard(
                                item = item,
                                controller = controller
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelfTagRequestCard(
    item: SelfTagItem,
    controller: SelfTagController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Employee Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.item_name),
                    contentDescription = "Employee",
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )


                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.username,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Empl ID: ${item.employeeCode}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Location: ${item.location}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            // Asset Details Heading
            Text(
                text = "Asset Details",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black
            )

            // Key-Value List
            AssetDetailRow(
                icon = R.drawable.laptop,
                label = "Asset Type",
                value = item.assetType
            )
            AssetDetailRow(
                icon = R.drawable.old_aset,
                label = "Old Asset ID",
                value = item.oldAssetId
            )
            AssetDetailRow(
                icon = R.drawable.new_asset,
                label = "New Asset ID",
                value = item.newAssetId
            )
            AssetDetailRow(
                icon = R.drawable.model_number,
                label = "Model Name",
                value = item.modelNumber
            )
            AssetDetailRow(
                icon = R.drawable.item_no,
                label = "Serial Number",
                value = item.serialNumber
            )
            AssetDetailRow(
                icon = R.drawable.configuration,
                label = "Configuration",
                value = item.configuration
            )
            AssetDetailRow(
                icon = R.drawable.envelope_3x,
                label = "Email",
                value = item.mailId
            )
            AssetDetailRow(
                icon = R.drawable.calendert,
                label = "Purchase Date",
                value = item.purchaseDate
            )

            // Action Buttons
            if (item.isTagged == 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            controller.approveTag(item.employeeCode, item.serialNumber)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "Approve",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            controller.rejectTag(item.employeeCode, item.serialNumber)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "Reject",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssetDetailRow(
    icon: Int,
    label: String,
    value: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)          // take the remaining space
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$label",
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(120.dp)
            )
        }
        Text(
            text = value ?: "N/A",
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier
                .wrapContentWidth()
        )
    }
}