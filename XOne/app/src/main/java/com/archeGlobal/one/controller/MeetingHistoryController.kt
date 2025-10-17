package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.BookingHistoryItem
import com.archeGlobal.one.model.BookingHistoryResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MeetingHistoryController(private val context: Context, private val userRole: String) {
    private val _bookings = MutableStateFlow<List<BookingHistoryItem>>(emptyList())
    val bookings: StateFlow<List<BookingHistoryItem>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchBookingHistory()
    }

    internal fun fetchBookingHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userEmail = UserDataManager.getInstance(context).getUserData()?.email ?: ""
                val request = mapOf("userEmail" to userEmail, "userRole" to userRole)
                val response: Response<BookingHistoryResponse> = RetrofitClient.apiService.getBookingHistory(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 200) {
                        _bookings.value = body.data
                        Log.d("MeetingHistoryController", "Fetched bookings: ${body.data.size}")
                    } else {
                        _errorMessage.value = "Failed to fetch bookings: Invalid response status (${body?.status})"
                    }
                } else {
                    _errorMessage.value = "API error: HTTP ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("MeetingHistoryController", "Error fetching bookings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}