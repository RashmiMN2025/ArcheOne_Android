package com.archeGlobal.one.controller

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.network.DocumentListResponse
import com.archeGlobal.one.network.ProfilePictureResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

/**
 * This class handles document upload operations with the new API
 */
class DocumentUploadManager(private val context: Context) {
    // Companion object for static members
    companion object {
        private const val TAG = "DocumentUploadManager"
    }

    // Add UserDataManager reference
    private val userDataManager = UserDataManager.getInstance(context)

    // Status indicators
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)
    val uploadSuccess = MutableLiveData<Boolean>(false)

    // Document type mapping for personal documents
    private val personalDocTypes = mapOf(
        "id" to "ID Card",
        "pan" to "PAN Card",
        "medical" to "Medical Insurance Card"
    )

    // Document type mapping for professional documents
    private val professionalDocTypes = mapOf(
        "aadhar" to "Aadhar Card",
        "passport" to "Passport",
        "pan" to "PAN card",
        "offer_letter" to "Offer Letter",
        "certificate" to "Certificate",
        "exp_letter" to "Experience Letter"
    )

    /**
     * Get the current user's employee ID
     */
    private fun getEmployeeId(): String? {
        val employeeId = OtpVerificationController.getUserData()?.employeeId
        if (employeeId.isNullOrEmpty()) {
            Log.e("DocumentUploadManager", "Employee ID not found in user data")
            return null
        }

        // Validate the format: must be NT followed by 4 digits
        if (!employeeId.matches(Regex("^NT\\d{4}$"))) {
            Log.e("DocumentUploadManager", "Employee ID has invalid format: $employeeId. Must be NT followed by 4 digits.")
            errorMessage.postValue("Invalid employee ID format. It must start with 'NT' followed by exactly 4 digits (e.g., NT1234)")
            return null
        }

        Log.d("DocumentUploadManager", "Retrieved employee ID from user data: $employeeId")
        return employeeId
    }

    /**
     * Get the current user's email
     */
    private fun getUserEmail(): String? {
        val email = OtpVerificationController.getUserData()?.email
        if (email.isNullOrEmpty()) {
            Log.e("DocumentUploadManager", "Email not found in user data")
            errorMessage.postValue("Email not found. Please log in again.")
            return null
        }
        Log.d("DocumentUploadManager", "Retrieved email from user data: $email")
        return email
    }

    /**
     * Get document type code from display name
     */
    fun getDocTypeCode(displayName: String): String? {
        // Direct mapping for the three required document types
        return when (displayName) {
            "ID Card" -> "id"
            "PAN Card" -> "pan"
            "Medical Insurance" -> "medical"
            else -> {
                // Try personal document types as fallback
                val personalEntry = personalDocTypes.entries.find { it.value == displayName }
                if (personalEntry != null) {
                    Log.d("DocumentUploadManager", "Found personal doc type: ${personalEntry.key} for $displayName")
                    return personalEntry.key
                }

                // Try professional document types as fallback
                val professionalEntry = professionalDocTypes.entries.find { it.value == displayName }
                if (professionalEntry != null) {
                    Log.d("DocumentUploadManager", "Found professional doc type: ${professionalEntry.key} for $displayName")
                    return professionalEntry.key
                }

                Log.e("DocumentUploadManager", "No document type found for: $displayName")
                null
            }
        }
    }

    /**
     * List documents for a user
     */
    fun listDocuments(onSuccess: (DocumentListResponse) -> Unit) {
        // Get employee ID from user data
        val employeeId = getEmployeeId()
        if (employeeId == null) {
            return // Error message already set in getEmployeeId()
        }

        // Get email from user data
        val email = getUserEmail()
        if (email == null) {
            return // Error message already set in getUserEmail()
        }

        isLoading.postValue(true)
        errorMessage.postValue(null)

        try {
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())

            // Log request parameters
            Log.d(TAG, "Listing documents for email: $email, employeeId: $employeeId")

            val call = RetrofitClient.apiService.listDocuments(emailPart, employeeIdPart)
            Log.d(TAG, "List documents request URL: ${call.request().url}")

            call.enqueue(object : Callback<DocumentListResponse> {
                override fun onResponse(
                    call: Call<DocumentListResponse>,
                    response: Response<DocumentListResponse>
                ) {
                    isLoading.postValue(false)

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        Log.d(TAG, "List documents success, status: ${responseBody.status}")

                        // Log all documents returned
                        responseBody.personalDoc?.forEach { doc ->
                            Log.d(
                                TAG,
                                "Document: ${doc.document_name}, type: ${doc.documentType}, " +
                                        "has data: ${!doc.doc_data.isNullOrBlank()}"
                            )
                        }

                        if (responseBody.status == 200) {
                            onSuccess(responseBody)
                        } else {
                            handleErrorResponse(responseBody)
                        }
                    } else {
                        handleApiError(response)
                    }
                }

                override fun onFailure(call: Call<DocumentListResponse>, t: Throwable) {
                    handleNetworkError(call, t)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception preparing list documents request", e)
            isLoading.postValue(false)
            errorMessage.postValue("Error: ${e.message}")
        }
    }

    /**
     * Delete a document
     */
    fun deleteDocument(documentName: String, documentType: String, onSuccess: (DocumentListResponse) -> Unit) {
        // Get employee ID from user data
        val employeeId = getEmployeeId()
        if (employeeId == null) {
            return // Error message already set in getEmployeeId()
        }

        // Get email from user data
        val email = getUserEmail()
        if (email == null) {
            return // Error message already set in getUserEmail()
        }

        isLoading.postValue(true)
        errorMessage.postValue(null)

        try {
            val params = mapOf(
                "email" to email,
                "employeeId" to employeeId,
                "documentType" to documentType
            )

            Log.d(TAG, "Deleting document: $documentName, type: $documentType, email: $email, employeeId: $employeeId")

            val call = RetrofitClient.apiService.deleteDoc(params)
            Log.d(TAG, "Delete document request URL: ${call.request().url}")

            call.enqueue(object : Callback<ProfilePictureResponse> {
                override fun onResponse(
                    call: Call<ProfilePictureResponse>,
                    response: Response<ProfilePictureResponse>
                ) {
                    isLoading.postValue(false)

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        Log.d(TAG, "Delete document success, status: ${responseBody.status}")

                        if (responseBody.status == 200) {
                            Toast.makeText(context, "Document deleted successfully", Toast.LENGTH_SHORT).show()
                            // Refresh document list
                            listDocuments { listResponse ->
                                onSuccess(listResponse)
                            }
                        } else {
                            val errorMsg = responseBody.message ?: "Failed to delete document"
                            errorMessage.postValue(errorMsg)
                            Log.e(TAG, "Delete document error: $errorMsg")
                        }
                    } else {
                        handleApiError(response)
                    }
                }

                override fun onFailure(call: Call<ProfilePictureResponse>, t: Throwable) {
                    handleNetworkError(call, t)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception preparing delete document request", e)
            isLoading.postValue(false)
            errorMessage.postValue("Error: ${e.message}")
        }
    }

    /**
     * Upload a document from a URI
     */
    fun uploadDocumentFromUri(
        documentName: String,
        uri: Uri,
        onSuccess: (DocumentListResponse) -> Unit
    ) {
        // Get employee ID from user data
        val employeeId = getEmployeeId()
        if (employeeId == null) {
            return // Error message already set in getEmployeeId()
        }

        // Get email from user data
        val email = getUserEmail()
        if (email == null) {
            return // Error message already set in getUserEmail()
        }

        // Get document type code
        val documentType = getDocTypeCode(documentName)
        if (documentType == null) {
            errorMessage.postValue("Unknown document type: $documentName")
            return
        }

        // Get the MIME type of the file
        val mimeType = context.contentResolver.getType(uri)

        // Check if the file is a PDF
        if (mimeType != "application/pdf") {
            // Try to check the file extension as a fallback
            val fileName = getFileNameFromUri(uri)
            if (fileName == null || !fileName.lowercase().endsWith(".pdf")) {
                errorMessage.postValue("Only PDF files are allowed. Please select a PDF document.")
                return
            }
        }

        val file = getFileFromUri(uri)
        if (file == null) {
            errorMessage.postValue("Failed to process file. Please try again.")
            return
        }

        // Check file size (5MB = 5 * 1024 * 1024 bytes)
        if (file.length() > 5 * 1024 * 1024) {
            errorMessage.postValue("File too large. Max allowed size is 5MB.")
            return
        }

        // Upload the file
        uploadDocument(documentType, file, email, employeeId, onSuccess)
    }

    /**
     * Upload a document from a bitmap (e.g., from camera)
     */
    fun uploadDocumentFromBitmap(
        documentName: String,
        bitmap: Bitmap,
        onSuccess: (DocumentListResponse) -> Unit
    ) {
        // Get employee ID from user data
        val employeeId = getEmployeeId()
        if (employeeId == null) {
            return // Error message already set in getEmployeeId()
        }

        // Get email from user data
        val email = getUserEmail()
        if (email == null) {
            return // Error message already set in getUserEmail()
        }

        // Get document type code
        val documentType = getDocTypeCode(documentName)
        if (documentType == null) {
            errorMessage.postValue("Invalid document type: $documentName")
            return
        }

        // Convert bitmap to file
        val file = saveBitmapToFile(bitmap)
        if (file == null) {
            errorMessage.postValue("Failed to process image. Please try again.")
            return
        }

        // Check file size (5MB = 5 * 1024 * 1024 bytes)
        if (file.length() > 5 * 1024 * 1024) {
            errorMessage.postValue("File too large. Max allowed size is 5MB.")
            return
        }

        // Upload the file
        uploadDocument(documentType, file, email, employeeId, onSuccess)
    }

    /**
     * Core upload method
     */
    private fun uploadDocument(
        documentType: String,
        file: File,
        email: String,
        employeeId: String,
        onSuccess: (DocumentListResponse) -> Unit
    ) {
        isLoading.postValue(true)
        errorMessage.postValue(null)
        uploadSuccess.postValue(false)

        try {
            // Log all parameters
            Log.d("DocumentUploadManager", "Uploading document with parameters:")
            Log.d("DocumentUploadManager", "- Document Type: $documentType")
            Log.d("DocumentUploadManager", "- Email: $email")
            Log.d("DocumentUploadManager", "- Employee ID: $employeeId")
            Log.d("DocumentUploadManager", "- File: ${file.name} (${file.length()} bytes)")

            // Create a map for request parameters
            val params = HashMap<String, RequestBody>()
            params["email"] = email.toRequestBody("text/plain".toMediaTypeOrNull())
            params["employeeId"] = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
            params["documentType"] = documentType.toRequestBody("text/plain".toMediaTypeOrNull())

            // Create MultipartBody.Part from file
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Make the API call with parameters
            val call = RetrofitClient.apiService.uploadDocument(
                filePart,
                params
            )

            // Log request details
            Log.d("DocumentUploadManager", "Upload request URL: ${call.request().url}")
            Log.d("DocumentUploadManager", "Upload request method: ${call.request().method}")

            // Execute the request
            call.enqueue(object : Callback<DocumentListResponse> {
                override fun onResponse(
                    call: Call<DocumentListResponse>,
                    response: Response<DocumentListResponse>
                ) {
                    isLoading.postValue(false)

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        Log.d("DocumentUploadManager", "Upload success, status: ${responseBody.status}")

                        if (responseBody.status == 200) {
                            // Show success message
                            Toast.makeText(context, "Document uploaded successfully!", Toast.LENGTH_SHORT).show()
                            uploadSuccess.postValue(true)
                            onSuccess(responseBody)
                        } else {
                            handleErrorResponse(responseBody)
                        }
                    } else {
                        handleApiError(response)
                    }
                }

                override fun onFailure(call: Call<DocumentListResponse>, t: Throwable) {
                    handleNetworkError(call, t)
                }
            })
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Exception preparing upload request", e)
            isLoading.postValue(false)
            errorMessage.postValue("Error preparing upload: ${e.message}")
        }
    }

    /**
     * Handle error response from API
     */
    private fun handleErrorResponse(responseBody: DocumentListResponse) {
        when (responseBody.status) {
            400 -> {
                if (responseBody.message is String) {
                    val message = responseBody.message as String
                    errorMessage.postValue(message)
                    Log.e("DocumentUploadManager", "API error 400: $message")
                } else {
                    errorMessage.postValue("Invalid request")
                    Log.e("DocumentUploadManager", "API error 400 with non-string message")
                }
            }
            413 -> {
                errorMessage.postValue("File too large. Max allowed size is 5MB.")
                Log.e("DocumentUploadManager", "API error 413: File too large")
            }
            500 -> {
                errorMessage.postValue("Internal server error")
                Log.e("DocumentUploadManager", "API error 500: Internal server error")
            }
            else -> {
                errorMessage.postValue("Server returned error code: ${responseBody.status}")
                Log.e("DocumentUploadManager", "API error ${responseBody.status}")
            }
        }
    }

    /**
     * Handle API error response
     */
    private fun handleApiError(response: Response<*>) {
        try {
            val errorBody = response.errorBody()?.string()
            Log.e("DocumentUploadManager", "API error ${response.code()}: $errorBody")
            errorMessage.postValue("Server error: ${response.code()}")
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Error parsing error response", e)
            errorMessage.postValue("Error: ${response.message()}")
        }
    }

    /**
     * Handle network error
     */
    private fun handleNetworkError(call: Call<*>, t: Throwable) {
        isLoading.postValue(false)
        val errorMsg = "Network error: ${t.message}"
        errorMessage.postValue(errorMsg)
        Log.e(TAG, errorMsg, t)

        try {
            Log.e(TAG, "Failed request URL: ${call.request().url}")
            Log.e(TAG, "Failed request method: ${call.request().method}")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging request details", e)
        }
    }

    /**
     * Get filename from URI
     */
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = it.getString(displayNameIndex)
                    }
                }
            }

            // If we couldn't get the filename from the cursor, try to get it from the URI path
            if (fileName == null) {
                fileName = uri.path?.let { path ->
                    path.substring(path.lastIndexOf('/') + 1)
                }
            }

            Log.d("DocumentUploadManager", "File name from URI: $fileName")
            return fileName
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Error getting filename from URI: ${e.message}")
            return null
        }
    }

    /**
     * Convert URI to File
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileExtension = contentResolver.getType(uri)?.let { mimeType ->
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            } ?: "pdf" // Default to PDF if the extension cannot be determined

            val fileName = getFileNameFromUri(uri) ?: "temp_upload.$fileExtension"
            val file = File(context.cacheDir, fileName)

            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("DocumentUploadManager", "Converted URI to file: ${file.absolutePath} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Error converting Uri to File: ${e.message}")
            null
        }
    }

    /**
     * Save Bitmap to File
     */
    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val fileName = "temp_upload.jpg"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            Log.d("DocumentUploadManager", "Saved bitmap to file: ${file.absolutePath} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Error saving bitmap to file", e)
            null
        }
    }
}