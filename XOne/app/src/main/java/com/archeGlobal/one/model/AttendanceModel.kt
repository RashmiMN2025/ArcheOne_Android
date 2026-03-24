package com.archeGlobal.one.model

data class LeaveBalance(
    val type: String,
    val balance: Double,
)

enum class AttendanceDayStatus {
    PRESENT,
    ABSENT,
    LATE,
    LEAVE,
    HOLIDAY,
    WEEKEND,
}
