package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class PunchInRequest(
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("employeeName") val employeeName: String,
    @SerializedName("employeeEmail") val employeeEmail: String,
    @SerializedName("location") val location: String
)

data class PunchInResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: PunchInData?
)

data class PunchInData(
    @SerializedName("id") val id: Int,
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("employeeName") val employeeName: String,
    @SerializedName("employeeEmail") val employeeEmail: String,
    @SerializedName("date") val date: String,
    @SerializedName("day") val day: String,
    @SerializedName("punchIn") val punchIn: String,
    @SerializedName("punchOut") val punchOut: List<String>,
    @SerializedName("workingHours") val workingHours: String,
    @SerializedName("deficit") val deficit: String,
    @SerializedName("attendanceStatus") val attendanceStatus: String,
    @SerializedName("location") val location: String
)

data class PunchOutRequest(
    @SerializedName("id") val id: Int
)

data class PunchOutResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: PunchInData?
)
