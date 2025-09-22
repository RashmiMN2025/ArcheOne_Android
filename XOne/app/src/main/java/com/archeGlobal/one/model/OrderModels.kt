package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for order management system
 */

data class OrderDetailsModel(
    val order: OrderDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val approvalActionState: OrderApprovalActionState = OrderApprovalActionState.Idle,
)

data class OrderDetails(
    val orderId: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val email: String,
    val orderStatus: OrderStatus,
    val orderDate: String,
    val orderTime: String,
    val orderItems: List<OrderItem>,
)

sealed class OrderApprovalActionState {
    object Idle : OrderApprovalActionState()

    object Loading : OrderApprovalActionState()

    data class Success(
        val message: String,
    ) : OrderApprovalActionState()

    data class Error(
        val message: String,
    ) : OrderApprovalActionState()
}

// DeskCart Order History Models
data class DeskCartOrderHistoryRequest(
    val email: String,
)

data class DeskCartOrderHistoryResponse(
    val status: Int,
    val orders: List<DeskCartOrderHistory>,
)

data class DeskCartOrderHistory(
    val order_Id: String,
    val Emp_Name: String,
    val Emp_ID: String,
    val Dept: String,
    val Location: String,
    val items: List<DeskCartOrderItem>,
    val Total_Items_in_Order: Int,
    val Order_Placed_Time: String,
    val Order_Closed_time: String,
    @SerializedName("Order_Processed_By_(Admin_team)")
    val orderProcessedByAdminTeam: String,
    val Order_Status: String,
    val Remarks: String,
)

data class DeskCartOrderItem(
    val materialId: String,
    val name: String,
    val count: Int,
)

// Model for Order History Screen State
data class OrderHistoryModel(
    val orders: List<DeskCartOrderHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// Sample data for demonstration
fun getSampleOrderDetails(orderId: String): OrderDetails =
    OrderDetails(
        orderId = orderId,
        employeeId = "NT9999",
        employeeName = "Nova O'Sullivan",
        department = "Technology",
        email = "webtestuser@arche.global",
        orderStatus = OrderStatus.PENDING,
        orderDate = "11 Aug 2025",
        orderTime = "10:30 AM",
        orderItems =
            listOf(
                OrderItem(itemName = "Pen", quantity = 5, iconName = "ic_pen"),
                OrderItem(itemName = "NotePad", quantity = 3, iconName = "ic_notepad"),
                OrderItem(itemName = "Marker", quantity = 2, iconName = "ic_marker"),
                OrderItem(itemName = "Envelope DL", quantity = 1, iconName = "ic_envelope_dl"),
            ),
    )
