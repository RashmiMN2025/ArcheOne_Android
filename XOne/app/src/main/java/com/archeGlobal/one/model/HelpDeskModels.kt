package com.archeGlobal.one.model

import com.archeGlobal.one.network.TicketItem
import com.archeGlobal.one.network.FAQCategory
import com.archeGlobal.one.network.FAQItem

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

// Extension function to convert API TicketItem to SupportTicket
fun TicketItem.toSupportTicket(): SupportTicket {
    val ticketStatus = when (status.uppercase()) {
        "OPEN" -> TicketStatus.OPEN
        "IN_PROGRESS", "IN PROGRESS" -> TicketStatus.IN_PROGRESS
        "CLOSED" -> TicketStatus.CLOSED
        "PENDING" -> TicketStatus.PENDING
        else -> TicketStatus.CLOSED // Default to closed for unknown statuses
    }

    return SupportTicket(
        id = id,
        ticketNumber = "#$id",
        title = subject,
        description = description,
        status = ticketStatus,
        category = "Helpdesk", // Default category as per API request
        createdDate = created_time,
        lastUpdate = null,
        details = null
    )
}

// Extension function to convert API FAQ structure to HelpDeskFAQ list
fun List<FAQCategory>.toHelpDeskFAQs(): List<HelpDeskFAQ> {
    val faqs = mutableListOf<HelpDeskFAQ>()
    
    this.forEachIndexed { categoryIndex, category ->
        category.items.forEachIndexed { itemIndex, item ->
            // Combine all answer descriptions into a single formatted answer
            val combinedAnswer = item.answer.joinToString("\n\n") { answer ->
                if (answer.cat.isNotBlank()) {
                    "• ${answer.cat}: ${answer.des}"
                } else {
                    "• ${answer.des}"
                }
            }
            
            faqs.add(
                HelpDeskFAQ(
                    id = "${categoryIndex}_${itemIndex}",
                    title = category.title,
                    question = item.question,
                    answer = combinedAnswer,
                    category = category.title
                )
            )
        }
    }
    
    // Add the default "Other issue" FAQ for ticket raising
    faqs.add(
        HelpDeskFAQ(
            id = "raise_ticket",
            title = "Other Issues",
            question = "Other issue raise concern",
            answer = "For any other issues not covered in the FAQ, please use the 'Raise a Ticket' button to create a support ticket.",
            category = "General"
        )
    )
    
    return faqs
}
