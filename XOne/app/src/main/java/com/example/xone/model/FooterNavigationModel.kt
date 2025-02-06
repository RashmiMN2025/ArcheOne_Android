package com.example.xone.model

data class FooterNavigationModel(
    val items: List<FooterNavigationItem> = listOf(
        FooterNavigationItem("Home", "home_icon", true),
        FooterNavigationItem("Chat", "chat_icon", false),
        FooterNavigationItem("SOS", "sos_icon", false),
        FooterNavigationItem("Profile", "profile_icon", false)
    )
)

data class FooterNavigationItem(
    val title: String,
    val icon: String,
    val isSelected: Boolean
) 