package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.ImageViewerActivity
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.network.DocumentListResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.network.UserDocument
import com.archeGlobal.one.utils.UserDataManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDocumentsController(private val context: Context) {
    
    companion object {
        private const val TAG = "UserDocumentsController"
    }
    
    private val userDataManager = UserDataManager.getInstance(context)
    private val navigator: Navigator = AndroidNavigator(context as ComponentActivity)
    private val documentUploadManager = DocumentUploadManager(context)
    
    // LiveData for documents
    private val _userDocuments = MutableLiveData<List<UserDocument>>(emptyList())
    val userDocuments: LiveData<List<UserDocument>> = _userDocuments
    
    // Expose DocumentUploadManager states
    val isLoading = documentUploadManager.isLoading
    val errorMessage = documentUploadManager.errorMessage
    val uploadSuccess = documentUploadManager.uploadSuccess
    
    init {
        loadUserDocuments()
        fetchDocumentsFromApi()
    }
    
    /**
     * Public method to refresh documents data when returning to the screen
     * Call this method in onResume or when the screen becomes visible again
     */
    fun refreshDocuments() {
        Log.d(TAG, "Refreshing documents data")
        fetchDocumentsFromApi()
    }
    
    private fun loadUserDocuments() {
        val userData = userDataManager.getUserData()
        val documents = userData?.userDetails?.documents ?: emptyList()
        _userDocuments.value = documents
        Log.d(TAG, "Loaded ${documents.size} documents from user data")
    }
    
    private fun fetchDocumentsFromApi() {
        Log.d(TAG, "Fetching latest documents from API")
        isLoading.postValue(true)
        documentUploadManager.listDocuments { response ->
            isLoading.postValue(false)
            processApiResponse(response)
        }
    }
    
    private fun processApiResponse(response: DocumentListResponse) {
        // Process personal documents from the API response
        val personalDocs = response.personalDoc?.map { doc ->
            // Use the document_name from the API response directly if available
            // Otherwise, map from documentType
            val docName = doc.document_name ?: when (doc.documentType) {
                "id" -> "ID Card"
                "pan" -> "PAN Card"
                "medical" -> "Medical Insurance"
                else -> "Unknown Document"
            }
            
            UserDocument(
                document_name = docName,
                doc_data = doc.doc_data ?: ""
            )
        }
        
        // Log the documents for debugging
        personalDocs?.forEach { doc ->
            Log.d(TAG, "Processed document: ${doc.document_name}, data: ${if (doc.doc_data.isBlank()) "empty" else "has data"}")
        }
        
        // Update the LiveData with the new documents
        _userDocuments.postValue(personalDocs ?: emptyList())
        Log.d(TAG, "Updated documents from API: ${personalDocs?.size ?: 0} documents")
    }
    
    fun viewDocument(document: UserDocument) {
        try {
            Log.d(TAG, "Requesting document for viewing: ${document.document_name}, type: ${document.documentType}")
            
            // Show loading indicator
            isLoading.postValue(true)
            
            // Get user credentials
            val email = userDataManager.getUserData()?.email ?: ""
            val employeeId = userDataManager.getUserData()?.employeeId ?: ""
            
            if (email.isEmpty() || employeeId.isEmpty()) {
                Toast.makeText(context, "User information not available", Toast.LENGTH_SHORT).show()
                isLoading.postValue(false)
                return
            }
            
            // Make an API call to get the latest document URL
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val isPersonalPart = "true".toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Make the document listing API call to get latest URLs
            RetrofitClient.apiService.listDocuments(emailPart, employeeIdPart, isPersonalPart).enqueue(object : Callback<DocumentListResponse> {
                override fun onResponse(
                    call: Call<DocumentListResponse>,
                    response: Response<DocumentListResponse>
                ) {
                    isLoading.postValue(false)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        
                        // Log the entire response for debugging
                        Log.d(TAG, "API response status: ${body.status}, message: ${body.message}")
                        Log.d(TAG, "Personal docs count: ${body.personalDoc?.size ?: 0}")
                        
                        // Log all documents in response for debugging
                        body.personalDoc?.forEach { doc ->
                            Log.d(TAG, "Document in response: name=${doc.document_name}, type=${doc.documentType}, " +
                                  "has data: ${!doc.doc_data.isNullOrBlank()}")
                            if (!doc.doc_data.isNullOrBlank()) {
                                Log.d(TAG, "Doc data starts with: ${doc.doc_data?.take(30)}...")
                            }
                        }
                        
                        // Try finding by document name first
                        var matchingDoc = body.personalDoc?.find { it.document_name == document.document_name }
                        
                        // If not found by name, try by document type
                        if (matchingDoc == null && document.documentType.isNotBlank()) {
                            matchingDoc = body.personalDoc?.find { it.documentType == document.documentType }
                            Log.d(TAG, "Searching by document type: ${document.documentType}")
                        }
                        
                        Log.d(TAG, "Looking for document: ${document.document_name}, type: ${document.documentType}")
                        
                        if (matchingDoc != null) {
                            Log.d(TAG, "Found matching document: ${matchingDoc.document_name}, " +
                                  "type: ${matchingDoc.documentType}, has data: ${!matchingDoc.doc_data.isNullOrBlank()}")
                            
                            if (!matchingDoc.doc_data.isNullOrBlank()) {
                                // We have a valid URL, open the document
                                val filePath = matchingDoc.doc_data!!
                                Log.d(TAG, "Found document URL: $filePath")
                                
                                // Since all documents are PDFs, always use WebViewActivity
                                val intent = Intent(context, WebViewActivity::class.java)
                                
                                // Pass the necessary parameters
                                intent.putExtra("fileUrl", filePath)
                                intent.putExtra("title", matchingDoc.document_name ?: document.document_name)
                                
                                // Add PDF specific flags
                                intent.putExtra("isPdf", true)
                                
                                // Add isPersonal flag specific to UserDocuments
                                intent.putExtra("isPersonal", true)
                                
                                Log.d(TAG, "Starting WebViewActivity for PDF: $filePath with isPersonal=true")
                                context.startActivity(intent)
                            } else {
                                // Document found but no URL available
                                Toast.makeText(context, "Document found but no URL available for ${document.document_name}", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "Document found but no URL in doc_data for ${document.document_name}")
                            }
                        } else {
                            // No matching document found
                            Toast.makeText(context, "No matching document found for ${document.document_name}", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "No matching document found for ${document.document_name} in response")
                        }
                    } else {
                        // API call failed
                        Toast.makeText(context, "Failed to get document data", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "API call failed: ${response.code()} ${response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<DocumentListResponse>, t: Throwable) {
                    isLoading.postValue(false)
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Network error: ${t.message}")
                }
            })
        } catch (e: Exception) {
            isLoading.postValue(false)
            Log.e(TAG, "Error viewing document: ${e.message}")
            Toast.makeText(context, "Error viewing document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun uploadDocument(documentName: String, uri: Uri, onSuccess: (DocumentListResponse) -> Unit) {
        // DocumentUploadManager will handle determining document type from documentName
        
        // Use DocumentUploadManager to handle the upload
        documentUploadManager.uploadDocumentFromUri(documentName, uri) { response ->
            if (response.status == 200) {
                Toast.makeText(context, "$documentName uploaded successfully", Toast.LENGTH_SHORT).show()
                // Process the response and update the documents list
                processApiResponse(response)
                onSuccess(response)
            } else {
                val errorMsg = if (response.message.toString().isNotBlank()) response.message.toString() else "Upload failed"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}