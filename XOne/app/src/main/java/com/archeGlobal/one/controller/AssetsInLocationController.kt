package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.AssetV2Request
import com.archeGlobal.one.model.AssetV2Response
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.screens.AssetInLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AssetsInLocationModel(
    val items: List<AssetInLocation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AssetsInLocationController(private val location: String) : ViewModel() {
    var model by mutableStateOf(AssetsInLocationModel())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = AssetV2Request(location = location)
                val response = RetrofitClient.apiService.getAssetsV2(request)
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val items = response.data.map { employee ->
                            AssetInLocation(
                                name = employee.username,
                                empId = employee.employeeCode,
                                location = employee.location,
                                assetCount = employee.assets.size
                            )
                        }.sortedBy { it.name.lowercase() }
                        model = model.copy(items = items, isLoading = false)
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