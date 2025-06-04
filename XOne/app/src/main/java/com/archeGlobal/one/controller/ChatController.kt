package com.archeGlobal.one.controller

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.archeGlobal.one.HomeActivity
import com.archeGlobal.one.navigation.Navigator

class ChatController(
    private val context: Context,
    private val navigator: Navigator
) {
    val viewModel: ChatViewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(ChatViewModel::class.java)
    }
    
    // Flag to track if we need to initialize a fresh chat next time
    private var shouldResetChat = false
    
    fun onBackPressed() {
        // Mark that we should reset chat next time
        shouldResetChat = true
        
        // Clear the chat history before navigating away
        viewModel.clearChatHistory()
        
        if (context is HomeActivity) {
            // In HomeActivity, navigate back to home
            navigator.navigateToHome()
        } else {
            // In standalone ChatActivity, just finish the activity
            (context as? ComponentActivity)?.finish()
        }
    }
    
    /**
     * Clears the chat history and sets the reset flag
     * Used when navigating to chat from other screens
     */
    fun clearChatHistory() {
        shouldResetChat = true
        viewModel.clearChatHistory()
    }
    
    /**
     * Called when entering the chat screen to check if we need to reset
     */
    fun onChatScreenEnter() {
        if (shouldResetChat) {
            // Reset flag
            shouldResetChat = false
            // Initialize fresh chat
            viewModel.loadMessages()
        }
    }
} 