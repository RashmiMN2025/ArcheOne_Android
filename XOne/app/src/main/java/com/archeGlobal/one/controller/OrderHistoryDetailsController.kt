package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.navigation.Navigator

class OrderHistoryDetailsController(
    private val context: Context,
    private val navigator: Navigator
) {
    fun onBackPressed() {
        Log.d("OrderHistoryDetailsController", "Back pressed - navigating back")
        navigator.navigateToOrderReceived()
    }

    fun onApproveOrder(orderId: String) {
        Log.d("OrderHistoryDetailsController", "Approve order clicked: $orderId")
        // Implement approve order logic here
        // This would typically call an API to approve the order
    }

    fun onRejectOrder(orderId: String) {
        Log.d("OrderHistoryDetailsController", "Reject order clicked: $orderId")
        // Implement reject order logic here
        // This would typically call an API to reject the order
    }
}