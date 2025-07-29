package com.archeGlobal.one.model

import com.archeGlobal.one.utils.DateFormatter
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Response model for combined travel history API
 */
data class TravelCombinedHistoryResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("order_history")
    val orderHistory: List<TravelOrderHistoryItem>,

    @SerializedName("approval_history")
    val approvalHistory: List<TravelApprovalHistoryItem>
)

/**
 * Model for order history items
 */
data class TravelOrderHistoryItem(
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
    val stayRequired: String?,

    @SerializedName("meal_pref")
    val mealPreference: String?,

    @SerializedName("seat_pref")
    val seatPreference: String?,

    @SerializedName("flight_time")
    val flightTime: String?,

    @SerializedName("frequent_flyer_num")
    val frequentFlyerNumber: String?,

    @SerializedName("Travel Details")
    val travelDetails: List<TravelDestination>? = null
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        // Parse the created date using DateFormatter utility
        val createdDate = DateFormatter.parseApiDate(createdAt)

        // Map status string to TravelStatus enum
        val travelStatus = when (status.lowercase()) {
            "approved" -> TravelStatus.APPROVED
            "rejected" -> TravelStatus.REJECTED
            else -> TravelStatus.PENDING
        }

        return TravelRequest(
            id = requestId,
            project = projectName,
            destination = travelDestination,
            approver = reportingManagerName,
            approverEmail = reportingManagerEmail,
            createdDate = createdDate ?: Date(),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = departureDate,
            arrivalDate = arrivalDate,
            stayRequired = stayRequired,
            mealPreference = mealPreference,
            seatPreference = seatPreference,
            flightTime = flightTime,
            frequentFlyerNumber = frequentFlyerNumber,
            travelDestinations = travelDetails
        )
    }
}

/**
 * Model for approval history items
 */
data class TravelApprovalHistoryItem(
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

    @SerializedName("status")
    val status: String,

    @SerializedName("action_token")
    val actionToken: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("remarks")
    val remarks: String?,

    @SerializedName("stay_required")
    val stayRequired: String?,

    @SerializedName("meal_pref")
    val mealPreference: String?,

    @SerializedName("seat_pref")
    val seatPreference: String?,

    @SerializedName("flight_time")
    val flightTime: String?,

    @SerializedName("frequent_flyer_num")
    val frequentFlyerNumber: String?,

    @SerializedName("Travel Details")
    val travelDetails: List<TravelDestination>? = null
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        // Parse the created date using DateFormatter utility
        val createdDate = DateFormatter.parseApiDate(createdAt)

        // Map status string to TravelStatus enum
        val travelStatus = when (status.lowercase()) {
            "approved" -> TravelStatus.APPROVED
            "rejected" -> TravelStatus.REJECTED
            else -> TravelStatus.PENDING
        }

        return TravelRequest(
            id = requestId,
            project = projectName,
            destination = travelDestination,
            approver = employeeName,
            approverEmail = null, // Approval history doesn't have manager email field
            createdDate = createdDate ?: Date(),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = departureDate,
            arrivalDate = arrivalDate,
            actionToken = actionToken,
            rejectionReason = remarks,
            stayRequired = stayRequired,
            mealPreference = mealPreference,
            seatPreference = seatPreference,
            flightTime = flightTime,
            frequentFlyerNumber = frequentFlyerNumber,
            travelDestinations = travelDetails
        )
    }
}
