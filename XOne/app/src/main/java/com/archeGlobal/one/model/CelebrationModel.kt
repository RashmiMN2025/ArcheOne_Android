package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class CelebrationResponse(
    @SerializedName("today") val today: List<CelebrationItem> = emptyList(),
    @SerializedName("tomorrow") val tomorrow: List<CelebrationItem> = emptyList()
)

data class CelebrationItem(
    @SerializedName("employee_name") val employeeName: String,
    @SerializedName("email") val email: String,
    @SerializedName("celebration_type") val celebrationType: String,
    @SerializedName("profile_pic") val profilePic: String = ""
)