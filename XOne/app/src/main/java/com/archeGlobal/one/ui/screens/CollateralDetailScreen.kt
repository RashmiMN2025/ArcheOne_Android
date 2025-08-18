package com.archeGlobal.one.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.archeGlobal.one.R
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.controller.CollateralController
import com.archeGlobal.one.network.SmartCollateralFile
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap

private const val THUMBNAIL_WIDTH = 600
private val collateralThumbnailCache = mutableStateMapOf<String, Bitmap?>()

private suspend fun getPdfThumbnail(context: android.content.Context, pdfUrl: String): Bitmap? = withContext(Dispatchers.IO) {
    // Return cached bitmap if present (do NOT cache null values)
    collateralThumbnailCache[pdfUrl]?.let { return@withContext it }

    try {
        val tempFile = downloadPdfToTemp(context, pdfUrl)
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) return@withContext null

        val thumbnail = renderPdfThumbnail(context, tempFile)
        tempFile.delete()
        if (thumbnail != null) {
            collateralThumbnailCache[pdfUrl] = thumbnail
        }
        return@withContext thumbnail
    } catch (e: Exception) {
        return@withContext null
    }
}

private suspend fun downloadPdfToTemp(context: android.content.Context, pdfUrl: String): File? = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        val fileName = "temp_pdf_${System.currentTimeMillis()}.pdf"
        val outputFile = File(context.cacheDir, fileName)
        val url = URL(pdfUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 10000
        if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null

        connection.inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        if (outputFile.exists() && outputFile.length() > 0) return@withContext outputFile
        return@withContext null
    } catch (e: Exception) {
        return@withContext null
    } finally {
        connection?.disconnect()
    }
}

private fun renderPdfThumbnail(context: android.content.Context, pdfFile: File): Bitmap? {
    var fileDescriptor: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null
    var page: PdfRenderer.Page? = null
    try {
        fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
        if (pdfRenderer.pageCount == 0) return null

        page = pdfRenderer.openPage(0)
        var pageWidth = page.width
        var pageHeight = page.height
        if (pageWidth <= 0 || pageHeight <= 0) {
            pageWidth = 595; pageHeight = 842 // Fallback to A4 size
        }

        val thumbnailWidth = THUMBNAIL_WIDTH
        val pageRatio = pageHeight.toFloat() / pageWidth.toFloat()
        val thumbnailHeight = maxOf(1, (thumbnailWidth * pageRatio).toInt())
        val bitmap = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    } catch (e: Exception) {
        return null
    } finally {
        try { page?.close(); pdfRenderer?.close(); fileDescriptor?.close() } catch (_: Exception) {}
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollateralDetailScreen(
    categoryName: String,
    files: List<SmartCollateralFile>,
    controller: CollateralController,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749) // Dark Gray
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                categoryName,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(files) { file ->
                        val context = LocalContext.current
                        var thumbnail by remember(file.fileUrl) { mutableStateOf(collateralThumbnailCache[file.fileUrl]) }
                        var isLoading by remember(file.fileUrl) { mutableStateOf(false) }

                        LaunchedEffect(file.fileUrl) {
                            if (file.fileUrl.endsWith(".pdf", ignoreCase = true) && thumbnail == null && !isLoading) {
                                isLoading = true
                                val thumbBmp = getPdfThumbnail(context, file.fileUrl)
                                thumbnail = thumbBmp
                                isLoading = false
                            }
                        }

                        // OUTER Column to put Text below Card
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // CARD: Thumbnail fills Card completely
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { controller.onFileClick(file) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                when {
                                    !file.thumbnailUrl.isNullOrEmpty() -> {
                                        AsyncImage(
                                            model = file.thumbnailUrl,
                                            contentDescription = file.fileName,
                                            modifier = Modifier
                                                .fillMaxSize(), // Fill Card (180.dp x full width)
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(id = R.drawable.ic_doc),
                                            error = painterResource(id = R.drawable.ic_doc)
                                        )
                                    }
                                    thumbnail != null -> {
                                        Image(
                                            bitmap = thumbnail!!.asImageBitmap(),
                                            contentDescription = file.fileName,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.fillMaxSize() // Fill Card
                                        )
                                    }
                                    isLoading -> {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                    else -> {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_doc),
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                            // File name BELOW the Card
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = file.fileName ?: "",
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}