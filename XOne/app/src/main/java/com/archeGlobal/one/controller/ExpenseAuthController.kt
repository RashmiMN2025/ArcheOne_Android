package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.network.EntraLoginRequest
import com.archeGlobal.one.network.ExpenseRetrofitClient
import com.archeGlobal.one.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseAuthController(
    private val context: Context,
) {
    private val tag = "ExpenseAuthController"

    fun loginWithStoredIdToken(callback: (Boolean, String, String?) -> Unit) {
        val storedIdToken = PreferencesManager(context).getMsalIdToken()

        Log.d(tag, "Attempting to obtain a fresh MSAL id token for Entra login")
        loginWithEntraIdTokenFromMsal { success, message, role ->
            if (success) {
                callback(true, message, role)
            } else if (storedIdToken.isNullOrBlank()) {
                Log.w(tag, "No stored MSAL id token fallback available: $message")
                callback(false, message, null)
            } else {
                Log.w(tag, "Fresh MSAL id token unavailable, falling back to stored id token: $message")
                loginWithEntraIdToken(storedIdToken, callback)
            }
        }
    }

    fun loginWithEntraIdTokenFromMsal(callback: (Boolean, String, String?) -> Unit) {
        refreshMsalIdToken { idToken ->
            if (idToken.isNullOrBlank()) {
                callback(false, "Could not obtain Microsoft identity token", null)
            } else {
                loginWithEntraIdToken(idToken, callback)
            }
        }
    }

    /**
     * Silently asks MSAL for a current id token (MSAL refreshes it internally if the
     * cached one has expired) and persists it. Calls back with null if no token could
     * be obtained, e.g. MSAL isn't ready or there's no signed-in account.
     */
    private fun refreshMsalIdToken(callback: (String?) -> Unit) {
        val msalManager = com.archeGlobal.one.service.MSALAuthenticationManager(context)
        msalManager.initialize { initialized ->
            if (!initialized) {
                callback(null)
                return@initialize
            }
            msalManager.getIdToken { idToken ->
                if (idToken.isNullOrBlank()) {
                    callback(null)
                    return@getIdToken
                }
                PreferencesManager(context).saveMsalIdToken(idToken)
                Log.d(tag, "Refreshed and stored MSAL id token for Entra login")
                callback(idToken)
            }
        }
    }

    fun loginWithEntraIdToken(
        idToken: String,
        callback: (Boolean, String, String?) -> Unit,
        allowRefreshRetry: Boolean = true,
    ) {
        if (idToken.isBlank()) {
            callback(false, "Missing Microsoft identity token", null)
            return
        }

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    ExpenseRetrofitClient.authService.entraLogin(
                        EntraLoginRequest(idToken = idToken),
                    )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.refreshToken.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            callback(false, "Expense login response missing refresh token", null)
                        }
                        return@launch
                    }

                    ExpenseRetrofitClient.cookieJar.saveRefreshToken(body!!.refreshToken)
                    PreferencesManager(context).apply {
                        setInt(ExpenseController.KEY_EXPENSE_USER_ID, body.id)
                        setString(ExpenseController.KEY_EXPENSE_USER_EMAIL, body.email)
                    }
                    Log.d(tag, "Expense entra login successful for ${body.email}")

                    val role = fetchCurrentUserRole()
                    withContext(Dispatchers.Main) {
                        callback(true, body.email, role)
                    }
                } else if (response.code() == 401 && allowRefreshRetry) {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.w(tag, "Entra login rejected id token (401) $errorBody; refreshing MSAL id token and retrying once")
                    withContext(Dispatchers.Main) {
                        refreshMsalIdToken { refreshedIdToken ->
                            if (refreshedIdToken.isNullOrBlank()) {
                                Log.w(tag, "Could not refresh MSAL id token after 401")
                                callback(false, "Expense login failed (401)", null)
                            } else {
                                Log.d(tag, "Retrying Entra login with refreshed MSAL id token")
                                loginWithEntraIdToken(refreshedIdToken, callback, allowRefreshRetry = false)
                            }
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Expense entra login failed: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        callback(false, "Expense login failed (${response.code()})", null)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Expense entra login exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback(false, e.message ?: "Expense login failed", null)
                }
            }
        }
    }

    private suspend fun fetchCurrentUserRole(): String? = withContext(Dispatchers.IO) {
        try {
            val response = ExpenseRetrofitClient.expenseService.getCurrentUserProfile()
            if (response.isSuccessful) {
                response.body()?.role?.takeIf { it.isNotBlank() }
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                Log.w(tag, "Fetch current user profile failed: HTTP ${response.code()} $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.w(tag, "Fetch current user profile exception: ${e.message}", e)
            null
        }
    }

    companion object {
        const val NO_SIGNED_IN_MSAL_ACCOUNT = "NO_SIGNED_IN_MSAL_ACCOUNT"
    }
}
