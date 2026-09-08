package com.sample.texteditor.showcase.talk

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Closing card — one entrance clock, letter cascade, PathMeasure signature,
 * graphicsLayer (no layout thrash), shared withFrameNanos backdrop.
 */
@Composable
internal fun ThankYouSlide(modifier: Modifier = Modifier) {
    val enter = remember { Animatable(0f) }
    val name = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var playJob by remember { mutableStateOf<Job?>(null) }

    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            enter.snapTo(0f)
            name.snapTo(0f)
            val letters = launch {
                enter.animateTo(1f, tween(1500, easing = Cinematic))
            }
            delay(720)
            name.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
            letters.join()
        }
    }

    LaunchedEffect(Unit) { play() }

    Box(
        modifier
            .fillMaxSize()
            .background(TalkInk.Bg)
            .pointerInput(Unit) {
                detectTapGestures { play() }
            },
    ) {
        ThankYouBackdrop(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "MOTION OS",
                color = TalkInk.Violet,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = enter.value.coerceIn(0f, 1f)
                },
            )
            Spacer(Modifier.height(18.dp))
            ThankYouLetters(progress = enter)
            Spacer(Modifier.height(28.dp))
            Text(
                text = "TEAM LEAD",
                color = TalkInk.Cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
                modifier = Modifier.graphicsLayer {
                    val e = lag(name.value, 0.12f)
                    alpha = e
                    translationY = (1f - e) * 12f
                },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hammad Nawaz",
                color = TalkInk.Mist,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    val e = FastOutSlowInEasing.transform(name.value.coerceIn(0f, 1f))
                    alpha = e
                    scaleX = lerp(0.86f, 1f, e)
                    scaleY = scaleX
                },
            )
            SignatureFlourish(
                progress = name,
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(28.dp)
                    .padding(top = 4.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "and the team",
                color = TalkInk.Mute,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer {
                    val e = lag(name.value, 0.28f)
                    alpha = e
                    translationY = (1f - e) * 16f
                },
            )
            Spacer(Modifier.height(28.dp))
            ApiChipRow(progress = name)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tap to play the close again",
                color = TalkInk.Dim,
                fontSize = 13.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = lag(name.value, 0.4f) * 0.85f
                },
            )
        }
    }
}

@Composable
private fun ThankYouLetters(progress: Animatable<Float, *>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ThankGlyphs.forEachIndexed { i, glyph ->
            if (glyph == null) {
                Spacer(Modifier.width(18.dp))
            } else {
                Text(
                    text = glyph,
                    color = TalkInk.Mist,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer {
                        val delay = 0.065f
                        val local = ((progress.value - i * delay) / 0.42f).coerceIn(0f, 1f)
                        val e = FastOutSlowInEasing.transform(local)
                        alpha = e
                        scaleX = lerp(0.35f, 1f, e)
                        scaleY = scaleX
                        translationY = (1f - e) * 56f
                        rotationZ = (1f - e) * -8f
                    },
                )
            }
        }
    }
}

@Composable
private fun ApiChipRow(progress: Animatable<Float, *>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClosingApis.forEachIndexed { i, api ->
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val delay = 0.08f
                        val local = ((progress.value - 0.35f - i * delay) / 0.5f).coerceIn(0f, 1f)
                        val e = FastOutSlowInEasing.transform(local)
                        alpha = e
                        translationY = (1f - e) * 18f
                        scaleX = lerp(0.85f, 1f, e)
                        scaleY = scaleX
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = api,
                    color = TalkInk.Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SignatureFlourish(
    progress: Animatable<Float, *>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val t = smoothstep(progress.value.coerceIn(0f, 1f))
        if (t <= 0.02f) return@Canvas
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.02f, h * 0.62f)
            cubicTo(w * 0.22f, h * 0.08f, w * 0.38f, h * 1.05f, w * 0.55f, h * 0.48f)
            cubicTo(w * 0.68f, h * 0.12f, w * 0.82f, h * 0.72f, w * 0.98f, h * 0.38f)
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        val segment = Path()
        if (measure.getSegment(0f, measure.length * t, segment, true)) {
            drawPath(
                path = segment,
                color = TalkInk.Amber,
                style = Stroke(width = 3.2f, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun ThankYouBackdrop(modifier: Modifier = Modifier) {
    var clock by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                clock = (now - start) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier) {
        val t = clock
        val cx = size.width * 0.5f
        val cy = size.height * 0.42f
        val minD = size.minDimension

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    TalkInk.Violet.copy(alpha = 0.16f + 0.04f * sin(t * 0.7f)),
                    Color.Transparent,
                ),
                center = Offset(cx - minD * 0.12f, cy),
                radius = minD * 0.55f,
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    TalkInk.Cyan.copy(alpha = 0.10f + 0.03f * cos(t * 0.55f)),
                    Color.Transparent,
                ),
                center = Offset(cx + minD * 0.18f, cy + minD * 0.06f),
                radius = minD * 0.42f,
            ),
        )

        drawIntoCanvas { canvas ->
            val p = android.graphics.Paint()
            p.color = TalkInk.Amber.copy(alpha = 0.22f).toArgb()
            p.maskFilter = BlurMaskFilter(48f + 10f * sin(t * 1.4f), BlurMaskFilter.Blur.NORMAL)
            canvas.nativeCanvas.drawCircle(cx, cy - 8f, minD * 0.18f, p)
        }

        val colors = listOf(TalkInk.Cyan, TalkInk.Violet, TalkInk.Pink, TalkInk.Amber, TalkInk.Teal)
        for (i in 0 until 22) {
            val speed = 0.28f + (i % 5) * 0.07f
            val a = t * speed + i * 0.41f
            val rx = minD * (0.26f + (i % 4) * 0.055f)
            val ry = rx * 0.36f
            val pos = Offset(cx + cos(a) * rx, cy + sin(a) * ry)
            drawCircle(
                color = colors[i % colors.size].copy(alpha = 0.55f),
                radius = 2.4f + (i % 3),
                center = pos,
            )
            val trail = a - 0.18f
            drawCircle(
                color = colors[i % colors.size].copy(alpha = 0.18f),
                radius = 1.6f,
                center = Offset(cx + cos(trail) * rx, cy + sin(trail) * ry),
            )
        }
    }
}

private val Cinematic = CubicBezierEasing(0.18f, 0.82f, 0.23f, 1.02f)

private val ThankGlyphs = listOf("T", "H", "A", "N", "K", null, "Y", "O", "U")

private val ClosingApis = listOf(
    "Animatable",
    "spring",
    "keyframes",
    "sharedElement",
    "Canvas",
    "Layout",
)

private fun lag(f: Float, amount: Float = 0.18f) =
    (f * (1f - amount)).coerceIn(0f, 1f)

private fun smoothstep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}
