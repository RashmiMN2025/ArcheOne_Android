package com.archeGlobal.one.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date formatting in the XOne application
 */
object DateFormatter {
    
    /**
     * Parses a date string from API and returns a Date object
     * Supports both "yyyy-MM-dd" and "yyyy-MM-dd HH:mm:ss" formats
     * 
     * @param dateString Date string from API
     * @return Parsed Date object or current date if parsing fails
     */
    fun parseApiDate(dateString: String?): Date {
        if (dateString.isNullOrEmpty()) {
            return Date()
        }
        
        return try {
            // Try parsing as date-only format first (yyyy-MM-dd)
            val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateOnlyFormat.parse(dateString)
        } catch (e: Exception) {
            try {
                // Fallback to date-time format (yyyy-MM-dd HH:mm:ss)
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                dateTimeFormat.parse(dateString)
            } catch (e2: Exception) {
                // Return current date if both parsing attempts fail
                Date()
            }
        } ?: Date()
    }
    
    /**
     * Formats a date string from API format (YYYY-MM-DD) to display format (D MMM YYYY)
     * 
     * @param dateString Date string in format "2025-09-01"
     * @return Formatted date string like "1 Sep 2025" or original string if parsing fails
     */
    fun formatTravelDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) {
            return ""
        }
        
        return try {
            // Parse the input date string (YYYY-MM-DD format)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            
            // Format to display format (D MMM YYYY)
            val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            // Return original string if parsing fails
            dateString
        }
    }
    
    /**
     * Formats a Date object to display format (D MMM YYYY)
     * 
     * @param date Date object to format
     * @return Formatted date string like "7 Jul 2025"
     */
    fun formatDisplayDate(date: Date?): String {
        if (date == null) {
            return ""
        }
        
        return try {
            val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Formats a date range from API format to display format
     * 
     * @param departureDate Departure date in format "2025-09-01"
     * @param arrivalDate Arrival date in format "2025-09-05"
     * @return Formatted date range like "1 Sep 2025 - 5 Sep 2025"
     */
    fun formatTravelDateRange(departureDate: String?, arrivalDate: String?): String {
        val formattedDeparture = formatTravelDate(departureDate)
        val formattedArrival = formatTravelDate(arrivalDate)
        
        return if (formattedDeparture.isNotEmpty() && formattedArrival.isNotEmpty()) {
            "$formattedDeparture - $formattedArrival"
        } else if (formattedDeparture.isNotEmpty()) {
            formattedDeparture
        } else if (formattedArrival.isNotEmpty()) {
            formattedArrival
        } else {
            ""
        }
    }
}