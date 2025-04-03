package com.archeGlobal.one.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.MyDocumentsController
import androidx.compose.ui.draw.clip
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.components.UniversalLoader

@Composable
fun MyDocumentsScreen(controller: MyDocumentsController, context: Context, employeeId: String, onBackPressed: () -> Unit) {
    val personalDocs by controller.personalDocs.observeAsState(emptyMap())
    val professionalDocs by controller.professionalDocs.observeAsState(emptyMap())
    val isLoading by controller.isLoading.observeAsState(false) // Observe the loading state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 10.dp)
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Centered Title
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Documents",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                        )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = "Upload or view your personal and professional documents here",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "Personal Documents",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        listOf("Aadhar Card", "Passport", "PAN Card").forEach { item ->
                            DocumentCard(item, personalDocs[item], controller, context, employeeId, true)
                        }
                    }

                    Text(
                        text = "Professional Documents",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        listOf("Offer Letter", "Certificate", "Experience Letter").forEach { item ->
                            DocumentCard(item, professionalDocs[item], controller, context, employeeId, false)
                        }
                    }

                    // Note about file size limit
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Text(
                            text = "Note: You can only upload images and PDFs. The file size limit is 5MB.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Show loader using UniversalLoader
        UniversalLoader(isLoading = isLoading)
    }
}

@Composable
fun DocumentCard(
    name: String,
    filePath: String?,
    controller: MyDocumentsController,
    context: Context,
    employeeId: String,
    isPersonal: Boolean
) {
    // State to show the upload options menu
    var showUploadOptions by remember { mutableStateOf(false) }

    // Launcher for file selection
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            controller.onUploadClick(name, uri, employeeId)
        }
    }

    // Launcher for camera capture
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            controller.onUploadCameraImage(name, bitmap, employeeId)
        }
    }

    // Launcher for image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            controller.onUploadClick(name, uri, employeeId)
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDD3825))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Upload button (white circle with red arrow)
                IconButton(
                    onClick = { showUploadOptions = true }, // Show upload options
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload_circle),
                        contentDescription = "Upload",
                        tint = Color.Unspecified, // Use the colors defined in the vector drawable
                        modifier = Modifier.size(24.dp)
                    )
                }

                // View button (eye icon)
                IconButton(
                    onClick = { controller.onViewClick(context, name, isPersonal) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_view_eye),
                        contentDescription = "View",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (showUploadOptions) {
        AlertDialog(
            onDismissRequest = { showUploadOptions = false },
            title = {
                Text(
                    text = "Upload Document",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // Title text in white for contrast
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                       // .background(Color.Black) // Set the dialog background to black
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Choose an option to upload your document:",
                        fontSize = 14.sp,
                        color = Color.Gray // Text in gray for readability
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showUploadOptions = false
                            fileLauncher.launch("*/*") // Allow all file types
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)) // Button background color
                    ) {
                        Text("Upload from File", color = Color.White) // Button text in white
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showUploadOptions = false
                            imagePickerLauncher.launch("image/*") // Allow only images
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)) // Button background color
                    ) {
                        Text("Pick from Gallery", color = Color.White) // Button text in white
                    }
                }
            },
            confirmButton = {}, // Remove the "Close" button
            dismissButton = {
                Button(
                    onClick = { showUploadOptions = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F4EE)) // Cancel button background color
                ) {
                    Text("Cancel", color = Color.Black) // Cancel button text in black
                }
            },
            containerColor = Color.White// Set the entire AlertDialog background to black
        )
    }
}