package com.archeGlobal.one.controller

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.FAQItem
import com.archeGlobal.one.model.Message
import com.archeGlobal.one.utils.ChatData
import com.archeGlobal.one.utils.ChatModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

class ChatViewModel : ViewModel() {
    private val chatData = ChatData.shared
    private val chatModel = ChatModel.shared

    val messages = mutableStateListOf<Message>()
    val inputText = mutableStateOf("")
    val isTyping = mutableStateOf(false)
    private var lastUserQuestion: String = ""

    // Define the FAQ message as a constant to ensure exact matching
    private val FAQ_MESSAGE = "I'm not sure about that. Could you please rephrase your question? If you have any issues, you can refer to the frequently asked questions below."

    init {
        loadMessages()
    }

    fun sendMessage(text: String) {
        if (text.trim().isEmpty()) return

        // Store the user's question for inclusion in the response
        lastUserQuestion = text

        // Add user message
        val userMessage = Message(
            content = text,
            isUser = true,
            timestamp = Date()
        )
        messages.add(userMessage)

        // Clear input field
        inputText.value = ""

        // Show typing indicator
        isTyping.value = true

        // Simulate typing delay
        viewModelScope.launch {
            delay(1000) // Simulate typing delay

            // Process the message and get a response with FAQ flag
            val (response, shouldShowFAQs) = processMessageWithFAQFlag(text)

            if (shouldShowFAQs) {
                // Send two separate messages: one for text, one for FAQ categories
                addBotMessage(response, showFAQs = false, includeUserQuestion = false)
                addBotMessage("", showFAQs = true, includeUserQuestion = false)
            } else {
                // Send single message as before
                addBotMessage(response, showFAQs = false, includeUserQuestion = true)
            }

            // Hide typing indicator
            isTyping.value = false
        }
    }

    fun selectFAQ(question: String) {
        // First add the selected question as a user message
        lastUserQuestion = question

        val userMessage = Message(
            content = question,
            isUser = true,
            timestamp = Date()
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

    private fun processMessageWithFAQFlag(text: String): Pair<String, Boolean> {
        // First check if it's a greeting to match iOS behavior exactly
        if (chatModel.isGreeting(text)) {
            return Pair(chatModel.getGreetingResponse(), false)
        }

        // Count words in the query
        val queryWords = text.lowercase().split(Regex("\\s+")).filter { it.length > 2 }

        // If query has 2-4 words, treat it like random text (skip all FAQ logic)
        if (queryWords.size in 2..4) {
            return Pair(FAQ_MESSAGE, true)
        }

        // Search for FAQs related to the query
        val relatedFAQs = chatData.searchFAQs(text)

        if (relatedFAQs.isNotEmpty()) {
            // Check if there's an exact match where the question equals the input text
            val exactMatch = relatedFAQs.find { it.question.equals(text, ignoreCase = true) }

            if (exactMatch != null) {
                // If there's an exact match, return the answer directly
                return Pair(exactMatch.answer, false)
            }

            // Check if we have 5+ words matching overall
            val overallMatchingWords = relatedFAQs.maxOfOrNull { faq ->
                val questionWords = faq.question.lowercase().split(Regex("\\s+")).filter { it.length > 2 }
                var matchingWordsCount = 0
                queryWords.forEach { queryWord ->
                    questionWords.forEach { questionWord ->
                        if (questionWord == queryWord || questionWord.contains(queryWord) || queryWord.contains(questionWord)) {
                            matchingWordsCount++
                        }
                    }
                }
                matchingWordsCount
            } ?: 0

            // Check for high-quality matches (when 5+ words match)
            val highQualityMatches = relatedFAQs.filter { faq ->
                val questionWords = faq.question.lowercase().split(Regex("\\s+")).filter { it.length > 2 }

                var matchingWordsCount = 0
                queryWords.forEach { queryWord ->
                    questionWords.forEach { questionWord ->
                        if (questionWord == queryWord || questionWord.contains(queryWord) || queryWord.contains(questionWord)) {
                            matchingWordsCount++
                        }
                    }
                }
                matchingWordsCount >= 5
            }

            // If we have exactly 1 word in the query AND it matches, keep current logic
            if (queryWords.size == 1 && overallMatchingWords >= 1) {
                // Original logic for single word queries
                if (relatedFAQs.size > 2) {
                    val faqList = StringBuilder("I found multiple relevant questions. Please select one to see its answer:\n")
                    relatedFAQs.take(5).forEach { faq ->
                        faqList.append("• ${faq.question}\n")
                    }
                    return Pair(faqList.toString(), false)
                }
                val response = relatedFAQs.joinToString("\n\n") { faq ->
                    "${faq.question}\n\n\n${faq.answer}"
                }
                return Pair(response, false)
            }

            // If we have 5+ words matching, keep current logic
            if (highQualityMatches.isNotEmpty()) {
                if (highQualityMatches.size == 1) {
                    // If there's only one high-quality match, show question and answer
                    val faq = highQualityMatches.first()
                    return Pair("${faq.question}\n\n\n${faq.answer}", false)
                } else {
                    // If there are multiple high-quality matches, show them as options
                    val faqList = StringBuilder("I found multiple relevant questions. Please select one to see its answer:\n")
                    highQualityMatches.take(5).forEach { faq ->
                        faqList.append("• ${faq.question}\n")
                    }
                    return Pair(faqList.toString(), false)
                }
            }

            // If there are more than 2 related FAQs (but no high-quality matches), show the "multiple relevant questions" message
            if (relatedFAQs.size > 2) {
                val faqList = StringBuilder("I found multiple relevant questions. Please select one to see its answer:\n")

                // Take at most 5 FAQs to avoid overcrowding
                relatedFAQs.take(5).forEach { faq ->
                    faqList.append("• ${faq.question}\n")
                }

                return Pair(faqList.toString(), false)
            }

            // If there is exactly 1 or 2 related FAQs, show the question(s) with their answer(s)
            // Add proper spacing (two blank lines) between question and answer
            val response = relatedFAQs.joinToString("\n\n") { faq ->
                "${faq.question}\n\n\n${faq.answer}"
            }
            return Pair(response, false)
        }

        // Default response for no FAQs match (same as random text)
        return Pair(FAQ_MESSAGE, true)
    }

    private fun addBotMessage(text: String, showMoreCategories: Boolean = false, showFAQs: Boolean = false, includeUserQuestion: Boolean = false) {
        // Check if user input is a single word (like "report")
        val isSingleWord = lastUserQuestion.trim().split("\\s+".toRegex()).size == 1

        // Determine if the text already contains a formatted FAQ question and answer
        val containsFormattedFAQ = text.contains("\n\n\n") // Check if it already has triple newline format

        // Don't include user question if we want to show FAQs (to ensure FAQ categories display properly)
        val shouldIncludeUserQuestion = includeUserQuestion && !showFAQs && !isSingleWord && lastUserQuestion.isNotEmpty() && !containsFormattedFAQ

        val botMessageContent = if (shouldIncludeUserQuestion) {
            // Only include the user question for multi-word queries when the response isn't already a formatted FAQ and we're not showing FAQs
            "$lastUserQuestion\n\n\n$text"
        } else {
            // For single-word queries, FAQ responses, or when showing FAQs, just use the text as is
            text
        }

        val botMessage = Message(
            content = botMessageContent,
            isUser = false,
            timestamp = Date(),
            showMoreCategories = showMoreCategories,
            showFAQs = showFAQs
        )
        messages.add(botMessage)
    }

    fun refreshChat() {
        messages.clear() // Clear old messages
        loadMessages() // Fetch fresh messages
    }

    fun loadMessages() {
        addBotMessage("👋 Welcome to ArcheOne Assistant!\n\nI'm your personal support guide, ready to help you navigate through ArcheOne's features and services.")

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
