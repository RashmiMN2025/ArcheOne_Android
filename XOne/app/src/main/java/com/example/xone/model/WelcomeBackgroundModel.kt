package com.example.xone.model

import androidx.compose.ui.graphics.Color
import com.example.xone.ui.theme.WelcomeBackgroundTop
import com.example.xone.ui.theme.WelcomeBackgroundMiddle
import com.example.xone.ui.theme.WelcomeBackgroundBottom

data class WelcomeBackgroundModel(
    val topColor: Color = WelcomeBackgroundTop,
    val middleColor: Color = WelcomeBackgroundMiddle,
    val bottomColor: Color = WelcomeBackgroundBottom
) 