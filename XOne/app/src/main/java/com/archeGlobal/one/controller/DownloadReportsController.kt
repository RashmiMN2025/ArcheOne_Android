package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.DownloadAssetInventoryRequest
import com.archeGlobal.one.model.DownloadAssetInventoryResponse
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DownloadReportsModel(
    val locations: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DownloadReportsController : ViewModel() {
    var model by mutableStateOf(DownloadReportsModel())
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
                        val sortedLocations = response.data.map { it.location }.sortedBy { it.lowercase() }
                        model = model.copy(
                            locations = sortedLocations,
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

    fun downloadReport(
        reportType: String,
        location: String?,
        userEmail: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filters = if (location != null && location.isNotBlank()) {
                    mapOf("location" to location)
                } else null

                val request = DownloadAssetInventoryRequest(
                    recipientEmail = userEmail ?: "",
                    filters = filters
                )

                val response: DownloadAssetInventoryResponse = if (reportType == "tagged") {
                    RetrofitClient.apiService.downloadAssets(request)
                } else {
                    RetrofitClient.apiService.downloadInventory(request)
                }

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        onSuccess(response.message ?: "Report sent successfully")
                    } else {
                        onError(response.message ?: "Failed to process request")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed: ${e.message}")
                }
            }
        }
    }

}