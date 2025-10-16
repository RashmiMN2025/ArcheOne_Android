package com.archeGlobal.one.model

import java.util.Date
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Date = Date(),
    val showMoreCategories: Boolean = false,
    val showFAQs: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (content != other.content) return false
        if (isUser != other.isUser) return false
        if (timestamp != other.timestamp) return false
        if (showMoreCategories != other.showMoreCategories) return false
        if (showFAQs != other.showFAQs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + isUser.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + showMoreCategories.hashCode()
        result = 31 * result + showFAQs.hashCode()
        return result
    }
}

data class FAQItem(
    val title: String,
    val question: String,
    val answer: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FAQItem

        if (question != other.question) return false
        if (answer != other.answer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = question.hashCode()
        result = 31 * result + answer.hashCode()
        return result
    }
}
