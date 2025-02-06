package com.example.xone.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/send-otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("/login")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>
}

data class SendOtpRequest(val email: String)
data class SendOtpResponse(val message: String, val status: Int)

data class VerifyOtpRequest(val email: String, val mobile: String, val employeeid: String, val otpFromUser: String)
data class VerifyOtpResponse(
    val message: String,
    val sessionId: String?,
    val location: String?,
    val designation: String?,
    val department: String?,
    val name: String?,
    val email: String?,
    val mobile: String?,
    val employeeid: String?,
    val status: String?,
    val services: List<Service>?
)

data class Service(val id: Int, val service: String)
