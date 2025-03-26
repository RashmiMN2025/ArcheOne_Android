package com.archeGlobal.one.network

import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SocialContent
import com.archeGlobal.one.model.CalendarResponse
import com.archeGlobal.one.model.SosBlogModel
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("send-otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("otpVerify")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<OtpVerifyResponse>

    @POST("login")
    fun login(@Header("Authorization") token: String, @Body request: LoginRequest): Call<VerifyOtpResponse>

    @POST("/sos")
    suspend fun submitSOS(@Body request: SOSRequest): Response<SOSResponse>

    @POST("/sos")
    fun createSOSRequest(@Body request: SOSRequest): Call<SOSResponse>

    @POST("/logout")
    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>

    @Multipart
    @POST("/upload")
    fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("employeeId") employeeId: RequestBody,
        @Part("documentType") documentType: RequestBody
    ): Call<DocumentUploadResponse>

    @GET("social")
    suspend fun getSocialContent(): Response<SocialContent>

    @POST("calendar")
    suspend fun getCalendar(@Body request: CalendarRequest): Response<CalendarResponse>

    @GET("calendar")
    fun getHolidays(): Call<CalendarResponse>
}

data class LogoutRequest(
    val employeeId: String
)

data class LogoutResponse(
    val status: Int,
    val message: String
)

data class DocumentUploadResponse(
    val status: Int,
    val message: String,
    @SerializedName("filePath") val filePath: String? = null,
    val personalDoc: List<Document> = emptyList(),
    val professionalDoc: List<Document> = emptyList()
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
    val employeeId: String
)

data class SendOtpResponse(
    val message: String,
    val status: Int
)

data class VerifyOtpRequest(
    val email: String,
    val mobile: String,
    val employeeId: String,
    val otpFromUser: String
)

data class OtpVerifyResponse(
    val status: Int,
    val message: String,
    val token: String
)

data class LoginRequest(
    val email: String,
    val mobile: String,
    val employeeId: String
)

data class VerifyOtpResponse(
    val message: String,
    val status: Int,
    val user: User?,
    val services: List<Service> = emptyList(),
    val profile_pic: String? = null,
    val sos: String? = null,
    val policiesList: List<PolicyModel.Policy> = emptyList(),
    val offices: List<Office> = emptyList(),
    val sosBlogs: List<SosBlogModel>,
    val assetDetails: List<AssetDetail> = emptyList()
)

data class User(
    val name: String,
    val designation: String,
    val department: String,
    val employeeid: String,
    val email: String,
    val mobile: String,
    val location: String,
    val state: String? = null,
    val userDetails: UserDetails? = null
)

data class UserDetails(
    val reporting_manager: String = "",
    val divisional_head: String = "",
    val pan: String = "",
    val uan: String = "",
    val blood_group: String = "",
    val permanent_address: String = "",
    val temporary_address: String = "",
    val emergency_contact_name: String = "",
    val emergency_contact_relation: String = "",
    val emergency_contact: String = "",
    val documents: List<UserDocument> = emptyList()
)

data class UserDocument(
    val document_name: String = "",
    val doc_data: String = ""
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
    val adminName: String? = null,
    val adminContact: String? = null,
    val hrName: String? = null,
    val hrContact: String? = null,
    val email: String? = null,
    val floorMap: String? = null,
    val redirection: String? = null
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

data class CalendarRequest(
    val state: String
)
