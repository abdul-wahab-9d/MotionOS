package com.sample.texteditor.showcase

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Keyframes theme wipe — cinematic horizontal reveal.
 *
 * Talk beat: AnimationSpec beyond tween/spring → keyframes + overshoot + clipRect.
 * Tap to wipe light ↔ dark.
 */
@Composable
fun KeyframesThemeWipeDemo(modifier: Modifier = Modifier) {
    var dark by remember { mutableStateOf(false) }
    val fraction = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    // CubicBezierEasing(0.2, 0.8, 0.2, 1.0) mirrors the standard iOS "spring-like" curve —
    // fast acceleration, gentle finish — which is why this feels premium rather than mechanical.
    val cinematic = remember { CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f) }

    fun wipeTo(targetDark: Boolean) {
        if (dark == targetDark && fraction.value == if (targetDark) 1f else 0f) return
        scope.launch {
            val target = if (targetDark) 1f else 0f
            fraction.animateTo(
                targetValue = target,
                // keyframes: unlike tween (one easing from A to B), keyframes lets you pin
                // intermediate values at specific times. Here the wipe races ahead to ~90 %,
                // overshoots to 103 %, then settles — that's three timing anchors, one spec.
                animationSpec = keyframes {
                    durationMillis = 850
                    // Race ahead, slight overshoot, settle — the keyframes story
                    ((target * 0.90f + if (target == 0f) 0.10f else 0f).coerceIn(0f, 1.05f) at 520 using cinematic)
                    ((target + if (target == 1f) 0.03f else -0.03f) at 700)
                    (target at 850 using FastOutSlowInEasing)
                },
            )
            dark = targetDark
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { wipeTo(!dark) },
    ) {
        ThemePane(
            isDark = false,
            label = "Light",
            hint = "Tap to wipe →",
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToFraction(fraction.value),
        ) {
            ThemePane(
                isDark = true,
                label = "Dark",
                hint = "← Tap to wipe",
            )
        }
    }
}

@Composable
private fun ThemePane(isDark: Boolean, label: String, hint: String) {
    val bg = if (isDark) Color(0xFF0B1220) else Color(0xFFF8FAFC)
    val fg = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val muted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accent = if (isDark) Color(0xFF22D3EE) else Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "KEYFRAMES",
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = label,
            color = fg,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "keyframes { … at millis with easing }",
            color = muted,
            fontSize = 14.sp,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = hint,
            color = muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(40.dp))
    }
}

// drawWithContent + clipRect: a hard geometric crop at the render layer.
// Using Box alpha or a scale modifier would fade/squash the content — we want a clean reveal
// edge, not a dissolve. clipRect passes zero cost when f == 1 (full rect is a no-op clip).
private fun Modifier.clipToFraction(f: Float): Modifier = drawWithContent {
    val right = size.width * f.coerceIn(0f, 1.05f).coerceAtMost(1f)
    clipRect(left = 0f, top = 0f, right = right, bottom = size.height) {
        this@drawWithContent.drawContent()
    }
}

@Preview
@Composable
private fun ThemeWipePreview() {
    KeyframesThemeWipeDemo()
}
