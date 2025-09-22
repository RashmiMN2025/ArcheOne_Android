package com.archeGlobal.one.model

import com.archeGlobal.one.R

data class MeetingRoom(
    val name: String,
    val capacity: Int,
    val equipment: String,
    val imageRes: Int = R.drawable.header_home, // Placeholder image
)
