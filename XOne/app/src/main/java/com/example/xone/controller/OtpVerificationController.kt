package com.example.xone.controller

import android.util.Log
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class OtpVerificationController {
    private val client = OkHttpClient()

    // Verify OTP and Login
    fun verifyOtp(email: String, mobile: String, employeeId: String, otp: String, callback: (String, Boolean) -> Unit) {
        val url = "http://172.19.2.240:5000/login"

        // Create JSON request body
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("mobile", mobile)
            put("employeeid", employeeId)
            put("otpFromUser", otp)
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        // Create request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Execute request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OtpVerificationController", "Network Error: ${e.message}")
                callback("Network error. Please try again.", true)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback("Server error. Try again later.", true)
                        return
                    }

                    val responseData = response.body?.string()
                    val jsonResponse = responseData?.let { JSONObject(it) }

                    val message = jsonResponse?.optString("message", "Something went wrong")
                    val isError = response.code != 200

                    callback(message ?: "Unknown error", isError)
                }
            }
        })
    }

    // Resend OTP
    fun resendOtp(email: String, callback: (String) -> Unit) {
        val url = "http://172.19.2.240:5000/send-otp"

        val jsonBody = JSONObject().apply {
            put("email", email)
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OtpVerificationController", "Network Error: ${e.message}")
                callback("Network error. Please try again.")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback("Failed to resend OTP. Try again.")
                        return
                    }

                    val responseData = response.body?.string()
                    val jsonResponse = responseData?.let { JSONObject(it) }
                    val message = jsonResponse?.optString("message", "OTP resend failed")

                    callback(message ?: "Unknown error")
                }
            }
        })
    }
}
