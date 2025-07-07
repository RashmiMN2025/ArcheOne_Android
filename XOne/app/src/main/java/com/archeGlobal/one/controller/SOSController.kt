package com.archeGlobal.one.controller

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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

    fun makeSOSCall() {
        val context = getApplication<Application>().applicationContext
        val userData = OtpVerificationController.getUserData()
        val sosNumber = userData?.sosContact
        
        if (sosNumber.isNullOrEmpty()) {
            Toast.makeText(context, "SOS contact number not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$sosNumber")
        }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(callIntent)
    }
}
