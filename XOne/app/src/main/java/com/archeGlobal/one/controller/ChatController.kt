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
    
    fun onBackPressed() {
        if (context is HomeActivity) {
            // In HomeActivity, navigate back to home
            navigator.navigateToHome()
        } else {
            // In standalone ChatActivity, just finish the activity
            (context as? ComponentActivity)?.finish()
        }
    }
} 