package com.archeGlobal.one.model

import com.archeGlobal.one.R

data class MeetingRoom(
    val room_type: String,
    val name: String,
    val capacity: Int,
    val equipment: String,
    val imageUrl: String,  // Changed to URL string for API response
    val room_id: String? = null,
    val location: String? = null,
    val location_name: String? = null
)

data class LocationsResponse(
    val status: Int,
    val message: String,
    val data: List<String>
)

data class AvailableRoomsRequest(
    val location: String,
    val startDate: String,
    val endDate: String,
    val noOfAttendees: Int
)

data class AvailableRoomsResponse(
    val status: Int,
    val message: String,
    val data: List<RoomData>
)

data class RoomData(
    val room_id: String,
    val room_name: String,
    val location: String,
    val capacity: Int,
    val room_desc: String,
    val room_type: String,
    val location_name: String,
    val room_image_url: String
)

data class UserInfo(
    val mail: String,
    val displayName: String
)

// Directly use List<UserData> for the API response
typealias SuggestUsersResponse = List<UserInfo>