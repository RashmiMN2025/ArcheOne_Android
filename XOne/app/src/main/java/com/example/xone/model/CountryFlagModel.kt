package com.example.xone.model

data class CountryFlagModel(
    val countryCode: String,
    val flagEmoji: String
)

fun getCountryFlag(countryName: String): String {
    return when (countryName) {
        "USA" -> "🇺🇸"
        "India" -> "🇮🇳"
        "Dubai" -> "🇦🇪"
        "Singapore" -> "🇸🇬"
        "Africa" -> "🇺🇬" // Using Uganda flag since that's the specific location
        "Latin America" -> "🇵🇪" // Using Peru flag since that's the specific location
        else -> "🌎"
    }
} 