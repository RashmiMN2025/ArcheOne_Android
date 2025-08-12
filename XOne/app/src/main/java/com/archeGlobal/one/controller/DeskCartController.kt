package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.DeskCartModel
import com.archeGlobal.one.model.EmployeeDetails
import com.archeGlobal.one.model.StationaryItem
import com.archeGlobal.one.navigation.Navigator

class DeskCartController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(DeskCartModel(isLoading = true))
        private set

    init {
        loadEmployeeData()
    }

    private fun loadEmployeeData() {
        try {
            // Get user data from UserDataManager or OtpVerificationController
            val userData = OtpVerificationController.getUserData()

            val employeeDetails = EmployeeDetails(
                emailId = userData?.email ?: "biswajit.d@arche.global",
                employeeId = userData?.employeeId ?: "NT1426",
                department = userData?.department ?: "Project Department"
            )

            model = model.copy(
                employeeDetails = employeeDetails,
                isLoading = false
            )

            Log.d("DeskCartController", "Employee data loaded: ${employeeDetails.employeeId}")
        } catch (e: Exception) {
            Log.e("DeskCartController", "Error loading employee data", e)
            model = model.copy(
                isLoading = false,
                error = "Failed to load employee data"
            )
        }
    }

    fun onBackPressed() {
        Log.d("DeskCartController", "Back pressed - navigating to Home")
        navigator.navigateToHome()
    }

    fun onAdminDashboardClick() {
        Log.d("DeskCartController", "Admin Dashboard clicked")
        navigator.navigateToAdminDashboard()
    }

    fun onIncreaseQuantity(item: StationaryItem) {
        if (item.currentQuantity < item.maxQuantity) {
            val updatedItems = model.stationaryItems.map {
                if (it.id == item.id) {
                    it.copy(currentQuantity = it.currentQuantity + 1)
                } else {
                    it
                }
            }
            model = model.copy(stationaryItems = updatedItems)
            Log.d("DeskCartController", "Increased ${item.name} quantity to ${item.currentQuantity + 1}")
        }
    }

    fun onDecreaseQuantity(item: StationaryItem) {
        if (item.currentQuantity > 0) {
            val updatedItems = model.stationaryItems.map {
                if (it.id == item.id) {
                    it.copy(currentQuantity = it.currentQuantity - 1)
                } else {
                    it
                }
            }
            model = model.copy(stationaryItems = updatedItems)
            Log.d("DeskCartController", "Decreased ${item.name} quantity to ${item.currentQuantity - 1}")
        }
    }

    fun onPlaceOrder() {
        val selectedItems = model.stationaryItems.filter { it.currentQuantity > 0 }

        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Please select at least one item to place order", Toast.LENGTH_SHORT).show()
            return
        }

        model = model.copy(isLoading = true)

        // Simulate order placement
        try {
            Log.d("DeskCartController", "Placing order for ${selectedItems.size} items")
            selectedItems.forEach { item ->
                Log.d("DeskCartController", "Ordering ${item.currentQuantity} x ${item.name}")
            }

            // Reset quantities after successful order
            val resetItems = model.stationaryItems.map {
                it.copy(currentQuantity = 0)
            }

            model = model.copy(
                stationaryItems = resetItems,
                isLoading = false,
                orderPlaced = true
            )

            Toast.makeText(
                context,
                "Order placed successfully for ${selectedItems.size} items!",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e("DeskCartController", "Error placing order", e)
            model = model.copy(
                isLoading = false,
                error = "Failed to place order. Please try again."
            )
            Toast.makeText(context, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    fun getTotalItemsSelected(): Int {
        return model.stationaryItems.sumOf { it.currentQuantity }
    }

    fun clearError() {
        model = model.copy(error = null)
    }
}
