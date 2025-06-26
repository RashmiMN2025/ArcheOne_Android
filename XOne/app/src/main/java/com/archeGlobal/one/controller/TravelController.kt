package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.TravelApprovalActionRequest
import com.archeGlobal.one.model.TravelApprovalActionResponse
import com.archeGlobal.one.model.TravelCombinedHistoryResponse
import com.archeGlobal.one.model.TravelHistoryRequest
import com.archeGlobal.one.model.TravelHistoryResponse
import com.archeGlobal.one.model.TravelRejectActionRequest
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelRequestResponse
import com.archeGlobal.one.model.TravelRequestSubmission
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controller for the Travel screens following MVC architecture
 */
class TravelController(private val navigator: Navigator, private val context: Context) {

    /**
     * Sealed class representing the state of travel history
     */
    sealed class TravelHistoryState {
        object Loading : TravelHistoryState()
        data class Success(
            val historyItems: List<TravelRequest>,
            val approvalItems: List<TravelRequest> = emptyList()
        ) : TravelHistoryState()
        data class Error(val message: String) : TravelHistoryState()
    }

    /**
     * Sealed class representing the state of travel approvals
     */
    sealed class TravelApprovalsState {
        object Loading : TravelApprovalsState()
        data class Success(val approvalRequests: List<TravelRequest>) : TravelApprovalsState()
        data class Error(val message: String) : TravelApprovalsState()
    }

    // State for travel approval actions
    sealed class TravelApprovalActionState {
        object Idle : TravelApprovalActionState()
        object Loading : TravelApprovalActionState()
        data class Success(val message: String) : TravelApprovalActionState()
        data class Error(val message: String) : TravelApprovalActionState()
    }

    // Date formatters
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // UI state
    var travelHistoryState by mutableStateOf<TravelHistoryState>(TravelHistoryState.Loading)
        private set

    // Travel approvals state
    var travelApprovalsState by mutableStateOf<TravelApprovalsState>(TravelApprovalsState.Loading)
        private set

    // Selected travel request for detail view
    var selectedTravelRequest by mutableStateOf<TravelRequest?>(null)
        private set

    // Travel approval action state
    var approvalActionState by mutableStateOf<TravelApprovalActionState>(TravelApprovalActionState.Idle)
        private set

    // Count of pending travel approvals
    var pendingApprovalCount by mutableStateOf(0)
        private set

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

    // Travel form fields
    var destination by mutableStateOf("")
        private set
    var projectName by mutableStateOf("")
        private set
    var businessJustification by mutableStateOf("")
        private set

    // Mode of transport options based on employee grade
    val transportOptions: List<String>
        get() {
            // Extract grade as a number if possible
            val gradeNumber = employeeGrade.replace("Grade ", "").toIntOrNull() ?: 0

            // For grade 6 and above, include flight option
            return if (gradeNumber >= 6) {
                listOf("Bus", "Flight", "Train")
            } else {
                listOf("Bus", "Train")
            }
        }
    var modeOfTransport by mutableStateOf("")
        private set
    var isTransportDropdownExpanded by mutableStateOf(false)
        private set

    // Initialize with current date
    private val currentDateFormatter = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
    private val currentDate = currentDateFormatter.format(Date())

    var departureDate by mutableStateOf(currentDate)
        private set
    var arrivalDate by mutableStateOf(currentDate)
        private set

    // Flight time preference options
    val flightTimeOptions = listOf(
        "Early Morning (00:00-06:00)",
        "Morning (06:00-12:00)",
        "Mid Day (12:00-18:00)",
        "Night (18:00-23:00)"
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
    val mealPreferenceOptions = listOf("Veg", "Non-Veg", "Jain")
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

    init {
        loadEmployeeDetails()
        loadCombinedTravelHistory()
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
        val request = TravelHistoryRequest(
            employeeId = employeeId,
            employeeEmail = employeeEmail
        )

        // Make API call to get travel history
        RetrofitClient.apiService.getTravelHistory(request).enqueue(object : Callback<TravelHistoryResponse> {
            override fun onResponse(call: Call<TravelHistoryResponse>, response: Response<TravelHistoryResponse>) {
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

            override fun onFailure(call: Call<TravelHistoryResponse>, t: Throwable) {
                Log.e("TravelController", "Network error loading travel history", t)
                travelHistoryState = TravelHistoryState.Error("Network error. Please check your connection and try again.")
            }
        })
    }

    /**
     * Load combined travel history for the current user
     * This uses the new combined history API endpoint
     */
    fun loadCombinedTravelHistory() {
        // Set to loading state
        travelHistoryState = TravelHistoryState.Loading

        // Create request with employee email (new API only needs email)
        val request = TravelHistoryRequest(
            employeeId = employeeId,
            employeeEmail = employeeEmail
        )

        // Make API call to get combined travel history
        RetrofitClient.apiService.getTravelCombinedHistory(request).enqueue(object : Callback<TravelCombinedHistoryResponse> {
            override fun onResponse(call: Call<TravelCombinedHistoryResponse>, response: Response<TravelCombinedHistoryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val orderHistoryItems = response.body()!!.orderHistory.map { it.toTravelRequest() }
                    val approvalHistoryItems = response.body()!!.approvalHistory.map { it.toTravelRequest() }

                    // Update state with both order history and approval history
                    travelHistoryState = TravelHistoryState.Success(
                        historyItems = orderHistoryItems,
                        approvalItems = approvalHistoryItems
                    )

                    Log.d("TravelController", "Loaded combined history: ${orderHistoryItems.size} orders, ${approvalHistoryItems.size} approvals")
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("TravelController", "Error loading combined history: ${response.code()}, Error: $errorBody")
                        travelHistoryState = TravelHistoryState.Error("Failed to load travel history. Please try again.")
                    } catch (e: Exception) {
                        Log.e("TravelController", "Error parsing error response", e)
                        travelHistoryState = TravelHistoryState.Error("Failed to load travel history. Please try again.")
                    }
                }
            }

            override fun onFailure(call: Call<TravelCombinedHistoryResponse>, t: Throwable) {
                Log.e("TravelController", "Network error loading combined history", t)
                travelHistoryState = TravelHistoryState.Error("Network error. Please check your connection and try again.")
            }
        })
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
            // When in travel request detail, navigate back to travel history
            navigator.navigateToTravel()
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
     * Navigate to travel request details screen
     */
    fun navigateToTravelDetails(travelRequestId: String) {
        // Find the travel request with the given ID from the current state
        val currentState = travelHistoryState
        if (currentState is TravelHistoryState.Success) {
            // Search in both history items and approval items
            val request = currentState.historyItems.find { it.id == travelRequestId } ?: currentState.approvalItems.find { it.id == travelRequestId }

            if (request != null) {
                // Store the selected travel request
                selectedTravelRequest = request
                // Navigate to the detail screen
                navigator.navigateToTravelRequestDetail()
            } else {
                Log.e("TravelController", "Travel request with ID $travelRequestId not found")
            }
        } else {
            Log.e("TravelController", "Cannot navigate to travel details: travel history not loaded")
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

        // If the request is already processed (approved / rejected) just show the read-only
        // details page that we reuse from travel history. Otherwise open the approval page
        if (travelRequest.status == TravelStatus.APPROVED || travelRequest.status == TravelStatus.REJECTED) {
            navigator.navigateToTravelApprovalDetails()
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
    fun hasPendingApprovals(): Boolean {
        return pendingApprovalCount > 0
    }

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
        // Set state to loading
        travelApprovalsState = TravelApprovalsState.Loading

        // Get the user's email from UserDataManager
        val userEmail = userDataManager.getUserData()?.email?.takeIf { it.isNotBlank() } ?: reportingManagerEmail

        if (userEmail.isBlank()) {
            travelApprovalsState = TravelApprovalsState.Error("User email not found")
            return
        }

        // Create the request body for combined history
        val request = TravelHistoryRequest(employeeId = "", employeeEmail = userEmail)

        // Make the API call using combined history endpoint
        RetrofitClient.apiService.getTravelCombinedHistory(request).enqueue(object : Callback<TravelCombinedHistoryResponse> {
            override fun onResponse(call: Call<TravelCombinedHistoryResponse>, response: Response<TravelCombinedHistoryResponse>) {
                if (response.isSuccessful) {
                    val combinedResponse = response.body()
                    if (combinedResponse != null && combinedResponse.status == 200) {
                        // Convert API response to UI models (only approval history)
                        val approvalRequests = combinedResponse.approvalHistory.map { it.toTravelRequest() }
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

            override fun onFailure(call: Call<TravelCombinedHistoryResponse>, t: Throwable) {
                Log.e("TravelController", "Error loading travel approvals", t)
                travelApprovalsState = TravelApprovalsState.Error("Network error: ${t.message}")
            }
        })
    }

    /**
     * Approve a travel request
     */
    fun approveTravelRequest(travelRequestId: String, remarks: String = "") {
        // Set the action state to loading
        approvalActionState = TravelApprovalActionState.Loading

        val currentState = travelApprovalsState
        val request: TravelRequest? = when (currentState) {
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
            val updatedRequests = currentState.approvalRequests.map {
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
        val approveRequest = TravelApprovalActionRequest(
            email = userEmail,
            requestId = travelRequestId,
            token = token
        )

        RetrofitClient.apiService.approveTravelRequest(approveRequest).enqueue(object : Callback<TravelApprovalActionResponse> {
            override fun onResponse(call: Call<TravelApprovalActionResponse>, response: Response<TravelApprovalActionResponse>) {
                if (response.isSuccessful) {
                    val approvalResponse = response.body()
                    if (approvalResponse != null && approvalResponse.status == 200) {
                        Log.d("TravelController", "Successfully approved travel request: $travelRequestId")
                        // Update the approval action state
                        approvalActionState = TravelApprovalActionState.Success(approvalResponse.message ?: "Request approved successfully")
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

            override fun onFailure(call: Call<TravelApprovalActionResponse>, t: Throwable) {
                Log.e("TravelController", "Error approving travel request", t)
                // Update the approval action state
                approvalActionState = TravelApprovalActionState.Error("Network error: ${t.message ?: "Unknown error"}")
                // Revert the state change if the API call failed
                loadTravelApprovals() // Reload the data
            }
        })
    }

    /**
     * Reject a travel request
     */
    fun rejectTravelRequest(travelRequestId: String, remarks: String = "", actionToken: String? = null) {
        // Set the action state to loading
        approvalActionState = TravelApprovalActionState.Loading

        val currentState = travelApprovalsState
        val request: TravelRequest? = when (currentState) {
            is TravelApprovalsState.Success -> currentState.approvalRequests.find { it.id == travelRequestId }
            else -> selectedTravelRequest?.takeIf { it.id == travelRequestId }
        }

        if (request == null) {
            Log.e("TravelController", "Travel request with ID $travelRequestId not found")
            approvalActionState = TravelApprovalActionState.Error("Travel request not found")
            return
        }

        if (currentState is TravelApprovalsState.Success) {
            val updatedRequests = currentState.approvalRequests.map {
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
        val rejectRequest = TravelRejectActionRequest(
            email = userEmail,
            requestId = travelRequestId,
            token = token,
            remarks = remarks
        )

        RetrofitClient.apiService.rejectTravelRequest(rejectRequest).enqueue(object : Callback<TravelApprovalActionResponse> {
            override fun onResponse(call: Call<TravelApprovalActionResponse>, response: Response<TravelApprovalActionResponse>) {
                if (response.isSuccessful) {
                    val rejectionResponse = response.body()
                    if (rejectionResponse != null && rejectionResponse.status == 200) {
                        Log.d("TravelController", "Successfully rejected travel request: $travelRequestId")
                        // Update the approval action state
                        approvalActionState = TravelApprovalActionState.Success(rejectionResponse.message ?: "Request rejected successfully")
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

            override fun onFailure(call: Call<TravelApprovalActionResponse>, t: Throwable) {
                Log.e("TravelController", "Error rejecting travel request", t)
                // Update the approval action state
                approvalActionState = TravelApprovalActionState.Error("Network error: ${t.message ?: "Unknown error"}")
                // Revert the state change if the API call failed
                loadTravelApprovals() // Reload the data
            }
        })
    }

    /**
     * Update destination field
     */
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

            // Reset flight-related fields if mode is not Flight
            if (value != "Flight") {
                flightTimePreference = ""
                seatPreference = ""
                frequentFlyerNumber = "0"
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
    private fun convertToApiDateFormat(displayDate: String): String {
        return try {
            val date = displayDateFormat.parse(displayDate)
            date?.let { apiDateFormat.format(it) } ?: displayDate
        } catch (e: Exception) {
            Log.e("TravelController", "Error converting date format", e)
            displayDate // Return original if parsing fails
        }
    }

    /**
     * Submit travel request
     */
    fun submitTravelRequest() {
        // Reset state
        isSubmitting = true
        submissionError = null

        // Extract the flight time value without the time range
        val flightTimeValue = when {
            flightTimePreference.contains("Early Morning") -> "Early Morning"
            flightTimePreference.contains("Morning") -> "Morning"
            flightTimePreference.contains("Mid Day") -> "Mid Day"
            flightTimePreference.contains("Night") -> "Night"
            else -> flightTimePreference
        }

        // Create travel request submission object
        val travelRequest = TravelRequestSubmission(
            employeeId = employeeId,
            employeeName = employeeName,
            employeeEmail = employeeEmail,
            mobile = mobileNumber,
            travelDestination = destination,
            projectName = projectName,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = convertToApiDateFormat(departureDate),
            arrivalDate = convertToApiDateFormat(arrivalDate),
            reportingManagerName = reportingManagerName,
            reportingManagerEmail = reportingManagerEmail,
            grade = employeeGrade,
            aadharNumber = aadharNumber,
            dateOfBirth = dateOfBirth,
            flightTime = flightTimeValue,
            seatPreference = seatPreference,
            mealPreference = if (mealPreferenceEnabled) mealPreference else "",
            stayRequired = stayRequired,
            frequentFlyerNumber = frequentFlyerNumber
        )

        // Make API call
        RetrofitClient.apiService.submitTravelRequest(travelRequest).enqueue(object : Callback<TravelRequestResponse> {
            override fun onResponse(call: Call<TravelRequestResponse>, response: Response<TravelRequestResponse>) {
                isSubmitting = false

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    if (responseBody.success) {
                        // Request was successful
                        Log.d("TravelController", "Travel request submitted successfully: ${responseBody.requestId}")
                        // Navigate back to home
                        navigator.navigateToHome()
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
                    } catch (e: Exception) {
                        submissionError = "Failed to submit travel request. Please try again."
                        Log.e("TravelController", "HTTP error: ${response.code()}, Error reading error body", e)
                    }
                }
            }

            override fun onFailure(call: Call<TravelRequestResponse>, t: Throwable) {
                isSubmitting = false
                submissionError = "Network error. Please check your connection and try again."
                Log.e("TravelController", "Network error submitting travel request", t)
            }
        })
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
}

/**
 * State representing the travel history screen UI state
 */
sealed class TravelHistoryState {
    object Loading : TravelHistoryState()
    data class Success(val travelRequests: List<TravelRequest>) : TravelHistoryState()
    data class Error(val message: String) : TravelHistoryState()
}
