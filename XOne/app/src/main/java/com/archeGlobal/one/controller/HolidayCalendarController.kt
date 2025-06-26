package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.ImageViewerActivity
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.model.CalendarResponse
import com.archeGlobal.one.model.GlobalEvent
import com.archeGlobal.one.model.Holiday
import com.archeGlobal.one.model.Milestone
import com.archeGlobal.one.network.ApiService
import com.archeGlobal.one.network.CalendarRequest
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.utils.NetworkResult
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    // LiveData for global events
    private val _globalEvents = MutableLiveData<List<GlobalEvent>>(emptyList())
    val globalEvents: LiveData<List<GlobalEvent>> = _globalEvents

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

    // Method to get global events for a specific date
    fun getGlobalEventsForDate(date: String): List<GlobalEvent> {
        if (date.isNullOrEmpty()) {
            Log.e("HolidayCalendarController", "Empty date provided to getGlobalEventsForDate")
            return emptyList()
        }

        Log.d("HolidayCalendarController", "Looking for global events on date: $date")
        Log.d("HolidayCalendarController", "Total global events available: ${_globalEvents.value?.size ?: 0}")

        // Hard-coded test for specific API event dates to verify if our data is loaded correctly
        // The dates below are taken directly from the API response you provided
        val knownDates = listOf(
            "08-03-2025", // International Women's Day
            "22-04-2025", // Earth Day
            "07-04-2025", // World Health Day
            "01-05-2025", // International Workers' day
            "05-06-2025", // World Environment Day
            "21-09-2025", // World Peace Day
            "19-11-2025", // International Men's Day
            "11-05-2025", // International Mother's Day
            "15-06-2025", // International Father's Day
            "11-04-2025", // International Pets Day
            "28-06-2025" // LGBT Pride Day
        )

        // Check if the requested date matches any known global event date
        if (knownDates.contains(date)) {
            Log.d("HolidayCalendarController", "**** FOUND DIRECT MATCH FOR KNOWN DATE: $date ****")
        }

        // First try parsing the request date
        val requestDate = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            LocalDate.parse(date, formatter)
        } catch (e: Exception) {
            Log.e("HolidayCalendarController", "Error parsing request date: $date - ${e.message}")
            return emptyList()
        }

        // Safe access to global events
        if (_globalEvents.value.isNullOrEmpty()) {
            Log.d("HolidayCalendarController", "No global events available")
            return emptyList()
        }

        // Log available events for debugging
        _globalEvents.value?.forEach { event ->
            Log.d("HolidayCalendarController", "Available global event: ${event.name}, date: ${event.date}")

            // Direct string comparison check
            if (event.date == date) {
                Log.d("HolidayCalendarController", "**** DIRECT STRING MATCH: ${event.name} on $date ****")
            }
        }

        // Find events matching this date
        val events = _globalEvents.value?.filter { event ->
            if (event.date.isNullOrEmpty()) {
                Log.e("HolidayCalendarController", "Global event ${event.name} has null or empty date")
                return@filter false
            }

            // FIRST: Try simple string equality for direct match
            if (event.date == date) {
                Log.d("HolidayCalendarController", "Direct string match for ${event.name}: ${event.date} == $date")
                return@filter true
            }

            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val eventDate = LocalDate.parse(event.date, formatter)

                // Extract day and month for simple comparison
                val eventDay = eventDate.dayOfMonth
                val eventMonth = eventDate.monthValue
                val requestDay = requestDate.dayOfMonth
                val requestMonth = requestDate.monthValue

                // Compare day and month (ignore year for simplicity)
                val matches = (eventDay == requestDay && eventMonth == requestMonth)
                Log.d("HolidayCalendarController", "Comparing ${event.name}: day $eventDay/$eventMonth == $requestDay/$requestMonth => $matches")
                matches
            } catch (e: Exception) {
                Log.e("HolidayCalendarController", "Error comparing dates for event ${event.name}: ${e.message}")
                // Attempt direct string comparison as fallback
                val eventDateParts = event.date.split("-")
                val requestDateParts = date.split("-")

                if (eventDateParts.size >= 2 && requestDateParts.size >= 2) {
                    val matches = (eventDateParts[0] == requestDateParts[0] && eventDateParts[1] == requestDateParts[1])
                    Log.d("HolidayCalendarController", "Fallback string comparison for ${event.name}: ${eventDateParts[0]}-${eventDateParts[1]} == ${requestDateParts[0]}-${requestDateParts[1]} => $matches")
                    matches
                } else {
                    false
                }
            }
        } ?: emptyList()

        Log.d("HolidayCalendarController", "Found ${events.size} global events for date $date")
        return events
    }

    // Method to get all global events
    fun getAllGlobalEvents(): List<GlobalEvent> {
        return _globalEvents.value ?: emptyList()
    }

    // Method to get global events for a specific month
    fun getGlobalEventsForMonth(month: Int): List<GlobalEvent> {
        return _globalEvents.value?.filter { event ->
            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val eventDate = LocalDate.parse(event.date, formatter)

                // Global events are single-day events, so we just check if they're in this month
                eventDate.monthValue == month
            } catch (e: Exception) {
                Log.e("HolidayCalendarController", "Error parsing date for global event: ${e.message}")
                false
            }
        } ?: emptyList()
    }
    fun onViewClick(context: Context, documentName: String, filePath: String) {
        if (filePath.isNullOrEmpty()) {
            Toast.makeText(context, "No document found for $documentName. Please upload document for the same.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the file is an image based on extension
        val isImage = filePath.endsWith(".jpg", ignoreCase = true) ||
            filePath.endsWith(".jpeg", ignoreCase = true) ||
            filePath.endsWith(".png", ignoreCase = true) ||
            filePath.endsWith(".webp", ignoreCase = true)

        // Create appropriate intent based on file type
        val intent = if (isImage) {
            Intent(context, ImageViewerActivity::class.java)
        } else {
            Intent(context, WebViewActivity::class.java)
        }

        intent.putExtra("fileUrl", filePath)
        intent.putExtra("title", documentName)
        context.startActivity(intent)
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

                        // Update milestones and log count
                        _milestones.value = calendarResponse.milestones
                        Log.d("HolidayCalendarController", "Loaded ${calendarResponse.milestones.size} milestones")

                        // Update global events and log count
                        _globalEvents.value = calendarResponse.globalEvents
                        Log.d("HolidayCalendarController", "Loaded ${calendarResponse.globalEvents.size} global events")

                        // Debug each global event
                        calendarResponse.globalEvents.forEach { event ->
                            Log.d("HolidayCalendarController", "Global event loaded: ${event.name}, date: ${event.date}")
                        }
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
