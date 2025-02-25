package com.example.xone.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("send-otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("login")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("assets")
    fun getAssetDetails(@Body request: AssetRequest): Call<AssetResponse>
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
    val category: String,
    val icon: String? = null
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

data class AssetResponse(
    val status: Int,
    val department: String,
    val designation: String,
    val location: String,
    val mail_id: String,
    val Employee_Code: String,
    val mobile_number: String,
    val username: String,
    val details: List<AssetDetail>
)

data class AssetDetail(
    val asset_type: String,
    val configuration: String,
    val date_of_issue: String,
    val model: String,
    val purchase_date: String?,
    val serial_number: String
)

data class AssetRequest(
    val employeeId: String
)
