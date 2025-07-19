package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.network.OtpVerifyResponse
import com.archeGlobal.one.network.SendOtpRequest
import com.archeGlobal.one.network.SendOtpResponse
import com.archeGlobal.one.network.VerifyOtpRequest

/**
 * Example usage of EncryptedAPIService in your existing controllers
 * * This demonstrates how to migrate from regular Retrofit calls to encrypted calls
 */
class EncryptedAPIUsageExample(private val context: Context) {

    companion object {
        private const val TAG = "EncryptedAPIExample"
    }

    private val encryptedAPIHelper = EncryptedAPIHelper(context)

    /**
     * Example 1: Send OTP using encrypted API
     * * BEFORE (Regular Retrofit):
     * RetrofitClient.apiService.sendOtp(request).enqueue(callback)
     * * AFTER (Encrypted):
     */
    fun sendOtpEncrypted(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        val request = SendOtpRequest(email, mobile, employeeId)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "send-otp",
            method = "POST",
            request = request,
            responseClass = SendOtpResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            if (error != null) {
                Log.e(TAG, "Send OTP failed: ${error.errorMessage}")
                error.handleError(callback)
            } else if (response != null) {
                Log.d(TAG, "Send OTP successful: ${response.message}")
                callback(response.message, response.status != 200)
            } else {
                callback("Unknown error occurred", true)
            }
        }
    }

    /**
     * Example 2: Verify OTP using encrypted API
     */
    fun verifyOtpEncrypted(
        email: String,
        mobile: String,
        employeeId: String,
        otpFromUser: String,
        isBiometric: Boolean = false,
        callback: (String, Boolean) -> Unit
    ) {
        val request = VerifyOtpRequest(email, mobile, employeeId, otpFromUser, isBiometric)

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "otpVerify",
            method = "POST",
            request = request,
            responseClass = OtpVerifyResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            if (error != null) {
                Log.e(TAG, "Verify OTP failed: ${error.errorMessage}")
                error.handleError(callback)
            } else if (response != null) {
                Log.d(TAG, "Verify OTP successful: ${response.message}")
                callback(response.message, response.status != 200)
            } else {
                callback("Unknown error occurred", true)
            }
        }
    }

    /**
     * Example 3: Using suspend functions for encrypted calls
     * * Use this approach in coroutines or suspend functions
     */
    suspend fun sendOtpEncryptedSuspend(
        email: String,
        mobile: String,
        employeeId: String
    ): Result<SendOtpResponse> {
        return try {
            val request = SendOtpRequest(email, mobile, employeeId)

            val response = encryptedAPIHelper.makeEncryptedCallSuspend(
                endpoint = "send-otp",
                method = "POST",
                request = request,
                responseClass = SendOtpResponse::class.java,
                withAuthHeader = false
            )

            Result.success(response)
        } catch (e: APIError) {
            Log.e(TAG, "Send OTP failed: ${e.errorMessage}")
            Result.failure(e)
        }
    }

    /**
     * Example 4: Making regular (non-encrypted) API calls
     * * Use this for endpoints that don't require encryption
     */
    fun sendOtpRegular(
        email: String,
        mobile: String,
        employeeId: String,
        callback: (String, Boolean) -> Unit
    ) {
        val request = SendOtpRequest(email, mobile, employeeId)

        encryptedAPIHelper.makeRegularCall(
            endpoint = "send-otp",
            method = "POST",
            request = request,
            responseClass = SendOtpResponse::class.java,
            withAuthHeader = false
        ) { response, error ->
            if (error != null) {
                Log.e(TAG, "Send OTP failed: ${error.errorMessage}")
                error.handleError(callback)
            } else if (response != null) {
                Log.d(TAG, "Send OTP successful: ${response.message}")
                callback(response.message, response.status != 200)
            } else {
                callback("Unknown error occurred", true)
            }
        }
    }

    /**
     * Example 5: Making authenticated encrypted calls
     * * Use this for endpoints that require authentication token
     */
    fun getProfileEncrypted(callback: (String, Boolean) -> Unit) {
        // Example profile request (you would define your actual request/response classes)
        val request = mapOf("action" to "getProfile")

        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "profile",
            method = "GET",
            request = request,
            responseClass = Map::class.java, // Replace with your actual response class
            withAuthHeader = true // This will add the Authorization header
        ) { response, error ->
            if (error != null) {
                Log.e(TAG, "Get profile failed: ${error.errorMessage}")
                error.handleError(callback)
            } else if (response != null) {
                Log.d(TAG, "Get profile successful")
                callback("Profile retrieved successfully", false)
            } else {
                callback("Unknown error occurred", true)
            }
        }
    }
}

/**
 * Migration Guide:
 * * 1. Replace RetrofitClient.apiService calls with EncryptedAPIHelper calls
 * 2. For encrypted endpoints, use makeEncryptedCall() or makeEncryptedCallSuspend()
 * 3. For regular endpoints, use makeRegularCall() or makeRegularCallSuspend()
 * 4. Handle APIError using the handleError() extension function
 * 5. Set withAuthHeader = true for authenticated endpoints
 * * OLD CODE:
 * RetrofitClient.apiService.sendOtp(request).enqueue(object : Callback<SendOtpResponse> {
 *     override fun onResponse(call: Call<SendOtpResponse>, response: Response<SendOtpResponse>) {
 *         if (response.isSuccessful) {
 *             // Handle success
 *         } else {
 *             // Handle error
 *         }
 *     }
 *     override fun onFailure(call: Call<SendOtpResponse>, t: Throwable) {
 *         // Handle failure
 *     }
 * })
 * * NEW CODE:
 * encryptedAPIHelper.makeEncryptedCall(
 *     endpoint = "send-otp",
 *     method = "POST",
 *     request = request,
 *     responseClass = SendOtpResponse::class.java
 * ) { response, error ->
 *     if (error != null) {
 *         error.handleError(callback)
 *     } else if (response != null) {
 *         // Handle success
 *     }
 * }
 */