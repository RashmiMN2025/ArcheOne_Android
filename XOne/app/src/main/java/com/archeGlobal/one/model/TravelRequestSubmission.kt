package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a single travel destination for API responses
 * Uses snake_case format from API responses
 */
data class TravelDestination(
    @SerializedName("travel_destination")
    val travelDestination: String,
    @SerializedName("origin_city")
    val originCity: String,
    @SerializedName("destination_city")
    val destinationCity: String,
    @SerializedName("departure_date") val departureDate: String,
    @SerializedName("arrival_date")
    val arrivalDate: String,
    @SerializedName("flight_time")
    val flightTimePreference: String = "",
)

/**
 * Data class representing a single travel detail for API requests
 * Uses camelCase format to match iOS implementation
 */
data class TravelDetail(
    @SerializedName("originCity")
    val originCity: String = "N/A",
    @SerializedName("destinationCity")
    val destinationCity: String,
    @SerializedName("departureDate")
    val departureDate: String,
    @SerializedName("arrivalDate")
    val arrivalDate: String,
    @SerializedName("flightTimePreference")
    val flightTime: String = "",
)

/**
 * Additional member for cab booking
 */
data class AdditionalMember(
    @SerializedName("name")
    val name: String,
)

/**
 * Cab details for travel request
 */
data class CabDetail(
    @SerializedName("travelType")
    val travelType: String,
    @SerializedName("cabType")
    val cabType: String,
    @SerializedName("travelDate")
    val travelDate: String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("pickupLocations")
    val pickupLocations: List<String>,
    @SerializedName("dropLocation")
    val dropLocation: String,
    @SerializedName("additionalMembers")
    val additionalMembers: List<AdditionalMember> = emptyList(),
)

/**
 * Request model for submitting a new travel request
 * Uses exact format to match working iOS implementation
 */
data class TravelRequestSubmission(
    @SerializedName("employeeName")
    val employeeName: String,
    @SerializedName("employeeEmail")
    val employeeEmail: String,
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("mobile")
    val mobile: String = "",
    @SerializedName("destinations")
    val travelDetails: List<TravelDetail> = emptyList(),
    @SerializedName("projectName")
    val projectName: String = "",
    @SerializedName("projectID")
    val projectID: String = "",
    @SerializedName("opportunityID")
    val opportunityID: String = "",
    @SerializedName("crmID")
    val crmID: String = "",
    @SerializedName("businessJustification")
    val businessJustification: String = "",
    @SerializedName("modeOfTransport")
    val modeOfTransport: String,
    @SerializedName("flightType")
    val flightType: String = "",
    @SerializedName("reportingManagerName")
    val reportingManagerName: String = "",
    @SerializedName("reportingManagerEmail")
    val reportingManagerEmail: String = "",
    @SerializedName("stayRequired")
    val stayRequired: Boolean = false,
    @SerializedName("cabRequired")
    val cabRequired: Boolean = false,
    @SerializedName("grade")
    val grade: String = "",
    @SerializedName("aadhar_number")
    val aadharNumber: String = "",
    @SerializedName("date_of_birth")
    val dateOfBirth: String = "",
    @SerializedName("frequentFlyerNum")
    val frequentFlyerNum: String = "",
    @SerializedName("mealPreference")
    val mealPref: String = "",
    @SerializedName("seatPreference")
    val seatPref: String = "",
    @SerializedName("cabDetails")
    val cabDetails: List<CabDetail> = emptyList(),
    @SerializedName("remarks")
    val remarks: String = "",
    @SerializedName("user_location")
    val userLocation: String = "",
    @SerializedName("multiTravel")
    val multiTravel: Boolean = false,
)

/**
 * Response model for travel request submission
 * Updated to handle both camelCase and snake_case responses
 */
data class TravelRequestResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    // Support both camelCase and snake_case for order_history
    @SerializedName("orderHistory")
    val orderHistory: List<TravelHistoryItem>? = null,
    @SerializedName("order_history")
    val orderHistorySnakeCase: List<TravelHistoryItem>? = null,
) {
    // Helper function to get order history regardless of format
    fun getAllOrderHistory(): List<TravelHistoryItem>? = orderHistory ?: orderHistorySnakeCase
}

/**
 * Helper function to create single destination travel request
 */
fun createSingleDestinationRequest(
    employeeId: String,
    employeeName: String,
    employeeEmail: String,
    mobile: String,
    originCity: String,
    destinationCity: String,
    projectName: String,
    businessJustification: String,
    projectId: String = "",
    opportunityId: String = "",
    crmId: String = "",
    modeOfTransport: String,
    departureDate: String,
    arrivalDate: String,
    reportingManagerName: String,
    reportingManagerEmail: String,
    stayRequired: Boolean,
    cabRequired: Boolean = false,
    grade: String,
    aadharNumber: String,
    dateOfBirth: String,
    frequentFlyerNumber: String,
    mealPreference: String,
    seatPreference: String,
    flightTime: String,
    cabDetails: List<CabDetail> = emptyList(),
    remarks: String = "",
    userLocation: String = "",
): TravelRequestSubmission {
    val travelDetail =
        TravelDetail(
            originCity = originCity,
            destinationCity = destinationCity,
            departureDate = departureDate,
            arrivalDate = arrivalDate,
            flightTime = flightTime,
        )

    return TravelRequestSubmission(
        employeeName = employeeName,
        employeeEmail = employeeEmail,
        employeeId = employeeId,
        mobile = mobile,
        travelDetails = listOf(travelDetail),
        projectName = projectName,
        projectID = projectId,
        opportunityID = opportunityId,
        crmID = crmId,
        businessJustification = businessJustification,
        modeOfTransport = modeOfTransport,
        flightType = "",
        reportingManagerName = reportingManagerName,
        reportingManagerEmail = reportingManagerEmail,
        stayRequired = stayRequired,
        cabRequired = cabRequired,
        grade = grade,
        aadharNumber = aadharNumber,
        dateOfBirth = dateOfBirth,
        frequentFlyerNum = frequentFlyerNumber,
        mealPref = mealPreference,
        seatPref = seatPreference,
        cabDetails = cabDetails,
        remarks = remarks,
        userLocation = userLocation,
        multiTravel = false,
    )
}

/**
 * Helper function to create multi-destination travel request
 */
fun createMultiDestinationRequest(
    employeeId: String,
    employeeName: String,
    employeeEmail: String,
    mobile: String,
    projectName: String,
    businessJustification: String,
    projectId: String = "",
    opportunityId: String = "",
    crmId: String = "",
    modeOfTransport: String,
    reportingManagerName: String,
    reportingManagerEmail: String,
    stayRequired: Boolean,
    cabRequired: Boolean = false,
    grade: String,
    aadharNumber: String,
    dateOfBirth: String,
    frequentFlyerNumber: String,
    mealPreference: String,
    seatPreference: String,
    travelDetails: List<TravelDetail>,
    cabDetails: List<CabDetail> = emptyList(),
    remarks: String = "",
    userLocation: String = "",
): TravelRequestSubmission =
    TravelRequestSubmission(
        employeeName = employeeName,
        employeeEmail = employeeEmail,
        employeeId = employeeId,
        mobile = mobile,
        travelDetails = travelDetails,
        projectName = projectName,
        projectID = projectId,
        opportunityID = opportunityId,
        crmID = crmId,
        businessJustification = businessJustification,
        modeOfTransport = modeOfTransport,
        flightType = "",
        reportingManagerName = reportingManagerName,
        reportingManagerEmail = reportingManagerEmail,
        stayRequired = stayRequired,
        cabRequired = cabRequired,
        grade = grade,
        aadharNumber = aadharNumber,
        dateOfBirth = dateOfBirth,
        frequentFlyerNum = frequentFlyerNumber,
        mealPref = mealPreference,
        seatPref = seatPreference,
        cabDetails = cabDetails,
        remarks = remarks,
        userLocation = userLocation,
        multiTravel = true,
    )
