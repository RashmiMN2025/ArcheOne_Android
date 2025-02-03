package com.example.xone.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidNavigator(private val context: Context) : Navigator {
    override fun openPulseLogin() {
        val pulseUrl = "https://pulse.netcon.in/login"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pulseUrl))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
} 