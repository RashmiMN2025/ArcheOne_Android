package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.network.DocumentUploadResponse
import com.archeGlobal.one.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

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
            Toast.makeText(context, "File not found!", Toast.LENGTH_SHORT).show()
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
        uploadDocument(documentName, file, employeeId)
    }

    // Upload document to the server
    private fun uploadDocument(documentName: String, file: File, employeeId: String) {
        isLoading.postValue(true) // Show loader

        val mimeType = android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension) ?: "application/octet-stream"

        // Convert file to RequestBody
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        // Convert other parameters to RequestBody
        val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
        val documentTypePart = documentName.toRequestBody("text/plain".toMediaTypeOrNull())

        val call = RetrofitClient.apiService.uploadDocument(filePart, employeeIdPart, documentTypePart)
        call.enqueue(object : retrofit2.Callback<DocumentUploadResponse> {
            override fun onResponse(call: retrofit2.Call<DocumentUploadResponse>, response: retrofit2.Response<DocumentUploadResponse>) {
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

                        // Mark document as uploaded
                        val newStatus = uploadStatus.value ?: mutableMapOf()
                        newStatus[documentName] = true
                        uploadStatus.postValue(newStatus)

                        Toast.makeText(context, "$documentName uploaded successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e("Upload", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<DocumentUploadResponse>, t: Throwable) {
                isLoading.postValue(false) // Hide loader
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Upload", "Network error: ${t.message}")
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
            Toast.makeText(context, "No file uploaded for $documentName", Toast.LENGTH_SHORT).show()
            return
        }

        // Open WebViewActivity to display PDF
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra("fileUrl", filePath)
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
