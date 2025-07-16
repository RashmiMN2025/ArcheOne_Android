package com.archeGlobal.one.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.UserDocumentsController
import com.archeGlobal.one.network.UserDocument
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun UserDocumentsScreen(
    controller: UserDocumentsController,
    context: Context,
    onBackPressed: () -> Unit
) {
    // Get documents from the controller
    val documents by controller.userDocuments.observeAsState(emptyList())
    val uploadStatus by controller.uploadStatus.observeAsState(emptyMap())

    // Observe loading state
    val isLoading by controller.isLoading.observeAsState(false)

    // Observe error messages
    val errorMessage by controller.errorMessage.observeAsState(null)

    // Observe upload success
    val uploadSuccess by controller.uploadSuccess.observeAsState(false)

    // Refresh documents data when screen becomes visible
    DisposableEffect(Unit) {
        controller.refreshDocuments()
        onDispose {}
    }

    // Handle upload success to refresh document list
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            controller.refreshDocuments()
        }
    }

    // State to track the currently selected document for upload
    var selectedDocument by remember { mutableStateOf<String?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedDocument?.let { docName ->
                controller.uploadDocument(docName, selectedUri) { _ ->
                    selectedDocument = null
                }
            }
        }
    }

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 35.dp, bottom = 10.dp)
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Documents",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Documents card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UniversalLoader(isLoading = true)
                        }
                    }

                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    val requiredDocs = listOf("PAN Card", "ID Card", "Medical Insurance Card")
                    val docMap = documents.associateBy { it.document_name }
                    val docsToShow = requiredDocs.map { docName ->
                        docMap[docName] ?: UserDocument(docName, "")
                    }

                    docsToShow.forEach { document ->
                        DocumentItem(
                            document = document,
                            isUploaded = uploadStatus[document.document_name] ?: false,
                            onViewClick = { controller.viewDocument(document) },
                            onUploadClick = {
                                selectedDocument = document.document_name
                                filePickerLauncher.launch("application/pdf")
                                Toast.makeText(context, "Please select a PDF file", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteClick = {
                                controller.deleteDocument(document) { success ->
                                    if (success) {
                                        Toast.makeText(context, "${document.document_name} deleted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to delete ${document.document_name}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        if (document != docsToShow.last()) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = Color.LightGray,
                                thickness = 1.5.dp
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = Color.LightGray,
                        thickness = 1.5.dp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "Note: You can only upload PDF files. The file size limit is 5MB.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                lineHeight = 17.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    document: UserDocument,
    isUploaded: Boolean,
    onViewClick: () -> Unit,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pdf_document),
                contentDescription = null,
                tint = Color(0xFFDD3825),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = document.document_name,
                modifier = Modifier.padding(start = 12.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.padding(end = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                onClick = onViewClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_view_eye),
                        contentDescription = "View",
                        tint = Color(0xFFDD3825),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "View",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                onClick = if (isUploaded) onDeleteClick else onUploadClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    if (isUploaded) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Delete",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
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
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "Upload",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}