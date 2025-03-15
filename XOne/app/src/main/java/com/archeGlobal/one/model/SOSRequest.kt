package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class SOSRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("category") val category: String, // Ensure this matches backend
    @SerializedName("query") val query: String // Ensure this matches backend
)
