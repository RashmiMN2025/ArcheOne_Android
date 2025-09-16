package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.*
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderHistoryController(
    private val context: Context,
    private val navigator: Navigator,
    private val sourceActivity: String? = null
) : ViewModel() {

    companion object {
        var selectedOrderForDetails: DeskCartOrderHistory? = null
        private var cachedOrderHistory: List<DeskCartOrderHistory>? = null
        private var lastLoadTime: Long = 0
        private const val CACHE_DURATION = 5 * 60 * 1000L // 5 minutes cache
    }

    var model by mutableStateOf(OrderHistoryModel())
        private set

    private val userDataManager = UserDataManager.getInstance(context)

    init {
        loadOrderHistoryIfNeeded()
    }

    private fun loadOrderHistoryIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val isCacheValid = cachedOrderHistory != null && (currentTime - lastLoadTime) < CACHE_DURATION

        if (isCacheValid) {
            // Use cached data
            model = model.copy(
                orders = cachedOrderHistory!!,
                isLoading = false,
                error = null
            )
            Log.d("OrderHistoryController", "Using cached order history: ${cachedOrderHistory!!.size} orders")
        } else {
            // Load fresh data
            loadOrderHistory()
        }
    }

    private fun loadOrderHistory() {
        model = model.copy(isLoading = true, error = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userEmail = userDataManager.getUserData()?.email ?: ""
                Log.d("OrderHistoryController", "Fetching order history for email: $userEmail")

                val request = DeskCartOrderHistoryRequest(email = userEmail)
                val response = RetrofitClient.apiService.getDeskCartUserHistory(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val orderHistoryResponse = response.body()!!
                        // Cache the results
                        cachedOrderHistory = orderHistoryResponse.orders
                        lastLoadTime = System.currentTimeMillis()

                        model = model.copy(
                            orders = orderHistoryResponse.orders,
                            isLoading = false
                        )
                        Log.d("OrderHistoryController", "Order history loaded successfully: ${orderHistoryResponse.orders.size} orders")
                    } else {
                        handleError("Failed to load order history: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun handleError(message: String) {
        Log.e("OrderHistoryController", message)
        model = model.copy(
            isLoading = false,
            error = message
        )
    }

    fun onBackPressed() {
        Log.d("OrderHistoryController", "Back button pressed - source activity: $sourceActivity")
        if (sourceActivity == "DeskCartActivity") {
            // Navigate back to DeskCart activity
            navigator.navigateToDeskCart()
        } else {
            // Default behavior - pop back stack within HomeActivity
            navigator.popBackStack()
        }
    }

    fun refreshOrderHistory() {
        // Force refresh by bypassing cache
        cachedOrderHistory = null
        lastLoadTime = 0
        loadOrderHistory()
    }

    fun onOrderClick(order: DeskCartOrderHistory) {
        Log.d("OrderHistoryController", "Order clicked: ${order.order_Id} - navigating to OrderHistoryDetailActivity")
        selectedOrderForDetails = order
        navigator.navigateToOrderHistoryDetailActivity()
    }

    fun clearError() {
        model = model.copy(error = null)
    }

    fun popBackStack() {
        navigator.popBackStack()
    }
}
