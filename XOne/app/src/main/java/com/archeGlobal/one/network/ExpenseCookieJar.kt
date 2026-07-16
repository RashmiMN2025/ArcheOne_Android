package com.archeGlobal.one.network

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Persists the expense API refresh token as an HTTP cookie so subsequent
 * z-transact API calls automatically include it.
 */
class ExpenseCookieJar(context: Context) : CookieJar {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    init {
        restorePersistedRefreshToken()
    }

    fun saveRefreshToken(
        token: String,
        host: String = ExpenseRetrofitClient.HOST,
    ) {
        if (token.isBlank()) return

        val cookie =
            Cookie
                .Builder()
                .name(REFRESH_TOKEN_COOKIE_NAME)
                .value(token)
                .domain(host)
                .path("/")
                .build()

        cookieStore.compute(host) { _, existing ->
            val updated = existing?.filterNot { it.name == REFRESH_TOKEN_COOKIE_NAME }?.toMutableList()
                ?: mutableListOf()
            updated.add(cookie)
            updated
        }

        preferences.edit { putString(KEY_REFRESH_TOKEN, token) }
        Log.d(TAG, "Stored expense refresh token cookie for $host")
    }

    fun clear() {
        cookieStore.clear()
        preferences.edit { remove(KEY_REFRESH_TOKEN) }
        Log.d(TAG, "Cleared expense refresh token cookie")
    }

    fun hasRefreshToken(): Boolean = getRefreshToken().isNullOrBlank().not()

    fun getRefreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>,
    ) {
        if (cookies.isEmpty()) return

        cookieStore.compute(url.host) { _, existing ->
            val updated = existing?.toMutableList() ?: mutableListOf()
            updated.addAll(cookies)
            updated
        }

        cookies
            .firstOrNull { it.name == REFRESH_TOKEN_COOKIE_NAME }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?.let { saveRefreshToken(it, url.host) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url.host].orEmpty()

    private fun restorePersistedRefreshToken() {
        val token = preferences.getString(KEY_REFRESH_TOKEN, null)
        if (!token.isNullOrBlank()) {
            saveRefreshToken(token)
        }
    }

    companion object {
        private const val TAG = "ExpenseCookieJar"
        private const val PREFS_NAME = "expense_api_cookies"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
    }
}
