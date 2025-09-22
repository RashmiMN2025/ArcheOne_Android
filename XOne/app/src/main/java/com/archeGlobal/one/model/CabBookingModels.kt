package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Enhanced Cab Booking Request Model to match the UI design specifications
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
    @SerializedName("projectName")
    val projectName: String? = null,
    @SerializedName("projectId")
    val projectId: String? = null,
    @SerializedName("opportunityId")
    val opportunityId: String? = null,
    @SerializedName("crmId")
    val crmId: String? = null,
    @SerializedName("businessJustification")
    val businessJustification: String,
    @SerializedName("serviceType")
    val serviceType: String = "Cab",
    @SerializedName("travelType")
    val travelType: String, // "Local Travel" or "Out of Local Station"
    @SerializedName("travelDate")
    val travelDate: String,
    @SerializedName("passengerCount")
    val passengerCount: Int, // Number of seats (5, 7, etc.)
    @SerializedName("additionalAttendees")
    val additionalAttendees: List<CabAttendee> = emptyList(),
    @SerializedName("cabType")
    val cabType: String, // "6 Seater", "7 Seater", etc.
    @SerializedName("duration")
    val duration: String, // "4 Hours", "8 Hours"
    @SerializedName("pickupLocations")
    val pickupLocations: List<CabLocation>,
    @SerializedName("dropLocation")
    val dropLocation: String,
    @SerializedName("reportingManagerName")
    val reportingManagerName: String,
    @SerializedName("reportingManagerEmail")
    val reportingManagerEmail: String,
    @SerializedName("grade")
    val grade: String,
    @SerializedName("aadharNumber")
    val aadharNumber: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
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
                    else -> TravelStatus.PENDING
                },
            businessJustification = null,
            modeOfTransport = "Cab",
            departureDate = travelDate,
            arrivalDate = travelDate,
        )
}

/**
 * Helper function to create cab booking request
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
): CabBookingRequest =
    CabBookingRequest(
        employeeName = employeeName,
        employeeEmail = employeeEmail,
        employeeId = employeeId,
        mobile = mobile,
        projectName = projectName,
        projectId = projectId,
        opportunityId = opportunityId,
        crmId = crmId,
        businessJustification = businessJustification,
        serviceType = "Cab",
        travelType = travelType,
        travelDate = travelDate,
        passengerCount = passengerCount,
        additionalAttendees = additionalAttendees,
        cabType = cabType,
        duration = duration,
        pickupLocations = pickupLocations,
        dropLocation = dropLocation,
        reportingManagerName = reportingManagerName,
        reportingManagerEmail = reportingManagerEmail,
        grade = grade,
        aadharNumber = aadharNumber,
        dateOfBirth = dateOfBirth,
    )
