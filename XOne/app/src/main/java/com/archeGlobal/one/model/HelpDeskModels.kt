package com.archeGlobal.one.model

import com.archeGlobal.one.network.FAQCategory
import com.archeGlobal.one.network.TicketItem

data class SupportTicket(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val category: String,
    val subCategory: String? = null,
    val createdDate: String,
    val closureComments: String? = null,
    val resolvedTime: String? = null,
    val lastUpdate: String? = null,
    val details: TicketDetails? = null,
)

data class TicketDetails(
    val platform: String,
    val deviceInfo: String,
    val appVersion: String,
    val rating: Int? = null,
    val additionalNotes: String? = null,
)

enum class TicketStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED,
    PENDING,
    ONHOLD
}

data class HelpDeskFAQ(
    val id: String,
    val title: String,
    val question: String,
    val answer: String,
    val category: String = "General",
    val dynamicFields: Boolean = false,
)

data class HelpDeskModel(
    val faqItems: List<HelpDeskFAQ> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// Extension function to convert API TicketItem to SupportTicket
fun TicketItem.toSupportTicket(): SupportTicket {
    val ticketStatus =
        when (status.uppercase()) {
            "OPEN" -> TicketStatus.OPEN
            "IN_PROGRESS", "IN PROGRESS" -> TicketStatus.IN_PROGRESS
            "CLOSED" -> TicketStatus.CLOSED
            "PENDING" -> TicketStatus.PENDING
            "ONHOLD" -> TicketStatus.ONHOLD
            else -> TicketStatus.CLOSED // Default to closed for unknown statuses
        }

    return SupportTicket(
        id = id,
        ticketNumber = "#$id",
        title = subject ?: "Support Ticket #$id", // Use default title if subject is null
        description = description,
        status = ticketStatus,
        category = category ?: "Helpdesk", // Use API category or default to Helpdesk
        subCategory = subcategory,
        createdDate = created_time,
        closureComments = closure_comments,
        resolvedTime = resolved_time,
        lastUpdate = null,
        details = null,
    )
}

// Dynamic Form Field Models
enum class DynamicFieldType {
    TEXT,
    DROPDOWN,
    LONG_TEXT,
}

data class DynamicFormField(
    val fieldId: String,
    val fieldName: String,
    val fieldType: DynamicFieldType,
    val isRequired: Boolean = false,
    val placeholder: String? = null,
    val dropdownOptions: List<String>? = null, // Only for DROPDOWN type
    val maxLength: Int? = null,
    val minLength: Int? = null,
    val validationRegex: String? = null,
    val errorMessage: String? = null,
    val displayOrder: Int = 0, // Field to control display order
)

data class DynamicFormFieldsResponse(
    val status: Int,
    val message: String? = null,
    val fields: List<DynamicFormField>,
)

data class DynamicFormFieldValue(
    val fieldId: String,
    val value: String,
)

data class DynamicTicketSubmissionRequest(
    val name: String,
    val email: String,
    val mobile: String,
    val category: String,
    val subcategory: String? = null,
    val query: String, // Main description field
    val dynamicFields: List<DynamicFormFieldValue>, // Dynamic field values
    val anonymous: Boolean = false,
)

// Response model for dynamic form submission
data class DynamicFormSubmissionResponse(
    val status: Int,
    val message: String,
    val ticketId: String? = null,
)

// Extension function to convert API FAQ structure to HelpDeskFAQ list
fun List<FAQCategory>.toHelpDeskFAQs(): List<HelpDeskFAQ> {
    val faqs = mutableListOf<HelpDeskFAQ>()

    this.forEachIndexed { categoryIndex, category ->
        category.items.forEachIndexed { itemIndex, item ->
            // Combine all answer descriptions into a single formatted answer
            val combinedAnswer =
                item.answer.joinToString("\n\n") { answer ->
                    if (answer.cat.isNotBlank()) {
                        "• ${answer.cat}: ${answer.des}"
                    } else {
                        "• ${answer.des}"
                    }
                }

            // Use ONLY the API flag to determine if FAQ has dynamic fields
            // No client-side pattern matching - backend is the single source of truth
            val hasDynamicFields = item.dynamicFields

            // Log FAQ processing for debugging
            android.util.Log.d(
                "HelpDeskFAQ",
                "FAQ: '${item.question}' | isDynamic: ${item.dynamicFields}",
            )

            faqs.add(
                HelpDeskFAQ(
                    id = "${categoryIndex}_$itemIndex",
                    title = category.title,
                    question = item.question,
                    answer = combinedAnswer,
                    category = category.title,
                    dynamicFields = hasDynamicFields,
                ),
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
            category = "General",
            dynamicFields = true, // Enable dynamic fields for other issues
        ),
    )

    return faqs
}
