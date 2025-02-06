package com.example.xone.model

data class FooterNavigationModel(
    val showHome: Boolean = true,
    val showChat: Boolean = true,
    val showSOS: Boolean = true,
    val showProfile: Boolean = true
)

data class FooterNavigationItem(
    val title: String,
    val icon: String,
    val isSelected: Boolean
) 