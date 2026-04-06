package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class LeaveRequest(
    @SerializedName("employeeCode") val employeeCode: String
)

data class LeaveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<LeaveData>
)

data class LeaveData(
    @SerializedName("id") val id: Int,
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("employeeName") val employeeName: String,
    @SerializedName("leaveType") val leaveType: String,
    @SerializedName("currentYearEligibility") val currentYearEligibility: Double,
    @SerializedName("leaveTaken") val leaveTaken: Double,
    @SerializedName("availableBalance") val availableBalance: Double,
    @SerializedName("directReporting") val directReporting: String,
    @SerializedName("managerName") val managerName: String
)
