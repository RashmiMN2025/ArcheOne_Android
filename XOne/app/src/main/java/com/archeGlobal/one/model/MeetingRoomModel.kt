package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

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
    val data: List<String>,
    val userRoles: List<String>
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

data class BookingHistoryItem(
    @SerializedName("booking_id")
    val bookingId: String,
    @SerializedName("room_name")
    val roomName: String,
    @SerializedName("host_email")
    val hostEmail: String,
    @SerializedName("meeting_type")
    val meetingType: String,
    @SerializedName("meeting_starttime")
    val meetingStarttime: String,
    @SerializedName("meeting_endtime")
    val meetingEndtime: String,
    @SerializedName("arche_attendees")
    val archeAttendees: String,
    @SerializedName("guest_attendees")
    val guestAttendees: String,
    @SerializedName("meeting_subject")
    val meetingSubject: String,
    @SerializedName("meeting_status")
    val meetingStatus: String,
    @SerializedName("business_justification")
    val businessJustification: String,
    @SerializedName("client_name")
    val clientName: String,
    @SerializedName("project_name")
    val projectName: String,
    @SerializedName("meeting_extension")
    val meetingExtension: String,
    @SerializedName("refreshment_required")
    val refreshmentRequired: String,
    @SerializedName("additional_request")
    val additionalRequest: String,
    @SerializedName("approval_status")
    val approvalStatus: String,
    @SerializedName("room_id")
    val roomId: String,
    @SerializedName("remark")
    val remark: String
)

data class BookingHistoryResponse(
    val status: Int,
    val data: List<BookingHistoryItem>
)

data class MeetingApprovalRequest(
    val designation: String,
    val response: String,
    val remark: String? = null  // Optional remark
)

data class MeetingApprovalResponse(
    val status: Int,
    val message: String
)

data class VerifyCheckInRequest(
    @SerializedName("qr_code")
    val qr_code: String
)

data class VerifyCheckInResponse(
    val status: Int,
    val data: VerifyData?
)

data class VerifyData(
    @SerializedName("booking_id")
    val booking_id: String,
    @SerializedName("is_valid")
    val is_valid: Boolean,
    @SerializedName("verification_timestamp")
    val verification_timestamp: String,
    val message: String,
    @SerializedName("qr_data")
    val qr_data: QrData
)

data class QrData(
    @SerializedName("booking_id")
    val booking_id: String,
    @SerializedName("room_id")
    val room_id: String,
    @SerializedName("qr_token")
    val qr_token: String,
    @SerializedName("meeting_starttime")
    val meeting_starttime: String,
    @SerializedName("meeting_endtime")
    val meeting_endtime: String
)
