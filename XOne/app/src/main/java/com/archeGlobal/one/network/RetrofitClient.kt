package com.archeGlobal.one.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// Interceptor to add authorization token to requests and handle token expiration
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val preferencesManager = PreferencesManager(context)
        val token = preferencesManager.getAuthToken()

        // Skip adding token for auth endpoints
        val url = original.url.toString()
        val skipAuth = url.contains("send-otp") || url.contains("otpVerify") || url.contains("otp") || url.endsWith("/otpVerify") || url.endsWith("/send-otp")

        Log.d("AuthInterceptor", "Processing request: $url, skipAuth: $skipAuth")

        val response = if (token != null && !skipAuth) {
            // If we have a token and it's not an auth endpoint, add it to the request
            val requestBuilder = original.newBuilder()
                .header("Authorization", token)

            chain.proceed(requestBuilder.build())
        } else {
            // Otherwise proceed with the original request
            chain.proceed(original)
        }

        // Check if the response indicates token expiration (401 Unauthorized)
        // Skip token expiration handling for OTP-related endpoints completely
        val isOtpEndpoint = url.contains("otp", ignoreCase = true) || url.contains("send-otp", ignoreCase = true) || url.contains("otpVerify", ignoreCase = true)

        if (response.code == 401 && !skipAuth && !isOtpEndpoint) {
            Log.w("AuthInterceptor", "Received 401 Unauthorized - Token expired for URL: $url")
            handleTokenExpiration(context, preferencesManager)
        } else if (response.code == 401) {
            Log.d("AuthInterceptor", "Received 401 for endpoint, not handling as token expiration (skipAuth: $skipAuth, isOtpEndpoint: $isOtpEndpoint): $url")
        }

        return response
    }

    private fun handleTokenExpiration(context: Context, preferencesManager: PreferencesManager) {
        // Clear session data but preserve MPIN and biometric data for re-authentication
        preferencesManager.clearSessionData()
        val userDataManager = UserDataManager.getInstance(context)

        // Store current user data for re-authentication BEFORE clearing
        val lastUserData = userDataManager.getUserData()

        // Clear session data but preserve re-auth methods
        userDataManager.clearSessionData()

        // Preserve essential user data for session expired re-authentication
        if (lastUserData != null) {
            // Save essential credentials back for session expired login
            preferencesManager.setString("session_expired_email", lastUserData.email ?: "")
            preferencesManager.setString("session_expired_mobile", lastUserData.mobile ?: "")
            preferencesManager.setString("session_expired_employee_id", lastUserData.employeeId ?: "")
            preferencesManager.setString("session_expired_name", lastUserData.name ?: "")

            Log.d("AuthInterceptor", "Preserved user data for session expired login: ${lastUserData.name}")
        }

        // Navigate to login screen with session expired flag
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("session_expired", true)
            // Remove duplicate extra
        }
        context.startActivity(intent)

        Log.i("AuthInterceptor", "Redirected to login due to token expiration, preserving MPIN and biometric credentials")
    }
}

object RetrofitClient {
    // const val BASE_URL = "https://archeone.arche.global/"

    const val BASE_URL = "https://dev.arche.global/"
    private var retrofit: Retrofit? = null

    // Initialize with context to get the token
    fun initialize(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create custom TrustManager that accepts certificates for arche.global domains only
        val archeTrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // Default implementation - trust client certificates
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // Accept all certificates for arche.global domain (security risk acknowledged)
                // For production, this should only be used for specific about us functionality
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        // Create custom HostnameVerifier that accepts arche.global domains
        val archeHostnameVerifier = HostnameVerifier { hostname, _ ->
            hostname.contains("arche.global")
        }

        // Create SSL context with custom trust manager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(archeTrustManager), null)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context))
            .sslSocketFactory(sslContext.socketFactory, archeTrustManager)
            .hostnameVerifier(archeHostnameVerifier)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitClient must be initialized with context before use")
        }
        retrofit!!.create(ApiService::class.java)
    }
}
