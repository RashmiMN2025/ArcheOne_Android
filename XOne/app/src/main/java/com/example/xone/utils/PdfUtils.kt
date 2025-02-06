package com.example.xone.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    fun openPdfFromAssets(context: Context, fileName: String) {
        try {
            // Copy file from assets to cache
            val file = File(context.cacheDir, fileName)
            if (!file.exists()) {
                val inputStream = context.assets.open(fileName)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }

            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            // Create intent to view PDF
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Start activity to view PDF
            context.startActivity(
                Intent.createChooser(intent, "Open PDF with...")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error - maybe show a toast or snackbar
        }
    }
} 