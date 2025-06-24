package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.ImageViewerActivity
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.network.DocumentListResponse

class MyDocumentsController(private val context: Context) {

    // Maps for document type to display name for UI presentation
    val personalDocTypes = mapOf(
        "aadhar" to "Aadhar Card",
        "passport" to "Passport",
        "pan" to "PAN Card"
    )

    val professionalDocTypes = mapOf(
        "offer_letter" to "Offer Letter",
        "certificate" to "Certificate",
        "exp_letter" to "Experience Letter"
    )

    // Store document file paths for viewing
    val personalDocs = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    val professionalDocs = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    // Store uploaded documents for enabling/disabling buttons dynamically
    val uploadStatus = MutableLiveData<MutableMap<String, Boolean>>(
        mutableMapOf(
            "Aadhar Card" to false,
            "PAN Card" to false,
            "Passport" to false,
            "Offer Letter" to false,
            "Certificate" to false,
            "Experience Letter" to false
        )
    )

    // We're no longer using these since functionality moved to DocumentUploadManager
    // Keeping them here for UI updates only
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    // This method is only used for the UI state now,
    // actual document processing has been moved to DocumentUploadManager
    fun updateDocumentsFromResponse(response: DocumentListResponse) {
        Log.d("MyDocumentsController", "Processing document response with ${response.personalDoc?.size ?: 0} personal docs and ${response.professionalDoc?.size ?: 0} professional docs")

        val newPersonalDocs = mutableMapOf<String, String>()
        val newProfessionalDocs = mutableMapOf<String, String>()
        val newUploadStatus = uploadStatus.value ?: mutableMapOf()

        // Update file paths and upload status from response
        response.personalDoc?.forEachIndexed { index, doc ->
            Log.d("MyDocumentsController", "Processing personal doc[$index]: docName=${doc.docName}, doc_type=${doc.doc_type}, filePath=${doc.filePath}")

            // Prioritize filePath over doc_data since the API primarily uses filePath
            val docData = doc.filePath ?: doc.doc_data
            if (!docData.isNullOrEmpty()) {
                // Simpler display name resolution strategy - prefer docName from API directly
                val displayName = doc.docName ?: personalDocTypes[doc.doc_type] ?: "Unknown Document"

                // Only add if we have a valid display name
                if (displayName.isNotEmpty()) {
                    newPersonalDocs[displayName] = docData
                    newUploadStatus[displayName] = true
                    Log.d("MyDocumentsController", "✓ Added personal doc: $displayName with path: $docData")
                } else {
                    Log.w("MyDocumentsController", "✗ Skipped personal doc with empty display name: ${doc.doc_type}")
                }
            } else {
                // Still track upload status even if file path is empty
                val displayName = doc.docName ?: personalDocTypes[doc.doc_type] ?: "Unknown Document"
                if (displayName.isNotEmpty()) {
                    newUploadStatus[displayName] = false
                    Log.d("MyDocumentsController", "✗ Document has no file path: $displayName")
                }
            }
        }

        response.professionalDoc?.forEachIndexed { index, doc ->
            Log.d("MyDocumentsController", "Processing professional doc[$index]: docName=${doc.docName}, doc_type=${doc.doc_type}, filePath=${doc.filePath}")

            // Prioritize filePath over doc_data since the API primarily uses filePath
            val docData = doc.filePath ?: doc.doc_data
            if (!docData.isNullOrEmpty()) {
                // Simpler display name resolution strategy - prefer docName from API directly
                val displayName = doc.docName ?: professionalDocTypes[doc.doc_type] ?: "Unknown Document"

                // Only add if we have a valid display name
                if (displayName.isNotEmpty()) {
                    newProfessionalDocs[displayName] = docData
                    newUploadStatus[displayName] = true
                    Log.d("MyDocumentsController", "✓ Added professional doc: $displayName with path: $docData")
                } else {
                    Log.w("MyDocumentsController", "✗ Skipped professional doc with empty display name: ${doc.doc_type}")
                }
            } else {
                // Still track upload status even if file path is empty
                val displayName = doc.docName ?: professionalDocTypes[doc.doc_type] ?: "Unknown Document"
                if (displayName.isNotEmpty()) {
                    newUploadStatus[displayName] = false
                    Log.d("MyDocumentsController", "✗ Document has no file path: $displayName")
                }
            }
        }

        // Update LiveData values
        personalDocs.postValue(newPersonalDocs)
        professionalDocs.postValue(newProfessionalDocs)
        uploadStatus.postValue(newUploadStatus)

        // Log the document maps for debugging
        Log.d("MyDocumentsController", "Personal docs after update: ${newPersonalDocs.keys}")
        Log.d("MyDocumentsController", "Professional docs after update: ${newProfessionalDocs.keys}")
    }

    private fun validateAndFormatUrlAndDetectPdf(url: String?): Pair<String?, Boolean> {
        if (url.isNullOrEmpty()) {
            Log.w("MyDocumentsController", "validateAndFormatUrlAndDetectPdf: Received null or empty URL.")
            return null to false
        }

        var formattedUrl = url.trim()
        // Ensure the URL has a scheme
        if (!formattedUrl.startsWith("http://", ignoreCase = true) &&
            !formattedUrl.startsWith("https://", ignoreCase = true) &&
            !formattedUrl.startsWith("file://", ignoreCase = true)
        ) {
            // Default to https if no scheme is present and it's not a local file path
            if (formattedUrl.startsWith("/")) { // Basic check for an absolute path that might be local
                formattedUrl = "file://$formattedUrl"
                Log.d("MyDocumentsController", "URL appears to be a local path, prepended file://: $formattedUrl")
            } else {
                formattedUrl = "https://$formattedUrl"
                Log.d("MyDocumentsController", "URL was missing scheme, prepended https: $formattedUrl")
            }
        }

        val isPdf = formattedUrl.endsWith(".pdf", ignoreCase = true)
        Log.d("MyDocumentsController", "Validated URL: '$formattedUrl', isPdf: $isPdf")
        return formattedUrl to isPdf
    }

    // Handle viewing documents
    fun onViewClick(context: Context, documentName: String) {
        Log.d("MyDocumentsController", "onViewClick: Looking for document: $documentName")
        Log.d("MyDocumentsController", "Current personal docs: ${personalDocs.value?.keys?.joinToString() ?: "empty"}")
        Log.d("MyDocumentsController", "Current professional docs: ${professionalDocs.value?.keys?.joinToString() ?: "empty"}")

        // Check both personal and professional documents
        var rawFilePath = personalDocs.value?.get(documentName)
        if (!rawFilePath.isNullOrEmpty()) {
            Log.d("MyDocumentsController", "Found in personal docs: $documentName -> $rawFilePath")
        }

        // If not found in personal docs, check professional docs
        if (rawFilePath.isNullOrEmpty()) {
            rawFilePath = professionalDocs.value?.get(documentName)
            if (!rawFilePath.isNullOrEmpty()) {
                Log.d("MyDocumentsController", "Found in professional docs: $documentName -> $rawFilePath")
            }
        }

        if (rawFilePath.isNullOrEmpty()) {
            errorMessage.postValue("No document found for $documentName. Please upload document first.")
            Log.w("MyDocumentsController", "onViewClick: No file path found for document: $documentName")
            return
        }
        Log.d("MyDocumentsController", "onViewClick: Raw file path for $documentName: $rawFilePath")

        val (formattedUrl, isPdf) = validateAndFormatUrlAndDetectPdf(rawFilePath)

        if (formattedUrl.isNullOrEmpty()) {
            errorMessage.postValue("Invalid document URL for $documentName.")
            Log.e("MyDocumentsController", "onViewClick: Invalid or empty URL after validation for $documentName. Original path: $rawFilePath")
            return
        }

        // Determine if the file is an image (and not a PDF)
        val isImage = !isPdf && (
            formattedUrl.endsWith(".jpg", ignoreCase = true) ||
                formattedUrl.endsWith(".jpeg", ignoreCase = true) ||
                formattedUrl.endsWith(".png", ignoreCase = true) ||
                formattedUrl.endsWith(".webp", ignoreCase = true)
            )

        Log.d("MyDocumentsController", "onViewClick: Document '$documentName' - Formatted URL: '$formattedUrl', isPdf: $isPdf, isImage: $isImage")

        // Create appropriate intent based on file type
        val intent = if (isImage) {
            Log.d("MyDocumentsController", "onViewClick: Opening '$documentName' in ImageViewerActivity.")
            Intent(context, ImageViewerActivity::class.java).apply {
                putExtra("fileUrl", formattedUrl)
                putExtra("title", documentName)
            }
        } else { // For PDFs and other non-image documents
            Log.d("MyDocumentsController", "onViewClick: Opening '$documentName' in WebViewActivity. isPdf: $isPdf")
            Intent(context, WebViewActivity::class.java).apply {
                putExtra("fileUrl", formattedUrl)
                putExtra("title", documentName)
                putExtra("isPdf", isPdf) // Explicitly pass if it's a PDF
            }
        }
        context.startActivity(intent)
    }
}
