package com.archeGlobal.one.controller

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

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
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                model = model.copy(isLoading = true)
            }
            try {
                val response = RetrofitClient.apiService.getStockList()
                if (response.isSuccessful) {
                    val stockListResponse = response.body()
                    if (stockListResponse?.status == 200) {
                        val stockData = stockListResponse.data
                        
                        // Process data on background thread
                        val filteredData = if (model.selectedLocation.isNotBlank()) {
                            stockData.filter { it.location == model.selectedLocation }
                        } else {
                            stockData
                        }
                        
                        val stockCategories = filteredData.toConsumptionStockCategories()
                        val usageCategories = filteredData.toUsageCategories()
                        
                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            model = model.copy(
                                stockCategories = stockCategories,
                                usageCategories = usageCategories,
                                isLoading = false,
                                error = null
                            )
                        }
                        Log.d("ConsumptionReportController", "Consumption data loaded successfully")
                    } else {
                        withContext(Dispatchers.Main) {
                            model = model.copy(
                                isLoading = false,
                                error = "Failed to load consumption data"
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        model = model.copy(
                            isLoading = false,
                            error = "Network error: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ConsumptionReportController", "Error loading consumption data", e)
                withContext(Dispatchers.Main) {
                    model = model.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
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
        val isUsage = model.selectedTab == ConsumptionTab.USAGE
        val locationParam = URLEncoder.encode(model.selectedLocation, "UTF-8")
        val url = "${RetrofitClient.BASE_URL}inventory/GetStockReport?location=${locationParam}&category=${categoryId}&isUsage=${isUsage}"

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Consumption Report")
                .setDescription("Downloading report...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "consumption_report_${categoryId}.csv")

            downloadManager.enqueue(request)
            Toast.makeText(context, "Downloading report...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ConsumptionReportController", "Error downloading report", e)
            Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDataForLocation(location: String) {
        // Reload all data with the new location filter
        loadConsumptionData()
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}
