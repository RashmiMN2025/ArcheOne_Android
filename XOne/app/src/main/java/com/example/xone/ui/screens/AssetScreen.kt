package com.example.xone.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.R
import com.example.xone.controller.AssetController
import com.example.xone.model.AssetModel
import com.example.xone.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetScreen(
    model: AssetModel,
    controller: AssetController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
    ) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    "Asset Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // User Information Section
                    Text(
                        "User Informations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    InfoRow("Name", model.name)
                    InfoRow("Employee ID", model.employeeId)
                    InfoRow("Mobile No", model.mobile)
                    InfoRow("Email", model.email)
                    InfoRow("Location", model.location)
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray
                    )

                    // Asset Details Section
                    Text(
                        "Asset Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    InfoRow("Serial No", model.assetDetails.serialNo)
                    InfoRow("Device Model", model.assetDetails.deviceModel)
                    InfoRow("Date Of Issue", model.assetDetails.dateOfIssue)
                    InfoRow("Configuration", model.assetDetails.configuration)
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray
                    )

                    // Report Issue Section
                    Text(
                        "Report An Issue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = model.issueDescription,
                        onValueChange = controller::onIssueDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Please describe your issue") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = PrimaryRed
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = controller::onSubmitIssue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                    ) {
                        Text(
                            "Submit",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            "$label : ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Text(
            value,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
} 