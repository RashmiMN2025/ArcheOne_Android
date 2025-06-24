package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Response model for travel history API
 */
data class TravelHistoryResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("order_history")
    val orderHistory: List<TravelHistoryItem>
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
    val updatedAt: String
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        // Parse the created date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val createdDate = try {
            dateFormat.parse(createdAt)
        } catch (e: Exception) {
            Date() // Fallback to current date if parsing fails
        }

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
            createdDate = createdDate ?: Date(),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = departureDate,
            arrivalDate = arrivalDate
        )
    }
}
