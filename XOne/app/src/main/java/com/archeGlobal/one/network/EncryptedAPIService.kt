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
import java.util.concurrent.TimeUnit

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
                // Provide a more user-friendly error message
                val userFriendlyMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out. Please check your internet connection and try again."
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                    e.message?.contains("connection", ignoreCase = true) == true -> "Connection failed. Please check your internet connection."
                    e.message?.contains("ssl", ignoreCase = true) == true -> "Secure connection failed. Please try again."
                    else -> "Unable to connect to server. Please try again."
                }
                throw APIError.UnknownError(-1, userFriendlyMessage)
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
                // Provide a more user-friendly error message
                val userFriendlyMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out. Please check your internet connection and try again."
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                    e.message?.contains("connection", ignoreCase = true) == true -> "Connection failed. Please check your internet connection."
                    e.message?.contains("ssl", ignoreCase = true) == true -> "Secure connection failed. Please try again."
                    else -> "Unable to connect to server. Please try again."
                }
                throw APIError.UnknownError(-1, userFriendlyMessage)
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
                        val errorMessage = errorResponse.message ?: "Unknown error occurred"
                        throw APIError.UnknownError(response.code, errorMessage)
                    } catch (ex: Exception) {
                        throw APIError.DecodingError
                    }
                }
            }
            400 -> {
                Log.d(TAG, "400 Error Response Body (encrypted): ${responseBody.take(100)}...")
                var errorMessage = "Invalid request. Please check your credentials."
                
                // First try to decrypt the response since it's an encrypted endpoint
                try {
                    Log.d(TAG, "Attempting to decrypt 400 error response")
                    val encryptedPayload = gson.fromJson(responseBody, EncryptedPayload::class.java)
                    val decryptedData = decryptResponse(encryptedPayload)
                    Log.d(TAG, "Decrypted 400 error response: $decryptedData")
                    
                    // Now parse the decrypted JSON
                    val jsonObject = org.json.JSONObject(decryptedData)
                    Log.d(TAG, "Decrypted JSON Object keys: ${jsonObject.keys().asSequence().toList()}")
                    
                    // Try different possible message field names
                    val possibleKeys = listOf("message", "Message", "error", "Error", "errorMessage", "error_message")
                    for (key in possibleKeys) {
                        val message = jsonObject.optString(key, "")
                        if (message.isNotBlank()) {
                            errorMessage = message
                            Log.d(TAG, "Extracted error message from decrypted JSON key '$key': $errorMessage")
                            throw APIError.BadRequest(errorMessage)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decrypt and parse 400 error response: ${e.message}")
                    // Fall back to trying plain JSON parsing (in case it's not encrypted)
                }
                
                // Fallback: Try to parse as plain JSON (in case the error response is not encrypted)
                try {
                    val jsonObject = org.json.JSONObject(responseBody)
                    Log.d(TAG, "Plain JSON Object keys: ${jsonObject.keys().asSequence().toList()}")
                    
                    val possibleKeys = listOf("message", "Message", "error", "Error", "errorMessage", "error_message")
                    for (key in possibleKeys) {
                        val message = jsonObject.optString(key, "")
                        if (message.isNotBlank()) {
                            errorMessage = message
                            Log.d(TAG, "Extracted error message from plain JSON key '$key': $errorMessage")
                            throw APIError.BadRequest(errorMessage)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse as plain JSON: ${e.message}")
                }
                
                // Last fallback: Try Gson parsing
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    val message = errorResponse.message
                    if (message != null && message.isNotBlank()) {
                        errorMessage = message
                        Log.d(TAG, "Extracted error message from Gson: $errorMessage")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse 400 error response with Gson: ${e.message}")
                }
                
                Log.d(TAG, "Final error message for 400: $errorMessage")
                throw APIError.BadRequest(errorMessage)
            }
            401 -> {
                var errorMessage = "Invalid credentials. Please try again."
                
                // Try to decrypt the response first
                try {
                    Log.d(TAG, "Attempting to decrypt 401 error response")
                    val encryptedPayload = gson.fromJson(responseBody, EncryptedPayload::class.java)
                    val decryptedData = decryptResponse(encryptedPayload)
                    Log.d(TAG, "Decrypted 401 error response: $decryptedData")
                    
                    val jsonObject = org.json.JSONObject(decryptedData)
                    val possibleKeys = listOf("message", "Message", "error", "Error", "errorMessage", "error_message")
                    for (key in possibleKeys) {
                        val message = jsonObject.optString(key, "")
                        if (message.isNotBlank()) {
                            errorMessage = message
                            Log.d(TAG, "Extracted 401 error message from decrypted JSON: $errorMessage")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decrypt 401 error response: ${e.message}")
                    // Fall back to plain JSON parsing
                    try {
                        val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                        val message = errorResponse.message
                        if (message != null && message.isNotBlank()) {
                            errorMessage = message
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to parse 401 error response: ${ex.message}")
                    }
                }
                
                throw APIError.Unauthorized(errorMessage)
            }
            403 -> {
                var errorMessage = "Access denied. App update may be required."
                
                // Try to decrypt the response first
                try {
                    Log.d(TAG, "Attempting to decrypt 403 error response")
                    val encryptedPayload = gson.fromJson(responseBody, EncryptedPayload::class.java)
                    val decryptedData = decryptResponse(encryptedPayload)
                    Log.d(TAG, "Decrypted 403 error response: $decryptedData")
                    
                    val jsonObject = org.json.JSONObject(decryptedData)
                    val possibleKeys = listOf("message", "Message", "error", "Error", "errorMessage", "error_message")
                    for (key in possibleKeys) {
                        val message = jsonObject.optString(key, "")
                        if (message.isNotBlank()) {
                            errorMessage = message
                            Log.d(TAG, "Extracted 403 error message from decrypted JSON: $errorMessage")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decrypt 403 error response: ${e.message}")
                    // Fall back to plain JSON parsing
                    try {
                        val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                        val message = errorResponse.message
                        if (message != null && message.isNotBlank()) {
                            errorMessage = message
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to parse 403 error response: ${ex.message}")
                    }
                }
                
                throw APIError.Forbidden(errorMessage)
            }
            500 -> {
                var errorMessage = "Server error. Please try again later."
                
                // Try to decrypt the response first
                try {
                    Log.d(TAG, "Attempting to decrypt 500 error response")
                    val encryptedPayload = gson.fromJson(responseBody, EncryptedPayload::class.java)
                    val decryptedData = decryptResponse(encryptedPayload)
                    Log.d(TAG, "Decrypted 500 error response: $decryptedData")
                    
                    val jsonObject = org.json.JSONObject(decryptedData)
                    val possibleKeys = listOf("message", "Message", "error", "Error", "errorMessage", "error_message")
                    for (key in possibleKeys) {
                        val message = jsonObject.optString(key, "")
                        if (message.isNotBlank()) {
                            errorMessage = message
                            Log.d(TAG, "Extracted 500 error message from decrypted JSON: $errorMessage")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decrypt 500 error response: ${e.message}")
                    // Fall back to plain JSON parsing
                    try {
                        val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                        val message = errorResponse.message
                        if (message != null && message.isNotBlank()) {
                            errorMessage = message
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to parse 500 error response: ${ex.message}")
                    }
                }
                
                throw APIError.ServerError(errorMessage)
            }
            else -> {
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    val errorMessage = errorResponse.message ?: "Server error occurred. Please try again."
                    throw APIError.UnknownError(response.code, errorMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse ${response.code} error response: ${e.message}")
                    throw APIError.UnknownError(response.code, "Server error occurred. Please try again.")
                }
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
                Log.d(TAG, "400 Plain Error Response Body (full): $responseBody")
                Log.d(TAG, "Plain response body length: ${responseBody.length}")
                var errorMessage = "Invalid request. Please check your details."
                
                // Try multiple parsing approaches
                try {
                    // Try to parse as plain JSON first
                    val jsonObject = org.json.JSONObject(responseBody)
                    Log.d(TAG, "Plain JSON Object keys: ${jsonObject.keys().asSequence().toList()}")
                    
                    // Try different possible message field names
                    val possibleKeys = listOf("message", "Message", "error", "Error", "errorMessage", "error_message")
                    for (key in possibleKeys) {
                        val message = jsonObject.optString(key, "")
                        if (message.isNotBlank()) {
                            errorMessage = message
                            Log.d(TAG, "Extracted plain error message from JSON key '$key': $errorMessage")
                            throw APIError.BadRequest(errorMessage)
                        }
                    }
                } catch (e: org.json.JSONException) {
                    Log.e(TAG, "Plain JSONException parsing response: ${e.message}")
                } catch (e: APIError) {
                    // Re-throw APIError (this is our success case)
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Plain unexpected exception parsing JSON: ${e.message}")
                }
                
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    Log.d(TAG, "Plain Gson parsed response: status=${errorResponse.status}, message=${errorResponse.message}")
                    val message = errorResponse.message
                    if (message != null && message.isNotBlank()) {
                        errorMessage = message
                        Log.d(TAG, "Extracted plain error message from Gson: $errorMessage")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse 400 plain error response with Gson: ${e.message}")
                }
                
                // Last resort: try to extract any text that looks like an error message
                if (errorMessage == "Invalid request. Please check your details." && responseBody.contains("message", ignoreCase = true)) {
                    try {
                        // Use regex to find message content
                        val messagePattern = """["']?message["']?\s*:\s*["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
                        val matchResult = messagePattern.find(responseBody)
                        matchResult?.let {
                            val extractedMessage = it.groupValues[1]
                            if (extractedMessage.isNotBlank()) {
                                errorMessage = extractedMessage
                                Log.d(TAG, "Extracted plain error message using regex: $errorMessage")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Plain regex extraction failed: ${e.message}")
                    }
                }
                
                Log.d(TAG, "Final plain error message for 400: $errorMessage")
                throw APIError.BadRequest(errorMessage)
            }
            401 -> {
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    val errorMessage = errorResponse.message ?: "Invalid credentials. Please try again."
                    throw APIError.Unauthorized(errorMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse 401 error response: ${e.message}")
                    throw APIError.Unauthorized("Invalid credentials. Please try again.")
                }
            }
            403 -> {
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    val errorMessage = errorResponse.message ?: "Access denied. App update may be required."
                    throw APIError.Forbidden(errorMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse 403 error response: ${e.message}")
                    throw APIError.Forbidden("Access denied. App update may be required.")
                }
            }
            500 -> {
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    val errorMessage = errorResponse.message ?: "Server error. Please try again later."
                    throw APIError.ServerError(errorMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse 500 error response: ${e.message}")
                    throw APIError.ServerError("Server error. Please try again later.")
                }
            }
            else -> {
                try {
                    val errorResponse = gson.fromJson(responseBody, APIErrorResponse::class.java)
                    val errorMessage = errorResponse.message ?: "Server error occurred. Please try again."
                    throw APIError.UnknownError(response.code, errorMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse ${response.code} error response: ${e.message}")
                    throw APIError.UnknownError(response.code, "Server error occurred. Please try again.")
                }
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
            Log.d(TAG, "Decrypted response: $result") // Log the decrypted response
            return result
        } catch (e: APIError) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting response: ${e.message}", e)
            throw APIError.DecryptionFailed
        }
    }

}
