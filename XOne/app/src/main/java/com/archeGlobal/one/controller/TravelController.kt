package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.CabAttendee
import com.archeGlobal.one.model.CabBookingResponse
import com.archeGlobal.one.model.CabLocation
import com.archeGlobal.one.model.EmployeeSearchRequest
import com.archeGlobal.one.model.EmployeeSearchResponse
import com.archeGlobal.one.model.EmployeeSearchResult
import com.archeGlobal.one.model.SuggestedUser
import com.archeGlobal.one.model.TravelApprovalActionRequest
import com.archeGlobal.one.model.TravelApprovalActionResponse
import com.archeGlobal.one.model.TravelCombinedHistoryResponse
import com.archeGlobal.one.model.TravelDetail
import com.archeGlobal.one.model.TravelHistoryRequest
import com.archeGlobal.one.model.TravelHistoryResponse
import com.archeGlobal.one.model.TravelRejectActionRequest
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelRequestResponse
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.model.TravelV2ApprovalHistoryResponse
import com.archeGlobal.one.model.TravelV2OrderHistoryResponse
import com.archeGlobal.one.model.TravelV2Request
import com.archeGlobal.one.model.createCabBookingRequest
import com.archeGlobal.one.model.createMultiDestinationRequest
import com.archeGlobal.one.model.createSingleDestinationRequest
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.CustomToast
import com.archeGlobal.one.utils.UserDataManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Controller for the Travel screens following MVC architecture
 */
class TravelController(
    private val navigator: Navigator,
    private val context: Context,
) {
    /**
     * Sealed class representing the state of travel history
     */
    sealed class TravelHistoryState {
        object Loading : TravelHistoryState()

        data class Success(
            val historyItems: List<TravelRequest>,
            val approvalItems: List<TravelRequest> = emptyList(),
        ) : TravelHistoryState()

        data class Error(
            val message: String,
        ) : TravelHistoryState()
    }

    /**
     * Sealed class representing the state of travel approvals
     */
    sealed class TravelApprovalsState {
        object Idle : TravelApprovalsState()

        object Loading : TravelApprovalsState()

        data class Success(
            val approvalRequests: List<TravelRequest>,
        ) : TravelApprovalsState()

        data class Error(
            val message: String,
        ) : TravelApprovalsState()
    }

    // State for travel approval actions
    sealed class TravelApprovalActionState {
        object Idle : TravelApprovalActionState()

        object Loading : TravelApprovalActionState()

        data class Success(
            val message: String,
        ) : TravelApprovalActionState()

        data class Error(
            val message: String,
        ) : TravelApprovalActionState()
    }

    // Date formatters - Using IST timezone to match Indian Standard Time
    private val displayDateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
    private val apiDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }

    // UI state
    var travelHistoryState by mutableStateOf<TravelHistoryState>(TravelHistoryState.Loading)
        private set

    // Travel approvals state
    var travelApprovalsState by mutableStateOf<TravelApprovalsState>(TravelApprovalsState.Idle)
        private set

    // Selected travel request for detail view
    var selectedTravelRequest by mutableStateOf<TravelRequest?>(null)
        private set

    // Track if we navigated to detail screen from approvals
    var isFromTravelApprovals by mutableStateOf(false)
        private set

    // Travel approval action state
    var approvalActionState by mutableStateOf<TravelApprovalActionState>(TravelApprovalActionState.Idle)
        private set

    // Count of pending travel approvals
    var pendingApprovalCount by mutableStateOf(0)
        private set

    // Flag to prevent concurrent API calls for travel approvals
    private var isLoadingTravelApprovals = false

    // Employee data for travel form
    var employeeName by mutableStateOf("")
        private set
    var employeeId by mutableStateOf("")
        private set
    var mobileNumber by mutableStateOf("")
        private set
    var employeeGrade by mutableStateOf("N/A")
        private set
    var dateOfBirth by mutableStateOf("")
        private set
    var aadharNumber by mutableStateOf("")
        private set
    var reportingManagerName by mutableStateOf("")
        private set
    var reportingManagerEmail by mutableStateOf("")
        private set
    var employeeEmail by mutableStateOf("")
        private set

    // UserDataManager instance
    private val userDataManager = UserDataManager.getInstance(context)

    // Data class for individual destination
    data class Destination(
        val id: String =
            java.util.UUID
                .randomUUID()
                .toString(),
        val originCity: String = "",
        val destination: String = "",
        val departureDate: String = "",
        val returnDate: String = "",
        val flightTimePreference: String = "",
    )

    // Travel form fields
    var originCity by mutableStateOf("")
        private set
    var destination by mutableStateOf("")
        private set
    var projectName by mutableStateOf("")
        private set
    var businessJustification by mutableStateOf("")
        private set

    // Multi-destination support
    var isMultiDestination by mutableStateOf(false)
        private set
    var destinations by mutableStateOf(listOf<Destination>())
        private set

    // Mode of transport options based on employee grade
    val transportOptions: List<String>
        get() {
            // Extract grade as a number if possible
            val gradeNumber = employeeGrade.replace("Grade ", "").toIntOrNull() ?: 0

            // For grade 6 and above, include flight option
            return if (gradeNumber >= 6) {
                listOf("Flight", "Bus", "Train", "Cab")
            } else {
                listOf("Bus", "Train", "Cab")
            }
        }
    var modeOfTransport by mutableStateOf("")
        private set
    var isTransportDropdownExpanded by mutableStateOf(false)
        private set

    // Initialize with current date - Using consistent format and IST timezone
    private val currentDateFormatter =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
    private val currentDate = currentDateFormatter.format(Date())

    var departureDate by mutableStateOf(currentDate)
        private set
    var arrivalDate by mutableStateOf(currentDate)
        private set

    // Flight type preference options
    val flightTypeOptions = listOf("International", "Domestic")
    var flightType by mutableStateOf("")
        private set
    var isFlightTypeDropdownExpanded by mutableStateOf(false)
        private set

    // Flight time preference options
    val flightTimeOptions =
        listOf(
            "Early Morning (00:00-06:00)",
            "Morning (06:00-12:00)",
            "Mid Day (12:00-18:00)",
            "Night (18:00-23:00)",
        )
    var flightTimePreference by mutableStateOf("")
        private set
    var isFlightTimeDropdownExpanded by mutableStateOf(false)
        private set

    // Seat preference options
    val seatPreferenceOptions = listOf("Aisle", "Window", "Any")
    var seatPreference by mutableStateOf("")
        private set
    var isSeatPrefDropdownExpanded by mutableStateOf(false)
        private set

    // Meal preference
    var mealPreferenceEnabled by mutableStateOf(false)
        private set
    val mealPreferenceOptions = listOf("Veg", "Non-Veg")
    var mealPreference by mutableStateOf("")
        private set
    var isMealPrefDropdownExpanded by mutableStateOf(false)
        private set

    // Stay required
    var stayRequired by mutableStateOf(false)
        private set

    // Frequent flyer number
    var frequentFlyerNumber by mutableStateOf("0")
        private set
    var showFrequentFlyerDialog by mutableStateOf(false)
        private set

    // Cab booking specific fields
    var isLocalTravel by mutableStateOf(false)
        private set
    var passengerCount by mutableStateOf(1)
        private set
    var cabType by mutableStateOf("")
        private set
    var cabDuration by mutableStateOf("")
        private set
    var pickupLocation by mutableStateOf("")
        private set
    var finalDropLocation by mutableStateOf("")
        private set
    var visitPoints by mutableStateOf(listOf<VisitPoint>())
        private set
    var mapDetails by mutableStateOf("")
        private set

    // Enhanced cab booking fields to match design specifications
    var projectId by mutableStateOf("")
        private set
    var opportunityId by mutableStateOf("")
        private set
    var crmId by mutableStateOf("")
        private set
    var cabTravelDate by mutableStateOf(currentDate)
        private set
    var attendeeSearchQuery by mutableStateOf("")
        private set
    var additionalAttendees by mutableStateOf(listOf<CabAttendee>())
        private set
    var cabPickupLocations by mutableStateOf(listOf<CabLocation>())
        private set

    // Employee search state
    var employeeSearchResults by mutableStateOf(listOf<EmployeeSearchResult>())
        private set
    var isSearchingEmployees by mutableStateOf(false)
        private set
    var showAttendeeSearch by mutableStateOf(false)
        private set

    // Suggested users search state (for cab booking)
    var suggestedUsers by mutableStateOf(listOf<SuggestedUser>())
        private set
    var isSearchingSuggestedUsers by mutableStateOf(false)
        private set

    // Cab submission state
    var isCabSubmitting by mutableStateOf(false)
        private set
    var cabSubmissionError by mutableStateOf<String?>(null)
        private set

    // Cab dropdown states
    var isCabTypeDropdownExpanded by mutableStateOf(false)
        private set
    var isCabDurationDropdownExpanded by mutableStateOf(false)
        private set

    // Enhanced cab options to match design
    val cabTypeOptions = listOf("5 Seats", "7 Seats")
    val cabDurationOptions = listOf("4 Hours", "8 Hours")
    val travelTypeOptions = listOf("Local Travel", "Out of Local Station")

    // Travel type dropdown state
    var isTravelTypeDropdownExpanded by mutableStateOf(false)
        private set

    // Data class for visit points
    data class VisitPoint(
        val id: String =
            java.util.UUID
                .randomUUID()
                .toString(),
        val location: String = "",
        val order: Int = 1,
    )

    // Flag to track if data has been loaded to prevent duplicate API calls
    private var isDataLoaded = false

    init {
        // Only load essential data (employee details) - no API calls
        loadEmployeeDetails()

        // Initialize with one destination for multi-destination mode
        destinations =
            listOf(
                Destination(
                    originCity = "",
                    destination = "",
                    departureDate = currentDate,
                    returnDate = currentDate,
                    flightTimePreference = "",
                ),
            )
        Log.d("TravelController", "TravelController created - data will be loaded on first access")
    }

    /**
     * Call this method when the Travel service is actually accessed by the user
     * This ensures data is loaded only when needed
     */
    fun onServiceAccessed() {
        Log.d("TravelController", "Travel service accessed - loading data")
        if (!isDataLoaded) {
            loadCombinedTravelHistory()
            isDataLoaded = true
        } else {
            Log.d("TravelController", "Travel data already loaded, skipping API call")
        }
    }

    /**
     * Load employee details from UserDataManager
     */
    private fun loadEmployeeDetails() {
        val userData = userDataManager.getUserData()

        // Update employee details from login response
        userData?.let { user ->
            employeeId = user.employeeId
            employeeName = user.name
            employeeEmail = user.email
            mobileNumber = user.mobile

            // Get reporting manager name and other details from user details if available
            user.userDetails?.let { details ->
                reportingManagerName = details.reporting_manager
                reportingManagerEmail = details.reporting_manager_mail
                dateOfBirth = details.date_of_birth
                aadharNumber = details.aadhar_number
                employeeGrade = details.grade.ifEmpty { "N/A" }
            }
        }
    }

    /**
     * Load travel history for the current user using the legacy API
     * This is kept for backward compatibility
     */
    fun loadTravelHistory() {
        // Set to loading state
        travelHistoryState = TravelHistoryState.Loading

        // Create request with employee ID and email
        val request =
            TravelHistoryRequest(
                employeeId = employeeId,
                employeeEmail = employeeEmail,
            )

        // Make API call to get travel history
        RetrofitClient.apiService.getTravelHistory(request).enqueue(
            object : Callback<TravelHistoryResponse> {
                override fun onResponse(
                    call: Call<TravelHistoryResponse>,
                    response: Response<TravelHistoryResponse>,
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val historyItems = response.body()!!.orderHistory.map { it.toTravelRequest() }
                        travelHistoryState = TravelHistoryState.Success(historyItems)
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e("TravelController", "Error loading travel history: ${response.code()}, Error: $errorBody")
                            travelHistoryState = TravelHistoryState.Error("Failed to load travel history. Please try again.")
                        } catch (e: Exception) {
                            Log.e("TravelController", "Error parsing error response", e)
                            travelHistoryState = TravelHistoryState.Error("Failed to load travel history. Please try again.")
                        }
                    }
                }

                override fun onFailure(
                    call: Call<TravelHistoryResponse>,
                    t: Throwable,
                ) {
                    Log.e("TravelController", "Network error loading travel history", t)
                    travelHistoryState = TravelHistoryState.Error("Network error. Please check your connection and try again.")
                }
            },
        )
    }

    /**
     * Load combined travel history for the current user
     * This uses the new v2 API endpoints for order-history and approval-history
     */
    fun loadCombinedTravelHistory() {
        // Set to loading state
        travelHistoryState = TravelHistoryState.Loading

        // Create request with employee email (v2 API only needs email)
        val request = TravelV2Request(employeeEmail = employeeEmail)

        // Variables to track both API calls
        var orderHistoryResponse: List<com.archeGlobal.one.model.TravelOrderHistoryItem>? = null
        var approvalHistoryResponse: List<com.archeGlobal.one.model.TravelApprovalHistoryItem>? = null
        var orderCallCompleted = false
        var approvalCallCompleted = false

        // Function to check if both calls are completed and process results
        fun processResults() {
            if (orderCallCompleted && approvalCallCompleted) {
                val orderItems = orderHistoryResponse ?: emptyList()
                val approvalItems = approvalHistoryResponse ?: emptyList()

                Log.d(
                    "TravelController",
                    "V2 API: Loaded ${orderItems.size} order history items and ${approvalItems.size} approval history items",
                )

                // Convert to TravelRequest objects
                val historyItems = orderItems.map { item ->
                    Log.d("TravelController", "Processing order ${item.requestId}")
                    item.toTravelRequest()
                }

                val approvalItemsConverted = approvalItems.map { item ->
                    Log.d("TravelController", "Processing approval ${item.requestId}")
                    item.toTravelRequest()
                }

                travelHistoryState = TravelHistoryState.Success(
                    historyItems = historyItems,
                    approvalItems = approvalItemsConverted,
                )

                Log.d(
                    "TravelController",
                    "V2 API: Loaded combined history: ${historyItems.size} order requests, ${approvalItemsConverted.size} approval requests",
                )
            }
        }

        // Make order history API call
        RetrofitClient.apiService.getTravelV2OrderHistory(request).enqueue(
            object : Callback<TravelV2OrderHistoryResponse> {
                override fun onResponse(
                    call: Call<TravelV2OrderHistoryResponse>,
                    response: Response<TravelV2OrderHistoryResponse>,
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        orderHistoryResponse = response.body()!!.orderHistory
                        Log.d("TravelController", "V2 Order history API success: ${orderHistoryResponse?.size} items")
                    } else {
                        Log.e("TravelController", "V2 Order history API error: ${response.code()}")
                        orderHistoryResponse = emptyList()
                    }
                    orderCallCompleted = true
                    processResults()
                }

                override fun onFailure(call: Call<TravelV2OrderHistoryResponse>, t: Throwable) {
                    Log.e("TravelController", "V2 Order history API network error", t)
                    orderHistoryResponse = emptyList()
                    orderCallCompleted = true
                    processResults()
                }
            }
        )

        // Make approval history API call
        RetrofitClient.apiService.getTravelV2ApprovalHistory(request).enqueue(
            object : Callback<TravelV2ApprovalHistoryResponse> {
                override fun onResponse(
                    call: Call<TravelV2ApprovalHistoryResponse>,
                    response: Response<TravelV2ApprovalHistoryResponse>,
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        approvalHistoryResponse = response.body()!!.approvalHistory
                        Log.d("TravelController", "V2 Approval history API success: ${approvalHistoryResponse?.size} items")
                    } else {
                        Log.e("TravelController", "V2 Approval history API error: ${response.code()}")
                        approvalHistoryResponse = emptyList()
                    }
                    approvalCallCompleted = true
                    processResults()
                }

                override fun onFailure(call: Call<TravelV2ApprovalHistoryResponse>, t: Throwable) {
                    Log.e("TravelController", "V2 Approval history API network error", t)
                    approvalHistoryResponse = emptyList()
                    approvalCallCompleted = true
                    processResults()
                }
            }
        )
    }

    // We now use real data from the API instead of mock data

    /**
     * Navigate back to previous screen
     * @param fromTravelDetail If true, we're navigating back from the travel request detail screen
     */
    fun onBackPressed(fromTravelDetail: Boolean = false) {
        // Reset approval action state when navigating back
        resetApprovalActionState()

        if (fromTravelDetail) {
            if (isFromTravelApprovals) {
                // If we came from travel approvals, go back to approvals
                isFromTravelApprovals = false // Reset the flag
                selectedTravelRequest = null // Clear selected request
                navigator.navigateToTravelApprovals()
            } else {
                // When in travel request detail from history, navigate back to travel screen
                selectedTravelRequest = null // Clear selected request
                navigator.navigateToTravel()
            }
        } else {
            // When in travel or travel history screen, navigate back to home
            navigator.navigateToHome()
        }
    }

    /**
     * Navigate to travel history screen
     * Refreshes travel history data before navigating
     */
    fun navigateToTravelHistory() {
        // Refresh travel history data before navigating using the new combined API
        loadCombinedTravelHistory()
        navigator.navigateToTravelExpenses()
    }

    /**
     * Navigate to travel request details screen from travel history
     */
    fun navigateToTravelDetails(travelRequestId: String) {
        // Find the travel request with the given ID from the current state
        val currentState = travelHistoryState
        if (currentState is TravelHistoryState.Success) {
            // Search in history items (this is for travel history)
            val request = currentState.historyItems.find { it.id == travelRequestId }

            if (request != null) {
                // Store the selected travel request
                selectedTravelRequest = request
                // Clear the approvals flag since we're coming from history
                isFromTravelApprovals = false
                // Navigate to the travel history detail screen
                navigator.navigateToTravelHistoryDetail()
            } else {
                Log.e("TravelController", "Travel request with ID $travelRequestId not found in history")
            }
        } else {
            Log.e("TravelController", "Cannot navigate to travel details: travel history not loaded")
        }
    }

    /**
     * Navigate to travel request details screen from travel approvals
     */
    fun navigateToTravelApprovalDetails(travelRequestId: String) {
        // Find the travel request with the given ID from approval state
        val currentApprovalState = travelApprovalsState
        if (currentApprovalState is TravelApprovalsState.Success) {
            val request = currentApprovalState.approvalRequests.find { it.id == travelRequestId }

            if (request != null) {
                // Store the selected travel request
                selectedTravelRequest = request
                // Navigate to the travel request detail screen (for approvals)
                navigator.navigateToTravelRequestDetail()
            } else {
                Log.e("TravelController", "Travel request with ID $travelRequestId not found in approvals")
            }
        } else {
            Log.e("TravelController", "Cannot navigate to travel approval details: approvals not loaded")
        }
    }

    /**
     * Navigate to travel approvals screen
     */
    fun navigateToTravelApprovals() {
        loadTravelApprovals()
        navigator.navigateToTravelApprovals()
    }

    /**
     * Navigate to the travel approval detail screen
     */
    fun navigateToTravelApprovalDetail(travelRequest: TravelRequest) {
        // Store the selected request so the destination screen can read it
        selectedTravelRequest = travelRequest

        // Mark that we're navigating from approvals
        isFromTravelApprovals = true

        // If the request is already processed (approved / rejected) just show the read-only
        // details page for travel approvals. Otherwise open the approval page
        if (travelRequest.status == TravelStatus.APPROVED || travelRequest.status == TravelStatus.REJECTED) {
            navigator.navigateToTravelRequestDetail()
        } else {
            navigator.navigateToTravelApprovalDetail()
        }
    }

    /**
     * Navigate to the travel approval confirmation screen
     */
    fun navigateToTravelApprovalConfirm(travelRequest: TravelRequest) {
        selectedTravelRequest = travelRequest
        navigator.navigateToTravelApprovalConfirm()
    }

    /**
     * Navigate to the travel approve screen using Intent-based navigation
     */
    fun navigateToTravelApprove(travelRequest: TravelRequest) {
        Log.d("TravelController", "navigateToTravelApprove called with request: ${travelRequest.id}")
        Log.d("TravelController", "Travel request details: destination=${travelRequest.destination}, status=${travelRequest.status}")

        // Set the selected travel request
        selectedTravelRequest = travelRequest

        try {
            // Use context to start the TravelApproveActivity
            val context = context as? android.app.Activity ?: return
            val intent = android.content.Intent(context, com.archeGlobal.one.ui.activities.TravelApproveActivity::class.java)

            // Pass the travel request as JSON
            val gson = com.google.gson.Gson()
            val travelRequestJson = gson.toJson(travelRequest)
            intent.putExtra("travel_request", travelRequestJson)

            // Start the activity
            context.startActivity(intent)
            Log.d("TravelController", "Started TravelApproveActivity with intent")
        } catch (e: Exception) {
            Log.e("TravelController", "Error navigating to travel approve screen: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Navigate to the travel reject screen using Intent-based navigation
     */
    fun navigateToTravelReject(travelRequest: TravelRequest) {
        Log.d("TravelController", "navigateToTravelReject called with request: ${travelRequest.id}")

        // Set the selected travel request
        selectedTravelRequest = travelRequest

        try {
            // Use context to start the TravelRejectActivity
            val context = context as? android.app.Activity ?: return
            val intent = android.content.Intent(context, com.archeGlobal.one.ui.activities.TravelRejectActivity::class.java)

            // Pass the travel request as JSON
            val gson = com.google.gson.Gson()
            val travelRequestJson = gson.toJson(travelRequest)
            intent.putExtra("travel_request", travelRequestJson)

            // Start the activity
            context.startActivity(intent)
            Log.d("TravelController", "Started TravelRejectActivity with intent")
        } catch (e: Exception) {
            Log.e("TravelController", "Error navigating to travel reject screen: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Navigate back to the previous screen
     */
    fun navigateBack() {
        // Reset approval action state when navigating back
        resetApprovalActionState()
        // Use popBackStack to go back, just like the back swipe gesture
        navigator.popBackStack()
    }

    /**
     * Reset the approval action state to Idle
     */
    fun resetApprovalActionState() {
        approvalActionState = TravelApprovalActionState.Idle
    }

    /**
     * Check if there are any pending travel approvals
     * @return True if there are pending approvals, false otherwise
     */
    fun hasPendingApprovals(): Boolean = pendingApprovalCount > 0

    /**
     * Check if there is any travel approval history
     * @return True if there is approval history, false otherwise
     */
    fun hasApprovalHistory(): Boolean {
        val currentState = travelHistoryState
        return if (currentState is TravelHistoryState.Success) {
            currentState.approvalItems.isNotEmpty()
        } else {
            false
        }
    }

    /**
     * Load travel approval requests from the API
     */
    fun loadTravelApprovals() {
        // Prevent concurrent API calls
        if (isLoadingTravelApprovals) {
            return
        }

        isLoadingTravelApprovals = true

        // Set state to loading only if not already in a success state with data
        if (travelApprovalsState is TravelApprovalsState.Idle) {
            travelApprovalsState = TravelApprovalsState.Loading
        }

        // Get the user's email from UserDataManager
        val userEmail = userDataManager.getUserData()?.email?.takeIf { it.isNotBlank() } ?: reportingManagerEmail

        if (userEmail.isBlank()) {
            travelApprovalsState = TravelApprovalsState.Error("User email not found")
            isLoadingTravelApprovals = false
            return
        }

        // Create the request body for v2 API (only needs email)
        val request = TravelV2Request(employeeEmail = userEmail)

        // Debug logging to see what user we're sending
        android.util.Log.d("TravelController", "Making V2 API call with userEmail: $userEmail")

        // Make the API call using v2 approval history endpoint
        RetrofitClient.apiService.getTravelV2ApprovalHistory(request).enqueue(
            object : Callback<TravelV2ApprovalHistoryResponse> {
                override fun onResponse(
                    call: Call<TravelV2ApprovalHistoryResponse>,
                    response: Response<TravelV2ApprovalHistoryResponse>,
                ) {
                    isLoadingTravelApprovals = false
                    if (response.isSuccessful) {
                        val v2Response = response.body()
                        if (v2Response != null && v2Response.status == 200) {
                            // Convert API response to UI models (only approval history)
                            val approvalRequests = v2Response.approvalHistory.map { it.toTravelRequest() }
                            travelApprovalsState = TravelApprovalsState.Success(approvalRequests)

                            // Update pending approvals count
                            pendingApprovalCount = approvalRequests.count { it.status == TravelStatus.PENDING }
                        } else {
                            travelApprovalsState = TravelApprovalsState.Error("Failed to load approval requests")
                        }
                    } else {
                        travelApprovalsState = TravelApprovalsState.Error("Error: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(
                    call: Call<TravelV2ApprovalHistoryResponse>,
                    t: Throwable,
                ) {
                    isLoadingTravelApprovals = false
                    Log.e("TravelController", "Error loading travel approvals", t)
                    travelApprovalsState = TravelApprovalsState.Error("Network error: ${t.message}")
                }
            },
        )
    }

    /**
     * Approve a travel request
     */
    fun approveTravelRequest(
        travelRequestId: String,
        remarks: String = "",
    ) {
        // Set the action state to loading
        approvalActionState = TravelApprovalActionState.Loading

        val currentState = travelApprovalsState
        val request: TravelRequest? =
            when (currentState) {
                is TravelApprovalsState.Success -> currentState.approvalRequests.find { it.id == travelRequestId }
                else -> selectedTravelRequest?.takeIf { it.id == travelRequestId }
            }

        if (request == null) {
            Log.e("TravelController", "Travel request with ID $travelRequestId not found")
            approvalActionState = TravelApprovalActionState.Error("Travel request not found")
            return
        }

        // First update the local state (if we have a list) to give immediate feedback
        if (currentState is TravelApprovalsState.Success) {
            val updatedRequests =
                currentState.approvalRequests.map {
                    if (it.id == travelRequestId) it.copy(status = TravelStatus.APPROVED) else it
                }
            travelApprovalsState = TravelApprovalsState.Success(updatedRequests)
        }

        // Log the action
        Log.d("TravelController", "Approving travel request: $travelRequestId")

        // Get the user email from UserDataManager
        val userEmail = userDataManager.getUserData()?.email?.takeIf { it.isNotBlank() } ?: reportingManagerEmail

        if (userEmail.isBlank()) {
            Log.e("TravelController", "Approver email is empty, cannot approve travel request")
            approvalActionState = TravelApprovalActionState.Error("Approver email not found")
            return
        }

        // Get the action token from the request
        val token = request.actionToken ?: ""

        // Make the API call to approve the request
        val approveRequest =
            TravelApprovalActionRequest(
                email = userEmail,
                requestId = travelRequestId,
                token = token,
            )

        RetrofitClient.apiService.approveTravelRequest(approveRequest).enqueue(
            object : Callback<TravelApprovalActionResponse> {
                override fun onResponse(
                    call: Call<TravelApprovalActionResponse>,
                    response: Response<TravelApprovalActionResponse>,
                ) {
                    if (response.isSuccessful) {
                        val approvalResponse = response.body()
                        if (approvalResponse != null && approvalResponse.status == 200) {
                            Log.d("TravelController", "Successfully approved travel request: $travelRequestId")
                            // Update the approval action state
                            approvalActionState =
                                TravelApprovalActionState.Success(approvalResponse.message ?: "Request approved successfully")
                            // State is already updated, no need to do anything else
                        } else {
                            Log.e("TravelController", "Error approving travel request: ${approvalResponse?.message}")
                            // Update the approval action state
                            approvalActionState = TravelApprovalActionState.Error(approvalResponse?.message ?: "Failed to approve request")
                            // Revert the state change if the API call failed
                            loadTravelApprovals() // Reload the data
                        }
                    } else {
                        Log.e("TravelController", "Error approving travel request: ${response.code()} ${response.message()}")
                        // Update the approval action state
                        approvalActionState = TravelApprovalActionState.Error("Error: ${response.code()} ${response.message()}")
                        // Revert the state change if the API call failed
                        loadTravelApprovals() // Reload the data
                    }
                }

                override fun onFailure(
                    call: Call<TravelApprovalActionResponse>,
                    t: Throwable,
                ) {
                    Log.e("TravelController", "Error approving travel request", t)
                    // Update the approval action state
                    approvalActionState = TravelApprovalActionState.Error("Network error: ${t.message ?: "Unknown error"}")
                    // Revert the state change if the API call failed
                    loadTravelApprovals() // Reload the data
                }
            },
        )
    }

    /**
     * Reject a travel request
     */
    fun rejectTravelRequest(
        travelRequestId: String,
        remarks: String = "",
        actionToken: String? = null,
    ) {
        // Set the action state to loading
        approvalActionState = TravelApprovalActionState.Loading

        val currentState = travelApprovalsState
        val request: TravelRequest? =
            when (currentState) {
                is TravelApprovalsState.Success -> currentState.approvalRequests.find { it.id == travelRequestId }
                else -> selectedTravelRequest?.takeIf { it.id == travelRequestId }
            }

        if (request == null) {
            Log.e("TravelController", "Travel request with ID $travelRequestId not found")
            approvalActionState = TravelApprovalActionState.Error("Travel request not found")
            return
        }

        if (currentState is TravelApprovalsState.Success) {
            val updatedRequests =
                currentState.approvalRequests.map {
                    if (it.id == travelRequestId) it.copy(status = TravelStatus.REJECTED) else it
                }
            travelApprovalsState = TravelApprovalsState.Success(updatedRequests)
        }

        // Log the action
        Log.d("TravelController", "Rejecting travel request: $travelRequestId with remarks: $remarks")

        // Get the user email from UserDataManager
        val userEmail = userDataManager.getUserData()?.email?.takeIf { it.isNotBlank() } ?: reportingManagerEmail

        if (userEmail.isBlank()) {
            Log.e("TravelController", "Approver email is empty, cannot reject travel request")
            approvalActionState = TravelApprovalActionState.Error("Approver email not found")
            return
        }

        // Get the action token from the request
        val token = request.actionToken ?: ""

        // Make the API call to reject the request (using the same endpoint as approve)
        val rejectRequest =
            TravelRejectActionRequest(
                email = userEmail,
                requestId = travelRequestId,
                token = token,
                remarks = remarks,
            )

        RetrofitClient.apiService.rejectTravelRequest(rejectRequest).enqueue(
            object : Callback<TravelApprovalActionResponse> {
                override fun onResponse(
                    call: Call<TravelApprovalActionResponse>,
                    response: Response<TravelApprovalActionResponse>,
                ) {
                    if (response.isSuccessful) {
                        val rejectionResponse = response.body()
                        if (rejectionResponse != null && rejectionResponse.status == 200) {
                            Log.d("TravelController", "Successfully rejected travel request: $travelRequestId")
                            // Update the approval action state
                            approvalActionState =
                                TravelApprovalActionState.Success(rejectionResponse.message ?: "Request rejected successfully")
                            // State is already updated, no need to do anything else
                        } else {
                            Log.e("TravelController", "Error rejecting travel request: ${rejectionResponse?.message}")
                            // Update the approval action state
                            approvalActionState = TravelApprovalActionState.Error(rejectionResponse?.message ?: "Failed to reject request")
                            // Revert the state change if the API call failed
                            loadTravelApprovals() // Reload the data
                        }
                    } else {
                        Log.e("TravelController", "Error rejecting travel request: ${response.code()} ${response.message()}")
                        // Update the approval action state
                        approvalActionState = TravelApprovalActionState.Error("Error: ${response.code()} ${response.message()}")
                        // Revert the state change if the API call failed
                        loadTravelApprovals() // Reload the data
                    }
                }

                override fun onFailure(
                    call: Call<TravelApprovalActionResponse>,
                    t: Throwable,
                ) {
                    Log.e("TravelController", "Error rejecting travel request", t)
                    // Update the approval action state
                    approvalActionState = TravelApprovalActionState.Error("Network error: ${t.message ?: "Unknown error"}")
                    // Revert the state change if the API call failed
                    loadTravelApprovals() // Reload the data
                }
            },
        )
    }

    /**
     * Update destination field
     */
    fun updateOriginCity(value: String) {
        originCity = value
    }

    fun updateDestination(value: String) {
        destination = value
    }

    /**
     * Update project name field
     */
    fun updateProjectName(value: String) {
        projectName = value
    }

    /**
     * Update business justification field
     */
    fun updateBusinessJustification(value: String) {
        businessJustification = value
    }


    /**
     * Update mode of transport field
     * If the selected mode is not available for the employee's grade, it will be reset
     */
    fun updateModeOfTransport(value: String) {
        // Check if the selected mode is available for the employee's grade
        if (transportOptions.contains(value)) {
            modeOfTransport = value

            // Set default flight type to "Domestic" when Flight is selected
            if (value == "Flight") {
                flightType = "Domestic"
                // Reset cab fields when switching to flight
                resetCabBookingFields()
            } else if (value == "Cab") {
                // Reset flight-related fields when switching to cab
                flightType = ""
                flightTimePreference = ""
                seatPreference = ""
                frequentFlyerNumber = "0"
                // Don't reset cab fields when switching TO cab mode
                // Set default to Local Travel if not already set
                if (isLocalTravel == false && cabType.isEmpty()) {
                    isLocalTravel = true
                }
            } else {
                // Reset both flight and cab fields for other transport modes
                flightType = ""
                flightTimePreference = ""
                seatPreference = ""
                frequentFlyerNumber = "0"
                resetCabBookingFields()
            }
        }
        // Close dropdown after selection
        isTransportDropdownExpanded = false
    }

    /**
     * Toggle the transport dropdown expanded state
     */
    fun toggleTransportDropdown() {
        isTransportDropdownExpanded = !isTransportDropdownExpanded
    }

    /**
     * Dismiss the transport dropdown
     */
    fun dismissTransportDropdown() {
        isTransportDropdownExpanded = false
    }

    /**
     * Update departure date field
     */
    fun updateDepartureDate(value: String) {
        departureDate = value
    }

    /**
     * Update arrival date field
     */
    fun updateArrivalDate(value: String) {
        arrivalDate = value
    }

    /**
     * Update departure date from date picker millis to avoid timezone issues
     */
    fun updateDepartureDateFromMillis(millis: Long) {
        departureDate = convertMillisToDisplayDateFormat(millis)
    }

    /**
     * Update arrival date from date picker millis to avoid timezone issues
     */
    fun updateArrivalDateFromMillis(millis: Long) {
        arrivalDate = convertMillisToDisplayDateFormat(millis)
    }

    /**
     * Update flight time preference field
     */
    fun updateFlightTimePreference(value: String) {
        flightTimePreference = value
        // Close dropdown after selection
        isFlightTimeDropdownExpanded = false
    }

    /**
     * Toggle the flight time dropdown expanded state
     */
    fun toggleFlightTimeDropdown() {
        isFlightTimeDropdownExpanded = !isFlightTimeDropdownExpanded
    }

    /**
     * Dismiss the flight time dropdown
     */
    fun dismissFlightTimeDropdown() {
        isFlightTimeDropdownExpanded = false
    }

    /**
     * Update flight type preference field
     */
    fun updateFlightType(value: String) {
        flightType = value
        // Close dropdown after selection
        isFlightTypeDropdownExpanded = false
    }

    /**
     * Toggle the flight type dropdown expanded state
     */
    fun toggleFlightTypeDropdown() {
        isFlightTypeDropdownExpanded = !isFlightTypeDropdownExpanded
    }

    /**
     * Dismiss the flight type dropdown
     */
    fun dismissFlightTypeDropdown() {
        isFlightTypeDropdownExpanded = false
    }

    /**
     * Update seat preference field
     */
    fun updateSeatPreference(value: String) {
        seatPreference = value
        // Close dropdown after selection
        isSeatPrefDropdownExpanded = false
    }

    /**
     * Toggle the seat preference dropdown expanded state
     */
    fun toggleSeatPrefDropdown() {
        isSeatPrefDropdownExpanded = !isSeatPrefDropdownExpanded
    }

    /**
     * Dismiss the seat preference dropdown
     */
    fun dismissSeatPrefDropdown() {
        isSeatPrefDropdownExpanded = false
    }

    /**
     * Toggle meal preference enabled state
     */
    fun toggleMealPreference(enabled: Boolean) {
        mealPreferenceEnabled = enabled
        // If disabled, clear the meal preference
        if (!enabled) {
            mealPreference = ""
        }
    }

    /**
     * Update meal preference field
     */
    fun updateMealPreference(value: String) {
        mealPreference = value
        // Close dropdown after selection
        isMealPrefDropdownExpanded = false
    }

    /**
     * Toggle the meal preference dropdown expanded state
     */
    fun toggleMealPrefDropdown() {
        isMealPrefDropdownExpanded = !isMealPrefDropdownExpanded
    }

    /**
     * Dismiss the meal preference dropdown
     */
    fun dismissMealPrefDropdown() {
        isMealPrefDropdownExpanded = false
    }

    /**
     * Toggle stay required state
     */
    fun toggleStayRequired(required: Boolean) {
        stayRequired = required
    }

    /**
     * Update frequent flyer number
     */
    fun updateFrequentFlyerNumber(value: String) {
        frequentFlyerNumber = value
    }

    /**
     * Show frequent flyer number dialog
     */
    fun showFrequentFlyerNumberDialog() {
        showFrequentFlyerDialog = true
    }

    /**
     * Dismiss frequent flyer number dialog
     */
    fun dismissFrequentFlyerNumberDialog() {
        showFrequentFlyerDialog = false
    }

    // State for travel request submission status
    var isSubmitting by mutableStateOf(false)
    var submissionError by mutableStateOf<String?>(null)

    /**
     * Convert display date format (dd MMM yyyy) to API date format (yyyy-MM-dd)
     */
    private fun convertToApiDateFormat(displayDate: String): String =
        try {
            val date = displayDateFormat.parse(displayDate)
            if (date != null) {
                val apiDate = apiDateFormat.format(date)
                Log.d("TravelController", "Converting display date '$displayDate' to API date: '$apiDate'")
                apiDate
            } else {
                displayDate
            }
        } catch (e: Exception) {
            Log.e("TravelController", "Error converting date format", e)
            displayDate // Return original if parsing fails
        }

    /**
     * Convert millis directly to API date format to avoid timezone issues
     * This method fixes the 1-day shift bug by avoiding string parsing and using IST timezone
     */
    private fun convertMillisToApiDateFormat(millis: Long): String =
        try {
            val date = Date(millis)
            apiDateFormat.format(date)
        } catch (e: Exception) {
            Log.e("TravelController", "Error converting millis to API date format", e)
            // Fallback to current date
            apiDateFormat.format(Date())
        }

    /**
     * Convert millis directly to display date format
     */
    private fun convertMillisToDisplayDateFormat(millis: Long): String =
        try {
            val date = Date(millis)
            val formattedDate = displayDateFormat.format(date)
            Log.d("TravelController", "Converting millis $millis to display date: $formattedDate")
            formattedDate
        } catch (e: Exception) {
            Log.e("TravelController", "Error converting millis to display date format", e)
            // Fallback to current date
            displayDateFormat.format(Date())
        }

    /**
     * Submit travel request
     */
    fun submitTravelRequest() {
        // Reset state
        isSubmitting = true
        submissionError = null

        // Extract the flight time value without the time range
        val flightTimeValue =
            when {
                flightTimePreference.contains("Early Morning") -> "Early Morning"
                flightTimePreference.contains("Morning") -> "Morning"
                flightTimePreference.contains("Mid Day") -> "Mid Day"
                flightTimePreference.contains("Night") -> "Night"
                else -> flightTimePreference
            }

        // Create travel request submission object
        val travelRequest =
            if (isMultiDestination) {
                // Multi-destination request
                val travelDetailsList =
                    destinations.map { dest ->
                        // Extract the flight time value for this specific destination
                        val destinationFlightTime =
                            when {
                                dest.flightTimePreference.contains("Early Morning") -> "Early Morning"
                                dest.flightTimePreference.contains("Morning") -> "Morning"
                                dest.flightTimePreference.contains("Mid Day") -> "Mid Day"
                                dest.flightTimePreference.contains("Night") -> "Night"
                                else -> dest.flightTimePreference
                            }

                        TravelDetail(
                            originCity = dest.originCity,
                            destinationCity = dest.destination,
                            departureDate = convertToApiDateFormat(dest.departureDate),
                            arrivalDate = convertToApiDateFormat(dest.returnDate),
                            flightTime = destinationFlightTime,
                        )
                    }

                Log.d("TravelController", "Submitting multi-destination travel request with ${travelDetailsList.size} destinations")

                createMultiDestinationRequest(
                    employeeId = employeeId,
                    employeeName = employeeName,
                    employeeEmail = employeeEmail,
                    mobile = mobileNumber,
                    projectName = projectName,
                    businessJustification = businessJustification,
                    projectId = projectId,
                    opportunityId = opportunityId,
                    crmId = crmId,
                    modeOfTransport = modeOfTransport,
                    reportingManagerName = reportingManagerName,
                    reportingManagerEmail = reportingManagerEmail,
                    stayRequired = stayRequired,
                    grade = employeeGrade,
                    aadharNumber = aadharNumber,
                    dateOfBirth = dateOfBirth,
                    frequentFlyerNumber = frequentFlyerNumber,
                    mealPreference = if (mealPreferenceEnabled) mealPreference else "",
                    seatPreference = seatPreference,
                    travelDetails = travelDetailsList,
                )
            } else {
                // Single destination request
                val apiDepartureDate = convertToApiDateFormat(departureDate)
                val apiArrivalDate = convertToApiDateFormat(arrivalDate)

                Log.d("TravelController", "Submitting single-destination travel request:")
                Log.d("TravelController", "  Display departure date: $departureDate")
                Log.d("TravelController", "  API departure date: $apiDepartureDate")
                Log.d("TravelController", "  Display arrival date: $arrivalDate")
                Log.d("TravelController", "  API arrival date: $apiArrivalDate")

                createSingleDestinationRequest(
                    employeeId = employeeId,
                    employeeName = employeeName,
                    employeeEmail = employeeEmail,
                    mobile = mobileNumber,
                    originCity = originCity,
                    destinationCity = destination,
                    projectName = projectName,
                    businessJustification = businessJustification,
                    projectId = projectId,
                    opportunityId = opportunityId,
                    crmId = crmId,
                    modeOfTransport = modeOfTransport,
                    departureDate = apiDepartureDate,
                    arrivalDate = apiArrivalDate,
                    reportingManagerName = reportingManagerName,
                    reportingManagerEmail = reportingManagerEmail,
                    stayRequired = stayRequired,
                    grade = employeeGrade,
                    aadharNumber = aadharNumber,
                    dateOfBirth = dateOfBirth,
                    frequentFlyerNumber = frequentFlyerNumber,
                    mealPreference = if (mealPreferenceEnabled) mealPreference else "",
                    seatPreference = seatPreference,
                    flightTime = flightTimeValue,
                )
            }

        // Log the travel request being sent for debugging
        Log.d("TravelController", "Submitting travel request:")
        Log.d("TravelController", "  Employee Name: ${travelRequest.employeeName}")
        Log.d("TravelController", "  Employee Email: ${travelRequest.employeeEmail}")
        Log.d("TravelController", "  Employee ID: ${travelRequest.employeeId}")
        Log.d("TravelController", "  Grade: ${travelRequest.grade}")
        Log.d("TravelController", "  Aadhar Number: ${travelRequest.aadharNumber}")
        Log.d("TravelController", "  Date of Birth: ${travelRequest.dateOfBirth}")
        Log.d("TravelController", "  Mobile: ${travelRequest.mobile}")
        Log.d("TravelController", "  Project Name: ${travelRequest.projectName}")
        Log.d("TravelController", "  Business Justification: ${travelRequest.businessJustification}")
        Log.d("TravelController", "  Mode of Transport: ${travelRequest.modeOfTransport}")
        Log.d("TravelController", "  Stay Required: ${travelRequest.stayRequired}")
        Log.d("TravelController", "  Meal Preference: ${travelRequest.mealPref}")
        Log.d("TravelController", "  Seat Preference: ${travelRequest.seatPref}")
        Log.d("TravelController", "  Frequent Flyer Number: ${travelRequest.frequentFlyerNum}")
        Log.d("TravelController", "  Reporting Manager Name: ${travelRequest.reportingManagerName}")
        Log.d("TravelController", "  Reporting Manager Email: ${travelRequest.reportingManagerEmail}")
        Log.d("TravelController", "  Multi Travel: ${travelRequest.multiTravel}")
        Log.d("TravelController", "  Travel Details Count: ${travelRequest.travelDetails.size}")
        travelRequest.travelDetails.forEachIndexed { index, detail ->
            Log.d("TravelController", "    Detail $index:")
            Log.d("TravelController", "      Origin City: ${detail.originCity}")
            Log.d("TravelController", "      Destination City: ${detail.destinationCity}")
            Log.d("TravelController", "      Departure Date: ${detail.departureDate}")
            Log.d("TravelController", "      Arrival Date: ${detail.arrivalDate}")
            Log.d("TravelController", "      Flight Time: ${detail.flightTime}")
        }

        // Log the serialized JSON for debugging
        try {
            val gson = com.google.gson.Gson()
            val jsonString = gson.toJson(travelRequest)
            Log.d("TravelController", "Serialized JSON: $jsonString")
        } catch (e: Exception) {
            Log.e("TravelController", "Error serializing request to JSON", e)
        }

        // Make API call
        RetrofitClient.apiService.submitTravelRequest(travelRequest).enqueue(
            object : Callback<TravelRequestResponse> {
                override fun onResponse(
                    call: Call<TravelRequestResponse>,
                    response: Response<TravelRequestResponse>,
                ) {
                    isSubmitting = false
                    Log.d("TravelController", "Received response - Code: ${response.code()}, Success: ${response.isSuccessful}")

                    try {
                        Log.d("TravelController", "Response body is null: ${response.body() == null}")

                        if (response.isSuccessful && response.body() != null) {
                            val responseBody = response.body()!!
                            Log.d("TravelController", "Response status: ${responseBody.status}")
                            Log.d("TravelController", "Response message: ${responseBody.message}")

                            if (responseBody.status == 200) {
                                // Request was successful
                                Log.d("TravelController", "Travel request submitted successfully")

                                // Show success toast message
                                CustomToast.show(context, "Travel request submitted successfully!")

                                // Log multi-destination details if available
                                responseBody.getAllOrderHistory()?.firstOrNull()?.let { order ->
                                    Log.d("TravelController", "Request ID: ${order.requestId}")
                                    order.travelDetails?.let { details ->
                                        Log.d("TravelController", "Travel details: ${details.size} destinations")
                                        details.forEach { dest ->
                                            Log.d(
                                                "TravelController",
                                                "  - ${dest.travelDestination}: ${dest.departureDate} to ${dest.arrivalDate}",
                                            )
                                        }
                                    }
                                }

                                // Clear any previous errors and navigate to travel history
                                submissionError = null
                                // Refresh travel history to show the new request
                                loadCombinedTravelHistory()
                                navigator.navigateToTravelHistory()
                            } else {
                                // Server returned an error
                                submissionError = responseBody.message
                                Log.e("TravelController", "Error submitting travel request: ${responseBody.message}")
                            }
                        } else {
                            // HTTP error
                            try {
                                val errorBody = response.errorBody()?.string()
                                submissionError = "Failed to submit travel request. Please try again."
                                Log.e("TravelController", "HTTP error: ${response.code()}, Error body: $errorBody")

                                // Also try to log the raw response if available
                                if (response.body() == null) {
                                    Log.e("TravelController", "Response body is null - likely parsing error")
                                } else {
                                    Log.e("TravelController", "Response body exists but response not successful")
                                }
                            } catch (e: Exception) {
                                submissionError = "Failed to submit travel request. Please try again."
                                Log.e("TravelController", "HTTP error: ${response.code()}, Error reading error body", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TravelController", "Exception in onResponse: ${e.message}", e)
                        submissionError = "Error processing response. Please try again."
                    }
                }

                override fun onFailure(
                    call: Call<TravelRequestResponse>,
                    t: Throwable,
                ) {
                    isSubmitting = false

                    // Check if this is a JSON parsing error after a successful request
                    if (t is com.google.gson.JsonSyntaxException &&
                        (
                            t.message?.contains("Expected an int but was BOOLEAN") == true ||
                                t.message?.contains("stay_required") == true
                        )
                    ) {
                        // This means the request was successful but response parsing failed
                        // Suppress the error and show success
                        Log.d("TravelController", "Request successful but response parsing failed - treating as success")

                        // Show success toast message
                        CustomToast.show(context, "Travel request submitted successfully!")

                        submissionError = null
                        // Refresh travel history to show the new request
                        loadCombinedTravelHistory()
                        navigator.navigateToTravelHistory()
                    } else {
                        // Genuine network error
                        submissionError = "Network error. Please check your connection and try again."
                        Log.e("TravelController", "Network error submitting travel request", t)
                        Log.e("TravelController", "Error type: ${t.javaClass.simpleName}")
                        Log.e("TravelController", "Error message: ${t.message}")
                        Log.e("TravelController", "Error cause: ${t.cause}")
                    }
                }
            },
        )
    }

    // Multi-destination functions

    /**
     * Toggle between single and multi-destination mode
     */
    fun toggleDestinationMode(isMulti: Boolean) {
        isMultiDestination = isMulti
        if (isMulti && destinations.isEmpty()) {
            // Initialize with one destination
            destinations =
                listOf(
                    Destination(
                        originCity = "",
                        destination = "",
                        departureDate = currentDate,
                        returnDate = currentDate,
                        flightTimePreference = "",
                    ),
                )
        }
    }

    /**
     * Add a new destination to the list
     */
    fun addDestination() {
        destinations = destinations +
            Destination(
                originCity = "",
                destination = "",
                departureDate = currentDate,
                returnDate = currentDate,
                flightTimePreference = "",
            )
    }

    /**
     * Remove a destination by ID
     */
    fun removeDestination(destinationId: String) {
        destinations = destinations.filter { it.id != destinationId }
    }

    /**
     * Update origin city field for a specific destination
     */
    fun updateDestinationOriginCity(
        destinationId: String,
        value: String,
    ) {
        destinations =
            destinations.map { destination ->
                if (destination.id == destinationId) {
                    destination.copy(originCity = value)
                } else {
                    destination
                }
            }
    }

    /**
     * Update flight time preference for a specific destination
     */
    fun updateDestinationFlightTimePreference(
        destinationId: String,
        value: String,
    ) {
        destinations =
            destinations.map { destination ->
                if (destination.id == destinationId) {
                    destination.copy(flightTimePreference = value)
                } else {
                    destination
                }
            }
    }

    /**
     * Update destination field for a specific destination
     */
    fun updateDestinationField(
        destinationId: String,
        value: String,
    ) {
        destinations =
            destinations.map { destination ->
                if (destination.id == destinationId) {
                    destination.copy(destination = value)
                } else {
                    destination
                }
            }
    }

    /**
     * Update departure date for a specific destination
     */
    fun updateDestinationDepartureDate(
        destinationId: String,
        value: String,
    ) {
        destinations =
            destinations.map { destination ->
                if (destination.id == destinationId) {
                    destination.copy(departureDate = value)
                } else {
                    destination
                }
            }
    }

    /**
     * Update departure date from millis for a specific destination
     */
    fun updateDestinationDepartureDateFromMillis(
        destinationId: String,
        millis: Long,
    ) {
        val formattedDate = convertMillisToDisplayDateFormat(millis)
        updateDestinationDepartureDate(destinationId, formattedDate)
    }

    /**
     * Update return date for a specific destination
     */
    fun updateDestinationReturnDate(
        destinationId: String,
        value: String,
    ) {
        destinations =
            destinations.map { destination ->
                if (destination.id == destinationId) {
                    destination.copy(returnDate = value)
                } else {
                    destination
                }
            }
    }

    /**
     * Update return date from millis for a specific destination
     */
    fun updateDestinationReturnDateFromMillis(
        destinationId: String,
        millis: Long,
    ) {
        val formattedDate = convertMillisToDisplayDateFormat(millis)
        updateDestinationReturnDate(destinationId, formattedDate)
    }

    /**
     * Explicitly set the selected travel request when navigating via Intent-based
     * activities (e.g. TravelApproveActivity / TravelRejectActivity).
     */
    fun selectTravelRequest(request: TravelRequest) {
        selectedTravelRequest = request
    }

    /**
     * Handle back navigation from Travel-related screens so that the
     * top-left back arrow behaves like the system back gesture.
     */
    fun onBackPressed() {
        navigator.popBackStack()
    }

    // Cab booking methods

    /**
     * Update local travel selection
     */
    fun updateLocalTravel(isLocal: Boolean) {
        isLocalTravel = isLocal
        // Reset passenger count to 1 when changing travel type
        if (isLocal && passengerCount == 1) {
            // Keep validation visible
        }
    }

    /**
     * Update travel type from dropdown selection
     */
    fun updateTravelType(travelType: String) {
        isLocalTravel = travelType == "Local Travel"
        isTravelTypeDropdownExpanded = false
    }

    /**
     * Toggle travel type dropdown
     */
    fun toggleTravelTypeDropdown() {
        isTravelTypeDropdownExpanded = !isTravelTypeDropdownExpanded
    }

    /**
     * Dismiss travel type dropdown
     */
    fun dismissTravelTypeDropdown() {
        isTravelTypeDropdownExpanded = false
    }

    /**
     * Update passenger count
     */
    fun updatePassengerCount(count: Int) {
        passengerCount = count
    }

    /**
     * Validate cab booking rules
     */
    fun validateCabBooking(): String? =
        if (isLocalTravel && passengerCount == 1) {
            "Single passenger not allowed for local travel"
        } else {
            null
        }

    /**
     * Update cab type
     */
    fun updateCabType(type: String) {
        cabType = type
        isCabTypeDropdownExpanded = false
    }

    /**
     * Toggle cab type dropdown
     */
    fun toggleCabTypeDropdown() {
        isCabTypeDropdownExpanded = !isCabTypeDropdownExpanded
    }

    /**
     * Dismiss cab type dropdown
     */
    fun dismissCabTypeDropdown() {
        isCabTypeDropdownExpanded = false
    }

    /**
     * Update cab duration
     */
    fun updateCabDuration(duration: String) {
        cabDuration = duration
        isCabDurationDropdownExpanded = false
    }

    /**
     * Toggle cab duration dropdown
     */
    fun toggleCabDurationDropdown() {
        isCabDurationDropdownExpanded = !isCabDurationDropdownExpanded
    }

    /**
     * Dismiss cab duration dropdown
     */
    fun dismissCabDurationDropdown() {
        isCabDurationDropdownExpanded = false
    }

    /**
     * Update pickup location
     */
    fun updatePickupLocation(location: String) {
        pickupLocation = location
    }

    /**
     * Update final drop location
     */
    fun updateFinalDropLocation(location: String) {
        finalDropLocation = location
    }

    /**
     * Add new visit point (max 3 allowed)
     */
    fun addVisitPoint() {
        if (visitPoints.size < 3) {
            val newPoint =
                VisitPoint(
                    order = visitPoints.size + 1,
                )
            visitPoints = visitPoints + newPoint
        }
    }

    /**
     * Remove visit point by ID
     */
    fun removeVisitPoint(pointId: String) {
        visitPoints =
            visitPoints
                .filter { it.id != pointId }
                .mapIndexed { index, point ->
                    point.copy(order = index + 1)
                }
    }

    /**
     * Update visit point location
     */
    fun updateVisitPointLocation(
        pointId: String,
        location: String,
    ) {
        visitPoints =
            visitPoints.map { point ->
                if (point.id == pointId) {
                    point.copy(location = location)
                } else {
                    point
                }
            }
    }

    /**
     * Update map details
     */
    fun updateMapDetails(details: String) {
        mapDetails = details
    }

    // Enhanced cab booking methods

    /**
     * Update project ID
     */
    fun updateProjectId(value: String) {
        projectId = value
    }

    /**
     * Update opportunity ID
     */
    fun updateOpportunityId(value: String) {
        opportunityId = value
    }

    /**
     * Update CRM ID
     */
    fun updateCrmId(value: String) {
        crmId = value
    }

    /**
     * Update cab travel date
     */
    fun updateCabTravelDate(value: String) {
        cabTravelDate = value
    }

    /**
     * Update cab travel date from date picker millis
     */
    fun updateCabTravelDateFromMillis(millis: Long) {
        cabTravelDate = convertMillisToDisplayDateFormat(millis)
    }

    /**
     * Update attendee search query
     */
    fun updateAttendeeSearchQuery(value: String) {
        attendeeSearchQuery = value
        // Clear old employee search results when query changes
        if (value.length < 2) {
            employeeSearchResults = emptyList()
            suggestedUsers = emptyList()
        }
    }

    /**
     * Search for employees to add as attendees
     */
    fun searchEmployees(query: String) {
        if (query.length < 3) return

        isSearchingEmployees = true
        val searchRequest =
            EmployeeSearchRequest(
                query = query,
                searchType = "name",
                limit = 10,
            )

        RetrofitClient.apiService.searchEmployees(searchRequest).enqueue(
            object : Callback<EmployeeSearchResponse> {
                override fun onResponse(
                    call: Call<EmployeeSearchResponse>,
                    response: Response<EmployeeSearchResponse>,
                ) {
                    isSearchingEmployees = false
                    if (response.isSuccessful && response.body() != null) {
                        val searchResponse = response.body()!!
                        if (searchResponse.status == 200) {
                            employeeSearchResults = searchResponse.employees
                        } else {
                            employeeSearchResults = emptyList()
                        }
                    } else {
                        employeeSearchResults = emptyList()
                        Log.e("TravelController", "Error searching employees: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<EmployeeSearchResponse>,
                    t: Throwable,
                ) {
                    isSearchingEmployees = false
                    employeeSearchResults = emptyList()
                    Log.e("TravelController", "Network error searching employees", t)
                }
            },
        )
    }

    /**
     * Search for suggested users using the simpler suggest-users API
     */
    fun searchSuggestedUsers(query: String) {
        if (query.length < 2) {
            suggestedUsers = emptyList()
            return
        }

        isSearchingSuggestedUsers = true

        RetrofitClient.apiService.suggestUsers(query).enqueue(
            object : Callback<List<SuggestedUser>> {
                override fun onResponse(
                    call: Call<List<SuggestedUser>>,
                    response: Response<List<SuggestedUser>>,
                ) {
                    isSearchingSuggestedUsers = false
                    if (response.isSuccessful && response.body() != null) {
                        suggestedUsers = response.body()!!
                    } else {
                        suggestedUsers = emptyList()
                        Log.e("TravelController", "Error searching suggested users: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<List<SuggestedUser>>,
                    t: Throwable,
                ) {
                    isSearchingSuggestedUsers = false
                    suggestedUsers = emptyList()
                    Log.e("TravelController", "Network error searching suggested users", t)
                }
            },
        )
    }

    /**
     * Add attendee from suggested users search results
     */
    fun addAttendeeFromSuggestedUser(suggestedUser: SuggestedUser) {
        val attendee =
            CabAttendee(
                name = suggestedUser.displayName,
                email = suggestedUser.mail,
                employeeId = null, // Not provided by suggest-users API
                department = null, // Not provided by suggest-users API
            )

        val currentAttendees = additionalAttendees.toMutableList()

        // Check if already added
        if (!currentAttendees.any { it.email == attendee.email }) {
            currentAttendees.add(attendee)
            additionalAttendees = currentAttendees
        }

        // Clear search results
        suggestedUsers = emptyList()
    }

    /**
     * Add attendee from search results
     */
    fun addAttendee(employee: EmployeeSearchResult) {
        val attendee =
            CabAttendee(
                name = employee.name,
                employeeId = employee.employeeId,
                email = employee.email,
                department = employee.department,
            )
        additionalAttendees = additionalAttendees + attendee
        attendeeSearchQuery = ""
        employeeSearchResults = emptyList()
        showAttendeeSearch = false
    }

    /**
     * Remove attendee by index
     */
    fun removeAttendee(index: Int) {
        additionalAttendees = additionalAttendees.filterIndexed { i, _ -> i != index }
    }

    /**
     * Toggle attendee search visibility
     */
    fun toggleAttendeeSearch() {
        showAttendeeSearch = !showAttendeeSearch
        if (!showAttendeeSearch) {
            attendeeSearchQuery = ""
            employeeSearchResults = emptyList()
        }
    }

    /**
     * Add pickup location
     */
    fun addPickupLocation() {
        val newLocation =
            CabLocation(
                address = "",
                order = cabPickupLocations.size + 1,
                isPickup = true,
            )
        cabPickupLocations = cabPickupLocations + newLocation
    }

    /**
     * Remove pickup location by ID
     */
    fun removePickupLocation(locationId: String) {
        cabPickupLocations =
            cabPickupLocations
                .filter { it.id != locationId }
                .mapIndexed { index, location ->
                    location.copy(order = index + 1)
                }
    }

    /**
     * Update pickup location address
     */
    fun updatePickupLocationAddress(
        locationId: String,
        address: String,
    ) {
        cabPickupLocations =
            cabPickupLocations.map { location ->
                if (location.id == locationId) {
                    location.copy(address = address)
                } else {
                    location
                }
            }
    }

    /**
     * Submit enhanced cab booking request
     */
    fun submitCabBookingRequest() {
        // Reset state
        isCabSubmitting = true
        cabSubmissionError = null

        // Validate required fields
        if (businessJustification.isBlank()) {
            cabSubmissionError = "Business justification is required"
            isCabSubmitting = false
            return
        }

        if (cabType.isBlank()) {
            cabSubmissionError = "Please select cab type"
            isCabSubmitting = false
            return
        }

        if (cabDuration.isBlank()) {
            cabSubmissionError = "Please select duration"
            isCabSubmitting = false
            return
        }

        if (pickupLocation.isBlank() && cabPickupLocations.isEmpty()) {
            cabSubmissionError = "Please provide pickup location"
            isCabSubmitting = false
            return
        }

        if (finalDropLocation.isBlank()) {
            cabSubmissionError = "Please provide drop location"
            isCabSubmitting = false
            return
        }

        // Create pickup locations list
        val allPickupLocations =
            if (pickupLocation.isNotBlank()) {
                listOf(CabLocation(address = pickupLocation, order = 1, isPickup = true)) + cabPickupLocations
            } else {
                cabPickupLocations
            }

        // Create cab booking request
        val cabRequest =
            createCabBookingRequest(
                employeeId = employeeId,
                employeeName = employeeName,
                employeeEmail = employeeEmail,
                mobile = mobileNumber,
                projectName = projectName.takeIf { it.isNotBlank() },
                projectId = projectId.takeIf { it.isNotBlank() },
                opportunityId = opportunityId.takeIf { it.isNotBlank() },
                crmId = crmId.takeIf { it.isNotBlank() },
                businessJustification = businessJustification,
                travelType = if (isLocalTravel) "Local Travel" else "Out of Local Station",
                travelDate = convertToApiDateFormat(cabTravelDate),
                passengerCount = passengerCount,
                additionalAttendees = additionalAttendees,
                cabType = cabType,
                duration = cabDuration,
                pickupLocations = allPickupLocations,
                dropLocation = finalDropLocation,
                reportingManagerName = reportingManagerName,
                reportingManagerEmail = reportingManagerEmail,
                grade = employeeGrade,
                aadharNumber = aadharNumber,
                dateOfBirth = dateOfBirth,
            )

        // Log the request for debugging
        Log.d("TravelController", "Submitting cab booking request:")
        Log.d("TravelController", "  Employee: ${cabRequest.employeeName}")
        Log.d("TravelController", "  Travel Type: ${cabRequest.travelType}")
        Log.d("TravelController", "  Date: ${cabRequest.travelDate}")
        Log.d("TravelController", "  Passengers: ${cabRequest.passengerCount}")
        Log.d("TravelController", "  Cab Type: ${cabRequest.cabType}")
        Log.d("TravelController", "  Duration: ${cabRequest.duration}")
        Log.d("TravelController", "  Pickup Locations: ${cabRequest.pickupLocations.size}")
        Log.d("TravelController", "  Additional Attendees: ${cabRequest.additionalAttendees.size}")

        // Make API call
        RetrofitClient.apiService.submitCabBooking(cabRequest).enqueue(
            object : Callback<CabBookingResponse> {
                override fun onResponse(
                    call: Call<CabBookingResponse>,
                    response: Response<CabBookingResponse>,
                ) {
                    isCabSubmitting = false
                    if (response.isSuccessful && response.body() != null) {
                        val cabResponse = response.body()!!
                        if (cabResponse.status == 200) {
                            // Success
                            CustomToast.show(context, "Cab booking request submitted successfully!")
                            cabSubmissionError = null

                            // Navigate to travel history
                            loadCombinedTravelHistory()
                            navigator.navigateToTravelHistory()
                        } else {
                            cabSubmissionError = cabResponse.message
                            Log.e("TravelController", "Error submitting cab booking: ${cabResponse.message}")
                        }
                    } else {
                        cabSubmissionError = "Failed to submit cab booking. Please try again."
                        Log.e("TravelController", "HTTP error: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<CabBookingResponse>,
                    t: Throwable,
                ) {
                    isCabSubmitting = false
                    cabSubmissionError = "Network error. Please check your connection and try again."
                    Log.e("TravelController", "Network error submitting cab booking", t)
                }
            },
        )
    }

    /**
     * Reset cab booking fields when switching away from cab mode
     */
    private fun resetCabBookingFields() {
        isLocalTravel = false
        passengerCount = 1
        cabType = ""
        cabDuration = ""
        pickupLocation = ""
        finalDropLocation = ""
        visitPoints = emptyList()
        mapDetails = ""

        // Reset enhanced cab booking fields
        projectId = ""
        opportunityId = ""
        crmId = ""
        cabTravelDate = currentDate
        attendeeSearchQuery = ""
        additionalAttendees = emptyList()
        cabPickupLocations = emptyList()
        employeeSearchResults = emptyList()
        suggestedUsers = emptyList()
        showAttendeeSearch = false
        cabSubmissionError = null

        // Reset dropdown states
        isCabTypeDropdownExpanded = false
        isCabDurationDropdownExpanded = false
        isTravelTypeDropdownExpanded = false
    }
}

/**
 * State representing the travel history screen UI state
 */
sealed class TravelHistoryState {
    object Loading : TravelHistoryState()

    data class Success(
        val travelRequests: List<TravelRequest>,
    ) : TravelHistoryState()

    data class Error(
        val message: String,
    ) : TravelHistoryState()
}
