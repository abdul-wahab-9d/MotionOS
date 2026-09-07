package com.sample.texteditor

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sample.texteditor.showcase.ComposePlaygroundApp
import com.sample.texteditor.showcase.talk.TalkClicker
import com.sample.texteditor.ui.theme.MotionOsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotionOsTheme {
                ComposePlaygroundApp()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (TalkClicker.dispatch(event.keyCode, event.action)) return true
        return super.dispatchKeyEvent(event)
    }
}
