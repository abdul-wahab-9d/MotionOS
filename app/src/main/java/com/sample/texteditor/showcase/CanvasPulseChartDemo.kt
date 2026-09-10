package com.sample.texteditor.showcase

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.sin

/**
 * Canvas pulse chart — area sparkline + animated bars.
 *
 * Talk beat: DrawScope for charts/graphs — path fill, stroke, bars, grid.
 * Reveal driven by a single Animatable(0f..1f).
 */
@Composable
fun CanvasPulseChartDemo(modifier: Modifier = Modifier) {
    // One Animatable drives both charts. They scale proportionally so the bars never finish
    // before the sparkline, keeping the reveal feeling intentional rather than coincidental.
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
    }

    val samples = remember {
        List(24) { i ->
            val t = i / 23f
            0.35f + 0.28f * sin(t * 6.2f) + 0.18f * sin(t * 13.1f + 0.7f)
        }
    }
    val bars = remember { listOf(0.42f, 0.68f, 0.55f, 0.82f, 0.61f, 0.74f, 0.48f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = "CANVAS CHARTS",
            color = Color(0xFF22D3EE),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Pulse Analytics",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Path area + stroke · bars · grid — all DrawScope",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.25f)
                .heightIn(min = 96.dp)
                .background(Color(0xFF0F172A), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .padding(12.dp),
        ) {
            val t = reveal.value
            val pad = 12.dp.toPx()
            val chartW = size.width - pad * 2
            val chartH = size.height - pad * 2
            val origin = Offset(pad, pad)

            // Grid
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 10f))
            for (i in 0..4) {
                val y = origin.y + chartH * (i / 4f)
                drawLine(
                    color = Color.White.copy(alpha = 0.06f),
                    start = Offset(origin.x, y),
                    end = Offset(origin.x + chartW, y),
                    strokeWidth = 1.5f,
                    pathEffect = dash,
                )
            }

            val visibleCount = (1 + (samples.size - 1) * t).toInt().coerceIn(1, samples.size)
            val pts = samples.take(visibleCount).mapIndexed { i, v ->
                val x = origin.x + chartW * (i / (samples.size - 1).toFloat())
                val y = origin.y + chartH * (1f - v.coerceIn(0f, 1f))
                Offset(x, y)
            }

            if (pts.size >= 2) {
                val area = Path().apply {
                    moveTo(pts.first().x, origin.y + chartH)
                    pts.forEach { lineTo(it.x, it.y) }
                    lineTo(pts.last().x, origin.y + chartH)
                    close()
                }
                drawPath(
                    path = area,
                    brush = Brush.verticalGradient(
                        // The alpha is `0.35f * t` so the fill gradient fades in with the reveal —
                        // constant alpha would let the audience see the empty area before the line draws.
                        listOf(Color(0xFF22D3EE).copy(alpha = 0.35f * t), Color.Transparent),
                    ),
                )

                val line = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
                }
                drawPath(
                    path = line,
                    color = Color(0xFF22D3EE),
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
                drawCircle(Color.White, 6f, pts.last())
                drawCircle(Color(0xFF22D3EE), 3.5f, pts.last())
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Weekly throughput",
            color = Color(0xFFE2E8F0),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 72.dp)
                .background(Color(0xFF0F172A), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        ) {
            val t = reveal.value
            val padX = 16.dp.toPx()
            val padY = 20.dp.toPx()
            val gap = 10.dp.toPx()
            val barAreaW = size.width - padX * 2
            val barW = (barAreaW - gap * (bars.size - 1)) / bars.size
            val maxH = size.height - padY * 2

            bars.forEachIndexed { i, value ->
                val h = maxH * value * FastOutSlowInEasing.transform(
                    // Shift each bar's local time window by i * 0.06 so bar 0 starts first.
                    // No coroutines or LaunchedEffects needed — stagger is pure math on the
                    // global reveal fraction.
                    ((t - i * 0.06f) / 0.7f).coerceIn(0f, 1f),
                )
                val left = padX + i * (barW + gap)
                val top = size.height - padY - h
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFA78BFA), Color(0xFF6366F1)),
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barW, h.coerceAtLeast(0f)),
                    cornerRadius = CornerRadius(8f, 8f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Reveal = Animatable(0→1). Bars use staggered local time.",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
        )
    }
}

@Preview
@Composable
private fun ChartPreview() {
    CanvasPulseChartDemo()
}
