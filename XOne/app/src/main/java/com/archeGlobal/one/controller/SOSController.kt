package com.archeGlobal.one.controller

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.EncryptedSOSResponse
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SOSResponse
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.network.EncryptedAPIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SOSController(
    application: Application,
) : AndroidViewModel(application) {
    private val _sosBlogs = MutableStateFlow<List<SosBlogModel>>(emptyList())
    val sosBlogs: StateFlow<List<SosBlogModel>> get() = _sosBlogs

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> get() = _isSubmitting

    private val encryptedApiService = EncryptedAPIService.getInstance(application)

    init {
        fetchSOSBlogs()
    }

    private fun fetchSOSBlogs() {
        viewModelScope.launch {
            _sosBlogs.value = OtpVerificationController.getSosBlogsData() ?: emptyList()
        }
    }

    fun makeSOSCall() {
        val context = getApplication<Application>().applicationContext
        val userData = OtpVerificationController.getUserData()
        val sosNumber = userData?.sosContact

        if (sosNumber.isNullOrEmpty()) {
            Toast.makeText(context, "SOS contact number not available", Toast.LENGTH_SHORT).show()
            return
        }

        val callIntent =
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$sosNumber")
            }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(callIntent)
    }

    suspend fun submitEncryptedSOSRequest(request: SOSRequest): Result<SOSResponse> =
        try {
            _isSubmitting.value = true
            Log.d("SOSController", "Submitting encrypted SOS request: ${request.category}")

            val response =
                encryptedApiService.encryptedRequest(
                    endpoint = "sos",
                    method = "POST",
                    body = request,
                    responseClass = EncryptedSOSResponse::class.java,
                    withAuthHeader = true,
                )

            // Convert EncryptedSOSResponse to SOSResponse
            val sosResponse =
                SOSResponse(
                    status = response.status == 200,
                    message = response.message,
                )

            Log.d("SOSController", "SOS request submitted successfully")
            Result.success(sosResponse)
        } catch (e: APIError) {
            Log.e("SOSController", "API Error submitting SOS request: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("SOSController", "Exception submitting SOS request: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isSubmitting.value = false
        }

    suspend fun submitEncryptedHelpdeskRequest(request: SOSRequest): Result<SOSResponse> =
        try {
            _isSubmitting.value = true
            Log.d("SOSController", "Submitting encrypted helpdesk request:")
            Log.d("SOSController", "  Category: ${request.category}")
            Log.d("SOSController", "  Subcategory: ${request.subcategory}")
            Log.d("SOSController", "  Query: ${request.query}")
            Log.d("SOSController", "  Email: ${request.email}")

            val response =
                encryptedApiService.encryptedRequest(
                    endpoint = "helpdesk",
                    method = "POST",
                    body = request,
                    responseClass = EncryptedSOSResponse::class.java,
                    withAuthHeader = true,
                )

            // Convert EncryptedSOSResponse to SOSResponse
            val sosResponse =
                SOSResponse(
                    status = response.status == 200,
                    message = response.message,
                )

            Log.d("SOSController", "Helpdesk request submitted successfully")
            Result.success(sosResponse)
        } catch (e: APIError) {
            Log.e("SOSController", "API Error submitting helpdesk request: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("SOSController", "Exception submitting helpdesk request: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isSubmitting.value = false
        }
}
