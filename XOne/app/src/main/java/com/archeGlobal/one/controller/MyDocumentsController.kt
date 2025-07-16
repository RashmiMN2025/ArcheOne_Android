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

    // UI state
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    fun updateDocumentsFromResponse(response: DocumentListResponse) {
        Log.d("MyDocumentsController", "Processing document response with ${response.personalDoc?.size ?: 0} personal docs and ${response.professionalDoc?.size ?: 0} professional docs")

        val newPersonalDocs = mutableMapOf<String, String>()
        val newProfessionalDocs = mutableMapOf<String, String>()
        val newUploadStatus = uploadStatus.value?.toMutableMap() ?: mutableMapOf()

        // Update file paths and upload status from response
        response.personalDoc?.forEachIndexed { index, doc ->
            Log.d("MyDocumentsController", "Processing personal doc[$index]: docName=${doc.docName}, doc_type=${doc.doc_type}, filePath=${doc.filePath}")

            // Prioritize filePath over doc_data
            val docData = doc.filePath ?: doc.doc_data
            val displayName = doc.docName ?: personalDocTypes[doc.doc_type] ?: "Unknown Document"

            if (!docData.isNullOrEmpty() && displayName.isNotEmpty()) {
                newPersonalDocs[displayName] = docData
                newUploadStatus[displayName] = true
                Log.d("MyDocumentsController", "✓ Added personal doc: $displayName with path: $docData")
            } else {
                newUploadStatus[displayName] = false
                Log.d("MyDocumentsController", "✗ Document has no file path or invalid name: $displayName")
            }
        }

        response.professionalDoc?.forEachIndexed { index, doc ->
            Log.d("MyDocumentsController", "Processing professional doc[$index]: docName=${doc.docName}, doc_type=${doc.doc_type}, filePath=${doc.filePath}")

            // Prioritize filePath over doc_data
            val docData = doc.filePath ?: doc.doc_data
            val displayName = doc.docName ?: professionalDocTypes[doc.doc_type] ?: "Unknown Document"

            if (!docData.isNullOrEmpty() && displayName.isNotEmpty()) {
                newProfessionalDocs[displayName] = docData
                newUploadStatus[displayName] = true
                Log.d("MyDocumentsController", "✓ Added professional doc: $displayName with path: $docData")
            } else {
                newUploadStatus[displayName] = false
                Log.d("MyDocumentsController", "✗ Document has no file path or invalid name: $displayName")
            }
        }

        // Ensure all expected documents are in uploadStatus
        listOf("Aadhar Card", "Passport", "PAN Card", "Offer Letter", "Certificate", "Experience Letter").forEach { docName ->
            if (docName !in newUploadStatus) {
                newUploadStatus[docName] = false
            }
        }

        // Update LiveData values
        personalDocs.postValue(newPersonalDocs)
        professionalDocs.postValue(newProfessionalDocs)
        uploadStatus.postValue(newUploadStatus)

        // Log the document maps for debugging
        Log.d("MyDocumentsController", "Personal docs after update: ${newPersonalDocs.keys}")
        Log.d("MyDocumentsController", "Professional docs after update: ${newProfessionalDocs.keys}")
        Log.d("MyDocumentsController", "Upload status after update: $newUploadStatus")
    }

    private fun validateAndFormatUrlAndDetectPdf(url: String?): Pair<String?, Boolean> {
        if (url.isNullOrEmpty()) {
            Log.w("MyDocumentsController", "validateAndFormatUrlAndDetectPdf: Received null or empty URL.")
            return null to false
        }

        var formattedUrl = url.trim()
        if (!formattedUrl.startsWith("http://", ignoreCase = true) &&
            !formattedUrl.startsWith("https://", ignoreCase = true) &&
            !formattedUrl.startsWith("file://", ignoreCase = true)
        ) {
            if (formattedUrl.startsWith("/")) {
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

    fun onViewClick(context: Context, documentName: String) {
        Log.d("MyDocumentsController", "onViewClick: Looking for document: $documentName")
        Log.d("MyDocumentsController", "Current personal docs: ${personalDocs.value?.keys?.joinToString() ?: "empty"}")
        Log.d("MyDocumentsController", "Current professional docs: ${professionalDocs.value?.keys?.joinToString() ?: "empty"}")

        var rawFilePath = personalDocs.value?.get(documentName)
        if (!rawFilePath.isNullOrEmpty()) {
            Log.d("MyDocumentsController", "Found in personal docs: $documentName -> $rawFilePath")
        }

        if (rawFilePath.isNullOrEmpty()) {
            rawFilePath = professionalDocs.value?.get(documentName)
            if (!rawFilePath.isNullOrEmpty()) {
                Log.d("MyDocumentsController", "Found in professional docs: $documentName -> $rawFilePath")
            }
        }

        if (rawFilePath.isNullOrEmpty()) {
            android.widget.Toast.makeText(context, "No document found for $documentName. Please upload document for the same.", android.widget.Toast.LENGTH_SHORT).show()
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

        val isImage = !isPdf && (
                formattedUrl.endsWith(".jpg", ignoreCase = true) ||
                        formattedUrl.endsWith(".jpeg", ignoreCase = true) ||
                        formattedUrl.endsWith(".png", ignoreCase = true) ||
                        formattedUrl.endsWith(".webp", ignoreCase = true)
                )

        Log.d("MyDocumentsController", "onViewClick: Document '$documentName' - Formatted URL: '$formattedUrl', isPdf: $isPdf, isImage: $isImage")

        val intent = if (isImage) {
            Log.d("MyDocumentsController", "onViewClick: Opening '$documentName' in ImageViewerActivity.")
            Intent(context, ImageViewerActivity::class.java).apply {
                putExtra("fileUrl", formattedUrl)
                putExtra("title", documentName)
            }
        } else {
            Log.d("MyDocumentsController", "onViewClick: Opening '$documentName' in WebViewActivity. isPdf: $isPdf")
            Intent(context, WebViewActivity::class.java).apply {
                putExtra("fileUrl", formattedUrl)
                putExtra("title", documentName)
                putExtra("isPdf", isPdf)
            }
        }
        context.startActivity(intent)
    }
}