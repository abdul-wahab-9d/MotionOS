package com.sample.texteditor.showcase.talk

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

internal object TalkInk {
    val Bg = Color(0xFF070B14)
    val Panel = Color(0xFF0F172A)
    val Line = Color(0xFF1E293B)
    val Mist = Color(0xFFE2E8F0)
    val Mute = Color(0xFF94A3B8)
    val Dim = Color(0xFF64748B)
    val Cyan = Color(0xFF22D3EE)
    val Violet = Color(0xFFA78BFA)
    val Teal = Color(0xFF5EEAD4)
    val Lime = Color(0xFFA3E635)
    val Amber = Color(0xFFFBBF24)
    val Pink = Color(0xFFF472B6)
}

internal fun TalkPhase.accent(): Color = when (this) {
    TalkPhase.Idle -> TalkInk.Violet
    TalkPhase.Map -> TalkInk.Cyan
    TalkPhase.Proof -> TalkInk.Lime
    TalkPhase.Draw -> TalkInk.Cyan
    TalkPhase.Layout -> TalkInk.Pink
    TalkPhase.Excess -> TalkInk.Amber
    TalkPhase.Ship -> TalkInk.Teal
}

internal fun TalkPhase.label(): String = name.lowercase()

/** Viewport-sized column that scrolls when the slide is taller than the tablet pane. */
@Composable
internal fun SlideColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        content = content,
    )
}

@Composable
internal fun HoldToStartCore(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val charge = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var armed by remember { mutableStateOf(false) }
    val clock = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                clock.floatValue = (now - start) / 1_000_000_000f
            }
        }
    }

    fun begin() {
        if (armed) return
        job?.cancel()
        job = scope.launch {
            charge.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
            if (charge.value >= 0.995f) {
                armed = true
                onComplete()
            }
        }
    }

    fun cancelHold() {
        if (armed) return
        job?.cancel()
        scope.launch {
            charge.animateTo(
                0f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        }
    }

    val glow = rememberInfiniteTransition(label = "core_glow").animateFloat(
        initialValue = 10f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "glow",
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(168.dp)
                .pointerInput(armed) {
                    detectTapGestures(
                        onPress = {
                            begin()
                            tryAwaitRelease()
                            cancelHold()
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val t = clock.floatValue
                val c = center
                val r = size.minDimension / 2f
                drawIntoCanvas { canvas ->
                    val p = Paint().asFrameworkPaint()
                    p.color = TalkInk.Violet.copy(alpha = 0.35f).toArgb()
                    p.maskFilter = BlurMaskFilter(glow.value, BlurMaskFilter.Blur.NORMAL)
                    canvas.nativeCanvas.drawCircle(c.x, c.y, r * 0.42f, p)
                }
                val orbitR = r * 0.72f
                val colors = listOf(TalkInk.Cyan, TalkInk.Violet, TalkInk.Pink)
                for (i in 0 until 3) {
                    val a = t * (0.7f + i * 0.35f) + i * 2.1f
                    val p = Offset(c.x + orbitR * cos(a), c.y + orbitR * sin(a) * 0.55f)
                    drawCircle(colors[i].copy(alpha = 0.55f), 5f, p)
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF1E1B4B), TalkInk.Bg),
                        center = c,
                        radius = r * 0.5f,
                    ),
                    radius = r * 0.48f,
                    center = c,
                )
                val arcTopLeft = Offset(c.x - r * 0.58f, c.y - r * 0.58f)
                val arcSize = Size(r * 1.16f, r * 1.16f)
                drawArc(
                    color = TalkInk.Line,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = 6f, cap = StrokeCap.Round),
                )
                drawArc(
                    color = TalkInk.Cyan,
                    startAngle = -90f,
                    sweepAngle = 360f * charge.value,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = 6f, cap = StrokeCap.Round),
                )
            }
            Text(
                text = if (armed) "GO" else "HOLD",
                color = TalkInk.Mist,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Hold the core to begin  ·  or press next",
            color = TalkInk.Mute,
            fontSize = 14.sp,
        )
    }
}

@Composable
internal fun StaggerColumn(
    items: List<String>,
    modifier: Modifier = Modifier,
    numbered: Boolean = true,
) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(items) {
        t.snapTo(0f)
        t.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { i, line ->
            val local = ((t.value - i * 0.08f) / 0.45f).coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(local)
            Row(
                modifier = Modifier.graphicsLayer {
                    alpha = eased
                    translationY = (1f - eased) * 28f
                },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (numbered) {
                    Text(
                        text = (i + 1).toString().padStart(2, '0'),
                        color = TalkInk.Dim,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    Box(
                        Modifier
                            .padding(top = 10.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(TalkInk.Cyan),
                    )
                }
                Text(
                    text = line,
                    color = TalkInk.Mist,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
internal fun DecisionTree(
    heading: String,
    branches: List<TreeBranch>,
    onJump: (String) -> Unit,
    wrapApi: @Composable (sharedKey: String?, content: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = heading,
            color = TalkInk.Mute,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        branches.forEach { branch ->
            val clickable = branch.jumpToId != null
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (clickable) {
                            Modifier.clickable { branch.jumpToId?.let(onJump) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "├──",
                    color = TalkInk.Dim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    modifier = Modifier.width(52.dp),
                )
                Text(
                    text = branch.question,
                    color = TalkInk.Mist,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                )
                wrapApi(branch.sharedKey) {
                    Text(
                        text = "→  ${branch.api}",
                        color = if (clickable) TalkInk.Cyan else TalkInk.Mute,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private enum class SpecKind { Tween, Spring, Keyframes, Infinite }

@Composable
internal fun SpecsPlayground(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(SpecKind.Tween) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val cinematic = remember { CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f) }

    fun play(kind: SpecKind) {
        selected = kind
        if (kind == SpecKind.Infinite) return
        scope.launch {
            offset.snapTo(0f)
            when (kind) {
                SpecKind.Tween -> offset.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
                SpecKind.Spring -> offset.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
                SpecKind.Keyframes -> offset.animateTo(
                    1f,
                    keyframes {
                        durationMillis = 850
                        0.90f at 520 using cinematic
                        1.06f at 700
                        1f at 850 using FastOutSlowInEasing
                    },
                )
                SpecKind.Infinite -> Unit
            }
        }
    }

    val infinite = rememberInfiniteTransition(label = "spec_inf")
    val infT = infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "inf",
    )

    Row(
        modifier.fillMaxWidth().fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecKind.entries.forEach { kind ->
                val active = selected == kind
                val meta = when (kind) {
                    SpecKind.Tween -> "Directed, timed  ·  enter / morph"
                    SpecKind.Spring -> "Physical  ·  drag release, playful"
                    SpecKind.Keyframes -> "Hand-authored  ·  overshoot, pauses"
                    SpecKind.Infinite -> "Loop  ·  loaders (sparingly)"
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { play(kind) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = kind.name.lowercase(),
                        color = if (active) TalkInk.Cyan else TalkInk.Mist,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = meta, color = TalkInk.Mute, fontSize = 14.sp)
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .heightIn(min = 140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TalkInk.Panel)
                .padding(24.dp),
        ) {
            Text(
                text = "SAME CHIP · FOUR SPECS",
                color = TalkInk.Dim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        val travel = if (selected == SpecKind.Infinite) infT.value else offset.value
                        translationX = travel * 220f
                    }
                    .size(width = 88.dp, height = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TalkInk.Cyan),
                contentAlignment = Alignment.Center,
            ) {
                Text("Pay", color = TalkInk.Bg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
internal fun CodeCaption(
    lines: List<String>,
    accentLine: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF020617))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        lines.forEachIndexed { i, line ->
            Text(
                text = line,
                color = if (i == accentLine) TalkInk.Lime else TalkInk.Mute,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = if (i == accentLine) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ApplyTable(modifier: Modifier = Modifier) {
    data class ApplyRow(val moment: String, val api: String, val chip: String)

    val rows = remember {
        listOf(
            ApplyRow("Primary CTA async", "Morphing button / updateTransition", "Morph"),
            ApplyRow("List → detail", "Shared elements", "Gallery"),
            ApplyRow("Theme / onboarding flourish", "keyframes (sparingly)", "Wipe"),
            ApplyRow("Analytics widget", "Canvas chart", "Chart"),
            ApplyRow("Radial quick actions", "custom Layout (if design requires)", "Orbital"),
            ApplyRow("Everything else", "Material + Visibility / Content", "Default"),
        )
    }
    var revealed by remember { mutableStateOf(List(rows.size) { false }) }
    var focus by remember { mutableStateOf(-1) }

    fun reveal(i: Int) {
        focus = i
        revealed = revealed.toMutableList().also { it[i] = true }
    }

    Column(modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            rows.forEachIndexed { i, row ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (focus == i) TalkInk.Cyan.copy(alpha = 0.25f)
                            else Color.White.copy(alpha = 0.06f),
                        )
                        .clickable { reveal(i) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        row.chip,
                        color = TalkInk.Mist,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(
                "MOMENT",
                color = TalkInk.Dim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                "API  ·  tap a chip or row",
                color = TalkInk.Dim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.2f),
            )
        }
        rows.forEachIndexed { i, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (focus == i) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                    .clickable { reveal(i) }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(row.moment, color = TalkInk.Mist, fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text(
                    text = if (revealed[i]) row.api else "—",
                    color = if (revealed[i]) TalkInk.Cyan else TalkInk.Dim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1.2f),
                )
            }
        }
    }
}

@Composable
internal fun DrawGridBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val step = 48.dp.toPx()
        val c = TalkInk.Cyan.copy(alpha = 0.04f)
        var x = 0f
        while (x < size.width) {
            drawLine(c, Offset(x, 0f), Offset(x, size.height), 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(c, Offset(0f, y), Offset(size.width, y), 1f)
            y += step
        }
    }
}
