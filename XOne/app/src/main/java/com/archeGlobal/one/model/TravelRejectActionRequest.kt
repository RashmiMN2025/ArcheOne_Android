package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for rejecting a travel request (V2 API)
 */
data class TravelRejectActionRequest(
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("remarks")
    val remarks: String,
)
