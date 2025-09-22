package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.PasswordResetRequest
import com.archeGlobal.one.model.PasswordResetResponse
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.EncryptedAPIHelper

class PasswordResetController(
    private val navigator: Navigator,
    private val context: Context,
) {
    private val encryptedAPIHelper = EncryptedAPIHelper(context)

    fun resetPassword(
        email: String,
        employeeId: String,
        callback: (response: PasswordResetResponse?, errorMessage: String?) -> Unit,
    ) {
        val request = PasswordResetRequest(email, employeeId)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "reset-password",
            method = "POST",
            request = request,
            responseClass = PasswordResetResponse::class.java,
            withAuthHeader = false, // Password reset doesn't require authentication
            handleTokenExpiration = false, // No token expiration handling needed for password reset
        ) { response, error ->
            if (error != null) {
                val errorMessage =
                    when (error) {
                        is APIError.BadRequest -> error.errorMessage
                        is APIError.ServerError -> error.errorMessage
                        is APIError.Unauthorized -> error.errorMessage
                        is APIError.Forbidden -> error.errorMessage
                        is APIError.UnknownError -> error.errorMessage
                        else -> "Password reset failed. Please try again."
                    }
                Log.e("PasswordResetController", "Encrypted API call failed: $errorMessage")
                callback(null, errorMessage)
            } else if (response != null) {
                if (response.status == 200) {
                    callback(response, null)
                } else {
                    val errorMessage = response.message ?: "Password reset failed"
                    callback(response, errorMessage)
                }
            } else {
                callback(null, "Unexpected error occurred")
            }
        }
    }
}
