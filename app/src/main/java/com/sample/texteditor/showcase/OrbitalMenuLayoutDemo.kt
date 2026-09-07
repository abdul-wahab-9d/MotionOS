package com.sample.texteditor.showcase

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Orbital menu — custom Layout from scratch.
 *
 * Talk beat: MeasurePolicy places children on a circle; Animatable expands radius.
 * No Row/Column math — pure custom layout.
 */
@Composable
fun OrbitalMenuLayoutDemo(modifier: Modifier = Modifier) {
    val labels = listOf("Home", "Search", "Labs", "Chart", "Share", "Settings")
    val colors = listOf(
        Color(0xFF22D3EE),
        Color(0xFFF472B6),
        Color(0xFFA3E635),
        Color(0xFFA78BFA),
        Color(0xFFFBBF24),
        Color(0xFF38BDF8),
    )
    var selected by remember { mutableIntStateOf(0) }
    val expand = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        expand.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "CUSTOM LAYOUT",
            color = Color(0xFFA78BFA),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Orbital Menu",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Layout { measurables, constraints → place on circle }",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
        )

        Spacer(Modifier.weight(1f, fill = false))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val side = minOf(maxWidth, maxHeight, 300.dp)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(side)) {
                OrbitalLayout(
                progress = expand.value,
                modifier = Modifier.fillMaxSize(),
            ) {
                labels.forEachIndexed { index, label ->
                    val active = index == selected
                    Box(
                        modifier = Modifier
                            .size(if (active) 64.dp else 56.dp)
                            .graphicsLayer {
                                val local =
                                    ((expand.value - index * 0.08f) / 0.75f).coerceIn(0f, 1f)
                                val e = FastOutSlowInEasing.transform(local)
                                alpha = e
                                scaleX = lerp(0.6f, 1f, e)
                                scaleY = scaleX
                            }
                            .clip(CircleShape)
                            .background(colors[index].copy(alpha = if (active) 1f else 0.75f))
                            .clickable {
                                selected = index
                                scope.launch {
                                    expand.snapTo(0.82f)
                                    expand.animateTo(
                                        1f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label.take(1),
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = labels[selected],
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Selected: ${labels[selected]}",
            color = Color(0xFFCBD5E1),
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun OrbitalLayout(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        val placeables = measurables.map { it.measure(loose) }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * 0.34f * progress.coerceIn(0f, 1f)
        val n = placeables.size.coerceAtLeast(1)

        layout(width, height) {
            placeables.forEachIndexed { i, placeable ->
                val angle = -PI.toFloat() / 2f + (2f * PI.toFloat() * i / n)
                val x = (cx + radius * cos(angle) - placeable.width / 2f).roundToInt()
                val y = (cy + radius * sin(angle) - placeable.height / 2f).roundToInt()
                placeable.placeRelative(x, y)
            }
        }
    }
}

@Preview
@Composable
private fun OrbitalPreview() {
    OrbitalMenuLayoutDemo()
}
