package com.sample.texteditor.showcase.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Drop-in async action button for any Compose app.
 *
 * Morphs Idle → Loading (circle + orbit) → Success (PathMeasure check)
 * or Error (PathMeasure X). Drive it from your ViewModel / coroutine result.
 *
 * ```
 * var state by remember { mutableStateOf(ActionButtonState.Idle) }
 * MorphingActionButton(
 *     state = state,
 *     idleLabel = "Pay $24",
 *     onClick = {
 *         state = ActionButtonState.Loading
 *         // …suspend work…
 *         state = ActionButtonState.Success
 *     },
 * )
 * ```
 *
 * Techniques: `updateTransition`, spring micro-interactions, shared-clock orbit,
 * Canvas + PathMeasure progressive stroke, `graphicsLayer` for content alpha/scale.
 */
@Stable
enum class ActionButtonState {
    Idle,
    Loading,
    Success,
    Error,
}

@Immutable
data class MorphingActionButtonColors(
    val idleContainer: Color = Color(0xFF2563EB),
    val idleContent: Color = Color.White,
    val loadingContainer: Color = Color(0xFF1E293B),
    val loadingAccent: Color = Color(0xFF38BDF8),
    val successContainer: Color = Color(0xFF059669),
    val successContent: Color = Color.White,
    val errorContainer: Color = Color(0xFFDC2626),
    val errorContent: Color = Color.White,
)

@Composable
fun MorphingActionButton(
    state: ActionButtonState,
    idleLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    successLabel: String = "Done",
    errorLabel: String = "Try again",
    enabled: Boolean = true,
    idleWidth: Dp = 220.dp,
    collapsedWidth: Dp = 56.dp,
    height: Dp = 56.dp,
    colors: MorphingActionButtonColors = MorphingActionButtonColors(),
) {
    // updateTransition instead of N × animateXAsState: all properties share one state machine,
    // so their specs can reference the *from* state (e.g. Idle→Loading uses tween, everything
    // else uses spring). With separate animateXAsState there is no "from" state available.
    val transition = updateTransition(targetState = state, label = "morphing_action")

    val width by transition.animateDp(
        transitionSpec = {
            when {
                ActionButtonState.Idle isTransitioningTo ActionButtonState.Loading ->
                    tween(320, easing = FastOutSlowInEasing)
                ActionButtonState.Loading isTransitioningTo ActionButtonState.Idle ->
                    tween(280, easing = FastOutSlowInEasing)
                else -> spring(dampingRatio = 0.85f, stiffness = 380f)
            }
        },
        label = "width",
    ) { s ->
        when (s) {
            ActionButtonState.Idle -> idleWidth
            ActionButtonState.Loading -> collapsedWidth
            ActionButtonState.Success, ActionButtonState.Error -> idleWidth * 0.72f
        }
    }

    val container by transition.animateColor(
        transitionSpec = { tween(320) },
        label = "container",
    ) { s ->
        when (s) {
            ActionButtonState.Idle -> colors.idleContainer
            ActionButtonState.Loading -> colors.loadingContainer
            ActionButtonState.Success -> colors.successContainer
            ActionButtonState.Error -> colors.errorContainer
        }
    }

    val contentColor by transition.animateColor(
        transitionSpec = { tween(280) },
        label = "content",
    ) { s ->
        when (s) {
            ActionButtonState.Idle -> colors.idleContent
            ActionButtonState.Loading -> colors.loadingAccent
            ActionButtonState.Success -> colors.successContent
            ActionButtonState.Error -> colors.errorContent
        }
    }

    // Shape morphs first; label trails 120 ms behind for a cinematic "structure leads" feel.
    // Reversing the delay (label leads, shape follows) breaks the illusion — try it.
    val idleTextAlpha by transition.animateFloat(
        transitionSpec = { tween(180, delayMillis = 120) },
        label = "idleText",
    ) { s -> if (s == ActionButtonState.Idle) 1f else 0f }

    val resultTextAlpha by transition.animateFloat(
        transitionSpec = { tween(200, delayMillis = 160) },
        label = "resultText",
    ) { s ->
        when (s) {
            ActionButtonState.Success, ActionButtonState.Error -> 1f
            else -> 0f
        }
    }

    val glyphProgress by transition.animateFloat(
        transitionSpec = {
            when (targetState) {
                ActionButtonState.Success -> tween(560, easing = FastOutSlowInEasing)
                ActionButtonState.Error -> tween(640, easing = FastOutSlowInEasing)
                else -> tween(160)
            }
        },
        label = "glyph",
    ) { s ->
        if (s == ActionButtonState.Success || s == ActionButtonState.Error) 1f else 0f
    }

    val glyphScale by transition.animateFloat(
        transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        },
        label = "glyphScale",
    ) { s ->
        when (s) {
            ActionButtonState.Success, ActionButtonState.Error -> 1.05f
            else -> 0.75f
        }
    }

    val clickable =
        enabled && (state == ActionButtonState.Idle || state == ActionButtonState.Error)

    Button(
        onClick = onClick,
        enabled = clickable,
        modifier = modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = contentColor,
            disabledContainerColor = container,
            disabledContentColor = contentColor,
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = idleLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.graphicsLayer { alpha = idleTextAlpha },
            )

            LoadingOrbit(
                color = colors.loadingAccent,
                modifier = Modifier
                    .size(28.dp)
                    // graphicsLayer alpha skips recomposition — only the RenderNode is updated
                    // each frame. Using Modifier.alpha() here would recompose the whole Button.
                    .graphicsLayer {
                        alpha = if (state == ActionButtonState.Loading) 1f else 0f
                    },
            )

            ResultGlyph(
                success = state != ActionButtonState.Error,
                progress = glyphProgress,
                color = contentColor,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        alpha = glyphProgress
                        scaleX = glyphScale
                        scaleY = glyphScale
                        translationX = if (resultTextAlpha > 0.01f) -36f else 0f
                    },
            )

            val resultLabel =
                if (state == ActionButtonState.Error) errorLabel else successLabel
            Text(
                text = resultLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.graphicsLayer {
                    alpha = resultTextAlpha
                    translationX = 18f
                },
            )
        }
    }
}

@Composable
private fun LoadingOrbit(
    color: Color,
    modifier: Modifier = Modifier,
) {
    var angle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        // withFrameNanos suspends until the next Choreographer vsync and hands us the exact
        // timestamp — unlike delay(16), it never drifts on 120 Hz or under GC pressure.
        withFrameNanos { last = it }
        while (true) {
            withFrameNanos { now ->
                val dt = (now - last) / 1_000_000_000f
                last = now
                angle = (angle + 240f * dt) % 360f
            }
        }
    }

    Canvas(modifier) {
        val stroke = size.minDimension * 0.12f
        val r = size.minDimension / 2f - stroke
        drawArc(
            color = color.copy(alpha = 0.22f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        val rad = angle * (PI.toFloat() / 180f)
        val tip = Offset(center.x + r * cos(rad), center.y + r * sin(rad))
        drawCircle(color, stroke * 1.1f, tip)
    }
}

@Composable
private fun ResultGlyph(
    success: Boolean,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val tRaw = progress.coerceIn(0f, 1f)
    // Smoothstep (cubic Hermite) instead of a linear t: we're inside a Canvas draw call,
    // not a Compose animation, so there is no easing parameter available here.
    val t = tRaw * tRaw * (3f - 2f * tRaw) // smoothstep

    Canvas(modifier) {
        val stroke = size.minDimension * 0.12f
        if (success) {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                moveTo(0.22f * w, 0.52f * h)
                lineTo(0.42f * w, 0.72f * h)
                lineTo(0.78f * w, 0.30f * h)
            }
            val measure = PathMeasure().apply { setPath(path, false) }
            val segment = Path()
            if (measure.getSegment(0f, measure.length * t, segment, true)) {
                drawPath(
                    segment,
                    color,
                    style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        } else {
            val w = size.width
            val h = size.height
            val p1 = Path().apply {
                moveTo(0.28f * w, 0.28f * h)
                lineTo(0.72f * w, 0.72f * h)
            }
            val p2 = Path().apply {
                moveTo(0.72f * w, 0.28f * h)
                lineTo(0.28f * w, 0.72f * h)
            }
            val m1 = PathMeasure().apply { setPath(p1, false) }
            val m2 = PathMeasure().apply { setPath(p2, false) }
            if (t > 0f) {
                val seg1 = Path()
                val part1 = (t.coerceAtMost(0.5f) * 2f)
                if (m1.getSegment(0f, m1.length * part1, seg1, true) && part1 > 0f) {
                    drawPath(seg1, color, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
            if (t > 0.5f) {
                val seg2 = Path()
                val part2 = ((t - 0.5f).coerceIn(0f, 0.5f) * 2f)
                if (m2.getSegment(0f, m2.length * part2, seg2, true) && part2 > 0f) {
                    drawPath(seg2, color, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun MorphingActionButtonPreview() {
    MorphingActionButton(
        state = ActionButtonState.Idle,
        idleLabel = "Confirm payment",
        onClick = {},
    )
}
