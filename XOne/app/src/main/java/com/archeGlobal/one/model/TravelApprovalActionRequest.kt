package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for approving or rejecting a travel request
 */
data class TravelApprovalActionRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("requestId")
    val requestId: String,

    @SerializedName("token")
    val token: String
)
