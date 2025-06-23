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

            // Process the message and get a response
            val response = processMessage(text)

            // Determine if we should show FAQs based on the response type
            val showFAQs = response == "I'm not sure about that. Could you please rephrase your question? If you have any issues, you can refer to the frequently asked questions below."

            // Add only one bot message
            addBotMessage(response, showFAQs = showFAQs, includeUserQuestion = true)

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
    
    private fun processMessage(text: String): String {
        // First check if it's a greeting to match iOS behavior exactly
        if (chatModel.isGreeting(text)) {
            return chatModel.getGreetingResponse()
        }

        // Search for FAQs related to the query
        val relatedFAQs = chatData.searchFAQs(text)

        if (relatedFAQs.isNotEmpty()) {
            // Always get the best match (first item) since our search now returns results sorted by relevance
            val bestMatch = relatedFAQs.first()
            
            // If there's only one match or the query is very specific (more than 3 words), show the answer directly
            if (relatedFAQs.size == 1 || text.split(Regex("\\s+")).filter { it.length > 2 }.size > 3) {
                // Return the best match with proper formatting
                return "${bestMatch.question}\n\n\n${bestMatch.answer}"
            }
            
            // If there are 2-3 related FAQs and the query is short, show the best match directly
            // but also mention there are other related questions
            if (relatedFAQs.size <= 3) {
                // Show the best match answer directly
                return "${bestMatch.question}\n\n\n${bestMatch.answer}"
            }
            
            // If there are more than 3 related FAQs, show the best match and also list other options
            val faqList = StringBuilder("${bestMatch.question}\n\n\n${bestMatch.answer}\n\n")
            faqList.append("\nRelated questions you might be interested in:\n")
            
            // Add the other related questions (skip the first one which we already displayed)
            relatedFAQs.drop(1).take(4).forEach { faq ->
                faqList.append("• ${faq.question}\n")
            }
            
            return faqList.toString()
        }

        // Default response if no FAQs match
        return "I'm not sure about that. Could you please rephrase your question? If you have any issues, you can refer to the frequently asked questions below."
    }      private fun addBotMessage(text: String, showMoreCategories: Boolean = false, showFAQs: Boolean = false, includeUserQuestion: Boolean = false) {
        // Check if user input is a single word (like "report")
        val isSingleWord = lastUserQuestion.trim().split("\\s+".toRegex()).size == 1
        
        // Determine if the text already contains a formatted FAQ question and answer
        val containsFormattedFAQ = text.contains("\n\n\n") // Check if it already has triple newline format
          
        val botMessageContent = if (includeUserQuestion && !isSingleWord && lastUserQuestion.isNotEmpty() && !containsFormattedFAQ) {
            // Only include the user question for multi-word queries when the response isn't already a formatted FAQ
            "$lastUserQuestion\n\n\n$text"
        } else {
            // For single-word queries or already formatted FAQ responses, just use the text as is
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