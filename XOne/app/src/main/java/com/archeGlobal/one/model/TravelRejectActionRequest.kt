package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for rejecting a travel request
 */
data class TravelRejectActionRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("remarks")
    val remarks: String,
)
