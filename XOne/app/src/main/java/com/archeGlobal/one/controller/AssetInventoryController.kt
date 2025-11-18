package com.archeGlobal.one.controller

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.CreateAssetCategoryRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.screens.AssetInventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AssetInventoryModel(
    val items: List<AssetInventoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AssetInventoryController : ViewModel() {
    var model by mutableStateOf(AssetInventoryModel())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getAssetTypeCounts()
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        model = model.copy(
                            items = response.data.map {
                                AssetInventoryItem(it.assetType, "Units: ${it.assetCount}")
                            },
                            isLoading = false
                        )
                    } else {
                        model = model.copy(
                            error = response.message,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    model = model.copy(
                        error = "Failed to load inventory: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // controller/AssetInventoryController.kt  (add after loadData())
    fun addAssetType(
        assetType: String,
        context: android.content.Context,
        onComplete: () -> Unit
    ) {
        if (assetType.isBlank()) {
            Toast.makeText(context, "Please enter an asset type", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = CreateAssetCategoryRequest(
                    assetType = assetType.trim(),
                    assetCount = 0
                )
                val response = RetrofitClient.apiService.createAssetCategory(request)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()

                    if (response.success && response.data != null) {
                        // Optimistically add to UI
                        val newItem = AssetInventoryItem(
                            name = response.data.assetType,
                            description = "Units: ${response.data.assetCount}"
                        )
                        model = model.copy(
                            items = model.items.toMutableList().apply { add(newItem) }
                        )
                        // Refresh full list to get correct count from server
                        loadData()
                    }
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to add asset type: ${e.message}", Toast.LENGTH_LONG).show()
                    onComplete()
                }
            }
        }
    }
}