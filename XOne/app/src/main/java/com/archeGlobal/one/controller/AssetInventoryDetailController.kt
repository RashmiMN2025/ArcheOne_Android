// Full updated AssetInventoryDetailController.kt
package com.archeGlobal.one.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.AddInventoryAssetItemRequest
import com.archeGlobal.one.model.InventoryRequest
import com.archeGlobal.one.model.UpdateCommissionRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.screens.AssetFormData
import com.archeGlobal.one.ui.screens.AssetInventoryDetailItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AssetInventoryDetailModel(
    val activeItems: List<AssetInventoryDetailItem> = emptyList(),
    val decommissionedItems: List<AssetInventoryDetailItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AssetInventoryDetailController(private val assetType: String) : ViewModel() {
    var model by mutableStateOf(AssetInventoryDetailModel())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = InventoryRequest(assetType = assetType.lowercase())
                val response = RetrofitClient.apiService.getInventory(request)

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val allItems = response.data.flatMap { group ->
                            group.assets.map { asset ->
                                AssetInventoryDetailItem(
                                    model = asset.makeModel,
                                    serialNo = asset.serialNumber,
                                    location = asset.location,
                                    configuration = asset.configuration,
                                    purchaseDate = asset.purchaseDate,
                                    warrantyStart = asset.warrantyStart,
                                    warrantyEnd = asset.warrantyEnd,
                                    updatedBy = "",
                                    isDecommissioned = !asset.isCommissioned   // Invert: false isCommissioned -> true isDecommissioned
                                )
                            }
                        }

                        // Split: active (isCommissioned true -> isDecommissioned false), decommissioned (isCommissioned false -> isDecommissioned true)
                        val active = allItems.filter { !it.isDecommissioned }
                        val decommissioned = allItems.filter { it.isDecommissioned }

                        model = model.copy(
                            activeItems = active,
                            decommissionedItems = decommissioned,
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
                        error = "Failed to load: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateCommissionStatus(
        serialNumber: String,
        isCommissioned: Boolean,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = UpdateCommissionRequest(
                    serialNumber = serialNumber,
                    isCommissioned = isCommissioned
                )
                val response = RetrofitClient.apiService.updateCommissionStatus(request)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()

                    if (response.success) {
                        loadData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun deleteAsset(serialNumber: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.deleteAsset(serialNumber)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                    if (response.success) {
                        loadData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Updated AssetInventoryDetailController.kt (add this function)
    fun createAsset(
        formData: AssetFormData,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = listOf(
                    AddInventoryAssetItemRequest(
                        assetType = assetType,
                        makeModel = formData.makeModel,
                        serialNumber = formData.serialNo,
                        configuration = formData.configuration,
                        location = formData.location,
                        updatedBy = formData.updatedBy,
                        purchaseDate = formData.purchaseDate,
                        warrantyStart = formData.warrantyStart,
                        warrantyEnd = formData.warrantyEnd,
                        isCommissioned = true
                    )
                )

                val response = RetrofitClient.apiService.createInventoryItem(request)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                    if (response.success) {
                        loadData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to add: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}