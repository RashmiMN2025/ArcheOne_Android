package com.archeGlobal.one.model

import java.util.Date

/**
 * Model class for travel requests
 */
data class TravelRequest(
    val id: String, // Format: TRV001, TRV002, etc.
    val project: String, // Project name
    val destination: String, // Travel destination (formatted for display)
    val approver: String, // Name of the approver
    val approverEmail: String? = null, // Email of the approver/reporting manager
    val createdDate: Date, // Date when the request was created
    val status: TravelStatus, // Status of the request
    val businessJustification: String? = null, // Business justification for the travel
    val modeOfTransport: String? = null, // Mode of transport for the travel
    val departureDate: String? = null, // Departure date for the travel
    val arrivalDate: String? = null, // Arrival date for the travel
    val actionToken: String? = null, // Token required for approve/reject API
    val rejectionReason: String? = null, // Reason for rejection if status is REJECTED
    val stayRequired: String? = null, // Whether stay is required (Yes/No)
    val mealPreference: String? = null, // Meal preference
    val seatPreference: String? = null, // Seat preference
    val flightTime: String? = null, // Preferred flight time
    val frequentFlyerNumber: String? = null, // Frequent flyer number if applicable
    val travelDestinations: List<TravelDestination>? = null, // Multi-destination support
    // Employee/Sender details
    val employeeName: String? = null, // Name of the employee who requested the travel
    val employeeEmail: String? = null, // Email of the employee who requested the travel
    val employeeId: String? = null, // ID of the employee who requested the travel
    val employeeMobile: String? = null, // Mobile number of the employee who requested the travel
) {
    /**
     * Check if this is a multi-destination travel request
     */
    fun isMultiDestination(): Boolean = travelDestinations != null && travelDestinations.size > 1

    /**
     * Get all destinations for multi-destination travel
     */
    fun getAllDestinations(): List<TravelDestination> = travelDestinations ?: emptyList()
}

/**
 * Represents the possible statuses of a travel request
 */
enum class TravelStatus {
    APPROVED,
    REJECTED,
    PENDING,
    CANCELLED,
}
