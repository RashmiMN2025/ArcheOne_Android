package com.archeGlobal.one.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.archeGlobal.one.HomeActivity
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.OtpVerificationActivity
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.network.EncryptedAPIService
import com.archeGlobal.one.service.MSALAuthenticationManager
import com.archeGlobal.one.utils.PreferencesManager
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EncryptedAPIHelper(
    private val context: Context,
) {
    companion object {
        private const val TAG = "EncryptedAPIHelper"
    }

    private val encryptedAPIService = EncryptedAPIService.getInstance(context)

    private suspend fun refreshMsalTokenIfAvailable(): String? {
        return suspendCoroutine { continuation ->
            try {
                val msalManager = MSALAuthenticationManager(context)
                msalManager.initialize { initSuccess ->
                    if (!initSuccess || !msalManager.isUserSignedIn()) {
                        continuation.resume(null)
                        return@initialize
                    }

                    msalManager.getAccessToken(
                        arrayOf("User.Read"),
                    ) { token ->
                        continuation.resume(token)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "MSAL token refresh error: ${e.message}")
                continuation.resume(null)
            }
        }
    }

    fun <T, R> makeEncryptedCall(
        endpoint: String,
        method: String,
        request: T,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false,
        handleTokenExpiration: Boolean = true,
        callback: (R?, APIError?) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            makeEncryptedCallInternal(
                endpoint = endpoint,
                method = method,
                request = request,
                responseClass = responseClass,
                withAuthHeader = withAuthHeader,
                handleTokenExpiration = handleTokenExpiration,
                callback = callback,
                retryCount = 0
            )
        }
    }

    /**
     * Internal method with retry logic for handling token expiration
     * Automatically refreshes MSAL token and retries on 401/403 errors
     */
    private suspend fun <T, R> makeEncryptedCallInternal(
        endpoint: String,
        method: String,
        request: T,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false,
        handleTokenExpiration: Boolean = true,
        callback: (R?, APIError?) -> Unit,
        retryCount: Int,
    ) {
        try {
            // Refresh MSAL token before making the call to keep session active
            if (withAuthHeader) {
                val refreshedToken = refreshMsalTokenIfAvailable()
                if (!refreshedToken.isNullOrBlank()) {
                    Log.d(TAG, "✓ MSAL token refreshed and saved (expires soon)")
                    PreferencesManager(context).saveAuthToken(refreshedToken)
                }
            }

            val response =
                encryptedAPIService.encryptedRequest(
                    endpoint = endpoint,
                    method = method,
                    body = request,
                    responseClass = responseClass,
                    withAuthHeader = withAuthHeader,
                )

            withContext(Dispatchers.Main) {
                Log.d(TAG, "✓ API call successful: $endpoint")
                callback(response, null)
            }
        } catch (e: APIError) {
            Log.e(TAG, "Encrypted API call failed: ${e.errorMessage}", e)
            Log.e(TAG, "APIError type: ${e::class.java.simpleName}")

            // Handle token expiration with automatic retry
            if (e is APIError.Unauthorized && handleTokenExpiration && retryCount < 1) {
                Log.d(TAG, "⚠ 401 Unauthorized detected - token may have expired")
                Log.d(TAG, "Attempting to refresh MSAL token and retry...")

                try {
                    // Explicitly refresh the token from MSAL
                    val newToken = refreshMsalTokenIfAvailable()
                    if (!newToken.isNullOrBlank()) {
                        Log.d(TAG, "✓ Token refreshed - retrying API call...")
                        PreferencesManager(context).saveAuthToken(newToken)

                        // Retry the API call with the new token
                        makeEncryptedCallInternal(
                            endpoint = endpoint,
                            method = method,
                            request = request,
                            responseClass = responseClass,
                            withAuthHeader = withAuthHeader,
                            handleTokenExpiration = handleTokenExpiration,
                            callback = callback,
                            retryCount = retryCount + 1
                        )
                        return
                    } else {
                        Log.e(TAG, "✗ Failed to get refreshed token from MSAL")
                    }
                } catch (refreshError: Exception) {
                    Log.e(TAG, "Error refreshing token: ${refreshError.message}")
                }

                // If token refresh/retry failed, handle token expiration
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Token refresh/retry failed - redirecting to login")
                    handleTokenExpiration(context)
                    callback(null, e)
                }
            } else if (e is APIError.Unauthorized && !handleTokenExpiration) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "APIError.Unauthorized detected but handleTokenExpiration disabled")
                    callback(null, e)
                }
            } else if (e is APIError.Forbidden) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Handling APIError.Forbidden - showing update dialog")
                    when (context) {
                        is OtpVerificationActivity -> context.showUpdateDialog()
                        is HomeActivity -> context.showUpdateDialog()
                        else -> handleAppUpdateRequired(context)
                    }
                    callback(null, e)
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback(null, e)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e(TAG, "Unexpected error in encrypted API call: ${e.message}", e)
                val userFriendlyMessage =
                    when {
                        e.message?.contains(
                            "timeout",
                            ignoreCase = true,
                        ) == true -> "Request timed out. Please check your internet connection and try again."
                        e.message?.contains(
                            "network",
                            ignoreCase = true,
                        ) == true -> "Network error. Please check your internet connection."
                        e.message?.contains(
                            "connection",
                            ignoreCase = true,
                        ) == true -> "Connection failed. Please check your internet connection."
                        else -> "Unable to connect to server. Please try again."
                    }
                callback(null, APIError.UnknownError(-1, userFriendlyMessage))
            }
        }
    }

    fun APIError.handleErrorWithCallback(callback: (String, Boolean) -> Unit) {
        when (this) {
            is APIError.BadRequest -> callback(this.errorMessage, true)
            is APIError.Unauthorized -> callback("Session expired. Please log in again.", true)
            is APIError.Forbidden -> callback("App update required. Please update to continue.", true)
            is APIError.ServerError -> callback(this.errorMessage, true)
            is APIError.UnknownError -> callback(this.errorMessage, true)
            else -> callback(this.errorMessage, true)
        }
    }

    fun APIError.handleErrorWithContext(
        context: Context,
        callback: (String, Boolean) -> Unit,
    ) {
        when (this) {
            is APIError.BadRequest -> callback(this.errorMessage, true)
            is APIError.Unauthorized -> {
                handleTokenExpiration(context)
                callback("Session expired. Please log in again.", true)
            }
            is APIError.Forbidden -> {
                when (context) {
                    is OtpVerificationActivity -> context.showUpdateDialog()
                    is HomeActivity -> context.showUpdateDialog()
                    else -> handleAppUpdateRequired(context)
                }
                callback("App update required. Please update to continue.", true)
            }
            is APIError.ServerError -> callback(this.errorMessage, true)
            is APIError.UnknownError -> callback(this.errorMessage, true)
            else -> callback(this.errorMessage, true)
        }
    }

    internal fun handleTokenExpiration(context: Context) {
        Log.w("APIError", "Token expired - redirecting to re-authentication")

        val preferencesManager = PreferencesManager(context)
        val userDataManager = UserDataManager.getInstance(context)

        val lastUserData = userDataManager.getUserData()
        if (lastUserData != null) {
            preferencesManager.apply {
                setString("lastEmail", lastUserData.email ?: "")
                setString("lastMobile", lastUserData.mobile ?: "")
                setString("lastEmployeeId", lastUserData.employeeId ?: "")
            }
        }

        preferencesManager.clearSessionData()
        userDataManager.clearSessionData()

        val intent =
            Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("session_expired", true)
                lastUserData?.let {
                    putExtra("last_email", it.email)
                    putExtra("last_mobile", it.mobile)
                    putExtra("last_employee_id", it.employeeId)
                }
            }
        context.startActivity(intent)
    }

    internal fun handleAppUpdateRequired(context: Context) {
        Log.w("APIError", "App update required")
        // Only redirect to LoginActivity for contexts other than OtpVerificationActivity and HomeActivity
        if (context !is OtpVerificationActivity && context !is HomeActivity) {
            val intent =
                Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("showUpdateDialog", true)
                }
            context.startActivity(intent)
        }
    }

    /**
     * Make API call with MSAL access token
     * 
     * This function automatically:
     * 1. Gets the cached MSAL access token (or refreshes if expired)
     * 2. Adds Authorization header with Bearer token
     * 3. Makes the encrypted API request
     * 4. Handles token expiration and refreshes automatically
     * 
     * Usage Example:
     * ```
     * apiHelper.makeEncryptedCallWithMsalToken(
     *     endpoint = "graph/me",
     *     method = "GET",
     *     request = Unit,
     *     responseClass = UserProfile::class.java
     * ) { response, error ->
     *     if (error == null && response != null) {
     *         Log.d("User", "Got profile: ${response.displayName}")
     *     }
     * }
     * ```
     */
    fun <T, R> makeEncryptedCallWithMsalToken(
        endpoint: String,
        method: String,
        request: T,
        responseClass: Class<R>,
        callback: (R?, APIError?) -> Unit,
    ) {
        // Call the existing makeEncryptedCall with MSAL token enabled
        makeEncryptedCall(
            endpoint = endpoint,
            method = method,
            request = request,
            responseClass = responseClass,
            withAuthHeader = true,  // Use MSAL token
            handleTokenExpiration = true,
            callback = callback,
        )
    }
}
