package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for travel approval/rejection actions
 */
data class TravelApprovalActionResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
)
