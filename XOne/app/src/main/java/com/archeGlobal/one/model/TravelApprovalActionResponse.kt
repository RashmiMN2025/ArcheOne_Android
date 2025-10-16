package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for travel approval/rejection actions (V2 API)
 * Returns updated approval history after action
 */
data class TravelApprovalActionResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("approval_history")
    val approvalHistory: TravelApprovalHistoryItem? = null,
)
