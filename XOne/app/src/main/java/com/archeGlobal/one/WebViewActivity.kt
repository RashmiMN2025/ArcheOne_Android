package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class WebViewActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileUrl = intent.getStringExtra("fileUrl") ?: ""
        val fileName = fileUrl.substringAfterLast("/").substringBeforeLast(".")
        val title = intent.getStringExtra("title") ?: "Floor Map"
        
        // Determine if the URL is for a PDF or a website
        val isPdf = fileUrl.endsWith(".pdf", ignoreCase = true)
        
        Log.d("WebViewActivity", "Loading URL: $fileUrl, isPdf: $isPdf")

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { 
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.Black
                                    )
                                }
                            },
                            actions = {
                                // Share button
                                IconButton(onClick = { 
                                    // Create share intent
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, title)
                                        putExtra(Intent.EXTRA_TEXT, "Check out this $title: $fileUrl")
                                    }
                                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                                    Log.d("WebViewActivity", "Sharing URL: $fileUrl")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.Black
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(Color.White)
                    ) {
                        // Loading indicator
                        var isLoading by remember { mutableStateOf(true) }
                        
                        // Show progress indicator while loading
                        if (isLoading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFDD3825)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isPdf) "Loading PDF..." else "Loading content...",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                        
                        // WebView for content display
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                WebView(context).apply {
                                    settings.apply {
                                        javaScriptEnabled = true
                                        allowFileAccess = true
                                        domStorageEnabled = true
                                        loadsImagesAutomatically = true
                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                                        useWideViewPort = true
                                        loadWithOverviewMode = true
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                        setSupportZoom(true)
                                        
                                        // Additional performance optimizations
                                        @Suppress("DEPRECATION")
                                        setRenderPriority(WebSettings.RenderPriority.HIGH)
                                        databaseEnabled = true
                                        blockNetworkImage = false
                                        layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                                        mediaPlaybackRequiresUserGesture = false
                                    }

                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                            // Handle all URLs within the WebView
                                            return false
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isLoading = false
                                            Log.d("WebViewActivity", "Page finished loading: $url")
                                        }

                                        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                            Toast.makeText(context, "Error loading content: $description", Toast.LENGTH_LONG).show()
                                            Log.e("WebViewActivity", "Error loading content: $description, URL: $failingUrl")
                                        }
                                    }

                                    webChromeClient = WebChromeClient()
                                    
                                    try {
                                        if (isPdf) {
                                            // For PDF files, use Google Docs viewer
                                            loadUrl("https://docs.google.com/viewer?url=$fileUrl&embedded=true")
                                            Log.d("WebViewActivity", "Loading PDF using Google Docs viewer: $fileUrl")
                                        } else {
                                            // For regular web content (case studies, blogs, job postings)
                                            loadUrl(fileUrl)
                                            Log.d("WebViewActivity", "Loading web content directly: $fileUrl")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("WebViewActivity", "Error loading URL: ${e.message}")
                                        Toast.makeText(context, "Failed to load the content: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}