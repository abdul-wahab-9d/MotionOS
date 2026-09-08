package com.sample.texteditor.showcase

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
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
 * Fan deck — custom Layout from scratch.
 *
 * Talk beat: MeasurePolicy places real children on an ellipse; zIndex brings
 * the focused card forward. graphicsLayer sells depth (rotationY / scale).
 * Canvas draws the ring — Layout does not. Spring settles after a swipe.
 */
@Composable
fun OrbitalMenuLayoutDemo(modifier: Modifier = Modifier) {
    val items = remember { FanItems }
    val n = items.size
    val expand = remember { Animatable(0f) }
    val selected = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        expand.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    fun snapTo(index: Int) {
        val target = index.mod(n).toFloat()
        scope.launch {
            selected.animateTo(
                target,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    val focused = selected.value.roundToInt().mod(n)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "CUSTOM LAYOUT",
            color = Color(0xFFF472B6),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Fan Deck",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Layout places on an ellipse  ·  swipe or tap  ·  spring settle",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(n) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val nearest = selected.value.roundToInt().coerceIn(0, n - 1)
                            snapTo(nearest)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val next = (selected.value - dragAmount / 160f)
                                    .coerceIn(0f, (n - 1).toFloat())
                                selected.snapTo(next)
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            FanOrbitBackdrop(
                expand = expand.value,
                accent = items[focused].color,
                modifier = Modifier.fillMaxSize(),
            )
            FanDeckLayout(
                selected = selected.value,
                expand = expand.value,
                modifier = Modifier.fillMaxSize(),
            ) {
                items.forEachIndexed { index, item ->
                    FanCard(
                        item = item,
                        index = index,
                        selected = selected.value,
                        count = n,
                        expand = expand.value,
                        density = density,
                        onTap = { snapTo(index) },
                    )
                }
            }
        }

        Text(
            text = items[focused].label,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = items[focused].kicker,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FanCard(
    item: FanItem,
    index: Int,
    selected: Float,
    count: Int,
    expand: Float,
    density: Float,
    onTap: () -> Unit,
) {
    val front = frontness(index, selected, count)
    Box(
        modifier = Modifier
            .size(width = 92.dp, height = 124.dp)
            .graphicsLayer {
                val local = ((expand - index * 0.07f) / 0.55f).coerceIn(0f, 1f)
                val e = FastOutSlowInEasing.transform(local)
                val depth = lerp(0.62f, 1.08f, front)
                alpha = e * lerp(0.45f, 1f, front)
                scaleX = depth * lerp(0.72f, 1f, e)
                scaleY = scaleX
                rotationY = -cos(orbitAngle(index, selected, count)) * 58f
                cameraDistance = 18f * density
                translationY = (1f - e) * 48f
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(item.color, item.color.copy(alpha = 0.35f), Color(0xFF0F172A)),
                ),
            )
            .pointerInput(index) {
                detectTapGestures { onTap() }
            }
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column {
            Text(
                text = item.label.take(1),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun FanDeckLayout(
    selected: Float,
    expand: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        val placeables = measurables.map { it.measure(loose) }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val n = placeables.size.coerceAtLeast(1)
        val rx = min(width, height) * 0.38f * expand.coerceIn(0f, 1f)
        val ry = rx * 0.38f
        val cx = width / 2f
        val cy = height / 2f + height * 0.04f

        layout(width, height) {
            placeables.forEachIndexed { i, p ->
                val pos = orbitOffset(i, selected, n, cx, cy, rx, ry)
                p.placeRelative(
                    x = (pos.x - p.width / 2f).roundToInt(),
                    y = (pos.y - p.height / 2f).roundToInt(),
                    zIndex = frontness(i, selected, n),
                )
            }
        }
    }
}

@Composable
private fun FanOrbitBackdrop(
    expand: Float,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f + size.height * 0.04f
        val rx = min(size.width, size.height) * 0.38f * expand.coerceIn(0f, 1f)
        val ry = rx * 0.38f
        if (rx < 8f) return@Canvas

        drawOval(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2f, ry * 2f),
            style = Stroke(
                width = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f)),
            ),
        )

        val front = Offset(cx, cy + ry)
        drawIntoCanvas { canvas ->
            val p = android.graphics.Paint()
            p.color = accent.copy(alpha = 0.28f).toArgb()
            p.maskFilter = BlurMaskFilter(36f, BlurMaskFilter.Blur.NORMAL)
            canvas.nativeCanvas.drawCircle(front.x, front.y, 52f, p)
        }
        drawCircle(accent.copy(alpha = 0.12f), 70f, front)
    }
}

private data class FanItem(
    val label: String,
    val kicker: String,
    val color: Color,
)

private val FanItems = listOf(
    FanItem("Home", "Radial slot  ·  Layout", Color(0xFF22D3EE)),
    FanItem("Search", "Measure, then place", Color(0xFFF472B6)),
    FanItem("Labs", "zIndex = frontness", Color(0xFFA3E635)),
    FanItem("Chart", "Canvas draws the ring", Color(0xFFA78BFA)),
    FanItem("Share", "graphicsLayer depth", Color(0xFFFBBF24)),
    FanItem("Settings", "spring settle on swipe", Color(0xFF38BDF8)),
)

/** 6 o'clock is the front of the deck. */
private fun orbitAngle(index: Int, selected: Float, count: Int): Float =
    PI.toFloat() / 2f + 2f * PI.toFloat() * (index - selected) / count.coerceAtLeast(1)

private fun orbitOffset(
    index: Int,
    selected: Float,
    count: Int,
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
): Offset {
    val a = orbitAngle(index, selected, count)
    return Offset(cx + rx * cos(a), cy + ry * sin(a))
}

private fun frontness(index: Int, selected: Float, count: Int): Float =
    ((sin(orbitAngle(index, selected, count)) + 1f) / 2f)

@Preview
@Composable
private fun OrbitalPreview() {
    OrbitalMenuLayoutDemo()
}
