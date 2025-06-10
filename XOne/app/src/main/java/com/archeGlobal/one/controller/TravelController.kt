package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.TravelHistoryRequest
import com.archeGlobal.one.model.TravelHistoryResponse
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelRequestResponse
import com.archeGlobal.one.model.TravelRequestSubmission
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.model.TravelApprovalRequest
import com.archeGlobal.one.model.TravelApprovalResponse
import com.archeGlobal.one.model.TravelApprovalItem
import com.archeGlobal.one.model.TravelApprovalActionRequest
import com.archeGlobal.one.model.TravelApprovalActionResponse
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controller for the Travel screens following MVC architecture
 */
class TravelController(private val navigator: Navigator, private val context: Context) {
    
    /**
     * Sealed class representing the state of travel approvals
     */
    sealed class TravelApprovalsState {
        object Loading : TravelApprovalsState()
        data class Success(val approvalRequests: List<TravelRequest>) : TravelApprovalsState()
        data class Error(val message: String) : TravelApprovalsState()
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
    
    // Employee data for travel form
    var employeeName by mutableStateOf("")
        private set
    var employeeId by mutableStateOf("")
        private set
    var mobileNumber by mutableStateOf("")
        private set
    var employeeGrade by mutableStateOf("N/A")
        private set
    var reportingManagerName by mutableStateOf("")
        private set
    var reportingManagerEmail by mutableStateOf("biswajit.d@arche.global")
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
    
    // Mode of transport options
    val transportOptions = listOf("Bus", "Car", "Flight", "Train")
    var modeOfTransport by mutableStateOf("")
        private set
    var isTransportDropdownExpanded by mutableStateOf(false)
        private set
    var departureDate by mutableStateOf("2 Jun 2025")
        private set
    var arrivalDate by mutableStateOf("2 Jun 2025")
        private set
    
    init {
        loadEmployeeDetails()
        loadTravelHistory()
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
            
            // Get reporting manager name from user details if available
            user.userDetails?.let { details ->
                reportingManagerName = details.reporting_manager
            }
            // reportingManagerEmail is already set to "biswajit.d@arche.global"
        }
    }
    
    /**
     * Load travel history for the current user
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
    
    // We now use real data from the API instead of mock data
    
    /**
     * Navigate back to previous screen
     * @param fromTravelDetail If true, we're navigating back from the travel request detail screen
     */
    fun onBackPressed(fromTravelDetail: Boolean = false) {
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
        // Refresh travel history data before navigating
        loadTravelHistory()
        navigator.navigateToTravelExpenses()
    }
    
    /**
     * Navigate to travel request details screen
     */
    fun navigateToTravelDetails(travelRequestId: String) {
        // Find the travel request with the given ID from the current state
        val currentState = travelHistoryState
        if (currentState is TravelHistoryState.Success) {
            val request = currentState.travelRequests.find { it.id == travelRequestId }
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
        // Load travel approvals data before navigating
        loadTravelApprovals()
        navigator.navigateToTravelApprovals()
    }
    
    /**
     * Load travel approval requests from the API
     */
    fun loadTravelApprovals() {
        // Set state to loading
        travelApprovalsState = TravelApprovalsState.Loading
        
        // Get the user's email from UserDataManager
        val userEmail = UserDataManager.getInstance(context).getUserData()?.email
        
        if (userEmail == null) {
            travelApprovalsState = TravelApprovalsState.Error("User email not found")
            return
        }
        
        // Create the request body
        val request = TravelApprovalRequest(managerEmail = userEmail)
        
        // Make the API call
        RetrofitClient.apiService.getTravelApprovalHistory(request).enqueue(object : Callback<TravelApprovalResponse> {
            override fun onResponse(call: Call<TravelApprovalResponse>, response: Response<TravelApprovalResponse>) {
                if (response.isSuccessful) {
                    val approvalResponse = response.body()
                    if (approvalResponse != null && approvalResponse.status == 200) {
                        // Convert API response to UI models
                        val approvalRequests = approvalResponse.approvalHistory.map { it.toTravelRequest() }
                        travelApprovalsState = TravelApprovalsState.Success(approvalRequests)
                    } else {
                        travelApprovalsState = TravelApprovalsState.Error("Failed to load approval requests")
                    }
                } else {
                    travelApprovalsState = TravelApprovalsState.Error("Error: ${response.code()} ${response.message()}")
                }
            }
            
            override fun onFailure(call: Call<TravelApprovalResponse>, t: Throwable) {
                Log.e("TravelController", "Error loading travel approvals", t)
                travelApprovalsState = TravelApprovalsState.Error("Network error: ${t.message}")
            }
        })
    }
    
    /**
     * Approve a travel request
     */
    fun approveTravelRequest(travelRequestId: String, actionToken: String? = null) {
        // Find the request in the current state
        val currentState = travelApprovalsState
        if (currentState is TravelApprovalsState.Success) {
            // Find the request with the matching ID
            val request = currentState.approvalRequests.find { it.id == travelRequestId }
            if (request == null) {
                Log.e("TravelController", "Travel request with ID $travelRequestId not found")
                return
            }
            
            // First update the local state to provide immediate feedback
            val updatedRequests = currentState.approvalRequests.map { 
                if (it.id == travelRequestId) {
                    it.copy(status = TravelStatus.APPROVED)
                } else {
                    it
                }
            }
            
            // Update the state
            travelApprovalsState = TravelApprovalsState.Success(updatedRequests)
            
            // Log the action
            Log.d("TravelController", "Approving travel request: $travelRequestId")
            
            // Make the API call to approve the request
            val token = actionToken ?: "dummy-token" // In a real implementation, this would come from the API
            val approveRequest = TravelApprovalActionRequest(
                requestId = travelRequestId,
                actionToken = token,
                action = "approve"
            )
            
            RetrofitClient.apiService.approveTravelRequest(approveRequest).enqueue(object : Callback<TravelApprovalActionResponse> {
                override fun onResponse(call: Call<TravelApprovalActionResponse>, response: Response<TravelApprovalActionResponse>) {
                    if (response.isSuccessful) {
                        val approvalResponse = response.body()
                        if (approvalResponse != null && approvalResponse.status == 200) {
                            Log.d("TravelController", "Successfully approved travel request: $travelRequestId")
                            // State is already updated, no need to do anything else
                        } else {
                            Log.e("TravelController", "Error approving travel request: ${approvalResponse?.message}")
                            // Revert the state change if the API call failed
                            loadTravelApprovals() // Reload the data
                        }
                    } else {
                        Log.e("TravelController", "Error approving travel request: ${response.code()} ${response.message()}")
                        // Revert the state change if the API call failed
                        loadTravelApprovals() // Reload the data
                    }
                }
                
                override fun onFailure(call: Call<TravelApprovalActionResponse>, t: Throwable) {
                    Log.e("TravelController", "Error approving travel request", t)
                    // Revert the state change if the API call failed
                    loadTravelApprovals() // Reload the data
                }
            })
        }
    }
    
    /**
     * Reject a travel request
     */
    fun rejectTravelRequest(travelRequestId: String, remarks: String = "", actionToken: String? = null) {
        // Find the request in the current state
        val currentState = travelApprovalsState
        if (currentState is TravelApprovalsState.Success) {
            // Find the request with the matching ID
            val request = currentState.approvalRequests.find { it.id == travelRequestId }
            if (request == null) {
                Log.e("TravelController", "Travel request with ID $travelRequestId not found")
                return
            }
            
            // First update the local state to provide immediate feedback
            val updatedRequests = currentState.approvalRequests.map { 
                if (it.id == travelRequestId) {
                    it.copy(status = TravelStatus.REJECTED)
                } else {
                    it
                }
            }
            
            // Update the state
            travelApprovalsState = TravelApprovalsState.Success(updatedRequests)
            
            // Log the action
            Log.d("TravelController", "Rejecting travel request: $travelRequestId with remarks: $remarks")
            
            // Make the API call to reject the request
            val token = actionToken ?: "dummy-token" // In a real implementation, this would come from the API
            val rejectRequest = TravelApprovalActionRequest(
                requestId = travelRequestId,
                actionToken = token,
                action = "reject",
                remarks = remarks
            )
            
            RetrofitClient.apiService.rejectTravelRequest(rejectRequest).enqueue(object : Callback<TravelApprovalActionResponse> {
                override fun onResponse(call: Call<TravelApprovalActionResponse>, response: Response<TravelApprovalActionResponse>) {
                    if (response.isSuccessful) {
                        val rejectionResponse = response.body()
                        if (rejectionResponse != null && rejectionResponse.status == 200) {
                            Log.d("TravelController", "Successfully rejected travel request: $travelRequestId")
                            // State is already updated, no need to do anything else
                        } else {
                            Log.e("TravelController", "Error rejecting travel request: ${rejectionResponse?.message}")
                            // Revert the state change if the API call failed
                            loadTravelApprovals() // Reload the data
                        }
                    } else {
                        Log.e("TravelController", "Error rejecting travel request: ${response.code()} ${response.message()}")
                        // Revert the state change if the API call failed
                        loadTravelApprovals() // Reload the data
                    }
                }
                
                override fun onFailure(call: Call<TravelApprovalActionResponse>, t: Throwable) {
                    Log.e("TravelController", "Error rejecting travel request", t)
                    // Revert the state change if the API call failed
                    loadTravelApprovals() // Reload the data
                }
            })
        }
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
     */
    fun updateModeOfTransport(value: String) {
        modeOfTransport = value
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
            reportingManagerEmail = reportingManagerEmail
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
}

/**
 * State representing the travel history screen UI state
 */
sealed class TravelHistoryState {
    object Loading : TravelHistoryState()
    data class Success(val travelRequests: List<TravelRequest>) : TravelHistoryState()
    data class Error(val message: String) : TravelHistoryState()
}
