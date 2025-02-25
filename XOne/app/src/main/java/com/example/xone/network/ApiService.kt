package com.example.xone.network

import com.example.xone.model.SOSRequest
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("send-otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("login")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @GET("assets/{employeeId}")
    fun getAssetDetails(@Path("employeeId") employeeId: String): Call<AssetResponse>

    @POST("/sos")
    suspend fun submitSOS(@Body request: SOSRequest): Response<SOSResponse>

    @Multipart
    @POST("/upload")
    fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("employeeId") employeeId: RequestBody,
        @Part("documentType") documentType: RequestBody
    ): Call<DocumentUploadResponse>
}

data class DocumentUploadResponse(
    val personalDoc: List<Document>,
    val professionalDoc: List<Document>,
    val status: Int,
    val message: String
)

data class Document(
    val id: Int,
    val docName: String,
    val filePath: String,
    val doc_type: String)

data class SOSResponse(
    @SerializedName("message") val message: String
)

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
    val details: List<AssetDetail>
)

data class AssetDetail(
    val asset_type: String,
    val configuration: String,
    val date_of_issue: String,
    val department: String,
    val designation: String,
    val division: String,
    val divisional_head: String?,
    val location: String,
    val mail_id: String,
    val mobile_number: String,
    val model: String,
    val new_asset_id: String,
    val old_asset_id: String,
    val reporting_to: String,
    val serial_number: String,
    val username: String,
    val purchase_date: String?,
    val Blood_group: String?,
    val Emergency_Contact: String?,
    val Employee_Code: String?,
    val Manager: String?,
    val PAN: String?,
    val UAN: String?
)
