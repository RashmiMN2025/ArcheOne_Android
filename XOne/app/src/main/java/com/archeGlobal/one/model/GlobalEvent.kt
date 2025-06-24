package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GlobalEvent(
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String, // Format: "DD-MM-YYYY"
    @SerializedName("image") val image: String? = null, // URL to the event image
    @SerializedName("description") val description: String = ""
) {
    // Convert date format if needed to ensure it's in DD-MM-YYYY format
    private fun getFormattedDate(): String {
        return try {
            // First try parsing with the expected format
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val parsedDate = LocalDate.parse(date, formatter)
            // Return in the correct format
            parsedDate.format(formatter)
        } catch (e: Exception) {
            // If parsing fails, return the original date
            date
        }
    }

    // Derived property for fromDate to maintain compatibility with existing code
    val fromDate: String
        get() = getFormattedDate()

    // Derived property for toDate (same as fromDate for single-day events)
    val toDate: String
        get() = getFormattedDate()

    val month: Int
        get() = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val localDate = LocalDate.parse(fromDate, formatter)
            localDate.monthValue
        } catch (e: Exception) {
            0
        }

    val day: Int
        get() = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val localDate = LocalDate.parse(fromDate, formatter)
            localDate.dayOfMonth
        } catch (e: Exception) {
            0
        }

    val isMultiDay: Boolean
        get() = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val startDate = LocalDate.parse(fromDate, formatter)
            val endDate = LocalDate.parse(toDate, formatter)
            startDate != endDate
        } catch (e: Exception) {
            false
        }

    val durationInDays: Int
        get() = try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val startDate = LocalDate.parse(fromDate, formatter)
            val endDate = LocalDate.parse(toDate, formatter)
            (endDate.toEpochDay() - startDate.toEpochDay() + 1).toInt()
        } catch (e: Exception) {
            1
        }
}
