package com.archeGlobal.one.utils

class ChatModel private constructor() {
    companion object {
        val shared = ChatModel()
        
        private val greetings = listOf(
            "hi", "hello", "hey", "good morning", "good afternoon", "good evening",
            "how are you", "what's up", "whats up", "sup", "greetings"
        )
        
        private val responses = listOf(
            "Hello! How can I help you today?",
            "Hi there! What can I do for you?",
            "Hey! How may I assist you?",
            "Good to see you! How can I help?",
            "Hello! Is there something I can help you with?"
        )
    }
    
    fun isGreeting(message: String): Boolean {
        val lowercasedMessage = message.lowercase().trim()
        return greetings.any { greeting ->
            lowercasedMessage.contains(greeting)
        }
    }
    
    fun getGreetingResponse(): String {
        return responses.random()
    }
} 