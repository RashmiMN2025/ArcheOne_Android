package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Enhanced Cab Booking Request Model to match the /travel/v2/request API endpoint
 * This matches the unified travel request format that handles both travel and cab bookings
 */
data class CabBookingRequest(
    @SerializedName("employeeName")
    val employeeName: String,
    @SerializedName("employeeEmail")
    val employeeEmail: String,
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("destinations")
    val destinations: List<Any> = emptyList(), // Empty array for cab bookings
    @SerializedName("projectName")
    val projectName: String? = null,
    @SerializedName("projectID")
    val projectID: String? = null,
    @SerializedName("opportunityID")
    val opportunityID: String? = null,
    @SerializedName("crmID")
    val crmID: String? = null,
    @SerializedName("businessJustification")
    val businessJustification: String,
    @SerializedName("modeOfTransport")
    val modeOfTransport: String = "Cab",
    @SerializedName("flightType")
    val flightType: String? = null,
    @SerializedName("reportingManagerName")
    val reportingManagerName: String,
    @SerializedName("reportingManagerEmail")
    val reportingManagerEmail: String,
    @SerializedName("stayRequired")
    val stayRequired: Boolean = false,
    @SerializedName("cabRequired")
    val cabRequired: Boolean = true,
    @SerializedName("grade")
    val grade: String,
    @SerializedName("aadhar_number")
    val aadharNumber: String,
    @SerializedName("date_of_birth")
    val dateOfBirth: String,
    @SerializedName("frequentFlyerNum")
    val frequentFlyerNum: String? = null,
    @SerializedName("mealPreference")
    val mealPreference: String? = null,
    @SerializedName("seatPreference")
    val seatPreference: String? = null,
    @SerializedName("cabDetails")
    val cabDetails: List<CabDetail>,
    @SerializedName("remarks")
    val remarks: String? = null,
    @SerializedName("user_location")
    val userLocation: String? = null,
)


/**
 * Cab Attendee Model for additional passengers
 */
data class CabAttendee(
    @SerializedName("name")
    val name: String,
    @SerializedName("employeeId")
    val employeeId: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("department")
    val department: String? = null,
)

/**
 * Cab Location Model for pickup points
 */
data class CabLocation(
    @SerializedName("id")
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    @SerializedName("address")
    val address: String,
    @SerializedName("order")
    val order: Int,
    @SerializedName("isPickup")
    val isPickup: Boolean = true, // true for pickup, false for drop
)

/**
 * Cab Booking Response Model
 */
data class CabBookingResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("bookingId")
    val bookingId: String? = null,
    @SerializedName("requestId")
    val requestId: String? = null,
    @SerializedName("estimatedCost")
    val estimatedCost: Double? = null,
    @SerializedName("driverDetails")
    val driverDetails: CabDriverDetails? = null,
)

/**
 * Cab Driver Details (if provided immediately)
 */
data class CabDriverDetails(
    @SerializedName("name")
    val name: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("vehicleNumber")
    val vehicleNumber: String,
    @SerializedName("vehicleType")
    val vehicleType: String,
)

/**
 * Suggested User for simple user search
 */
data class SuggestedUser(
    @SerializedName("mail")
    val mail: String,
    @SerializedName("displayName")
    val displayName: String,
)

/**
 * Employee Search Request for finding additional attendees
 */
data class EmployeeSearchRequest(
    @SerializedName("query")
    val query: String,
    @SerializedName("searchType")
    val searchType: String = "name", // "name", "email", "employeeId"
    @SerializedName("department")
    val department: String? = null,
    @SerializedName("limit")
    val limit: Int = 10,
)

/**
 * Employee Search Response
 */
data class EmployeeSearchResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("employees")
    val employees: List<EmployeeSearchResult>,
)

/**
 * Employee Search Result
 */
data class EmployeeSearchResult(
    @SerializedName("name")
    val name: String,
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("department")
    val department: String,
    @SerializedName("designation")
    val designation: String? = null,
    @SerializedName("location")
    val location: String? = null,
)

/**
 * Cab History Request Model
 */
data class CabHistoryRequest(
    @SerializedName("employeeEmail")
    val employeeEmail: String,
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("fromDate")
    val fromDate: String? = null,
    @SerializedName("toDate")
    val toDate: String? = null,
)

/**
 * Cab History Response Model
 */
data class CabHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("cabBookings")
    val cabBookings: List<CabHistoryItem>,
)

/**
 * Cab History Item Model
 */
data class CabHistoryItem(
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("bookingDate")
    val bookingDate: String,
    @SerializedName("travelDate")
    val travelDate: String,
    @SerializedName("travelType")
    val travelType: String,
    @SerializedName("cabType")
    val cabType: String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("pickupLocation")
    val pickupLocation: String,
    @SerializedName("dropLocation")
    val dropLocation: String,
    @SerializedName("status")
    val status: String, // "Pending", "Approved", "Rejected", "Completed"
    @SerializedName("driverDetails")
    val driverDetails: CabDriverDetails? = null,
    @SerializedName("totalCost")
    val totalCost: Double? = null,
    @SerializedName("approverName")
    val approverName: String? = null,
    @SerializedName("remarks")
    val remarks: String? = null,
) {
    /**
     * Convert to TravelRequest for unified display in travel history
     */
    fun toTravelRequest(): TravelRequest =
        TravelRequest(
            id = requestId,
            project = "Cab Booking",
            destination = "$pickupLocation → $dropLocation",
            approver = approverName ?: "Manager",
            approverEmail = null,
            createdDate = java.util.Date(), // Would need proper date parsing
            status =
                when (status.toLowerCase()) {
                    "approved" -> TravelStatus.APPROVED
                    "rejected" -> TravelStatus.REJECTED
                    "cancelled" -> TravelStatus.CANCELLED
                    else -> TravelStatus.PENDING
                },
            businessJustification = null,
            modeOfTransport = "Cab",
            departureDate = travelDate,
            arrivalDate = travelDate,
        )
}

/**
 * Helper function to create cab booking request matching /travel/v2/request API format
 */
fun createCabBookingRequest(
    employeeId: String,
    employeeName: String,
    employeeEmail: String,
    mobile: String,
    projectName: String?,
    projectId: String?,
    opportunityId: String?,
    crmId: String?,
    businessJustification: String,
    travelType: String,
    travelDate: String,
    passengerCount: Int,
    additionalAttendees: List<CabAttendee>,
    cabType: String,
    duration: String,
    pickupLocations: List<CabLocation>,
    dropLocation: String,
    reportingManagerName: String,
    reportingManagerEmail: String,
    grade: String,
    aadharNumber: String,
    dateOfBirth: String,
): CabBookingRequest {
    // Convert CabLocation list to simple string addresses
    val pickupAddresses = pickupLocations.map { it.address }

    // Convert CabAttendee list to AdditionalMember list
    val additionalMembers = additionalAttendees.map {
        AdditionalMember(name = it.name)
    }

    // Create the cabDetails array
    val cabDetails = listOf(
        CabDetail(
            travelType = travelType, // "Local Travel" or "Out of Local Station"
            cabType = cabType,
            travelDate = travelDate,
            duration = duration,
            pickupLocations = pickupAddresses,
            dropLocation = dropLocation,
            additionalMembers = additionalMembers
        )
    )

    return CabBookingRequest(
        employeeName = employeeName,
        employeeEmail = employeeEmail,
        employeeId = employeeId,
        mobile = mobile,
        destinations = emptyList(), // Always empty for cab bookings
        projectName = projectName,
        projectID = projectId,
        opportunityID = opportunityId,
        crmID = crmId,
        businessJustification = businessJustification,
        modeOfTransport = "Cab",
        flightType = null,
        reportingManagerName = reportingManagerName,
        reportingManagerEmail = reportingManagerEmail,
        stayRequired = false,
        cabRequired = true,
        grade = grade,
        aadharNumber = aadharNumber,
        dateOfBirth = dateOfBirth,
        frequentFlyerNum = null,
        mealPreference = null,
        seatPreference = null,
        cabDetails = cabDetails,
        remarks = null,
        userLocation = null
    )
}
