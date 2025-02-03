package com.example.xone.model

data class WelcomeModel(
    val title: String = "Hi there!",
    val subtitle: String = "Good morning!",
    val description1: String = "Your journey to XOne",
    val description2: String = "begins here!",
    val buttons: List<ButtonModel> = listOf(
        ButtonModel("XOne"),
        ButtonModel("Pulse")
    )
)

data class ButtonModel(
    val text: String
) 