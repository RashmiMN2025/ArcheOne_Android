package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Response model for travel approval history API
 */
data class TravelApprovalResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("approval_history")
    val approvalHistory: List<TravelApprovalItem>
)

/**
 * Model for individual travel approval item
 */
data class TravelApprovalItem(
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

    @SerializedName("request_id")
    val requestId: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("action_token")
    val actionToken: String,

    @SerializedName("remarks")
    val remarks: String
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        // Parse dates if possible
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
            createdDate = parseDate(createdAt),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = departureDate,
            arrivalDate = arrivalDate,
            actionToken = actionToken
        )
    }

    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
