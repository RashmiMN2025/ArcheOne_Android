package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.navigation.Navigator
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PolicyController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _policies = mutableStateOf<List<PolicyModel.Policy>>(emptyList())
    
    val model: PolicyModel
        get() = PolicyModel(policies = _policies.value)
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        loadPolicies()
    }
    
    private fun loadPolicies() {
        _isLoading.value = true
        
        coroutineScope.launch {
            try {
                // Simulate network delay (remove in production)
                delay(1000)
                
                // Get policies data
                val policiesData = OtpVerificationController.getPoliciesData() ?: emptyList()
                _policies.value = policiesData
                
                Log.d("PolicyController", "Loaded ${policiesData.size} policies")
            } catch (e: Exception) {
                Log.e("PolicyController", "Error loading policies: ${e.message}", e)
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onPolicyClick(policy: PolicyModel.Policy) {
        // Use PDFViewerScreen to open PDF via our navigator
        Log.d("PolicyController", "Opening policy PDF with PDFViewerScreen: ${policy.filePath}")
        navigator.navigateToPDFViewer(policy.filePath, policy.policyName)
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
    
    // Clean up resources when no longer needed
    fun onCleared() {
        coroutineScope.cancel()
    }
}
