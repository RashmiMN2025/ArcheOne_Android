package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.BusinessCardActivity
import com.archeGlobal.one.PolicyActivity
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.navigation.Navigator
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.archeGlobal.one.network.ApiService
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.controller.OtpVerificationController

class PolicyController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    // Use State for model so Compose will recompose when policies change
    private val _model = mutableStateOf(PolicyModel())
    val model: State<PolicyModel> = _model
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        loadPolicies()
    }
    
    private fun loadPolicies() {
        _isLoading.value = true
        
        coroutineScope.launch {
            try {
                // First try to get from API
                val response = RetrofitClient.apiService.getPolicies()
                if (response.isSuccessful && response.body() != null) {
                    val policyResponses = response.body()!!
                    val transformedPolicies = policyResponses.map { policyResponse ->
                        PolicyModel.Policy(
                            policyName = policyResponse.name,
                            filePath = policyResponse.pdfUrl,
                            previewUrl = policyResponse.previewUrl
                        )
                    }
                    _model.value = PolicyModel(policies = transformedPolicies)
                    Log.d("PolicyController", "Loaded ${transformedPolicies.size} policies from API")
                } else {
                    // Fallback to cached data if API fails
                    val policiesData = OtpVerificationController.getPoliciesData()
                    if (policiesData != null) {
                        _model.value = PolicyModel(policies = policiesData)
                        Log.d("PolicyController", "Loaded ${policiesData.size} policies from cache")
                    } else {
                        Log.e("PolicyController", "No policies data available")
                        Toast.makeText(context, "Failed to load policies", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Try to load from cache if API call fails
                try {
                    val policiesData = com.archeGlobal.one.controller.OtpVerificationController.getPoliciesData()
                    if (policiesData != null) {
                        _model.value = PolicyModel(policies = policiesData)
                        Log.d("PolicyController", "Loaded ${policiesData.size} policies from cache after API error")
                    } else {
                        throw e // Re-throw if no cache available
                    }
                } catch (e2: Exception) {
                    Log.e("PolicyController", "Error loading policies: ${e2.message}", e2)
                    Toast.makeText(context, "Failed to load policies", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onPolicyClick(policy: PolicyModel.Policy) {
        // Use WebViewActivity for viewing PDFs with PDF.js
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra("fileUrl", policy.filePath)
            putExtra("title", policy.policyName)
            putExtra("isPdf", true)
            putExtra("showSosButton", policy.showSosButton)
            // Add flag to use PDF.js viewer
            putExtra("usePdfJs", true)
            putExtra("isFloorMap", true) // This will use the PDF.js viewer implementation
        }
        context.startActivity(intent)
    }

    fun onBackClick() {
        (context as? PolicyActivity)?.finishWithAnimation()
    }
    
    // Clean up resources when no longer needed
    fun onCleared() {
        coroutineScope.cancel()
    }
}
