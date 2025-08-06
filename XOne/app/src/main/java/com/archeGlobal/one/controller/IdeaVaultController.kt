package com.archeGlobal.one.controller

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AboutMeModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.FeedbackRequest
import com.archeGlobal.one.network.FeedbackResponse
import com.archeGlobal.one.utils.EncryptedAPIHelper

class IdeaVaultController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val encryptedAPIHelper = EncryptedAPIHelper(context)

    var employeeData by mutableStateOf(
        AboutMeModel(
            name = OtpVerificationController.getUserData()?.name ?: "",
            email = OtpVerificationController.getUserData()?.email ?: ""
        )
    )

    var isSubmitting by mutableStateOf(false)

    fun submitFeedback(
        category: String?,
        feedback: String,
        callback: (String, Boolean) -> Unit
    ) {
        if (feedback.isBlank()) {
            callback("Please provide feedback before submitting.", true)
            return
        }

        isSubmitting = true

        val feedbackRequest = FeedbackRequest(
            name = employeeData.name,
            email = employeeData.email,
            category = if (category != "Select Category") category else null,
            feedback = feedback,
            rating = 0,
            platform = "Android",
            deviceName = Build.MODEL,
            version = Build.VERSION.RELEASE
        )

        Log.d("IdeaVaultController", "Submitting encrypted feedback request")

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "feedback",
            method = "POST",
            request = feedbackRequest,
            responseClass = FeedbackResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            isSubmitting = false

            if (error != null) {
                Log.e("IdeaVaultController", "Feedback submission failed: ${error.errorMessage}")
                callback("Failed to submit feedback: ${error.errorMessage}", true)
            } else {
                Log.d("IdeaVaultController", "Feedback submitted successfully!")
                callback("Feedback submitted successfully!", false)
            }
        }
    }

    fun onBackPressed() {
        navigator.navigateToHome()
    }
}
