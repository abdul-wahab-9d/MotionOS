package com.sample.texteditor.showcase

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Neon Rush — endless 3-lane racer on Compose Canvas.
 *
 * - `withFrameNanos` game loop (road, spawns, collisions)
 * - Score: +10 (and a speed bonus) each time a rival passes you
 * - Spring lane changes (`Animatable`)
 * - Nitro trail ring buffer + crash sparks
 * Controls: drag to steer, tap left/right half to change lane.
 */

private enum class RushPhase { Ready, Playing, GameOver }

private data class RivalCar(
    var lane: Int,
    var y: Float,
    val color: Color,
    val widthFactor: Float = 1f,
    var dodged: Boolean = false,
)

private data class Spark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color,
)

private object RushColors {
    val Night = Color(0xFF050814)
    val Asphalt = Color(0xFF141B2D)
    val LanePaint = Color(0xFFE2E8F0)
    val Cyan = Color(0xFF22D3EE)
    val Pink = Color(0xFFF472B6)
    val Lime = Color(0xFFA3E635)
    val Player = Color(0xFF38BDF8)
    val RivalA = Color(0xFFF43F5E)
    val RivalB = Color(0xFFFBBF24)
    val RivalC = Color(0xFFA78BFA)
}

@Composable
fun NeonRushGame(modifier: Modifier = Modifier) {
    val laneCount = 3
    var phase by remember { mutableStateOf(RushPhase.Ready) }
    var score by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(0) }
    var speed by remember { mutableFloatStateOf(420f) }
    var roadOffset by remember { mutableFloatStateOf(0f) }
    var shake by remember { mutableFloatStateOf(0f) }
    var frameTick by remember { mutableIntStateOf(0) }

    val playerLane = remember { Animatable(1f) }
    val rivals = remember { mutableListOf<RivalCar>() }
    val sparks = remember { mutableListOf<Spark>() }
    val nitroTrail = remember { ArrayDeque<Offset>(48) }

    var canvasW by remember { mutableFloatStateOf(0f) }
    var canvasH by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    fun laneX(lane: Float, roadLeft: Float, laneW: Float, carW: Float): Float =
        roadLeft + lane * laneW + (laneW - carW) / 2f

    fun reset() {
        score = 0
        speed = 420f
        roadOffset = 0f
        shake = 0f
        rivals.clear()
        sparks.clear()
        nitroTrail.clear()
        scope.launch { playerLane.snapTo(1f) }
        phase = RushPhase.Playing
    }

    fun steerTo(lane: Int) {
        if (phase != RushPhase.Playing) return
        val target = lane.coerceIn(0, laneCount - 1).toFloat()
        scope.launch {
            // On tap: spring to the exact lane. Spring on release gives the "settle" feel;
            // tween would arrive exactly on time but feel mechanical by comparison.
            playerLane.animateTo(
                target,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 450f),
            )
        }
    }

    fun steerByDelta(dx: Float) {
        if (phase != RushPhase.Playing || canvasW == 0f) return
        val roadW = canvasW * 0.72f
        val laneW = roadW / laneCount
        val next = (playerLane.value + dx / laneW).coerceIn(0f, (laneCount - 1).toFloat())
        scope.launch { playerLane.snapTo(next) }
    }

    fun snapNearestLane() {
        if (phase != RushPhase.Playing) return
        val nearest = playerLane.value.toInt().coerceIn(0, laneCount - 1)
        val frac = playerLane.value - nearest
        val target = if (frac > 0.5f) (nearest + 1).coerceAtMost(laneCount - 1) else nearest
        steerTo(target)
    }

    LaunchedEffect(phase, canvasW, canvasH) {
        if (phase != RushPhase.Playing || canvasW == 0f || canvasH == 0f) return@LaunchedEffect

        var last = 0L
        var spawnAcc = 0f
        // withFrameNanos suspends until the next Choreographer vsync — unlike while(true)+delay(16),
        // it works correctly on 90 Hz and 120 Hz screens and never drifts under GC pressure.
        withFrameNanos { last = it }

        while (phase == RushPhase.Playing) {
            withFrameNanos { now ->
                // Clamp dt to 50 ms: if the app is backgrounded or the GC pauses, dt can spike
                // to 300 ms+, teleporting rivals and causing false collisions. 50 ms = max lurch.
                val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                last = now
                frameTick++

                speed = (speed + 18f * dt).coerceAtMost(980f)
                roadOffset = (roadOffset + speed * dt) % 120f
                shake = (shake - dt * 4f).coerceAtLeast(0f)

                val roadW = canvasW * 0.72f
                val roadLeft = (canvasW - roadW) / 2f
                val laneW = roadW / laneCount
                val carW = laneW * 0.55f
                val carH = carW * 1.55f
                val playerY = canvasH * 0.78f
                val playerX = laneX(playerLane.value, roadLeft, laneW, carW)

                nitroTrail.addLast(Offset(playerX + carW / 2f, playerY + carH * 0.85f))
                while (nitroTrail.size > 40) nitroTrail.removeFirst()

                spawnAcc += speed * dt
                val spawnEvery = lerp(520f, 280f, ((speed - 420f) / 560f).coerceIn(0f, 1f))
                if (spawnAcc >= spawnEvery) {
                    spawnAcc = 0f
                    val occupied = rivals.filter { it.y < canvasH * 0.35f }.map { it.lane }.toSet()
                    val free = (0 until laneCount).filter { it !in occupied }
                    if (free.isNotEmpty()) {
                        val palette = listOf(
                            RushColors.RivalA,
                            RushColors.RivalB,
                            RushColors.RivalC,
                            RushColors.Pink,
                        )
                        rivals += RivalCar(
                            lane = free.random(),
                            y = -carH * 1.4f,
                            color = palette.random(),
                            widthFactor = Random.nextFloat() * 0.15f + 0.92f,
                        )
                    }
                }

                val iterator = rivals.listIterator()
                while (iterator.hasNext()) {
                    val r = iterator.next()
                    r.y += speed * dt * 0.92f
                    if (r.y > canvasH + carH) {
                        iterator.remove()
                        continue
                    }

                    val rw = carW * r.widthFactor
                    val rh = carH * r.widthFactor
                    val rx = laneX(r.lane.toFloat(), roadLeft, laneW, rw)
                    val overlapX =
                        abs((playerX + carW / 2f) - (rx + rw / 2f)) < (carW + rw) * 0.38f
                    val overlapY =
                        abs((playerY + carH / 2f) - (r.y + rh / 2f)) < (carH + rh) * 0.38f
                    if (overlapX && overlapY) {
                        repeat(18) {
                            sparks += Spark(
                                x = playerX + carW / 2f,
                                y = playerY + carH / 2f,
                                vx = Random.nextFloat() * 520f - 260f,
                                vy = Random.nextFloat() * -380f - 40f,
                                life = Random.nextFloat() * 0.45f + 0.35f,
                                color = listOf(
                                    RushColors.Cyan,
                                    RushColors.Pink,
                                    RushColors.Lime,
                                    Color.White,
                                ).random(),
                            )
                        }
                        shake = 1f
                        best = maxOf(best, score)
                        phase = RushPhase.GameOver
                        return@withFrameNanos
                    }

                    // Passed the player in another lane (or a near-miss) — that's a dodge.
                    if (!r.dodged && r.y + rh * 0.5f > playerY + carH * 0.5f) {
                        r.dodged = true
                        val speedBonus = ((speed - 420f) / 70f).toInt().coerceAtLeast(0)
                        score += 10 + speedBonus
                    }
                }

                val sIt = sparks.listIterator()
                while (sIt.hasNext()) {
                    val s = sIt.next()
                    s.life -= dt
                    if (s.life <= 0f) {
                        sIt.remove()
                        continue
                    }
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.vy += 520f * dt
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RushColors.Night)
            .onSizeChanged {
                canvasW = it.width.toFloat()
                canvasH = it.height.toFloat()
            }
            .pointerInput(phase) {
                detectTapGestures { offset ->
                    when (phase) {
                        RushPhase.Ready, RushPhase.GameOver -> reset()
                        RushPhase.Playing -> {
                            val lane = if (offset.x < size.width / 2f) {
                                (playerLane.value - 1f).toInt()
                            } else {
                                (playerLane.value + 1f).toInt()
                            }
                            steerTo(lane)
                        }
                    }
                }
            }
            .pointerInput(phase) {
                if (phase != RushPhase.Playing) return@pointerInput
                detectDragGestures(
                    onDragEnd = { snapNearestLane() },
                    onDragCancel = { snapNearestLane() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        steerByDelta(dragAmount.x)
                    },
                )
            },
    ) {
        // frameTick forces Canvas invalidation each game tick
        val tick = frameTick
        RushTrack(
            laneCount = laneCount,
            roadOffset = roadOffset,
            shake = shake,
            playerLane = playerLane.value,
            rivals = rivals.toList(),
            sparks = sparks.toList(),
            nitroTrail = nitroTrail.toList(),
            phase = phase,
            tick = tick,
            laneX = ::laneX,
        )

        RushHud(score = score, best = best)

        when (phase) {
            RushPhase.Ready -> RushOverlay(
                title = "Neon Rush",
                body = "Drag to steer · Tap sides to change lane\nDodge traffic. How far can you go?",
                cta = "Tap to race",
            )
            RushPhase.GameOver -> RushOverlay(
                title = "Crashed",
                body = "Score $score\nBest $best",
                cta = "Tap to retry",
            )
            RushPhase.Playing -> {
                Text(
                    text = "◀ drag ▶",
                    color = Color.White.copy(alpha = 0.28f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp),
                )
            }
        }
    }
}

@Composable
private fun RushTrack(
    laneCount: Int,
    roadOffset: Float,
    shake: Float,
    playerLane: Float,
    rivals: List<RivalCar>,
    sparks: List<Spark>,
    nitroTrail: List<Offset>,
    phase: RushPhase,
    tick: Int,
    laneX: (Float, Float, Float, Float) -> Float,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Reading `tick` here makes Compose treat it as a state read — the Canvas redraws
        // every game tick even when no other state observed inside DrawScope changed.
        @Suppress("UNUSED_EXPRESSION")
        tick // keep draw tied to game clock

        val canvasWidth = size.width
        val canvasHeight = size.height
        val shakeX = if (shake > 0f) sin(shake * 40f) * 14f * shake else 0f
        val shakeY = if (shake > 0f) sin(shake * 55f) * 8f * shake else 0f

        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0B1020), RushColors.Night, Color(0xFF12081A)),
            ),
        )

        val roadW = canvasWidth * 0.72f
        val roadLeft = (canvasWidth - roadW) / 2f + shakeX
        val laneW = roadW / laneCount

        drawRoundRect(
            color = RushColors.Asphalt,
            topLeft = Offset(roadLeft, shakeY),
            size = Size(roadW, canvasHeight),
            cornerRadius = CornerRadius(18f, 18f),
        )

        drawLine(
            brush = Brush.verticalGradient(
                listOf(
                    RushColors.Cyan.copy(alpha = 0.15f),
                    RushColors.Cyan,
                    RushColors.Cyan.copy(alpha = 0.2f),
                ),
            ),
            start = Offset(roadLeft, 0f),
            end = Offset(roadLeft, canvasHeight),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
        drawLine(
            brush = Brush.verticalGradient(
                listOf(
                    RushColors.Pink.copy(alpha = 0.15f),
                    RushColors.Pink,
                    RushColors.Pink.copy(alpha = 0.2f),
                ),
            ),
            start = Offset(roadLeft + roadW, 0f),
            end = Offset(roadLeft + roadW, canvasHeight),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )

        for (i in 1 until laneCount) {
            val x = roadLeft + i * laneW
            var y = -40f + (roadOffset % 80f)
            while (y < canvasHeight + 40f) {
                drawLine(
                    color = RushColors.LanePaint.copy(alpha = 0.55f),
                    start = Offset(x, y),
                    end = Offset(x, y + 36f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
                y += 80f
            }
        }

        drawRect(
            brush = Brush.verticalGradient(
                listOf(RushColors.Cyan.copy(alpha = 0.18f), Color.Transparent),
            ),
            topLeft = Offset(roadLeft, 0f),
            size = Size(roadW, canvasHeight * 0.22f),
        )

        val carW = laneW * 0.55f
        val carH = carW * 1.55f
        val playerY = canvasHeight * 0.78f + shakeY
        val playerX = laneX(playerLane, roadLeft, laneW, carW)

        if (nitroTrail.size >= 2 && phase == RushPhase.Playing) {
            for (i in 0 until nitroTrail.lastIndex) {
                val t = (i + 1) / nitroTrail.lastIndex.toFloat()
                val a = nitroTrail[i]
                val b = nitroTrail[i + 1]
                drawLine(
                    color = RushColors.Cyan.copy(alpha = 0.08f + 0.45f * t),
                    start = Offset(a.x + shakeX, a.y + shakeY),
                    end = Offset(b.x + shakeX, b.y + shakeY),
                    strokeWidth = lerp(2f, 10f, t),
                    cap = StrokeCap.Round,
                )
            }
        }

        rivals.forEach { r ->
            val rw = carW * r.widthFactor
            val rh = carH * r.widthFactor
            val rx = laneX(r.lane.toFloat(), roadLeft, laneW, rw)
            drawRushCar(
                left = rx,
                top = r.y + shakeY,
                width = rw,
                height = rh,
                body = r.color,
                accent = Color.White.copy(alpha = 0.35f),
                facingDown = true,
            )
        }

        val lean = (playerLane - playerLane.toInt().coerceIn(0, 2)) * 8f
        rotate(degrees = lean, pivot = Offset(playerX + carW / 2f, playerY + carH / 2f)) {
            drawRushCar(
                left = playerX,
                top = playerY,
                width = carW,
                height = carH,
                body = RushColors.Player,
                accent = RushColors.Lime,
                facingDown = false,
            )
        }

        sparks.forEach { s ->
            drawCircle(
                color = s.color.copy(alpha = s.life.coerceIn(0f, 1f)),
                radius = 4f + 6f * s.life,
                center = Offset(s.x + shakeX, s.y + shakeY),
            )
        }
    }
}

@Composable
private fun BoxScope.RushHud(score: Int, best: Int) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "NEON RUSH",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = score.toString(),
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
        )
        if (best > 0) {
            Text(
                text = "BEST $best",
                color = RushColors.Pink.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )
        }
    }
}

@Composable
private fun RushOverlay(title: String, body: String, cta: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.62f),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = cta,
                    color = RushColors.Cyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

private fun DrawScope.drawRushCar(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    body: Color,
    accent: Color,
    facingDown: Boolean,
) {
    val cabinTop = if (facingDown) top + height * 0.38f else top + height * 0.18f
    val cabinH = height * 0.32f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(left + 4f, top + height - 10f),
        size = Size(width - 8f, 14f),
        cornerRadius = CornerRadius(8f, 8f),
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                body.copy(alpha = 0.95f),
                body,
                Color(
                    red = body.red * 0.75f,
                    green = body.green * 0.75f,
                    blue = body.blue * 0.75f,
                    alpha = body.alpha,
                ),
            ),
        ),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(width * 0.22f, width * 0.22f),
    )

    drawRoundRect(
        color = Color(0xFF0F172A).copy(alpha = 0.75f),
        topLeft = Offset(left + width * 0.14f, cabinTop),
        size = Size(width * 0.72f, cabinH),
        cornerRadius = CornerRadius(8f, 8f),
    )

    val lightY = if (facingDown) top + height * 0.12f else top + height * 0.82f
    val lightColor = if (facingDown) Color.White.copy(alpha = 0.85f) else RushColors.Pink
    drawCircle(lightColor, width * 0.08f, Offset(left + width * 0.22f, lightY))
    drawCircle(lightColor, width * 0.08f, Offset(left + width * 0.78f, lightY))

    drawLine(
        color = accent,
        start = Offset(left + width * 0.5f, top + height * 0.12f),
        end = Offset(left + width * 0.5f, top + height * 0.88f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.12f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(width * 0.22f, width * 0.22f),
        style = Stroke(width = 2f),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050814)
@Composable
private fun NeonRushPreview() {
    NeonRushGame()
}
