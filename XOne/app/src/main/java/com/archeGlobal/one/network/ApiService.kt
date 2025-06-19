package com.archeGlobal.one.network

import com.archeGlobal.one.model.ApiGreetingCategory
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SocialContent
import com.archeGlobal.one.model.CalendarResponse
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.model.PasswordResetRequest
import com.archeGlobal.one.model.PasswordResetResponse
import com.archeGlobal.one.model.TravelRequestSubmission
import com.archeGlobal.one.model.TravelRequestResponse
import com.archeGlobal.one.model.TravelHistoryRequest
import com.archeGlobal.one.model.TravelHistoryResponse
import com.archeGlobal.one.model.TravelApprovalRequest
import com.archeGlobal.one.model.TravelApprovalResponse
import com.archeGlobal.one.model.TravelApprovalActionRequest
import com.archeGlobal.one.model.TravelApprovalActionResponse
import com.archeGlobal.one.model.TravelCombinedHistoryResponse
import com.archeGlobal.one.model.TravelRejectActionRequest
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
import retrofit2.http.PartMap

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

    // Document API - List Files
    @Multipart
    @POST("/upload")
    fun listDocuments(
        @Part("email") email: RequestBody,
        @Part("employeeId") employeeId: RequestBody,
        @Part("isPersonal") isPersonal: RequestBody? = null
    ): Call<DocumentListResponse>
    
    // Document API - Upload File
    @Multipart
    @POST("/upload")
    fun uploadDocument(
        @Part file: MultipartBody.Part,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<DocumentListResponse>
    
    // Profile Picture API - Upload
    @Multipart
    @POST("/upload_profile")
    fun uploadProfilePicture(
        @Part file: MultipartBody.Part,
        @Part("email") email: RequestBody,
        @Part("employeeId") employeeId: RequestBody
    ): Call<ProfilePictureResponse>

    @GET("social")
    suspend fun getSocialContent(): Response<SocialContent>

    @POST("calendar")
    suspend fun getCalendar(@Body request: CalendarRequest): Response<CalendarResponse>

    @GET("calendar")
    fun getHolidays(): Call<CalendarResponse>

    @GET("policies")
    suspend fun getPolicies(): Response<List<PolicyResponse>>

    @GET("greetings")
    suspend fun getGreetingCards(): Response<Map<String, List<String>>>

    @POST("feedback")
    suspend fun submitFeedback(@Body request: FeedbackRequest): Response<FeedbackResponse>
    
    @POST("/reset-password")
    fun resetPassword(@Body request: PasswordResetRequest): Call<PasswordResetResponse>
    
    @GET("daily-event")
    suspend fun getDailyEvent(): Response<EventResponse>
    
    @POST("travel-request")
    fun submitTravelRequest(@Body request: TravelRequestSubmission): Call<TravelRequestResponse>
    
    @POST("travel-request")
    fun getTravelHistory(@Body request: TravelHistoryRequest): Call<TravelHistoryResponse>
    
    @POST("travel-request/combined-history")
    fun getTravelCombinedHistory(@Body request: TravelHistoryRequest): Call<TravelCombinedHistoryResponse>
    
    @POST("travel-request/approval-history")
    fun getTravelApprovalHistory(@Body request: TravelApprovalRequest): Call<TravelApprovalResponse>
    
    @POST("travel-request/approve")
    fun approveTravelRequest(@Body request: TravelApprovalActionRequest): Call<TravelApprovalActionResponse>
    
    @POST("travel-request/reject")
    fun rejectTravelRequest(@Body request: TravelRejectActionRequest): Call<TravelApprovalActionResponse>
}

data class FeedbackRequest(
    val name: String? = null,
    val email: String,
    val category: String? = null,
    val feedback: String?,
    val rating: Int,
    val platform: String,
    val deviceName: String,
    val version: String
)

data class FeedbackResponse(
    val status: Int,
    val message: String
)

data class PolicyResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("pdfUrl")
    val pdfUrl: String,
    @SerializedName("previewUrl")
    val previewUrl: String
)

data class LogoutRequest(
    val employeeId: String
)

data class MyDocRequest(
    val employeeId: String,
    val email: String,
    val documentType: String? = null
)

data class LogoutResponse(
    val status: Int,
    val message: String
)

data class DocumentListResponse(
    val status: Int,
    val message: Any, // Can be a string message or list of files
    val personalDoc: List<Document>? = emptyList(),
    val professionalDoc: List<Document>? = emptyList()
)

data class DocumentFile(
    val fileName: String,
    val url: String
)

data class Document(
    val id: Int? = null,
    val docName: String? = null,
    val filePath: String? = null,
    val doc_type: String? = null,
    // Fields from the API response
    val document_name: String? = null,
    val doc_data: String? = null,
    val documentType: String? = null
)

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
    val otpFromUser: String,
    val isBiometric: Boolean = false
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
    val assetDetails: List<AssetDetail> = emptyList(),
    val communique: List<CommuniqueModel.Communique> = emptyList(),
    val greetings: Map<String, List<String>>? = null,
    val greetingCategories1: List<ApiGreetingCategory>? = null, // changed from greetingCategories
    @SerializedName(value = "eventPopup", alternate = ["event", "dailyEvent", "eventData"]) val eventData: EventResponse? = null
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
    val doc_data: String = "" ,
    val documentType: String = ""
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
    val serial_number: String,
    val hostname: String? 
)

data class CalendarRequest(
    val state: String
)
