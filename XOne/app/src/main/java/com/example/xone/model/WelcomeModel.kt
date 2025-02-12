package com.example.xone.model

import java.util.*

data class WelcomeModel(
    val title: String = "Welcome to",
    val subtitle: String = "XOne",
    val description1: String = "Your one-stop solution",
    val description2: String = "for everything",
    val buttons: List<Button> = listOf(
        Button("Login to XOne"),
        Button("Login to Pulse")
    )
) {
    data class Button(val text: String)

    companion object {
        // Function to determine the dynamic subtitle based on the time of day
        fun getTimeBasedGreeting(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour in 6..11 -> "Good morning!"
                hour in 12..17 -> "Good afternoon!"
                hour in 18..21 -> "Good evening!"
                else -> "Good night!"
            }
        }
    }
}
