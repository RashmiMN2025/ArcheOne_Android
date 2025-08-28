package com.archeGlobal.one.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.network.EncryptedAPIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EncryptedAPIHelper(private val context: Context) {

    companion object {
        private const val TAG = "EncryptedAPIHelper"
    }

    private val encryptedAPIService = EncryptedAPIService.getInstance(context)

    fun <T, R> makeEncryptedCall(
        endpoint: String,
        method: String,
        request: T,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false,
        handleTokenExpiration: Boolean = true,
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

                    if (e is APIError.Unauthorized && handleTokenExpiration) {
                        Log.d(TAG, "Handling APIError.Unauthorized - calling handleTokenExpiration")
                        handleTokenExpiration(context)
                    } else if (e is APIError.Unauthorized && !handleTokenExpiration) {
                        Log.d(TAG, "APIError.Unauthorized detected but handleTokenExpiration disabled")
                    }

                    if (e is APIError.Forbidden) {
                        Log.d(TAG, "Handling APIError.Forbidden - showing update dialog")
                        if (context is com.archeGlobal.one.OtpVerificationActivity) {
                            (context as com.archeGlobal.one.OtpVerificationActivity).showUpdateDialog()
                        } else {
                            handleAppUpdateRequired(context)
                        }
                    }

                    callback(null, e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Unexpected error in encrypted API call: ${e.message}", e)
                    val userFriendlyMessage = when {
                        e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out. Please check your internet connection and try again."
                        e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                        e.message?.contains("connection", ignoreCase = true) == true -> "Connection failed. Please check your internet connection."
                        else -> "Unable to connect to server. Please try again."
                    }
                    callback(null, APIError.UnknownError(-1, userFriendlyMessage))
                }
            }
        }
    }

    fun <T, R> makeRegularCall(
        endpoint: String,
        method: String,
        request: T?,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false,
        handleTokenExpiration: Boolean = true,
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

                    if (e is APIError.Unauthorized && handleTokenExpiration) {
                        handleTokenExpiration(context)
                    } else if (e is APIError.Unauthorized && !handleTokenExpiration) {
                        Log.d(TAG, "APIError.Unauthorized detected but handleTokenExpiration disabled for regular call")
                    }

                    if (e is APIError.Forbidden) {
                        handleAppUpdateRequired(context)
                    }

                    callback(null, e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Unexpected error in regular API call: ${e.message}", e)
                    val userFriendlyMessage = when {
                        e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out. Please check your internet connection and try again."
                        e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                        e.message?.contains("connection", ignoreCase = true) == true -> "Connection failed. Please check your internet connection."
                        else -> "Unable to connect to server. Please try again."
                    }
                    callback(null, APIError.UnknownError(-1, userFriendlyMessage))
                }
            }
        }
    }

    fun APIError.handleError(callback: (String, Boolean) -> Unit) {
        when (this) {
            is APIError.BadRequest -> callback(this.errorMessage, true)
            is APIError.Unauthorized -> callback(this.errorMessage, true)
            is APIError.ServerError -> callback(this.errorMessage, true)
            is APIError.UnknownError -> callback(this.errorMessage, true)
            else -> callback(this.errorMessage, true)
        }
    }

    fun APIError.handleErrorWithContext(context: Context, callback: (String, Boolean) -> Unit) {
        when (this) {
            is APIError.BadRequest -> callback(this.errorMessage, true)
            is APIError.Unauthorized -> {
                handleTokenExpiration(context)
                callback("Session expired. Please log in again.", true)
            }
            is APIError.Forbidden -> {
                if (context is com.archeGlobal.one.OtpVerificationActivity) {
                    (context as com.archeGlobal.one.OtpVerificationActivity).showUpdateDialog()
                } else {
                    handleAppUpdateRequired(context)
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

        val intent = Intent(context, com.archeGlobal.one.LoginActivity::class.java).apply {
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
        Log.w("APIError", "App update required - navigating to login with update dialog")
        val intent = Intent(context, com.archeGlobal.one.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("showUpdateDialog", true)
        }
        context.startActivity(intent)
    }
}