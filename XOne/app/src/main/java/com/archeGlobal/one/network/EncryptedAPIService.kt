package com.archeGlobal.one.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.APIErrorResponse
import com.archeGlobal.one.model.EncryptedPayload
import com.archeGlobal.one.utils.AESEncryption
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.RSAEncryption
import com.archeGlobal.one.utils.RSAKeyManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class EncryptedAPIService private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "EncryptedAPIService"
        private const val TIMEOUT_SECONDS = 120L
        
        @Volatile
        private var INSTANCE: EncryptedAPIService? = null
        
        fun getInstance(context: Context): EncryptedAPIService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EncryptedAPIService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val baseUrl = RetrofitClient.BASE_URL
    private val gson = Gson()
    private val rsaKeyManager = RSAKeyManager(context)
    private val preferencesManager = PreferencesManager(context)
    
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Make an encrypted API request
     */
    suspend fun <T, R> encryptedRequest(
        endpoint: String,
        method: String,
        body: T,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false
    ): R {
        return withContext(Dispatchers.IO) {
            try {
                // Generate random AES key and IV
                val aesKey = AESEncryption.generateRandomKey()
                val iv = AESEncryption.generateRandomIV()
                val aes = AESEncryption(aesKey, iv)
                
                // Encrypt the request body
                val jsonData = gson.toJson(body).toByteArray(Charsets.UTF_8)
                val encryptedData = aes.encrypt(jsonData)
                    ?: throw APIError.EncryptionFailed
                
                // Get server public key and encrypt the AES key
                val serverPublicKey = rsaKeyManager.getServerPublicKey()
                    ?: throw APIError.InvalidKey
                
                val encryptedAESKey = RSAEncryption.encrypt(aesKey, serverPublicKey)
                    ?: throw APIError.EncryptionFailed
                
                // Create encrypted payload
                val payload = EncryptedPayload(
                    encryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT),
                    encryptedKey = Base64.encodeToString(encryptedAESKey, Base64.DEFAULT),
                    initializationVector = Base64.encodeToString(iv, Base64.DEFAULT)
                )
                
                // Create HTTP request
                val request = createRequest(endpoint, method, gson.toJson(payload), withAuthHeader)
                
                // Execute request
                val response = okHttpClient.newCall(request).execute()
                
                handleResponse(response, responseClass)
                
            } catch (e: APIError) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in encrypted request: ${e.message}", e)
                throw APIError.UnknownError(-1, "Unexpected error: ${e.message}")
            }
        }
    }
    
    /**
     * Make a regular (non-encrypted) API request
     */
    suspend fun <T, R> request(
        endpoint: String,
        method: String,
        body: T?,
        responseClass: Class<R>,
        withAuthHeader: Boolean = false
    ): R {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = if (body != null) gson.toJson(body) else null
                val request = createRequest(endpoint, method, requestBody, withAuthHeader)
                val response = okHttpClient.newCall(request).execute()
                
                handlePlainResponse(response, responseClass)
                
            } catch (e: APIError) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in plain request: ${e.message}", e)
                throw APIError.UnknownError(-1, "Unexpected error: ${e.message}")
            }
        }
    }
    
    private fun createRequest(
        endpoint: String,
        method: String,
        bodyJson: String?,
        withAuthHeader: Boolean
    ): Request {
        val url = "${baseUrl.removeSuffix("/")}/$endpoint"
        val requestBuilder = Request.Builder().url(url)
        
        // Add authorization header if needed
        if (withAuthHeader) {
            val token = preferencesManager.getAuthToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", token)
            }
        }
        
        // Add body for non-GET requests
        val requestBody: RequestBody? = if (bodyJson != null) {
            bodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        } else {
            null
        }
        
        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post(requestBody ?: "".toRequestBody())
            "PUT" -> requestBuilder.put(requestBody ?: "".toRequestBody())
            "DELETE" -> requestBuilder.delete(requestBody)
            else -> throw APIError.InvalidURL
        }
        
        return requestBuilder.build()
    }
    
    private fun <R> handleResponse(response: Response, responseClass: Class<R>): R {
        val responseBody = response.body?.string() ?: throw APIError.DecodingError
        
        Log.d(TAG, "Response status: ${response.code}")
        Log.d(TAG, "Raw response: ${responseBody.take(200)}...")
        
        when (response.code) {
            200 -> {
                try {
                    // Decode encrypted response
                    val encryptedPayload = gson.fromJson(responseBody, EncryptedPayload::class.java)
                    
                    // Decrypt the response
                    val decryptedData = decryptResponse(encryptedPayload)
                    
                    // Parse the decrypted response
                    return gson.fromJson(decryptedData, responseClass)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error decrypting response: ${e.message}", e)
                    
                    // Try to parse as plain error response
                    try {
                        val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                        throw APIError.UnknownError(response.code, errorResponse.message)
                    } catch (ex: Exception) {
                        throw APIError.DecodingError
                    }
                }
            }
            400 -> {
                val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                throw APIError.BadRequest(errorResponse.message)
            }
            401 -> {
                val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                throw APIError.Unauthorized(errorResponse.message)
            }
            500 -> {
                val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                throw APIError.ServerError(errorResponse.message)
            }
            else -> {
                throw APIError.UnknownError(response.code, "Unexpected response code: ${response.code}")
            }
        }
    }
    
    private fun <R> handlePlainResponse(response: Response, responseClass: Class<R>): R {
        val responseBody = response.body?.string() ?: throw APIError.DecodingError
        
        Log.d(TAG, "Response status: ${response.code}")
        Log.d(TAG, "Raw response: ${responseBody.take(200)}...")
        
        when (response.code) {
            200 -> {
                try {
                    return gson.fromJson(responseBody, responseClass)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing plain response: ${e.message}", e)
                    throw APIError.DecodingError
                }
            }
            400 -> {
                val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                throw APIError.BadRequest(errorResponse.message)
            }
            401 -> {
                val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                throw APIError.Unauthorized(errorResponse.message)
            }
            500 -> {
                val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                throw APIError.ServerError(errorResponse.message)
            }
            else -> {
                throw APIError.UnknownError(response.code, "Unexpected response code: ${response.code}")
            }
        }
    }
    
    private fun decryptResponse(encryptedPayload: EncryptedPayload): String {
        try {
            // Decode base64 data
            val encryptedKey = Base64.decode(encryptedPayload.encryptedKey, Base64.DEFAULT)
            val encryptedData = Base64.decode(encryptedPayload.encryptedData, Base64.DEFAULT)
            val iv = Base64.decode(encryptedPayload.initializationVector, Base64.DEFAULT)
            
            // Decrypt the AES key using client private key
            val clientPrivateKey = rsaKeyManager.getClientPrivateKey()
                ?: throw APIError.InvalidKey
            
            val aesKey = RSAEncryption.decrypt(encryptedKey, clientPrivateKey)
                ?: throw APIError.DecryptionFailed
            
            // Decrypt the response data using AES
            val aes = AESEncryption(aesKey, iv)
            val decryptedData = aes.decrypt(encryptedData)
                ?: throw APIError.DecryptionFailed
            
            val result = String(decryptedData, Charsets.UTF_8)
            Log.d(TAG, "Successfully decrypted response")
            return result
            
        } catch (e: APIError) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting response: ${e.message}", e)
            throw APIError.DecryptionFailed
        }
    }
} 