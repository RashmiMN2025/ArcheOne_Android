package com.archeGlobal.one

import android.os.Bundle
import android.util.Log
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.draw.clipToBounds
import androidx.core.view.WindowCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.XOneTheme
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Renders a remote PDF using a native PDF library (PdfiumAndroid via
 * com.github.barteksc:android-pdf-viewer). Much faster than the PDF.js
 * WebView path that [WebViewActivity] uses - skip JS bootstrap and canvas
 * rendering, ~200ms first paint instead of several seconds.
 */
class PdfViewerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FILE_URL = "fileUrl"
        const val EXTRA_TITLE = "title"
        private const val TAG = "PdfViewerActivity"
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Window background renders behind everything (incl. system bar area while
        // PDFView animations momentarily over-draw their Compose bounds).
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
        // Status bar sits on a black strip - force white icons regardless of system theme.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val fileUrl = intent.getStringExtra(EXTRA_FILE_URL).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Document"
        Log.d(TAG, "Loading PDF: $fileUrl")

        setContent {
            XOneTheme(darkTheme = false, dynamicColor = false) {
                var localFile by remember { mutableStateOf<File?>(null) }
                var error by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(fileUrl) {
                    if (fileUrl.isBlank()) {
                        error = "No PDF URL provided"
                        return@LaunchedEffect
                    }
                    val downloaded = downloadPdf(fileUrl)
                    if (downloaded != null) {
                        localFile = downloaded
                    } else {
                        error = "Failed to load PDF"
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                  // Inner content sits below the status bar; the black behind the
                  // status bar comes from the outer Box. clipToBounds keeps the
                  // PDFView from overdrawing into the status bar area while scrolling.
                  Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .clipToBounds()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    WelcomeBackgroundTop,
                                    WelcomeBackgroundMiddle,
                                    WelcomeBackgroundBottom,
                                ),
                            ),
                        ),
                ) {
                    // PDF content area starts below the fixed header (TopAppBar is 64dp).
                    // clipToBounds keeps PDFView's scroll over-draw from bleeding behind
                    // the transparent header.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 64.dp)
                            .clipToBounds(),
                    ) {
                        when {
                            error != null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = error ?: "",
                                        color = Color.Black,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(24.dp),
                                    )
                                }
                            }
                            localFile != null -> {
                                PdfRenderer(
                                    file = localFile!!,
                                    onError = { msg -> error = msg },
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    UniversalLoader(isLoading = true)
                                }
                            }
                        }
                    }

                    // Fixed header overlay drawn on top of the PDF area. Transparent so
                    // the parent Box's 3-stop gradient flows continuously through it.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                    ) {
                        TopAppBar(
                            title = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                        maxLines = 1,
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.Black,
                                    )
                                }
                            },
                            actions = {
                                Spacer(modifier = Modifier.width(48.dp))
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                        )
                    }
                  }
                }
            }
        }
    }

    private suspend fun downloadPdf(url: String): File? = withContext(Dispatchers.IO) {
        try {
            val target = File(cacheDir, "communique_${url.hashCode()}.pdf")
            if (target.exists() && target.length() > 0) {
                Log.d(TAG, "Using cached PDF: ${target.absolutePath}")
                return@withContext target
            }
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "PDF download failed: HTTP ${response.code}")
                    return@withContext null
                }
                response.body?.byteStream()?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext null
            }
            Log.d(TAG, "Downloaded PDF (${target.length()} bytes) to ${target.absolutePath}")
            target
        } catch (e: Exception) {
            Log.e(TAG, "PDF download error: ${e.message}", e)
            null
        }
    }
}

@Composable
private fun PdfRenderer(
    file: File,
    onError: (String) -> Unit,
) {
    // The PDF load is fired once in the factory; if the parent recomposes we don't
    // want to reset the view. Keep onError in an updated ref so the callback the
    // factory captured still hits the latest state setter.
    val errorRef = androidx.compose.runtime.rememberUpdatedState(onError)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PDFView(ctx, null).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                fromFile(file)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .enableAnnotationRendering(true)
                    .scrollHandle(null)
                    .spacing(8)
                    .onError { t ->
                        Log.e("PdfRenderer", "PDF render error: ${t.message}", t)
                        errorRef.value.invoke("Could not display this PDF")
                    }
                    .load()
            }
        },
    )
}
