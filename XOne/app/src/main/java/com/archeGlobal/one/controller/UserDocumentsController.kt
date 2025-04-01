package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.UserDocument
import com.archeGlobal.one.utils.UserDataManager
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider

class UserDocumentsController(private val context: Context) {
    
    private val userDataManager = UserDataManager.getInstance(context)
    private val navigator: Navigator = AndroidNavigator(context as androidx.activity.ComponentActivity)
    
    // LiveData for documents
    private val _userDocuments = MutableLiveData<List<UserDocument>>(emptyList())
    val userDocuments: LiveData<List<UserDocument>> = _userDocuments
    
    init {
        loadUserDocuments()
    }
    
    private fun loadUserDocuments() {
        val userData = userDataManager.getUserData()
        val documents = userData?.userDetails?.documents ?: emptyList()
        _userDocuments.value = documents
        Log.d(TAG, "Loaded ${documents.size} documents")
    }
    
    fun viewDocument(document: UserDocument) {
        if (document.doc_data.isBlank()) {
            Toast.makeText(context, "No data available for ${document.document_name}", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Force treat PAN Card and Medical Insurance Card as PDFs
            val forcePdf = document.document_name == "PAN Card" || document.document_name == "Medical Insurance Card"
            
            Log.d(TAG, "Viewing document: ${document.document_name}, forcePdf: $forcePdf")
            
            // Create a temporary file from base64 data
            val fileInfo = saveBase64ToTempFile(document.doc_data, document.document_name, forcePdf)

            // Special handling for PAN Card and Medical Insurance Card - open directly with WebView in offline mode
            if (document.document_name == "PAN Card" || document.document_name == "Medical Insurance Card") {
                Log.d(TAG, "Special handling for ${document.document_name}: using WebView in offline mode")
                val intent = Intent(context, WebViewActivity::class.java).apply {
                    putExtra("fileUrl", fileInfo.uri.toString())
                    putExtra("title", document.document_name)
                    putExtra("isLocalFile", true)
                    putExtra("isPdf", true)
                    putExtra("base64Data", document.doc_data)  // Include raw base64 data
                    putExtra("useOfflineMode", true)  // Use offline mode for direct viewing
                }
                context.startActivity(intent)
                return
            }
            
            // Standard handling for other documents
            if (fileInfo.isPdf) {
                // For PDFs, use the dedicated PDF Viewer that policy uses
                Log.d(TAG, "Opening PDF with PDF Viewer: ${fileInfo.uri}, doc name: ${document.document_name}")
                
                // Try opening with the PDF viewer
                try {
                    navigator.navigateToPDFViewer(fileInfo.uri.toString(), document.document_name)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to PDF Viewer: ${e.message}")
                    
                    // Try with WebView as fallback
                    Log.d(TAG, "Falling back to WebView for PDF: ${fileInfo.uri}")
                    val intent = Intent(context, WebViewActivity::class.java).apply {
                        putExtra("fileUrl", fileInfo.uri.toString())
                        putExtra("title", document.document_name)
                        putExtra("isLocalFile", true)
                        putExtra("isPdf", true)
                        putExtra("base64Data", document.doc_data) // Add base64 data as additional fallback
                    }
                    
                    try {
                        context.startActivity(intent)
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error opening WebView fallback: ${e2.message}")
                        Toast.makeText(context, "Error viewing document: ${e2.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // For images and other non-PDF files, use WebView
                Log.d(TAG, "Opening non-PDF with WebView: ${fileInfo.uri}, doc name: ${document.document_name}")
                val intent = Intent(context, WebViewActivity::class.java).apply {
                    putExtra("fileUrl", fileInfo.uri.toString())
                    putExtra("title", document.document_name)
                    putExtra("isLocalFile", true)
                    putExtra("isPdf", false) // Explicitly mark as not PDF
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error viewing document: ${e.message}")
            Toast.makeText(context, "Error viewing document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun downloadDocument(document: UserDocument) {
        if (document.doc_data.isBlank()) {
            Toast.makeText(context, "No data available for ${document.document_name}", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Force treat PAN Card and Medical Insurance Card as PDFs
            val forcePdf = document.document_name == "PAN Card" || document.document_name == "Medical Insurance Card"
            
            // Create a file in the downloads folder
            val downloadFile = saveBase64ToDownloads(document.doc_data, document.document_name, forcePdf)
            
            Toast.makeText(context, "${document.document_name} downloaded successfully", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading document: ${e.message}")
            Toast.makeText(context, "Error downloading document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Class to hold file data and type information
    data class FileInfo(val uri: Uri, val isPdf: Boolean)
    
    private fun saveBase64ToTempFile(base64Data: String, documentName: String, forcePdf: Boolean = false): FileInfo {
        // Sanity check - make sure we have data
        if (base64Data.isBlank()) {
            Log.e(TAG, "Empty base64 data for $documentName")
            throw IllegalArgumentException("No data available for $documentName")
        }
        
        // Check if the base64 string begins with a data URI prefix
        val isPdfDataUri = base64Data.startsWith("data:application/pdf;base64,")
        val isImageDataUri = base64Data.startsWith("data:image/")
        
        // Log the beginning of the data for debugging
        val firstFewChars = if (base64Data.length > 30) base64Data.substring(0, 30) + "..." else base64Data
        Log.d(TAG, "Data for $documentName starts with: $firstFewChars")
        
        // Remove Base64 prefix if any (like "data:application/pdf;base64,")
        val pureBase64 = when {
            base64Data.contains(",") -> {
                base64Data.substring(base64Data.indexOf(",") + 1)
            }
            else -> base64Data
        }
        
        try {
            // Decode Base64 to byte array
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            Log.d(TAG, "Decoded ${decodedBytes.size} bytes for $documentName")
            
            // Create a file info before writing to ensure we get the right type
            val fileExtensionInfo = guessFileExtension(decodedBytes)
            
            // Override isPdf if forced
            val adjustedFileExtensionInfo = if (forcePdf) {
                FileExtensionInfo("pdf", true)
            } else {
                fileExtensionInfo
            }
            
            Log.d(TAG, "Determined file type: ${adjustedFileExtensionInfo.extension}, isPdf: ${adjustedFileExtensionInfo.isPdf} (force: $forcePdf)")
            
            // Use a safer filename
            val safeFileName = documentName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val tempFile = File(context.cacheDir, "${safeFileName}.${adjustedFileExtensionInfo.extension}")
            
            // Write decoded data to file
            FileOutputStream(tempFile).use { outputStream ->
                outputStream.write(decodedBytes)
                outputStream.flush()
            }
            
            // Verify file was created and has content
            if (tempFile.exists() && tempFile.length() > 0) {
                Log.d(TAG, "Successfully created file at ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
            } else {
                Log.e(TAG, "File creation failed or file is empty: ${tempFile.absolutePath}")
            }
            
            // Force isPdf if it came with PDF MIME type or if forced by parameter
            val finalIsPdf = isPdfDataUri || adjustedFileExtensionInfo.isPdf || forcePdf
            
            // Create a content URI using FileProvider for better access
            val fileUri = try {
                // Try to use FileProvider for secure access
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    tempFile
                )
            } catch (e: Exception) {
                // Fall back to basic file URI if FileProvider fails
                Log.w(TAG, "FileProvider failed, falling back to basic URI: ${e.message}")
                Uri.fromFile(tempFile)
            }
            
            Log.d(TAG, "Final URI for $documentName: $fileUri, isPdf: $finalIsPdf")
            
            return FileInfo(fileUri, finalIsPdf)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing base64 data: ${e.message}", e)
            throw e
        }
    }
    
    data class FileExtensionInfo(val extension: String, val isPdf: Boolean)
    
    private fun guessFileExtension(bytes: ByteArray): FileExtensionInfo {
        // Check file signature (magic numbers)
        if (bytes.size > 4) {
            // PDF signature: %PDF (25 50 44 46)
            if (bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() && 
                bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()) {
                return FileExtensionInfo("pdf", true)
            }
            
            // JPG signature: FF D8 FF
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && 
                bytes[2] == 0xFF.toByte()) {
                return FileExtensionInfo("jpg", false)
            }
            
            // PNG signature: 89 50 4E 47
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && 
                bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
                return FileExtensionInfo("png", false)
            }
        }
        
        // Default to PDF if unable to determine
        return FileExtensionInfo("pdf", true)
    }
    
    private fun saveBase64ToDownloads(base64Data: String, documentName: String, forcePdf: Boolean = false): File {
        // Remove Base64 prefix if any
        val pureBase64 = if (base64Data.contains(",")) {
            base64Data.substring(base64Data.indexOf(",") + 1)
        } else {
            base64Data
        }
        
        // Decode Base64 to byte array
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        
        // Determine file extension
        val fileExtensionInfo = guessFileExtension(decodedBytes)
        
        // Apply force PDF if requested
        val adjustedFileExtensionInfo = if (forcePdf) {
            FileExtensionInfo("pdf", true)
        } else {
            fileExtensionInfo
        }
        
        // Create file in downloads directory
        val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        val downloadFile = File(downloadsDir, "${documentName.replace(" ", "_")}.${adjustedFileExtensionInfo.extension}")
        
        // Write decoded data to file
        FileOutputStream(downloadFile).use { it.write(decodedBytes) }
        
        return downloadFile
    }
    
    companion object {
        private const val TAG = "UserDocumentsController"
    }
} 