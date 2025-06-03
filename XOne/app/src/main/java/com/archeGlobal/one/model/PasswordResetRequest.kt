package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class PasswordResetRequest(
    @SerializedName("email") val email: String,
    @SerializedName("employeeId") val employeeId: String
)

data class PasswordResetResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("newPassword") val newPassword: String? = null,
    @SerializedName("message") val message: String? = null
)
