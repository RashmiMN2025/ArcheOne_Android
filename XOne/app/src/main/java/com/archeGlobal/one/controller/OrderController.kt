package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.OrderApprovalActionState
import com.archeGlobal.one.model.OrderDetailsModel
import com.archeGlobal.one.model.OrderHistoryItem
import com.archeGlobal.one.model.OrderStatus
import com.archeGlobal.one.model.getSampleOrderDetails
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OrderController(
    private val context: Context,
    private val navigator: Navigator,
    private val coroutineScope: CoroutineScope
) {
    var model by mutableStateOf(OrderDetailsModel())
        private set

    var orderHistoryItem by mutableStateOf<OrderHistoryItem?>(null)
        private set

    var approvalActionState by mutableStateOf<OrderApprovalActionState>(OrderApprovalActionState.Idle)
        private set

    fun loadOrderDetails(orderId: String) {
        try {
            model = model.copy(isLoading = true, error = null)

            // For now, load sample data
            val orderDetails = getSampleOrderDetails(orderId)

            model = model.copy(
                order = orderDetails,
                isLoading = false,
                error = null
            )

            Log.d("OrderController", "Order details loaded successfully for ID: $orderId")
        } catch (e: Exception) {
            Log.e("OrderController", "Error loading order details", e)
            model = model.copy(
                isLoading = false,
                error = "Failed to load order details"
            )
        }
    }

    fun approveOrder(orderId: String, remarks: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                approvalActionState = OrderApprovalActionState.Loading
                Log.d("OrderController", "Approving order: $orderId with remarks: $remarks")

                // Simulate API call
                delay(2000)

                // Update the order status in model
                model.order?.let { currentOrder ->
                    val updatedOrder = currentOrder.copy(orderStatus = OrderStatus.APPROVED)
                    model = model.copy(order = updatedOrder)
                }

                approvalActionState = OrderApprovalActionState.Success("Order approved successfully!")
                Log.d("OrderController", "Order approved successfully: $orderId")
            } catch (e: Exception) {
                Log.e("OrderController", "Error approving order", e)
                approvalActionState = OrderApprovalActionState.Error("Failed to approve order. Please try again.")
            }
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                approvalActionState = OrderApprovalActionState.Loading
                Log.d("OrderController", "Rejecting order: $orderId with reason: $reason")

                // Simulate API call
                delay(2000)

                // Update the order status in model
                model.order?.let { currentOrder ->
                    val updatedOrder = currentOrder.copy(orderStatus = OrderStatus.REJECTED)
                    model = model.copy(order = updatedOrder)
                }

                approvalActionState = OrderApprovalActionState.Success("Order rejected successfully!")
                Log.d("OrderController", "Order rejected successfully: $orderId")
            } catch (e: Exception) {
                Log.e("OrderController", "Error rejecting order", e)
                approvalActionState = OrderApprovalActionState.Error("Failed to reject order. Please try again.")
            }
        }
    }

    fun onBackPressed() {
        navigator.popBackStack()
    }

    fun navigateBack() {
        navigator.popBackStack()
    }

    fun navigateToOrderDetails(orderId: String) {
        loadOrderDetails(orderId)
        navigator.navigateToOrderDetails(orderId)
    }

    fun clearError() {
        model = model.copy(error = null)
    }

    fun resetApprovalActionState() {
        approvalActionState = OrderApprovalActionState.Idle
    }
}
