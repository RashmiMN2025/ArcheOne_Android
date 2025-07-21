package com.archeGlobal.one.model

data class SupportTicket(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val category: String,
    val createdDate: String,
    val lastUpdate: String? = null,
    val details: TicketDetails? = null
)

data class TicketDetails(
    val platform: String,
    val deviceInfo: String,
    val appVersion: String,
    val rating: Int? = null,
    val additionalNotes: String? = null
)

enum class TicketStatus {
    OPEN, IN_PROGRESS, CLOSED, PENDING
}

data class HelpDeskFAQ(
    val id: String,
    val title: String,
    val question: String,
    val answer: String,
    val category: String = "General"
)

data class HelpDeskModel(
    val faqItems: List<HelpDeskFAQ> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
