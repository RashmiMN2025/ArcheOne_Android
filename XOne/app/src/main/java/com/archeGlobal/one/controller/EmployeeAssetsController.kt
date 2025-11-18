package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.AssetV2Request
import com.archeGlobal.one.model.DeleteTagAssetRequest
import com.archeGlobal.one.model.EmployeeAssetGroup
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EmployeeAssetsModel(
    val employee: EmployeeAssetGroup? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class EmployeeAssetsController(private val employeeCode: String) : ViewModel() {
    var model by mutableStateOf(EmployeeAssetsModel())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = AssetV2Request(employeeCode = employeeCode)
                val response = RetrofitClient.apiService.getAssetsV2(request)
                withContext(Dispatchers.Main) {
                    if (response.success && response.data.isNotEmpty()) {
                        model = model.copy(employee = response.data[0], isLoading = false)
                    } else {
                        model = model.copy(error = response.message ?: "No data found", isLoading = false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    model = model.copy(error = "Failed to load: ${e.message}", isLoading = false)
                }
            }
        }
    }

    fun fetchAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = AssetV2Request(employeeCode = employeeCode)
                val response = RetrofitClient.apiService.getAssetsV2(request)
                withContext(Dispatchers.Main) {
                    if (response.success && response.data.isNotEmpty()) {
                        model = model.copy(
                            employee = response.data.first(),
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

    fun deleteAsset(serialNumber: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.deleteTagAsset(
                    DeleteTagAssetRequest(
                        serialNumber
                    )
                )
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        fetchAssets()
                        onResult(response.message)
                    } else {
                        onResult(response.message)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Failed: ${e.message}")
                }
            }
        }
    }

}