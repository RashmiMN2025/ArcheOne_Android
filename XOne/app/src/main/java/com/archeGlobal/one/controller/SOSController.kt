package com.archeGlobal.one.controller

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archeGlobal.one.model.SosBlogModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SOSController(application: Application) : AndroidViewModel(application) {

    private val _sosBlogs = MutableStateFlow<List<SosBlogModel>>(emptyList())
    val sosBlogs: StateFlow<List<SosBlogModel>> get() = _sosBlogs

    init {
        fetchSOSBlogs()
    }

    private fun fetchSOSBlogs() {
        viewModelScope.launch {
            _sosBlogs.value = OtpVerificationController.getSosBlogsData() ?: emptyList()
        }
    }

    fun makeSOSCall(phoneNumber: String) {
        val context = getApplication<Application>().applicationContext
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(callIntent)
    }

    fun raiseConcern() {
        // Handle the "Raise a Concern" functionality (e.g., open a feedback form)
    }

    fun viewEmergencyContact() {
        try {
            // Log before creating the intent
            Log.d("SOSController", "viewEmergencyContact called")
            
            // Get application context
            val context = getApplication<Application>().applicationContext
            
            // Create intent for HomeActivity
            val intent = Intent(context, com.archeGlobal.one.HomeActivity::class.java).apply {
                // Set flags to clear other activities and start this one as a new task
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                
                // IMPORTANT: Set these extras to trigger the emergency contact view
                putExtra("navigateTo", "locations")
                putExtra("isEmergencyContact", true)
            }
            
            // Log the intent before starting activity
            Log.d("SOSController", "Starting HomeActivity with emergency contact navigation")
            
            // Start the activity
            context.startActivity(intent)
            
            Log.d("SOSController", "HomeActivity started successfully")
        } catch (e: Exception) {
            Log.e("SOSController", "Error navigating to emergency contact: ${e.message}", e)
        }
    }
}