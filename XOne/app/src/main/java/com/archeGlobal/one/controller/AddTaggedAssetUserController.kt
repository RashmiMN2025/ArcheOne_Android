// AddTaggedAssetUserController.kt
package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.SuggestAssetUsersResponse
import com.archeGlobal.one.model.SuggestedAssetUser
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TagUserController(private val context: Context) {
    private val _suggestedUsers = MutableStateFlow<List<SuggestedAssetUser>>(emptyList())
    val suggestedUsers: StateFlow<List<SuggestedAssetUser>> = _suggestedUsers.asStateFlow()

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
                val response = RetrofitClient.apiService.suggestAssetUsers(name)
                if (response.success) {
                    // Filter out null or empty emailId values
                    _suggestedUsers.value = response.data
                        .filter { it.emailId!!.isNotBlank() }
                    Log.d("TagUserController", "Fetched users: ${_suggestedUsers.value}")
                    if (_suggestedUsers.value.isEmpty()) {
                        _errorMessage.value = "No valid users found"
                    }
                } else {
                    _suggestedUsers.value = emptyList()
                    _errorMessage.value = "API error: ${response.status} - ${response.message}"
                }
            } catch (e: Exception) {
                _suggestedUsers.value = emptyList()
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("TagUserController", "Error fetching users", e)
            } finally {
                _isLoadingSuggestions.value = false
            }
        }
    }
}