package com.archeGlobal.one.network

import com.google.gson.annotations.SerializedName

data class EntraLoginRequest(
    @SerializedName("id_token")
    val idToken: String,
)

data class EntraLoginResponse(
    val email: String,
    val id: Int,
    @SerializedName("refresh_token")
    val refreshToken: String,
)
