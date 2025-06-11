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
    val token: String,
    
    @SerializedName("remarks")
    val remarks: String? = null, // Optional remarks for approval or rejection
    
    @SerializedName("action")
    val action: String? = null // Action to take: "approve" or "reject"
)
