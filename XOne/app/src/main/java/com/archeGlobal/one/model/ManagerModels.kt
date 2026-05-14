package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class ManagerDashboardRequest(
    @SerializedName("approverEmail") val approverEmail: String
)

data class ManagerDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: ManagerDashboardData? = null
)

data class ManagerDashboardData(
    @SerializedName("requests") val requests: List<ApprovalRequestItem>,
    @SerializedName("total") val total: Int
)

data class ApprovalRequestItem(
    @SerializedName("eventId") val eventId: String? = null,
    @SerializedName("employeeName") val employeeName: String? = "",
    @SerializedName("employeeCode") val employeeCode: String? = "",
    @SerializedName("startDate") val startDate: String? = "",
    @SerializedName("endDate") val endDate: String? = "",
    @SerializedName("totalDays") val totalDays: Int? = 0,
    @SerializedName("requestType") val requestType: String? = "",
    @SerializedName("leaveDuration") val leaveDuration: String? = null,
    @SerializedName("approver") val approver: String? = "",
    @SerializedName("approverEmail") val approverEmail: String? = "",
    @SerializedName("secondApprover") val secondApprover: String? = null,
    @SerializedName("secondApproverEmail") val secondApproverEmail: String? = null,
    @SerializedName("status") val status: String? = "pending",
    @SerializedName("reason") val reason: String? = "",
    @SerializedName("description") val description: String? = "",
    @SerializedName("punchIn") val punchIn: String? = null,
    @SerializedName("punchOut") val punchOut: String? = null,
    @SerializedName("createdAt") val createdAt: String? = "",
    @SerializedName("updatedAt") val updatedAt: String? = "",
    @SerializedName("category") val category: String? = "approval request",
)

data class ApprovalActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: ApprovalActionData? = null
)

data class ApprovalActionData(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class RejectActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: RejectActionData? = null
)

data class RejectActionData(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class DeletionRequestCreateBody(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("reason") val reason: String,
)

data class DeletionRequestCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: DeletionRequestCreateData? = null,
)

data class DeletionRequestCreateData(
    @SerializedName("id") val id: String? = null,
    @SerializedName("eventId") val eventId: String? = null,
    @SerializedName("status") val status: String? = null,
)