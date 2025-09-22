package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.R
import com.archeGlobal.one.model.*
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.screens.OrderReceivedModel
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderReceivedController(
    private val context: Context,
    private val navigator: Navigator,
) : ViewModel() {
    companion object {
        var selectedOrderForDetails: OrderHistoryItem? = null
    }

    var model by mutableStateOf(OrderReceivedModel())
        private set

    var selectedOrderItem by mutableStateOf<OrderHistoryItem?>(null)
        private set

    private val userDataManager = UserDataManager.getInstance(context)

    init {
        loadOrders()
    }

    private fun loadOrders() {
        model = model.copy(isLoading = true, error = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("OrderReceivedController", "Fetching order history from API...")
                val response = RetrofitClient.apiService.getOrderHistory()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val orderHistoryResponse = response.body()!!
                        model =
                            model.copy(
                                orders = orderHistoryResponse.orders,
                                isLoading = false,
                            )
                        Log.d("OrderReceivedController", "Orders loaded successfully: ${orderHistoryResponse.orders.size} orders")
                    } else {
                        handleError("Failed to load orders: ${response.message()}")
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
        Log.e("OrderReceivedController", message)
        model =
            model.copy(
                isLoading = false,
                error = message,
            )
    }

    fun onBackPressed() {
        Log.d("OrderReceivedController", "Back button pressed - finishing activity")
        (context as? ComponentActivity)?.let { act ->
            act.finish()
            act.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    fun refreshOrders() {
        loadOrders()
    }

    fun onOrderClick(order: OrderHistoryItem) {
        Log.d("OrderReceivedController", "Order clicked: ${order.orderId}")
        selectedOrderItem = order
        selectedOrderForDetails = order
        navigator.navigateToOrderDetails(order.orderId)
    }

    fun getPendingOrdersCount(): Int = model.orders.count { it.orderStatus.lowercase() == "pending" }

    fun getTotalOrdersCount(): Int = model.orders.size

    fun clearError() {
        model = model.copy(error = null)
    }
}
