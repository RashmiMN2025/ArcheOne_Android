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
import com.archeGlobal.one.network.CalendarRequest
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

    var selectedDate by mutableStateOf<LocalDate?>(LocalDate.now())

    fun resetToCurrentMonth() {
        currentMonth = YearMonth.now()
        selectedDate = LocalDate.now()
    }

    var attendanceMap by mutableStateOf<Map<Int, AttendanceDayStatus>>(emptyMap())
        private set

    // Raw type string per day (leave type or attendance status) used for dot colour
    var attendanceTypeMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set

    // Resolved request status per day ("approved" / "pending" / "rejected" / "")
    var attendanceRequestStatusMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set

    var leaveBalances by mutableStateOf<List<LeaveBalance>>(emptyList())
        private set

    var optionalHolidays by mutableStateOf<List<String>>(emptyList())
    var isOptionalHolidaysLoading by mutableStateOf(false)
        private set

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
                    val requestStatusMap = mutableMapOf<Int, String>()
                    for (record in records) {
                        val day = record.date.split("-").last().toIntOrNull() ?: continue
                        val dow = currentMonth.atDay(day).dayOfWeek
                        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) continue
                        val status = resolveStatus(record.attendanceStatus, record.requests)
                        apiMap[day] = status
                        typeMap[day] = resolveRawType(record.attendanceStatus, record.requests)
                        requestStatusMap[day] = resolveRequestStatus(record.requests)
                    }
                    val weekends = attendanceMap
                    attendanceMap = weekends + apiMap
                    attendanceTypeMap = typeMap
                    attendanceRequestStatusMap = requestStatusMap
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
        // PRIORITIZE FIRST REQUEST: If there's at least one request, use its status
        val firstRequest = requests?.firstOrNull()
        if (firstRequest != null) {
            val isApproved = firstRequest.status?.equals("approved", ignoreCase = true) ?: false
            if (isApproved && firstRequest.requestType != "Regularisation") {
                return AttendanceDayStatus.LEAVE
            }
        }

        val status = attendanceStatus?.lowercase() ?: ""
        return when {
            status.contains("holiday") -> AttendanceDayStatus.HOLIDAY
            status.contains("present") -> AttendanceDayStatus.PRESENT
            status.contains("late")    -> AttendanceDayStatus.ABSENT
            status.contains("absent")  -> AttendanceDayStatus.ABSENT
            firstRequest != null && firstRequest.requestType != "Regularisation" -> AttendanceDayStatus.LEAVE
            else -> AttendanceDayStatus.ABSENT
        }
    }

    private fun resolveRequestStatus(
        requests: List<com.archeGlobal.one.model.AttendanceDayRequest>?,
    ): String {
        // PRIORITIZE FIRST REQUEST: Return the status of the first request if it exists
        val firstRequest = requests?.firstOrNull() ?: return ""
        return firstRequest.status?.lowercase() ?: ""
    }

    private fun resolveRawType(
        attendanceStatus: String?,
        requests: List<com.archeGlobal.one.model.AttendanceDayRequest>?,
    ): String {
        // PRIORITIZE FIRST REQUEST: Return the type of the first request if it exists
        val firstRequest = requests?.firstOrNull()
        if (firstRequest != null) return firstRequest.requestType ?: "None"
        
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

    fun fetchOptionalHolidays() {
        if (optionalHolidays.isNotEmpty()) {
            android.util.Log.d("OptionalHoliday", "Already loaded ${optionalHolidays.size} holidays, skipping fetch")
            return
        }
        val userState = com.archeGlobal.one.repository.UserRepository(context).getUserState() ?: "Karnataka"
        android.util.Log.d("OptionalHoliday", "Fetching optional holidays for state: $userState")
        isOptionalHolidaysLoading = true
        val encryptedAPIHelper = com.archeGlobal.one.utils.EncryptedAPIHelper(context)
        encryptedAPIHelper.makeEncryptedCall(
            endpoint = "calendar",
            method = "POST",
            request = CalendarRequest(state = userState),
            responseClass = com.archeGlobal.one.model.CalendarResponse::class.java,
            withAuthHeader = true,
        ) { response, error ->
            if (error != null) {
                android.util.Log.e("OptionalHoliday", "Encrypted call error: ${error.errorMessage}")
                isOptionalHolidaysLoading = false
                return@makeEncryptedCall
            }
            if (response == null) {
                android.util.Log.e("OptionalHoliday", "Null response received")
                isOptionalHolidaysLoading = false
                return@makeEncryptedCall
            }
            android.util.Log.d("OptionalHoliday", "Response status: ${response.status}, total holidays: ${response.holidays.size}")
            val allHolidays = response.holidays
            android.util.Log.d("OptionalHoliday", "All holiday types: ${allHolidays.map { it.holidayType }.distinct()}")
            val rhHolidays = allHolidays.filter { it.holidayType == "RH" }
            android.util.Log.d("OptionalHoliday", "RH holidays count: ${rhHolidays.size}")
            rhHolidays.forEach { android.util.Log.d("OptionalHoliday", "  RH: ${it.name} (${it.date})") }
            optionalHolidays = rhHolidays.map { "${it.name} (${it.date})" }
            isOptionalHolidaysLoading = false
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

    /**
     * Performs validation for regularization logic.
     * Returns a pair of (Boolean, String?) indicating (isValid, errorMessage).
     */
    suspend fun validateRegularization(date: LocalDate): Pair<Boolean, String?> {
        val today = LocalDate.now()
        
        // 1. Past dates only
        if (!date.isBefore(today)) {
            return Pair(false, "Regularization can only be applied for past dates.")
        }

        val userData = UserDataManager.getInstance(context).getUserData()
        val userEmail = userData?.email ?: return Pair(false, "User email not found.")
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // 2. Fetch record for the date
        val response = withContext(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.getAttendanceRecords(
                    AttendanceRequest(userEmail, dateStr, dateStr)
                )
            } catch (e: Exception) {
                null
            }
        }

        if (response == null || !response.isSuccessful || response.body()?.success != true) {
            return Pair(false, "Failed to fetch attendance record for this date.")
        }

        val records = response.body()?.data ?: emptyList()
        if (records.isEmpty()) {
            return Pair(false, "No attendance record found for this date.")
        }

        val record = records.first()

        // 3. Must have Punch In
        if (record.punchIn.isNullOrEmpty()) {
            return Pair(false, "Can't regularise. No punch in record found.")
        }

        // 4. Check monthly limit (max 3)
        // To check monthly limit accurately, we need to fetch all records for that month
        val monthStart = date.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val monthEnd = date.withDayOfMonth(date.lengthOfMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        
        val monthResponse = withContext(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.getAttendanceRecords(
                    AttendanceRequest(userEmail, monthStart, monthEnd)
                )
            } catch (e: Exception) {
                null
            }
        }

        if (monthResponse != null && monthResponse.isSuccessful && monthResponse.body()?.success == true) {
            val monthRecords = monthResponse.body()?.data ?: emptyList()
            var count = 0
            for (rec in monthRecords) {
                val hasRegularization = rec.requests?.any {
                    val type = it.requestType ?: "None".lowercase()
                    val status = it.status ?: "None".lowercase()
                    (type.contains("regularisation") || type.contains("regularize") || type.contains("regularised")) &&
                    (status == "approved" || status == "pending")
                } ?: false
                if (hasRegularization) count++
            }
            
            if (count >= 3) {
                return Pair(false, "You have already reached the maximum limit of 3 regularizations for this month.")
            }
        }

        // 5. No existing request on this day (especially regularisation)
        val hasExistingRequest = record.requests?.any {
            it.status ?: "None".lowercase() == "pending" || it.status ?: "None".lowercase() == "approved"
        } ?: false
        
        if (hasExistingRequest) {
            return Pair(false, "A request already exists for this date.")
        }

        return Pair(true, null)
    }
}
