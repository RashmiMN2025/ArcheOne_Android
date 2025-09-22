package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class OrderReceivedModel(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class Order(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("status")
    val status: OrderStatus,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("date_time") // Keep for backward compatibility
    val dateTime: String = "$date $time",
    @SerializedName("is_new")
    val isNew: Boolean = true,
    @SerializedName("items")
    val items: List<OrderItem> = emptyList(),
    @SerializedName("total_items")
    val totalItems: Int = 0,
)

data class OrderItem(
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("icon_name")
    val iconName: String,
)

enum class OrderStatus(
    val displayName: String,
    val colorHex: String,
) {
    PENDING("Pending", "#D32F2F"),
    PROCESSING("Processing", "#FF9800"),
    APPROVED("Approved", "#4CAF50"),
    REJECTED("Rejected", "#D32F2F"),
    COMPLETED("Completed", "#4CAF50"),
    CANCELLED("Cancelled", "#757575"),
}

// API Request/Response models for Order History
data class OrderHistoryResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("orders")
    val orders: List<OrderHistoryItem>,
    @SerializedName("message")
    val message: String? = null,
)

data class OrderHistoryItem(
    @SerializedName("order_Id")
    val orderId: String,
    @SerializedName("Emp_Name")
    val empName: String,
    @SerializedName("Emp_ID")
    val empId: String,
    @SerializedName("Dept")
    val dept: String,
    @SerializedName("Location")
    val location: String,
    @SerializedName("items")
    val items: List<OrderHistoryItemDetail>,
    @SerializedName("Total_Items_in_Order")
    val totalItemsInOrder: Int,
    @SerializedName("Order_Placed_Time")
    val orderPlacedTime: String,
    @SerializedName("Order_Closed_time")
    val orderClosedTime: String,
    @SerializedName("Order_Processed_By_(Admin_team)")
    val orderProcessedBy: String,
    @SerializedName("Order_Status")
    val orderStatus: String,
    @SerializedName("Remarks")
    val remarks: String,
    @SerializedName("Emailid")
    val emailId: String,
)

data class OrderHistoryItemDetail(
    @SerializedName("materialId")
    val materialId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("count")
    val count: Int,
)

// API Request/Response models
data class OrdersRequest(
    @SerializedName("admin_email")
    val adminEmail: String,
)

data class OrdersResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("orders")
    val orders: List<Order>,
    @SerializedName("message")
    val message: String? = null,
)

// Sample data for development
fun getSampleOrders(): List<Order> =
    listOf(
        Order(
            orderId = "ORD001",
            status = OrderStatus.PENDING,
            userName = "John Doe",
            employeeId = "EMP123",
            date = "8 Aug 2025",
            time = "12:03 PM",
            isNew = true,
            items =
                listOf(
                    OrderItem("Pen", 5, "ic_pen"),
                    OrderItem("Notepad", 2, "ic_notepad"),
                ),
            totalItems = 7,
        ),
        Order(
            orderId = "ORD002",
            status = OrderStatus.PENDING,
            userName = "Jane Smith",
            employeeId = "EMP456",
            date = "7 Aug 2025",
            time = "12:03 PM",
            isNew = false,
            items =
                listOf(
                    OrderItem("Stapler", 1, "ic_stapler"),
                    OrderItem("Pencil", 10, "ic_pencil"),
                ),
            totalItems = 11,
        ),
        Order(
            orderId = "ORD003",
            status = OrderStatus.PENDING,
            userName = "Mike Johnson",
            employeeId = "EMP789",
            date = "6 Aug 2025",
            time = "11:45 AM",
            isNew = true,
            items =
                listOf(
                    OrderItem("Marker", 3, "ic_marker"),
                    OrderItem("Glue", 2, "ic_glue"),
                    OrderItem("Scissor", 1, "ic_scissor"),
                ),
            totalItems = 6,
        ),
        Order(
            orderId = "ORD004",
            status = OrderStatus.COMPLETED,
            userName = "Sarah Wilson",
            employeeId = "EMP321",
            date = "5 Aug 2025",
            time = "10:20 AM",
            isNew = false,
            items =
                listOf(
                    OrderItem("Envelope DL", 20, "ic_envelope_dl"),
                    OrderItem("Tape", 2, "ic_tape"),
                ),
            totalItems = 22,
        ),
        Order(
            orderId = "ORD005",
            status = OrderStatus.PENDING,
            userName = "Alex Brown",
            employeeId = "EMP654",
            date = "4 Aug 2025",
            time = "09:30 AM",
            isNew = true,
            items =
                listOf(
                    OrderItem("Punching Machine", 1, "ic_punching_machine"),
                    OrderItem("Envelope A4", 15, "ic_envelope_a4"),
                ),
            totalItems = 16,
        ),
    )
