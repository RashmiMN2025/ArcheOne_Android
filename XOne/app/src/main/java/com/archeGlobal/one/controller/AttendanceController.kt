package com.archeGlobal.one.controller

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AttendanceDayStatus
import com.archeGlobal.one.model.LeaveBalance
import com.archeGlobal.one.model.LeaveRequest
import com.archeGlobal.one.model.ManagerDashboardRequest
import com.archeGlobal.one.model.ApprovalRequestItem
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth

class AttendanceController(private val context: Context) {

    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var attendanceMap by mutableStateOf<Map<Int, AttendanceDayStatus>>(emptyMap())
        private set

    var leaveBalances by mutableStateOf<List<LeaveBalance>>(emptyList())
        private set

    var optionalHolidays by mutableStateOf<List<String>>(emptyList())

    var isLoading by mutableStateOf(false)
        private set

    var approvalRequests by mutableStateOf<List<ApprovalRequestItem>>(emptyList())
        private set

    var isApprovalsLoading by mutableStateOf(false)
        private set

    var approvalsErrorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadData()
        fetchLeaveBalances()
    }

    fun previousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        loadData()
    }

    fun nextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        loadData()
    }

    fun loadData() {
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
    }

    fun fetchLeaveBalances() {
        loadData()
        val userData = UserDataManager.getInstance(context).getUserData()
        val userEmail = userData?.email ?: ""

        if (userEmail.isEmpty()) return

        isLoading = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.getLeaves(LeaveRequest(userEmail))
                if (response.isSuccessful && response.body()?.success == true) {
                    val leaveDataList = response.body()?.data?.leaves ?: emptyList()
                    leaveBalances = leaveDataList.map {
                        LeaveBalance(
                            type = it.leaveType,
                            balance = it.effectiveBalance
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error or keep existing balances
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchManagerApprovals() {
        val userData = UserDataManager.getInstance(context).getUserData()
        val approverEmail = userData?.email ?: ""

        if (approverEmail.isEmpty()) {
            approvalsErrorMessage = "Approver email not found"
            return
        }

        isApprovalsLoading = true
        approvalsErrorMessage = null

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.getManagerDashboard(
                    ManagerDashboardRequest(approverEmail)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    approvalRequests = response.body()?.data?.requests ?: emptyList()
                } else {
                    approvalsErrorMessage = response.body()?.message ?: "Failed to fetch approvals"
                }
            } catch (e: Exception) {
                approvalsErrorMessage = "Error: ${e.message}"
            } finally {
                isApprovalsLoading = false
            }
        }
    }
}
