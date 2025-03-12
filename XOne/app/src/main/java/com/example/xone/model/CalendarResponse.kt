package com.example.xone.model

import com.google.gson.annotations.SerializedName

data class CalendarResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("milestones") val milestones: List<Milestone> = emptyList(),
    @SerializedName("holidays") val holidays: List<Holiday>,
    @SerializedName("holidaysFile") val holidaysFile: String
) 