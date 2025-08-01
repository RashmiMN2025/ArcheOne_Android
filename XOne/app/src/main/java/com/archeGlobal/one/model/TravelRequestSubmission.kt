package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a single travel destination for API responses
 * Uses snake_case format from API responses
 */
data class TravelDestination(
    @SerializedName("travel_destination")
    val travelDestination: String,

    @SerializedName("departure_date") val departureDate: String,

    @SerializedName("arrival_date")
    val arrivalDate: String,

    @SerializedName("flight_time")
    val flightTimePreference: String = ""
)

/**
 * Data class representing a single travel destination for API requests
 * Uses camelCase format for API requests
 */
data class TravelDestinationRequest(
    @SerializedName("origin_city")
    val originCity: String,

    @SerializedName("destination_city")
    val destinationCity: String,

    @SerializedName("departureDate")
    val departureDate: String,

    @SerializedName("arrivalDate")
    val arrivalDate: String,

    @SerializedName("flightTimePreference")
    val flightTimePreference: String = ""
)

/**
 * Request model for submitting a new travel request
 * Supports both single and multi-destination travel
 */
data class TravelRequestSubmission(
    @SerializedName("employeeId")
    val employeeId: String,

    @SerializedName("employeeName")
    val employeeName: String,

    @SerializedName("employeeEmail")
    val employeeEmail: String,

    @SerializedName("mobile")
    val mobile: String,

    @SerializedName("projectName")
    val projectName: String,

    @SerializedName("businessJustification")
    val businessJustification: String,

    @SerializedName("modeOfTransport")
    val modeOfTransport: String,

    @SerializedName("reportingManagerName")
    val reportingManagerName: String,

    @SerializedName("reportingManagerEmail")
    val reportingManagerEmail: String,

    @SerializedName("stayRequired")
    val stayRequired: Boolean = false,

    @SerializedName("grade")
    val grade: String,

    @SerializedName("aadhar_number")
    val aadharNumber: String,

    @SerializedName("date_of_birth")
    val dateOfBirth: String,

    @SerializedName("frequentFlyerNumber")
    val frequentFlyerNumber: String = "0",

    @SerializedName("mealPreference")
    val mealPreference: String = "",

    @SerializedName("seatPreference")
    val seatPreference: String = "",

    // Multi-destination support
    @SerializedName("destinations")
    val destinations: List<TravelDestinationRequest>,

    // Legacy fields for backward compatibility (single destination)
    @SerializedName("origin_city")
    val originCity: String? = null,

    @SerializedName("destination_city")
    val destinationCity: String? = null,

    @SerializedName("departureDate")
    val departureDate: String? = null,

    @SerializedName("arrivalDate")
    val arrivalDate: String? = null,

    @SerializedName("flightTime")
    val flightTime: String? = null
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
    val orderHistorySnakeCase: List<TravelHistoryItem>? = null
) {
    // Helper function to get order history regardless of format
    fun getAllOrderHistory(): List<TravelHistoryItem>? {
        return orderHistory ?: orderHistorySnakeCase
    }
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
    modeOfTransport: String,
    departureDate: String,
    arrivalDate: String,
    reportingManagerName: String,
    reportingManagerEmail: String,
    stayRequired: Boolean,
    grade: String,
    aadharNumber: String,
    dateOfBirth: String,
    frequentFlyerNumber: String,
    mealPreference: String,
    seatPreference: String,
    flightTime: String
): TravelRequestSubmission {
    val destination = TravelDestinationRequest(
        originCity = originCity,
        destinationCity = destinationCity,
        departureDate = departureDate,
        arrivalDate = arrivalDate,
        flightTimePreference = flightTime
    )

    return TravelRequestSubmission(
        employeeId = employeeId,
        employeeName = employeeName,
        employeeEmail = employeeEmail,
        mobile = mobile,
        projectName = projectName,
        businessJustification = businessJustification,
        modeOfTransport = modeOfTransport,
        reportingManagerName = reportingManagerName,
        reportingManagerEmail = reportingManagerEmail,
        stayRequired = stayRequired,
        grade = grade,
        aadharNumber = aadharNumber,
        dateOfBirth = dateOfBirth,
        frequentFlyerNumber = frequentFlyerNumber,
        mealPreference = mealPreference,
        seatPreference = seatPreference,
        destinations = listOf(destination),
        // Legacy fields for backward compatibility
        originCity = originCity,
        destinationCity = destinationCity,
        departureDate = departureDate,
        arrivalDate = arrivalDate,
        flightTime = flightTime
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
    modeOfTransport: String,
    reportingManagerName: String,
    reportingManagerEmail: String,
    stayRequired: Boolean,
    grade: String,
    aadharNumber: String,
    dateOfBirth: String,
    frequentFlyerNumber: String,
    mealPreference: String,
    seatPreference: String,
    destinations: List<TravelDestinationRequest>
): TravelRequestSubmission {
    return TravelRequestSubmission(
        employeeId = employeeId,
        employeeName = employeeName,
        employeeEmail = employeeEmail,
        mobile = mobile,
        projectName = projectName,
        businessJustification = businessJustification,
        modeOfTransport = modeOfTransport,
        reportingManagerName = reportingManagerName,
        reportingManagerEmail = reportingManagerEmail,
        stayRequired = stayRequired,
        grade = grade,
        aadharNumber = aadharNumber,
        dateOfBirth = dateOfBirth,
        frequentFlyerNumber = frequentFlyerNumber,
        mealPreference = mealPreference,
        seatPreference = seatPreference,
        destinations = destinations,
        // Don't include legacy fields for multi-destination
       // travelDestination = null,
        departureDate = null,
        arrivalDate = null,
        flightTime = null
    )
}
