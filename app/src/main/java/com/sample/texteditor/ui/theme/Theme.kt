package com.sample.texteditor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MotionOsColors = darkColorScheme(
    primary = Color(0xFF22D3EE),
    onPrimary = Color(0xFF070B14),
    background = Color(0xFF070B14),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
)

@Composable
fun MotionOsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MotionOsColors,
        content = content,
    )
}
