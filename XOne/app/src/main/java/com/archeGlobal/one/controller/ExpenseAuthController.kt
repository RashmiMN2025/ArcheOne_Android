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

    fun loginWithStoredIdToken(callback: (Boolean, String) -> Unit) {
        val idToken = PreferencesManager(context).getMsalIdToken()
        if (idToken.isNullOrBlank()) {
            callback(false, "MSAL id token not found. Please login again.")
            return
        }
        loginWithEntraIdToken(idToken, callback)
    }

    fun loginWithEntraIdTokenFromMsal(callback: (Boolean, String) -> Unit) {
        val msalManager = com.archeGlobal.one.service.MSALAuthenticationManager(context)
        msalManager.initialize { initialized ->
            if (!initialized) {
                callback(false, "Authentication system not ready")
                return@initialize
            }
            if (!msalManager.isUserSignedIn()) {
                Log.d(tag, "Skipping entra login: no signed-in MSAL account")
                callback(false, NO_SIGNED_IN_MSAL_ACCOUNT)
                return@initialize
            }
            msalManager.getIdToken { idToken ->
                if (idToken.isNullOrBlank()) {
                    callback(false, "Could not obtain Microsoft identity token")
                    return@getIdToken
                }
                loginWithEntraIdToken(idToken, callback)
            }
        }
    }

    fun loginWithEntraIdToken(
        idToken: String,
        callback: (Boolean, String) -> Unit,
    ) {
        if (idToken.isBlank()) {
            callback(false, "Missing Microsoft identity token")
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
                            callback(false, "Expense login response missing refresh token")
                        }
                        return@launch
                    }

                    ExpenseRetrofitClient.cookieJar.saveRefreshToken(body!!.refreshToken)
                    Log.d(tag, "Expense entra login successful for ${body.email}")
                    withContext(Dispatchers.Main) {
                        callback(true, body.email)
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Expense entra login failed: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        callback(false, "Expense login failed (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Expense entra login exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback(false, e.message ?: "Expense login failed")
                }
            }
        }
    }

    companion object {
        const val NO_SIGNED_IN_MSAL_ACCOUNT = "NO_SIGNED_IN_MSAL_ACCOUNT"
    }
}
