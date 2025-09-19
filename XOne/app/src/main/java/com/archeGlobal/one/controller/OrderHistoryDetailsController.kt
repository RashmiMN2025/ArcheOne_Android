package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.R
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.DeskCartUpdateOrderStatusRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderHistoryDetailsController(
    private val context: Context,
    private val navigator: Navigator
) : ViewModel() {
    var remarks by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var selectedAction by mutableStateOf("collected") // "collected" or "cancelled"
        private set
    
    private val userDataManager = UserDataManager.getInstance(context)

    fun updateRemarks(newRemarks: String) {
        remarks = newRemarks
        Log.d("OrderHistoryDetailsController", "Remarks updated: $newRemarks")
    }
    
    fun updateSelectedAction(action: String) {
        selectedAction = action
        Log.d("OrderHistoryDetailsController", "Selected action: $action")
    }

    fun onBackPressed() {
        Log.d("OrderHistoryDetailsController", "Back button pressed - finishing activity")
        (context as? ComponentActivity)?.let { act ->
            act.finish()
            act.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    fun onApproveOrder(orderId: String) {
        Log.d("OrderHistoryDetailsController", "Approve order clicked: $orderId with remarks: $remarks")
        updateOrderStatus(orderId, "approved", remarks.ifEmpty { "Order approved" })
    }

    fun onRejectOrder(orderId: String) {
        Log.d("OrderHistoryDetailsController", "Reject order clicked: $orderId with remarks: $remarks")
        if (remarks.isEmpty()) {
            Toast.makeText(context, "Please enter rejection remarks", Toast.LENGTH_SHORT).show()
            return
        }
        updateOrderStatus(orderId, "rejected", remarks)
    }
    
    private fun updateOrderStatus(orderId: String, status: String, rejectionRemarks: String) {
        val userData = userDataManager.getUserData()
        val adminName = userData?.name ?: "Admin"
        
        isLoading = true
        
        val request = DeskCartUpdateOrderStatusRequest(
            orderId = orderId,
            newStatus = status,
            processedBy = adminName,
            rejectionRemarks = rejectionRemarks
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("OrderHistoryDetailsController", "Updating order status to $status for order $orderId")
                val response = RetrofitClient.apiService.updateDeskCartOrderStatus(request)
                
                withContext(Dispatchers.Main) {
                    isLoading = false
                    
                    if (response.isSuccessful && response.body() != null) {
                        val message = if (status == "approved") "Order approved successfully" else "Order rejected successfully"
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        
                        Log.d("OrderHistoryDetailsController", "Order $orderId $status successfully")
                        
                        // Navigate back to order received screen
                        navigator.navigateToOrderReceived()
                    } else {
                        handleError("Failed to $status order: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    handleError("Network error: ${e.message}")
                }
            }
        }
    }
    
    fun onCloseOrder(orderId: String) {
        Log.d("OrderHistoryDetailsController", "Close order clicked: $orderId with remarks: $remarks")
        if (remarks.isEmpty()) {
            Toast.makeText(context, "Please enter remarks", Toast.LENGTH_SHORT).show()
            return
        }
        
        val status = if (selectedAction == "collected") "closed" else "cancelled"
        updateOrderStatus(orderId, status, remarks)
    }
    
    private fun handleError(message: String) {
        Log.e("OrderHistoryDetailsController", message)
        Toast.makeText(context, "Failed to update order. Please try again.", Toast.LENGTH_SHORT).show()
    }
}