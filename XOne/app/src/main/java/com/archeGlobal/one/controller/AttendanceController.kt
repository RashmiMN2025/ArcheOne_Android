package com.archeGlobal.one.controller

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AttendanceDayData
import com.archeGlobal.one.model.AttendanceDayStatus
import com.archeGlobal.one.model.AttendanceRequest
import com.archeGlobal.one.model.LeaveBalance
import com.archeGlobal.one.model.LeaveRequest
import com.archeGlobal.one.model.ManagerDashboardRequest
import com.archeGlobal.one.model.ApprovalRequestItem
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AttendanceController(private val context: Context) {

    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var attendanceMap by mutableStateOf<Map<Int, AttendanceDayStatus>>(emptyMap())
        private set

    // Raw type string per day (leave type or attendance status) used for dot colour
    var attendanceTypeMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set

    var leaveBalances by mutableStateOf<List<LeaveBalance>>(emptyList())
        private set

    var optionalHolidays by mutableStateOf<List<String>>(emptyList())

    var isLoading by mutableStateOf(false)
        private set

    var isAttendanceLoading by mutableStateOf(false)
        private set

    val isPageLoading get() = isLoading || isAttendanceLoading

    var approvalRequests by mutableStateOf<List<ApprovalRequestItem>>(emptyList())
        private set

    var isApprovalsLoading by mutableStateOf(false)
        private set

    var approvalsErrorMessage by mutableStateOf<String?>(null)
        private set

    var selectedDateRecords by mutableStateOf<List<AttendanceDayData>>(emptyList())
        private set

    var isDetailLoading by mutableStateOf(false)
        private set

    init {
        fetchLeaveBalances()
    }

    fun previousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        fetchAttendance()
    }

    fun nextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        fetchAttendance()
    }

    fun fetchAttendance() {
        val userData = UserDataManager.getInstance(context).getUserData()
        val userEmail = userData?.email ?: return

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = currentMonth.atDay(1).format(formatter)
        val endDate = currentMonth.atEndOfMonth().format(formatter)

        // Seed weekends immediately so calendar isn't blank while loading
        val daysInMonth = currentMonth.lengthOfMonth()
        attendanceMap = (1..daysInMonth)
            .filter { day ->
                val dow = currentMonth.atDay(day).dayOfWeek
                dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
            }
            .associateWith { AttendanceDayStatus.WEEKEND }

        isAttendanceLoading = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAttendanceRecords(
                        AttendanceRequest(userEmail, startDate, endDate)
                    )
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    val records = response.body()?.data ?: emptyList()
                    val apiMap = mutableMapOf<Int, AttendanceDayStatus>()
                    val typeMap = mutableMapOf<Int, String>()
                    for (record in records) {
                        val day = record.date.split("-").last().toIntOrNull() ?: continue
                        val dow = currentMonth.atDay(day).dayOfWeek
                        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) continue
                        val status = resolveStatus(record.attendanceStatus, record.requests)
                        apiMap[day] = status
                        typeMap[day] = resolveRawType(record.attendanceStatus, record.requests)
                    }
                    val weekends = attendanceMap
                    attendanceMap = weekends + apiMap
                    attendanceTypeMap = typeMap
                }
            } catch (e: Exception) {
                // Keep weekend seeds on error
            } finally {
                isAttendanceLoading = false
            }
        }
    }

    private fun resolveStatus(
        attendanceStatus: String?,
        requests: List<com.archeGlobal.one.model.AttendanceDayRequest>?,
    ): AttendanceDayStatus {
        // Approved leave request → LEAVE
        val hasApprovedLeave = requests?.any {
            it.status.equals("approved", ignoreCase = true) &&
                it.requestType != "Regularisation"
        } ?: false
        if (hasApprovedLeave) return AttendanceDayStatus.LEAVE

        val status = attendanceStatus?.lowercase() ?: ""
        return when {
            status.contains("holiday") -> AttendanceDayStatus.HOLIDAY
            status.contains("present") -> AttendanceDayStatus.PRESENT
            status.contains("late")    -> AttendanceDayStatus.ABSENT
            status.contains("absent")  -> AttendanceDayStatus.ABSENT
            requests?.any { it.requestType != "Regularisation" } == true -> AttendanceDayStatus.LEAVE
            else -> AttendanceDayStatus.ABSENT
        }
    }

    private fun resolveRawType(
        attendanceStatus: String?,
        requests: List<com.archeGlobal.one.model.AttendanceDayRequest>?,
    ): String {
        // Approved request takes priority
        val approved = requests?.firstOrNull { it.status.equals("approved", ignoreCase = true) }
        if (approved != null) return approved.requestType
        // Pending request next
        val pending = requests?.firstOrNull { it.status.equals("pending", ignoreCase = true) }
        if (pending != null) return pending.requestType
        // Fall back to raw attendance status
        return attendanceStatus ?: ""
    }

    fun fetchLeaveBalances() {
        fetchAttendance()
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

    fun fetchAttendanceForDate(date: LocalDate) {
        val userData = UserDataManager.getInstance(context).getUserData()
        val userEmail = userData?.email ?: return

        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        selectedDateRecords = emptyList()
        isDetailLoading = true

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAttendanceRecords(
                        AttendanceRequest(userEmail, dateStr, dateStr)
                    )
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    selectedDateRecords = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                // keep empty
            } finally {
                isDetailLoading = false
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
