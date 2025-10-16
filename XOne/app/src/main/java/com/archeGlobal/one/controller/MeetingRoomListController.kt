package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.AvailableRoomsRequest
import com.archeGlobal.one.model.AvailableRoomsResponse
import com.archeGlobal.one.model.MeetingRoom
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MeetingRoomListController(
    private val context: Context,
    private val location: String,
    private val startDate: String,
    private val endDate: String,
    private val noOfAttendees: Int
) {
    private val _rooms = MutableStateFlow<List<MeetingRoom>>(emptyList())
    val rooms: StateFlow<List<MeetingRoom>> = _rooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchRooms()
    }

    private fun fetchRooms() {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val request = AvailableRoomsRequest(location, startDate, endDate, noOfAttendees)
                val response: Response<AvailableRoomsResponse> = RetrofitClient.apiService.getAvailableRooms(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 200) {
                        _rooms.value = body.data.map { roomData ->
                            MeetingRoom(
                                room_type = roomData.room_type,
                                name = roomData.room_name,
                                capacity = roomData.capacity,
                                equipment = roomData.room_desc,
                                imageUrl = roomData.room_image_url,  // Use URL for background image
                                room_id = roomData.room_id,
                                location = roomData.location,
                                location_name = roomData.location_name
                            )
                        }
                        Log.d("MeetingRoomListController", "Fetched rooms: ${_rooms.value}")
                    } else {
                        _errorMessage.value = body?.message ?: "Failed to fetch available rooms"
                    }
                } else {
                    _errorMessage.value = "API error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("MeetingRoomListController", "Error fetching rooms", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshRooms() {
        fetchRooms()
    }
}
