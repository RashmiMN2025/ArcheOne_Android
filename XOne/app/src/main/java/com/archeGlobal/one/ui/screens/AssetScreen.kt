package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.AssetController
import com.archeGlobal.one.model.AssetModel
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.components.UniversalLoader
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

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
        TopAppBar(
            title = { 
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Asset Information",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
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
                // Add invisible spacer with same size as navigation icon for balance
                Spacer(modifier = Modifier.width(48.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Box(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 15.dp) // Added horizontal padding
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
                                "User Informations",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            with(model.assetDetails) {
                                InfoRow("Serial No", serialNo)
                                InfoRow("Device Model", deviceModel)
                                InfoRow("Date Of Issue", dateOfIssue)
                                InfoRow("Configuration", configuration)
                            }

                            Divider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color.LightGray
                            )

                            // Report An Issue Section
                            Text(
                                "Report An Issue",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            var localText by remember { mutableStateOf("") }
                            val context = LocalContext.current
                            
                            // Use AndroidView with a standard EditText
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { context ->
                                    val editText = android.widget.EditText(context).apply {
                                        // Set properties
                                        hint = "Please describe your issue"
                                        setTextColor(android.graphics.Color.BLACK)
                                        setHintTextColor(android.graphics.Color.GRAY)
                                        gravity = android.view.Gravity.TOP
                                        minLines = 4
                                        maxLines = 6
                                        
                                        // Set background and padding
                                        background = android.graphics.drawable.GradientDrawable().apply {
                                            setColor(android.graphics.Color.WHITE)
                                            setStroke(2, android.graphics.Color.LTGRAY)
                                            cornerRadius = 16f
                                        }
                                        setPadding(24, 16, 24, 16)
                                        
                                        // Set text change listener
                                        addTextChangedListener(object : android.text.TextWatcher {
                                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                            override fun afterTextChanged(s: android.text.Editable?) {
                                                val newText = s.toString()
                                                localText = newText
                                                controller.onIssueDescriptionChange(newText)
                                            }
                                        })
                                    }
                                    editText
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(bottom = 8.dp)
                            )

                            Button(
                                onClick = { 
                                    if (localText.isBlank()) {
                                        Toast.makeText(context, "Please describe your issue", Toast.LENGTH_SHORT).show()
                                    } else {
                                        controller.onIssueDescriptionChange(localText)
                                        controller.onSubmitIssue()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryRed
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Submit",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.White
                                )
                            }
                        }
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
            .padding(vertical = 4.dp)
    ) {
        Text(
            "$label : ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value.ifEmpty { "N/A" },
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}