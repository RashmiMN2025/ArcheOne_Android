package com.archeGlobal.one.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.controller.MyDocumentsController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.components.UniversalLoader
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(controller: MyDocumentsController, context: Context, employeeId: String, onBackPressed: () -> Unit) {
    val personalDocs by controller.personalDocs.observeAsState(emptyMap())
    val professionalDocs by controller.professionalDocs.observeAsState(emptyMap())
    val isLoading by controller.isLoading.observeAsState(false)
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<String?>(null) }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera launcher - Moved before its usage
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Toast.makeText(context, "Image captured successfully", Toast.LENGTH_SHORT).show()
            showUploadDialog = false
            // TODO: Handle the bitmap - save/upload it
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Toast.makeText(context, "File selected successfully", Toast.LENGTH_SHORT).show()
            showUploadDialog = false
            // TODO: Handle the file - save/upload it
        }
    }

    LaunchedEffect(Unit) {
        val sampleName = "" // Change this based on your document
        val sampleUri = null // You need a valid URI to upload a document
        controller.uploadDocument(sampleName, sampleUri, employeeId,true)
    }
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

                Spacer(modifier = Modifier.weight(1f)) // Pushes text to center

                Text(
                    text = "My Documents",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1.5f)) // Balances right side
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
        if (isLoading) {
            UniversalLoader(isLoading = true)
        }

        // Upload Dialog
        if (showUploadDialog) {
            UploadDialog(
                onDismiss = { showUploadDialog = false },
                onCameraClick = {
                    if (hasCameraPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onFilesClick = { galleryLauncher.launch("application/pdf") }
            )
        }
    }
}

@Composable
private fun UploadDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFilesClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Upload Document",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    "Choose an option to upload your document",
                    fontSize = 17.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Camera Button
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fingerprint),
                        contentDescription = "Camera",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Camera", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gallery Button
                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery),
                        contentDescription = "Gallery",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Gallery", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Files Button
                Button(
                    onClick = onFilesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_file),
                        contentDescription = "Files",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Files", fontSize = 14.sp)
                }
            }
        }
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

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher for camera capture
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            controller.onUploadCameraImage(name, bitmap, employeeId)
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for file selection
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            controller.onUploadClick(name, uri, employeeId)
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
            .height(48.dp)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F0))
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
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showUploadOptions = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color(0xFFDD3825),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_upload_circle),
                            contentDescription = "Upload",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { controller.onViewClick(context, name, isPersonal) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_view_eye),
                        contentDescription = "View",
                        tint = Color(0xFFDD3825),
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Choose an option to upload your document:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Camera Button
                    Button(
                        onClick = {
                            showUploadOptions = false
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "Camera",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Camera", color = Color.White, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Gallery Button
                    Button(
                        onClick = {
                            showUploadOptions = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_gallery),
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Gallery", color = Color.White, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Files Button
                    Button(
                        onClick = {
                            showUploadOptions = false
                            fileLauncher.launch("application/pdf")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_file),
                            contentDescription = "Files",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Files", color = Color.White, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White
        )
    }
}