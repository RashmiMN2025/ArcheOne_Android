package com.archeGlobal.one.repository

import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for handling travel request data operations
 */
class TravelRepository {
    /**
     * Get travel history for the current user
     */
    fun getTravelHistory(): Flow<List<TravelRequest>> = flow {
        try {
            // This would be replaced with actual API call when endpoint is available
            // val response = apiService.getTravelHistory()
            // if (response.isSuccessful && response.body() != null) {
            //     emit(response.body()!!)
            // } else {
            //     throw Exception("Failed to fetch travel history")
            // }
            
            // For now, emit mock data
            emit(getMockTravelHistory())
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
    
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
                createdDate = java.text.SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.REJECTED
            ),
            TravelRequest(
                id = "TRV007",
                project = "Hhh",
                destination = "Hhh",
                approver = "Brindha Arvindaraj",
                createdDate = java.text.SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.APPROVED
            ),
            TravelRequest(
                id = "TRV006",
                project = "Hdhd",
                destination = "Hdhd",
                approver = "Brindha Arvindaraj",
                createdDate = java.text.SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.PENDING
            ),
            TravelRequest(
                id = "TRV005",
                project = "NDA",
                destination = "Dhdhdhd",
                approver = "Brindha Arvindaraj",
                createdDate = java.text.SimpleDateFormat("dd MMM yyyy").parse("3 Jun 2025"),
                status = TravelStatus.PENDING
            )
        )
    }
}
