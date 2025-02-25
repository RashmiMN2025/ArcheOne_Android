package com.example.xone.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.xone.WebViewActivity
import com.example.xone.network.DocumentUploadResponse
import com.example.xone.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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

    fun onUploadClick(documentName: String, fileUri: Uri, employeeId: String) {
        val file = getFileFromUri(context, fileUri)
        if (file == null) {
            Toast.makeText(context, "File not found!", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert file to RequestBody
        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        // Convert other parameters to RequestBody
        val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
        val documentTypePart = documentName.toRequestBody("text/plain".toMediaTypeOrNull())

        val call = RetrofitClient.apiService.uploadDocument(filePart, employeeIdPart, documentTypePart)
        call.enqueue(object : Callback<DocumentUploadResponse> {
            override fun onResponse(call: Call<DocumentUploadResponse>, response: Response<DocumentUploadResponse>) {
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

            override fun onFailure(call: Call<DocumentUploadResponse>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Upload", "Network error: ${t.message}")
            }
        })
    }

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

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_upload.pdf")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("Upload", "Error processing file: ${e.message}")
            null
        }
    }
}
