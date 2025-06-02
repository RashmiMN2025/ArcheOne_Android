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
    val uploadStatus = MutableLiveData<MutableMap<String, Boolean>>(mutableMapOf(
        "Aadhar Card" to false,
        "PAN Card" to false,
        "Passport" to false,
        "Offer Letter" to false,
        "Certificate" to false,
        "Experience Letter" to false
    ))

    // We're no longer using these since functionality moved to DocumentUploadManager
    // Keeping them here for UI updates only
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)


    // This method is only used for the UI state now,
    // actual document processing has been moved to DocumentUploadManager
    fun updateDocumentsFromResponse(response: DocumentListResponse) {
        val newPersonalDocs = mutableMapOf<String, String>()
        val newProfessionalDocs = mutableMapOf<String, String>()
        val newUploadStatus = uploadStatus.value ?: mutableMapOf()

        // Update file paths and upload status from response
        response.personalDoc?.forEach { doc ->
            // Use doc_data from the API response or fall back to filePath
            val docData = doc.doc_data ?: doc.filePath
            if (!docData.isNullOrEmpty()) {
                // Use document_name from API or fall back to mapping from documentType/doc_type
                val docType = doc.documentType ?: doc.doc_type
                val displayName = if (doc.document_name != null) {
                    doc.document_name
                } else if (docType != null) {
                    personalDocTypes[docType] ?: doc.docName ?: "Unknown Document"
                } else {
                    doc.docName ?: "Unknown Document"
                }

                // Only add if we have a valid display name
                if (displayName.isNotEmpty()) {
                    newPersonalDocs[displayName] = docData
                    newUploadStatus[displayName] = true
                }
            }
        }

        response.professionalDoc?.forEach { doc ->
            // Use doc_data from the API response or fall back to filePath
            val docData = doc.doc_data ?: doc.filePath
            if (!docData.isNullOrEmpty()) {
                // Use document_name from API or fall back to mapping from documentType/doc_type
                val docType = doc.documentType ?: doc.doc_type
                val displayName = if (doc.document_name != null) {
                    doc.document_name
                } else if (docType != null) {
                    professionalDocTypes[docType] ?: doc.docName ?: "Unknown Document"
                } else {
                    doc.docName ?: "Unknown Document"
                }

                // Only add if we have a valid display name
                if (displayName.isNotEmpty()) {
                    newProfessionalDocs[displayName] = docData
                    newUploadStatus[displayName] = true
                }
            }
        }

        // Update LiveData values
        personalDocs.postValue(newPersonalDocs)
        professionalDocs.postValue(newProfessionalDocs)
        uploadStatus.postValue(newUploadStatus)
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
    fun onViewClick(context: Context, documentName: String, isPersonal: Boolean) {
        val rawFilePath = if (isPersonal) {
            personalDocs.value?.get(documentName)
        } else {
            professionalDocs.value?.get(documentName)
        }

        if (rawFilePath.isNullOrEmpty()) {
            errorMessage.postValue("No document found for $documentName. Please upload document first.")
            Log.w("MyDocumentsController", "onViewClick: No file path found for document: $documentName, isPersonal: $isPersonal")
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