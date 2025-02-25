package com.example.xone

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        val fileUrl = intent.getStringExtra("fileUrl") ?: ""

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        if (fileUrl.endsWith(".pdf")) {
            // Open PDF using Google Docs Viewer if needed
            val pdfUrl = "https://docs.google.com/gview?embedded=true&url=$fileUrl"
            webView.loadUrl(pdfUrl)
        } else {
            webView.loadUrl(fileUrl)
        }
    }
}
