package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for submitting a new travel request
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
    
    @SerializedName("travelDestination")
    val travelDestination: String,
    
    @SerializedName("projectName")
    val projectName: String,
    
    @SerializedName("businessJustification")
    val businessJustification: String,
    
    @SerializedName("modeOfTransport")
    val modeOfTransport: String,
    
    @SerializedName("departureDate")
    val departureDate: String,
    
    @SerializedName("arrivalDate")
    val arrivalDate: String,
    
    @SerializedName("reportingManagerName")
    val reportingManagerName: String,
    
    @SerializedName("reportingManagerEmail")
    val reportingManagerEmail: String,
    
    @SerializedName("grade")
    val grade: String,
    
    @SerializedName("aadhar_number")
    val aadharNumber: String,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String,
    
    @SerializedName("flightTime")
    val flightTime: String = "",
    
    @SerializedName("seatPref")
    val seatPreference: String = "",
    
    @SerializedName("mealPref")
    val mealPreference: String = "",
    
    @SerializedName("stayRequired")
    val stayRequired: Boolean = false,
    
    @SerializedName("frequentFlyerNum")
    val frequentFlyerNumber: String = "0"
)

/**
 * Response model for travel request submission
 */
data class TravelRequestResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("requestId")
    val requestId: String? = null,
    
    @SerializedName("status")
    val status: String? = null
)
