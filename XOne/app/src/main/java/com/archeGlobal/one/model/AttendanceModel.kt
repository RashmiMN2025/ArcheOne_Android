package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class LeaveBalance(
    val type: String,
    val balance: Double,
)

enum class AttendanceDayStatus {
    PRESENT,
    ABSENT,
    LATE,
    LEAVE,
    HOLIDAY,
    WEEKEND,
}

data class AttendanceRequest(
    @SerializedName("employeeEmail") val employeeEmail: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
)

data class AttendanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<AttendanceDayData>,
)

data class AttendanceDayData(
    @SerializedName("date") val date: String,
    @SerializedName("day") val day: String,
    @SerializedName("punchIn") val punchIn: String?,
    @SerializedName("punchOut") val punchOut: List<String>?,
    @SerializedName("workingHours") val workingHours: String?,
    @SerializedName("deficit") val deficit: String?,
    @SerializedName("attendanceStatus") val attendanceStatus: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("requests") val requests: List<AttendanceDayRequest>?,
)

data class AttendanceDayRequest(
    @SerializedName("id") val id: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String,
    @SerializedName("punchIn") val punchIn: String?,
    @SerializedName("punchOut") val punchOut: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("requestType") val requestType: String,
    @SerializedName("leaveDuration") val leaveDuration: String,
)
