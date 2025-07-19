package com.archeGlobal.one.model

data class AdminDashboardModel(
    val dashboardItems: List<AdminDashboardItem> = getDefaultAdminDashboardItems(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AdminDashboardItem(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val badgeCount: Int = 0,
    val isEnabled: Boolean = true
)

fun getDefaultAdminDashboardItems(): List<AdminDashboardItem> {
    return listOf(
        AdminDashboardItem(
            id = "inventory",
            title = "Inventory",
            description = "Manage stationary stock",
            iconName = "ic_inventory",
            badgeCount = 0
        ),
        AdminDashboardItem(
            id = "order_received",
            title = "Order Received",
            description = "View and process orders",
            iconName = "ic_order_received",
            badgeCount = 1 // Sample notification badge
        ),
        AdminDashboardItem(
            id = "consumption_report",
            title = "Consumption Report",
            description = "Analyze stationary usage",
            iconName = "ic_consumption_report",
            badgeCount = 0
        )
    )
}
