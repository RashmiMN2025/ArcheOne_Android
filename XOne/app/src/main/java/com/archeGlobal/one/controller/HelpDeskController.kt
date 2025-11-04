package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import com.archeGlobal.one.model.*
import com.archeGlobal.one.network.DynamicFormFieldsRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.network.TicketsRequest
import com.archeGlobal.one.network.TicketsResponse
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpDeskController(
    private val context: Context,
) {
    private val _model = MutableStateFlow(HelpDeskModel())
    val model: StateFlow<HelpDeskModel> = _model.asStateFlow()

    private var navigate: (String) -> Unit = {}
    private val apiService = RetrofitClient.apiService
    private val userDataManager = UserDataManager.getInstance(context)
    private var navigationSource: String? = null

    // Add navigation trigger counter for auto-refresh
    private val _navigationTrigger = MutableStateFlow(0L)
    val navigationTrigger: StateFlow<Long> = _navigationTrigger.asStateFlow()

    // Dynamic form fields state
    private val _dynamicFormFields = MutableStateFlow<List<DynamicFormField>>(emptyList())
    val dynamicFormFields: StateFlow<List<DynamicFormField>> = _dynamicFormFields.asStateFlow()

    private val _dynamicFieldsLoading = MutableStateFlow(false)
    val dynamicFieldsLoading: StateFlow<Boolean> = _dynamicFieldsLoading.asStateFlow()

    private val _dynamicFieldsError = MutableStateFlow<String?>(null)
    val dynamicFieldsError: StateFlow<String?> = _dynamicFieldsError.asStateFlow()

    fun setNavigationCallback(navCallback: (String) -> Unit) {
        navigate = navCallback
    }

    fun setNavigationSource(source: String) {
        navigationSource = source
        android.util.Log.d("HelpDeskController", "Navigation source set to: $source")
    }

    fun getNavigationSource(): String? = navigationSource

    init {
        loadFAQFromLogin()
        // Note: loadTicketsData() is called separately when user navigates to ticket tracking
    }

    private fun loadFAQFromLogin() {
        // Set loading state
        _model.value = _model.value.copy(isLoading = true, error = null)
        android.util.Log.d("HelpDeskController", "Loading FAQ data from login response only")

        // Get FAQ data from login response (stored in UserDataManager)
        val loginFaqData = userDataManager.getFAQData()
        android.util.Log.d("HelpDeskController", "Login FAQ data: ${loginFaqData?.size ?: 0} categories")

        val faqItems =
            if (loginFaqData != null && loginFaqData.isNotEmpty()) {
                android.util.Log.d("HelpDeskController", "Using login FAQ data")
                loginFaqData.toHelpDeskFAQs()
            } else {
                android.util.Log.d("HelpDeskController", "No login FAQ data, using default FAQ data")
                getDefaultFAQData()
            }

        android.util.Log.d("HelpDeskController", "Final FAQ items: ${faqItems.size}")

        _model.value =
            _model.value.copy(
                faqItems = faqItems,
                isLoading = false,
                error = null,
            )
    }

    // Legacy method - kept for reference but not used since /faq endpoint returns 404
    private fun refreshLoginAndLoadFAQ() {
        // This method is no longer used since we only rely on login FAQ data
        loadFAQFromLogin()
    }

    // Legacy methods removed - FAQ data now comes only from login response
    // The /faq API endpoint returns 404, so we rely entirely on login data

    private fun getDefaultFAQData(): List<HelpDeskFAQ> =
        listOf(
            // Hardware Issues
            HelpDeskFAQ(
                id = "laptop_not_booting",
                title = "Hardware Issues",
                question = "Laptop/Desktop not booting",
                answer = "• Check Power Supply: Ensure the power cable is securely connected to both the device and the power outlet. Try a different outlet or adapter if available. For laptops, check if the battery is charged.\n\n• Look for Indicator Lights or Sounds: Check for power or charging lights, and listen for fan noise or beeps.\n\n• Try a Hard Reset: Hold the power button for 10-15 seconds, then restart the device.\n\n• Disconnect External Devices: Remove all USB devices and accessories to eliminate hardware conflicts.\n\n• Boot into Safe Mode or BIOS: Press F2, F10, DEL, or ESC during startup to enter BIOS or Safe Mode.\n\n• Check Display Connection: Ensure the monitor is securely connected to the CPU or laptop's display output.\n\n• Use Recovery Media: Insert a bootable USB or recovery disk to troubleshoot startup issues.\n\n• Document Any Error Messages: Note down any error codes or messages for further IT support.",
                category = "Hardware",
            ),
            HelpDeskFAQ(
                id = "printer_not_working",
                title = "Hardware Issues",
                question = "Printer not working",
                answer = "• Check Power and Connections: Ensure the printer is powered on and all cables are securely connected.\n\n• Verify Network Connection: For network printers, check if they're connected to the correct network.\n\n• Update Printer Drivers: Download and install the latest drivers from the manufacturer's website.\n\n• Clear Print Queue: Cancel all pending print jobs and restart the print spooler service.\n\n• Run Printer Troubleshooter: Use the built-in Windows or Mac printer troubleshooter.\n\n• Check Ink/Toner Levels: Replace cartridges if they're low or empty.\n\n• Clean Print Heads: Use the printer's maintenance utility to clean print heads if quality is poor.",
                category = "Hardware",
            ),
            // Network & Connectivity
            HelpDeskFAQ(
                id = "vpn_connection_issue",
                title = "Network & Connectivity",
                question = "Unable to connect to VPN",
                answer = "• Check Internet Connection: Ensure your base internet connection is stable before connecting to VPN.\n\n• Verify VPN Credentials: Double-check your username, password, and server settings.\n\n• Try Different VPN Servers: Switch to a different server location if available.\n\n• Restart Network Services: Disable and re-enable your network adapter or restart your router.\n\n• Update VPN Client: Ensure you're using the latest version of your VPN software.\n\n• Check Firewall Settings: Temporarily disable firewall or add VPN client to exceptions.\n\n• Contact IT Support: If issues persist, contact your IT department for corporate VPN configuration.",
                category = "Network",
            ),
            HelpDeskFAQ(
                id = "wifi_issues",
                title = "Network & Connectivity",
                question = "Wi-Fi not working or slow",
                answer = "• Restart Your Device: Turn off Wi-Fi on your device, wait 30 seconds, then turn it back on.\n\n• Restart Router/Modem: Unplug your router for 30 seconds, then plug it back in.\n\n• Check Signal Strength: Move closer to the router or check for physical obstructions.\n\n• Forget and Reconnect: Remove the Wi-Fi network from your device and reconnect with the password.\n\n• Update Network Drivers: Ensure your device's network drivers are up to date.\n\n• Check for Interference: Move away from other electronic devices that might cause interference.\n\n• Reset Network Settings: As a last resort, reset your device's network settings to defaults.",
                category = "Network",
            ),
            HelpDeskFAQ(
                id = "network_drive_access",
                title = "Network & Connectivity",
                question = "Network drive access issues",
                answer = "• Verify Network Connection: Ensure you're connected to the corporate network or VPN.\n\n• Check Drive Mapping: Verify the network drive path and mapping in File Explorer.\n\n• Re-enter Credentials: Try disconnecting and reconnecting with your domain credentials.\n\n• Test with UNC Path: Try accessing the drive directly using \\\\server\\share format.\n\n• Clear Stored Credentials: Remove old credentials from Windows Credential Manager.\n\n• Contact IT Support: Network drives often require specific permissions from IT department.",
                category = "Network",
            ),
            HelpDeskFAQ(
                id = "internet_connectivity",
                title = "Network & Connectivity",
                question = "Internet connectivity problems",
                answer = "• Check Physical Connections: Ensure all ethernet cables are securely connected.\n\n• Restart Network Equipment: Power cycle your modem, router, and device.\n\n• Run Network Troubleshooter: Use your operating system's built-in network diagnostic tools.\n\n• Check DNS Settings: Try using public DNS servers like 8.8.8.8 or 1.1.1.1.\n\n• Disable VPN/Proxy: Temporarily disable any VPN or proxy connections.\n\n• Update Network Drivers: Ensure your network adapter drivers are current.\n\n• Contact ISP: If all else fails, contact your internet service provider for assistance.",
                category = "Network",
            ),
            // Other Issues
            HelpDeskFAQ(
                id = "raise_ticket",
                title = "Other Issues",
                question = "Other issue raise concern",
                answer = "For any other issues not covered in the FAQ, please use the 'Raise a Ticket' button to create a support ticket.",
                category = "General",
            ),
        )

    fun loadTicketsData(category: String = "Helpdesk", subCategory: String? = null) {
        // Set loading state and clear existing tickets to prevent showing old data
        _model.value =
            _model.value.copy(
                isLoading = true,
                error = null,
                tickets = emptyList(), // Clear tickets immediately to prevent flash
            )

        // Get user name from login data
        val userName = OtpVerificationController.getUserData()?.name ?: ""

        if (userName.isBlank()) {
            _model.value =
                _model.value.copy(
                    isLoading = false,
                    error = "User name not found. Please log in again.",
                )
            return
        }

        val request =
            TicketsRequest(
                name = userName,
                category = category,
                subCategory = subCategory,
            )

        Log.d("HelpDeskController", "Starting API call for tickets with category: $category, subCategory: $subCategory")
        apiService.getTickets(request).enqueue(
            object : Callback<TicketsResponse> {
                override fun onResponse(
                    call: Call<TicketsResponse>,
                    response: Response<TicketsResponse>,
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val ticketsResponse = response.body()!!
                        val supportTickets = ticketsResponse.tickets.map { it.toSupportTicket() }

                        _model.value =
                            _model.value.copy(
                                tickets = supportTickets,
                                isLoading = false,
                                error = null,
                            )
                    } else {
                        _model.value =
                            _model.value.copy(
                                isLoading = false,
                                error = "Failed to load tickets: ${response.message()}",
                            )
                    }
                }

                override fun onFailure(
                    call: Call<TicketsResponse>,
                    t: Throwable,
                ) {
                    _model.value =
                        _model.value.copy(
                            isLoading = false,
                            error = "Network error: ${t.message}",
                        )
                }
            },
        )
    }

    fun navigateToTrackTickets(category: String = "Helpdesk") {
        // Load tickets data when navigating to ticket tracking
        loadTicketsData(category)

        // Trigger navigation counter to force refresh in TicketTrackingScreen
        _navigationTrigger.value = System.currentTimeMillis()

        navigate("track_tickets")
    }

    fun navigateToFAQDetail(faqId: String) {
        navigate("faq_detail/$faqId")
    }

    fun navigateBack() {
        // Navigate back to the source screen if available, otherwise go to helpdesk
        when (navigationSource) {
            "asset" -> {
                android.util.Log.d("HelpDeskController", "Navigating back to asset screen from navigateBack")
                // Navigate to asset screen by starting AssetActivity
                val intent = android.content.Intent(context, com.archeGlobal.one.AssetActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                // Finish HomeActivity to prevent going back to it
                (context as? android.app.Activity)?.finish()
            }
            else -> {
                android.util.Log.d("HelpDeskController", "Navigating back to helpdesk screen")
                navigate("helpdesk")
            }
        }
    }

    fun navigateToHome() {
        // Navigate back to the source screen if available, otherwise go to home
        when (navigationSource) {
            "asset" -> {
                android.util.Log.d("HelpDeskController", "Navigating back to asset screen from navigateToHome")
                // Navigate to asset screen by starting AssetActivity
                val intent = android.content.Intent(context, com.archeGlobal.one.AssetActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
                // Finish HomeActivity to prevent going back to it
                (context as? android.app.Activity)?.finish()
            }
            else -> {
                android.util.Log.d("HelpDeskController", "Navigating to home screen")
                navigate("home")
            }
        }
    }

    fun getFAQById(id: String): HelpDeskFAQ? = _model.value.faqItems.find { it.id == id }

    fun raiseConcern(
        question: String,
        description: String,
    ) {
        navigate("chat")
    }

    fun raiseTicket(
        question: String,
        description: String,
    ) {
        // Get the FAQ by question to find its category
        val faq = _model.value.faqItems.find { it.question == question }
        if (faq != null) {
            val encodedTitle = java.net.URLEncoder.encode("Raise a Ticket", "UTF-8")
            val encodedCategory = java.net.URLEncoder.encode(faq.category, "UTF-8")
            val encodedSubcategory = java.net.URLEncoder.encode(faq.question, "UTF-8")
            navigate("raise_concern/$encodedTitle?category=$encodedCategory&subcategory=$encodedSubcategory")
        } else {
            // Fallback to old behavior if FAQ not found
            val encodedTitle = java.net.URLEncoder.encode("Raise a Ticket", "UTF-8")
            val encodedCategory = java.net.URLEncoder.encode(question, "UTF-8")
            navigate("raise_concern/$encodedTitle?category=$encodedCategory")
        }
    }

    fun navigateToRaiseConcern(title: String = "Raise a Concern") {
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        navigate("raise_concern/$encodedTitle")
    }

    fun refreshTickets() {
        loadTicketsData()
    }

    fun triggerNavigationRefresh() {
        // Trigger navigation counter to force refresh
        _navigationTrigger.value = System.currentTimeMillis()
        // Also refresh the tickets data
        refreshTickets()
    }

    fun refreshFAQData() {
        loadFAQFromLogin()
    }

    // Deprecated methods - kept for compatibility but no longer called automatically
    @Deprecated("Use navigateToTrackTickets() for on-demand loading")
    fun onLoginCompleted() {
        Log.d("HelpDeskController", "onLoginCompleted - deprecated, tickets now loaded only when needed")
        // Don't load tickets automatically anymore
    }

    /**
     * Mock API for testing dynamic form fields
     * TODO: Remove this and use real API when backend is ready
     */
    private fun getMockDynamicFields(
        category: String,
        subcategory: String?,
    ): List<DynamicFormField> {
        Log.d("HelpDeskController", "Using MOCK API for category: $category, subcategory: $subcategory")

        return when (category) {
            "Hardware Issues" -> {
                when (subcategory) {
                    "Laptop/Desktop not booting" ->
                        listOf(
                            DynamicFormField(
                                fieldId = "device_type",
                                fieldName = "Device Type",
                                fieldType = DynamicFieldType.DROPDOWN,
                                isRequired = true,
                                placeholder = "Select device type",
                                dropdownOptions = listOf("Laptop", "Desktop", "Workstation"),
                            ),
                            DynamicFormField(
                                fieldId = "serial_number",
                                fieldName = "Serial Number",
                                fieldType = DynamicFieldType.TEXT,
                                isRequired = true,
                                placeholder = "Enter device serial number",
                                maxLength = 50,
                                minLength = 5,
                            ),
                            DynamicFormField(
                                fieldId = "error_message",
                                fieldName = "Error Message (if any)",
                                fieldType = DynamicFieldType.LONG_TEXT,
                                isRequired = false,
                                placeholder = "Describe any error messages you see",
                                maxLength = 500,
                            ),
                            DynamicFormField(
                                fieldId = "last_working",
                                fieldName = "Last Working Date",
                                fieldType = DynamicFieldType.TEXT,
                                isRequired = false,
                                placeholder = "e.g., 2025-01-15",
                            ),
                        )
                    "Printer not working" ->
                        listOf(
                            DynamicFormField(
                                fieldId = "printer_model",
                                fieldName = "Printer Model",
                                fieldType = DynamicFieldType.TEXT,
                                isRequired = true,
                                placeholder = "Enter printer model",
                            ),
                            DynamicFormField(
                                fieldId = "printer_location",
                                fieldName = "Printer Location",
                                fieldType = DynamicFieldType.TEXT,
                                isRequired = true,
                                placeholder = "e.g., Floor 3, Room 301",
                            ),
                            DynamicFormField(
                                fieldId = "issue_type",
                                fieldName = "Issue Type",
                                fieldType = DynamicFieldType.DROPDOWN,
                                isRequired = true,
                                dropdownOptions =
                                    listOf(
                                        "Not printing",
                                        "Paper jam",
                                        "Poor print quality",
                                        "Network connection issue",
                                        "Other",
                                    ),
                            ),
                        )
                    else ->
                        listOf(
                            DynamicFormField(
                                fieldId = "asset_id",
                                fieldName = "Asset ID",
                                fieldType = DynamicFieldType.TEXT,
                                isRequired = false,
                                placeholder = "Enter asset ID if known",
                            ),
                        )
                }
            }
            "Network & Connectivity" ->
                listOf(
                    DynamicFormField(
                        fieldId = "connection_type",
                        fieldName = "Connection Type",
                        fieldType = DynamicFieldType.DROPDOWN,
                        isRequired = true,
                        dropdownOptions = listOf("WiFi", "Ethernet", "VPN", "Mobile Hotspot"),
                    ),
                    DynamicFormField(
                        fieldId = "location",
                        fieldName = "Your Location",
                        fieldType = DynamicFieldType.TEXT,
                        isRequired = true,
                        placeholder = "e.g., Office Floor 2",
                    ),
                    DynamicFormField(
                        fieldId = "when_started",
                        fieldName = "When Did This Start?",
                        fieldType = DynamicFieldType.TEXT,
                        isRequired = false,
                        placeholder = "e.g., This morning, 3 days ago",
                    ),
                    DynamicFormField(
                        fieldId = "troubleshooting_steps",
                        fieldName = "Troubleshooting Steps Tried",
                        fieldType = DynamicFieldType.LONG_TEXT,
                        isRequired = false,
                        placeholder = "Describe what you've already tried to fix the issue",
                    ),
                )
            "Other Issue" ->
                listOf(
                    DynamicFormField(
                        fieldId = "urgency",
                        fieldName = "Urgency Level",
                        fieldType = DynamicFieldType.DROPDOWN,
                        isRequired = true,
                        dropdownOptions = listOf("Low", "Medium", "High", "Critical"),
                    ),
                    DynamicFormField(
                        fieldId = "affected_users",
                        fieldName = "Number of Affected Users",
                        fieldType = DynamicFieldType.DROPDOWN,
                        isRequired = false,
                        dropdownOptions = listOf("Just me", "2-5 people", "6-10 people", "More than 10"),
                    ),
                )
            else ->
                listOf(
                    DynamicFormField(
                        fieldId = "additional_info",
                        fieldName = "Additional Information",
                        fieldType = DynamicFieldType.LONG_TEXT,
                        isRequired = false,
                        placeholder = "Any additional details that might help us resolve your issue",
                    ),
                )
        }
    }

    /**
     * Fetch dynamic form fields for a specific category and optional subcategory
     * This will be called when a category is selected in the raise ticket form
     */
    fun loadDynamicFormFields(
        category: String,
        subcategory: String? = null,
    ) {
        _dynamicFieldsLoading.value = true
        _dynamicFieldsError.value = null

        // Use mock API for now - toggle this flag when real API is ready
        val useMockApi = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (useMockApi) {
                    // Simulate network delay
                    kotlinx.coroutines.delay(800)

                    withContext(Dispatchers.Main) {
                        val mockFields = getMockDynamicFields(category, subcategory)
                        _dynamicFormFields.value = mockFields
                        _dynamicFieldsLoading.value = false
                        Log.d(
                            "HelpDeskController",
                            "Loaded ${mockFields.size} MOCK dynamic fields for category: $category, subcategory: $subcategory",
                        )
                    }
                } else {
                    // Real API call
                    val request = DynamicFormFieldsRequest(category = category, subcategory = subcategory)
                    val response = apiService.getDynamicFormFields(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val fieldsResponse = response.body()!!
                            if (fieldsResponse.status == 200) {
                                _dynamicFormFields.value = fieldsResponse.fields
                                _dynamicFieldsLoading.value = false
                                Log.d(
                                    "HelpDeskController",
                                    "Loaded ${fieldsResponse.fields.size} dynamic fields for category: $category, subcategory: $subcategory",
                                )
                            } else {
                                _dynamicFieldsError.value = fieldsResponse.message ?: "Failed to load form fields"
                                _dynamicFieldsLoading.value = false
                                Log.e("HelpDeskController", "API returned non-200 status: ${fieldsResponse.status}")
                            }
                        } else {
                            _dynamicFieldsError.value = "Failed to load form fields: ${response.message()}"
                            _dynamicFieldsLoading.value = false
                            Log.e("HelpDeskController", "API call failed: ${response.code()} - ${response.message()}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _dynamicFieldsError.value = "Error loading form fields: ${e.message}"
                    _dynamicFieldsLoading.value = false
                    Log.e("HelpDeskController", "Exception loading dynamic fields", e)
                }
            }
        }
    }

    /**
     * Clear dynamic form fields when category changes or form is reset
     */
    fun clearDynamicFormFields() {
        _dynamicFormFields.value = emptyList()
        _dynamicFieldsError.value = null
        _dynamicFieldsLoading.value = false
    }
}
