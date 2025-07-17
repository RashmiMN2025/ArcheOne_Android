package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Encrypted payload structure for secure API communication
 */
data class EncryptedPayload(
    @SerializedName("encryptedData")
    val encryptedData: String,
    
    @SerializedName("encryptedKey")
    val encryptedKey: String,
    
    @SerializedName("initializationVector")
    val initializationVector: String
)

/**
 * API error response model
 */
data class APIErrorResponse(
    @SerializedName("status")
    val status: Int? = null,
    
    @SerializedName("message")
    val message: String
)

/**
 * API error types matching iOS implementation
 */
sealed class APIError(
    val statusCode: Int,
    val errorMessage: String
) : Exception(errorMessage) {
    
    object InvalidURL : APIError(400, "Invalid API URL.")
    
    class BadRequest(message: String) : APIError(400, message)
    
    class Unauthorized(message: String) : APIError(401, message)
    
    class Forbidden(message: String) : APIError(403, message)
    
    class ServerError(message: String) : APIError(500, message)
    
    object DecodingError : APIError(400, "Failed to decode response.")
    
    class SSLPinningFailed(message: String) : APIError(-1, "SSL Pinning Failed: $message")
    
    object EncryptionFailed : APIError(400, "Encryption failed.")
    
    object DecryptionFailed : APIError(400, "Decryption failed.")
    
    object KeyExchangeFailed : APIError(400, "Key exchange failed.")
    
    object InvalidKey : APIError(400, "Invalid server public key.")
    
    class PEMFileError(message: String) : APIError(400, "PEM file error: $message")
    
    class UnknownError(statusCode: Int, message: String) : APIError(statusCode, message)
} 