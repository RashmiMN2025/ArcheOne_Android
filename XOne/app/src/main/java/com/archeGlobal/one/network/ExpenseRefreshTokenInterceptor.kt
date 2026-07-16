package com.archeGlobal.one.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the stored expense refresh token as a Cookie header on each request.
 */
class ExpenseRefreshTokenInterceptor(
    private val cookieJar: ExpenseCookieJar,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.toString()

        if (url.contains("/auth/v1/entra/login")) {
            return chain.proceed(original)
        }

        val refreshToken = cookieJar.getRefreshToken()
        Log.d(TAG, "Expense request: $url")
        Log.d(TAG, "Refresh token available: ${!refreshToken.isNullOrBlank()}")

        val request =
            if (!refreshToken.isNullOrBlank()) {
                val cookieHeader = "${ExpenseCookieJar.REFRESH_TOKEN_COOKIE_NAME}=$refreshToken"
                Log.d(TAG, "Sending Cookie header: $cookieHeader")
                original
                    .newBuilder()
                    .header("Cookie", cookieHeader)
                    .build()
            } else {
                Log.w(TAG, "No refresh token in CookieJar — request sent without Cookie header")
                original
            }

        return chain.proceed(request)
    }

    companion object {
        private const val TAG = "ExpenseRefreshToken"
    }
}
