package com.archeGlobal.one.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.CommuniqueModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Warning
import com.archeGlobal.one.ui.components.UniversalLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

private const val THUMBNAIL_WIDTH = 300 // unified thumbnail width for both remote and PDF

// Cache for PDF bitmaps to avoid re-rendering
private val communiqueThumbnailCache = ConcurrentHashMap<String, Bitmap?>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuniqueScreen(
    model: CommuniqueModel,
    onCommuniqueClick: (CommuniqueModel.Communique) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false
) {
    val context = LocalContext.current

    // Shared map of preloaded thumbnails keyed by filePath
    val preloadedThumbnails = remember { mutableStateMapOf<String, Bitmap?>() }

    // Preload thumbnails concurrently when communique list changes
    LaunchedEffect(model.communiques) {
        // Only preload those without previewUrl and not already cached
        val toLoad = model.communiques.filter {
            it.previewUrl.isNullOrEmpty() && preloadedThumbnails[it.filePath] == null
        }
        if (toLoad.isNotEmpty()) {
            val results = toLoad.map { communique ->
                async(Dispatchers.IO) {
                    communique.filePath to getPdfThumbnail(context, communique.filePath)
                }
            }.awaitAll()
            results.forEach { (path, bmp) -> preloadedThumbnails[path] = bmp }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Communique",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            
            if (!isLoading) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(model.communiques) { communique ->
                        CommuniqueCard(
                            communique = communique,
                            preloadedThumbnail = preloadedThumbnails[communique.filePath],
                            onClick = { onCommuniqueClick(communique) }
                        )
                    }
                }
            }
        }
        
        UniversalLoader(isLoading = isLoading)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommuniqueCard(
    communique: CommuniqueModel.Communique,
    preloadedThumbnail: Bitmap?,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // State for loading when not preloaded
    var thumbnail by remember { mutableStateOf<Bitmap?>(preloadedThumbnail) }
    var isLoading by remember { mutableStateOf(preloadedThumbnail == null && communique.previewUrl.isNullOrEmpty()) }

    val hasPreview = !communique.previewUrl.isNullOrEmpty()

    // If no preview and not preloaded yet, launch load
    if (!hasPreview && thumbnail == null) {
        LaunchedEffect(communique.filePath) {
            thumbnail = getPdfThumbnail(context, communique.filePath)
            isLoading = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f), // Increased height for larger preview
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (hasPreview) {
                    // Remote preview using Coil's AsyncImage
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(communique.previewUrl)
                            .crossfade(true)
                            .size(THUMBNAIL_WIDTH)
                            .build(),
                        placeholder = painterResource(id = R.drawable.ic_policy_default),
                        error = painterResource(id = R.drawable.ic_policy_default),
                        contentDescription = communique.communiqueName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        thumbnail?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = communique.communiqueName,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: Icon(
                            painter = painterResource(id = R.drawable.ic_policy_default),
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = communique.communiqueName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
        )
    }
}

/**
 * Downloads a PDF from a URL and generates a thumbnail from its first page
 */
private suspend fun getPdfThumbnail(context: Context, pdfUrl: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        // Return cached bitmap if present (do NOT cache null values)
        communiqueThumbnailCache[pdfUrl]?.let {
            Log.d("CommuniqueThumbnail", "Using cached thumbnail for $pdfUrl")
            return@withContext it
        }
        Log.d("CommuniqueThumbnail", "Generating thumbnail for $pdfUrl")
        // Download PDF file to cache directory
        val tempFile = downloadPdfToTemp(context, pdfUrl)
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
            Log.e("CommuniqueThumbnail", "Failed to download PDF from $pdfUrl")
            return@withContext null // Do not cache failures
        }
        // Render the first page as a thumbnail
        val thumbnail = renderPdfThumbnail(context, tempFile)
        // Clean up the temp file
        tempFile.delete()
        // Cache the bitmap only if successfully created
        if (thumbnail != null) {
            communiqueThumbnailCache[pdfUrl] = thumbnail
        }
        return@withContext thumbnail
    } catch (e: Exception) {
        Log.e("CommuniqueThumbnail", "Error creating thumbnail from $pdfUrl", e)
        return@withContext null // Do not cache failures
    }
}

/**
 * Downloads PDF from a URL to a temporary file
 */
private suspend fun downloadPdfToTemp(context: Context, pdfUrl: String): File? = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        val fileName = "temp_pdf_${System.currentTimeMillis()}.pdf"
        val outputFile = File(context.cacheDir, fileName)
        
        val url = URL(pdfUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000 // Reduced from 15000
        connection.readTimeout = 10000 // Reduced from 15000
        
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e("CommuniqueThumbnail", "HTTP error code: ${connection.responseCode}")
            return@withContext null
        }
        
        connection.inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        
        if (outputFile.exists() && outputFile.length() > 0) {
            Log.d("CommuniqueThumbnail", "PDF downloaded successfully to ${outputFile.absolutePath}")
            return@withContext outputFile
        } else {
            Log.e("CommuniqueThumbnail", "Downloaded file is empty or doesn't exist")
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e("CommuniqueThumbnail", "Error downloading PDF: ${e.message}", e)
        return@withContext null
    } finally {
        connection?.disconnect()
    }
}

/**
 * Renders the first page of a PDF as a thumbnail
 */
private fun renderPdfThumbnail(context: Context, pdfFile: File): Bitmap? {
    var fileDescriptor: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null
    var page: PdfRenderer.Page? = null
    try {
        fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
        if (pdfRenderer.pageCount == 0) {
            Log.e("CommuniqueThumbnail", "PDF has no pages")
            return null
        }
        // Get the first page
        page = pdfRenderer.openPage(0)
        // Safely obtain page dimensions
        var pageWidth = page?.width ?: 0
        var pageHeight = page?.height ?: 0
        if (pageWidth <= 0 || pageHeight <= 0) {
            Log.w("CommuniqueThumbnail", "Page reported zero width/height. Using fallback dimensions.")
            pageWidth = 595  // A4 width in points at 72 dpi
            pageHeight = 842 // A4 height in points at 72 dpi
        }
        // Create a scaled bitmap (fixed thumbnail width for consistency)
        val thumbnailWidth = THUMBNAIL_WIDTH
        val pageRatio = pageHeight.toFloat() / pageWidth.toFloat()
        val thumbnailHeight = maxOf(1, (thumbnailWidth * pageRatio).toInt()) // ensure > 0
        val bitmap = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888)
        // Render the page to the bitmap
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        Log.d("CommuniqueThumbnail", "Successfully rendered thumbnail with dimensions ${bitmap.width}x${bitmap.height}")
        return bitmap
    } catch (e: Exception) {
        Log.e("CommuniqueThumbnail", "Error rendering PDF", e)
        return null
    } finally {
        try {
            page?.close()
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (e: Exception) {
            Log.e("CommuniqueThumbnail", "Error closing resources: ${e.message}", e)
        }
    }
} 