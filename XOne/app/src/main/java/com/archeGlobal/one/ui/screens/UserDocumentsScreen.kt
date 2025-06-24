package com.archeGlobal.one.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.DocumentUploadManager
import com.archeGlobal.one.controller.UserDocumentsController
import com.archeGlobal.one.network.UserDocument
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import android.widget.Toast

@Composable
fun UserDocumentsScreen(
    controller: UserDocumentsController,
    context: Context,
    onBackPressed: () -> Unit
) {
    // Get documents from the controller
    val documents by controller.userDocuments.observeAsState(emptyList())
    
    // Observe loading state
    val isLoading by controller.isLoading.observeAsState(false)
    
    // Refresh documents data when screen becomes visible
    // This ensures we always have the latest data
    DisposableEffect(Unit) {
        // Refresh documents when the composable enters the composition
        controller.refreshDocuments()
        
        onDispose {
            // This block is called when the composable leaves the composition
            // No cleanup needed for this use case
        }
    }
    
    // Observe error messages
    val errorMessage by controller.errorMessage.observeAsState(null)
    
    // Observe upload success
    val uploadSuccess by controller.uploadSuccess.observeAsState(false)
    
    // State to track the currently selected document for upload
    var selectedDocument by remember { mutableStateOf<String?>(null) }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedDocument?.let { docName ->
                controller.uploadDocument(docName, selectedUri) { _ ->
                    // Reset selected document after upload
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
                
                // Centered Title
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
                // Empty box for symmetry
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
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Show loading indicator if needed
                    if (isLoading) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UniversalLoader(isLoading = true)
                        }
                    }
                    
                    // Show error message if any
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Always show all three document types
                    val requiredDocs = listOf("PAN Card", "ID Card", "Medical Insurance")
                    
                    // Create a map of existing documents by name
                    val docMap = documents.associateBy { it.document_name }
                    
                    // Create the final list of documents to show
                    val docsToShow = requiredDocs.map { docName ->
                        // Use existing document if available, otherwise create placeholder
                        docMap[docName] ?: UserDocument(docName, "")
                    }
                    
                    // Display each document
                    docsToShow.forEach { document ->                        DocumentItem(
                            document = document,
                            onViewClick = { controller.viewDocument(document) },
                            onUploadClick = { 
                                selectedDocument = document.document_name
                                // Explicitly specify PDF MIME type to only allow PDF files
                                filePickerLauncher.launch("application/pdf")
                                Toast.makeText(context, "Please select a PDF file", Toast.LENGTH_SHORT).show()
                            }
                        )
                        
                        // Add divider except after the last item
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

                    // Important Note Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Align icon and text in one line
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
    onViewClick: () -> Unit,
    onUploadClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Document row with icon and name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document icon (PDF)
            Icon(
                painter = painterResource(id = R.drawable.ic_pdf_document),
                contentDescription = null,
                tint = Color(0xFFDD3825),
                modifier = Modifier.size(24.dp)
            )
            
            // Document name
            Text(
                text = document.document_name,
                modifier = Modifier
                    .padding(start = 12.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        
        // Action buttons row
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View button with text
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
              // Upload button with text
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                onClick = onUploadClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Upload",
                        tint = Color(0xFFDD3825),
                        modifier = Modifier.size(20.dp)
                    )
                    
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