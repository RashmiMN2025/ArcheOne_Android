package com.archeGlobal.one.model

import com.archeGlobal.one.utils.DateFormatter
import com.google.gson.annotations.SerializedName

/**
 * Response model for travel approval history API
 */
data class TravelApprovalResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("approval_history")
    val approvalHistory: List<TravelApprovalItem>,
)

/**
 * Model for individual travel approval item
 */
data class TravelApprovalItem(
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
    @SerializedName("project_name")
    val projectName: String,
    @SerializedName("project_id")
    val projectId: String? = null,
    @SerializedName("opportunity_id")
    val opportunityId: String? = null,
    @SerializedName("crm_id")
    val crmId: String? = null,
    @SerializedName("business_justification")
    val businessJustification: String,
    @SerializedName("mode_of_transport")
    val modeOfTransport: String,
    @SerializedName("reporting_manager_name")
    val reportingManagerName: String? = null,
    @SerializedName("reporting_manager_email")
    val reportingManagerEmail: String? = null,
    @SerializedName("action_token")
    val actionToken: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("rejection_description")
    val rejectionDescription: String? = null,
    @SerializedName("remarks")
    val remarks: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("stay_required")
    val stayRequired: Boolean? = null,
    @SerializedName("meal_pref")
    val mealPreference: String? = null,
    @SerializedName("seat_pref")
    val seatPreference: String? = null,
    @SerializedName("frequent_flyer_num")
    val frequentFlyerNumber: String? = null,
    @SerializedName("travelDetails")
    val travelDetails: List<TravelDestination>? = null,
    // Legacy fields for backward compatibility
    @SerializedName("travel_destination")
    val travelDestination: String? = null,
    @SerializedName("departure_date")
    val departureDate: String? = null,
    @SerializedName("arrival_date")
    val arrivalDate: String? = null,
    @SerializedName("flight_time")
    val flightTime: String? = null,
    @SerializedName("Travel Details")
    val legacyTravelDetails: List<TravelDestination>? = null,
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        // Parse dates if possible
        val travelStatus =
            when (status.lowercase()) {
                "approved" -> TravelStatus.APPROVED
                "rejected" -> TravelStatus.REJECTED
                "cancelled" -> TravelStatus.CANCELLED
                else -> TravelStatus.PENDING
            }

        // Get travel details from new format or fall back to legacy
        val allTravelDetails = travelDetails ?: legacyTravelDetails

        // Handle multi-destination display
        val destinationDisplay =
            if (allTravelDetails != null && allTravelDetails.isNotEmpty()) {
                // Multi-destination: show origin → destination format
                if (allTravelDetails.size == 1) {
                    val detail = allTravelDetails.first()
                    "${detail.originCity} → ${detail.destinationCity}"
                } else {
                    val firstDetail = allTravelDetails.first()
                    "${firstDetail.originCity} → ${firstDetail.destinationCity} (+${allTravelDetails.size - 1} more)"
                }
            } else {
                // Single destination (legacy format)
                travelDestination ?: "Unknown Destination"
            }

        // Get departure/arrival dates from travel details or legacy fields
        val firstDetail = allTravelDetails?.firstOrNull()
        val depDate = firstDetail?.departureDate ?: departureDate ?: ""
        val arrDate = firstDetail?.arrivalDate ?: arrivalDate ?: ""

        return TravelRequest(
            id = requestId,
            project = projectName,
            destination = destinationDisplay,
            approver = employeeName,
            approverEmail = reportingManagerEmail,
            createdDate = DateFormatter.parseApiDate(createdAt),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = depDate,
            arrivalDate = arrDate,
            actionToken = actionToken,
            travelDestinations = allTravelDetails,
        )
    }

    /**
     * Check if this is a multi-destination travel request
     */
    fun isMultiDestination(): Boolean {
        val allTravelDetails = travelDetails ?: legacyTravelDetails
        return allTravelDetails != null && allTravelDetails.size > 1
    }

    /**
     * Get all destinations for multi-destination travel
     */
    fun getAllDestinations(): List<TravelDestination> {
        return travelDetails ?: legacyTravelDetails ?: emptyList()
    }
}
