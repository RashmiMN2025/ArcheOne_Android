package com.example.xone.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.xone.WebViewActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MyDocumentsController(private val context: Context) {

    // Stores the uploaded file paths (documentName -> filePath)
    private val uploadedFiles = mutableMapOf<String, String>()

    fun onUploadClick(documentName: String, fileUri: Uri, employeeId: String) {
        val file = getFileFromUri(context, fileUri)
        if (file == null) {
            Log.e("Upload", "File not found!")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create("application/pdf".toMediaTypeOrNull(), file))
            .addFormDataPart("employeeId", employeeId)
            .addFormDataPart("documentType", documentName)
            .build()

        val request = Request.Builder()
            .url("https://pulse.netcon.in:7000/upload")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Upload", "Failed to upload: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("Upload", "Upload failed: ${response.message}")
                        return
                    }

                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val filePath = jsonResponse.getJSONObject("fileDetails").getString("filePath")
                            uploadedFiles[documentName] = filePath // Store file path for viewing
                            Log.d("Upload", "File uploaded successfully! Path: $filePath")
                        } catch (e: Exception) {
                            Log.e("Upload", "Error parsing JSON response: ${e.message}")
                        }
                    } else {
                        Log.e("Upload", "Response body is null!")
                    }
                }
            }
        })
    }

    fun onViewClick(context: Context, filePath: String) {
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra("fileUrl", filePath) // Encode spaces
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
