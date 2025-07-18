package com.archeGlobal.one.ui.screens

import com.archeGlobal.one.model.TravelStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper function to map string status to TravelStatus enum
 */
fun mapStringToTravelStatus(status: String): TravelStatus {
    return when (status.lowercase()) {
        "approved" -> TravelStatus.APPROVED
        "rejected" -> TravelStatus.REJECTED
        else -> TravelStatus.PENDING
    }
}

/**
 * Helper function to format date strings
 */
fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        // If parsing fails, try to format the date as-is or return original
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}