package com.sample.texteditor.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.texteditor.showcase.components.ActionButtonState
import com.sample.texteditor.showcase.components.MorphingActionButton
import com.sample.texteditor.showcase.components.MorphingActionButtonColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive demo for [MorphingActionButton] — simulates async pay / fail.
 */
@Composable
fun MorphingActionButtonDemo(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(ActionButtonState.Idle) }
    val scope = rememberCoroutineScope()

    fun run(succeed: Boolean) {
        if (state == ActionButtonState.Loading) return
        scope.launch {
            state = ActionButtonState.Loading
            delay(1600)
            state = if (succeed) ActionButtonState.Success else ActionButtonState.Error
            delay(1800)
            state = ActionButtonState.Idle
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Reusable in any app",
            color = Color(0xFF34D399),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Wire state from your ViewModel.\nCopy MorphingActionButton.kt into any module.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        MorphingActionButton(
            state = state,
            idleLabel = "Pay $24.00",
            successLabel = "Paid",
            errorLabel = "Failed",
            onClick = {
                when (state) {
                    ActionButtonState.Idle -> run(succeed = true)
                    ActionButtonState.Error -> run(succeed = true)
                    else -> Unit
                }
            },
            colors = MorphingActionButtonColors(
                idleContainer = Color(0xFF2563EB),
                loadingAccent = Color(0xFF38BDF8),
                successContainer = Color(0xFF059669),
                errorContainer = Color(0xFFDC2626),
            ),
        )

        Spacer(Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Chip("Simulate success") {
                state = ActionButtonState.Idle
                run(succeed = true)
            }
            Chip("Simulate error") {
                state = ActionButtonState.Idle
                run(succeed = false)
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Use cases: checkout · login · save · send · delete confirm",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.08f),
    ) {
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}
