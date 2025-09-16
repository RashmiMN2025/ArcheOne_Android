package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.AdminDashboardItem
import com.archeGlobal.one.model.AdminDashboardModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboardController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {
    var model by mutableStateOf(AdminDashboardModel())
        private set

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                model = model.copy(isLoading = true)
            }
            try {
                val response = RetrofitClient.apiService.getStockList()
                if (response.isSuccessful) {
                    val stockListResponse = response.body()
                    val orderPendingCount = stockListResponse?.orderPending ?: 0

                    withContext(Dispatchers.Main) {
                        // Update the "order_received" item's badge count
                        updateBadgeCount("order_received", orderPendingCount)

                        model = model.copy(isLoading = false)
                    }
                    Log.d("AdminDashboardController", "Dashboard data loaded successfully. Order pending count: $orderPendingCount")
                } else {
                    withContext(Dispatchers.Main) {
                        model = model.copy(
                            isLoading = false,
                            error = "Failed to load dashboard data: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboardController", "Error loading dashboard data", e)
                withContext(Dispatchers.Main) {
                    model = model.copy(
                        isLoading = false,
                        error = "Failed to load dashboard data: ${e.message}"
                    )
                }
            }
        }
    }

    fun onBackPressed() {
        Log.d("AdminDashboardController", "Back pressed - navigating to DeskCart")
        navigator.navigateToDeskCart()
    }

    fun onDashboardItemClick(item: AdminDashboardItem) {
        Log.d("AdminDashboardController", "Dashboard item clicked: ${item.title}")

        when (item.id) {
            "inventory" -> {
                handleInventoryClick()
            }
            "order_received" -> {
                handleOrderReceivedClick()
            }
            "consumption_report" -> {
                handleConsumptionReportClick()
            }
            else -> {
                Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleInventoryClick() {
        Log.d("AdminDashboardController", "Inventory clicked")
        navigator.navigateToInventory()
    }

    private fun handleOrderReceivedClick() {
        Log.d("AdminDashboardController", "Order Received clicked")
        navigator.navigateToOrderReceived()

        // Clear the badge count when clicked
        val updatedItems = model.dashboardItems.map {
            if (it.id == "order_received") {
                it.copy(badgeCount = 0)
            } else {
                it
            }
        }
        model = model.copy(dashboardItems = updatedItems)
    }

    private fun handleConsumptionReportClick() {
        Log.d("AdminDashboardController", "Consumption Report clicked")
        navigator.navigateToConsumptionReport()
    }

    fun updateBadgeCount(itemId: String, count: Int) {
        val updatedItems = model.dashboardItems.map {
            if (it.id == itemId) {
                it.copy(badgeCount = count)
            } else {
                it
            }
        }
        model = model.copy(dashboardItems = updatedItems)
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}
