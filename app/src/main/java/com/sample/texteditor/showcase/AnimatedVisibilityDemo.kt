package com.sample.texteditor.showcase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AnimatedVisibility — the baseline show/hide API.
 *
 * Talk beat: "Appear / disappear?" branch of the animation tree. Everyone already
 * knows this one; this demo makes it concrete — same card, three Enter/Exit
 * transition pairs, combined with `+` and toggled live.
 */
@Immutable
private data class VisibilitySpec(
    val label: String,
    val enter: EnterTransition,
    val exit: ExitTransition,
    val hint: String,
)

private val VisibilitySpecs = listOf(
    VisibilitySpec(
        label = "Fade",
        enter = fadeIn(tween(280)),
        exit = fadeOut(tween(220)),
        hint = "fadeIn() / fadeOut() — alpha only",
    ),
    VisibilitySpec(
        label = "Expand",
        // `+` is operator fun EnterTransition.plus(EnterTransition): EnterTransition.
        // Both transitions run simultaneously — expand reserves layout space while fade handles alpha.
        enter = fadeIn(tween(220)) + expandVertically(tween(320)),
        exit = fadeOut(tween(160)) + shrinkVertically(tween(280)),
        hint = "expandVertically() — animates height, pushes siblings",
    ),
    VisibilitySpec(
        label = "Slide + Scale",
        enter = fadeIn(tween(220)) + slideInVertically(tween(320)) { it / 3 } + scaleIn(initialScale = 0.9f),
        exit = fadeOut(tween(160)) + slideOutVertically(tween(240)) { it / 3 } + scaleOut(targetScale = 0.9f),
        hint = "slideInVertically + scaleIn — combine transitions with +",
    ),
)

@Composable
fun AnimatedVisibilityDemo(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(VisibilitySpecs.first()) }
    var visible by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        Text(
            text = "THE BASELINE",
            color = Color(0xFF60A5FA),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "AnimatedVisibility",
            color = Color(0xFFF1F5F9),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "One boolean. Enter plays when it flips true, Exit plays when it flips false.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisibilitySpecs.forEach { spec ->
                val active = spec.label == selected.label
                Surface(
                    onClick = { selected = spec },
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) Color(0xFF60A5FA).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                ) {
                    Text(
                        text = spec.label,
                        color = if (active) Color(0xFF60A5FA) else Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Show details", color = Color(0xFFF1F5F9), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(selected.hint, color = Color(0xFF64748B), fontSize = 12.sp)
            }
            Switch(
                checked = visible,
                onCheckedChange = { visible = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF60A5FA)),
            )
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(
            visible = visible,
            enter = selected.enter,
            exit = selected.exit,
            // AnimatedVisibility keeps the content in the composition tree for the full duration
            // of Enter and Exit. Unlike View.GONE (which removes the view immediately), the
            // composable stays alive — no manual "don't remove until animation ends" bookkeeping.
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111827))
                    .padding(20.dp),
            ) {
                Text("Card content", color = Color(0xFFF1F5F9), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "This block only exists in the tree while visible == true. " +
                        "AnimatedVisibility keeps it composed during Enter/Exit, then removes it — " +
                        "no manual invalidate() or visibility flag juggling.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Use cases: error banners · optional form fields · empty-state swaps · tooltips",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
        )
    }
}

@Preview
@Composable
private fun AnimatedVisibilityDemoPreview() {
    AnimatedVisibilityDemo()
}
