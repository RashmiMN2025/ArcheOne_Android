package com.archeGlobal.one.model

import com.archeGlobal.one.utils.DateFormatter
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Response model for combined travel history API
 */
data class TravelCombinedHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("order_history")
    val orderHistory: List<TravelOrderHistoryItem>,
    @SerializedName("approval_history")
    val approvalHistory: List<TravelApprovalHistoryItem>,
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
    @SerializedName("travelDetails")
    val travelDetails: List<TravelDestination>? = null,
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

        // Handle destination display using travelDetails array
        // Debug logging
        android.util.Log.d("TravelCombinedHistory", "Request $requestId: travelDetails = ${travelDetails?.size ?: "null"}")
        travelDetails?.forEachIndexed { index, detail ->
            android.util.Log.d(
                "TravelCombinedHistory",
                "  Detail $index: origin=${detail.originCity}, destination=${detail.destinationCity}",
            )
        }

        val destinationDisplay =
            when {
                travelDetails != null && travelDetails.isNotEmpty() -> {
                    if (travelDetails.size == 1) {
                        val detail = travelDetails[0]
                        // Single destination: show origin → destination if origin exists, otherwise just destination
                        if (!detail.originCity.isNullOrEmpty()) {
                            "${detail.originCity} → ${detail.destinationCity}"
                        } else {
                            detail.destinationCity
                        }
                    } else {
                        // Multi-destination: show count and first destination
                        val firstDestination = travelDetails[0].destinationCity
                        "$firstDestination +${travelDetails.size - 1} more"
                    }
                }
                else -> {
                    // Fallback: Show a more descriptive message
                    "Travel Request #${requestId.takeLast(4)}"
                }
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
            stayRequired = stayRequired,
            mealPreference = mealPreference,
            seatPreference = seatPreference,
            flightTime = flightTime,
            frequentFlyerNumber = frequentFlyerNumber,
            travelDestinations = travelDetails,
            employeeName = employeeName,
            employeeEmail = employeeEmail,
            employeeId = employeeId,
            employeeMobile = mobile,
        )
    }
}

/**
 * Model for approval history items
 * Updated to support new API format with additional fields
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
    val reportingManagerName: String,
    @SerializedName("reporting_manager_email")
    val reportingManagerEmail: String,
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
    val stayRequired: String?,
    @SerializedName("meal_pref")
    val mealPreference: String?,
    @SerializedName("seat_pref")
    val seatPreference: String?,
    @SerializedName("frequent_flyer_num")
    val frequentFlyerNumber: String?,
    @SerializedName("travelDetails")
    val travelDetails: List<TravelDestination>? = null,
    // Legacy fields for backward compatibility
    @SerializedName("departure_date")
    val departureDate: String? = null,
    @SerializedName("arrival_date")
    val arrivalDate: String? = null,
    @SerializedName("flight_time")
    val flightTime: String? = null,
    // Cab booking fields
    @SerializedName("travel_type")
    val travelType: String? = null,
    @SerializedName("cab_type")
    val cabType: String? = null,
    @SerializedName("travel_date")
    val travelDate: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("pickup_locations")
    val pickupLocations: List<String>? = null,
    @SerializedName("drop_location")
    val dropLocation: String? = null,
    @SerializedName("additional_members")
    val additionalMembers: String? = null,
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        android.util.Log.d("DEBUG_BUILD_CHECK", "NEW TravelApprovalHistoryItem.toTravelRequest() called for $requestId")
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

        // Handle destination display using travelDetails array or fallback to legacy fields
        // Debug logging
        android.util.Log.d("TravelCombinedHistory", "Request $requestId: travelDetails = ${travelDetails?.size ?: "null"}, mode = $modeOfTransport")
        travelDetails?.forEachIndexed { index, detail ->
            android.util.Log.d(
                "TravelCombinedHistory",
                "  Detail $index: origin=${detail.originCity}, destination=${detail.destinationCity}",
            )
        }

        val destinationDisplay =
            when {
                travelDetails != null && travelDetails.isNotEmpty() -> {
                    if (travelDetails.size == 1) {
                        val detail = travelDetails[0]
                        // Single destination: show origin → destination if origin exists, otherwise just destination
                        if (!detail.originCity.isNullOrEmpty()) {
                            "${detail.originCity} → ${detail.destinationCity}"
                        } else {
                            detail.destinationCity
                        }
                    } else {
                        // Multi-destination: show count and first destination
                        val firstDestination = travelDetails[0].destinationCity
                        "$firstDestination +${travelDetails.size - 1} more"
                    }
                }
                // Fallback for cab bookings and other formats that might not have travelDetails
                else -> {
                    // For cab bookings and other modes, create a descriptive display
                    when (modeOfTransport.lowercase()) {
                        "cab" -> {
                            // Cab bookings might not have detailed destination info
                            "Cab Booking - ${projectName.take(20)}${if (projectName.length > 20) "..." else ""}"
                        }
                        else -> {
                            // Other travel modes: fallback to project or request ID
                            if (projectName.isNotBlank()) {
                                "Travel for ${projectName.take(20)}${if (projectName.length > 20) "..." else ""}"
                            } else {
                                "Travel Request #${requestId.takeLast(4)}"
                            }
                        }
                    }
                }
            }

        // Debug logging to understand what data we're receiving and mapping
        android.util.Log.d("TravelApprovalMapping", "=== MAPPING REQUEST $requestId ===")
        android.util.Log.d("TravelApprovalMapping", "API employee_name: $employeeName")
        android.util.Log.d("TravelApprovalMapping", "API employee_email: $employeeEmail")
        android.util.Log.d("TravelApprovalMapping", "API employee_id: $employeeId")
        android.util.Log.d("TravelApprovalMapping", "API mobile: $mobile")
        android.util.Log.d("TravelApprovalMapping", "API reporting_manager_name: $reportingManagerName")
        android.util.Log.d("TravelApprovalMapping", "API reporting_manager_email: $reportingManagerEmail")

        // Get dates from travel details or use legacy fields
        val firstDetail = travelDetails?.firstOrNull()
        val depDate = firstDetail?.departureDate ?: departureDate ?: ""
        val arrDate = firstDetail?.arrivalDate ?: arrivalDate ?: ""

        val travelRequest =
            TravelRequest(
                id = requestId,
                project = projectName,
                destination = destinationDisplay,
                approver = reportingManagerName, // The actual approver from API
                approverEmail = reportingManagerEmail, // The actual approver email from API
                createdDate = createdDate ?: Date(),
                status = travelStatus,
                businessJustification = businessJustification,
                modeOfTransport = modeOfTransport,
                departureDate = depDate,
                arrivalDate = arrDate,
                actionToken = actionToken,
                rejectionReason = rejectionDescription ?: remarks, // Use rejection_description first, then fall back to remarks
                stayRequired = stayRequired,
                mealPreference = mealPreference,
                seatPreference = seatPreference,
                flightTime = firstDetail?.flightTimePreference ?: flightTime,
                frequentFlyerNumber = frequentFlyerNumber,
                travelDestinations = travelDetails,
                employeeName = employeeName,
                employeeEmail = employeeEmail,
                employeeId = employeeId,
                employeeMobile = mobile,
                // Cab booking fields
                travelType = travelType,
                cabType = cabType,
                travelDate = travelDate,
                duration = duration,
                pickupLocations = pickupLocations,
                dropLocation = dropLocation,
                additionalMembers = additionalMembers,
                projectId = projectId,
                opportunityId = opportunityId,
                crmId = crmId,
            )

        // Log what we're actually putting in the TravelRequest
        android.util.Log.d("TravelApprovalMapping", "CREATED TravelRequest:")
        android.util.Log.d("TravelApprovalMapping", "  TravelRequest.employeeName: ${travelRequest.employeeName}")
        android.util.Log.d("TravelApprovalMapping", "  TravelRequest.employeeEmail: ${travelRequest.employeeEmail}")
        android.util.Log.d("TravelApprovalMapping", "  TravelRequest.employeeId: ${travelRequest.employeeId}")
        android.util.Log.d("TravelApprovalMapping", "  TravelRequest.employeeMobile: ${travelRequest.employeeMobile}")
        android.util.Log.d("TravelApprovalMapping", "  TravelRequest.approver: ${travelRequest.approver}")
        android.util.Log.d("TravelApprovalMapping", "=== END MAPPING ===")

        return travelRequest
    }
}

/**
 * V2 API Models for new travel endpoints
 */

/**
 * Response model for /travel/v2/approval-history-count
 */
data class TravelV2ApprovalHistoryCountResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("approval_history_count")
    val approvalHistoryCount: Int,
)

/**
 * Response model for /travel/v2/order-history
 */
data class TravelV2OrderHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("order_history")
    val orderHistory: List<TravelOrderHistoryItem>,
)

/**
 * Response model for /travel/v2/approval-history
 */
data class TravelV2ApprovalHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("approval_history")
    val approvalHistory: List<TravelApprovalHistoryItem>,
)

/**
 * Request model for V2 APIs (only requires employeeEmail)
 */
data class TravelV2Request(
    @SerializedName("employeeEmail")
    val employeeEmail: String,
)

/**
 * Response model for /travel/v2/admin/history
 */
data class TravelV2AdminHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("admin_history")
    val adminHistory: List<TravelApprovalHistoryItem>,
)
