package com.archeGlobal.one.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.archeGlobal.one.R
import com.archeGlobal.one.model.PolicyModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.archeGlobal.one.ui.components.UniversalLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import com.archeGlobal.one.ui.theme.GraphikFontFamily

// Cache for PDF bitmaps to avoid re-rendering
private val pdfThumbnailCache = ConcurrentHashMap<String, Bitmap?>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    model: PolicyModel,
    onPolicyClick: (PolicyModel.Policy) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") } // State for search query

    // Filter policies based on the search query
    val filteredPolicies = model.policies.filter { policy ->
    policy.policyName.contains(searchQuery, ignoreCase = true)
}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Policies",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Bold,
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

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(width = 1.dp, color = Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            singleLine = true,
                            textStyle = TextStyle( // Added textStyle for innerTextField
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search policies...",
                                            color = Color.Gray.copy(alpha = 0.6f),
                                            fontSize = 16.sp,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Policy List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (filteredPolicies.isEmpty()) {
                    // Show a message if no policies match the search query
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No policies found",
                            fontSize = 16.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredPolicies, key = { it.filePath }) { policy ->
                            PolicyCard(policy = policy, onClick = { onPolicyClick(policy) })
                        }
                    }
                }
            }
        }

        UniversalLoader(isLoading = isLoading)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PolicyCard(
    policy: PolicyModel.Policy,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(policy.filePath) {
        isLoading = true
        thumbnail = getPdfThumbnail(context, policy.filePath)
        isLoading = false
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp) // Add padding around the card
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(0.7f) // Adjust aspect ratio for the card
        ) {
            Card(
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        thumbnail?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = policy.policyName,
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

            // SOS Circle Tag for Specific Policies
            if (policy.policyName == "Anti Bribery and Anti Corruption Policy" || policy.policyName == "PoSH Policy") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Align the SOS tag to the top-right corner
                        .offset(x = (-8).dp, y = 8.dp) // Adjust position slightly
                        .size(24.dp) // Size of the SOS circle
                        .background(color = Color(0xFFE94235), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SOS",
                        fontSize = 10.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

            Text(
                text = policy.policyName,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 12.sp,
                maxLines = 2,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }

/**
 * Downloads a PDF from a URL and generates a thumbnail from its first page
 */
private suspend fun getPdfThumbnail(context: Context, pdfUrl: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        // Return cached bitmap if present (do NOT cache null values)
        pdfThumbnailCache[pdfUrl]?.let {
            Log.d("PolicyThumbnail", "Using cached thumbnail for $pdfUrl")
            return@withContext it
        }
        Log.d("PolicyThumbnail", "Generating thumbnail for $pdfUrl")
        // Download PDF file to cache directory
        val tempFile = downloadPdfToTemp(context, pdfUrl)
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
            Log.e("PolicyThumbnail", "Failed to download PDF from $pdfUrl")
            return@withContext null // Do not cache failures
        }
        // Render the first page as a thumbnail
        val thumbnail = renderPdfThumbnail(context, tempFile)
        // Clean up the temp file
        tempFile.delete()
        // Cache the bitmap only if successfully created
        if (thumbnail != null) {
            pdfThumbnailCache[pdfUrl] = thumbnail
        }
        return@withContext thumbnail
    } catch (e: Exception) {
        Log.e("PolicyThumbnail", "Error creating thumbnail from $pdfUrl", e)
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
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e("PolicyThumbnail", "HTTP error code: ${connection.responseCode}")
            return@withContext null
        }
        
        connection.inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        
        if (outputFile.exists() && outputFile.length() > 0) {
            Log.d("PolicyThumbnail", "PDF downloaded successfully to ${outputFile.absolutePath}")
            return@withContext outputFile
        } else {
            Log.e("PolicyThumbnail", "Downloaded file is empty or doesn't exist")
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e("PolicyThumbnail", "Error downloading PDF: ${e.message}", e)
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
            Log.e("PolicyThumbnail", "PDF has no pages")
            return null
        }
        // Get the first page
        page = pdfRenderer.openPage(0)
        // Safely obtain page dimensions
        var pageWidth = page?.width ?: 0
        var pageHeight = page?.height ?: 0
        if (pageWidth <= 0 || pageHeight <= 0) {
            Log.w("PolicyThumbnail", "Page reported zero width/height. Using fallback dimensions.")
            pageWidth = 595  // A4 width in points at 72 dpi
            pageHeight = 842 // A4 height in points at 72 dpi
        }
        // Create a scaled bitmap (fixed thumbnail width for consistency)
        val thumbnailWidth = 500
        val pageRatio = pageHeight.toFloat() / pageWidth.toFloat()
        val thumbnailHeight = maxOf(1, (thumbnailWidth * pageRatio).toInt()) // ensure > 0
        val bitmap = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888)
        // Render the page to the bitmap
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        Log.d("PolicyThumbnail", "Successfully rendered thumbnail with dimensions ${bitmap.width}x${bitmap.height}")
        return bitmap
    } catch (e: Exception) {
        Log.e("PolicyThumbnail", "Error rendering PDF", e)
        return null
    } finally {
        try {
            page?.close()
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (e: Exception) {
            Log.e("PolicyThumbnail", "Error closing resources: ${e.message}", e)
        }
    }
}
