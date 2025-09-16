package com.archeGlobal.one.network

import com.archeGlobal.one.model.AddInventoryItemRequest
import com.archeGlobal.one.model.AddInventoryItemResponse
import com.archeGlobal.one.model.ApiGreetingCategory
import com.archeGlobal.one.model.CalendarResponse
import com.archeGlobal.one.model.CelebrationResponse
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.model.PasswordResetRequest
import com.archeGlobal.one.model.PasswordResetResponse
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SocialContent
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.StockListResponse
import com.archeGlobal.one.model.TravelApprovalActionRequest
import com.archeGlobal.one.model.TravelApprovalActionResponse
import com.archeGlobal.one.model.TravelCombinedHistoryResponse
import com.archeGlobal.one.model.TravelHistoryRequest
import com.archeGlobal.one.model.TravelHistoryResponse
import com.archeGlobal.one.model.TravelRejectActionRequest
import com.archeGlobal.one.model.TravelRequestResponse
import com.archeGlobal.one.model.TravelRequestSubmission
import com.archeGlobal.one.model.UpdateInventoryItemRequest
import com.archeGlobal.one.model.UpdateInventoryItemResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun downloadReport(@Url url: String): Response<ResponseBody>

    @POST("send-otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("otpVerify")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<OtpVerifyResponse>

    @POST("login")
    fun login(@Header("Authorization") token: String, @Body request: LoginRequest): Call<VerifyOtpResponse>

    @POST("/sos")
    suspend fun submitSOS(@Body request: SOSRequest): Response<SOSResponse>

    @POST("/helpdesk")
    suspend fun submitHelpdesk(@Body request: SOSRequest): Response<SOSResponse>

    @POST("/logout")
    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>

    @HTTP(method = "DELETE", path = "/delete_doc", hasBody = true)
    fun deleteDoc(@Body params: Map<String, String>): Call<ProfilePictureResponse>

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

    @POST("travel-request-one")
    fun submitTravelRequest(@Body request: TravelRequestSubmission): Call<TravelRequestResponse>

    @POST("travel-request-one")
    fun getTravelHistory(@Body request: TravelHistoryRequest): Call<TravelHistoryResponse>

    @POST("travel-request-one/combined-history-one")
    fun getTravelCombinedHistory(@Body request: TravelHistoryRequest): Call<TravelCombinedHistoryResponse>

    @POST("travel-request-one/approve-one")
    fun approveTravelRequest(@Body request: TravelApprovalActionRequest): Call<TravelApprovalActionResponse>

    @POST("travel-request-one/reject-one")
    fun rejectTravelRequest(@Body request: TravelRejectActionRequest): Call<TravelApprovalActionResponse>

    @GET("employee-celebration")
    suspend fun getEmployeeCelebration(): Response<CelebrationResponse>

    // @GET("faq")  // Endpoint returns 404 - FAQ data comes from login response instead
    // fun getFAQData(): Call<FAQDataResponse>

    @POST("tickets")
    fun getTickets(@Body request: TicketsRequest): Call<TicketsResponse>

    @POST("admin/orders")
    fun getOrders(@Body request: com.archeGlobal.one.model.OrdersRequest): Call<com.archeGlobal.one.model.OrdersResponse>

    @GET("deskcart/stocklist")
    suspend fun getStockList(): Response<StockListResponse>

    @POST("deskcart/stocklist/add")
    suspend fun addInventoryItem(@Body request: AddInventoryItemRequest): Response<AddInventoryItemResponse>

    @POST("deskcart/stocklist/update")
    suspend fun updateInventoryItem(@Body request: UpdateInventoryItemRequest): Response<UpdateInventoryItemResponse>

    @POST("deskcart/orderlist/allHistory")
    suspend fun getOrderHistory(): Response<com.archeGlobal.one.model.OrderHistoryResponse>

    @POST("deskcart/orderlist/eligibility")
    suspend fun getDeskCartEligibility(@Body request: DeskCartEligibilityRequest): Response<DeskCartEligibilityResponse>

    @POST("deskcart/orderlist/userHistory")
    suspend fun getDeskCartUserHistory(@Body request: com.archeGlobal.one.model.DeskCartOrderHistoryRequest): Response<com.archeGlobal.one.model.DeskCartOrderHistoryResponse>

    @POST("deskcart/orderlist/placeOrder")
    suspend fun placeDeskCartOrder(@Body request: DeskCartPlaceOrderRequest): Response<DeskCartPlaceOrderResponse>

    @POST("deskcart/orderlist/updateOrderStatus")
    suspend fun updateDeskCartOrderStatus(@Body request: DeskCartUpdateOrderStatusRequest): Response<DeskCartUpdateOrderStatusResponse>
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
    val email: String,
    val deviceId: String
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
    val isBiometric: Boolean = false,
    val appVersion: String,
    val deviceModel: String,
    val deviceId: String,
    val platform: String,
    val osVersion: String,
    val stayLoggedIn: Boolean = false
)

data class OtpVerifyResponse(
    val status: Int,
    val message: String,
    val token: String
)

data class LoginRequest(
    val email: String,
    val mobile: String,
    val employeeId: String,
    val platform: String,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val deviceId: String
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
    @SerializedName(value = "eventPopup", alternate = ["event", "dailyEvent", "eventData"]) val eventData: EventResponse? = null,
    val faqList: List<FAQCategory>? = null,
    val whatsNew: List<WhatsNewItem>? = null,
    val smartCollateral: List<SmartCollateralCategory>? = emptyList()
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
    val reporting_manager_mail: String = "",
    val divisional_head: String = "",
    val pan: String = "",
    val uan: String = "",
    val grade: String = "",
    val aadhar_number: String = "",
    val date_of_birth: String = "",
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
    val doc_data: String = "",
    val documentType: String = ""
)

data class Service(
    val id: Int,
    val service: String,
    val favourite: Boolean = false,
    val category: String,
    val icon: String? = null,
    val url: String? = null,
    val isNew: Boolean = false
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

data class WhatsNewItem(
    val category: String,
    val description: String
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

data class TicketsRequest(
    val name: String,
    val category: String,
    val subcategory: String? = null
)

data class TicketsResponse(
    val tickets: List<TicketItem>
)

data class TicketItem(
    val created_time: String,
    val description: String,
    val id: String,
    val status: String,
    val category: String? = null,
    val subcategory: String? = null,
    val closure_comments: String? = null,
    val resolved_time: String? = null,
    val subject: String? = null // Make subject optional since API doesn't always return it
)

data class FAQCategory(
    val title: String,
    val items: List<FAQItem>
)

data class FAQItem(
    val question: String,
    val answer: List<FAQAnswer>
)

data class FAQAnswer(
    val cat: String,
    val des: String
)

data class FAQDataResponse(
    val status: Int,
    val faqList: List<FAQCategory>
)

data class DeskCartEligibilityRequest(
    val email: String
)

data class DeskCartEligibilityResponse(
    val status: Int,
    val isAdmin: Boolean,
    val order: List<DeskCartItem>
)

data class DeskCartItem(
    val name: String,
    val materialId: String,
    val imageUrl: String,
    val limit: Int
)

data class DeskCartPlaceOrderRequest(
    val email: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    @SerializedName("Location")
    val location: String,
    val items: List<DeskCartOrderItem>
)

data class DeskCartOrderItem(
    val materialId: String,
    val count: Int
)

data class DeskCartPlaceOrderResponse(
    val status: Int,
    val message: String,
    val orders: List<DeskCartOrder>
)

data class DeskCartOrder(
    @SerializedName("order_Id")
    val orderId: String,
    @SerializedName("Emp_Name")
    val empName: String,
    @SerializedName("Emp_ID")
    val empId: String,
    @SerializedName("Dept")
    val dept: String,
    @SerializedName("Location")
    val location: String,
    val items: List<DeskCartOrderHistoryItem>,
    @SerializedName("Total_Items_in_Order")
    val totalItemsInOrder: Int,
    @SerializedName("Order_Placed_Time")
    val orderPlacedTime: String,
    @SerializedName("Order_Closed_time")
    val orderClosedTime: String,
    @SerializedName("Order_Processed_By_(Admin_team)")
    val orderProcessedBy: String,
    @SerializedName("Order_Status")
    val orderStatus: String,
    @SerializedName("Remarks")
    val remarks: String,
    @SerializedName("Emailid")
    val emailId: String
)

data class DeskCartOrderHistoryItem(
    val materialId: String,
    val name: String,
    val count: Int
)

data class DeskCartUpdateOrderStatusRequest(
    val orderId: String,
    val newStatus: String, // approved, rejected, cancelled, closed
    val processedBy: String, // name of admin
    val rejectionRemarks: String
)

data class DeskCartUpdateOrderStatusResponse(
    val status: Int,
    val orders: List<DeskCartOrder>
)
data class SmartCollateralFile(
    val id: Int,
    val fileName: String,
    val fileUrl: String,
    val thumbnailUrl: String? = null
)

data class SmartCollateralCategory(
    val id: Int,
    val name: String,
    val files: List<SmartCollateralFile>
)
