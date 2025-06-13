package com.archeGlobal.one.model

import java.util.Date

/**
 * Model class for travel requests
 */
data class TravelRequest(
    val id: String,                  // Format: TRV001, TRV002, etc.
    val project: String,             // Project name
    val destination: String,         // Travel destination
    val approver: String,            // Name of the approver
    val createdDate: Date,           // Date when the request was created
    val status: TravelStatus,        // Status of the request
    val businessJustification: String? = null, // Business justification for the travel
    val modeOfTransport: String? = null,       // Mode of transport for the travel
    val departureDate: String? = null,         // Departure date for the travel
    val arrivalDate: String? = null,           // Arrival date for the travel
    val actionToken: String? = null            // Token required for approve/reject API
)

/**
 * Represents the possible statuses of a travel request
 */
enum class TravelStatus {
    APPROVED, REJECTED, PENDING
}
