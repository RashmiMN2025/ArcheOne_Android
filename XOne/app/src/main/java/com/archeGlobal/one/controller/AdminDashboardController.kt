package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.AdminDashboardActivity
import com.archeGlobal.one.model.AdminDashboardItem
import com.archeGlobal.one.model.AdminDashboardModel
import com.archeGlobal.one.navigation.Navigator

class AdminDashboardController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(AdminDashboardModel())
        private set

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        try {
            model = model.copy(isLoading = false)
            Log.d("AdminDashboardController", "Dashboard data loaded successfully")
        } catch (e: Exception) {
            Log.e("AdminDashboardController", "Error loading dashboard data", e)
            model = model.copy(
                isLoading = false,
                error = "Failed to load dashboard data"
            )
        }
    }

    fun onBackPressed() {
        if (context is AdminDashboardActivity) {
            context.finishWithAnimation()
        } else {
            navigator.navigateToHome()
        }
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
        Toast.makeText(context, "Order processing feature coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to order processing screen

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
        Toast.makeText(context, "Consumption report feature coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to consumption report screen
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