package com.sample.texteditor.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compose animation playground — mapped to presentation sections.
 */
@Immutable
private data class DemoEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val section: String,
    val reusable: Boolean,
    val icon: ImageVector,
    val accent: Color,
)

private val Demos = listOf(
    DemoEntry(
        id = "shared_elements",
        title = "Shared Element Gallery",
        subtitle = "SharedTransitionLayout · list → detail morph",
        section = "Advanced Transitions",
        reusable = false,
        icon = Icons.Outlined.SwapHoriz,
        accent = Color(0xFF38BDF8),
    ),
    DemoEntry(
        id = "keyframes_wipe",
        title = "Keyframes Theme Wipe",
        subtitle = "keyframes AnimationSpec + clipRect overshoot",
        section = "Advanced Transitions",
        reusable = false,
        icon = Icons.Outlined.DarkMode,
        accent = Color(0xFFFBBF24),
    ),
    DemoEntry(
        id = "morphing_button",
        title = "Morphing Action Button",
        subtitle = "updateTransition · Idle → Loading → Success/Error",
        section = "Advanced Transitions",
        reusable = true,
        icon = Icons.Outlined.Bolt,
        accent = Color(0xFF34D399),
    ),
    DemoEntry(
        id = "aurora_unlock",
        title = "Aurora Unlock",
        subtitle = "Animatable · springs · PathMeasure · withFrameNanos",
        section = "Advanced Transitions",
        reusable = false,
        icon = Icons.Outlined.AutoAwesome,
        accent = Color(0xFFA78BFA),
    ),
    DemoEntry(
        id = "canvas_chart",
        title = "Canvas Pulse Chart",
        subtitle = "DrawScope area sparkline + staggered bars",
        section = "Custom Layout & Graphics",
        reusable = false,
        icon = Icons.Outlined.BubbleChart,
        accent = Color(0xFF22D3EE),
    ),
    DemoEntry(
        id = "orbital_layout",
        title = "Orbital Menu Layout",
        subtitle = "Custom Layout / MeasurePolicy — circle placement",
        section = "Custom Layout & Graphics",
        reusable = false,
        icon = Icons.Outlined.Hub,
        accent = Color(0xFFF472B6),
    ),
    DemoEntry(
        id = "neon_rush",
        title = "Neon Rush",
        subtitle = "Canvas game loop · spring steer · particle trails",
        section = "Custom Layout & Graphics",
        reusable = false,
        icon = Icons.Outlined.DirectionsCar,
        accent = Color(0xFF22D3EE),
    ),
)

@Composable
fun ComposePlaygroundScreen(
    onOpenDemo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(top = 48.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "COMPOSE LAB",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Presentation demos",
                color = Color(0xFFF1F5F9),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Advanced transitions · Canvas · custom Layout",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Demos, key = { it.id }) { demo ->
                DemoCard(demo = demo, onClick = { onOpenDemo(demo.id) })
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DemoCard(demo: DemoEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF111827), Color(0xFF0B1220)),
                ),
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(demo.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(demo.icon, contentDescription = null, tint = demo.accent)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = demo.section.uppercase(),
                color = demo.accent.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = demo.title,
                    color = Color(0xFFF8FAFC),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (demo.reusable) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "REUSABLE",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF34D399).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = demo.subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun PlaygroundDemoHost(
    demoId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (demoId) {
        null -> ComposePlaygroundScreen(onOpenDemo = {}, modifier = modifier)
        "shared_elements" -> DemoScaffold("Shared Elements", onBack, modifier) {
            SharedElementGalleryDemo()
        }
        "keyframes_wipe" -> FullBleedDemo(onBack, modifier) {
            KeyframesThemeWipeDemo()
        }
        "morphing_button" -> DemoScaffold("Morphing Action Button", onBack, modifier) {
            MorphingActionButtonDemo()
        }
        "aurora_unlock" -> FullBleedDemo(onBack, modifier) {
            AuroraUnlockScreen()
        }
        "canvas_chart" -> DemoScaffold("Canvas Pulse Chart", onBack, modifier) {
            CanvasPulseChartDemo()
        }
        "orbital_layout" -> DemoScaffold("Orbital Menu Layout", onBack, modifier) {
            OrbitalMenuLayoutDemo()
        }
        "neon_rush" -> FullBleedDemo(onBack, modifier) {
            NeonRushGame()
        }
        else -> ComposePlaygroundScreen(onOpenDemo = {}, modifier = modifier)
    }
}

@Composable
private fun FullBleedDemo(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        content()
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun DemoScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFE2E8F0),
                )
            }
            Text(
                text = title,
                color = Color(0xFFF1F5F9),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun PlaygroundPreview() {
    ComposePlaygroundScreen(onOpenDemo = {})
}
