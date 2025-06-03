package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventController(
    private val apiService: ApiService,
    private val context: Context
) {
    companion object {
        private const val TAG = "EventController"
        private const val PREF_NAME = "event_preferences"
        private const val KEY_LAST_SHOWN_DATE = "last_shown_date"
    }

    private val _eventData = MutableStateFlow<EventResponse?>(null)
    val eventData: StateFlow<EventResponse?> = _eventData.asStateFlow()

    private val _isEventPopupVisible = MutableStateFlow(false)
    val isEventPopupVisible: StateFlow<Boolean> = _isEventPopupVisible.asStateFlow()

    // Initialize and check if we should show the event today
    fun initialize() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            fetchDailyEvent()
            checkIfShouldShowEvent()
        }
    }

    private suspend fun fetchDailyEvent() {
        try {
            val response = apiService.getDailyEvent()
            if (response.isSuccessful) {
                val eventDetails = response.body() // This is EventResponse?
                if (eventDetails != null) {
                    _eventData.value = eventDetails
                    Log.d(TAG, "Fetched event: ${eventDetails.title}, Image: ${eventDetails.image}")
                } else {
                    // Successful response but empty body
                    Log.d(TAG, "Daily event response successful but body is null. HTTP Status: ${response.code()}")
                    _eventData.value = null
                }
            } else {
                // Unsuccessful response (e.g., 404, 500)
                Log.e(TAG, "Failed to fetch daily event. HTTP Status: ${response.code()}, Error: ${response.errorBody()?.string()}")
                _eventData.value = null
                Log.e(TAG, "Failed to fetch event: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily event", e)
        }
    }

    private fun checkIfShouldShowEvent() {
        // Only show the event if we have event data
        if (_eventData.value == null) {
            _isEventPopupVisible.value = false
            return
        }

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastShownDate = sharedPref.getString(KEY_LAST_SHOWN_DATE, "")
        val currentDate = getCurrentDate()

        // Show popup if it hasn't been shown today
        if (lastShownDate != currentDate) {
            _isEventPopupVisible.value = true
        } else {
            _isEventPopupVisible.value = false
        }
    }

    fun dismissEventPopup() {
        // Save the current date as the last shown date
        val currentDate = getCurrentDate()
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_LAST_SHOWN_DATE, currentDate)
            apply()
        }
        
        // Hide the popup
        _isEventPopupVisible.value = false
    }

    // Format date as yyyy-MM-dd
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
