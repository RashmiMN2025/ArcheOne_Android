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
        response.personalDoc.forEach { doc ->
            if (!doc.filePath.isNullOrEmpty()) {
                val displayName = personalDocTypes[doc.doc_type] ?: doc.docName
                newPersonalDocs[displayName] = doc.filePath
                newUploadStatus[displayName] = true
            }
        }
        
        response.professionalDoc.forEach { doc ->
            if (!doc.filePath.isNullOrEmpty()) {
                val displayName = professionalDocTypes[doc.doc_type] ?: doc.docName
                newProfessionalDocs[displayName] = doc.filePath
                newUploadStatus[displayName] = true
            }
        }

        // Update LiveData values
        personalDocs.postValue(newPersonalDocs)
        professionalDocs.postValue(newProfessionalDocs)
        uploadStatus.postValue(newUploadStatus)
    }

    // Handle viewing documents
    fun onViewClick(context: Context, documentName: String, isPersonal: Boolean) {
        val filePath = if (isPersonal) {
            personalDocs.value?.get(documentName)
        } else {
            professionalDocs.value?.get(documentName)
        }

        if (filePath.isNullOrEmpty()) {
            errorMessage.postValue("No document found for $documentName. Please upload document first.")
            return
        }

        // Check if the file is an image based on extension
        val isImage = filePath.endsWith(".jpg", ignoreCase = true) || 
                     filePath.endsWith(".jpeg", ignoreCase = true) || 
                     filePath.endsWith(".png", ignoreCase = true) ||
                     filePath.endsWith(".webp", ignoreCase = true)

        // Create appropriate intent based on file type
        val intent = if (isImage) {
            Intent(context, ImageViewerActivity::class.java)
        } else {
            Intent(context, WebViewActivity::class.java)
        }

        intent.putExtra("fileUrl", filePath)
        intent.putExtra("title", documentName)
        context.startActivity(intent)
    }
}
