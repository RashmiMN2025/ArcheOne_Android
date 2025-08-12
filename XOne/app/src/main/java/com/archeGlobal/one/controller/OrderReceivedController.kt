package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.*
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.UserDataManager

class OrderReceivedController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(OrderReceivedModel())
        private set

    private val userDataManager = UserDataManager.getInstance(context)

    init {
        loadOrders()
    }

    private fun loadOrders() {
        model = model.copy(isLoading = true, error = null)

        // For now, use sample data since we don't have the actual API endpoint
        // In production, you would call the actual API:
        // loadOrdersFromAPI()

        // Simulate API delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                val orders = getSampleOrders()
                model = model.copy(
                    orders = orders,
                    isLoading = false
                )
                Log.d("OrderReceivedController", "Orders loaded successfully: ${orders.size} orders")
            } catch (e: Exception) {
                Log.e("OrderReceivedController", "Error loading orders", e)
                model = model.copy(
                    isLoading = false,
                    error = "Failed to load orders"
                )
            }
        }, 1000) // 1 second delay to simulate network call
    }

    private fun loadOrdersFromAPI() {
        val userData = userDataManager.getUserData()
        if (userData?.email == null) {
            model = model.copy(
                isLoading = false,
                error = "User not authenticated"
            )
            return
        }

        val request = OrdersRequest(adminEmail = userData.email)

        // Note: This endpoint doesn't exist yet in ApiService
        // You would need to add it to ApiService.kt:
        // @POST("admin/orders")
        // fun getOrders(@Body request: OrdersRequest): Call<OrdersResponse>

        /*
        RetrofitClient.apiService.getOrders(request)
            .enqueue(object : Callback<OrdersResponse> {
                override fun onResponse(call: Call<OrdersResponse>, response: Response<OrdersResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val ordersResponse = response.body()!!
                        model = model.copy(
                            orders = ordersResponse.orders,
                            isLoading = false
                        )
                        Log.d("OrderReceivedController", "Orders loaded from API: ${ordersResponse.orders.size} orders")
                    } else {
                        handleError("Failed to load orders: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                    handleError("Network error: ${t.message}")
                }
            })
        */
    }

    private fun handleError(message: String) {
        Log.e("OrderReceivedController", message)
        model = model.copy(
            isLoading = false,
            error = message
        )
    }

    fun onBackPressed() {
        Log.d("OrderReceivedController", "Back button pressed - navigating to admin dashboard")
        navigator.navigateToAdminDashboard()
    }

    fun refreshOrders() {
        loadOrders()
    }

    fun onOrderClick(order: Order) {
        Log.d("OrderReceivedController", "Order clicked: ${order.orderId}")
        navigator.navigateToOrderDetails(order.orderId)
    }

    fun markOrderAsProcessed(orderId: String) {
        val updatedOrders = model.orders.map { order ->
            if (order.orderId == orderId) {
                order.copy(
                    status = OrderStatus.PROCESSING,
                    isNew = false
                )
            } else {
                order
            }
        }
        model = model.copy(orders = updatedOrders)
        Log.d("OrderReceivedController", "Order $orderId marked as processed")
    }

    fun getNewOrdersCount(): Int {
        return model.orders.count { it.isNew && it.status == OrderStatus.PENDING }
    }

    fun getPendingOrdersCount(): Int {
        return model.orders.count { it.status == OrderStatus.PENDING }
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}
