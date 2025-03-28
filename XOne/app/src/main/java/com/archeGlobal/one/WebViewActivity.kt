package com.archeGlobal.one

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
        
        // Get base64 data if available (for PDF fallback)
        val base64Data = intent.getStringExtra("base64Data")
        
        // Determine if the URL is for a PDF
        val isPdf = intent.getBooleanExtra("isPdf", fileUrl.endsWith(".pdf", ignoreCase = true))
        // Check if this is a local file
        val isLocalFile = intent.getBooleanExtra("isLocalFile", false)
        
        // Special handling flag for PAN and Medical Insurance documents
        val isSpecialDocument = title == "PAN Card" || title == "Medical Insurance Card"
        
        // Flag to track if we should use offline mode (no PDF.js)
        val useOfflineMode = intent.getBooleanExtra("useOfflineMode", false)
        
        // Check if this is specific policy that needs SOS button
        val showSosButton = title.contains("Anti Bribery", ignoreCase = true) || 
                            title.contains("POSH", ignoreCase = true)
        
        Log.d("WebViewActivity", "Loading URL: $fileUrl, isPdf: $isPdf, isLocalFile: $isLocalFile, hasBase64: ${base64Data != null}, isSpecialDocument: $isSpecialDocument, useOfflineMode: $useOfflineMode")

        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE0DCD1),  // Light Grey/Beige
                                    Color(0xFFC8C8CA),  // Medium Grey
                                    Color(0xFF474749)   // Dark Grey
                                )
                            )
                        )
                ) {
                    // Main content
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top app bar
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
                                // SOS button only for specified policies
                                if (showSosButton) {
                                    IconButton(onClick = { 
                                        // Create intent for SOSActivity with special flags
                                        val intent = Intent(this@WebViewActivity, SOSActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                   Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                                                   Intent.FLAG_ACTIVITY_NO_ANIMATION
                                            putExtra("fromPdfViewer", true)
                                            putExtra("preventWhiteBar", true)
                                        }
                                        
                                        // Force current activity to have proper display settings
                                        window.statusBarColor = android.graphics.Color.TRANSPARENT
                                        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
                                        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 
                                                                             android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        
                                        // Start activity with no animation
                                        startActivity(intent)
                                        overridePendingTransition(0, 0)
                                    }) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color(0xFFDD3825), shape = androidx.compose.foundation.shape.CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "SOS",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                } else {
                                    // Add empty spacer with same size as navigation icon for balance
                                    Spacer(modifier = Modifier.width(48.dp))
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                        
                        // WebView Card that takes full width
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 0.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Loading indicator
                                var isLoading by remember { mutableStateOf(true) }
                                
                                // Show progress indicator while loading
                                if (isLoading) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
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

                                                    // ──────────────────────────────────────────────────────────
                                                    // ADD THIS FALLBACK CHECK:
                                                    // If we detect that the page is still blank after loading,
                                                    // we attempt to open with an external PDF viewer.
                                                    // You can fine-tune the "blank detection" logic as needed.
                                                    // ──────────────────────────────────────────────────────────
                                                    val webViewContentHeight = view?.contentHeight ?: 0
                                                    if (webViewContentHeight == 0 && isPdf && isSpecialDocument) {
                                                        Log.w("WebViewActivity", "WebView content is empty. Attempting to open externally.")
                                                        openPdfInExternalViewer(context)
                                                    }
                                                }

                                                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                                    Toast.makeText(context, "Error loading content: $description", Toast.LENGTH_LONG).show()
                                                    Log.e("WebViewActivity", "Error loading content: $description, URL: $failingUrl")

                                                    // If the page fails to load, try opening externally if it's a PDF
                                                    if (isPdf && isSpecialDocument) {
                                                        openPdfInExternalViewer(context)
                                                    }
                                                }
                                            }

                                            webChromeClient = WebChromeClient()
                                            
                                            try {
                                                if (isLocalFile) {
                                                    // For local files
                                                    val uri = Uri.parse(fileUrl)
                                                    
                                                    // Special direct handling for PAN Card and Medical Insurance Card documents
                                                    if (isSpecialDocument && base64Data != null && base64Data.isNotEmpty()) {
                                                        Log.d("WebViewActivity", "Using special direct base64 loading for: $title")
                                                        val pureBase64 = if (base64Data.contains(",")) {
                                                            base64Data.substring(base64Data.indexOf(",") + 1)
                                                        } else {
                                                            base64Data
                                                        }
                                                        
                                                        Log.d("WebViewActivity", "Base64 data length: ${pureBase64.length}")
                                                        
                                                        // Check if offline mode is requested or if we should use the full PDF.js approach
                                                        if (useOfflineMode) {
                                                            // Use a simple approach that doesn't rely on external resources
                                                            val simpleHtmlWrapper = """
                                                                <!DOCTYPE html>
                                                                <html>
                                                                <head>
                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                                    <style>
                                                                        body, html, iframe { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
                                                                    </style>
                                                                </head>
                                                                <body>
                                                                    <iframe src="data:application/pdf;base64,$pureBase64" width="100%" height="100%" style="border: none;"></iframe>
                                                                </body>
                                                                </html>
                                                            """.trimIndent()
                                                            
                                                            loadDataWithBaseURL(null, simpleHtmlWrapper, "text/html", "UTF-8", null)
                                                            Log.d("WebViewActivity", "Loaded special document using simple iframe (offline mode)")
                                                            
                                                            // Add error handling for iframe loading
                                                            webViewClient = object : WebViewClient() {
                                                                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                                                    Log.e("WebViewActivity", "Error loading iframe content: $description, URL: $failingUrl")
                                                                    Toast.makeText(context, "Error loading PDF: $description", Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                        } else {
                                                            // Use PDF.js for robust rendering with fallback
                                                            val htmlWrapper = """
                                                                <!DOCTYPE html>
                                                                <html>
                                                                <head>
                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                                    <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.min.js"></script>
                                                                    <style>
                                                                        body, html { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
                                                                        #viewerContainer { width: 100%; height: 100%; overflow: auto; position: absolute; }
                                                                        #viewer { position: absolute; width: 100%; }
                                                                        #fallbackContainer { display: none; width: 100%; height: 100%; }
                                                                    </style>
                                                                </head>
                                                                <body>
                                                                    <div id="viewerContainer">
                                                                        <div id="viewer" class="pdfViewer"></div>
                                                                    </div>
                                                                    
                                                                    <!-- Fallback container if PDF.js fails -->
                                                                    <div id="fallbackContainer">
                                                                        <iframe src="data:application/pdf;base64,$pureBase64" width="100%" height="100%" style="border: none;"></iframe>
                                                                    </div>
                                                                    
                                                                    <script>
                                                                        // Track if PDF.js loaded successfully
                                                                        let pdfJsLoaded = false;
                                                                        
                                                                        // Fallback function to use if PDF.js fails to load
                                                                        function useFallback() {
                                                                            if (!pdfJsLoaded) {
                                                                                console.log('Using fallback viewer');
                                                                                document.getElementById('viewerContainer').style.display = 'none';
                                                                                document.getElementById('fallbackContainer').style.display = 'block';
                                                                            }
                                                                        }
                                                                        
                                                                        // Set a timeout to check if PDF.js loaded
                                                                        setTimeout(useFallback, 3000);
                                                                        
                                                                        try {
                                                                            // Configure PDF.js worker
                                                                            pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.worker.min.js';
                                                                            
                                                                            // Load the PDF
                                                                            const loadingTask = pdfjsLib.getDocument({data: atob('$pureBase64')});
                                                                            loadingTask.promise.then(function(pdf) {
                                                                                console.log('PDF loaded');
                                                                                pdfJsLoaded = true;
                                                                                
                                                                                // Variable to track the current page
                                                                                let currentPageNum = 1;
                                                                                const numPages = pdf.numPages;
                                                                                const viewer = document.getElementById('viewer');
                                                                                
                                                                                // Function to render a page
                                                                                function renderPage(pageNumber) {
                                                                                    pdf.getPage(pageNumber).then(function(page) {
                                                                                        const viewport = page.getViewport({scale: 1.0});
                                                                                        
                                                                                        // Create a container for this page
                                                                                        const pageContainer = document.createElement('div');
                                                                                        pageContainer.className = 'page';
                                                                                        pageContainer.style.position = 'relative';
                                                                                        pageContainer.style.width = viewport.width + 'px';
                                                                                        pageContainer.style.height = viewport.height + 'px';
                                                                                        pageContainer.style.margin = '10px auto';
                                                                                        viewer.appendChild(pageContainer);
                                                                                        
                                                                                        // Create canvas for this page
                                                                                        const canvas = document.createElement('canvas');
                                                                                        pageContainer.appendChild(canvas);
                                                                                        
                                                                                        const context = canvas.getContext('2d');
                                                                                        canvas.height = viewport.height;
                                                                                        canvas.width = viewport.width;
                                                                                        
                                                                                        // Render PDF page
                                                                                        const renderContext = {
                                                                                            canvasContext: context,
                                                                                            viewport: viewport
                                                                                        };
                                                                                        
                                                                                        page.render(renderContext);
                                                                                    });
                                                                                }
                                                                                
                                                                                // Render all pages
                                                                                for (let i = 1; i <= numPages; i++) {
                                                                                    renderPage(i);
                                                                                }
                                                                            }, function (reason) {
                                                                                // Error handling
                                                                                console.error(reason);
                                                                                useFallback();
                                                                            });
                                                                        } catch (e) {
                                                                            console.error('Error setting up PDF.js: ' + e);
                                                                            useFallback();
                                                                        }
                                                                    </script>
                                                                </body>
                                                                </html>
                                                            """.trimIndent()
                                                            
                                                            loadDataWithBaseURL("https://example.com", htmlWrapper, "text/html", "UTF-8", null)
                                                            Log.d("WebViewActivity", "Loaded special document using PDF.js with fallback")
                                                        }
                                                        
                                                        return@apply
                                                    }
                                                    
                                                    if (isPdf) {
                                                        // For local PDF files, try a different approach
                                                        Log.d("WebViewActivity", "Loading local PDF file using alternative method")
                                                        
                                                        // Enable all necessary settings for PDF content
                                                        settings.javaScriptEnabled = true
                                                        settings.allowFileAccess = true
                                                        settings.allowContentAccess = true
                                                        settings.domStorageEnabled = true
                                                        settings.setSupportZoom(true)
                                                        settings.builtInZoomControls = true
                                                        
                                                        // Try direct base64 loading first if available
                                                        if (base64Data != null && base64Data.isNotEmpty()) {
                                                            val pureBase64 = if (base64Data.contains(",")) {
                                                                base64Data.substring(base64Data.indexOf(",") + 1)
                                                            } else {
                                                                base64Data
                                                            }
                                                            
                                                            val htmlWrapper = """
                                                                <!DOCTYPE html>
                                                                <html>
                                                                <head>
                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                                    <style>
                                                                        body, html, iframe { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
                                                                    </style>
                                                                </head>
                                                                <body>
                                                                    <iframe src="data:application/pdf;base64,$pureBase64" width="100%" height="100%" style="border: none;"></iframe>
                                                                </body>
                                                                </html>
                                                            """.trimIndent()
                                                            
                                                            loadDataWithBaseURL(null, htmlWrapper, "text/html", "UTF-8", null)
                                                            Log.d("WebViewActivity", "Loaded PDF directly using base64 data")
                                                            return@apply
                                                        }
                                                        
                                                        // Enable all necessary settings for PDF content
                                                        settings.javaScriptEnabled = true
                                                        settings.allowFileAccess = true
                                                        settings.allowContentAccess = true
                                                        settings.domStorageEnabled = true
                                                        settings.setSupportZoom(true)
                                                        settings.builtInZoomControls = true
                                                        
                                                        // Add a timeout to give WebView time to initialize
                                                        Handler().postDelayed({
                                                            // Try loading directly with a data URI
                                                            val actualPath = uri.path ?: ""
                                                            val file = java.io.File(actualPath)
                                                            
                                                            if (file.exists()) {
                                                                Log.d("WebViewActivity", "PDF file exists at: ${file.absolutePath}, size: ${file.length()} bytes")
                                                                // Try Content URI approach
                                                                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                                                    context,
                                                                    context.applicationContext.packageName + ".provider",
                                                                    file
                                                                )
                                                                
                                                                if (isSpecialDocument) {
                                                                    // For PAN and Medical Insurance documents, try direct data URI loading first
                                                                    try {
                                                                        Log.d("WebViewActivity", "Special document fallback - loading directly")
                                                                        val bytes = file.readBytes()
                                                                        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                                                                        
                                                                        // Use PDF.js for robust rendering
                                                                        val htmlWrapper = """
                                                                            <!DOCTYPE html>
                                                                            <html>
                                                                            <head>
                                                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                                                <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.min.js"></script>
                                                                                <style>
                                                                                    body, html { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
                                                                                    #viewerContainer { width: 100%; height: 100%; overflow: auto; position: absolute; }
                                                                                    #viewer { position: absolute; width: 100%; }
                                                                                </style>
                                                                            </head>
                                                                            <body>
                                                                                <div id="viewerContainer">
                                                                                    <div id="viewer" class="pdfViewer"></div>
                                                                                </div>
                                                                                
                                                                                <script>
                                                                                    // Configure PDF.js worker
                                                                                    pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.worker.min.js';
                                                                                    
                                                                                    // Load the PDF
                                                                                    const loadingTask = pdfjsLib.getDocument({data: atob('$base64')});
                                                                                    loadingTask.promise.then(function(pdf) {
                                                                                        console.log('PDF loaded');
                                                                                        
                                                                                        // Variable to track the current page
                                                                                        let currentPageNum = 1;
                                                                                        const numPages = pdf.numPages;
                                                                                        const viewer = document.getElementById('viewer');
                                                                                        
                                                                                        // Function to render a page
                                                                                        function renderPage(pageNumber) {
                                                                                            pdf.getPage(pageNumber).then(function(page) {
                                                                                                const viewport = page.getViewport({scale: 1.0});
                                                                                                
                                                                                                // Create a container for this page
                                                                                                const pageContainer = document.createElement('div');
                                                                                                pageContainer.className = 'page';
                                                                                                pageContainer.style.position = 'relative';
                                                                                                pageContainer.style.width = viewport.width + 'px';
                                                                                                pageContainer.style.height = viewport.height + 'px';
                                                                                                pageContainer.style.margin = '10px auto';
                                                                                                viewer.appendChild(pageContainer);
                                                                                                
                                                                                                // Create canvas for this page
                                                                                                const canvas = document.createElement('canvas');
                                                                                                pageContainer.appendChild(canvas);
                                                                                                
                                                                                                const context = canvas.getContext('2d');
                                                                                                canvas.height = viewport.height;
                                                                                                canvas.width = viewport.width;
                                                                                                
                                                                                                // Render PDF page
                                                                                                const renderContext = {
                                                                                                    canvasContext: context,
                                                                                                    viewport: viewport
                                                                                                };
                                                                                                
                                                                                                page.render(renderContext);
                                                                                            });
                                                                                        }
                                                                                        
                                                                                        // Render all pages
                                                                                        for (let i = 1; i <= numPages; i++) {
                                                                                            renderPage(i);
                                                                                        }
                                                                                    }, function (reason) {
                                                                                        // Error handling
                                                                                        console.error(reason);
                                                                                        document.body.innerHTML = '<div style="color: red; padding: 20px;">Error loading PDF: ' + reason + '</div>';
                                                                                    });
                                                                                </script>
                                                                            </body>
                                                                            </html>
                                                                        """.trimIndent()
                                                                        
                                                                        loadDataWithBaseURL("https://example.com", htmlWrapper, "text/html", "UTF-8", null)
                                                                        Log.d("WebViewActivity", "Loaded special document fallback using PDF.js")
                                                                        return@postDelayed
                                                                    } catch (e: Exception) {
                                                                        Log.e("WebViewActivity", "Error in special document direct loading: ${e.message}", e)
                                                                    }
                                                                }
                                                                
                                                                Log.d("WebViewActivity", "Using content URI: $contentUri")
                                                                loadUrl("https://docs.google.com/gview?embedded=true&url=$contentUri")
                                                                
                                                                // Set up a fallback timer in case Google Docs viewer doesn't work
                                                                Handler().postDelayed({
                                                                    // Try direct base64 loading if we detect file wasn't loaded
                                                                    try {
                                                                        // Read the file bytes
                                                                        val bytes = file.readBytes()
                                                                        if (bytes.isNotEmpty()) {
                                                                            // Convert to base64
                                                                            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                                                                            
                                                                            // Create a data URI
                                                                            val dataUri = "data:application/pdf;base64,$base64"
                                                                            
                                                                            // Load using an HTML wrapper with PDF.js
                                                                            val htmlWrapper = """
                                                                                <!DOCTYPE html>
                                                                                <html>
                                                                                <head>
                                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                                                    <style>
                                                                                        body, html, iframe { margin: 0; padding: 0; height: 100%; width: 100%; }
                                                                                    </style>
                                                                                </head>
                                                                                <body>
                                                                                    <iframe src="$dataUri" width="100%" height="100%" style="border: none;"></iframe>
                                                                                </body>
                                                                                </html>
                                                                            """.trimIndent()
                                                                            
                                                                            loadDataWithBaseURL(null, htmlWrapper, "text/html", "UTF-8", null)
                                                                            Log.d("WebViewActivity", "Loaded PDF using data URI fallback approach")
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Log.e("WebViewActivity", "Error in fallback PDF loading: ${e.message}", e)
                                                                    }
                                                                }, 5000) // 5 seconds fallback timer
                                                            } else {
                                                                Log.e("WebViewActivity", "PDF file doesn't exist at: $actualPath")
                                                                Toast.makeText(context, "Error: PDF file not found", Toast.LENGTH_LONG).show()
                                                            }
                                                        }, 500)
                                                    } else {
                                                        // For images and other file types
                                                        loadUrl(uri.toString())
                                                        Log.d("WebViewActivity", "Loading local non-PDF file: $fileUrl")
                                                    }
                                                } else if (isPdf) {
                                                    // For remote PDF files, use Google Docs viewer
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

        this.setTheme(R.style.Theme_XOne)
    }

    private fun openPdfInExternalViewer(context: Context) {
        try {
            // Rebuild the local content:// URI
            val fileUri = Uri.parse(intent.getStringExtra("fileUrl"))
            Log.d("WebViewActivity", "Falling back to external PDF viewer; URI=$fileUri")

            // Create an Intent that prompts the user to open in any external PDF app
            val externalIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            // Verify there is at least one app that can handle this intent
            val packageManager = context.packageManager
            val activities = packageManager.queryIntentActivities(externalIntent, 0)
            if (activities.isNotEmpty()) {
                context.startActivity(externalIntent)
            } else {
                Toast.makeText(context, "No PDF viewer app found on this device.", Toast.LENGTH_LONG).show()
                Log.e("WebViewActivity", "No external PDF viewer available.")
            }
        } catch (e: Exception) {
            Log.e("WebViewActivity", "Failed to open PDF externally: ${e.message}", e)
            Toast.makeText(context, "Failed to open PDF externally: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}