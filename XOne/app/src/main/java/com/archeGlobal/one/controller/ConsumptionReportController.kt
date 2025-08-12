package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.ConsumptionReportModel
import com.archeGlobal.one.model.ConsumptionTab
import com.archeGlobal.one.model.toConsumptionStockCategories
import com.archeGlobal.one.model.toUsageCategories
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.launch

class ConsumptionReportController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {
    var model by mutableStateOf(ConsumptionReportModel())
        private set

    init {
        loadConsumptionData()
    }

    private fun loadConsumptionData() {
        viewModelScope.launch {
            model = model.copy(isLoading = true)
            try {
                val response = RetrofitClient.apiService.getStockList()
                if (response.isSuccessful) {
                    val stockListResponse = response.body()
                    if (stockListResponse?.status == 200) {
                        val stockData = stockListResponse.data
                        
                        // Filter by selected location if applicable
                        val filteredData = if (model.selectedLocation.isNotBlank()) {
                            stockData.filter { it.location == model.selectedLocation }
                        } else {
                            stockData
                        }
                        
                        val stockCategories = filteredData.toConsumptionStockCategories()
                        val usageCategories = filteredData.toUsageCategories()
                        
                        model = model.copy(
                            stockCategories = stockCategories,
                            usageCategories = usageCategories,
                            isLoading = false,
                            error = null
                        )
                        Log.d("ConsumptionReportController", "Consumption data loaded successfully")
                    } else {
                        model = model.copy(
                            isLoading = false,
                            error = "Failed to load consumption data"
                        )
                    }
                } else {
                    model = model.copy(
                        isLoading = false,
                        error = "Network error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ConsumptionReportController", "Error loading consumption data", e)
                model = model.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun onBackPressed() {
        Log.d("ConsumptionReportController", "Back pressed - navigating to Admin Dashboard")
        navigator.navigateToAdminDashboard()
    }

    fun onTabSelected(tab: ConsumptionTab) {
        Log.d("ConsumptionReportController", "Tab selected: $tab")
        model = model.copy(selectedTab = tab)
    }

    fun onLocationSelected(location: String) {
        Log.d("ConsumptionReportController", "Location selected: $location")
        model = model.copy(selectedLocation = location)
        // TODO: Load data for the selected location
        loadDataForLocation(location)
    }

    fun onDownloadReport(categoryId: String) {
        Log.d("ConsumptionReportController", "Download report for category: $categoryId")
        Toast.makeText(context, "Downloading report for $categoryId...", Toast.LENGTH_SHORT).show()
        // TODO: Implement actual download functionality
    }

    private fun loadDataForLocation(location: String) {
        // Reload all data with the new location filter
        loadConsumptionData()
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}
