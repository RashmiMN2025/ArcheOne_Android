package com.example.xone.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.xone.model.PolicyModel
import com.example.xone.navigation.Navigator
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PolicyController(
    private val context: Context,
    private val navigator: Navigator
) {
    val model = PolicyModel(policies = OtpVerificationController.getPoliciesData() ?: emptyList())

    fun onPolicyClick(policy: PolicyModel.Policy) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(policy.filePath), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun onDownloadClick(policy: PolicyModel.Policy) {
        val fileName = policy.filePath.substringAfterLast("/")
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (file.exists()) {
            Toast.makeText(context, "File already downloaded!", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(policy.filePath).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Download", "Download failed: ${e.message}")

                // Move Toast to main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // Move Toast to main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Download complete: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    fun onBackClick() {
        navigator.navigateToHome()
    }
}
