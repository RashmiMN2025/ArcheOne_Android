// Updated MeetSpaceController.kt
package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.LocationsResponse
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MeetSpaceController(private val context: Context) {
    private val _locations = MutableStateFlow<List<String>>(emptyList())
    val locations: StateFlow<List<String>> = _locations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response: Response<LocationsResponse> = RetrofitClient.apiService.getMeetingLocations()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 200) {
                        _locations.value = body.data
                        Log.d("MeetSpaceController", "Fetched locations: ${body.data}")
                    } else {
                        _errorMessage.value = body?.message ?: "Failed to fetch locations"
                    }
                } else {
                    _errorMessage.value = "API error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("MeetSpaceController", "Error fetching locations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Optional: Add a refresh function if you want to allow retrying
    fun refreshLocations() {
        fetchLocations()
    }
}