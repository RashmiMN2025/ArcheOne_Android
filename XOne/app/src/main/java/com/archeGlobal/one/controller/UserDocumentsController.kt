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

    // Expose DocumentUploadManager states
    val isLoading = documentUploadManager.isLoading
    val errorMessage = documentUploadManager.errorMessage
    val uploadSuccess = documentUploadManager.uploadSuccess

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    /**
     * Check if the URL points to a valid PDF or an HTML error page
     */
    private fun isValidPdfUrl(url: String): Boolean {
        // Check if URL is empty or blank
        if (url.isBlank()) return false

        // Check if URL contains HTML content indicators (in case HTML is returned directly)
        if (url.contains("<html>", ignoreCase = true) || url.contains("<!DOCTYPE", ignoreCase = true) ||
            url.contains("<body>", ignoreCase = true) ||
            url.contains("text/html", ignoreCase = true) ||
            url.contains("<title>", ignoreCase = true) ||
            url.contains("</html>", ignoreCase = true)
        ) {
            return false
        }

        // Check for common error messages in the URL content
        if (url.contains("error", ignoreCase = true) ||
            url.contains("not found", ignoreCase = true) ||
            url.contains("404", ignoreCase = true) ||
            url.contains("file not exist", ignoreCase = true)
        ) {
            return false
        }

        // Check if it's a valid HTTP/HTTPS URL
        if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
            // Additional check: if it's a download URL, it should contain certain patterns
            if (url.contains("download", ignoreCase = true) || url.endsWith(".pdf", ignoreCase = true)) {
                return true
            }
            // For pulse.netcon.in URLs specifically, check if it follows the expected pattern
            if (url.contains("pulse.netcon.in", ignoreCase = true) && url.contains("download_doc", ignoreCase = true)) {
                return true
            }
        }

        // If it doesn't match any valid patterns, consider it invalid
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

            // Make the document listing API call to get latest URLs - isPersonal is required for UserDocuments
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
                            Log.d(
                                TAG,
                                "Document in response: name=${doc.document_name}, type=${doc.documentType}, " +
                                    "has data: ${!doc.doc_data.isNullOrBlank()}"
                            )
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
                                            <!DOCTYPE html>
                                            <html>
                                            <head>
                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                <style>
                                                    body {
                                                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                                        margin: 0;
                                                        padding: 0;
                                                        background: linear-gradient(to bottom, #E0DCD1, #C8C8CA, #474749);
                                                        min-height: 100vh;
                                                        display: flex;
                                                        align-items: center;
                                                        justify-content: center;
                                                        padding-top: 200px;
                                                    }
                                                    .container {
                                                        text-align: center;
                                                        max-width: 400px;
                                                        width: 100%;
                                                        padding: 20px;
                                                    }
                                                    .message {
                                                        color: #333;
                                                        font-size: 14px;
                                                        font-weight: bold;
                                                        line-height: 1.4;
                                                        margin: 0;
                                                    }
                                                </style>
                                            </head>
                                            <body>
                                                <div class="container">
                                                    <div class="message">
                                                        No document found for ${matchingDoc.document_name}.<br>
                                                        Please upload the document.
                                                    </div>
                                                </div>
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
                                // Document found but no URL available - show NoDocumentFoundActivity
                                val intent = Intent(context, NoDocumentFoundActivity::class.java)
                                intent.putExtra("documentName", document.document_name)
                                context.startActivity(intent)
                                Log.d(TAG, "Document found but no URL in doc_data for ${document.document_name}")
                            }
                        } else {
                            // No matching document found - show NoDocumentFoundActivity
                            val intent = Intent(context, NoDocumentFoundActivity::class.java)
                            intent.putExtra("documentName", document.document_name)
                            context.startActivity(intent)
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
