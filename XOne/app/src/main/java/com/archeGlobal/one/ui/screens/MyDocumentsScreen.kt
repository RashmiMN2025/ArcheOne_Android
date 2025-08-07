package com.archeGlobal.one.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.DocumentUploadManager
import com.archeGlobal.one.controller.MpinController
import com.archeGlobal.one.controller.MyDocumentsController
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(controller: MyDocumentsController, context: Context, onBackPressed: () -> Unit) {
    // Create the document upload manager
    val uploadManager = remember { DocumentUploadManager(context) }

    val personalDocs by controller.personalDocs.observeAsState(emptyMap())
    val professionalDocs by controller.professionalDocs.observeAsState(emptyMap())
    val uploadStatus by controller.uploadStatus.observeAsState(emptyMap())
    val isLoading by uploadManager.isLoading.observeAsState(false)
    val errorMessage by uploadManager.errorMessage.observeAsState(null)
    val uploadSuccess by uploadManager.uploadSuccess.observeAsState(false)

    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<String?>(null) }

    val mpinController = remember { MpinController(context) }
    val userDataManager = remember { UserDataManager.getInstance(context) }
    var showMpinPrompt by remember { mutableStateOf(true) }
    var enteredMpin by remember { mutableStateOf("") }
    var mpinError by remember { mutableStateOf<String?>(null) }
    val focusRequesters = remember { List(4) { FocusRequester() } }
    var focusedIndex by remember { mutableStateOf(-1) }
    var isVerifyingMpin by remember { mutableStateOf(false) }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Toast.makeText(context, "Image captured successfully", Toast.LENGTH_SHORT).show()
            showUploadDialog = false
            selectedDocument?.let { docName ->
                uploadManager.uploadDocumentFromBitmap(docName, it) { response ->
                    controller.updateDocumentsFromResponse(response)
                }
            }
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
            selectedDocument?.let { docName ->
                uploadManager.uploadDocumentFromUri(docName, uri) { response ->
                    controller.updateDocumentsFromResponse(response)
                }
            }
        }
    }

    // File launcher for PDFs
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Toast.makeText(context, "File selected successfully", Toast.LENGTH_SHORT).show()
            showUploadDialog = false
            selectedDocument?.let { docName ->
                uploadManager.uploadDocumentFromUri(docName, uri) { response ->
                    controller.updateDocumentsFromResponse(response)
                }
            }
        }
    }

    // Show error message if any
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Handle upload success
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            // Refresh document list
            uploadManager.listDocuments { response ->
                Log.d("MyDocumentsScreen", "Refreshing documents after successful upload")
                controller.updateDocumentsFromResponse(response)
            }
        }
    }

    // Fetch documents only after MPIN is verified
    LaunchedEffect(showMpinPrompt) {
        if (!showMpinPrompt) {
            Log.d("MyDocumentsScreen", "MPIN verified, loading documents")
            uploadManager.listDocuments { response ->
                controller.updateDocumentsFromResponse(response)
            }
        }
    }

    // Track if initial load has completed to avoid showing toast prematurely
    var hasInitialLoadCompleted by remember { mutableStateOf(false) }

    // Update when loading completes, but only after MPIN is verified
    LaunchedEffect(isLoading, showMpinPrompt) {
        if (!isLoading && !hasInitialLoadCompleted && !showMpinPrompt) {
            hasInitialLoadCompleted = true
        }
    }

    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isKeyboardVisible = imeInsets.getBottom(density) > 0

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
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749) // Dark Gray
                        )
                    )
                )
                .then(if (showMpinPrompt) Modifier.blur(8.dp) else Modifier)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 10.dp)
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "My Documents",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1.5f))
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
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
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 32.dp),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Personal Documents",
                                    fontSize = 17.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
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
                                        DocumentCard(
                                            name = item,
                                            filePath = personalDocs[item],
                                            isUploaded = uploadStatus[item] ?: false,
                                            controller = controller,
                                            context = context,
                                            uploadManager = uploadManager,
                                            fileLauncher = fileLauncher,
                                            setSelectedDocument = { selectedDocument = it }
                                        )
                                    }
                                }

                                Text(
                                    text = "Professional Documents",
                                    fontSize = 17.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
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
                                        DocumentCard(
                                            name = item,
                                            filePath = professionalDocs[item],
                                            isUploaded = uploadStatus[item] ?: false,
                                            controller = controller,
                                            context = context,
                                            uploadManager = uploadManager,
                                            fileLauncher = fileLauncher,
                                            setSelectedDocument = { selectedDocument = it }
                                        )
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
                                        fontSize = 12.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
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
                    onFilesClick = { fileLauncher.launch("application/pdf") }
                )
            }
        }

        if (showMpinPrompt) {
            // Blur and block background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
                    .blur(6.dp)
                    .zIndex(10f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            if (dragAmount > 60) { // right swipe
                                onBackPressed()
                            }
                        }
                    }
            )

            // Loader if verifying
            if (isVerifyingMpin) {
                UniversalLoader(isLoading = true)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(101f),
                contentAlignment = if (isKeyboardVisible) Alignment.TopCenter else Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFFFEF7F2),
                    shadowElevation = 24.dp,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .widthIn(min = 340.dp, max = 420.dp)
                        .padding(horizontal = 16.dp)
                        .then(
                            if (isKeyboardVisible) {
                                Modifier.padding(top = 32.dp)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                            .widthIn(min = 340.dp, max = 420.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = com.archeGlobal.one.R.drawable.lock),
                            contentDescription = "Lock",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            "Enter Your MPIN",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Please enter your 4-digit MPIN to access your documents",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Enter MPIN",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Color(0xFF7B7B7B),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 0 until 4) {
                                val hasDigit = enteredMpin.getOrNull(i)?.isDigit() == true
                                OutlinedTextField(
                                    value = enteredMpin.getOrNull(i)?.toString() ?: "",
                                    onValueChange = { value ->
                                        if (value.length <= 1 && value.all { it.isDigit() }) {
                                            val chars = enteredMpin.padEnd(4).toCharArray()
                                            chars[i] = value.firstOrNull() ?: ' '
                                            enteredMpin = String(chars).replace(" ", "")
                                            if (value.isNotEmpty() && i < 3) {
                                                focusRequesters[i + 1].requestFocus()
                                            }
                                        }
                                        if (value.isEmpty() && i > 0) {
                                            val chars = enteredMpin.padEnd(4).toCharArray()
                                            chars[i] = ' '
                                            enteredMpin = String(chars).replace(" ", "")
                                            focusRequesters[i - 1].requestFocus()
                                        }
                                    },
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(65.dp)
                                        .focusRequester(focusRequesters[i])
                                        .padding(horizontal = 4.dp)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) focusedIndex = i
                                        }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (focusedIndex == i) Color(0xFFDD3825) else Color.Gray,
                                            shape = MaterialTheme.shapes.medium
                                        ),
                                    textStyle = TextStyle(
                                        fontSize = 28.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    ),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        disabledContainerColor = Color.White,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        disabledTextColor = Color.Black,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    isError = mpinError != null && enteredMpin.length == 4
                                )
                                if (i < 3) Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                        Button(
                            onClick = {
                                if (enteredMpin.isEmpty()) {
                                    mpinError = "Please enter the MPIN"
                                } else if (enteredMpin.length < 4) {
                                    mpinError = "Please enter the MPIN"
                                } else {
                                    isVerifyingMpin = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        kotlinx.coroutines.delay(700)
                                        if (mpinController.validateMpin(enteredMpin)) {
                                            mpinError = null
                                            enteredMpin = ""
                                            showMpinPrompt = false
                                            userDataManager.preferencesManager.setAppLockState(false)
                                        } else {
                                            mpinError = "Invalid MPIN"
                                            enteredMpin = ""
                                        }
                                        isVerifyingMpin = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDD3825),
                                contentColor = Color.White
                            ),
                            enabled = !isVerifyingMpin
                        ) {
                            Text(
                                "Unlock",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Reset MPIN button
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(context, com.archeGlobal.one.ui.screens.MpinActivity::class.java)
                                intent.putExtra("resetMpin", true)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .width(140.dp)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0B4AA),
                                contentColor = Color(0xFFDD3825),
                                disabledContainerColor = Color(0xFFE0B4AA),
                                disabledContentColor = Color(0xFFDD3825)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFDD3825)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "Reset MPIN",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (mpinError != null) {
                            Text(
                                text = mpinError!!,
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
            return // Block rest of the screen until MPIN is entered
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
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    "Choose an option to upload your document",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
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
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Camera",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Camera",
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Text(
                        "Gallery",
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Text(
                        "Files",
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentCard(
    name: String,
    filePath: String?,
    isUploaded: Boolean,
    controller: MyDocumentsController,
    context: Context,
    uploadManager: DocumentUploadManager,
    fileLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    setSelectedDocument: (String?) -> Unit
) {
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
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (isUploaded) {
                            // Delete document
                            val documentType = controller.personalDocTypes.entries
                                .find { it.value == name }?.key
                                ?: controller.professionalDocTypes.entries
                                    .find { it.value == name }?.key
                            if (documentType != null) {
                                uploadManager.deleteDocument(name, documentType) { response ->
                                    controller.updateDocumentsFromResponse(response)
                                    Toast.makeText(context, "Document deleted successfully", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Error: Unknown document type", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Set the selected document and launch file picker
                            setSelectedDocument(name)
                            fileLauncher.launch("application/pdf")
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isUploaded) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
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
                }

                IconButton(
                    onClick = { controller.onViewClick(context, name) },
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
}
