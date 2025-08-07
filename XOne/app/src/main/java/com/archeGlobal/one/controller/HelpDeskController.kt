package com.archeGlobal.one.controller

import android.content.Context
import androidx.compose.runtime.*
import com.archeGlobal.one.model.*
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.network.TicketsRequest
import com.archeGlobal.one.network.TicketsResponse
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpDeskController(private val context: Context) {
    private val _model = MutableStateFlow(HelpDeskModel())
    val model: StateFlow<HelpDeskModel> = _model.asStateFlow()

    private var navigate: (String) -> Unit = {}
    private val apiService = RetrofitClient.apiService
    private val userDataManager = UserDataManager.getInstance(context)
    private var navigationSource: String? = null
    
    // Add navigation trigger counter for auto-refresh
    private val _navigationTrigger = MutableStateFlow(0L)
    val navigationTrigger: StateFlow<Long> = _navigationTrigger.asStateFlow()

    fun setNavigationCallback(navCallback: (String) -> Unit) {
        navigate = navCallback
    }

    fun setNavigationSource(source: String) {
        navigationSource = source
        android.util.Log.d("HelpDeskController", "Navigation source set to: $source")
    }

    fun getNavigationSource(): String? {
        return navigationSource
    }

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

        val faqItems = if (loginFaqData != null && loginFaqData.isNotEmpty()) {
            android.util.Log.d("HelpDeskController", "Using login FAQ data")
            loginFaqData.toHelpDeskFAQs()
        } else {
            android.util.Log.d("HelpDeskController", "No login FAQ data, using default FAQ data")
            getDefaultFAQData()
        }

        android.util.Log.d("HelpDeskController", "Final FAQ items: ${faqItems.size}")

        _model.value = _model.value.copy(
            faqItems = faqItems,
            isLoading = false,
            error = null
        )
    }

    // Legacy method - kept for reference but not used since /faq endpoint returns 404
    private fun refreshLoginAndLoadFAQ() {
        // This method is no longer used since we only rely on login FAQ data
        loadFAQFromLogin()
    }

    // Legacy methods removed - FAQ data now comes only from login response
    // The /faq API endpoint returns 404, so we rely entirely on login data

    private fun getDefaultFAQData(): List<HelpDeskFAQ> {
        return listOf(
            // Hardware Issues
            HelpDeskFAQ(
                id = "laptop_not_booting",
                title = "Hardware Issues",
                question = "Laptop/Desktop not booting",
                answer = "• Check Power Supply: Ensure the power cable is securely connected to both the device and the power outlet. Try a different outlet or adapter if available. For laptops, check if the battery is charged.\n\n• Look for Indicator Lights or Sounds: Check for power or charging lights, and listen for fan noise or beeps.\n\n• Try a Hard Reset: Hold the power button for 10-15 seconds, then restart the device.\n\n• Disconnect External Devices: Remove all USB devices and accessories to eliminate hardware conflicts.\n\n• Boot into Safe Mode or BIOS: Press F2, F10, DEL, or ESC during startup to enter BIOS or Safe Mode.\n\n• Check Display Connection: Ensure the monitor is securely connected to the CPU or laptop's display output.\n\n• Use Recovery Media: Insert a bootable USB or recovery disk to troubleshoot startup issues.\n\n• Document Any Error Messages: Note down any error codes or messages for further IT support.",
                category = "Hardware"
            ),
            HelpDeskFAQ(
                id = "printer_not_working",
                title = "Hardware Issues",
                question = "Printer not working",
                answer = "• Check Power and Connections: Ensure the printer is powered on and all cables are securely connected.\n\n• Verify Network Connection: For network printers, check if they're connected to the correct network.\n\n• Update Printer Drivers: Download and install the latest drivers from the manufacturer's website.\n\n• Clear Print Queue: Cancel all pending print jobs and restart the print spooler service.\n\n• Run Printer Troubleshooter: Use the built-in Windows or Mac printer troubleshooter.\n\n• Check Ink/Toner Levels: Replace cartridges if they're low or empty.\n\n• Clean Print Heads: Use the printer's maintenance utility to clean print heads if quality is poor.",
                category = "Hardware"
            ),
            // Network & Connectivity
            HelpDeskFAQ(
                id = "vpn_connection_issue",
                title = "Network & Connectivity",
                question = "Unable to connect to VPN",
                answer = "• Check Internet Connection: Ensure your base internet connection is stable before connecting to VPN.\n\n• Verify VPN Credentials: Double-check your username, password, and server settings.\n\n• Try Different VPN Servers: Switch to a different server location if available.\n\n• Restart Network Services: Disable and re-enable your network adapter or restart your router.\n\n• Update VPN Client: Ensure you're using the latest version of your VPN software.\n\n• Check Firewall Settings: Temporarily disable firewall or add VPN client to exceptions.\n\n• Contact IT Support: If issues persist, contact your IT department for corporate VPN configuration.",
                category = "Network"
            ),
            HelpDeskFAQ(
                id = "wifi_issues",
                title = "Network & Connectivity",
                question = "Wi-Fi not working or slow",
                answer = "• Restart Your Device: Turn off Wi-Fi on your device, wait 30 seconds, then turn it back on.\n\n• Restart Router/Modem: Unplug your router for 30 seconds, then plug it back in.\n\n• Check Signal Strength: Move closer to the router or check for physical obstructions.\n\n• Forget and Reconnect: Remove the Wi-Fi network from your device and reconnect with the password.\n\n• Update Network Drivers: Ensure your device's network drivers are up to date.\n\n• Check for Interference: Move away from other electronic devices that might cause interference.\n\n• Reset Network Settings: As a last resort, reset your device's network settings to defaults.",
                category = "Network"
            ),
            HelpDeskFAQ(
                id = "network_drive_access",
                title = "Network & Connectivity",
                question = "Network drive access issues",
                answer = "• Verify Network Connection: Ensure you're connected to the corporate network or VPN.\n\n• Check Drive Mapping: Verify the network drive path and mapping in File Explorer.\n\n• Re-enter Credentials: Try disconnecting and reconnecting with your domain credentials.\n\n• Test with UNC Path: Try accessing the drive directly using \\\\server\\share format.\n\n• Clear Stored Credentials: Remove old credentials from Windows Credential Manager.\n\n• Contact IT Support: Network drives often require specific permissions from IT department.",
                category = "Network"
            ),
            HelpDeskFAQ(
                id = "internet_connectivity",
                title = "Network & Connectivity",
                question = "Internet connectivity problems",
                answer = "• Check Physical Connections: Ensure all ethernet cables are securely connected.\n\n• Restart Network Equipment: Power cycle your modem, router, and device.\n\n• Run Network Troubleshooter: Use your operating system's built-in network diagnostic tools.\n\n• Check DNS Settings: Try using public DNS servers like 8.8.8.8 or 1.1.1.1.\n\n• Disable VPN/Proxy: Temporarily disable any VPN or proxy connections.\n\n• Update Network Drivers: Ensure your network adapter drivers are current.\n\n• Contact ISP: If all else fails, contact your internet service provider for assistance.",
                category = "Network"
            ),
            // Other Issues
            HelpDeskFAQ(
                id = "raise_ticket",
                title = "Other Issues",
                question = "Other issue raise concern",
                answer = "For any other issues not covered in the FAQ, please use the 'Raise a Ticket' button to create a support ticket.",
                category = "General"
            )
        )
    }

    fun loadTicketsData(category: String = "Helpdesk") {
        // Set loading state and clear existing tickets to prevent showing old data
        _model.value = _model.value.copy(
            isLoading = true,
            error = null,
            tickets = emptyList() // Clear tickets immediately to prevent flash
        )

        // Get user name from login data
        val userName = OtpVerificationController.getUserData()?.name ?: ""

        if (userName.isBlank()) {
            _model.value = _model.value.copy(
                isLoading = false,
                error = "User name not found. Please log in again."
            )
            return
        }

        val request = TicketsRequest(
            name = userName,
            category = category,
            subcategory = null // Currently not filtering by subcategory when loading tickets
        )

        apiService.getTickets(request).enqueue(object : Callback<TicketsResponse> {
            override fun onResponse(call: Call<TicketsResponse>, response: Response<TicketsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val ticketsResponse = response.body()!!
                    val supportTickets = ticketsResponse.tickets.map { it.toSupportTicket() }

                    _model.value = _model.value.copy(
                        tickets = supportTickets,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _model.value = _model.value.copy(
                        isLoading = false,
                        error = "Failed to load tickets: ${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<TicketsResponse>, t: Throwable) {
                _model.value = _model.value.copy(
                    isLoading = false,
                    error = "Network error: ${t.message}"
                )
            }
        })
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

    fun getFAQById(id: String): HelpDeskFAQ? {
        return _model.value.faqItems.find { it.id == id }
    }

    fun raiseConcern(question: String, description: String) {
        navigate("chat")
    }

    fun raiseTicket(question: String, description: String) {
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
}
