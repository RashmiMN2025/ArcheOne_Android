package com.example.xone.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xone.model.Holiday
import com.example.xone.model.CalendarResponse
import com.example.xone.network.ApiService
import com.example.xone.network.CalendarRequest
import com.example.xone.repository.UserRepository
import com.example.xone.utils.NetworkResult
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    
    init {
        fetchHolidays()
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
                        _holidayFileUrl.value = calendarResponse.holidaysFile
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

