package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.SuggestUsersResponse
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MeetingRoomController(private val context: Context) {
    private val _suggestedUsers = MutableStateFlow<List<String>>(emptyList())
    val suggestedUsers: StateFlow<List<String>> = _suggestedUsers.asStateFlow()

    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions: StateFlow<Boolean> = _isLoadingSuggestions.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchSuggestedUsers(name: String) {
        if (name.isBlank()) {
            _suggestedUsers.value = emptyList()
            _errorMessage.value = null
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            _isLoadingSuggestions.value = true
            _errorMessage.value = null
            try {
                val response: Response<SuggestUsersResponse> = RetrofitClient.apiService.suggestUsers(name)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isNotEmpty()) {
                        // Filter out null or empty mail values
                        _suggestedUsers.value = body
                            .filter { it.mail != null && it.mail.isNotBlank() }
                            .map { it.mail }
                        Log.d("MeetingRoomController", "Fetched users: ${_suggestedUsers.value}")
                        if (_suggestedUsers.value.isEmpty()) {
                            _errorMessage.value = "No valid users found"
                        }
                    } else {
                        _suggestedUsers.value = emptyList()
                        _errorMessage.value = "No users found"
                    }
                } else {
                    _suggestedUsers.value = emptyList()
                    _errorMessage.value = "API error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _suggestedUsers.value = emptyList()
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("MeetingRoomController", "Error fetching users", e)
            } finally {
                _isLoadingSuggestions.value = false
            }
        }
    }
}