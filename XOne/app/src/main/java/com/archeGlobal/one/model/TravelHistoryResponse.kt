package com.archeGlobal.one.model

import com.archeGlobal.one.utils.DateFormatter
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Response model for cab details in travel history
 */
data class CabDetailsResponse(
    @SerializedName("travelType")
    val travelType: String? = null,
    @SerializedName("cabType")
    val cabType: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("travelDate")
    val travelDate: String? = null,
    @SerializedName("pickups")
    val pickups: List<PickupLocationResponse>? = null,
    @SerializedName("dropLocation")
    val dropLocation: String? = null,
    @SerializedName("dropMapDetails")
    val dropMapDetails: String? = null,
    @SerializedName("additionalMembers")
    val additionalMembers: List<AdditionalMemberResponse>? = null,
)

/**
 * Response model for pickup location
 */
data class PickupLocationResponse(
    @SerializedName("location")
    val location: String,
    @SerializedName("mapDetails")
    val mapDetails: String? = null,
)

/**
 * Response model for additional members in cab booking
 */
data class AdditionalMemberResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String? = null,
)

/**
 * Response model for travel history API
 */
data class TravelHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("order_history")
    val orderHistory: List<TravelHistoryItem>,
)

/**
 * Model for individual travel history item
 */
data class TravelHistoryItem(
    @SerializedName("request_id")
    val requestId: String,
    @SerializedName("employee_name")
    val employeeName: String,
    @SerializedName("employee_email")
    val employeeEmail: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("travel_destination")
    val travelDestination: String,
    @SerializedName("project_name")
    val projectName: String,
    @SerializedName("business_justification")
    val businessJustification: String,
    @SerializedName("departure_date")
    val departureDate: String,
    @SerializedName("arrival_date")
    val arrivalDate: String,
    @SerializedName("mode_of_transport")
    val modeOfTransport: String,
    @SerializedName("reporting_manager_name")
    val reportingManagerName: String,
    @SerializedName("reporting_manager_email")
    val reportingManagerEmail: String,
    @SerializedName("action_token")
    val actionToken: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("rejection_description")
    val rejectionDescription: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("stay_required")
    val stayRequired: Boolean? = null,
    @SerializedName("meal_pref")
    val mealPreference: String? = null,
    @SerializedName("seat_pref")
    val seatPreference: String? = null,
    @SerializedName("flight_time")
    val flightTime: String? = null,
    @SerializedName("frequent_flyer_num")
    val frequentFlyerNumber: String? = null,
    @SerializedName("Travel Details")
    val travelDetails: List<TravelDestination>? = null,
    @SerializedName("cabDetails")
    val cabDetails: CabDetailsResponse? = null,
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        // Parse the created date using DateFormatter utility
        val createdDate = DateFormatter.parseApiDate(createdAt)

        // Map status string to TravelStatus enum
        val travelStatus =
            when (status.lowercase()) {
                "approved" -> TravelStatus.APPROVED
                "rejected" -> TravelStatus.REJECTED
                "cancelled" -> TravelStatus.CANCELLED
                else -> TravelStatus.PENDING
            }

        // Handle multi-destination display
        val destinationDisplay =
            if (travelDetails != null && travelDetails.isNotEmpty()) {
                // Multi-destination: show count and first destination
                if (travelDetails.size == 1) {
                    travelDetails.first().travelDestination
                } else {
                    "${travelDetails.first().travelDestination} (+${travelDetails.size - 1} more)"
                }
            } else {
                // Single destination
                travelDestination
            }

        return TravelRequest(
            id = requestId,
            project = projectName,
            destination = destinationDisplay,
            approver = reportingManagerName,
            approverEmail = reportingManagerEmail,
            createdDate = createdDate ?: Date(),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = departureDate,
            arrivalDate = arrivalDate,
            rejectionReason = rejectionDescription,
            travelDestinations = travelDetails,
            // Cab details
            travelType = cabDetails?.travelType,
            cabType = cabDetails?.cabType,
            travelDate = cabDetails?.travelDate,
            duration = cabDetails?.duration,
            pickupLocations = cabDetails?.pickups?.map { it.location },
            pickupMapDetails = cabDetails?.pickups?.map { "${it.location}|${it.mapDetails ?: ""}" },
            dropLocation = cabDetails?.dropLocation,
            dropMapDetails = cabDetails?.dropMapDetails,
            additionalMembers = cabDetails?.additionalMembers?.joinToString(", ") { it.name },
        )
    }

    /**
     * Check if this is a multi-destination travel request
     */
    fun isMultiDestination(): Boolean = travelDetails != null && travelDetails.size > 1

    /**
     * Get all destinations for multi-destination travel
     */
    fun getAllDestinations(): List<TravelDestination> = travelDetails ?: emptyList()
}
