package com.spothit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SpotHitScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B1024), Color(0xFF111B34), Color(0xFF0B1024)),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    Surface(color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .then(modifier)
        ) {
            content()
        }
    }
}
