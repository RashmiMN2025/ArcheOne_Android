package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for approving or rejecting a travel request
 */
data class TravelApprovalActionRequest(
    @SerializedName("request_id")
    val requestId: String,
    
    @SerializedName("action_token")
    val actionToken: String,
    
    @SerializedName("action")
    val action: String, // "approve" or "reject"
    
    @SerializedName("remarks")
    val remarks: String? = null // Optional remarks for rejection
)
