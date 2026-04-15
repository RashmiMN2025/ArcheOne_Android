package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class ChatbotRequest(
    @SerializedName("question") val question: String
)

data class ChatbotResponse(
    @SerializedName("answer") val answer: String
)
