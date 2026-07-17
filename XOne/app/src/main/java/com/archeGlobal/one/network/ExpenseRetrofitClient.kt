package com.archeGlobal.one.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ExpenseRetrofitClient {
    const val AUTH_BASE_URL = "https://api.dev.z-transact.yavar.ai/"
    const val EXPENSE_BASE_URL = "https://api.dev.z-transact.yavar.ai/expense/"
    const val HOST = "api.dev.z-transact.yavar.ai"

    private var authRetrofit: Retrofit? = null
    private var expenseRetrofit: Retrofit? = null
    private var initialized = false
    lateinit var cookieJar: ExpenseCookieJar
        private set

    fun initialize(context: Context) {
        if (initialized) return

        cookieJar = ExpenseCookieJar(context.applicationContext)

        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

        val okHttpClient =
            OkHttpClient
                .Builder()
                .cookieJar(cookieJar)
                .addInterceptor(ExpenseRefreshTokenInterceptor(cookieJar))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

        authRetrofit =
            Retrofit
                .Builder()
                .baseUrl(AUTH_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        expenseRetrofit =
            Retrofit
                .Builder()
                .baseUrl(EXPENSE_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        initialized = true
        Log.d(
            "ExpenseRetrofitClient",
            "Initialized expense clients — auth: $AUTH_BASE_URL, expense: $EXPENSE_BASE_URL",
        )
    }

    val authService: ExpenseAuthService by lazy {
        if (authRetrofit == null) {
            throw IllegalStateException("ExpenseRetrofitClient must be initialized before use")
        }
        authRetrofit!!.create(ExpenseAuthService::class.java)
    }

    val tripService: ExpenseTripService by lazy {
        if (expenseRetrofit == null) {
            throw IllegalStateException("ExpenseRetrofitClient must be initialized before use")
        }
        expenseRetrofit!!.create(ExpenseTripService::class.java)
    }

    val expenseService: ExpenseService by lazy {
        if (expenseRetrofit == null) {
            throw IllegalStateException("ExpenseRetrofitClient must be initialized before use")
        }
        expenseRetrofit!!.create(ExpenseService::class.java)
    }

    val logoutService: ExpenseLogoutService by lazy {
        if (expenseRetrofit == null) {
            throw IllegalStateException("ExpenseRetrofitClient must be initialized before use")
        }
        expenseRetrofit!!.create(ExpenseLogoutService::class.java)
    }

    fun clearSession() {
        if (initialized) {
            cookieJar.clear()
        }
    }
}
