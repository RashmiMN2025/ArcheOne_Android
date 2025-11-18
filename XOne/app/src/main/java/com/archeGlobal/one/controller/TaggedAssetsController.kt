package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.screens.TaggedAssetItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TaggedAssetsModel(
    val items: List<TaggedAssetItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TaggedAssetsController : ViewModel() {
    var model by mutableStateOf(TaggedAssetsModel())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getLocationAssetCounts()
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val sortedData = response.data.sortedBy { it.location.lowercase() }
                        model = model.copy(
                            items = sortedData.map { TaggedAssetItem(it.location, it.assetCount) },
                            isLoading = false
                        )
                    } else {
                        model = model.copy(error = response.message, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    model = model.copy(error = "Failed to load: ${e.message}", isLoading = false)
                }
            }
        }
    }
}