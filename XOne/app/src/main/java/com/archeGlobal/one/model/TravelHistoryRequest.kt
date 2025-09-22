package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for getting travel history
 */
data class TravelHistoryRequest(
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("employeeEmail")
    val employeeEmail: String,
)
