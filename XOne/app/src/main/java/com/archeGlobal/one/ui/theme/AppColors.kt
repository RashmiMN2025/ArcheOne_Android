package com.archeGlobal.one.ui.theme

import androidx.compose.ui.graphics.Color

fun getColorForApp(title: String): Color {
    return when (title) {
        "ID" -> Color(0xFF1c5c89)
        "Asset" -> Color(0xFFb33d1f)
        "Timesheet" -> Color(0xFF999900)
        "Leave" -> Color(0xFFb67f3e)
        "MyDocuments" -> Color(0xFF7c4c91)
        "My Career" -> Color(0xFFC71585)
        "eLearning" -> Color(0xFF2a7aad)
        "Goal Setting/KPI" -> Color(0xFFa34200)
        "XCard" -> Color(0xFF007A78)
        "Medical" -> Color(0xFFc67817)
        "Finance" -> Color(0xFF2a3a4b)
        "Admin" -> Color(0xFF7c4c91)
        "HR" -> Color(0xFF4a8c38)
        "Holiday Calendar" -> Color(0xFFcc4629)
        "Client Calendar" -> Color(0xFF1c5c89)
        "Greetings" -> Color(0xFF696969)
        "XConnect" -> Color(0xFF2981cc)
        "Locations" -> Color(0xFF800020)
        "Helpdesk" -> Color(0xFF12806a)
        "Announcements" -> Color(0xFFff6347)
        "XProfile" -> Color(0xFF00bfff)
        "Password Reset" -> Color(0xFF8B008B)
        "Policy" -> Color(0xFF123456)
        "SOS" -> Color(0xFFFF0000)
        "Travel & Expenses" -> Color(0xFF4B0082)
        "SAP" -> Color(0xFF0000CD)
        else -> Color(0xFF091857)
    }
}
