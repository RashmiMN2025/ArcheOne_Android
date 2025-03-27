package com.archeGlobal.one.controller

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.Holiday
import com.archeGlobal.one.model.Milestone
import com.archeGlobal.one.model.CalendarResponse
import com.archeGlobal.one.network.ApiService
import com.archeGlobal.one.network.CalendarRequest
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.utils.NetworkResult
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HolidayCalendarController(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {
    
    // LiveData for holidays
    private val _holidays = MutableLiveData<NetworkResult<CalendarResponse>>()
    val holidays: LiveData<NetworkResult<CalendarResponse>> = _holidays
    
    // LiveData for holiday PDF file URL
    private val _holidayFileUrl = MutableLiveData<String>()
    val holidayFileUrl: LiveData<String> = _holidayFileUrl

    // LiveData for milestones
    private val _milestones = MutableLiveData<List<Milestone>>(emptyList())
    val milestones: LiveData<List<Milestone>> = _milestones
    
    // Set to track hidden holidays by their date string
    private val _hiddenHolidays = mutableSetOf<String>()
    
    // LiveData to notify when hidden holidays change
    private val _hiddenHolidaysUpdated = MutableLiveData<Boolean>(false)
    val hiddenHolidaysUpdated: LiveData<Boolean> = _hiddenHolidaysUpdated
    
    init {
        fetchHolidays()
    }
    
    // Method to hide a holiday by its date
    fun hideHoliday(holiday: Holiday) {
        _hiddenHolidays.add(holiday.date)
        _hiddenHolidaysUpdated.value = !(_hiddenHolidaysUpdated.value ?: false)
    }
    
    // Method to check if a holiday is hidden
    fun isHolidayHidden(holiday: Holiday): Boolean {
        return _hiddenHolidays.contains(holiday.date)
    }
    
    // Method to get filtered holidays that are not hidden
    fun getVisibleHolidays(holidays: List<Holiday>): List<Holiday> {
        return holidays.filter { !_hiddenHolidays.contains(it.date) }
    }
    
    // Method to get milestones for a specific date
    fun getMilestonesForDate(date: String): List<Milestone> {
        return _milestones.value?.filter {
            try {
                val milestoneDate = SimpleDateFormat("MM-dd-yyyy", Locale.US).parse(it.poDate)
                val requestDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(date)
                
                val milestoneDateString = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(milestoneDate)
                val requestDateString = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(requestDate)
                
                milestoneDateString == requestDateString
            } catch (e: Exception) {
                false
            }
        } ?: emptyList()
    }
    
    fun fetchHolidays() {
        _holidays.value = NetworkResult.Loading()
        
        // Get user's state (defaulting to Karnataka if not available)
        val userState = userRepository.getUserState() ?: "Karnataka"
        
        // Use viewModelScope to launch coroutine
        viewModelScope.launch {
            try {
                // Create the request with the state parameter
                val request = CalendarRequest(state = userState)
                
                // Use the POST method with state parameter
                val response = apiService.getCalendar(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val calendarResponse = response.body()!!
                    if (calendarResponse.status == 200) {
                        _holidays.value = NetworkResult.Success(calendarResponse)
                        
                        // Set and log the holidays file URL
                        val holidayFileUrl = calendarResponse.holidaysFile
                        
                        if (!holidayFileUrl.isNullOrBlank()) {
                            _holidayFileUrl.value = holidayFileUrl
                            Log.d("HolidayCalendarController", "Holiday PDF URL: $holidayFileUrl")
                        } else {
                            // Set a default PDF URL if none is provided
                            val defaultUrl = "https://archaeglobal.com/holidays_2025.pdf"
                            _holidayFileUrl.value = defaultUrl
                            Log.w("HolidayCalendarController", "Using default holiday PDF URL: $defaultUrl")
                        }
                        
                        _milestones.value = calendarResponse.milestones
                    } else {
                        _holidays.value = NetworkResult.Error("Server returned error status: ${calendarResponse.status}")
                    }
                } else {
                    _holidays.value = NetworkResult.Error("Failed to fetch holidays: ${response.message()}")
                }
            } catch (e: Exception) {
                _holidays.value = NetworkResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    private fun handleCalendarResponse(response: Response<CalendarResponse>): NetworkResult<List<Holiday>> {
        return if (response.isSuccessful) {
            val holidays = response.body()?.holidays ?: emptyList()
            NetworkResult.Success(holidays)
        } else {
            NetworkResult.Error("Error ${response.code()}: ${response.message()}")
        }
    }
    
    // For testing or offline mode
    fun getDefaultHolidays(): List<Holiday> {
        return listOf(
            Holiday(
                name = "New Year Day",
                date = "01-01-2025",
                holidayType = "Yes"
            ),
            Holiday(
                name = "Republic Day",
                date = "26-01-2025",
                holidayType = "Yes"
            ),
            Holiday(
                name = "Independence Day",
                date = "15-08-2025",
                holidayType = "Yes"
            ),
            Holiday(
                name = "Gandhi Jayanthi",
                date = "02-10-2025",
                holidayType = "Yes"
            ),
            Holiday(
                name = "Christmas",
                date = "25-12-2025",
                holidayType = "Yes"
            )
        )
    }
}

