package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import com.archeGlobal.one.WebViewActivity

class WebLinksController(private val context: Context) {
    companion object {
        private const val TAG = "WebLinksController"
    }

    fun openFinancePortal(url: String) {
        openWebView(url, "Finance")
    }

    fun openMedicalPortal(url: String) {
        openWebView(url, "Medical")
    }

    fun openTravelPortal(url: String) {
        openWebView(url, "Travel & Expenses")
    }

    fun openSAPPortal(url: String) {
        openWebView(url, "SAP")
    }

    fun openZingHRPortal(url: String) {
        openWebView(url, "ZingHR")
    }

    private fun openWebView(url: String, title: String) {
        try {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra("fileUrl", url)
                putExtra("title", title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "Opening $title portal in WebView: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening $title portal: ${e.message}")
        }
    }
}
