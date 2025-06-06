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
    val reportingManagerEmail: String
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
