package com.sample.texteditor.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.sample.texteditor.showcase.talk.MotionOsApp

/**
 * Root of Motion OS: tablets open the live talk deck, phones open the lab list.
 */
@Composable
fun ComposePlaygroundApp(modifier: Modifier = Modifier) {
    var forceLab by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        if (tablet && !forceLab) {
            MotionOsApp(
                onOpenLab = { forceLab = true },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LabHost(
                showReturnToTalk = tablet && forceLab,
                onReturnToTalk = { forceLab = false },
            )
        }
    }
}

@Composable
private fun LabHost(
    showReturnToTalk: Boolean,
    onReturnToTalk: () -> Unit,
) {
    val context = LocalContext.current
    var demoId by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14)),
    ) {
        if (showReturnToTalk && demoId == null) {
            Text(
                text = "←  BACK TO TALK",
                color = Color(0xFF22D3EE),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .statusBarsPadding()
                    .clickable(onClick = onReturnToTalk)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        if (demoId == null) {
            ComposePlaygroundScreen(
                onOpenDemo = { id ->
                    if (PortraitDemoActivity.isPortraitDemo(id)) {
                        PortraitDemoActivity.open(context, id)
                    } else {
                        demoId = id
                    }
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            PlaygroundDemoHost(
                demoId = demoId,
                onBack = { demoId = null },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
