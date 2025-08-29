package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HolidayResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("holidays") val holidays: List<Holiday>,
    @SerializedName("holidaysFile") val holidaysFile: String
)

data class Holiday(
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String, // Format: "DD-MM-YYYY"
    @SerializedName(value = "holidayType", alternate = ["holiday_type", "type"]) val holidayType: String, // "Yes", "RH", or "NA"
    @SerializedName("icon") val icon: String? = null, // URL to the holiday icon
    @SerializedName("description") val description: String
) {
    val isApplicable: Boolean
        get() = holidayType == "Yes" || holidayType == "RH"

    val month: Int
        get() = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val localDate = LocalDate.parse(date, formatter)
            localDate.monthValue
        } catch (e: Exception) {
            0
        }

    val day: Int
        get() = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val localDate = LocalDate.parse(date, formatter)
            localDate.dayOfMonth
        } catch (e: Exception) {
            0
        }
}

data class Milestone(
    @SerializedName("Customer") val customer: String,
    @SerializedName("Project") val project: String,
    @SerializedName("Event") val event: String,
    @SerializedName("PO_date") val poDate: String
)
