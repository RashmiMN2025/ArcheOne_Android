package com.archeGlobal.one.model

import androidx.compose.ui.graphics.Color
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

data class WelcomeBackgroundModel(
    val topColor: Color = WelcomeBackgroundTop,
    val middleColor: Color = WelcomeBackgroundMiddle,
    val bottomColor: Color = WelcomeBackgroundBottom
)
