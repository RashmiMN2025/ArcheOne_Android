package com.example.xone.controller

import android.content.Context
import android.content.Intent
import android.net.Uri

class SOSController(private val context: Context) {

    fun makeSOSCall() {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:") // Replace with actual emergency number
        }
        context.startActivity(callIntent)
    }

    fun raiseConcern() {
        // Handle the "Raise a Concern" functionality (e.g., open a feedback form)
    }

    fun viewEmergencyContact() {
        // Handle viewing emergency contacts
    }
}
