package com.archeGlobal.one.network

import com.archeGlobal.one.model.AddInventoryItemRequest
import com.archeGlobal.one.model.AddInventoryItemResponse
import com.archeGlobal.one.model.ApiGreetingCategory
import com.archeGlobal.one.model.AvailableRoomsRequest
import com.archeGlobal.one.model.AvailableRoomsResponse
import com.archeGlobal.one.model.BookingHistoryResponse
import com.archeGlobal.one.model.CabBookingRequest
import com.archeGlobal.one.model.CabBookingResponse
import com.archeGlobal.one.model.CabHistoryRequest
import com.archeGlobal.one.model.CabHistoryResponse
import com.archeGlobal.one.model.CalendarResponse
import com.archeGlobal.one.model.CelebrationResponse
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.model.EmployeeSearchRequest
import com.archeGlobal.one.model.EmployeeSearchResponse
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.model.LocationsResponse
import com.archeGlobal.one.model.MeetingApprovalRequest
import com.archeGlobal.one.model.MeetingApprovalResponse
import com.archeGlobal.one.model.PasswordResetRequest
import com.archeGlobal.one.model.PasswordResetResponse
import com.archeGlobal.one.model.PolicyModel
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SocialContent
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.model.StockListResponse
import com.archeGlobal.one.model.SuggestUsersResponse
import com.archeGlobal.one.model.SuggestedUser
import com.archeGlobal.one.model.TravelApprovalActionRequest
import com.archeGlobal.one.model.TravelApprovalActionResponse
import com.archeGlobal.one.model.TravelCancelActionRequest
import com.archeGlobal.one.model.TravelCancelActionResponse
import com.archeGlobal.one.model.TravelCombinedHistoryResponse
import com.archeGlobal.one.model.TravelHistoryRequest
import com.archeGlobal.one.model.TravelHistoryResponse
import com.archeGlobal.one.model.TravelRejectActionRequest
import com.archeGlobal.one.model.TravelRequestResponse
import com.archeGlobal.one.model.TravelRequestSubmission
import com.archeGlobal.one.model.TravelV2AdminHistoryResponse
import com.archeGlobal.one.model.TravelV2ApprovalHistoryCountResponse
import com.archeGlobal.one.model.TravelV2ApprovalHistoryResponse
import com.archeGlobal.one.model.TravelV2OrderHistoryResponse
import com.archeGlobal.one.model.TravelV2Request
import com.archeGlobal.one.model.UpdateInventoryItemRequest
import com.archeGlobal.one.model.UpdateInventoryItemResponse
import com.archeGlobal.one.model.VerifyCheckInRequest
import com.archeGlobal.one.model.VerifyCheckInResponse
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
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url
import retrofit2.http.Query

interface ApiService {
    @GET
    suspend fun downloadReport(
        @Url url: String,
    ): Response<ResponseBody>

    @POST("send-otp")
    fun sendOtp(
        @Body request: SendOtpRequest,
    ): Call<SendOtpResponse>

    @POST("otpVerify")
    fun verifyOtp(
        @Body request: VerifyOtpRequest,
    ): Call<OtpVerifyResponse>

    @POST("login")
    fun login(
        @Header("Authorization") token: String,
        @Body request: LoginRequest,
    ): Call<VerifyOtpResponse>

    @POST("/sos")
    suspend fun submitSOS(
        @Body request: SOSRequest,
    ): Response<SOSResponse>

    @POST("/helpdesk")
    suspend fun submitHelpdesk(
        @Body request: SOSRequest,
    ): Response<SOSResponse>

    @POST("/logout")
    fun logout(
        @Body request: LogoutRequest,
    ): Call<LogoutResponse>

    @HTTP(method = "DELETE", path = "/delete_doc", hasBody = true)
    fun deleteDoc(
        @Body params: Map<String, String>,
    ): Call<ProfilePictureResponse>

    // Document API - List Files
    @Multipart
    @POST("/upload")
    fun listDocuments(
        @Part("email") email: RequestBody,
        @Part("employeeId") employeeId: RequestBody,
        @Part("isPersonal") isPersonal: RequestBody? = null,
    ): Call<DocumentListResponse>

    // Document API - Upload File
    @Multipart
    @POST("/upload")
    fun uploadDocument(
        @Part file: MultipartBody.Part,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
    ): Call<DocumentListResponse>

    // Profile Picture API - Upload
    @Multipart
    @POST("/upload_profile")
    fun uploadProfilePicture(
        @Part file: MultipartBody.Part,
        @Part("email") email: RequestBody,
        @Part("employeeId") employeeId: RequestBody,
    ): Call<ProfilePictureResponse>

    @GET("social")
    suspend fun getSocialContent(): Response<SocialContent>

    @POST("calendar")
    suspend fun getCalendar(
        @Body request: CalendarRequest,
    ): Response<CalendarResponse>

    @GET("policies")
    suspend fun getPolicies(): Response<List<PolicyResponse>>

    @GET("greetings")
    suspend fun getGreetingCards(): Response<Map<String, List<String>>>

    @POST("feedback")
    suspend fun submitFeedback(
        @Body request: FeedbackRequest,
    ): Response<FeedbackResponse>

    @POST("/reset-password")
    fun resetPassword(
        @Body request: PasswordResetRequest,
    ): Call<PasswordResetResponse>

    @GET("daily-event")
    suspend fun getDailyEvent(): Response<EventResponse>

    @POST("travel/v2/request")
    fun submitTravelRequest(
        @Body request: TravelRequestSubmission,
    ): Call<TravelRequestResponse>

    @POST("travel-request-one")
    fun getTravelHistory(
        @Body request: TravelHistoryRequest,
    ): Call<TravelHistoryResponse>

    @POST("travel-request-one/combined-history-one")
    fun getTravelCombinedHistory(
        @Body request: TravelHistoryRequest,
    ): Call<TravelCombinedHistoryResponse>

    @POST("travel/v2/approve")
    fun approveTravelRequest(
        @Body request: TravelApprovalActionRequest,
    ): Call<TravelApprovalActionResponse>

    @POST("travel/v2/reject")
    fun rejectTravelRequest(
        @Body request: TravelRejectActionRequest,
    ): Call<TravelApprovalActionResponse>

    @POST("travel/v2/admin/cancel")
    fun cancelTravelRequest(
        @Body request: TravelCancelActionRequest,
    ): Call<TravelCancelActionResponse>

    @GET("employee-celebration")
    suspend fun getEmployeeCelebration(): Response<CelebrationResponse>

    @POST("tickets")
    fun getTickets(
        @Body request: TicketsRequest,
    ): Call<TicketsResponse>

    @POST("admin/orders")
    fun getOrders(
        @Body request: com.archeGlobal.one.model.OrdersRequest,
    ): Call<com.archeGlobal.one.model.OrdersResponse>

    @GET("deskcart/stocklist")
    suspend fun getStockList(): Response<StockListResponse>

    @POST("deskcart/stocklist/add")
    suspend fun addInventoryItem(
        @Body request: AddInventoryItemRequest,
    ): Response<AddInventoryItemResponse>

    @POST("deskcart/stocklist/update")
    suspend fun updateInventoryItem(
        @Body request: UpdateInventoryItemRequest,
    ): Response<UpdateInventoryItemResponse>

    @POST("deskcart/orderlist/allHistory")
    suspend fun getOrderHistory(): Response<com.archeGlobal.one.model.OrderHistoryResponse>

    @POST("deskcart/orderlist/eligibility")
    suspend fun getDeskCartEligibility(
        @Body request: DeskCartEligibilityRequest,
    ): Response<DeskCartEligibilityResponse>

    @POST("deskcart/orderlist/userHistory")
    suspend fun getDeskCartUserHistory(
        @Body request: com.archeGlobal.one.model.DeskCartOrderHistoryRequest,
    ): Response<com.archeGlobal.one.model.DeskCartOrderHistoryResponse>

    @POST("deskcart/orderlist/placeOrder")
    suspend fun placeDeskCartOrder(
        @Body request: DeskCartPlaceOrderRequest,
    ): Response<DeskCartPlaceOrderResponse>

    @POST("deskcart/orderlist/updateOrderStatus")
    suspend fun updateDeskCartOrderStatus(@Body request: DeskCartUpdateOrderStatusRequest): Response<DeskCartUpdateOrderStatusResponse>

    @POST("meeting/v1/locations&roles")
    suspend fun getLocationsAndRoles(@Body request: Map<String, String>): Response<LocationsResponse>

    @POST("/meeting/v1/available_rooms")
    suspend fun getAvailableRooms(@Body request: AvailableRoomsRequest): Response<AvailableRoomsResponse>

    @GET("suggest-users")
    suspend fun suggestUser(@Query("name") name: String): Response<SuggestUsersResponse>

    @GET("suggest-users")
    fun suggestUsers(
        @retrofit2.http.Query("name") name: String,
    ): Call<List<SuggestedUser>>

    @POST("/meeting/v1/request_booking")
    suspend fun requestBooking(@Body request: BookingRequest): Response<BookingResponse>

    @POST("meeting/v1/booking-history")
    suspend fun getBookingHistory(@Body request: Map<String, String>): Response<BookingHistoryResponse>

    @PUT("meeting/v1/meeting_approval_status/{booking_id}")
    suspend fun updateMeetingApprovalStatus(
        @Path("booking_id") bookingId: String,
        @Body request: MeetingApprovalRequest
    ): Response<MeetingApprovalResponse>

    @POST("meeting/v1/qr_code/verify_and_checkin/{booking_id}")
    suspend fun verifyAndCheckIn(
        @Path("booking_id") bookingId: String
    ): Response<VerifyCheckInResponse>
 
   // Cab Booking APIs
    @POST("travel/v2/request")
    fun submitCabBooking(
        @Body request: CabBookingRequest,
    ): Call<CabBookingResponse>

    @POST("cab-booking/history")
    fun getCabHistory(
        @Body request: CabHistoryRequest,
    ): Call<CabHistoryResponse>

    @POST("employees/search")
    fun searchEmployees(
        @Body request: EmployeeSearchRequest,
    ): Call<EmployeeSearchResponse>



    // V2 Travel APIs
    @POST("travel/v2/approval-history-count")
    fun getTravelV2ApprovalHistoryCount(
        @Body request: TravelV2Request,
    ): Call<TravelV2ApprovalHistoryCountResponse>

    @POST("travel/v2/order-history")
    fun getTravelV2OrderHistory(
        @Body request: TravelV2Request,
    ): Call<TravelV2OrderHistoryResponse>

    @POST("travel/v2/approval-history")
    fun getTravelV2ApprovalHistory(
        @Body request: TravelV2Request,
    ): Call<TravelV2ApprovalHistoryResponse>

    @POST("travel/v2/admin/history")
    fun getTravelV2AdminHistory(
        @Body request: TravelV2Request,
    ): Call<TravelV2AdminHistoryResponse>

    @POST("travel/v2/admin/history")
    @Streaming
    suspend fun downloadTravelAdminReport(
        @Body request: TravelV2Request,
    ): Response<ResponseBody>
}

data class FeedbackRequest(
    val name: String? = null,
    val email: String,
    val category: String? = null,
    val feedback: String?,
    val rating: Int,
    val platform: String,
    val deviceName: String,
    val version: String,
)

data class FeedbackResponse(
    val status: Int,
    val message: String,
)

data class PolicyResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("pdfUrl")
    val pdfUrl: String,
    @SerializedName("previewUrl")
    val previewUrl: String,
)

data class LogoutRequest(
    val email: String,
    val deviceId: String,
)

data class LogoutResponse(
    val status: Int,
    val message: String,
)

data class DocumentListResponse(
    val status: Int,
    val message: Any, // Can be a string message or list of files
    val personalDoc: List<Document>? = emptyList(),
    val professionalDoc: List<Document>? = emptyList(),
)

data class Document(
    val id: Int? = null,
    val docName: String? = null,
    val filePath: String? = null,
    val doc_type: String? = null,
    // Fields from the API response
    val document_name: String? = null,
    val doc_data: String? = null,
    val documentType: String? = null,
)

data class SOSResponse(
    @SerializedName("message") val message: String,
)

data class SendOtpRequest(
    val email: String,
    val mobile: String,
    val employeeId: String,
)

data class SendOtpResponse(
    val message: String,
    val status: Int,
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
    val stayLoggedIn: Boolean = false,
)

data class OtpVerifyResponse(
    val status: Int,
    val message: String,
    val token: String,
)

data class LoginRequest(
    val email: String,
    val mobile: String,
    val employeeId: String,
    val platform: String,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val deviceId: String,
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
    val smartCollateral: List<SmartCollateralCategory>? = emptyList(),
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
    val userDetails: UserDetails? = null,
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
    val documents: List<UserDocument> = emptyList(),
)

data class UserDocument(
    val document_name: String = "",
    val doc_data: String = "",
    val documentType: String = "",
)

data class Service(
    val id: Int,
    val service: String,
    val favourite: Boolean = false,
    val category: String,
    val icon: String? = null,
    val url: String? = null,
    val isNew: Boolean = false,
)

data class Office(
    val id: Int,
    val address: String,
    val email: String,
    val country: String,
    val companyName: String? = null,
    val regionaloffice: List<RegionalOffice>,
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
    val redirection: String? = null,
)

data class WhatsNewItem(
    val category: String,
    val description: String,
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
    val details: List<AssetDetail>,
)

data class AssetDetail(
    val asset_type: String,
    val configuration: String,
    val date_of_issue: String,
    val model: String,
    val purchase_date: String?,
    val serial_number: String,
    val hostname: String?,
)

data class CalendarRequest(
    val state: String,
)

data class TicketsRequest(
    val name: String,
    val category: String,
    val subCategory: String? = null,
)

data class TicketsResponse(
    val tickets: List<TicketItem>,
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
    val subject: String? = null, // Make subject optional since API doesn't always return it
)

data class FAQCategory(
    val title: String,
    val items: List<FAQItem>,
)

data class FAQItem(
    val question: String,
    val answer: List<FAQAnswer>,
)

data class FAQAnswer(
    val cat: String,
    val des: String,
)

data class FAQDataResponse(
    val status: Int,
    val faqList: List<FAQCategory>,
)

data class DeskCartEligibilityRequest(
    val email: String,
)

data class DeskCartEligibilityResponse(
    val status: Int,
    val isAdmin: Boolean,
    val order: List<DeskCartItem>,
)

data class DeskCartItem(
    val name: String,
    val materialId: String,
    val imageUrl: String,
    val limit: Int,
)

data class DeskCartPlaceOrderRequest(
    val email: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    @SerializedName("Location")
    val location: String,
    val items: List<DeskCartOrderItem>,
)

data class DeskCartOrderItem(
    val materialId: String,
    val count: Int,
)

data class DeskCartPlaceOrderResponse(
    val status: Int,
    val message: String,
    val orders: List<DeskCartOrder>,
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
    val emailId: String,
)

data class DeskCartOrderHistoryItem(
    val materialId: String,
    val name: String,
    val count: Int,
)

data class DeskCartUpdateOrderStatusRequest(
    val orderId: String,
    val newStatus: String, // approved, rejected, cancelled, closed
    val processedBy: String, // name of admin
    val rejectionRemarks: String,
)

data class DeskCartUpdateOrderStatusResponse(
    val status: Int,
    val orders: List<DeskCartOrder>,
)

data class SmartCollateralFile(
    val id: Int,
    val fileName: String,
    val fileUrl: String,
    val thumbnailUrl: String? = null,
)

data class SmartCollateralCategory(
    val id: Int,
    val name: String,
    val files: List<SmartCollateralFile>,
)

data class BookingRequest(
    @SerializedName("room_id") val room_id: String ?,
    @SerializedName("room_name") val room_name: String,
    @SerializedName("room_location") val room_location: String ?,
    @SerializedName("host_email") val host_email: String,
    @SerializedName("meeting_type") val meeting_type: String,
    @SerializedName("meeting_starttime") val meeting_starttime: String,
    @SerializedName("meeting_endtime") val meeting_endtime: String,
    @SerializedName("arche_attendees") val arche_attendees: List<String>,
    @SerializedName("guest_attendees") val guest_attendees: List<String>,
    @SerializedName("meeting_subject") val meeting_subject: String = "",
    @SerializedName("meeting_status") val meeting_status: String? = null,
    @SerializedName("business_justification") val business_justification: String,
    @SerializedName("client_name") val client_name: String,
    @SerializedName("project_name") val project_name: String,
//    @SerializedName("meeting_extension") val meeting_extension: String,
    @SerializedName("refreshment_required") val refreshment_required: String,
    @SerializedName("additional_request") val additional_request: String,
    @SerializedName("approval_status") val approval_status: String = "",
    @SerializedName("line_manager_email") val line_manager_email: String
)

data class BookingResponse(
    val status: Int,
    val booking_id: String,
    val message: String
)
