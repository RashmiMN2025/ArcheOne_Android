package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.*
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderHistoryController(
    private val context: Context,
    private val navigator: Navigator
) {

    companion object {
        var selectedOrderForDetails: DeskCartOrderHistory? = null
    }

    var model by mutableStateOf(OrderHistoryModel())
        private set

    private val userDataManager = UserDataManager.getInstance(context)

    init {
        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        model = model.copy(isLoading = true, error = null)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userEmail = userDataManager.getUserData()?.email ?: ""
                Log.d("OrderHistoryController", "Fetching order history for email: $userEmail")
                
                val request = DeskCartOrderHistoryRequest(email = userEmail)
                val response = RetrofitClient.apiService.getDeskCartUserHistory(request)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val orderHistoryResponse = response.body()!!
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
        Log.d("OrderHistoryController", "Back button pressed - navigating back")
        navigator.popBackStack()
    }

    fun refreshOrderHistory() {
        loadOrderHistory()
    }

    fun onOrderClick(order: DeskCartOrderHistory) {
        Log.d("OrderHistoryController", "Order clicked: ${order.order_Id}")
        selectedOrderForDetails = order
        // Convert DeskCartOrderHistory to OrderHistoryItem format for compatibility with OrderDetailsScreen
        val convertedOrder = OrderHistoryItem(
            orderId = order.order_Id,
            empName = order.Emp_Name,
            empId = order.Emp_ID,
            dept = order.Dept,
            location = order.Location,
            items = order.items.map { 
                OrderHistoryItemDetail(
                    materialId = "", 
                    name = it.name, 
                    count = it.count
                ) 
            },
            totalItemsInOrder = order.Total_Items_in_Order,
            orderPlacedTime = order.Order_Placed_Time,
            orderClosedTime = order.Order_Closed_time,
            orderProcessedBy = order.orderProcessedByAdminTeam,
            orderStatus = order.Order_Status,
            remarks = order.Remarks,
            emailId = userDataManager.getUserData()?.email ?: ""
        )
        // Store the converted order in the existing controller's companion object
        OrderReceivedController.selectedOrderForDetails = convertedOrder
        navigator.navigateToOrderDetails(order.order_Id)
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}