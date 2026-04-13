package com.archeGlobal.one.controller

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.archeGlobal.one.AssetActivity
import com.archeGlobal.one.BusinessCardActivity
import com.archeGlobal.one.CommuniqueActivity
import com.archeGlobal.one.GreetingsActivity
import com.archeGlobal.one.HolidayCalendarActivity
import com.archeGlobal.one.LocationsActivity
import com.archeGlobal.one.MeetSpaceActivity
import com.archeGlobal.one.PolicyActivity
import com.archeGlobal.one.model.PunchInRequest
import com.archeGlobal.one.model.PunchInResponse
import com.archeGlobal.one.model.AboutMeModel

import com.archeGlobal.one.model.CelebrationResponse
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.HomeItem
import com.archeGlobal.one.model.HomeModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.network.Service
import com.archeGlobal.one.utils.ImageCache
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Comparator
import java.util.Date
import java.util.Locale

class HomeController(
    private val navigator: Navigator,
    private val context: Context,
    initialModel: HomeModel = HomeModel(),
) {
    val attendanceController = AttendanceController(context)

    var showPunchInDialog by mutableStateOf(false)
    var showPunchOutDialog by mutableStateOf(false)
    var currentLocationText by mutableStateOf("Fetching location...")
    var isPunchedIn by mutableStateOf(false)
    var punchInTime by mutableStateOf("")
    var punchOutTime by mutableStateOf("")
    var timeSpent by mutableStateOf("00 h 00 m")
    private var punchId by mutableIntStateOf(-1)
    private var timerJob: kotlinx.coroutines.Job? = null
    private var punchInDateTime: Date? = null

    val hasReportees: Boolean
        get() = UserDataManager.getInstance(context).getUserData()?.hasReportees ?: false


    fun showPunchIn() {
        showPunchInDialog = true
        fetchCurrentLocation()
    }

    fun dismissPunchInDialog() {
        showPunchInDialog = false
    }

    fun onPunchInConfirmed() {
        dismissPunchInDialog()
        
        val userData = UserDataManager.getInstance(context).getUserData()
        val request = com.archeGlobal.one.model.PunchInRequest(
            employeeCode = userData?.employeeId ?: "",
            employeeName = userData?.name ?: "",
            employeeEmail = userData?.email ?: "",
            location = currentLocationText
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.punchIn(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        isPunchedIn = true
                        punchId = data?.id ?: -1
                        punchOutTime = ""
                        
                        // Parse punchIn time for the timer and display
                        val apiFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val displayFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                        try {
                             data?.punchIn?.let {
                                 val parsedDate = apiFormat.parse(it)
                                 if (parsedDate != null) {
                                     // Set display time
                                     punchInTime = displayFormat.format(parsedDate)

                                     // Set dateTime for timer calculation
                                     val calendar = Calendar.getInstance()
                                     val timeCalendar = Calendar.getInstance()
                                     timeCalendar.time = parsedDate
                                     calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                                     calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                                     calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
                                     punchInDateTime = calendar.time
                                 } else {
                                     punchInTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                     punchInDateTime = Date()
                                 }
                             } ?: run {
                                 punchInTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                 punchInDateTime = Date()
                             }
                        } catch (e: Exception) {
                            punchInTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            punchInDateTime = Date()
                        }
                        
                        android.widget.Toast.makeText(context, response.body()?.message, android.widget.Toast.LENGTH_SHORT).show()
                        savePunchState()
                        startTimer()
                    } else {
                        android.widget.Toast.makeText(context, "Punch in failed: ${response.body()?.message ?: "Unknown error"}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isPunchedIn) {
                val now = Date()
                val diff = now.time - (punchInDateTime?.time ?: now.time)
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                
                withContext(Dispatchers.Main) {
                    timeSpent = String.format("%02d h %02d m", hours, minutes)
                    savePunchState()
                }
                kotlinx.coroutines.delay(60000) // Update every minute
            }
        }
    }

    fun showPunchOut() {
        showPunchOutDialog = true
        fetchCurrentLocation()
    }

    fun dismissPunchOutDialog() {
        showPunchOutDialog = false
    }

    fun onPunchOutConfirmed() {
        dismissPunchOutDialog()
        
        val request = com.archeGlobal.one.model.PunchOutRequest(
            id = punchId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.punchOut(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        isPunchedIn = false
                        timerJob?.cancel()
                        timerJob = null

                        val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val lastPunchOut = data?.punchOut?.lastOrNull()
                        val punchInFromApi = data?.punchIn ?: punchInTime

                        // Update punchOutTime from API response
                        punchOutTime = lastPunchOut ?: fmt.format(Date())

                        // Calculate timeSpent as punchIn → latest punchOut using server times
                        timeSpent = if (!punchInFromApi.isNullOrEmpty() && !lastPunchOut.isNullOrEmpty()) {
                            try {
                                val inTime = fmt.parse(punchInFromApi)
                                val outTime = fmt.parse(lastPunchOut)
                                if (inTime != null && outTime != null) {
                                    val diff = outTime.time - inTime.time
                                    if (diff > 0) {
                                        val hours = diff / (1000 * 60 * 60)
                                        val minutes = (diff / (1000 * 60)) % 60
                                        String.format("%02d h %02d m", hours, minutes)
                                    } else timeSpent
                                } else timeSpent
                            } catch (_: Exception) { timeSpent }
                        } else {
                            // Fallback: use punchInDateTime if server times unavailable
                            val diff = Date().time - (punchInDateTime?.time ?: Date().time)
                            if (diff > 0) {
                                val hours = diff / (1000 * 60 * 60)
                                val minutes = (diff / (1000 * 60)) % 60
                                String.format("%02d h %02d m", hours, minutes)
                            } else timeSpent
                        }

                        android.widget.Toast.makeText(context, response.body()?.message, android.widget.Toast.LENGTH_SHORT).show()
                        savePunchState()
                    } else {
                        android.widget.Toast.makeText(context, "Punch out failed: ${response.body()?.message ?: "Unknown error"}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun fetchLocationAfterPermission() {
        currentLocationText = "Fetching location..."
        fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hasCoarse = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
                val hasFine = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasCoarse && !hasFine) {
                    withContext(Dispatchers.Main) { currentLocationText = "Location permission required" }
                    return@launch
                }

                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

                if (location == null) {
                    withContext(Dispatchers.Main) { currentLocationText = "Determining location..." }
                    return@launch
                }

                @Suppress("DEPRECATION")
                val addresses = Geocoder(context, Locale.getDefault())
                    .getFromLocation(location.latitude, location.longitude, 1)

                val address = addresses?.firstOrNull()
                val locationStr = if (address != null) {
                    listOfNotNull(
                        address.subLocality,
                        address.locality ?: address.subAdminArea,
                        address.adminArea,
                        address.countryName,
                        address.postalCode,
                    ).filter { it.isNotBlank() }.joinToString(", ")
                } else {
                    "${location.latitude}, ${location.longitude}"
                }

                withContext(Dispatchers.Main) { currentLocationText = locationStr }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { currentLocationText = "Unable to fetch location" }
            }
        }
    }

    var employeeData by mutableStateOf(
        AboutMeModel(
            name = OtpVerificationController.getUserData()?.name ?: "",
            email = OtpVerificationController.getUserData()?.email ?: "",
        ),
    )
    private val _eventData = MutableStateFlow<com.archeGlobal.one.model.EventResponse?>(null)
    val eventData: StateFlow<com.archeGlobal.one.model.EventResponse?> = _eventData.asStateFlow()

    private val _showEventPopup = MutableStateFlow(false)
    val showEventPopup: StateFlow<Boolean> = _showEventPopup.asStateFlow()

    // Pride Month specific state
    private val _isPrideMonth = MutableStateFlow(false)
    val isPrideMonth: StateFlow<Boolean> = _isPrideMonth.asStateFlow()

    private val _showPrideMonthDialog = MutableStateFlow(false)
    val showPrideMonthDialog: StateFlow<Boolean> = _showPrideMonthDialog.asStateFlow()

    // Celebration state
    private val _celebrationData = MutableStateFlow<CelebrationResponse?>(null)
    val celebrationData: StateFlow<CelebrationResponse?> =
        _celebrationData.asStateFlow().also {
            Log.d("CelebrationController", "CelebrationData StateFlow created")
        }

    private val _showCelebrationDialog = MutableStateFlow(false)
    val showCelebrationDialog: StateFlow<Boolean> = _showCelebrationDialog.asStateFlow()

    // WhatsNew dialog state management
    private val _showWhatsNewDialog = MutableStateFlow(false)
    val showWhatsNewDialog: StateFlow<Boolean> = _showWhatsNewDialog.asStateFlow()

    // Initialize PreferencesManager early to avoid null pointer exceptions
    private val preferencesManager by lazy { PreferencesManager(context) }

    // Companion object and other class members follow
    companion object {
        private const val PREF_NAME = "event_preferences"
        private const val KEY_LAST_SHOWN_DATE = "last_shown_date"
        private const val KEY_PRIDE_MONTH_SHOWN = "pride_month_shown"
        private const val KEY_USING_PRIDE_ICON = "using_pride_icon"
        private const val KEY_WHATS_NEW_SHOWN = "whats_new_shown"
    }

    // Initialize event handling
    init {
        Log.d("EventController", "HomeController INIT BLOCK 1 - ${this.hashCode()}")
        Log.d("EventController", "Initializing HomeController and fetching daily event")
        Log.d("EventController", "UserDataManager instance: ${UserDataManager.getInstance(context)}")

        // Load persisted punch state
        loadPunchState()

        if (_isPrideMonth.value) {
            // Don't automatically show Pride Month dialog - only show when pinned message is clicked
            // Just set the flag that it's Pride Month, but don't show dialog
            Log.d("EventController", "It's Pride Month, but not showing dialog automatically")
        } else {
            // If not Pride Month, fetch the regular daily event
            fetchEventFromLoginData()
        }

        // Fetch celebration data only if auth token exists (user is already logged in)
        val preferencesManager = PreferencesManager(context)
        if (preferencesManager.getAuthToken() != null) {
            Log.d("CelebrationController", "Auth token exists, fetching celebration data immediately")
            fetchCelebrationData()
        } else {
            Log.d("CelebrationController", "No auth token found, will fetch celebration data after login")
        }

        // Check if WhatsNew dialog should be shown
        checkWhatsNewDialog()
    }

    // Add these methods for punch state persistence
    fun loadPunchState() {
        val state = preferencesManager.getPunchState()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        Log.d("PunchState", "loadPunchState() called | today=$today | savedDate=${state.lastPunchDate}")
        Log.d("PunchState", "Saved state | isPunchedIn=${state.isPunchedIn} | punchIn=${state.punchInTime} | punchOut=${state.punchOutTime} | punchId=${state.punchId} | timeSpent=${state.timeSpent}")

        if (state.lastPunchDate != today) {
            Log.d("PunchState", "No valid saved state for today — seeding from login API response")

            // No saved state for today (new day, fresh login, or user switched) —
            // seed punch state from the login API response for the current user.
            val userData = UserDataManager.getInstance(context).getUserData()
            val loginPunchIn = userData?.loginPunchIn
            val loginPunchOut = userData?.loginPunchOut
            val loginPunchId = userData?.loginPunchId ?: -1

            Log.d("PunchState", "Login API attendance | punchIn=$loginPunchIn | punchOut=$loginPunchOut | attendanceId=$loginPunchId")

            if (!loginPunchIn.isNullOrEmpty()) {
                isPunchedIn = loginPunchOut.isNullOrEmpty()
                punchInTime = loginPunchIn
                punchOutTime = loginPunchOut ?: ""
                punchId = loginPunchId
                punchInDateTime = null

                Log.d("PunchState", "Seeded from login API | isPunchedIn=$isPunchedIn | punchInTime=$punchInTime | punchOutTime=$punchOutTime | punchId=$punchId")

                val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                if (!isPunchedIn && !loginPunchOut.isNullOrEmpty()) {
                    // Already punched out — calculate final timeSpent from punchIn → punchOut
                    timeSpent = try {
                        val inTime = fmt.parse(loginPunchIn)
                        val outTime = fmt.parse(loginPunchOut)
                        if (inTime != null && outTime != null) {
                            val diff = outTime.time - inTime.time
                            if (diff > 0) {
                                val hours = diff / (1000 * 60 * 60)
                                val minutes = (diff / (1000 * 60)) % 60
                                String.format("%02d h %02d m", hours, minutes)
                            } else "00 h 00 m"
                        } else "00 h 00 m"
                    } catch (_: Exception) { "00 h 00 m" }
                    Log.d("PunchState", "Already punched out | timeSpent=$timeSpent (punchIn=$loginPunchIn → punchOut=$loginPunchOut)")
                } else {
                    // Still punched in — set punchInDateTime and show elapsed time immediately
                    timeSpent = "00 h 00 m"
                    try {
                        val parsed = fmt.parse(loginPunchIn)
                        if (parsed != null) {
                            val cal = java.util.Calendar.getInstance()
                            val timeCal = java.util.Calendar.getInstance()
                            timeCal.time = parsed
                            cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                            cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                            cal.set(java.util.Calendar.SECOND, timeCal.get(java.util.Calendar.SECOND))
                            punchInDateTime = cal.time
                            // Show elapsed time right away instead of waiting for timer tick
                            val diff = Date().time - cal.timeInMillis
                            if (diff > 0) {
                                val hours = diff / (1000 * 60 * 60)
                                val minutes = (diff / (1000 * 60)) % 60
                                timeSpent = String.format("%02d h %02d m", hours, minutes)
                            }
                            Log.d("PunchState", "Still punched in | punchInDateTime=${punchInDateTime} | initial timeSpent=$timeSpent | starting timer")
                            startTimer()
                        }
                    } catch (e: Exception) {
                        Log.e("PunchState", "Failed to parse loginPunchIn time: $loginPunchIn | error=${e.message}")
                    }
                }
            } else {
                Log.d("PunchState", "No loginPunchIn in login API response — resetting to clean state")
                isPunchedIn = false
                punchInTime = ""
                punchOutTime = ""
                timeSpent = "00 h 00 m"
                punchId = -1
                punchInDateTime = null
            }
            savePunchState()
            Log.d("PunchState", "Saved seeded state to prefs")
        } else {
            // Load persisted state for today (belongs to the current user since we clear on logout)
            Log.d("PunchState", "Loading persisted state for today")
            isPunchedIn = state.isPunchedIn
            punchInTime = state.punchInTime
            punchOutTime = state.punchOutTime
            timeSpent = state.timeSpent
            punchId = state.punchId
            if (state.punchInDateTime > 0) {
                punchInDateTime = Date(state.punchInDateTime)
            }
            Log.d("PunchState", "Restored | isPunchedIn=$isPunchedIn | punchIn=$punchInTime | punchOut=$punchOutTime | punchId=$punchId | timeSpent=$timeSpent | punchInDateTime=$punchInDateTime")

            if (isPunchedIn) {
                Log.d("PunchState", "Resuming timer from persisted state")
                startTimer()
            }
        }
    }

    fun savePunchState() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        preferencesManager.savePunchState(
            isPunchedIn = isPunchedIn,
            punchInTime = punchInTime,
            punchInDateTime = punchInDateTime?.time ?: 0L,
            punchId = punchId,
            punchOutTime = punchOutTime,
            lastPunchDate = today,
            timeSpent = timeSpent
        )
    }

    init {
        Log.d("EventController", "HomeController INIT BLOCK 2 - ${this.hashCode()}")
        // Check app version and handle first install vs updates vs returning users
        checkAppVersionAndMarkServices()
    }

    // Helper method to navigate within the same activity
    private fun navigate(route: String) {
        if (navigator is AndroidNavigator) {
            navigator.navController?.navigate(route)
        }
    }

    // Add these:
    private var navigationCount = 0
    var onShowRatingDialog: (() -> Unit)? = null

    private fun handleNavigation(action: () -> Unit) {
        navigationCount++
        if (navigationCount % 10 == 0) {
            onShowRatingDialog?.invoke()
        }
        action()
    }

    // Event-related methods
    // Process event data to ensure image URLs are valid and properly formatted
    private fun processEventData(event: com.archeGlobal.one.model.EventResponse?): com.archeGlobal.one.model.EventResponse? {
        if (event == null) return null
        val originalImageUrl = event.image ?: ""

        Log.d("EventController", "Processing event image URL: $originalImageUrl")

        // Check if the image URL is valid and properly formatted
        if (originalImageUrl.isBlank()) {
            Log.d("EventController", "Image URL is blank or null, returning original event")
            return event // Return original event if image is blank or null
        }

        // Ensure the URL is properly formatted (starts with http:// or https://)
        val formattedImageUrl =
            if (!originalImageUrl.startsWith("http://") && !originalImageUrl.startsWith("https://")) {
                // Assuming dev.arche.global is the base for relative paths
                "https://dev.arche.global:7000/$originalImageUrl".trim()
            } else {
                originalImageUrl.trim()
            }

        Log.d("EventController", "Formatted event image URL: $formattedImageUrl")
        return event.copy(image = formattedImageUrl)
    }

    private fun fetchEventFromLoginData() {
        Log.d("EventController", "Starting to fetch daily event from login response")
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                // Get event data from UserDataManager instead of making a separate API call
                val userDataManager = UserDataManager.getInstance(context)
                var eventResponse = userDataManager.getEventData() // eventResponse is EventResponse?
                Log.d("EventController", "Fetched event from UserDataManager: Title=${eventResponse?.title}, Image=${eventResponse?.image}")

                // Process the event data (e.g., format image URL)
                eventResponse = processEventData(eventResponse)

                // Update the StateFlow with the processed event data
                _eventData.value = eventResponse
                Log.d(
                    "EventController",
                    "Updated _eventData StateFlow. New value: Title=${_eventData.value?.title}, Image=${_eventData.value?.image}",
                )

                // Switch to main thread for UI updates related to event popup visibility
                withContext(Dispatchers.Main) {
                    checkIfShouldShowEvent() // This will use the new _eventData.value
                }
            } catch (e: Exception) {
                Log.e("EventController", "Exception while fetching event from login data: ${e.message}")
                e.printStackTrace()
                // Do not create a mock event on exception
                _eventData.value = null
            }
        }
    }

    // Helper function to check if event response is empty
    private fun isEventResponseEmpty(event: com.archeGlobal.one.model.EventResponse?): Boolean {
        if (event == null) return true

        // Check if all important fields are null or empty
        val hasTitle = !event.title.isNullOrBlank()
        val hasDescription = !event.description.isNullOrBlank()
        val hasImage = !event.image.isNullOrBlank()
        val hasDate = !event.date.isNullOrBlank()

        // Event is considered empty if it has no meaningful content
        val isEmpty = !hasTitle && !hasDescription && !hasImage && !hasDate

        Log.d(
            "EventController",
            "Event emptiness check - hasTitle: $hasTitle, hasDescription: $hasDescription, hasImage: $hasImage, hasDate: $hasDate, isEmpty: $isEmpty",
        )

        return isEmpty
    }

    private fun checkIfShouldShowEvent() {
        // If it's Pride Month, don't show regular events and don't auto-show Pride Month dialog
        if (_isPrideMonth.value) {
            Log.d("EventController", "It's Pride Month, not showing regular events or auto Pride Month dialog")
            return
        }

        // Only show the event if we have event data and it's not empty
        val currentEventData = _eventData.value
        if (currentEventData == null || isEventResponseEmpty(currentEventData)) {
            Log.d("EventController", "Event data is null or empty, not showing popup")
            _showEventPopup.value = false
            return
        }

        Log.d("EventController", "Event data available: Title=${currentEventData.title}, Image=${currentEventData.image}")

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastShownDate = sharedPref.getString(KEY_LAST_SHOWN_DATE, "")
        val currentDate = getCurrentDate()

        Log.d("EventController", "Last shown date: '$lastShownDate', Current date: '$currentDate'")

        // Show popup if it hasn't been shown today
        if (lastShownDate != currentDate) {
            Log.d("EventController", "Setting showEventPopup to TRUE - not shown today yet")
            _showEventPopup.value = true

            // For debugging purposes, let's log the current state
            Log.d("EventController", "Current state - showEventPopup: ${_showEventPopup.value}, eventData: ${_eventData.value != null}")
        } else {
            Log.d("EventController", "Setting showEventPopup to FALSE - already shown today")
            _showEventPopup.value = false
        }
    }

    fun dismissEventPopup() {
        Log.d("EventController", "dismissEventPopup called")

        // Save the current date as the last shown date
        val currentDate = getCurrentDate()
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        Log.d("EventController", "Saving last shown date: $currentDate")

        with(sharedPref.edit()) {
            putString(KEY_LAST_SHOWN_DATE, currentDate)
            apply()
        }

        // Verify the date was saved correctly
        val savedDate = sharedPref.getString(KEY_LAST_SHOWN_DATE, "")
        Log.d("EventController", "Verified saved date: $savedDate")

        // Hide the popup
        _showEventPopup.value = false
        Log.d("EventController", "Set showEventPopup to false")

        _showEventPopup.value = false
    }

    // Format date as yyyy-MM-dd
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Check if current month is June (Pride Month)
    open fun checkIfPrideMonth() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)

        // June is month 5 in Calendar (0-based index)
        _isPrideMonth.value = currentMonth == Calendar.JUNE

        Log.d("EventController", "Current month: ${currentMonth + 1}, Is Pride Month: ${_isPrideMonth.value}")
    }

    // Check if we should show the Pride Month dialog
    fun checkIfShouldShowPrideMonthDialog() {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val prideMonthShown = sharedPref.getBoolean(KEY_PRIDE_MONTH_SHOWN, false)

        // Show Pride Month dialog if it hasn't been shown yet during this Pride Month
        if (!prideMonthShown) {
            Log.d("EventController", "Setting showPrideMonthDialog to TRUE - not shown yet this Pride Month")
            _showPrideMonthDialog.value = true

            // Mark as shown
            with(sharedPref.edit()) {
                putBoolean(KEY_PRIDE_MONTH_SHOWN, true)
                apply()
            }
        } else {
            Log.d("EventController", "Not showing Pride Month dialog - already shown this Pride Month")
        }
    }

    // Force show Pride Month dialog (triggered by pin)
    fun showPrideMonthDialog() {
        _showPrideMonthDialog.value = true
    }

    // Dismiss Pride Month dialog
    fun dismissPrideMonthDialog() {
        Log.d("EventController", "dismissPrideMonthDialog called")
        _showPrideMonthDialog.value = false
    }

    // Toggle between default and Pride Month launcher icons by enabling/disabling
    // activity-alias components declared in AndroidManifest.xml
    fun togglePrideIcon() {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentlyUsingPrideIcon = sharedPref.getBoolean(KEY_USING_PRIDE_ICON, false)

        // Component names for the two launcher aliases
        val defaultAlias =
            android.content.ComponentName(
                context,
                "com.archeGlobal.one.SplashAlias",
            )
        val prideAlias =
            android.content.ComponentName(
                context,
                "com.archeGlobal.one.SplashAliasPride",
            )

        val pm = context.packageManager
        if (currentlyUsingPrideIcon) {
            // Switch back to default icon
            pm.setComponentEnabledSetting(
                defaultAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP,
            )
            pm.setComponentEnabledSetting(
                prideAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP,
            )
        } else {
            // Switch to pride icon
            pm.setComponentEnabledSetting(
                prideAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP,
            )
            pm.setComponentEnabledSetting(
                defaultAlias,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP,
            )
        }

        // Persist the new state
        sharedPref.edit().putBoolean(KEY_USING_PRIDE_ICON, !currentlyUsingPrideIcon).apply()

        // Notify user – the launcher might take a moment to refresh
        val msg =
            if (currentlyUsingPrideIcon) {
                "Switched back to regular app icon"
            } else {
                "Switched to Pride app icon"
            }
        android.widget.Toast
            .makeText(context, msg, android.widget.Toast.LENGTH_SHORT)
            .show()

        // Close the dialog
        dismissPrideMonthDialog()
    }

    // Check if we're using the Pride icon
    fun isUsingPrideIcon(): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_USING_PRIDE_ICON, false)
    }

    // Celebration methods - restored to original working version
    private fun fetchCelebrationData() {
        val userData = OtpVerificationController.getUserData()
        Log.d("CelebrationController", "Fetching celebration data - User data available: ${userData != null}, Email: ${userData?.email}")
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val response = RetrofitClient.apiService.getEmployeeCelebration()
                if (response.isSuccessful) {
                    val celebrationData = response.body()
                    val todayCount = celebrationData?.today?.size ?: 0
                    val tomorrowCount = celebrationData?.tomorrow?.size ?: 0
                    Log.d("CelebrationController", "Celebration data fetched successfully - Today: $todayCount, Tomorrow: $tomorrowCount")
                    Log.d("CelebrationController", "Setting celebration data to StateFlow...")
                    _celebrationData.value = celebrationData
                    Log.d("CelebrationController", "StateFlow updated. Current value not null: ${_celebrationData.value != null}")
                    Log.d(
                        "CelebrationController",
                        "StateFlow celebration count: Today=${_celebrationData.value?.today?.size}, Tomorrow=${_celebrationData.value?.tomorrow?.size}",
                    )
                } else {
                    Log.e("CelebrationController", "Failed to fetch celebration data: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CelebrationController", "Exception while fetching celebration data: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun showCelebrationDialog() {
        Log.d("CelebrationController", "Showing celebration dialog")
        _showCelebrationDialog.value = true
    }

    fun dismissCelebrationDialog() {
        Log.d("CelebrationController", "Dismissing celebration dialog")
        _showCelebrationDialog.value = false
    }

    fun showWhatsNewDialog() {
        Log.d("HomeController", "Showing WhatsNew dialog")
        _showWhatsNewDialog.value = true
    }

    fun dismissWhatsNewDialog() {
        Log.d("HomeController", "Dismissing WhatsNew dialog")
        _showWhatsNewDialog.value = false
        // Mark as shown so it doesn't show again
        preferencesManager.setBoolean(KEY_WHATS_NEW_SHOWN, true)
    }

    fun showEventPopupDialog() {
        _showEventPopup.value = true
    }

    private fun checkWhatsNewDialog() {
        Log.d("HomeController", "Checking if WhatsNew dialog should be shown")

        // Check if dialog has already been shown
        val alreadyShown = preferencesManager.getBoolean(KEY_WHATS_NEW_SHOWN, false)
        if (alreadyShown) {
            Log.d("HomeController", "WhatsNew dialog already shown, skipping")
            return
        }

        // Check if we have WhatsNew data from the login response
        val whatsNewData = UserDataManager.getInstance(context).getWhatsNewData()
        if (whatsNewData.isNullOrEmpty()) {
            Log.d("HomeController", "No WhatsNew data available, skipping dialog")
            return
        }

        // Check install type - only show on fresh install
        val installType = preferencesManager.getInstallType()
        if (installType != "NEW") {
            Log.d("HomeController", "Not a fresh install (installType: $installType), skipping WhatsNew dialog")
            return
        }

        Log.d("HomeController", "Showing WhatsNew dialog for fresh install with ${whatsNewData.size} items")
        _showWhatsNewDialog.value = true
    }

    fun onCelebrationWishesClick(
        email: String,
        employeeName: String,
        celebrationType: String,
    ) {
        Log.d("CelebrationController", "Wishes clicked for email: $email, name: $employeeName, type: $celebrationType")

        // Get greetings data from UserDataManager
        val userDataManager = UserDataManager.getInstance(context)
        val greetingsData = userDataManager.getGreetingsData()
        val categoryMessages = userDataManager.getGreetingCategoriesData()

        // Find the appropriate category based on celebration type
        val categoryName =
            when (celebrationType.lowercase()) {
                "birthday" -> "Birthday"
                "work anniversary" -> "Career Milestone"
                else -> celebrationType
            }

        // Get greetings for the category
        val categoryGreetings = greetingsData?.get(categoryName) ?: emptyList()
        val firstGreeting = categoryGreetings.firstOrNull() ?: ""

        // Get default message for the category and pre-fill with employee name
        val defaultMessage = categoryMessages?.find { it.name == categoryName }?.message ?: ""
        val personalizedMessage =
            if (defaultMessage.isNotEmpty()) {
                // Replace any generic greetings with the actual employee name
                defaultMessage
                    .replace("Dear colleague", "Dear $employeeName", ignoreCase = true)
                    .replace("Dear team member", "Dear $employeeName", ignoreCase = true)
                    .replace("Dear employee", "Dear $employeeName", ignoreCase = true)
                    .replace("Dear friend", "Dear $employeeName", ignoreCase = true)
                    .replace("Dear one", "Dear $employeeName", ignoreCase = true)
                    .let { message ->
                        // If the message doesn't start with "Dear [name]", prepend it
                        if (!message.trimStart().startsWith("Dear $employeeName", ignoreCase = true)) {
                            "Dear $employeeName,\n\n$message"
                        } else {
                            message
                        }
                    }
            } else {
                "Dear $employeeName,\n\nCongratulations on your special day!"
            }

        // Navigate directly to GreetingDetailActivity
        val intent = Intent(context, com.archeGlobal.one.GreetingDetailActivity::class.java)
        intent.putExtra("imageUrl", firstGreeting)
        intent.putExtra("category", categoryName)
        intent.putExtra("message", personalizedMessage)
        intent.putExtra("recipientEmail", email)
        intent.putExtra("recipientName", employeeName)
        intent.putStringArrayListExtra("allGreetings", ArrayList(categoryGreetings))

        context.startActivity(intent)
        dismissCelebrationDialog()
    }

    var model by mutableStateOf(
        HomeModel(
            userName = OtpVerificationController.getUserData()?.name ?: "",
            designation = OtpVerificationController.getUserData()?.designation ?: "",
            department = OtpVerificationController.getUserData()?.department ?: "",
            employeeId = OtpVerificationController.getUserData()?.employeeId ?: "",
            profilePicture = OtpVerificationController.getUserData()?.profilePic,
            showAllApps = true,
            categories =
                OtpVerificationController.getUserData()?.let { userData ->
                    userData.services
                        .groupBy { it.category }
                        .toSortedMap(
                            Comparator { a, b ->
                                // If either is MyApps, handle special case
                                when {
                                    a.equals("MyApps", ignoreCase = true) && !b.equals("MyApps", ignoreCase = true) -> 1
                                    !a.equals("MyApps", ignoreCase = true) && b.equals("MyApps", ignoreCase = true) -> -1
                                    else -> a.compareTo(b, ignoreCase = true)
                                }
                            },
                        ).mapValues { (_, services) ->
                            services.map { service ->
                                HomeItem(
                                    title = service.service,
                                    icon = service.icon ?: service.service.lowercase().replace(" ", ""),
                                    isFavorite = service.favourite,
                                    category = service.category,
                                    isNew = shouldShowAsNew(service),
                                    stickerText = getStickerText(),
                                )
                            }
                        }
                } ?: emptyMap(),
            favorites = preferencesManager.getFavorites(),
            footerNavigation =
                FooterNavigationModel(
                    showHome = true,
                    showChat = false,
                    showSOS = false,
                    showProfile = false,
                ),
        ),
    )
        private set

    fun onItemClick(item: HomeItem) {
        handleNavigation {
            // ...existing navigation logic...
            Log.d("HomeController", "onItemClick: ${item.title}")
            Log.d("HomeController", "onItemClick lowercase: ${item.title.lowercase()}")
            when (item.title.lowercase()) {
                "locations" -> {
                    // Load locations data on-demand before navigating
                    if (context is com.archeGlobal.one.HomeActivity) {
                        Log.d("HomeController", "Loading locations data on-demand")
                        context.locationsController.onServiceAccessed()
                    }
                    val intent = Intent(context, LocationsActivity::class.java)
                    context.startActivity(intent)
                }
                "business card" -> {
                    // Business card doesn't require data loading - navigate directly
                    Log.d("HomeController", "Navigating to Business Card")
                    val intent = Intent(context, BusinessCardActivity::class.java)
                    context.startActivity(intent)
                }
                "asset" -> {
                    // Load asset data on-demand before navigating
                    if (context is com.archeGlobal.one.HomeActivity) {
                        Log.d("HomeController", "Loading asset data on-demand")
                        context.assetController.onServiceAccessed()
                    }
                    val intent = Intent(context, AssetActivity::class.java)
                    context.startActivity(intent)
                }
                "deskcart" -> {
                    // Load DeskCart data on-demand before navigating
                    if (context is com.archeGlobal.one.HomeActivity) {
                        Log.d("HomeController", "Loading DeskCart data on-demand")
                        context.deskCartController.onServiceAccessed()
                    }
                    Log.d("HomeController", "Navigating to DeskCart")
                    navigator.navigateToDeskCart()
                }
                "calendar" -> {
                    val intent = Intent(context, HolidayCalendarActivity::class.java)
                    context.startActivity(intent)
                }
                "greetings" -> {
                    val intent = Intent(context, GreetingsActivity::class.java)
                    context.startActivity(intent)
                }
                "policy" -> {
                    val intent = Intent(context, PolicyActivity::class.java)
                    context.startActivity(intent)
                }
                "profile connect" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for Profile Connect")
                    navigate("service_not_available?serviceName=Profile Connect")
                }
                "profile" -> {
                    // Load profile data on-demand before navigating
                    if (context is com.archeGlobal.one.HomeActivity) {
                        Log.d("HomeController", "Loading profile data on-demand")
                        context.profileController.onServiceAccessed()
                    }
                    Log.d("HomeController", "Navigating to Profile")
                    navigator.navigateToProfile()
                }
                "zentask" -> {
                    Log.d("HomeController", "Navigating to Zentask")
                    navigator.navigateToTodo()
                }
                "punch in" -> {
                    Log.d("HomeController", "Showing Punch In dialog")
                    showPunchIn()
                }
                "punch out" -> {
                    Log.d("HomeController", "Showing Punch Out dialog")
                    showPunchOut()
                }
                "id" -> navigator.navigateToID()
                "timesheet" -> {
                    Log.d("HomeController", "Navigating to Attendance screen")
                    attendanceController.resetToCurrentMonth()
                    attendanceController.fetchLeaveBalances()
                    navigate("attendance")
                }
                "apply leave" -> {
                    Log.d("HomeController", "Navigating to Apply Leave screen")
                    navigate("apply_leave")
                }
                "regularize" -> {
                    Log.d("HomeController", "Navigating to Attendance screen for regularization")
                    attendanceController.resetToCurrentMonth()
                    navigate("attendance")
                }
                "apply outdoor" -> {
                    Log.d("HomeController", "Navigating directly to Apply Outdoor Duty screen")
                    val today = java.time.LocalDate.now().toString()
                    navigate("apply_outdoor_duty?date=$today")
                }
                "manager approvals" -> {
                    Log.d("HomeController", "Navigating to Approval Requests screen")
                    if (context is com.archeGlobal.one.HomeActivity) {
                        context.approvalRequestsController.fetchManagerApprovals()
                    }
                    navigate("approval_requests")
                }
                "leave" -> {
                    Log.d("HomeController", "Navigating to Apply Leave screen")
                    navigate("apply_leave")
                }
                "my documents", "mydocuments" -> {
                    Log.d("MyDocuments", "Navigating to My Documents")
                    navigator.navigateToMyDocuments()
                }
                "my career" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for My Career")
                    navigate("service_not_available?serviceName=My Career")
                }
                "elearning" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for eLearning")
                    navigate("service_not_available?serviceName=eLearning")
                }
                "goal setting/kpi", "goal" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for Goal Setting/KPI")
                    navigate("service_not_available?serviceName=Goal Setting/KPI")
                }
                "medical" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("medical")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to Medical with URL: $url")
                        navigator.navigateToMedicalWithUrl(url)
                    } else {
                        Log.d("HomeController", "Medical URL not found in API response")
                        navigate("service_not_available?serviceName=Medical")
                    }
                }
                "mypay" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("mypay")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to MyPay with URL: $url")
                        navigator.navigateToMyPayWithUrl(url)
                    } else {
                        Log.d("HomeController", "MyPay URL not found in API response")
                        navigate("service_not_available?serviceName=MyPay")
                    }
                }
                "zinghr" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("zinghr")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to ZingHR with URL: $url")
                        navigator.navigateToZingHRWithUrl(url)
                    } else {
                        Log.d("HomeController", "ZingHR URL not found in API response")
                        navigate("service_not_available?serviceName=ZingHR")
                    }
                }
                "sap" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("sap")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to SAP with URL: $url")
                        navigator.navigateToSAPWithUrl(url)
                    } else {
                        Log.d("HomeController", "SAP URL not found in API response")
                        navigate("service_not_available?serviceName=SAP")
                    }
                }
                "ample" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("ample")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to Ample with URL: $url")
                        navigator.navigateToAmpleWithUrl(url)
                    } else {
                        Log.d("HomeController", "Ample URL not found in API response")
                        navigate("service_not_available?serviceName=Ample")
                    }
                }
                "about us", "aboutus" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("About Us")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to About Us with URL: $url")
                        navigator.navigateToAboutUsWithUrl(url)
                    } else {
                        Log.d("HomeController", "About Us URL not found in API response")
                        navigate("service_not_available?serviceName=About Us")
                    }
                }
                "archehonours" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("About Us")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to Arche Honours with URL: $url")
                        navigator.navigateToArcheHonoursWithUrl(url)
                    } else {
                        Log.d("HomeController", "Arche Honours URL not found in API response")
                        navigate("service_not_available?serviceName=Arche Honours")
                    }
                }
                "admin" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for Admin")
                    navigate("service_not_available?serviceName=Admin")
                }
                "hr" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for HR")
                    navigate("service_not_available?serviceName=HR")
                }
                "client calendar" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for Client Calendar")
                    navigate("service_not_available?serviceName=Client Calendar")
                }
                "connect" -> {
                    Log.d("XConnect", "Navigating to XConnect")
                    navigator.navigateToXConnect()
                }
                "blogs" -> {
                    Log.d("XConnect", "Navigating to XConnect for blogs")
                    navigator.navigateToXConnect("Blogs")
                }
                "helpdesk" -> {
                    Log.d("HomeController", "Navigating to Helpdesk screen")
                    navigate("helpdesk")
                }
                "announcements" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for Announcements")
                    navigate("service_not_available?serviceName=Announcements")
                }
                "xprofile" -> navigator.navigateToXProfile()
                "password reset" -> {
                    Log.d("HomeController", "Navigating to Service Not Available screen for Password Reset")
                    navigate("service_not_available?serviceName=Password Reset")
                }
                "sos" -> {
                    Log.d("SOS", "Navigating to SOS")
                    navigator.navigateToSOS(false)
                }
                "travel", "traveldesk", "travel desk" -> {
                    // Load travel data on-demand before navigating
                    if (context is com.archeGlobal.one.HomeActivity) {
                        Log.d("HomeController", "Loading travel data on-demand")
                        context.travelController.onServiceAccessed()
                        // Approval count will be loaded by TravelScreen's lifecycle (ON_RESUME)
                        // No need to call it here to avoid duplicate API calls
                    }
                    Log.d("HomeController", "Navigating to Travel Screen")
                    navigator.navigateToTravel()
                }
                "corevalues", "core values" -> {
                    Log.d("HomeController", "Navigating to Core Values")
                    navigator.navigateToCoreValues()
                }
                "vision" -> {
                    Log.d("HomeController", "Navigating to Vision")
                    navigator.navigateToVision()
                }
                "communique" -> {
                    val intent = Intent(context, CommuniqueActivity::class.java)
                    context.startActivity(intent)
                }
                "archeodyssey", "arche odyssey" -> {
                    Log.d("HomeController", "Navigating to Arche Odyssey")
                    navigator.navigateToArcheOdyssey()
                }
                "know your org" -> {
                    Log.d("HomeController", "Navigating to Arche Odyssey via Know Your Org")
                    navigator.navigateToArcheOdyssey()
                }
                "pulse" -> {
                    val url = UserDataManager.getInstance(context).getServiceUrl("pulse")
                    if (url != null) {
                        Log.d("HomeController", "Navigating to Pulse with URL: $url")
                        navigator.openPulseLoginWithUrl(url)
                    } else {
                        Log.d("HomeController", "Pulse URL not found in API response")
                        navigate("service_not_available?serviceName=Pulse")
                    }
                }
                "ideavault", "idea vault" -> {
                    Log.d("HomeController", "Navigating to Idea Vault")
                    navigator.navigateToIdeaVault()
                }
                "smart collateral" -> {
                    Log.d("HomeController", "Navigating to Smart Collateral")
                    val intent = Intent(context, com.archeGlobal.one.SmartCollateralActivity::class.java)
                    context.startActivity(intent)
                }
                "meetspace" -> {
                    Log.d("HomeController", "Navigating to MeetSpace")
                    val intent = Intent(context, MeetSpaceActivity::class.java)
                    context.startActivity(intent)
                }
                else -> {
                    // Default case for any non-handled services
                    Log.d("HomeController", "Navigating to Service Not Available screen for ${item.title}")
                    navigate("service_not_available?serviceName=${Uri.encode(item.title)}")
                }
            }
        }
    }

    fun onAllAppsClick() =
        handleNavigation {
            Log.d("HomeController", "All Apps clicked. Current state: ${model.showAllApps}")
            model =
                model.copy(
                    showAllApps = true,
                    viewFavorites = false,
                )
            Log.d("HomeController", "New state - showAllApps: ${model.showAllApps}, viewFavorites: ${model.viewFavorites}")
        }

    fun onFavoritesClick() =
        handleNavigation {
            Log.d("HomeController", "Favorites clicked. Current state: ${model.viewFavorites}")
            model =
                model.copy(
                    viewFavorites = true,
                    showAllApps = false,
                )
            Log.d("HomeController", "New state - showAllApps: ${model.showAllApps}, viewFavorites: ${model.viewFavorites}")
        }

    fun onShowProfileClick() =
        handleNavigation {
            navigator.navigateToXProfile()
        }

    fun onToggleFavorite(item: HomeItem) {
        val currentFavorites = model.favorites.toMutableMap()
        val category = item.category.ifEmpty { "Default" }

        val categoryFavorites = currentFavorites[category]?.toMutableList() ?: mutableListOf()

        if (item.isFavorite) {
            // Remove from favorites
            categoryFavorites.removeAll { it.title == item.title }
            if (categoryFavorites.isEmpty()) {
                currentFavorites.remove(category)
            } else {
                currentFavorites[category] = categoryFavorites
            }
        } else {
            // Add to favorites
            categoryFavorites.add(item.copy(isFavorite = true))
            currentFavorites[category] = categoryFavorites
        }

        // Sort favorites by category and update model
        val sortedFavorites = currentFavorites.toSortedMap(String.CASE_INSENSITIVE_ORDER)

        // Update model and save to preferences
        model = model.copy(favorites = sortedFavorites)
        preferencesManager.saveFavorites(sortedFavorites)

        // Update item's favorite status in categories
        val updatedCategories =
            model.categories.mapValues { (_, items) ->
                items.map {
                    if (it.title == item.title) {
                        it.copy(isFavorite = !it.isFavorite)
                    } else {
                        it
                    }
                }
            }

        model =
            model.copy(
                categories = updatedCategories,
            )
    }

    fun onFooterHomeClick() =
        handleNavigation {
            // Already on home screen, no action needed
        }

    fun onFooterChatClick() =
        handleNavigation {
            navigator.navigateToChat()
        }

    fun onFooterSOSClick() =
        handleNavigation {
            // Use the navigator to navigate to SOS screen
            navigator.navigateToSOS(true)
        }

    fun onFooterProfileClick() =
        handleNavigation {
            navigator.navigateToProfile()
        }

    fun onXCardClick() =
        handleNavigation {
            navigator.navigateToBusinessCard()
        }

    fun refreshUserData() {
        val userData = OtpVerificationController.getUserData()
        model =
            model.copy(
                userName = userData?.name ?: "",
                designation = userData?.designation ?: "",
                department = userData?.department ?: "",
                employeeId = userData?.employeeId ?: "",
                profilePicture = userData?.profilePic,
                categories =
                    userData?.let { data ->
                        data.services
                            .groupBy { it.category }
                            .toSortedMap(
                                Comparator { a, b ->
                                    // If either is MyApps, handle special case
                                    when {
                                        a.equals("MyApps", ignoreCase = true) && !b.equals("MyApps", ignoreCase = true) -> 1
                                        !a.equals("MyApps", ignoreCase = true) && b.equals("MyApps", ignoreCase = true) -> -1
                                        else -> a.compareTo(b, ignoreCase = true)
                                    }
                                },
                            ).mapValues { (_, services) ->
                                services.map { service ->
                                    HomeItem(
                                        title = service.service,
                                        icon = service.icon ?: service.service.lowercase().replace(" ", ""),
                                        isFavorite = service.favourite,
                                        category = service.category,
                                        isNew = shouldShowAsNew(service),
                                        stickerText = getStickerText(),
                                    )
                                }
                            }
                    } ?: emptyMap(),
                favorites = preferencesManager.getFavorites(),
            )
    }

    fun getCurrentViewItems(): List<HomeItem> =
        when {
            model.viewFavorites -> model.favorites.values.flatten()
            else -> model.categories.values.flatten()
        }

    fun updateProfilePicture(profilePicUrl: String?) {
        Log.d("HomeController", "Updating profile picture to: $profilePicUrl")

        // Invalidate the image cache first to ensure fresh loading
        ImageCache.invalidateProfileImageCache()

        // Force a model update with a new instance to trigger recomposition
        model =
            model.copy(
                profilePicture = profilePicUrl,
                // Adding a small change to any property forces recomposition
                userName = model.userName,
            )

        // Call refreshUserData after a short delay to ensure UI updates
        // This helps when we're on the home screen and need immediate refresh
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            refreshUserData()
        }, 300) // Short delay to ensure the update propagates
    }

    private fun shouldShowAsNew(service: Service): Boolean {
        val userHasntSeen = preferencesManager.isServiceNew(service.service)
        val installType = preferencesManager.getInstallType()

        // Show New sticker when backend explicitly marks service as isNew: true
        val shouldShow = service.isNew

        Log.d(
            "HomeController",
            "Service '${service.service}': installType=$installType, backendSaysNew=${service.isNew}, userHasntSeen=$userHasntSeen, shouldShow=$shouldShow",
        )
        return shouldShow
    }

    private fun getStickerText(): String {
        val installType = preferencesManager.getInstallType()
        val stickerText = "New" // Always show "New" regardless of install type
        Log.d("HomeController", "getStickerText: installType=$installType, returning '$stickerText'")
        return stickerText
    }

    private fun checkAppVersionAndMarkServices() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "unknown"
            val storedVersion = preferencesManager.getAppVersion()

            if (storedVersion.isEmpty()) {
                // First time install - clear any existing seen services and store version
                // This ensures New stickers will show for services marked as new by backend
                preferencesManager.clearSeenServices()
                preferencesManager.setAppVersion(currentVersion)
                preferencesManager.setInstallType("NEW")
                Log.d("HomeController", "First install detected, version: $currentVersion")
            } else if (storedVersion != currentVersion) {
                // App update detected - clear seen services to allow new ones to show
                preferencesManager.clearSeenServices()
                preferencesManager.setAppVersion(currentVersion)
                preferencesManager.setInstallType("UPDATED")
                Log.d("HomeController", "App update detected: $storedVersion -> $currentVersion")
            } else {
                // Returning user with same version - services may remain as seen
                Log.d("HomeController", "Returning user, version: $currentVersion")
            }
        } catch (e: Exception) {
            Log.e("HomeController", "Error checking app version", e)
        }
    }

    fun markServicesAsSeen(services: List<HomeItem>) {
        val serviceNames = services.map { it.title }
        Log.d("HomeController", "About to mark ${serviceNames.size} services as seen: $serviceNames")
        preferencesManager.markAllServicesAsSeen(serviceNames)
        Log.d("HomeController", "Marked ${serviceNames.size} services as seen")
    }

    // Call this after login completes - celebration data fetch removed
    fun onLoginCompleted() {
        Log.d("CelebrationController", "onLoginCompleted called on HomeController ${this.hashCode()}")
        // Celebration data fetch removed to avoid unnecessary API calls
    }
}
