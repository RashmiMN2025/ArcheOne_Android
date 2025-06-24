package com.archeGlobal.one.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility class for generating QR codes for business cards
 */
object QRCodeGenerator {

    // Enum to define layout types
    enum class QRLayoutType {
        VERTICAL,
        HORIZONTAL
    }

    /**
     * Generates a QR code bitmap from the provided business card details
     * * @param name Person's name
     * @param title Job title/designation
     * @param email Email address
     * @param phone Phone number
     * @param location Location
     * @param layoutType Layout type (vertical or horizontal)
     * @param size Size of the QR code (width and height in pixels)
     * @return Bitmap containing the QR code, or null if generation fails
     */
    fun generateQRCode(
        name: String,
        title: String,
        email: String,
        phone: String,
        location: String,
        layoutType: QRLayoutType,
        size: Int = 300
    ): Bitmap? {
        try {
            val url = generateQRUrl(name, title, email, phone, location, layoutType)

            // Generate QR code bitmap using ZXing
            val bitMatrix = MultiFormatWriter().encode(
                url,
                BarcodeFormat.QR_CODE,
                size,
                size
            )

            return createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Generates the URL for the QR code
     */
    private fun generateQRUrl(
        name: String,
        title: String,
        email: String,
        phone: String,
        location: String,
        layoutType: QRLayoutType
    ): String {
        val baseUrl = if (layoutType == QRLayoutType.VERTICAL) {
            "https://pulse.netcon.in:7000/bcard/vertical"
        } else {
            "https://pulse.netcon.in:7000/bcard/horizontal"
        }

        val queryParams = mapOf(
            "name" to name,
            "title" to title,
            "email" to email,
            "phone" to phone,
            "location" to location
        )

        val queryString = queryParams.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
        }

        return "$baseUrl?$queryString"
    }

    /**
     * Converts a BitMatrix to a Bitmap
     */
    private fun createBitmap(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
