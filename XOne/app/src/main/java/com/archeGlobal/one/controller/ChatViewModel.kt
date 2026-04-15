package com.archeGlobal.one.controller

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.FAQItem
import com.archeGlobal.one.model.Message
import com.archeGlobal.one.model.ChatbotRequest
import com.archeGlobal.one.model.ChatbotResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.ChatData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

class ChatViewModel : ViewModel() {
    private val chatData = ChatData.shared

    val messages = mutableStateListOf<Message>()
    val inputText = mutableStateOf("")
    val isTyping = mutableStateOf(false)
    private var lastUserQuestion: String = ""

    init {
        loadMessages()
    }

    fun sendMessage(text: String) {
        if (text.trim().isEmpty()) return

        // Store the user's question for inclusion in the response
        lastUserQuestion = text

        // Add user message
        val userMessage =
            Message(
                content = text,
                isUser = true,
                timestamp = Date(),
            )
        messages.add(userMessage)

        // Clear input field
        inputText.value = ""

        // Show typing indicator
        isTyping.value = true

        viewModelScope.launch {
            // Directly call the Chatbot API
            try {
                val response = RetrofitClient.apiService.getChatbotResponse(ChatbotRequest(text))
                if (response.isSuccessful && response.body() != null) {
                    val answer = response.body()?.answer ?: ""
                    if (answer.trim().isNotEmpty()) {
                        addBotMessage(answer, showFAQs = false, includeUserQuestion = false)
                    } else {
                        addBotMessage("I'm sorry, I couldn't find an answer for that.", includeUserQuestion = false)
                    }
                } else {
                    addBotMessage("Sorry, I'm having trouble responding right now. Please try again later.", includeUserQuestion = false)
                }
            } catch (e: Exception) {
                addBotMessage("An error occurred. Please check your connection.", includeUserQuestion = false)
            } finally {
                // Hide typing indicator
                isTyping.value = false
            }
        }
    }

    fun selectFAQ(question: String) {
        // First add the selected question as a user message
        lastUserQuestion = question

        val userMessage =
            Message(
                content = question,
                isUser = true,
                timestamp = Date(),
            )
        messages.add(userMessage)

        // Show typing indicator
        isTyping.value = true

        // Simulate typing delay
        viewModelScope.launch {
            delay(1000) // Simulate typing delay

            // Find the FAQ item with this question
            val faqItem = findFAQByQuestion(question)

            // Add the answer as a bot message
            if (faqItem != null) {
                addBotMessage(faqItem.answer, includeUserQuestion = true)
            } else {
                addBotMessage("I couldn't find information about that. Please try asking something else.", includeUserQuestion = true)
            }

            // Hide typing indicator
            isTyping.value = false
        }
    }

    private fun findFAQByQuestion(question: String): FAQItem? {
        // First check the main FAQs
        for (faq in chatData.faqs) {
            if (faq.question == question) {
                return faq
            }
        }

        // Then check the keyword mappings
        for (faqs in chatData.keywordMappings.values) {
            for (faq in faqs) {
                if (faq.question == question) {
                    return faq
                }
            }
        }

        return null
    }

    private fun addBotMessage(
        text: String,
        showMoreCategories: Boolean = false,
        showFAQs: Boolean = false,
        includeUserQuestion: Boolean = false,
    ) {
        // Check if user input is a single word (like "report")
        val isSingleWord = lastUserQuestion.trim().split("\\s+".toRegex()).size == 1

        // Determine if the text already contains a formatted FAQ question and answer
        val containsFormattedFAQ = text.contains("\n\n\n") // Check if it already has triple newline format

        // Don't include user question if we want to show FAQs (to ensure FAQ categories display properly)
        val shouldIncludeUserQuestion =
            includeUserQuestion && !showFAQs && !isSingleWord && lastUserQuestion.isNotEmpty() && !containsFormattedFAQ

        val botMessageContent =
            if (shouldIncludeUserQuestion) {
                // Only include the user question for multi-word queries when the response isn't already a formatted FAQ and we're not showing FAQs
                "$lastUserQuestion\n\n\n$text"
            } else {
                // For single-word queries, FAQ responses, or when showing FAQs, just use the text as is
                text
            }

        val botMessage =
            Message(
                content = botMessageContent,
                isUser = false,
                timestamp = Date(),
                showMoreCategories = showMoreCategories,
                showFAQs = showFAQs,
            )
        messages.add(botMessage)
    }

    fun refreshChat() {
        messages.clear() // Clear old messages
        loadMessages() // Fetch fresh messages
    }

    fun loadMessages() {
        addBotMessage(
            "👋 Welcome to ArcheOne Assistant!\n\nI'm your personal support guide, ready to help you navigate through ArcheOne's features and services.",
        )

        // Add support categories message
        addBotMessage("Here's what I can help you with:\nFeel free to ask any questions!", showFAQs = true)
    }

    fun loadMoreFAQs(messageId: String) {
        val messageToUpdate = messages.find { it.id == messageId }
        if (messageToUpdate != null) {
            val index = messages.indexOf(messageToUpdate)
            if (index >= 0) {
                val updatedMessage = messageToUpdate.copy(showMoreCategories = true)
                messages[index] = updatedMessage // Update the message in the list
            }
        }
    }

    /**
     * Clears the chat history when exiting the chat screen
     * This ensures a fresh chat experience when the user returns
     */
    fun clearChatHistory() {
        messages.clear()
        inputText.value = ""
        isTyping.value = false
        lastUserQuestion = ""
    }
}
