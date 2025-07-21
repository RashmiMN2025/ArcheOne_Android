package com.archeGlobal.one.controller

import androidx.compose.runtime.*
import com.archeGlobal.one.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelpDeskController {
    private val _model = MutableStateFlow(HelpDeskModel())
    val model: StateFlow<HelpDeskModel> = _model.asStateFlow()

    private var navigate: (String) -> Unit = {}

    fun setNavigationCallback(navCallback: (String) -> Unit) {
        navigate = navCallback
    }

    init {
        loadFAQData()
        loadTicketsData()
    }

    private fun loadFAQData() {
        val faqItems = listOf(
            HelpDeskFAQ(
                id = "faq1",
                title = "Technical Issues",
                question = "What should I do if I encounter technical issues?",
                answer = "Restart the app, clear cache, or update to the latest version. If the issue persists, contact platform@arche.global",
                category = "Technical"
            ),
            HelpDeskFAQ(
                id = "faq2",
                title = "Performance Management",
                question = "How do I access my Performance Management (PMS)?",
                answer = "Navigate to the PMS section from the main dashboard. Ensure you have the required permissions.",
                category = "Performance"
            ),
            HelpDeskFAQ(
                id = "faq3",
                title = "Payslips & Tax Forms",
                question = "How can I access my Payslips, Form16 and Form 12A?",
                answer = "You can access payslips and tax forms through the HR section. Login with your employee credentials.",
                category = "HR"
            ),
            HelpDeskFAQ(
                id = "faq4",
                title = "Emergency Contacts",
                question = "How do I access emergency contact information?",
                answer = "Emergency contacts are available in the SOS section of the app. You can also update your emergency contacts there.",
                category = "Emergency"
            ),
            HelpDeskFAQ(
                id = "faq5",
                title = "Travel Requests",
                question = "How do I raise a Travel request?",
                answer = "Go to Travel section, select your destination, dates, and submit for approval. Ensure all required fields are completed.",
                category = "Travel"
            ),
            HelpDeskFAQ(
                id = "faq6",
                title = "Anonymous Reporting",
                question = "How do I report an issue/security risk/Non Compliance anonymously?",
                answer = "Use the anonymous reporting feature in the Compliance section. All reports are handled confidentially.",
                category = "Compliance"
            ),
            HelpDeskFAQ(
                id = "faq7",
                title = "Other Issues",
                question = "Other issue raise concern",
                answer = "For any other issues not covered in the FAQ, please use the 'Raise Concern' button to create a support ticket.",
                category = "General"
            )
        )

        _model.value = _model.value.copy(faqItems = faqItems)
    }

    private fun loadTicketsData() {
        val sampleTickets = listOf(
            SupportTicket(
                id = "1",
                ticketNumber = "#58202",
                title = "Other",
                description = "Hello Team, You have received ratings from Nova O&#39;Sullivan.",
                status = TicketStatus.CLOSED,
                category = "Other",
                createdDate = "Jul 9, 2025 01:34 PM",
                details = TicketDetails(
                    platform = "Android",
                    deviceInfo = "Pixel 8a",
                    appVersion = "16",
                    rating = 5,
                    additionalNotes = "Best regards, ArcheOne Team"
                )
            ),
            SupportTicket(
                id = "2",
                ticketNumber = "#58157",
                title = "Other",
                description = "Login issue",
                status = TicketStatus.CLOSED,
                category = "Other",
                createdDate = "Jul 8, 2025 02:15 PM"
            ),
            SupportTicket(
                id = "3",
                ticketNumber = "#58156",
                title = "Other",
                description = "App crash on startup",
                status = TicketStatus.CLOSED,
                category = "Other",
                createdDate = "Jul 7, 2025 11:30 AM"
            ),
            SupportTicket(
                id = "4",
                ticketNumber = "#58155",
                title = "Other",
                description = "Cannot access travel history",
                status = TicketStatus.CLOSED,
                category = "Other",
                createdDate = "Jul 6, 2025 09:45 AM"
            ),
            SupportTicket(
                id = "5",
                ticketNumber = "#58152",
                title = "Other",
                description = "Profile picture not updating",
                status = TicketStatus.CLOSED,
                category = "Other",
                createdDate = "Jul 5, 2025 03:20 PM"
            )
        )

        _model.value = _model.value.copy(tickets = sampleTickets)
    }

    fun navigateToTrackTickets() {
        navigate("track_tickets")
    }

    fun navigateToFAQDetail(faqId: String) {
        navigate("faq_detail/$faqId")
    }

    fun navigateBack() {
        navigate("home")
    }

    fun getFAQById(id: String): HelpDeskFAQ? {
        return _model.value.faqItems.find { it.id == id }
    }

    fun raiseConcern(question: String, description: String) {
        navigate("chat")
    }
}
