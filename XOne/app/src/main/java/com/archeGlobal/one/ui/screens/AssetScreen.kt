package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.AssetController
import com.archeGlobal.one.model.AssetDetails
import com.archeGlobal.one.model.AssetModel
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetScreen(
    model: AssetModel,
    controller: AssetController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
        var showIssueDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749) // Dark Gray
                        )
                    )
                )
        ) {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Asset Information",
                            color = Color.Black,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { controller.onBackPressed() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(50.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp)
            ) {
                when {
                    model.isLoading -> {
                        UniversalLoader(isLoading = model.isLoading)
                    }
                    model.error != null -> {
                        Text(
                            text = model.error,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // User Information Section
                                Text(
                                    "User Information",
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                InfoRow("Name:", model.name)
                                InfoRow("Employee ID:", model.employeeId)
                                InfoRow("Mobile No:", model.mobile)
                                InfoRow("Email:", model.email)
                                InfoRow("Location:", model.location)

                                Divider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = Color.LightGray
                                )

                                // Asset Details Section
                                Text(
                                    "Asset Details",
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Iterate over the asset details array
                                model.assetDetails?.forEachIndexed { index, asset ->
                                    AssetDetailCard(asset)
                                    if (index < model.assetDetails.size - 1) { // Add a divider except after the last item
                                        Divider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = Color.LightGray,
                                            thickness = 1.dp
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = Color.LightGray
                                )

                                // Information Notice
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, start = 0.dp, end = 0.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFC8C8CA).copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.info),
                                            contentDescription = "Information",
                                            tint = Color(0xFFE94235),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = "Report any issues you may have with your assigned asset using \"Raise an issue\" bar below.",
                                            color = Color.Black,
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                // Raise an Issue Button
                                Button(
                                    onClick = { showIssueDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryRed
                                    ),
                                    shape = RoundedCornerShape(25.dp)
                                ) {
                                    Text(
                                        "Raise an Issue",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Issue Dialog
        if (showIssueDialog) {
            IssueDialog(
                onDismiss = { showIssueDialog = false },
                onSubmit = { issueText ->
                    controller.onIssueDescriptionChange(issueText)
                    controller.onSubmitIssue()
                    showIssueDialog = false
                }
            )
        }
    }
}

@Composable
fun IssueDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var issueText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5) // Cream color background
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Report an Issue",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Issue input field with rounded corners
                OutlinedTextField(
                    value = issueText,
                    onValueChange = { issueText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = { Text("Please describe your issue") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.Black,
                        cursorColor = Color.Gray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Submit button
                Button(
                    onClick = {
                        if (issueText.isBlank()) {
                            Toast.makeText(context, "Please describe your issue", Toast.LENGTH_SHORT).show()
                        } else {
                            onSubmit(issueText)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    )
                ) {
                    Text(
                        "Submit",
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Close text
                Text(
                    text = "Close",
                    color = PrimaryRed,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onDismiss() }
                )
            }
        }
    }
}

@Composable
fun AssetDetailCard(asset: AssetDetails) {
    // Add this log at the start of the composable
    android.util.Log.d("AssetScreen", "Displaying asset: hostName=${asset.hostName}")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Add some spacing between assets
    ) {
        InfoRow("Asset Type:", asset.assetType)
        InfoRow("Asset ID/Host Name:", asset.hostName)
        InfoRow("Serial No:", asset.serialNo)
        InfoRow("Device Model:", asset.deviceModel)
        InfoRow("Date Of Issue:", asset.dateOfIssue)
        InfoRow("Configuration:", asset.configuration)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value.ifEmpty { "N/A" },
            fontSize = 15.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
    }
}
