package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.network.EncryptedAPIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class for making encrypted API calls
 */
class EncryptedAPIHelper(private val context: Context) {

    companion object {
        private const val TAG = "EncryptedAPIHelper"
    }

    private val encryptedAPIService = EncryptedAPIService.getInstance(context)

    /**
     * Make an encrypted API call with callback
     */
    fun <T, R> makeEncryptedCall(
        endpoint: String,
        method: String,
        request: T,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false,
        callback: (R?, APIError?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = encryptedAPIService.encryptedRequest(
                    endpoint = endpoint,
                    method = method,
                    body = request,
                    responseClass = responseClass,
                    withAuthHeader = withAuthHeader
                )

                withContext(Dispatchers.Main) {
                    callback(response, null)
                }
            } catch (e: APIError) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Encrypted API call failed: ${e.errorMessage}", e)
                    Log.e(TAG, "APIError type: ${e::class.java.simpleName}")

                    // Handle token expiration automatically
                    if (e is APIError.Unauthorized) {
                        Log.d(TAG, "Handling APIError.Unauthorized - calling handleTokenExpiration")
                        handleTokenExpiration(context)
                    }

                    // Handle app update required (403 Forbidden)
                    if (e is APIError.Forbidden) {
                        Log.d(TAG, "Handling APIError.Forbidden - showing update dialog directly")
                        showUpdateDialogDirect(context)
                    }

                    Log.d(TAG, "Calling controller callback with error")
                    callback(null, e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Unexpected error in encrypted API call: ${e.message}", e)
                    callback(null, APIError.UnknownError(-1, "Unexpected error: ${e.message}"))
                }
            }
        }
    }

    /**
     * Make a regular (non-encrypted) API call with callback
     */
    fun <T, R> makeRegularCall(
        endpoint: String,
        method: String,
        request: T?,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false,
        callback: (R?, APIError?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = encryptedAPIService.request(
                    endpoint = endpoint,
                    method = method,
                    body = request,
                    responseClass = responseClass,
                    withAuthHeader = withAuthHeader
                )

                withContext(Dispatchers.Main) {
                    callback(response, null)
                }
            } catch (e: APIError) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Regular API call failed: ${e.errorMessage}", e)

                    // Handle token expiration automatically
                    if (e is APIError.Unauthorized) {
                        handleTokenExpiration(context)
                    }

                    // Handle app update required (403 Forbidden)
                    if (e is APIError.Forbidden) {
                        handleAppUpdateRequired(context)
                    }

                    callback(null, e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Unexpected error in regular API call: ${e.message}", e)
                    callback(null, APIError.UnknownError(-1, "Unexpected error: ${e.message}"))
                }
            }
        }
    }

    /**
     * Suspend version of encrypted API call
     */
    suspend fun <T, R> makeEncryptedCallSuspend(
        endpoint: String,
        method: String,
        request: T,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false
    ): R {
        return encryptedAPIService.encryptedRequest(
            endpoint = endpoint,
            method = method,
            body = request,
            responseClass = responseClass,
            withAuthHeader = withAuthHeader
        )
    }

    /**
     * Suspend version of regular API call
     */
    suspend fun <T, R> makeRegularCallSuspend(
        endpoint: String,
        method: String,
        request: T?,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false
    ): R {
        return encryptedAPIService.request(
            endpoint = endpoint,
            method = method,
            body = request,
            responseClass = responseClass,
            withAuthHeader = withAuthHeader
        )
    }
}

/**
 * Extension function to handle APIError in callbacks
 */
fun APIError.handleError(callback: (String, Boolean) -> Unit) {
    when (this) {
        is APIError.BadRequest -> callback(this.errorMessage, true)
        is APIError.Unauthorized -> callback(this.errorMessage, true)
        is APIError.ServerError -> callback(this.errorMessage, true)
        is APIError.UnknownError -> callback(this.errorMessage, true)
        else -> callback(this.errorMessage, true)
    }
}

/**
 * Enhanced extension function to handle APIError with context for token expiration
 */
fun APIError.handleErrorWithContext(context: Context, callback: (String, Boolean) -> Unit) {
    when (this) {
        is APIError.BadRequest -> callback(this.errorMessage, true)
        is APIError.Unauthorized -> {
            // Handle token expiration by redirecting to login
            handleTokenExpiration(context)
            callback("Session expired. Please log in again.", true)
        }
        is APIError.Forbidden -> {
            // Handle app update required
            handleAppUpdateRequired(context)
            callback("App update required. Please update to continue.", true)
        }
        is APIError.ServerError -> callback(this.errorMessage, true)
        is APIError.UnknownError -> callback(this.errorMessage, true)
        else -> callback(this.errorMessage, true)
    }
}

/**
 * Helper function to handle token expiration
 */
internal fun handleTokenExpiration(context: Context) {
    android.util.Log.w("APIError", "Token expired - redirecting to re-authentication")

    // Get managers
    val preferencesManager = PreferencesManager(context)
    val userDataManager = UserDataManager.getInstance(context)

    // Store current user data temporarily for re-authentication
    val lastUserData = userDataManager.getUserData()
    if (lastUserData != null) {
        preferencesManager.apply {
            setString("lastEmail", lastUserData.email ?: "")
            setString("lastMobile", lastUserData.mobile ?: "")
            setString("lastEmployeeId", lastUserData.employeeId ?: "")
        }
    }

    // Clear session data but keep MPIN/biometric
    preferencesManager.clearSessionData()
    userDataManager.clearSessionData()

    // Navigate to login screen with session expired flag and last user data
    val intent = android.content.Intent(context, com.archeGlobal.one.LoginActivity::class.java).apply {
        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("session_expired", true)
        // Pass last user data for quick re-authentication
        lastUserData?.let {
            putExtra("last_email", it.email)
            putExtra("last_mobile", it.mobile)
            putExtra("last_employee_id", it.employeeId)
        }
    }
    context.startActivity(intent)
}

/**
 * Helper function to handle app update required (403 Forbidden)
 */
internal fun showUpdateDialogDirect(context: Context) {
    android.util.Log.w("APIError", "App update required - showing dialog directly")
    android.util.Log.d("APIError", "Context type: ${context::class.java.simpleName}")
    
    try {
        // Create and show AlertDialog directly
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("App Update Required")
        builder.setMessage("A newer version of the app is required to continue. Please update from the Play Store to proceed.")
        builder.setPositiveButton("Update Now") { _, _ ->
            android.util.Log.d("APIError", "Update button clicked - opening Play Store")
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("APIError", "Failed to open Play Store: ${e.message}")
                // Fallback to web browser
                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                webIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(webIntent)
            }
        }
        // No cancel button - force user to update
        builder.setCancelable(false)
        builder.setOnKeyListener { _, keyCode, _ ->
            // Prevent back button from dismissing the dialog
            keyCode == android.view.KeyEvent.KEYCODE_BACK
        }
        
        val dialog = builder.create()
        android.util.Log.d("APIError", "Showing update dialog")
        dialog.show()
        android.util.Log.d("APIError", "Dialog shown successfully")
    } catch (e: Exception) {
        android.util.Log.e("APIError", "Failed to show update dialog: ${e.message}", e)
        // Fallback to the original intent-based approach
        handleAppUpdateRequired(context)
    }
}

internal fun handleAppUpdateRequired(context: Context) {
    android.util.Log.w("APIError", "App update required - showing update dialog")
    android.util.Log.d("APIError", "Context type: ${context::class.java.simpleName}")
    
    try {
        // Navigate to login screen with update dialog flag
        val intent = android.content.Intent(context, com.archeGlobal.one.LoginActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("showUpdateDialog", true)
        }
        android.util.Log.d("APIError", "Starting LoginActivity with showUpdateDialog=true")
        context.startActivity(intent)
        android.util.Log.d("APIError", "Successfully started LoginActivity")
    } catch (e: Exception) {
        android.util.Log.e("APIError", "Failed to start LoginActivity: ${e.message}", e)
    }
}
