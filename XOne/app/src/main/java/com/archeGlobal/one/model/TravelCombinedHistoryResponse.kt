package com.archeGlobal.one.model

import com.archeGlobal.one.utils.DateFormatter
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
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
    @SerializedName("user_location")
    val userLocation: String? = null,
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
    @SerializedName("departure_date")
    val departureDate: String? = null,
    @SerializedName("arrival_date")
    val arrivalDate: String? = null,
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
    @SerializedName("remarks")
    val remarks: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("stay_required")
    val stayRequired: Boolean? = null,
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
    @SerializedName("cabDetails")
    val cabDetails: CabDetails? = null,
) {
    /**
     * Convert to TravelRequest model for UI display
     */
    fun toTravelRequest(): TravelRequest {
        android.util.Log.d("TravelOrderHistory", "=== ORDER HISTORY ITEM CONVERSION ===")
        android.util.Log.d("TravelOrderHistory", "Request ID: $requestId")
        android.util.Log.d("TravelOrderHistory", "Mode of Transport: $modeOfTransport")
        android.util.Log.d("TravelOrderHistory", "Has cabDetails: ${cabDetails != null}")

        // Log cab details if present
        cabDetails?.let { cab ->
            android.util.Log.d("TravelOrderHistory", "=== CAB DETAILS ===")
            android.util.Log.d("TravelOrderHistory", "  travelType: ${cab.travelType}")
            android.util.Log.d("TravelOrderHistory", "  cabType: ${cab.cabType}")
            android.util.Log.d("TravelOrderHistory", "  travelDate: ${cab.travelDate}")
            android.util.Log.d("TravelOrderHistory", "  duration: ${cab.duration}")
            android.util.Log.d("TravelOrderHistory", "  dropLocation: ${cab.dropLocation}")
            android.util.Log.d("TravelOrderHistory", "  dropMapDetails: ${cab.dropMapDetails}")
            android.util.Log.d("TravelOrderHistory", "  Has pickups: ${cab.pickups != null}")
            android.util.Log.d("TravelOrderHistory", "  Pickups count: ${cab.pickups?.size ?: 0}")
            cab.pickups?.forEachIndexed { index, pickup ->
                android.util.Log.d("TravelOrderHistory", "    Pickup $index: location='${pickup.location}', mapDetails='${pickup.mapDetails}'")
            }
            android.util.Log.d("TravelOrderHistory", "  Has additionalMembers: ${cab.additionalMembers != null}")
            android.util.Log.d("TravelOrderHistory", "  Additional members count: ${cab.additionalMembers?.size ?: 0}")
            cab.additionalMembers?.forEachIndexed { index, member ->
                android.util.Log.d("TravelOrderHistory", "    Member $index: name='${member.name}', email='${member.email}'")
            }
        }

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

        // Map cab details using helper methods to support both old and new formats
        val cabTravelType = cabDetails?.getActualTravelType()
        val cabTypeValue = cabDetails?.getActualCabType()
        val cabDuration = cabDetails?.duration
        val cabTravelDate = cabDetails?.getActualTravelDate()
        val cabPickupLocations = cabDetails?.getActualPickupLocations()
        val cabPickupMapDetails = cabDetails?.pickups?.map { "${it.location}|${it.mapDetails ?: ""}" }
        val cabDropLocation = cabDetails?.getActualDropLocation()
        val cabDropMapDetails = cabDetails?.dropMapDetails
        val cabAdditionalMembers = cabDetails?.getActualAdditionalMembers()

        val travelRequest = TravelRequest(
            id = requestId,
            project = projectName,
            destination = destinationDisplay,
            approver = reportingManagerName,
            approverEmail = reportingManagerEmail,
            createdDate = createdDate ?: Date(),
            status = travelStatus,
            businessJustification = businessJustification,
            modeOfTransport = modeOfTransport,
            departureDate = departureDate ?: "",
            arrivalDate = arrivalDate ?: "",
            rejectionReason = rejectionDescription,
            stayRequired = stayRequired?.toString(),
            mealPreference = mealPreference,
            seatPreference = seatPreference,
            flightTime = flightTime,
            frequentFlyerNumber = frequentFlyerNumber,
            travelDestinations = travelDetails,
            employeeName = employeeName,
            employeeEmail = employeeEmail,
            employeeId = employeeId,
            employeeMobile = mobile,
            // Cab booking fields
            travelType = cabTravelType,
            cabType = cabTypeValue,
            travelDate = cabTravelDate,
            duration = cabDuration,
            pickupLocations = cabPickupLocations,
            pickupMapDetails = cabPickupMapDetails,
            dropLocation = cabDropLocation,
            dropMapDetails = cabDropMapDetails,
            additionalMembers = cabAdditionalMembers,
            projectId = projectId,
            opportunityId = opportunityId,
            crmId = crmId,
        )

        // Log the mapped TravelRequest cab data
        android.util.Log.d("TravelOrderHistory", "=== MAPPED TO TRAVELREQUEST ===")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.travelType: ${travelRequest.travelType}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.cabType: ${travelRequest.cabType}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.travelDate: ${travelRequest.travelDate}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.duration: ${travelRequest.duration}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.pickupLocations: ${travelRequest.pickupLocations}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.pickupMapDetails: ${travelRequest.pickupMapDetails}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.dropLocation: ${travelRequest.dropLocation}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.dropMapDetails: ${travelRequest.dropMapDetails}")
        android.util.Log.d("TravelOrderHistory", "  travelRequest.additionalMembers: ${travelRequest.additionalMembers}")
        android.util.Log.d("TravelOrderHistory", "=== END ORDER HISTORY CONVERSION ===")

        return travelRequest
    }
}

/**
 * Cab details nested object from API
 * Updated to support new pickups structure with map details
 * Uses custom deserializers to handle both old (string array) and new (object array) formats
 */
data class CabDetails(
    @SerializedName("travelType")
    val travelType: String? = null,
    @SerializedName("cab_travel_type")
    val cabTravelType: String? = null,  // Legacy field
    @SerializedName("cabType")
    val cabType: String? = null,
    @SerializedName("cab_type")
    val legacyCabType: String? = null,  // Legacy field
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("travelDate")
    val travelDate: String? = null,
    @SerializedName("travel_date")
    val legacyTravelDate: String? = null,  // Legacy field
    @JsonAdapter(PickupsDeserializer::class)
    @SerializedName("pickups")
    val pickups: List<PickupLocationResponse>? = null,  // Handles both string[] and object[] formats
    @SerializedName("pickup_locations")
    val pickupLocations: List<String>? = null,  // Legacy field
    @SerializedName("dropLocation")
    val dropLocation: String? = null,
    @SerializedName("drop_location")
    val legacyDropLocation: String? = null,  // Legacy field
    @SerializedName("dropMapDetails")
    val dropMapDetails: String? = null,
    @JsonAdapter(AdditionalMembersDeserializer::class)
    @SerializedName("additionalMembers")
    val additionalMembers: List<AdditionalMemberResponse>? = null,  // Handles both string[] and object[] formats
    @SerializedName("additional_members")
    val legacyAdditionalMembers: List<String>? = null,  // Legacy field
) {
    // Helper methods to get the right field regardless of format
    fun getActualTravelType() = travelType ?: cabTravelType
    fun getActualCabType() = cabType ?: legacyCabType
    fun getActualTravelDate() = travelDate ?: legacyTravelDate
    fun getActualDropLocation() = dropLocation ?: legacyDropLocation
    fun getActualPickupLocations(): List<String>? = pickups?.map { it.location } ?: pickupLocations
    fun getActualAdditionalMembers(): String? =
        additionalMembers?.joinToString(", ") { it.name }
        ?: legacyAdditionalMembers?.joinToString(", ")
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
    @SerializedName("cabDetails")
    val cabDetails: CabDetails? = null,
    // Legacy fields for backward compatibility
    @SerializedName("departure_date")
    val departureDate: String? = null,
    @SerializedName("arrival_date")
    val arrivalDate: String? = null,
    @SerializedName("flight_time")
    val flightTime: String? = null,
    // Cab booking fields (root level - for backward compatibility)
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
        android.util.Log.d("TravelApprovalHistory", "=== APPROVAL HISTORY ITEM CONVERSION ===")
        android.util.Log.d("TravelApprovalHistory", "Request ID: $requestId")
        android.util.Log.d("TravelApprovalHistory", "Mode of Transport: $modeOfTransport")
        android.util.Log.d("TravelApprovalHistory", "Status: $status")
        android.util.Log.d("TravelApprovalHistory", "Has cabDetails: ${cabDetails != null}")

        // Log cab details if present
        cabDetails?.let { cab ->
            android.util.Log.d("TravelApprovalHistory", "=== CAB DETAILS ===")
            android.util.Log.d("TravelApprovalHistory", "  travelType: ${cab.travelType}")
            android.util.Log.d("TravelApprovalHistory", "  cabType: ${cab.cabType}")
            android.util.Log.d("TravelApprovalHistory", "  travelDate: ${cab.travelDate}")
            android.util.Log.d("TravelApprovalHistory", "  duration: ${cab.duration}")
            android.util.Log.d("TravelApprovalHistory", "  dropLocation: ${cab.dropLocation}")
            android.util.Log.d("TravelApprovalHistory", "  dropMapDetails: ${cab.dropMapDetails}")
            android.util.Log.d("TravelApprovalHistory", "  Has pickups: ${cab.pickups != null}")
            android.util.Log.d("TravelApprovalHistory", "  Pickups count: ${cab.pickups?.size ?: 0}")
            cab.pickups?.forEachIndexed { index, pickup ->
                android.util.Log.d("TravelApprovalHistory", "    Pickup $index: location='${pickup.location}', mapDetails='${pickup.mapDetails}'")
            }
            android.util.Log.d("TravelApprovalHistory", "  Has additionalMembers: ${cab.additionalMembers != null}")
            android.util.Log.d("TravelApprovalHistory", "  Additional members count: ${cab.additionalMembers?.size ?: 0}")
            cab.additionalMembers?.forEachIndexed { index, member ->
                android.util.Log.d("TravelApprovalHistory", "    Member $index: name='${member.name}', email='${member.email}'")
            }
        }

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
        android.util.Log.d("TravelApprovalHistory", "travelDetails count: ${travelDetails?.size ?: "null"}")
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

        // Map cab details - prefer nested cabDetails object, fall back to root level fields
        val cabTravelType = cabDetails?.getActualTravelType() ?: travelType
        val cabTypeValue = cabDetails?.getActualCabType() ?: cabType
        val cabDuration = cabDetails?.duration ?: duration
        val cabTravelDate = cabDetails?.getActualTravelDate() ?: travelDate
        val cabPickupLocations = cabDetails?.getActualPickupLocations() ?: pickupLocations
        val cabPickupMapDetails = cabDetails?.pickups?.map { "${it.location}|${it.mapDetails ?: ""}" }
        val cabDropLocation = cabDetails?.getActualDropLocation() ?: dropLocation
        val cabDropMapDetails = cabDetails?.dropMapDetails
        val cabAdditionalMembers = cabDetails?.getActualAdditionalMembers() ?: additionalMembers

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
                // Cab booking fields (prefer cabDetails object)
                travelType = cabTravelType,
                cabType = cabTypeValue,
                travelDate = cabTravelDate,
                duration = cabDuration,
                pickupLocations = cabPickupLocations,
                pickupMapDetails = cabPickupMapDetails,
                dropLocation = cabDropLocation,
                dropMapDetails = cabDropMapDetails,
                additionalMembers = cabAdditionalMembers,
                projectId = projectId,
                opportunityId = opportunityId,
                crmId = crmId,
            )

        // Log what we're actually putting in the TravelRequest
        android.util.Log.d("TravelApprovalHistory", "=== MAPPED TO TRAVELREQUEST ===")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.id: ${travelRequest.id}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.employeeName: ${travelRequest.employeeName}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.employeeEmail: ${travelRequest.employeeEmail}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.status: ${travelRequest.status}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.travelType: ${travelRequest.travelType}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.cabType: ${travelRequest.cabType}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.travelDate: ${travelRequest.travelDate}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.duration: ${travelRequest.duration}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.pickupLocations: ${travelRequest.pickupLocations}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.pickupMapDetails: ${travelRequest.pickupMapDetails}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.dropLocation: ${travelRequest.dropLocation}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.dropMapDetails: ${travelRequest.dropMapDetails}")
        android.util.Log.d("TravelApprovalHistory", "  travelRequest.additionalMembers: ${travelRequest.additionalMembers}")
        android.util.Log.d("TravelApprovalHistory", "=== END APPROVAL HISTORY CONVERSION ===")

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
    @SerializedName("pending_history_count")
    val pendingHistoryCount: Int = 0,
    @SerializedName("isAdmin")
    val isAdmin: Boolean = false,
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
    @SerializedName("user_location")
    val userLocation: String? = null,
    @SerializedName("mode_of_transport")
    val modeOfTransport: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("start_date")
    val startDate: String? = null,
    @SerializedName("end_date")
    val endDate: String? = null,
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

/**
 * Custom deserializer for pickups field to handle both old format (string array)
 * and new format (object array with location and mapDetails)
 */
class PickupsDeserializer : JsonDeserializer<List<PickupLocationResponse>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<PickupLocationResponse> {
        if (json.isJsonNull) {
            return emptyList()
        }

        val jsonArray = json.asJsonArray
        val pickups = mutableListOf<PickupLocationResponse>()

        for (element in jsonArray) {
            when {
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    // Old format: just a string like "kochi"
                    val location = element.asString
                    pickups.add(PickupLocationResponse(location = location, mapDetails = null))
                }
                element.isJsonObject -> {
                    // New format: object with location and mapDetails
                    val pickup = context.deserialize<PickupLocationResponse>(
                        element,
                        PickupLocationResponse::class.java
                    )
                    pickups.add(pickup)
                }
            }
        }

        return pickups
    }
}

/**
 * Custom deserializer for additional members field to handle both old format (string array)
 * and new format (object array with name and email)
 */
class AdditionalMembersDeserializer : JsonDeserializer<List<AdditionalMemberResponse>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<AdditionalMemberResponse> {
        if (json.isJsonNull) {
            return emptyList()
        }

        val jsonArray = json.asJsonArray
        val members = mutableListOf<AdditionalMemberResponse>()

        for (element in jsonArray) {
            when {
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    // Old format: just a string name like "Pramukh JS"
                    val name = element.asString
                    members.add(AdditionalMemberResponse(name = name, email = null))
                }
                element.isJsonObject -> {
                    // New format: object with name and email
                    val member = context.deserialize<AdditionalMemberResponse>(
                        element,
                        AdditionalMemberResponse::class.java
                    )
                    members.add(member)
                }
            }
        }

        return members
    }
}
