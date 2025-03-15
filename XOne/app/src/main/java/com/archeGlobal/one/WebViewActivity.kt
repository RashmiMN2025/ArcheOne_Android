package com.archeGlobal.one

import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        val fileUrl = intent.getStringExtra("fileUrl") ?: ""
        Log.d("WebViewActivity", "Loading PDF from URL: $fileUrl")

        // Configure WebView for best PDF rendering
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // Set up WebViewClient to handle errors
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Toast.makeText(this@WebViewActivity, "Error loading PDF: $description", Toast.LENGTH_LONG).show()
                Log.e("WebViewActivity", "Error loading PDF: $description, URL: $failingUrl")
            }
        }

        webView.webChromeClient = WebChromeClient()

        try {
            // Try loading the PDF directly first
            webView.loadUrl(fileUrl)
            
            // Add a backup message in case it doesn't load
            Toast.makeText(this, "Loading PDF. Please wait...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("WebViewActivity", "Error loading PDF URL: ${e.message}")
            Toast.makeText(this, "Failed to load the document: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}