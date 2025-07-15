package com.archeGlobal.one.network

import android.content.Context
import com.archeGlobal.one.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Interceptor to add authorization token to requests
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = PreferencesManager(context).getAuthToken()

        // Skip adding token for auth endpoints
        val skipAuth = original.url.toString().contains("send-otp") || original.url.toString().contains("otpVerify")

        return if (token != null && !skipAuth) {
            // If we have a token and it's not an auth endpoint, add it to the request
            val requestBuilder = original.newBuilder()
                .header("Authorization", token)

            chain.proceed(requestBuilder.build())
        } else {
            // Otherwise proceed with the original request
            chain.proceed(original)
        }
    }
}

object RetrofitClient {
    const val BASE_URL = "https://dev.arche.global:7000/"
    private var retrofit: Retrofit? = null

    // Initialize with context to get the token
    fun initialize(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context))
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
