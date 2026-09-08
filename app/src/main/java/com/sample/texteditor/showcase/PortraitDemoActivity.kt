package com.sample.texteditor.showcase

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sample.texteditor.ui.theme.MotionOsTheme

class PortraitDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        val demoId = intent.getStringExtra(EXTRA_DEMO) ?: DEMO_AURORA
        setContent {
            MotionOsTheme {
                Box(Modifier.fillMaxSize()) {
                    when (demoId) {
                        DEMO_NEON -> NeonRushGame(Modifier.fillMaxSize())
                        else -> AuroraUnlockScreen(Modifier.fillMaxSize())
                    }
                    IconButton(
                        onClick = { finish() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 40.dp, start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to talk",
                            tint = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_DEMO = "demo"
        const val DEMO_AURORA = "aurora_unlock"
        const val DEMO_NEON = "neon_rush"

        fun isPortraitDemo(demoId: String): Boolean =
            demoId == DEMO_AURORA || demoId == DEMO_NEON

        fun open(context: Context, demoId: String) {
            context.startActivity(
                Intent(context, PortraitDemoActivity::class.java)
                    .putExtra(EXTRA_DEMO, demoId),
            )
        }
    }
}
