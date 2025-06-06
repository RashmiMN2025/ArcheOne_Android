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
    
    // Date formatters
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // UI state
    var travelHistoryState by mutableStateOf<TravelHistoryState>(TravelHistoryState.Loading)
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
     */
    fun onBackPressed() {
        navigator.navigateToHome()
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
        // To be implemented when creating the travel details screen
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
