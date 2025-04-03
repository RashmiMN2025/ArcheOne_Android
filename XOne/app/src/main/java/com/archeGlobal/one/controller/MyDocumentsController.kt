package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.ImageViewerActivity
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.network.DocumentUploadResponse
import com.archeGlobal.one.network.MyDocRequest
import com.archeGlobal.one.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@Suppress("IMPLICIT_CAST_TO_ANY")
class MyDocumentsController(private val context: Context) {

    val personalDocs = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    val professionalDocs = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    // Store uploaded documents for enabling/disabling buttons dynamically
    val uploadStatus = MutableLiveData<MutableMap<String, Boolean>>(mutableMapOf(
        "Aadhar Card" to false,
        "PAN Card" to false,
        "Passport" to false,
        "Driving License" to false,
        "Other" to false
    ))

    // Add a loading state
    val isLoading = MutableLiveData(false)

    // Handle file upload
    fun onUploadClick(documentName: String, fileUri: Uri, employeeId: String) {
        val file = getFileFromUri(context, fileUri)
        if (file == null) {
            Toast.makeText(context, "No document found for $documentName. Please upload document for the same.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check file size (5MB = 5 * 1024 * 1024 bytes)
        if (file.length() > 5 * 1024 * 1024) {
            Toast.makeText(context, "File size exceeds 5MB limit!", Toast.LENGTH_SHORT).show()
            return
        }

        uploadDocument(documentName, file, employeeId)
    }


    // Handle camera image upload
    fun onUploadCameraImage(documentName: String, bitmap: Bitmap, employeeId: String) {
        val file = saveBitmapToFile(bitmap)
        if (file == null) {
            Toast.makeText(context, "Failed to process image!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check file size (5MB = 5 * 1024 * 1024 bytes)
        if (file.length() > 5 * 1024 * 1024) {
            Toast.makeText(context, "Image size exceeds 5MB limit!", Toast.LENGTH_SHORT).show()
            return
        }

        uploadDocument(documentName, file, employeeId, false)
    }


    fun uploadDocument(documentName: String, file: File?, employeeId: String, isFetching: Boolean = false) {
        isLoading.postValue(true) // Show loader

        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file?.extension ?: "") ?: "application/octet-stream"

        val requestFile = file?.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = file?.let { MultipartBody.Part.createFormData("file", it.name, requestFile!!) }

        val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
        val documentTypePart = documentName.toRequestBody("text/plain".toMediaTypeOrNull())

        // Create the call variable
        val call: Call<DocumentUploadResponse> = if (isFetching) {
            val requestBody = MyDocRequest(employeeId, documentName)
            RetrofitClient.apiService.fetchDocuments(requestBody) // Ensure this returns Call<DocumentUploadResponse>
        } else {
            RetrofitClient.apiService.uploadDocument(filePart!!, employeeIdPart, documentTypePart) // Ensure this returns Call<DocumentUploadResponse>
        }

        call.enqueue(object : Callback<DocumentUploadResponse> {
            override fun onResponse(
                call: Call<DocumentUploadResponse>,
                response: Response<DocumentUploadResponse>
            ) {
                isLoading.postValue(false) // Hide loader

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val newPersonalDocs = personalDocs.value ?: mutableMapOf()
                        val newProfessionalDocs = professionalDocs.value ?: mutableMapOf()

                        // Update file paths from response
                        it.personalDoc.forEach { doc ->
                            if (!doc.filePath.isNullOrEmpty()) {
                                newPersonalDocs[doc.docName] = doc.filePath
                            }
                        }
                        it.professionalDoc.forEach { doc ->
                            if (!doc.filePath.isNullOrEmpty()) {
                                newProfessionalDocs[doc.docName] = doc.filePath
                            }
                        }

                        // Update LiveData values
                        personalDocs.postValue(newPersonalDocs)
                        professionalDocs.postValue(newProfessionalDocs)

                        if (!isFetching) {
                            // Mark document as uploaded
                            val newStatus = uploadStatus.value ?: mutableMapOf()
                            newStatus[documentName] = true
                            uploadStatus.postValue(newStatus)

                            Toast.makeText(context, "$documentName uploaded successfully!", Toast.LENGTH_SHORT).show()

                            // Fetch updated documents list after upload
                            uploadDocument(documentName, null, employeeId, isFetching = true)
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e("API Call", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DocumentUploadResponse>, t: Throwable) {
                isLoading.postValue(false) // Hide loader
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API Call", "Network error: ${t.message}")
            }
        })
    }

    // Handle viewing documents
    fun onViewClick(context: Context, documentName: String, isPersonal: Boolean) {
        val filePath = if (isPersonal) {
            personalDocs.value?.get(documentName)
        } else {
            professionalDocs.value?.get(documentName)
        }

        if (filePath.isNullOrEmpty()) {
            Toast.makeText(context, "No document found for $documentName. Please upload document for the same.", Toast.LENGTH_SHORT).show()
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

    // Convert Uri to File
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileExtension = contentResolver.getType(uri)?.let { mimeType ->
                android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            } ?: "tmp" // Default to "tmp" if the extension cannot be determined

            val fileName = "temp_upload.$fileExtension"
            val file = File(context.cacheDir, fileName)

            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("Upload", "Error converting Uri to File: ${e.message}")
            null
        }
    }

    // Save Bitmap to File
    private fun saveBitmapToFile(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): File? {
        return try {
            val extension = when (format) {
                Bitmap.CompressFormat.JPEG -> "jpg"
                Bitmap.CompressFormat.PNG -> "png"
                Bitmap.CompressFormat.WEBP -> "webp"
                else -> "jpg"
            }

            val fileName = "temp_image_upload.$extension"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(format, 100, outputStream)
            }
            file
        } catch (e: Exception) {
            Log.e("Upload", "Error saving bitmap to file: ${e.message}")
            null
        }
    }
}
