package com.archeGlobal.one.controller

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AttendanceDayStatus
import com.archeGlobal.one.model.LeaveBalance
import java.time.DayOfWeek
import java.time.YearMonth

class AttendanceController(private val context: Context) {

    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var attendanceMap by mutableStateOf<Map<Int, AttendanceDayStatus>>(emptyMap())
        private set

    var leaveBalances by mutableStateOf<List<LeaveBalance>>(emptyList())
        private set

    init {
        loadData()
    }

    fun previousMonth() {
        currentMonth = currentMonth.minusMonths(1)
    }

    fun nextMonth() {
        currentMonth = currentMonth.plusMonths(1)
    }

    private fun loadData() {
        val daysInMonth = currentMonth.lengthOfMonth()
        val weekends = (1..daysInMonth)
            .filter { day ->
                val dow = currentMonth.atDay(day).dayOfWeek
                dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
            }
            .associateWith { AttendanceDayStatus.WEEKEND }

        // Placeholder attendance data — replace with API call when available
        val mockAttendance = mapOf(
            1 to AttendanceDayStatus.PRESENT,
            3 to AttendanceDayStatus.PRESENT,
            4 to AttendanceDayStatus.PRESENT,
            5 to AttendanceDayStatus.PRESENT,
            6 to AttendanceDayStatus.LATE,
            7 to AttendanceDayStatus.PRESENT,
            10 to AttendanceDayStatus.PRESENT,
            11 to AttendanceDayStatus.ABSENT,
            12 to AttendanceDayStatus.PRESENT,
            13 to AttendanceDayStatus.PRESENT,
            14 to AttendanceDayStatus.LEAVE,
            17 to AttendanceDayStatus.HOLIDAY,
            18 to AttendanceDayStatus.PRESENT,
            19 to AttendanceDayStatus.PRESENT,
            20 to AttendanceDayStatus.PRESENT,
            21 to AttendanceDayStatus.PRESENT,
        )

        attendanceMap = weekends + mockAttendance

        leaveBalances = listOf(
            LeaveBalance("Apply Optional Holiday", 2.0),
            LeaveBalance("Apply Privilege Leave", 24.0),
            LeaveBalance("Apply Casual Leave", 9.0),
            LeaveBalance("Apply Sick Leave", 12.0),
            LeaveBalance("Apply Paternity Leaves", 5.0),
            LeaveBalance("Apply Work from Home", 2.0),
        )
    }
}
