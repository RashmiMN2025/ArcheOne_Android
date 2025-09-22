package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

// This class will directly model the JSON object received in the "eventPopup" field.
data class EventResponse(
    @SerializedName(value = "name", alternate = ["title", "eventTitle", "heading", "eventName"])
    val title: String? = null, // Using nullable types and default null for robustness
    @SerializedName(value = "eventDate", alternate = ["date", "dateTime", "displayDate", "showDate"])
    val date: String? = null,
    @SerializedName(value = "fromDate", alternate = ["startDate", "beginDate"])
    val fromDate: String? = null,
    @SerializedName(value = "toDate", alternate = ["endDate", "finishDate"])
    val toDate: String? = null,
    @SerializedName(value = "description", alternate = ["desc", "content", "eventDescription", "details", "text"])
    val description: String? = null,
    @SerializedName(value = "image", alternate = ["imageUrl", "eventImage", "imgSrc", "img", "banner", "thumbnail"])
    val image: String? = null,
)
