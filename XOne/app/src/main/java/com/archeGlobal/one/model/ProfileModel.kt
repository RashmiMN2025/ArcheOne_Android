package com.archeGlobal.one.model

data class ProfileModel(
    val name: String,
    val email: String,
    val profilePicture: String? = null,
    val version: String = "Version 1.0",
    val lastLoginTime: String = "", // Added last login time
    val menuItems: List<ProfileMenuItem> = listOf(
        ProfileMenuItem("About Me", "person"),
        ProfileMenuItem("Address/Coordinates", "home"),
        ProfileMenuItem("Emergency Contact", "phone"),
        ProfileMenuItem("Documents", "document"),
        ProfileMenuItem("Log out", "logout")
    )
)

data class ProfileMenuItem(
    val title: String,
    val icon: String
)
