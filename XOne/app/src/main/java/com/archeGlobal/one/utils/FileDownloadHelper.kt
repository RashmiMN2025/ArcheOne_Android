package com.archeGlobal.one.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileDownloadHelper(
    private val context: Context,
) {
    companion object {
        private const val TAG = "FileDownloadHelper"
        private const val AUTHORITY = "com.archeGlobal.one.fileprovider"
    }

    data class DownloadResult(
        val success: Boolean,
        val filePath: String?,
        val errorMessage: String?,
    )

    fun saveCSVFile(
        responseBody: ResponseBody,
        category: String,
        location: String,
        isUsage: Boolean,
    ): DownloadResult =
        try {
            val fileName = generateFileName(category, location, isUsage)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveFileToDownloads(responseBody, fileName)
            } else {
                saveFileToExternalStorage(responseBody, fileName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving CSV file", e)
            DownloadResult(false, null, "Failed to save file: ${e.message}")
        }

    private fun generateFileName(
        category: String,
        location: String,
        isUsage: Boolean,
    ): String {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val dateString = dateFormatter.format(Date())

        val safeCategory = category.replace(" ", "_")
        val safeLocation = location.replace(" ", "_")
        val reportType = if (isUsage) "MonthlyUsageReport" else "StockReport"

        return "${safeCategory}_${reportType}_${safeLocation}_$dateString.csv"
    }

    private fun saveFileToDownloads(
        responseBody: ResponseBody,
        fileName: String,
    ): DownloadResult =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues =
                android.content.ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    responseBody.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d(TAG, "File saved successfully to Downloads: $fileName")
                DownloadResult(true, Environment.DIRECTORY_DOWNLOADS + "/" + fileName, null)
            } else {
                DownloadResult(false, null, "Failed to create file in Downloads")
            }
        } else {
            saveFileToExternalStorage(responseBody, fileName)
        }

    private fun saveFileToExternalStorage(
        responseBody: ResponseBody,
        fileName: String,
    ): DownloadResult {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            responseBody.byteStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        Log.d(TAG, "File saved successfully to: ${file.absolutePath}")
        return DownloadResult(true, file.absolutePath, null)
    }

    fun openFileLocation(filePath: String?) {
        try {
            if (filePath != null) {
                val file = File(filePath)

                if (file.exists()) {
                    val uri =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(context, AUTHORITY, file)
                        } else {
                            Uri.fromFile(file)
                        }

                    val intent =
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "text/csv")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        openDownloadsFolder()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file location", e)
            openDownloadsFolder()
        }
    }

    private fun openDownloadsFolder() {
        try {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload"),
                        "resource/folder",
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Downloads folder", e)
        }
    }

    fun shareFile(filePath: String?) {
        try {
            if (filePath != null) {
                val file = File(filePath)

                if (file.exists()) {
                    val uri =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(context, AUTHORITY, file)
                        } else {
                            Uri.fromFile(file)
                        }

                    val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Consumption Report")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                    val chooser = Intent.createChooser(shareIntent, "Share CSV Report")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
        }
    }
}
