package com.archeGlobal.one.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.archeGlobal.one.R

// Font files have been renamed to lowercase with underscores to comply with Android naming conventions

val GraphikFontFamily =
    FontFamily(
        Font(R.font.graphik_black, FontWeight.Black),
        Font(R.font.graphik_black_italic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.graphik_bold, FontWeight.Bold),
        Font(R.font.graphik_bold_italic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.graphik_extralight, FontWeight.ExtraLight),
        Font(R.font.graphik_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(R.font.graphik_light, FontWeight.Light),
        Font(R.font.graphik_light_italic, FontWeight.Light, FontStyle.Italic),
        Font(R.font.graphik_medium, FontWeight.Medium),
        Font(R.font.graphik_medium_italic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.graphik_regular, FontWeight.Normal),
        Font(R.font.graphik_regular_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.graphik_semibold, FontWeight.SemiBold),
        Font(R.font.graphik_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
        Font(R.font.graphik_super, FontWeight.ExtraBold),
        Font(R.font.graphik_super_italic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(R.font.graphik_thin, FontWeight.Thin),
        Font(R.font.graphik_thin_italic, FontWeight.Thin, FontStyle.Italic),
    )

// Keep GeistFontFamily for backward compatibility
val GeistFontFamily = GraphikFontFamily
