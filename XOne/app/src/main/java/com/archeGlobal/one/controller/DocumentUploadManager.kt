package com.archeGlobal.one.controller

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.network.DocumentListResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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
    
    // Status indicators
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)
    val uploadSuccess = MutableLiveData<Boolean>(false)
    
    // Document type mapping for personal documents
    private val personalDocTypes = mapOf(
        "aadhar" to "Aadhar Card",
        "passport" to "Passport",
        "pan" to "PAN Card"
    )
    
    // Document type mapping for professional documents
    private val professionalDocTypes = mapOf(
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
        // Try personal document types first
        val personalEntry = personalDocTypes.entries.find { it.value == displayName }
        if (personalEntry != null) {
            Log.d("DocumentUploadManager", "Found personal doc type: ${personalEntry.key} for $displayName")
            return personalEntry.key
        }
        
        // Try professional document types
        val professionalEntry = professionalDocTypes.entries.find { it.value == displayName }
        if (professionalEntry != null) {
            Log.d("DocumentUploadManager", "Found professional doc type: ${professionalEntry.key} for $displayName")
            return professionalEntry.key
        }
        
        Log.e("DocumentUploadManager", "No document type found for: $displayName")
        return null
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
        
        try {
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Log request parameters
            Log.d("DocumentUploadManager", "Listing documents for email: $email, employeeId: $employeeId")
            
            val call = RetrofitClient.apiService.listDocuments(emailPart, employeeIdPart)
            Log.d("DocumentUploadManager", "List documents request URL: ${call.request().url}")
            
            call.enqueue(object : Callback<DocumentListResponse> {
                override fun onResponse(
                    call: Call<DocumentListResponse>,
                    response: Response<DocumentListResponse>
                ) {
                    isLoading.postValue(false)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        Log.d("DocumentUploadManager", "List documents success, status: ${responseBody.status}")
                        
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
            Log.e("DocumentUploadManager", "Exception preparing list documents request", e)
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
            errorMessage.postValue("Invalid document type: $documentName")
            return
        }
        
        // Convert URI to file
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
            
            // Get MIME type
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(file.extension) ?: "application/octet-stream"
            Log.d("DocumentUploadManager", "- MIME Type: $mimeType")
            
            // Create request parts
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val documentTypePart = documentType.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Make the API call
            val call = RetrofitClient.apiService.uploadDocument(
                filePart, emailPart, employeeIdPart, documentTypePart
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
    private fun handleApiError(response: Response<DocumentListResponse>) {
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
    private fun handleNetworkError(call: Call<DocumentListResponse>, t: Throwable) {
        isLoading.postValue(false)
        val errorMsg = "Network error: ${t.message}"
        errorMessage.postValue(errorMsg)
        Log.e("DocumentUploadManager", errorMsg, t)
        
        try {
            Log.e("DocumentUploadManager", "Failed request URL: ${call.request().url}")
            Log.e("DocumentUploadManager", "Failed request method: ${call.request().method}")
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Error logging request details", e)
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
            } ?: "tmp" // Default to "tmp" if extension can't be determined
            
            val fileName = "temp_upload.$fileExtension"
            val file = File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d("DocumentUploadManager", "Converted URI to file: ${file.absolutePath} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e("DocumentUploadManager", "Error converting URI to file", e)
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
