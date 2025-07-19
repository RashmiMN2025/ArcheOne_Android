package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.NoDocumentFoundActivity
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.DocumentListResponse
import com.archeGlobal.one.network.ProfilePictureResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.network.UserDocument
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
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

    // LiveData for upload status, mirroring MyDocumentsController
    private val _uploadStatus = MutableLiveData<MutableMap<String, Boolean>>(
        mutableMapOf(
            "PAN Card" to false,
            "ID Card" to false,
            "Medical Insurance Card" to false
        )
    )
    val uploadStatus: LiveData<MutableMap<String, Boolean>> = _uploadStatus

    // Expose DocumentUploadManager states
    val isLoading = documentUploadManager.isLoading
    val errorMessage = documentUploadManager.errorMessage
    val uploadSuccess = documentUploadManager.uploadSuccess

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        loadUserDocuments()
        fetchDocumentsFromApi()
    }

    fun refreshDocuments() {
        Log.d(TAG, "Refreshing documents data")
        fetchDocumentsFromApi()
    }

    private fun loadUserDocuments() {
        val userData = userDataManager.getUserData()
        val documents = userData?.userDetails?.documents ?: emptyList()
        _userDocuments.value = documents
        updateUploadStatus(documents)
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
        val personalDocs = response.personalDoc?.map { doc ->
            val docName = doc.document_name ?: when (doc.documentType) {
                "id" -> "ID Card"
                "pan" -> "PAN Card"
                "medical" -> "Medical Insurance Card"
                else -> "Unknown Document"
            }
            UserDocument(
                document_name = docName,
                doc_data = doc.doc_data ?: "",
                documentType = doc.documentType ?: ""
            )
        } ?: emptyList()
        personalDocs.forEach { doc ->
            Log.d(TAG, "Processed document: ${doc.document_name}, data: ${if (doc.doc_data.isBlank()) "empty" else "has data"}, type: ${doc.documentType}")
        }
        _userDocuments.postValue(personalDocs)
        updateUploadStatus(personalDocs)
        Log.d(TAG, "Updated documents from API: ${personalDocs.size} documents")
    }

    private fun updateUploadStatus(documents: List<UserDocument>) {
        val newUploadStatus = mutableMapOf(
            "PAN Card" to false,
            "ID Card" to false,
            "Medical Insurance Card" to false
        )
        documents.forEach { doc ->
            val docName = doc.document_name
            if (docName in newUploadStatus && !doc.doc_data.isNullOrBlank()) {
                newUploadStatus[docName] = true
            }
        }
        _uploadStatus.postValue(newUploadStatus)
        Log.d(TAG, "Updated uploadStatus: $newUploadStatus")
    }

    private fun isValidPdfUrl(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.contains("text/html", ignoreCase = true)) {
            return false
        }
        if (url.contains("error", ignoreCase = true) ||
            url.contains("not found", ignoreCase = true) ||
            url.contains("404", ignoreCase = true) ||
            url.contains("file not exist", ignoreCase = true)
        ) {
            return false
        }
        if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
            if (url.contains("download", ignoreCase = true) || url.endsWith(".pdf", ignoreCase = true)) {
                return true
            }
            if (url.contains("dev.arche.global", ignoreCase = true) && url.contains("download_doc", ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun checkDocumentUrl(url: String, onResult: (isPdf: Boolean, htmlContent: String?) -> Unit) {
        ioScope.launch {
            try {
                val client = OkHttpClient.Builder().followRedirects(true).build()
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    val contentType = response.header("Content-Type") ?: ""
                    if (contentType.contains("application/pdf", true)) {
                        withContext(Dispatchers.Main) {
                            onResult(true, null)
                        }
                    } else {
                        val bodyString = response.body?.string() ?: ""
                        withContext(Dispatchers.Main) {
                            onResult(false, bodyString.take(5000))
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, null)
                }
            }
        }
    }

    fun viewDocument(document: UserDocument) {
        try {
            Log.d(TAG, "Requesting document for viewing: ${document.document_name}, type: ${document.documentType}")
            isLoading.postValue(true)
            val email = userDataManager.getUserData()?.email ?: ""
            val employeeId = userDataManager.getUserData()?.employeeId ?: ""
            if (email.isEmpty() || employeeId.isEmpty()) {
                Toast.makeText(context, "User information not available", Toast.LENGTH_SHORT).show()
                isLoading.postValue(false)
                return
            }
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val isPersonalPart = "true".toRequestBody("text/plain".toMediaTypeOrNull())
            RetrofitClient.apiService.listDocuments(emailPart, employeeIdPart, isPersonalPart).enqueue(object : Callback<DocumentListResponse> {
                override fun onResponse(
                    call: Call<DocumentListResponse>,
                    response: Response<DocumentListResponse>
                ) {
                    isLoading.postValue(false)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        Log.d(TAG, "API response status: ${body.status}, message: ${body.message}")
                        Log.d(TAG, "Personal docs count: ${body.personalDoc?.size ?: 0}")
                        body.personalDoc?.forEach { doc ->
                            Log.d(
                                TAG,
                                "Document in response: name=${doc.document_name}, type=${doc.documentType}, " +
                                    "has data: ${!doc.doc_data.isNullOrBlank()}"
                            )
                            if (!doc.doc_data.isNullOrBlank()) {
                                Log.d(TAG, "Doc data starts with: ${doc.doc_data?.take(30)}...")
                            }
                        }
                        var matchingDoc = body.personalDoc?.find { it.document_name == document.document_name }
                        if (matchingDoc == null && document.documentType.isNotBlank()) {
                            matchingDoc = body.personalDoc?.find { it.documentType == document.documentType }
                            Log.d(TAG, "Searching by document type: ${document.documentType}")
                        }
                        Log.d(TAG, "Looking for document: ${document.document_name}, type: ${document.documentType}")
                        if (matchingDoc != null) {
                            Log.d(
                                TAG,
                                "Found matching document: ${matchingDoc.document_name}, " +
                                    "type: ${matchingDoc.documentType}, has data: ${!matchingDoc.doc_data.isNullOrBlank()}"
                            )
                            if (!matchingDoc.doc_data.isNullOrBlank()) {
                                val filePath = matchingDoc.doc_data!!
                                Log.d(TAG, "Found document URL: $filePath - performing content check")
                                checkDocumentUrl(filePath) { isPdf, htmlContent ->
                                    if (isPdf) {
                                        val intent = Intent(context, WebViewActivity::class.java).apply {
                                            putExtra("fileUrl", filePath)
                                            putExtra("title", matchingDoc.document_name ?: document.document_name)
                                            putExtra("isPdf", true)
                                            putExtra("isPersonal", true)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        val displayHtml = """
                                            <html>
                                            <body>
                                            <h2>No document found for ${matchingDoc.document_name}.</h2>
                                            <p>Please upload the document.</p>
                                            </body>
                                            </html>
                                        """.trimIndent()
                                        val intent = Intent(context, WebViewActivity::class.java).apply {
                                            putExtra("rawHtmlContent", displayHtml)
                                            putExtra("title", matchingDoc.document_name ?: document.document_name)
                                            putExtra("isPdf", false)
                                            putExtra("isPersonal", true)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            } else {
                                val intent = Intent(context, NoDocumentFoundActivity::class.java)
                                intent.putExtra("documentName", document.document_name)
                                context.startActivity(intent)
                                Log.d(TAG, "Document found but no URL in doc_data for ${document.document_name}")
                            }
                        } else {
                            val intent = Intent(context, NoDocumentFoundActivity::class.java)
                            intent.putExtra("documentName", document.document_name)
                            context.startActivity(intent)
                            Log.d(TAG, "No matching document found for ${document.document_name} in response")
                        }
                    } else {
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
        documentUploadManager.uploadDocumentFromUri(documentName, uri) { response ->
            if (response.status == 200) {
                Toast.makeText(context, "$documentName uploaded successfully", Toast.LENGTH_SHORT).show()
                processApiResponse(response)
                onSuccess(response)
                // Explicitly refresh documents to ensure UI updates
                fetchDocumentsFromApi()
            } else {
                val errorMsg = if (response.message.toString().isNotBlank()) response.message.toString() else "Upload failed"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                errorMessage.postValue(errorMsg)
            }
        }
    }

    fun deleteDocument(document: UserDocument, onComplete: (Boolean) -> Unit) {
        try {
            Log.d(TAG, "Requesting deletion of document: ${document.document_name}, type: ${document.documentType}")
            isLoading.postValue(true)
            val email = userDataManager.getUserData()?.email ?: ""
            val employeeId = userDataManager.getUserData()?.employeeId ?: ""
            // Map document_name to documentType if documentType is empty
            val documentType = when {
                document.documentType.isNotBlank() -> document.documentType
                document.document_name == "PAN Card" -> "pan"
                document.document_name == "ID Card" -> "id"
                document.document_name == "Medical Insurance Card" -> "medical"
                else -> {
                    isLoading.postValue(false)
                    Toast.makeText(context, "Unknown document type for ${document.document_name}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Unknown document type for ${document.document_name}")
                    onComplete(false)
                    return
                }
            }
            if (email.isEmpty() || employeeId.isEmpty()) {
                isLoading.postValue(false)
                Toast.makeText(context, "User information not available", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Missing required data: email=$email, employeeId=$employeeId")
                onComplete(false)
                return
            }
            val params = mapOf(
                "email" to email,
                "employeeId" to employeeId,
                "documentType" to documentType
            )
            RetrofitClient.apiService.deleteDoc(params).enqueue(object : Callback<ProfilePictureResponse> {
                override fun onResponse(
                    call: Call<ProfilePictureResponse>,
                    response: Response<ProfilePictureResponse>
                ) {
                    isLoading.postValue(false)
                    if (response.isSuccessful && response.body()?.status == 200) {
                        Log.d(TAG, "Document deleted successfully: ${document.document_name}")
                        fetchDocumentsFromApi() // Refresh document list
                        onComplete(true)
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to delete document"
                        Log.e(TAG, "Delete API failed: ${response.code()} $errorMsg")
                        errorMessage.postValue(errorMsg)
                        onComplete(false)
                    }
                }

                override fun onFailure(call: Call<ProfilePictureResponse>, t: Throwable) {
                    isLoading.postValue(false)
                    val errorMsg = "Network error: ${t.message}"
                    Log.e(TAG, errorMsg)
                    errorMessage.postValue(errorMsg)
                    onComplete(false)
                }
            })
        } catch (e: Exception) {
            isLoading.postValue(false)
            Log.e(TAG, "Error deleting document: ${e.message}")
            errorMessage.postValue("Error deleting document: ${e.message}")
            onComplete(false)
        }
    }
}
