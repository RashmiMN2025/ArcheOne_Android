package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class SOSRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("category") val category: String, // Ensure this matches backend
    @SerializedName("subcategory") val subcategory: String? = null, // New subcategory field
    @SerializedName("query") val query: String, // Ensure this matches backend
    @SerializedName("anonymous") val anonymous: Boolean = false,
)

data class SOSResponse(
    val status: Boolean,
    val message: String,
)

data class EncryptedSOSResponse(
    val status: Int,
    val message: String,
    val encryptedData: String? = null,
)
