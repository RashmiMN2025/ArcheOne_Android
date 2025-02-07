package com.example.xone.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("send-otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("login")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>
}

data class SendOtpRequest(
    val email: String,
    val mobile: String,
    val employeeid: String
)

data class SendOtpResponse(
    val message: String,
    val status: Int
)

data class VerifyOtpRequest(
    val email: String,
    val mobile: String,
    val employeeid: String,
    val otpFromUser: String
)

data class VerifyOtpResponse(
    val message: String,
    val status: Int,
    val email: String,
    val name: String,
    val designation: String,
    val department: String,
    val location: String,
    val mobile: String,
    val employeeid: String,
    val services: List<Service>,
    val offices: List<Office>
)

data class Service(
    val id: Int,
    val service: String,
    val favourite: Boolean = false,
    val category: String
)

data class Office(
    val id: Int,
    val address: String,
    val email: String,
    val country: String,
    val companyName: String? = null,
    val regionaloffice: List<RegionalOffice>
)

data class RegionalOffice(
    val region: String,
    val id: Int,
    val address: String,
    val companyName: String? = null,
    val hrContact: String? = null,
    val adminContact: String? = null,
    val email: String? = null
)
