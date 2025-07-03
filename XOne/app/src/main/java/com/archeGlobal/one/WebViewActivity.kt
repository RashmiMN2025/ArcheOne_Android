package com.archeGlobal.one

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.XOneTheme
import org.json.JSONObject
import android.net.http.SslError
import android.webkit.SslErrorHandler

class WebViewActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val fileUrl = intent.getStringExtra("fileUrl") ?: ""
        val title = intent.getStringExtra("title") ?: "Floor Map"

        // Get base64 data if available (for PDF fallback)
        val base64Data = intent.getStringExtra("base64Data")

        // Determine if the URL is for a PDF
        val isPdf = intent.getBooleanExtra("isPdf", fileUrl.endsWith(".pdf", ignoreCase = true))
        // Check if this is a local file
        val isLocalFile = intent.getBooleanExtra("isLocalFile", false)

        // Special handling flag for PAN and Medical Insurance documents
        val isSpecialDocument = title == "PAN Card" || title == "Medical Insurance Card"

        // Special handling for Floor Map PDFs
        val isFloorMap = title == "Floor Map" || title.contains("Floor Map", ignoreCase = true) || title.contains("Location", ignoreCase = true)

        // Flag to track if we should use offline mode (no PDF.js)
        val useOfflineMode = intent.getBooleanExtra("useOfflineMode", false)

        // Check if this is specific policy that needs SOS button
        val showSosButton = title.contains("Anti Bribery", ignoreCase = true) || title.contains("POSH", ignoreCase = true)

        val rawHtmlContent = intent.getStringExtra("rawHtmlContent")
        if (rawHtmlContent != null && rawHtmlContent.isNotBlank()) {
            setContent {
                XOneTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE0DCD1), // Light Grey/Beige
                                        Color(0xFFC8C8CA), // Medium Grey
                                        Color(0xFF474749) // Dark Grey
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Top app bar with gradient background
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
                                    // Empty spacer for balance
                                    Spacer(modifier = Modifier.width(48.dp))
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent
                                )
                            )

                            // WebView content
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.defaultTextEncodingName = "utf-8"
                                        settings.javaScriptEnabled = true
                                        loadDataWithBaseURL(null, rawHtmlContent, "text/html", "UTF-8", null)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            )
                        }
                    }
                }
            }
            return
        }

        Log.d("WebViewActivity", "Loading URL: $fileUrl, isPdf: $isPdf, isLocalFile: $isLocalFile, hasBase64: ${base64Data != null}, isSpecialDocument: $isSpecialDocument, isFloorMap: $isFloorMap, useOfflineMode: $useOfflineMode")

        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE0DCD1), // Light Grey/Beige
                                    Color(0xFFC8C8CA), // Medium Grey
                                    Color(0xFF474749) // Dark Grey
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
                                        try {
                                            Log.d("WebViewActivity", "Navigating to SOS from policy")
                                            // Create intent for SOSActivity with special flags
                                            val intent = Intent(this@WebViewActivity, SOSActivity::class.java).apply {
                                                // Don't use FLAG_ACTIVITY_NEW_TASK as it can cause issues with parcelable objects
                                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION

                                                // Set showHeader to false to ensure we don't get navigation conflicts
                                                putExtra("showHeader", false)
                                                putExtra("fromPdfViewer", true)
                                                putExtra("preventWhiteBar", true)
                                                // Add this to track when opened from policy
                                                putExtra("fromPolicy", true)
                                            }

                                            // Force current activity to have proper display settings
                                            window.statusBarColor = android.graphics.Color.TRANSPARENT
                                            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
                                            window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                                            // Start activity with no animation
                                            startActivity(intent)
                                            overridePendingTransition(0, 0)
                                        } catch (e: Exception) {
                                            Log.e("WebViewActivity", "Error navigating to SOS: ${e.message}", e)
                                            e.printStackTrace() // Print full stack trace for better debugging
                                        }
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
                                .padding(0.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .fillMaxWidth()
                            ) {
                                // Loading indicator
                                var isLoading by remember { mutableStateOf(true) }

                                // Show UniversalLoader while loading
                                if (isLoading) {
                                    UniversalLoader(isLoading = true)
                                }

                                // WebView for content display
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .fillMaxWidth()
                                        .padding(0.dp),
                                    factory = { context ->
                                        WebView(context).apply {
                                            layoutParams = android.view.ViewGroup.LayoutParams(
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            settings.apply {
                                                javaScriptEnabled = true
                                                allowFileAccess = true
                                                domStorageEnabled = true
                                                loadsImagesAutomatically = true
                                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                                cacheMode = WebSettings.LOAD_DEFAULT

                                                // Enhanced viewport and scaling settings for better width fitting
                                                useWideViewPort = true
                                                loadWithOverviewMode = true
                                                setSupportZoom(true)
                                                builtInZoomControls = true
                                                displayZoomControls = false

                                                // Force width to match screen
                                                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

                                                // Additional settings for better rendering
                                                @SuppressLint("SetJavaScriptEnabled")
                                                javaScriptEnabled = true
                                                setNeedInitialFocus(true)

                                                // Enable DOM storage and databases
                                                domStorageEnabled = true
                                                databaseEnabled = true

                                                // Additional settings for SAP portal
                                                javaScriptCanOpenWindowsAutomatically = true
                                                setSupportMultipleWindows(true)
                                                allowContentAccess = true

                                                // Additional performance optimizations
                                                @Suppress("DEPRECATION")
                                                setRenderPriority(WebSettings.RenderPriority.HIGH)
                                                blockNetworkImage = false
                                                mediaPlaybackRequiresUserGesture = false
                                            }

                                            webViewClient = object : WebViewClient() {
                                                private var pageLoaded = false

                                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                                    return false
                                                }

                                                override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                                                    Log.e("WebViewActivity", "SSL Error: ${error.primaryError} on URL: ${error.url}")

                                                    // Create an alert dialog to inform the user
                                                    val builder = AlertDialog.Builder(context)
                                                    builder.setTitle("SSL Certificate Error")

                                                    // Customize message based on the type of SSL error
                                                    val errorMessage = when (error.primaryError) {
                                                        SslError.SSL_NOTYETVALID -> "The certificate is not yet valid."
                                                        SslError.SSL_EXPIRED -> "The certificate has expired."
                                                        SslError.SSL_IDMISMATCH -> "The certificate hostname does not match."
                                                        SslError.SSL_UNTRUSTED -> "The certificate authority is not trusted."
                                                        SslError.SSL_DATE_INVALID -> "The certificate date is invalid."
                                                        else -> "An unknown SSL error occurred."
                                                    }

                                                    builder.setMessage("A security issue was detected with the website's SSL certificate: $errorMessage\n\nDo you want to proceed anyway? (Not recommended)")
                                                    builder.setPositiveButton("Proceed") { _, _ ->
                                                        handler.proceed() // Allow the user to proceed (use with caution)
                                                    }
                                                    builder.setNegativeButton("Cancel") { _, _ ->
                                                        handler.cancel() // Cancel the request
                                                        Toast.makeText(context, "Connection aborted due to SSL error.", Toast.LENGTH_LONG).show()
                                                    }
                                                    builder.setCancelable(false) // Prevent dismissing the dialog without a choice
                                                    builder.show()
                                                }

                                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                                                    val isPersonal = intent.getBooleanExtra("isPersonal", false)
                                                    val documentTitle = intent.getStringExtra("title") ?: ""

                                                    if (isPersonal && request?.url != null) {
                                                        Log.d("WebViewActivity", "Intercepting request for UserDocuments: ${request.url}")

                                                        // Check if the URL pattern suggests it might return HTML error content
                                                        val url = request.url.toString()
                                                        if (url.contains("download_doc") && url.contains("pulse.netcon.in")) {
                                                            Log.d("WebViewActivity", "Detected potential document download URL: $url")
                                                        }
                                                    }

                                                    return super.shouldInterceptRequest(view, request)
                                                }

                                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                                    super.onPageStarted(view, url, favicon)
                                                    isLoading = true
                                                    pageLoaded = false
                                                    Log.d("WebViewActivity", "Page started loading: $url")

                                                    // Check if this is a UserDocuments screen
                                                    val isPersonal = intent.getBooleanExtra("isPersonal", false)
                                                    val documentTitle = intent.getStringExtra("title") ?: ""

                                                    // For UserDocuments, check if URL might lead to HTML content
                                                    if (isPersonal && url != null) {
                                                        Log.d("WebViewActivity", "UserDocuments page started loading: $url")
                                                    }
                                                }

                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                    pageLoaded = true

                                                    // Check if this is a UserDocuments screen and if the page contains HTML error content
                                                    val isPersonal = intent.getBooleanExtra("isPersonal", false)
                                                    val documentTitle = intent.getStringExtra("title") ?: ""

                                                    if (isPersonal && view != null) {
                                                        // Add a delay to ensure the page is fully loaded
                                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                            // Inject JavaScript to check if the page contains HTML error content
                                                            view.evaluateJavascript(
                                                                """
                                                                (function() {
                                                                    try {
                                                                        var bodyText = document.body ? document.body.innerText : '';
                                                                        var htmlContent = document.documentElement ? document.documentElement.innerHTML : '';
                                                                        
                                                                        // Check if this looks like an HTML page rather than a PDF
                                                                        var isHtmlContent = (htmlContent.includes('<html') || htmlContent.includes('<!DOCTYPE')) && 
                                                                                          htmlContent.includes('<body') &&
                                                                                          !htmlContent.includes('application/pdf') &&
                                                                                          !htmlContent.includes('pdf.js') &&
                                                                                          !htmlContent.includes('pdfobject') &&
                                                                                          bodyText.length < 2000;
                                                                        
                                                                        // Also check if the content looks like server error or plain text
                                                                        var hasPlainTextContent = bodyText.length > 0 && bodyText.length < 1000 && 
                                                                                                 !bodyText.includes('PDF') &&
                                                                                                 (bodyText.includes('error') || bodyText.includes('not found') || 
                                                                                                  bodyText.includes('file') || bodyText.includes('server') ||
                                                                                                  htmlContent.length < 5000);
                                                                        
                                                                        if (isHtmlContent || hasPlainTextContent) {
                                                                            return JSON.stringify({
                                                                                isError: true,
                                                                                content: bodyText.trim()
                                                                            });
                                                                        }
                                                                        
                                                                        return JSON.stringify({isError: false});
                                                                    } catch (e) {
                                                                        return JSON.stringify({isError: false});
                                                                    }
                                                                })();
                                                                """.trimIndent()
                                                            ) { result ->
                                                                try {
                                                                    Log.d("WebViewActivity", "JavaScript result: $result")
                                                                    // Remove quotes from the result and parse JSON
                                                                    val cleanResult = result?.replace("\\\"", "\"")?.trim('"') ?: ""
                                                                    if (cleanResult.isNotEmpty() && cleanResult.startsWith("{")) {
                                                                        val resultObj = JSONObject(cleanResult)
                                                                        if (resultObj.getBoolean("isError")) {
                                                                            val htmlErrorContent = resultObj.getString("content")
                                                                            Log.d("WebViewActivity", "Detected HTML error content for document: $documentTitle")
                                                                            Log.d("WebViewActivity", "Content length: ${htmlErrorContent.length}")

                                                                            // Create a formatted HTML display for the error content
                                                                            val formattedHtml = """
                                                                                <!DOCTYPE html>
                                                                                <html>
                                                                                <head>
                                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                                                    <style>
                                                                                        body {
                                                                                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                                                                            margin: 0;
                                                                                            padding: 20px;
                                                                                            background-color: #f5f5f5;
                                                                                            color: #333;
                                                                                            line-height: 1.6;
                                                                                        }
                                                                                        .container {
                                                                                            background-color: white;
                                                                                            padding: 30px;
                                                                                            border-radius: 12px;
                                                                                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                                                                                            max-width: 100%;
                                                                                            margin: 0 auto;
                                                                                        }
                                                                                        .header {
                                                                                            color: #dd3825;
                                                                                            font-size: 20px;
                                                                                            font-weight: bold;
                                                                                            margin-bottom: 20px;
                                                                                            text-align: center;
                                                                                            border-bottom: 2px solid #dd3825;
                                                                                            padding-bottom: 10px;
                                                                                        }
                                                                                        .content {
                                                                                            white-space: pre-wrap;
                                                                                            font-size: 13px;
                                                                                            background-color: #f8f9fa;
                                                                                            padding: 20px;
                                                                                            border-radius: 8px;
                                                                                            border-left: 4px solid #dd3825;
                                                                                            margin-bottom: 20px;
                                                                                            font-family: 'Courier New', monospace;
                                                                                            overflow-x: auto;
                                                                                        }
                                                                                        .message {
                                                                                            text-align: center;
                                                                                            color: #666;
                                                                                            font-size: 16px;
                                                                                            font-weight: 500;
                                                                                            background-color: #fff3cd;
                                                                                            padding: 15px;
                                                                                            border-radius: 8px;
                                                                                            border: 1px solid #ffeaa7;
                                                                                        }
                                                                                    </style>
                                                                                </head>
                                                                                <body>
                                                                                    <div class="container">
                                                                                        <div class="header">Document Response</div>
                                                                                        <div class="content">${htmlErrorContent.replace("<", "<").replace(">", ">").replace("\n", "<br>")}</div>
                                                                                        <div class="message">
                                                                                            ⚠️ No document found for $documentTitle<br>
                                                                                            Please upload the document to view it here.
                                                                                        </div>
                                                                                    </div>
                                                                                </body>
                                                                                </html>
                                                                            """.trimIndent()

                                                                            // Load the formatted HTML content
                                                                            view.loadDataWithBaseURL(null, formattedHtml, "text/html", "UTF-8", null)
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Log.e("WebViewActivity", "Error parsing JavaScript result: ${e.message}")
                                                                }
                                                            }
                                                        }, 1000) // Reduced delay to 1 second

                                                        // Also try an immediate check
                                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                            view.evaluateJavascript("document.readyState") { readyState ->
                                                                Log.d("WebViewActivity", "Document ready state: $readyState")
                                                            }
                                                        }, 500)
                                                    }

                                                    // Delay hiding the loader to ensure content is actually rendered
                                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                        if (pageLoaded) {
                                                            isLoading = false
                                                        }
                                                    }, 1000) // 1 second delay

                                                    Log.d("WebViewActivity", "Page finished loading: $url")

                                                    // Special handling for SAP portal
                                                    if (url?.contains("businessbydesign.cloud.sap") == true) {
                                                        view?.evaluateJavascript(
                                                            """
                                                            (function() {
                                                                var meta = document.querySelector('meta[name="viewport"]');
                                                                if (!meta) {
                                                                    meta = document.createElement('meta');
                                                                    meta.name = 'viewport';
                                                                    document.head.appendChild(meta);
                                                                }
                                                                meta.content = 'width=1024, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes';
                                                                
                                                                // Add styles for SAP portal
                                                                var style = document.createElement('style');
                                                                style.textContent = `
                                                                    body {
                                                                        margin: 0 !important;
                                                                        padding: 0 !important;
                                                                        min-width: 1024px !important;
                                                                        min-height: 100vh !important;
                                                                        width: 100% !important;
                                                                        height: 100% !important;
                                                                        overflow: auto !important;
                                                                    }
                                                                    #shell {
                                                                        width: 100% !important;
                                                                        height: 100% !important;
                                                                        position: absolute !important;
                                                                        top: 0 !important;
                                                                        left: 0 !important;
                                                                    }
                                                                    iframe {
                                                                        width: 100% !important;
                                                                        height: 100% !important;
                                                                        position: absolute !important;
                                                                        top: 0 !important;
                                                                        left: 0 !important;
                                                                        border: none !important;
                                                                    }
                                                                `;
                                                                document.head.appendChild(style);
                                                                
                                                                // Force visibility of main container
                                                                var mainContainer = document.querySelector('#shell') || document.body;
                                                                if (mainContainer) {
                                                                    mainContainer.style.display = 'block';
                                                                    mainContainer.style.visibility = 'visible';
                                                                    mainContainer.style.opacity = '1';
                                                                }
                                                            })();
                                                            """.trimIndent(),
                                                            null
                                                        )
                                                    }
                                                }

                                                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                                    Toast.makeText(context, "Error loading content: $description", Toast.LENGTH_LONG).show()
                                                    Log.e("WebViewActivity", "Error loading content: $description, URL: $failingUrl")
                                                }
                                            }

                                            webChromeClient = object : WebChromeClient() {
                                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                    super.onProgressChanged(view, newProgress)
                                                    Log.d("WebViewActivity", "Loading progress: $newProgress%")
                                                }
                                            }

                                            // Set initial scale
                                            setInitialScale(100)

                                            // Enable hardware acceleration
                                            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

                                            // Prevent WebView from losing focus
                                            setOnTouchListener { _, _ ->
                                                requestFocus()
                                                false
                                            }

                                            try {
                                                if (isLocalFile) {
                                                    // For local files
                                                    val uri = Uri.parse(fileUrl)

                                                    // Special direct handling for PAN Card, Medical Insurance Card, and Floor Map documents
                                                    if ((isSpecialDocument || isFloorMap) && isPdf) {
                                                        Log.d("WebViewActivity", "Using special PDF handling for: $title")

                                                        if (base64Data != null && base64Data.isNotEmpty()) {
                                                            val pureBase64 = if (base64Data.contains(",")) {
                                                                base64Data.substring(base64Data.indexOf(",") + 1)
                                                            } else {
                                                                base64Data
                                                            }

                                                            Log.d("WebViewActivity", "Base64 data length: ${pureBase64.length}")

                                                            // Use PDF.js for robust rendering with fallback
                                                            val htmlWrapper = """
                                                                <!DOCTYPE html>
                                                                <html>
                                                                <head>
                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                                                    <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.min.js"></script>
                                                                    <style>
                                                                        body, html { 
                                                                            margin: 0 !important; 
                                                                            padding: 0 !important; 
                                                                            height: 100% !important; 
                                                                            width: 100% !important; 
                                                                            overflow: hidden !important;
                                                                        }
                                                                        #viewerContainer { 
                                                                            width: 100% !important; 
                                                                            height: 100% !important; 
                                                                            overflow: auto !important; 
                                                                            position: absolute !important;
                                                                            left: 0 !important;
                                                                            top: 0 !important;
                                                                            right: 0 !important;
                                                                            bottom: 0 !important;
                                                                        }
                                                                        #viewer { 
                                                                            position: absolute !important; 
                                                                            width: 100% !important; 
                                                                            left: 0 !important;
                                                                            right: 0 !important;
                                                                            margin: 0 !important;
                                                                            padding: 0 !important;
                                                                        }
                                                                        #fallbackContainer { 
                                                                            display: none; 
                                                                            width: 100% !important; 
                                                                            height: 100% !important;
                                                                            margin: 0 !important;
                                                                            padding: 0 !important;
                                                                        }
                                                                        .page { 
                                                                            box-shadow: 0 2px 5px rgba(0,0,0,0.2); 
                                                                            margin: 0 !important;
                                                                            padding: 0 !important;
                                                                            width: 100% !important;
                                                                        }
                                                                        iframe {
                                                                            width: 100% !important;
                                                                            height: 100% !important;
                                                                            border: none !important;
                                                                            margin: 0 !important;
                                                                            padding: 0 !important;
                                                                        }
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
                                                                            
                                                                            // Get container width for scaling calculation
                                                                            const containerWidth = document.getElementById('viewerContainer').clientWidth;
                                                                            
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
                                                                                        // Calculate scale to fit the page width to container
                                                                                        const originalViewport = page.getViewport({scale: 1.0});
                                                                                        const scale = (containerWidth - 20) / originalViewport.width; // -20 for margins
                                                                                        const viewport = page.getViewport({scale: scale});
                                                                                        
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
                                                            Log.d("WebViewActivity", "Loaded document using PDF.js with fit-to-width scaling")
                                                            return@apply
                                                        } else if (isFloorMap && isPdf) {
                                                            // For Floor Map PDFs without base64 data, try to load and convert the file
                                                            try {
                                                                val actualPath = uri.path ?: ""
                                                                val file = java.io.File(actualPath)

                                                                if (file.exists()) {
                                                                    Log.d("WebViewActivity", "Floor Map PDF file exists at: ${file.absolutePath}, size: ${file.length()} bytes")

                                                                    // Read the file bytes
                                                                    val bytes = file.readBytes()
                                                                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)

                                                                    // Use PDF.js for rendering
                                                                    val htmlWrapper = """
                                                                        <!DOCTYPE html>
                                                                        <html>
                                                                        <head>
                                                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                                                            <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.min.js"></script>
                                                                            <style>
                                                                                body, html { 
                                                                                    margin: 0 !important; 
                                                                                    padding: 0 !important; 
                                                                                    height: 100% !important; 
                                                                                    width: 100% !important; 
                                                                                    overflow: hidden !important;
                                                                                }
                                                                                #viewerContainer { 
                                                                                    width: 100% !important; 
                                                                                    height: 100% !important; 
                                                                                    overflow: auto !important; 
                                                                                    position: absolute !important;
                                                                                    left: 0 !important;
                                                                                    top: 0 !important;
                                                                                    right: 0 !important;
                                                                                    bottom: 0 !important;
                                                                                }
                                                                                #viewer { 
                                                                                    position: absolute !important; 
                                                                                    width: 100% !important; 
                                                                                    left: 0 !important;
                                                                                    right: 0 !important;
                                                                                    margin: 0 !important;
                                                                                    padding: 0 !important;
                                                                                }
                                                                                #fallbackContainer { 
                                                                                    display: none; 
                                                                                    width: 100% !important; 
                                                                                    height: 100% !important;
                                                                                    margin: 0 !important;
                                                                                    padding: 0 !important;
                                                                                }
                                                                                .page { 
                                                                                    box-shadow: 0 2px 5px rgba(0,0,0,0.2); 
                                                                                    margin: 0 !important;
                                                                                    padding: 0 !important;
                                                                                    width: 100% !important;
                                                                                }
                                                                                iframe {
                                                                                    width: 100% !important;
                                                                                    height: 100% !important;
                                                                                    border: none !important;
                                                                                    margin: 0 !important;
                                                                                    padding: 0 !important;
                                                                                }
                                                                            </style>
                                                                        </head>
                                                                        <body>
                                                                            <div id="viewerContainer">
                                                                                <div id="viewer" class="pdfViewer"></div>
                                                                            </div>
                                                                            
                                                                            <!-- Fallback container if PDF.js fails -->
                                                                            <div id="fallbackContainer">
                                                                                <iframe src="data:application/pdf;base64,$base64" width="100%" height="100%" style="border: none;"></iframe>
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
                                                                                    
                                                                                    // Get container width for scaling calculation
                                                                                    const containerWidth = document.getElementById('viewerContainer').clientWidth;
                                                                                    
                                                                                    // Load the PDF
                                                                                    const loadingTask = pdfjsLib.getDocument({data: atob('$base64')});
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
                                                                                                // Calculate scale to fit the page width to container
                                                                                                const originalViewport = page.getViewport({scale: 1.0});
                                                                                                const scale = (containerWidth - 20) / originalViewport.width; // -20 for margins
                                                                                                const viewport = page.getViewport({scale: scale});
                                                                                                
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
                                                                    Log.d("WebViewActivity", "Loaded Floor Map PDF using PDF.js with fit-to-width scaling")
                                                                    return@apply
                                                                }
                                                            } catch (e: Exception) {
                                                                Log.e("WebViewActivity", "Error loading Floor Map PDF: ${e.message}", e)
                                                            }
                                                        }
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
                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
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
                                                                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
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
                                                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
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
                                                        // For images, create an HTML wrapper with proper image display
                                                        val htmlWrapper = """
                                                            <!DOCTYPE html>
                                                            <html>
                                                            <head>
                                                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                                                <style>
                                                                    body, html {
                                                                        margin: 0;
                                                                        padding: 0;
                                                                        height: 100%;
                                                                        width: 100%;
                                                                        display: flex;
                                                                        justify-content: center;
                                                                        align-items: center;
                                                                        background-color: #f5f5f5;
                                                                    }
                                                                    img {
                                                                        max-width: 100vw;
                                                                        width: 100%;
                                                                        height: auto;
                                                                        max-height: 100vh;
                                                                        object-fit: contain;
                                                                        box-shadow: 0 2px 5px rgba(0,0,0,0.2);
                                                                    }
                                                                </style>
                                                            </head>
                                                            <body>
                                                                <img src="$fileUrl" alt="Document Image">
                                                            </body>
                                                            </html>
                                                        """.trimIndent()

                                                        loadDataWithBaseURL(null, htmlWrapper, "text/html", "UTF-8", null)
                                                        Log.d("WebViewActivity", "Loading image file: $fileUrl")
                                                    }
                                                } else if (isPdf) {
                                                    // For remote PDF files
                                                    if (isFloorMap) {
                                                        // For Floor Map PDFs, use the same PDF.js implementation as policies
                                                        val htmlWrapper = """
                                                            <!DOCTYPE html>
                                                            <html>
                                                            <head>
                                                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                                                <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.min.js"></script>
                                                                <style>
                                                                    body, html { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
                                                                    #viewerContainer { width: 100%; height: 100%; overflow: auto; position: absolute; }
                                                                    #viewer { 
                                                                        position: absolute; 
                                                                        width: 100%; 
                                                                        max-width: 100vw;
                                                                        margin: 0 auto;
                                                                    }
                                                                    #fallbackContainer { display: none; width: 100%; height: 100%; }
                                                                    .page { 
                                                                        box-shadow: 0 2px 5px rgba(0,0,0,0.2); 
                                                                        margin-bottom: 15px !important;
                                                                        width: 100% !important;
                                                                        max-width: 100vw !important;
                                                                    }
                                                                </style>
                                                            </head>
                                                            <body>
                                                                <div id="viewerContainer">
                                                                    <div id="viewer" class="pdfViewer"></div>
                                                                </div>
                                                                
                                                                <!-- Fallback container if PDF.js fails -->
                                                                <div id="fallbackContainer">
                                                                    <iframe src="https://docs.google.com/viewer?url=$fileUrl&embedded=true" width="100%" height="100%" style="border: none;"></iframe>
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
                                                                        
                                                                        // Get container width for scaling calculation
                                                                        const containerWidth = document.getElementById('viewerContainer').clientWidth;
                                                                        
                                                                        // Load the PDF
                                                                        const loadingTask = pdfjsLib.getDocument('$fileUrl');
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
                                                                                    // Calculate scale to fit the page width to container
                                                                                    const originalViewport = page.getViewport({scale: 1.0});
                                                                                    const scale = (containerWidth - 20) / originalViewport.width; // -20 for margins
                                                                                    const viewport = page.getViewport({scale: scale});
                                                                                    
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
                                                        Log.d("WebViewActivity", "Loading Floor Map PDF using policy-style PDF.js viewer with fit-to-width: $fileUrl")
                                                    } else {
                                                        // Use Google Docs viewer for other PDF types
                                                        loadUrl("https://docs.google.com/viewer?url=$fileUrl&embedded=true")
                                                        Log.d("WebViewActivity", "Loading PDF using Google Docs viewer: $fileUrl")
                                                    }
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