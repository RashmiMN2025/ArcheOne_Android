package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class LeaveRequest(
    @SerializedName("userEmail") val userEmail: String
)

data class LeaveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: LeaveContainer
)

data class LeaveContainer(
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("leaves") val leaves: List<LeaveData>
)

data class LeaveData(
    @SerializedName("leaveType") val leaveType: String,
    @SerializedName("currentYearEligibility") val currentYearEligibility: Double,
    @SerializedName("leaveTaken") val leaveTaken: Double,
    @SerializedName("availableBalance") val availableBalance: Double,
    @SerializedName("pendingRequests") val pendingRequests: Double,
    @SerializedName("effectiveBalance") val effectiveBalance: Double
)

data class CreateLeaveRequest(
    @SerializedName("employeeName") val employeeName: String,
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("requestType") val requestType: String,
    @SerializedName("leaveDuration") val leaveDuration: String,
    @SerializedName("description") val description: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("punchIn") val punchIn: String? = null,
    @SerializedName("punchOut") val punchOut: String? = null
)

data class CreateLeaveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: CreateLeaveResponseData? = null
)

data class CreateLeaveResponseData(
    @SerializedName("requests") val requests: List<LeaveRequestItem>,
    @SerializedName("eventId") val eventId: String,
    @SerializedName("totalDays") val totalDays: Int
)

data class LeaveRequestItem(
    @SerializedName("id") val id: String,
    @SerializedName("eventId") val eventId: String,
    @SerializedName("employeeName") val employeeName: String,
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("requestDate") val requestDate: String,
    @SerializedName("requestType") val requestType: String,
    @SerializedName("leaveDuration") val leaveDuration: String,
    @SerializedName("approver") val approver: String,
    @SerializedName("approverEmail") val approverEmail: String,
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
