package com.archeGlobal.one.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TypingIndicator() {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFFAF5))
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // First dot with delay 0
        BouncingDot(0)

        // Second dot with delay 100ms
        BouncingDot(100)

        // Third dot with delay 200ms
        BouncingDot(200)
    }
}

@Composable
private fun BouncingDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(400, delayMillis),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "scaleAnimation",
    )

    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(400, delayMillis),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "opacityAnimation",
    )

    Box(
        modifier =
            Modifier
                .padding(horizontal = 2.dp)
                .size(6.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = opacity)),
    )
}
