package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for getting travel approval history
 */
data class TravelApprovalRequest(
    @SerializedName("managerEmail")
    val managerEmail: String,
)
