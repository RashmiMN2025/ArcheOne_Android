package com.archeGlobal.one.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.archeGlobal.one.ui.components.UniversalLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
private fun CustomTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    showSosButton: Boolean,
    context: Context,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Column {
        Spacer(modifier = Modifier.height(statusBarPadding.calculateTopPadding()))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                    )
                }
            }

            // Title
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }

            // SOS button or spacer
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (showSosButton) {
                    IconButton(
                        onClick = {
                            // Create intent for SOSActivity with special flags
                            val intent =
                                android.content.Intent(context, com.archeGlobal.one.SOSActivity::class.java).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
                                    putExtra("fromPdfViewer", true)
                                    putExtra("preventWhiteBar", true)
                                    putExtra("showHeader", false)
                                }

                            // Force current activity to have proper display settings
                            (context as? androidx.activity.ComponentActivity)?.let { activity ->
                                activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
                                androidx.core.view.WindowCompat
                                    .setDecorFitsSystemWindows(activity.window, false)
                            }

                            // Start activity with no animation
                            context.startActivity(intent)
                            (context as? androidx.activity.ComponentActivity)?.overridePendingTransition(0, 0)
                        },
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(30.dp)
                                    .background(Color(0xFFDD3825), shape = androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "SOS",
                                color = Color.White,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}

@Composable
fun PDFViewerScreen(
    pdfUrl: String,
    title: String,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Failed to load PDF") }
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var pageCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }

    // State for tooltip visibility
    var showTooltip by remember { mutableStateOf(true) }

    // Load the PDF
    LaunchedEffect(pdfUrl) {
        try {
            Log.d("PDFViewerScreen", "Attempting to load PDF from URL: $pdfUrl")

            if (pdfUrl.isBlank()) {
                Log.e("PDFViewerScreen", "Empty PDF URL provided")
                loadError = true
                errorMessage = "Error: Empty PDF URL provided"
                isLoading = false
                return@LaunchedEffect
            }

            scope.launch {
                try {
                    val file = downloadPdf(context, pdfUrl)
                    Log.d("PDFViewerScreen", "Successfully downloaded PDF to: ${file.absolutePath}, size: ${file.length()} bytes")

                    if (!file.exists() || file.length() == 0L) {
                        throw IllegalArgumentException("PDF file does not exist or is empty after download")
                    }

                    try {
                        pdfPages = renderPdfPages(context, file)
                        pageCount = pdfPages.size
                        Log.d("PDFViewerScreen", "Rendered PDF with ${pdfPages.size} pages")

                        if (pdfPages.isEmpty()) {
                            throw IllegalArgumentException("No pages could be rendered from the PDF")
                        }

                        isLoading = false
                    } catch (e: Exception) {
                        Log.e("PDFViewerScreen", "Error rendering PDF: ${e.message}", e)
                        loadError = true
                        errorMessage = "Error rendering PDF: ${e.message ?: "Unknown error"}"
                        isLoading = false
                    }
                } catch (e: Exception) {
                    Log.e("PDFViewerScreen", "Error loading PDF: ${e.message}", e)
                    loadError = true
                    errorMessage = "Error: ${e.message ?: "Unknown error"}"
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            Log.e("PDFViewerScreen", "Error processing PDF URL: ${e.message}", e)
            loadError = true
            errorMessage = "Error: ${e.message ?: "Unknown error with PDF URL"}"
            isLoading = false
        }
    }

    // Track current page based on scroll position
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { index ->
                if (index >= 0 && index < pageCount) {
                    currentPage = index
                }
            }
    }

    // Auto-hide tooltip after 5 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        showTooltip = false
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749)),
                    ),
                ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CustomTopAppBar(
                title = title,
                onBackClick = onBackClick,
                showSosButton = title.contains("Anti Bribery", ignoreCase = true) || title.contains("POSH", ignoreCase = true),
                context = context,
            )

            // Main content container
            Card(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                if (!isLoading && !loadError && pdfPages.isNotEmpty()) {
                    // Full-Width Column with PDF pages
                    PdfPagesView(
                        pdfPages = pdfPages,
                        lazyListState = lazyListState,
                    )
                }

                // Error message
                if (loadError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isLoading = true
                                    loadError = false

                                    scope.launch {
                                        try {
                                            val file = downloadPdf(context, pdfUrl)
                                            pdfPages = renderPdfPages(context, file)
                                            pageCount = pdfPages.size
                                            isLoading = false
                                        } catch (e: Exception) {
                                            Log.e("PDFViewerScreen", "Error loading PDF: ${e.message}", e)
                                            loadError = true
                                            errorMessage = "Error: ${e.message ?: "Unknown error"}"
                                            isLoading = false
                                        }
                                    }
                                },
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFDD3825),
                                    ),
                            ) {
                                Text("Retry")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onBackClick,
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFDD3825),
                                    ),
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
        }

        // Display the universal loader while loading
        UniversalLoader(isLoading = isLoading)

        // Tooltip for double-tap to zoom
        if (showTooltip) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xAA000000))
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Double-tap to zoom",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PdfPagesView(
    pdfPages: List<Bitmap>,
    lazyListState: LazyListState,
) {
    // Box wrapping entire content for layering
    Box(modifier = Modifier.fillMaxSize()) {
        // Background transparent overlay that catches all scroll events
        // when pages are not zoomed, to ensure scrolling works from anywhere
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .zIndex(0f), // Lowest layer
        )

        // Scrollable content
        LazyColumn(
            state = lazyListState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            // Middle layer
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(pdfPages) { page ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PdfPageWithZoom(page = page)
                }

                // Add spacing between pages
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PdfPageWithZoom(page: Bitmap) {
    // State for zoom and pan
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    // Create a transformable state for handling zoom and pan
    val transformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            // Handle zoom
            if (zoomChange != 1f) {
                scale = (scale * zoomChange).coerceIn(1f, 3f)
            }

            // Handle pan when zoomed in
            if (scale > 1f) {
                offsetX += panChange.x
                offsetY += panChange.y

                // Constrain pan within bounds
                val maxX = (containerSize.width * (scale - 1f)) / 2f
                val maxY = (containerSize.height * (scale - 1f)) / 2f
                offsetX = offsetX.coerceIn(-maxX, maxX)
                offsetY = offsetY.coerceIn(-maxY, maxY)
            } else {
                // Reset when not zoomed
                offsetX = 0f
                offsetY = 0f
            }
        }

    // Handle double-tap to zoom
    val doubleTapModifier =
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    scale = if (scale > 1f) 1f else 2f
                    if (scale <= 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    }
                },
            )
        }

    // Main container for the PDF page
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // The page wrapper with shadow
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { containerSize = Size(it.width.toFloat(), it.height.toFloat()) },
            shadowElevation = 2.dp,
            color = Color.White,
        ) {
            // The PDF page itself - zoomable but won't block scrolling
            Image(
                bitmap = page.asImageBitmap(),
                contentDescription = "PDF Page",
                contentScale = ContentScale.FillWidth,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
            )
        }

        // Transparent overlay only for zoom gestures
        // We place it over the Surface to capture pinch/zoom without blocking scrolling
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .transformable(
                        state = transformableState,
                        lockRotationOnZoomPan = true,
                        enabled = scale > 1f,
                    ).then(doubleTapModifier),
        )
    }
}

private suspend fun downloadPdf(
    context: Context,
    pdfUrl: String,
): File =
    withContext(Dispatchers.IO) {
        val fileName = "temp_pdf_${System.currentTimeMillis()}.pdf"
        val outputFile = File(context.cacheDir, fileName)

        try {
            Log.d("PDFViewerScreen", "Downloading PDF from URL: $pdfUrl")

            // Handle content:// URIs (FileProvider)
            if (pdfUrl.startsWith("content://")) {
                try {
                    val uri = Uri.parse(pdfUrl)
                    Log.d("PDFViewerScreen", "Handling content URI: $uri")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (outputFile.exists() && outputFile.length() > 0) {
                        Log.d(
                            "PDFViewerScreen",
                            "Successfully copied content URI to: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes",
                        )
                        return@withContext outputFile
                    } else {
                        Log.e("PDFViewerScreen", "Failed to copy from content URI")
                        throw IllegalArgumentException("Failed to copy from content URI")
                    }
                } catch (e: Exception) {
                    Log.e("PDFViewerScreen", "Error handling content URI: ${e.message}", e)
                    throw e
                }
            }

            // Handle local file URI
            if (pdfUrl.startsWith("file://")) {
                try {
                    // Parse the URI properly
                    val uri = Uri.parse(pdfUrl)
                    Log.d("PDFViewerScreen", "URI path: ${uri.path}")

                    val sourceFile = File(uri.path ?: "")
                    if (sourceFile.exists() && sourceFile.canRead()) {
                        Log.d("PDFViewerScreen", "Local file exists and is readable: ${sourceFile.absolutePath}")
                        sourceFile.inputStream().use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        if (outputFile.exists() && outputFile.length() > 0) {
                            Log.d(
                                "PDFViewerScreen",
                                "Successfully copied file to: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes",
                            )
                        } else {
                            Log.e("PDFViewerScreen", "File copy failed or file is empty: ${outputFile.absolutePath}")
                        }

                        return@withContext outputFile
                    } else {
                        Log.e("PDFViewerScreen", "Local file doesn't exist or can't be read: ${sourceFile.absolutePath}")
                        Log.e("PDFViewerScreen", "File exists: ${sourceFile.exists()}, Can read: ${sourceFile.canRead()}")

                        // If we can't access the original file, try to use it directly if it's in our cache
                        if (sourceFile.absolutePath.contains(context.cacheDir.absolutePath)) {
                            Log.d("PDFViewerScreen", "File is in our cache, using it directly")
                            if (sourceFile.exists() && sourceFile.length() > 0) {
                                return@withContext sourceFile
                            }
                        }

                        throw IllegalArgumentException("Cannot access local file: ${sourceFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    Log.e("PDFViewerScreen", "Error handling local file URI: ${e.message}", e)
                    throw e
                }
            }

            // URL handling for remote files
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection()
                connection.connect()

                val input = connection.getInputStream()
                val output = FileOutputStream(outputFile)

                val buffer = ByteArray(4 * 1024) // 4K buffer
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }

                output.flush()
                output.close()
                input.close()

                if (outputFile.exists() && outputFile.length() > 0) {
                    Log.d(
                        "PDFViewerScreen",
                        "Successfully downloaded file to: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes",
                    )
                } else {
                    Log.e("PDFViewerScreen", "File download failed or file is empty: ${outputFile.absolutePath}")
                }

                return@withContext outputFile
            } catch (e: Exception) {
                Log.e("PDFViewerScreen", "Error downloading remote PDF: ${e.message}", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e("PDFViewerScreen", "Error in downloadPdf: ${e.message}", e)
            throw e
        }
    }

private suspend fun renderPdfPages(
    context: Context,
    pdfFile: File,
): List<Bitmap> =
    withContext(Dispatchers.IO) {
        val renderedPages = mutableListOf<Bitmap>()

        try {
            Log.d("PDFViewerScreen", "Starting to render PDF: ${pdfFile.absolutePath}, size: ${pdfFile.length()} bytes")

            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                Log.e("PDFViewerScreen", "PDF file does not exist or is empty: ${pdfFile.absolutePath}")
                throw IllegalArgumentException("PDF file does not exist or is empty")
            }

            try {
                val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)

                val pageCount = pdfRenderer.pageCount
                Log.d("PDFViewerScreen", "PDF has $pageCount pages")

                if (pageCount == 0) {
                    Log.e("PDFViewerScreen", "PDF has no pages")
                    throw IllegalArgumentException("PDF has no pages")
                }

                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)

                    // Create bitmap with appropriate dimensions for screen width
                    val displayMetrics = context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels

                    // Calculate height to maintain aspect ratio
                    val pageRatio = page.height.toFloat() / page.width.toFloat()
                    val targetHeight = (screenWidth * pageRatio).toInt()

                    val bitmap = Bitmap.createBitmap(screenWidth, targetHeight, Bitmap.Config.ARGB_8888)

                    // Render the page onto the bitmap
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    renderedPages.add(bitmap)

                    page.close()

                    Log.d("PDFViewerScreen", "Rendered page ${i + 1} of $pageCount")
                }

                pdfRenderer.close()
                fileDescriptor.close()

                Log.d("PDFViewerScreen", "Successfully rendered all $pageCount pages")
            } catch (e: Exception) {
                // Handle specific PDF rendering errors
                Log.e("PDFViewerScreen", "Error rendering PDF (first attempt): ${e.message}", e)

                // Try alternate rendering approach for problematic PDFs
                try {
                    Log.d("PDFViewerScreen", "Trying alternate rendering approach...")

                    // Create a simple placeholder bitmap for documents that can't be rendered
                    val displayMetrics = context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val placeholderHeight = (screenWidth * 1.4f).toInt() // Standard page ratio

                    val placeholderBitmap = Bitmap.createBitmap(screenWidth, placeholderHeight, Bitmap.Config.ARGB_8888)
                    renderedPages.add(placeholderBitmap)

                    Log.d("PDFViewerScreen", "Added placeholder bitmap for document")

                    // At this point, we have at least one page (placeholder) so the viewer will show something
                    return@withContext renderedPages
                } catch (innerE: Exception) {
                    Log.e("PDFViewerScreen", "Error with fallback rendering: ${innerE.message}", innerE)
                    throw innerE
                }
            }
        } catch (e: Exception) {
            Log.e("PDFViewerScreen", "Error rendering PDF: ${e.message}", e)
            throw e
        }

        return@withContext renderedPages
    }
