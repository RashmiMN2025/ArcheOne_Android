package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import com.archeGlobal.one.WebViewActivity

class WebLinksController(private val context: Context) {
    companion object {
        private const val TAG = "WebLinksController"
        
        // URLs for different services
        private const val FINANCE_URL = "https://ess.azatecon.com/login"
        private const val MEDICAL_URL = "https://ilhc.icicilombard.com/Customer/iCard"
        private const val TRAVEL_URL = "https://ithsmart.travelhouseindia.in/travel/travel_web.xhtml"
        private const val SAP_URL = "https://my422539.businessbydesign.cloud.sap/sap/public/ap/ui/repository/SAP_UI/HTMLOBERON5/client.html?app.component=/SAP_UI_CT/Main/root.uiccwoc&rootWindow=X&redirectUrl=/sap/public/byd/runtime"
        private const val ZINGHR_URL = "https://portal.zinghr.com/2015/pages/authentication/zing.aspx?ccode=netcongrp"
    }

    fun openFinancePortal() {
        openWebView(FINANCE_URL, "Finance")
    }

    fun openMedicalPortal() {
        openWebView(MEDICAL_URL, "Medical")
    }

    fun openTravelPortal() {
        openWebView(TRAVEL_URL, "Travel & Expenses")
    }

    fun openSAPPortal() {
        openWebView(SAP_URL, "SAP")
    }

    fun openZingHRPortal() {
        openWebView(ZINGHR_URL, "ZingHR")
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