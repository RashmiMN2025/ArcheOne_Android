package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for travel cancellation action (Admin only)
 * Returns updated admin history after cancellation
 */
data class TravelCancelActionResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("admin_history")
    val adminHistory: List<TravelApprovalHistoryItem>? = null,
)