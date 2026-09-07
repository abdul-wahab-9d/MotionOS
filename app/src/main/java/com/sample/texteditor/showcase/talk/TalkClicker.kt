package com.sample.texteditor.showcase.talk

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

interface TalkClickerHandler {
    fun next(): Boolean
    fun prev(): Boolean
    fun toggleNotes(): Boolean
}

/**
 * Activity-level clicker. Volume, D-pad, space, and media keys
 * reach here even when Compose doesn't have focus.
 */
object TalkClicker {
    @Volatile
    var handler: TalkClickerHandler? = null

    fun dispatch(keyCode: Int, action: Int): Boolean {
        if (action != KeyEvent.ACTION_DOWN) return false
        val h = handler ?: return false
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_SPACE,
            KeyEvent.KEYCODE_PAGE_DOWN,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_NAVIGATE_NEXT,
            -> h.next()

            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_PAGE_UP,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_NAVIGATE_PREVIOUS,
            -> h.prev()

            KeyEvent.KEYCODE_N,
            KeyEvent.KEYCODE_INFO,
            -> h.toggleNotes()

            else -> false
        }
    }
}

@Composable
fun rememberTalkSession(): TalkSession {
    val session = remember { TalkSession() }
    DisposableEffect(session) {
        TalkClicker.handler = session
        onDispose {
            if (TalkClicker.handler === session) TalkClicker.handler = null
        }
    }
    return session
}
