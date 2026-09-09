package com.sample.texteditor.showcase

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/**
 * Aurora Unlock — cinematic Compose animation showcase.
 *
 * Shared `withFrameNanos` clock, hold-to-charge `Animatable`, `updateTransition`,
 * orbit trails, and BlurMaskFilter glow. Hold the core to unlock.
 */

@Stable
enum class AuroraPhase {
    Idle,
    Charging,
    Armed,
    Unlocking,
    Unlocked,
}

private val Bg = Color(0xFF070B14)
private val Teal = Color(0xFF5EEAD4)
private val Cyan = Color(0xFF22D3EE)
private val Violet = Color(0xFFA78BFA)
private val Magenta = Color(0xFFF472B6)
private val Success = Color(0xFF34D399)
private val Mist = Color(0xFFE2E8F0)

private val Cinematic = CubicBezierEasing(0.18f, 0.82f, 0.23f, 1.02f)

private fun lead(f: Float, amount: Float = 0.12f) =
    (f * (1f + amount)).coerceIn(0f, 1f)

private fun lag(f: Float, amount: Float = 0.18f) =
    (f * (1f - amount)).coerceIn(0f, 1f)

private fun smoothstep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private data class Orbiter(
    val speed: Float,
    val radiusRatio: Float,
    val color: Color,
    val width: Float,
    val trailLen: Int,
    val wobble: Float = 0f,
    val wobbleFreq: Float = 1f,
)

@Composable
fun AuroraUnlockScreen(modifier: Modifier = Modifier) {
    var phase by remember { mutableStateOf(AuroraPhase.Idle) }
    val charge = remember { Animatable(0f) }
    val unlockT = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var chargeJob by remember { mutableStateOf<Job?>(null) }

    // Shared clock — one progress drives all orbit trails
    var clock by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                clock = (now - start) / 1_000_000_000f
            }
        }
    }

    fun reset() {
        chargeJob?.cancel()
        scope.launch {
            phase = AuroraPhase.Idle
            charge.snapTo(0f)
            unlockT.snapTo(0f)
            burst.snapTo(0f)
        }
    }

    fun beginCharge() {
        if (phase == AuroraPhase.Unlocking || phase == AuroraPhase.Unlocked) return
        chargeJob?.cancel()
        phase = AuroraPhase.Charging
        chargeJob = scope.launch {
            // Fill to 1 over ~1.35s with cinematic ease feel via continuous animateTo
            charge.animateTo(
                targetValue = 1f,
                animationSpec = tween(1350, easing = FastOutSlowInEasing),
            )
            if (charge.value >= 0.995f) {
                phase = AuroraPhase.Armed
                burst.snapTo(0f)
                burst.animateTo(1f, tween(420, easing = Cinematic))
                delay(90.milliseconds)
                phase = AuroraPhase.Unlocking
                unlockT.snapTo(0f)
                unlockT.animateTo(1f, tween(720, easing = Cinematic))
                phase = AuroraPhase.Unlocked
            }
        }
    }

    fun endCharge() {
        if (phase != AuroraPhase.Charging) return
        chargeJob?.cancel()
        scope.launch {
            // Spring settle back — organic interactive feel
            charge.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
            phase = AuroraPhase.Idle
        }
    }

    val transition = updateTransition(phase, label = "aurora")
    val hintAlpha by transition.animateFloat(
        transitionSpec = { tween(280) },
        label = "hint",
    ) { p ->
        when (p) {
            AuroraPhase.Idle -> 1f
            AuroraPhase.Charging -> 0.55f
            else -> 0f
        }
    }
    val statusColor by transition.animateColor(
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) },
        label = "statusColor",
    ) { p ->
        when (p) {
            AuroraPhase.Unlocked -> Success
            AuroraPhase.Armed, AuroraPhase.Unlocking -> Magenta
            AuroraPhase.Charging -> Cyan
            AuroraPhase.Idle -> Mist.copy(alpha = 0.7f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF101828), Bg),
                    radius = 1200f,
                ),
            ),
    ) {
        // Soft ambient aurora wash
        Canvas(Modifier.fillMaxSize()) {
            val t = clock
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Teal.copy(alpha = 0.07f + 0.03f * sin(t * 0.7f)),
                        Violet.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.35f, size.height * 0.38f),
                    radius = size.minDimension * 0.55f,
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Magenta.copy(alpha = 0.06f + 0.025f * cos(t * 0.55f)),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.72f, size.height * 0.55f),
                    radius = size.minDimension * 0.42f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "AURORA",
                color = Mist.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 6.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (phase) {
                    AuroraPhase.Idle -> "Hold the core to gather light"
                    AuroraPhase.Charging -> "Gathering aurora…"
                    AuroraPhase.Armed -> "Critical charge"
                    AuroraPhase.Unlocking -> "Releasing seal"
                    AuroraPhase.Unlocked -> "Unlocked"
                },
                color = statusColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            AuroraCore(
                clock = clock,
                charge = charge.value,
                burst = burst.value,
                phase = phase,
                modifier = Modifier
                    .size(280.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                beginCharge()
                                tryAwaitRelease()
                                endCharge()
                            },
                        )
                    },
            )

            Spacer(Modifier.weight(1f))

            // Hint — graphicsLayer so alpha changes don't recompose text layout every frame
            Text(
                text = "press & hold",
                color = Mist,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.graphicsLayer { alpha = hintAlpha * 0.65f },
            )

            Spacer(Modifier.height(28.dp))

            UnlockChips(
                reveal = if (phase == AuroraPhase.Unlocked) 1f else unlockT.value,
                visible = phase == AuroraPhase.Unlocking || phase == AuroraPhase.Unlocked,
            )

            Spacer(Modifier.height(20.dp))

            if (phase == AuroraPhase.Unlocked) {
                Surface(
                    onClick = { reset() },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.08f),
                ) {
                    Text(
                        text = "Reset",
                        color = Mist.copy(alpha = 0.85f),
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        fontSize = 14.sp,
                    )
                }
            } else {
                Spacer(Modifier.height(44.dp))
            }
        }
    }
}

@Composable
private fun UnlockChips(reveal: Float, visible: Boolean) {
    val labels = listOf("Vault open", "Biometric cleared", "Session live")
    val delay = 0.14f
    val span = 1f + (labels.size - 1) * delay
    val global = lead(reveal) * span

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        labels.forEachIndexed { index, label ->
            val start = index * delay
            val localT = ((global - start) / 1f).coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(localT)
            val show = visible && localT > 0f
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Success.copy(alpha = 0.12f * eased),
                modifier = Modifier.graphicsLayer {
                    alpha = if (show) eased else 0f
                    translationY = (1f - eased) * 18f
                    scaleX = lerp(0.86f, 1f, eased)
                    scaleY = scaleX
                },
            ) {
                Text(
                    text = label,
                    color = Success.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AuroraCore(
    clock: Float,
    charge: Float,
    burst: Float,
    phase: AuroraPhase,
    modifier: Modifier = Modifier,
) {
    val orbiters = remember {
        listOf(
            Orbiter(0.85f, 0.78f, Teal.copy(alpha = 0.85f), 5.5f, 72),
            Orbiter(1.45f, 0.62f, Cyan.copy(alpha = 0.9f), 4.5f, 64, wobble = 0.04f, wobbleFreq = 2.1f),
            Orbiter(2.1f, 0.48f, Violet.copy(alpha = 0.9f), 4f, 56, wobble = 0.05f, wobbleFreq = 1.6f),
            Orbiter(-1.15f, 0.70f, Magenta.copy(alpha = 0.75f), 3.5f, 48, wobble = 0.03f, wobbleFreq = 2.4f),
        )
    }
    val trails = remember { List(orbiters.size) { ArrayDeque<Offset>(80) } }
    val glowPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
    }

    // Core scales with charge via graphicsLayer — no layout recomposition
    val coreScale = lerp(1f, 1.08f, charge) + burst * 0.12f

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = coreScale
            scaleY = coreScale
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val R = size.minDimension / 2f
            val speedMul = lerp(1f, 3.2f, charge)

            // Outer faint ring
            drawCircle(
                color = Mist.copy(alpha = 0.08f + charge * 0.06f),
                radius = R * 0.92f,
                style = Stroke(width = 1.5f),
            )

            // Charge arc (sweeps from top)
            if (charge > 0.001f) {
                val stroke = 6f
                val arcR = R * 0.92f
                val topLeft = Offset(c.x - arcR, c.y - arcR)
                val arcSize = Size(arcR * 2f, arcR * 2f)
                drawArc(
                    color = Mist.copy(alpha = 0.12f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Teal, Cyan, Violet, Magenta, Teal),
                        center = c,
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * charge,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            // Orbit trails
            orbiters.forEachIndexed { i, orb ->
                val wobble =
                    1f + orb.wobble * sin(clock * orb.wobbleFreq * 2f * PI.toFloat())
                val r = R * orb.radiusRatio * wobble
                val angle = clock * orb.speed * speedMul
                val pt = Offset(
                    c.x + r * cos(angle),
                    c.y + r * sin(angle),
                )
                val buf = trails[i]
                buf.addLast(pt)
                while (buf.size > orb.trailLen) buf.removeFirst()
                drawTrail(buf, orb.color, orb.width * lerp(1f, 1.35f, charge))
            }

            // Core glow (BlurMaskFilter)
            val glowR = R * lerp(0.22f, 0.34f, charge)
            val glowColor = when (phase) {
                AuroraPhase.Unlocked, AuroraPhase.Unlocking -> Success
                AuroraPhase.Armed -> Magenta
                else -> Cyan
            }
            drawGlow(c, glowR * 1.85f, glowColor.copy(alpha = 0.35f + charge * 0.35f), glowPaint)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        glowColor.copy(alpha = 0.85f),
                        glowColor.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                    center = c,
                    radius = glowR,
                ),
                radius = glowR,
                center = c,
            )

            // Burst shockwave
            if (burst > 0.01f) {
                val br = R * lerp(0.2f, 1.05f, smoothstep(burst))
                drawCircle(
                    color = Magenta.copy(alpha = (1f - burst) * 0.55f),
                    radius = br,
                    style = Stroke(width = lerp(10f, 1f, burst)),
                )
                drawCircle(
                    color = Cyan.copy(alpha = (1f - burst) * 0.35f),
                    radius = br * 0.78f,
                    style = Stroke(width = lerp(6f, 1f, burst)),
                )
            }
        }
    }
}

private fun DrawScope.drawTrail(
    points: ArrayDeque<Offset>,
    color: Color,
    width: Float,
) {
    if (points.size < 2) return
    val n = points.size - 1
    for (i in 0 until n) {
        val p0 = points.elementAt(i)
        val p1 = points.elementAt(i + 1)
        val headness = (i + 1) / n.toFloat()
        val eased = headness.pow(2.1f)
        drawLine(
            color = color.copy(alpha = lerp(0.02f, 0.95f, eased)),
            start = p0,
            end = p1,
            strokeWidth = width * lerp(0.45f, 1.2f, eased),
            cap = StrokeCap.Round,
        )
    }
    // Hot head
    val tip = points.last()
    val prev = points.elementAt(points.lastIndex - 1)
    val dx = tip.x - prev.x
    val dy = tip.y - prev.y
    val len = hypot(dx, dy)
    if (len > 0.1f) {
        val nx = dx / len
        val ny = dy / len
        val headLen = width * 4.2f
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(color, Color.White),
                start = Offset(tip.x - nx * headLen, tip.y - ny * headLen),
                end = tip,
            ),
            start = Offset(tip.x - nx * headLen, tip.y - ny * headLen),
            end = tip,
            strokeWidth = width * 1.15f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawGlow(
    center: Offset,
    radius: Float,
    color: Color,
    frameworkPaint: NativePaint,
) {
    drawIntoCanvas { canvas ->
        frameworkPaint.color = color.toArgb()
        frameworkPaint.maskFilter =
            BlurMaskFilter(radius * 0.55f, BlurMaskFilter.Blur.NORMAL)
        canvas.nativeCanvas.drawCircle(center.x, center.y, radius * 0.55f, frameworkPaint)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF070B14)
@Composable
private fun AuroraUnlockPreview() {
    AuroraUnlockScreen()
}
