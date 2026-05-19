package com.archeGlobal.one

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.XOneTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Renders a remote PDF using Android's built-in [android.graphics.pdf.PdfRenderer]
 * (API 21+). No third-party native libraries, so the resulting APK has no extra
 * .so files to keep 16 KB page-size aligned for Play Store.
 *
 * Pages are rendered to bitmaps lazily as they enter the [LazyColumn] viewport.
 * Access to the renderer is serialized through a [Mutex] — PdfRenderer cannot
 * have multiple pages open simultaneously.
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
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
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
                                    PdfPagesView(
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
private fun PdfPagesView(
    file: File,
    onError: (String) -> Unit,
) {
    val errorRef = rememberUpdatedState(onError)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }.toInt()

    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    val mutex = remember { Mutex() }

    DisposableEffect(file) {
        try {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val r = PdfRenderer(fd)
            fileDescriptor = fd
            renderer = r
            pageCount = r.pageCount
            if (pageCount == 0) {
                errorRef.value.invoke("Empty PDF")
            }
        } catch (e: Exception) {
            Log.e("PdfPagesView", "Failed to open PDF: ${e.message}", e)
            errorRef.value.invoke("Could not display this PDF")
        }
        onDispose {
            try { renderer?.close() } catch (_: Exception) {}
            try { fileDescriptor?.close() } catch (_: Exception) {}
            renderer = null
            fileDescriptor = null
        }
    }

    val r = renderer
    if (r != null && pageCount > 0) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(pageCount) { index ->
                PdfPage(
                    renderer = r,
                    pageIndex = index,
                    targetWidthPx = screenWidthPx,
                    mutex = mutex,
                )
            }
        }
    }
}

@Composable
private fun PdfPage(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int,
    mutex: Mutex,
) {
    var bitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }
    var pageAspect by remember(pageIndex) { mutableStateOf<Float?>(null) }

    LaunchedEffect(pageIndex, targetWidthPx) {
        withContext(Dispatchers.IO) {
            try {
                mutex.withLock {
                    val page = renderer.openPage(pageIndex)
                    try {
                        val targetHeight =
                            (page.height.toFloat() / page.width.toFloat() * targetWidthPx).toInt()
                        val bmp = Bitmap.createBitmap(
                            targetWidthPx,
                            targetHeight,
                            Bitmap.Config.ARGB_8888,
                        )
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pageAspect = page.width.toFloat() / page.height.toFloat()
                        bitmap = bmp
                    } finally {
                        page.close()
                    }
                }
            } catch (e: Exception) {
                Log.e("PdfPage", "Failed to render page $pageIndex: ${e.message}", e)
            }
        }
    }

    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Page ${pageIndex + 1}",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    } else {
        // Placeholder while loading - use the page's aspect ratio if known,
        // otherwise approximate A4 (1 / sqrt(2) ≈ 0.707).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(pageAspect ?: 0.707f)
                .background(Color.White),
        )
    }
}
