package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import com.archeGlobal.one.navigation.Navigator
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Controller for the Travel screen following MVC architecture
 */
class TravelController(private val navigator: Navigator) {

    // UI state
    var travelHistoryState by mutableStateOf<TravelHistoryState>(TravelHistoryState.Loading)
        private set
    
    init {
        loadTravelHistory()
    }
    
    /**
     * Load travel history for the current user
     */
    fun loadTravelHistory() {
        // Set to loading state
        travelHistoryState = TravelHistoryState.Loading
        
        // Mock data for now, would be replaced with API call in future
        travelHistoryState = TravelHistoryState.Success(getMockTravelHistory())
    }
    
    /**
     * Mock data for development purposes
     */
    private fun getMockTravelHistory(): List<TravelRequest> {
        return listOf(
            TravelRequest(
                id = "TRV008",
                project = "Test",
                destination = "Jdjd",
                approver = "Brindha Arvindaraj",
                createdDate = SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.REJECTED
            ),
            TravelRequest(
                id = "TRV007",
                project = "Hhh",
                destination = "Hhh",
                approver = "Brindha Arvindaraj",
                createdDate = SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.APPROVED
            ),
            TravelRequest(
                id = "TRV006",
                project = "Hdhd",
                destination = "Hdhd",
                approver = "Brindha Arvindaraj",
                createdDate = SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.PENDING
            ),
            TravelRequest(
                id = "TRV005",
                project = "NDA",
                destination = "Dhdhdhd",
                approver = "Brindha Arvindaraj",
                createdDate = SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.PENDING
            )
        )
    }
    
    /**
     * Navigate back to previous screen
     */
    fun onBackPressed() {
        navigator.navigateToHome()
    }
    
    /**
     * Navigate to create new travel request screen
     * Note: This will be implemented in the future
     */
    fun navigateToCreateTravelRequest() {
        // To be implemented when creating the travel request form screen
    }
    
    /**
     * Navigate to travel request details screen
     */
    fun navigateToTravelDetails(travelRequestId: String) {
        // To be implemented when creating the travel details screen
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
