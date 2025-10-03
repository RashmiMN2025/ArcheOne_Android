package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for cancelling an approved travel request (Admin only)
 */
data class TravelCancelActionRequest(
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("remarks")
    val remarks: String,
)