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
    
    init {
        // Add welcome message with waving hand emoji
        addBotMessage("👋 Welcome to ArcheOne Assistant!")
        
        // Add support categories message
        addBotMessage("Here's what I can help you with:\nFeel free to ask any questions!", showFAQs = true)
    }
    
    fun sendMessage(text: String) {
        if (text.trim().isEmpty()) return
        
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
            // Only show FAQs with default "I'm not sure" response
            val showFAQs = response == "I'm not sure about that. Could you please rephrase your question? If you have any issues, you can refer to the frequently asked questions below."
            
            addBotMessage(response, showFAQs = showFAQs)
            
            // Hide typing indicator
            isTyping.value = false
        }
    }
    
    fun selectFAQ(question: String) {
        // First add the selected question as a user message
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
                addBotMessage(faqItem.answer)
            } else {
                addBotMessage("I couldn't find information about that. Please try asking something else.")
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
            // Check if there's an exact match where the question equals the input text
            val exactMatch = relatedFAQs.find { it.question.equals(text, ignoreCase = true) }
            
            if (exactMatch != null) {
                // If there's an exact match, return the answer directly
                return exactMatch.answer
            }
            
            // Format multiple matches as bullet points (• prefix)
            val faqList = StringBuilder("I found multiple relevant questions. Please select one to see its answer:\n\n")
            
            // Take at most 5 FAQs to avoid overcrowding, exactly like iOS
            relatedFAQs.take(5).forEach { faq ->
                faqList.append("• ${faq.question}\n")
            }
            
            return faqList.toString()
        }
        
        // Default response if no FAQs match - will show FAQs inline
        // This matches the iOS version's fallback response exactly
        return "I'm not sure about that. Could you please rephrase your question? If you have any issues, you can refer to the frequently asked questions below."
    }
    
    private fun addBotMessage(text: String, showMoreCategories: Boolean = false, showFAQs: Boolean = false) {
        val botMessage = Message(
            content = text,
            isUser = false,
            timestamp = Date(),
            showMoreCategories = showMoreCategories,
            showFAQs = showFAQs
        )
        messages.add(botMessage)
    }
    
    fun loadMoreFAQs() {
        // Find the message with FAQs and update it to show more
        val messagesToUpdate = messages.filter { !it.isUser && it.showFAQs }
        
        if (messagesToUpdate.isNotEmpty()) {
            // Get the first FAQ message (usually the categories section)
            val messageToUpdate = messagesToUpdate.first()
            val index = messages.indexOf(messageToUpdate)
            
            if (index >= 0) {
                // Create a new message with showMoreCategories set to true
                val updatedMessage = messageToUpdate.copy(showMoreCategories = true)
                
                // Important: Use set() to ensure state is properly updated for Compose
                messages[index] = updatedMessage
            }
        }
    }
} 