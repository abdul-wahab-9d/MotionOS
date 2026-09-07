package com.sample.texteditor.showcase.talk

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Motion OS — the talk as a Compose app.
 * Tablet landscape: slides + live demos. Clicker: volume / D-pad / space.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MotionOsApp(
    onOpenLab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = rememberTalkSession()
    KeepPresentationAwake()
    LockLandscape()

    val slide = session.current
    val phaseTransition = updateTransition(slide.phase, label = "talk_phase")
    val chromeAlpha by phaseTransition.animateFloat(
        transitionSpec = { tween(280) },
        label = "chrome",
    ) { if (it == TalkPhase.Idle) 0.45f else 1f }

    Box(
        modifier
            .fillMaxSize()
            .background(TalkInk.Bg),
    ) {
        if (slide.phase == TalkPhase.Draw) {
            DrawGridBackdrop()
        }
        SharedTransitionLayout(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                TalkTopBar(
                    session = session,
                    chromeAlpha = chromeAlpha,
                    onOpenLab = onOpenLab,
                )
                AnimatedContent(
                    targetState = slide,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        fadeIn(tween(280)) togetherWith fadeOut(tween(180))
                    },
                    contentKey = { it.id },
                    label = "slide",
                ) { target ->
                    TalkSlideLayout(
                        slide = target,
                        session = session,
                        visibilityScope = this,
                    )
                }
                TalkBottomBar(session = session, chromeAlpha = chromeAlpha)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun androidx.compose.animation.SharedTransitionScope.TalkSlideLayout(
    slide: DeckSlide,
    session: TalkSession,
    visibilityScope: androidx.compose.animation.AnimatedContentScope,
) {
    when (slide.layout) {
        SlideLayout.Full -> TalkSlideBody(slide, session, visibilityScope, Modifier.fillMaxSize())
        SlideLayout.FullBleed -> {
            val demo = slide.kind as? SlideKind.Demo
            Box(Modifier.fillMaxSize()) {
                if (demo != null) {
                    TalkDemoStage(demo.demoId, Modifier.fillMaxSize())
                } else {
                    TalkSlideBody(slide, session, visibilityScope, Modifier.fillMaxSize())
                }
                if (demo != null) {
                    val titleMod = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                    val key = demo.sharedKey
                    if (key != null) {
                        Text(
                            text = slide.title,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = titleMod.sharedElement(
                                state = rememberSharedContentState(key),
                                animatedVisibilityScope = visibilityScope,
                            ),
                        )
                    } else {
                        Text(
                            text = slide.title,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = titleMod,
                        )
                    }
                }
            }
        }
        SlideLayout.Split -> {
            val demo = slide.kind as? SlideKind.Demo
            Row(Modifier.fillMaxSize().padding(end = 16.dp, bottom = 8.dp)) {
                TalkSlideBody(
                    slide = slide,
                    session = session,
                    visibilityScope = visibilityScope,
                    modifier = Modifier.weight(0.92f).fillMaxHeight(),
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .padding(vertical = 24.dp)
                        .background(TalkInk.Line),
                )
                if (demo != null) {
                    TalkDemoStage(
                        demoId = demo.demoId,
                        modifier = Modifier
                            .weight(1.08f)
                            .fillMaxHeight()
                            .padding(start = 12.dp, top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TalkTopBar(
    session: TalkSession,
    chromeAlpha: Float,
    onOpenLab: () -> Unit,
) {
    val slide = session.current
    val n = session.slides.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "MOTION OS",
            color = TalkInk.Mute.copy(alpha = chromeAlpha),
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontSize = 11.sp,
        )
        Text(
            text = slide.phase.label(),
            color = slide.phase.accent().copy(alpha = chromeAlpha),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = if (session.shortTrack) "30 MIN" else "60 MIN",
            color = TalkInk.Mute,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { session.toggleShortTrack() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Text(
            text = "LAB",
            color = TalkInk.Mute,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onOpenLab)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Text(
            text = "${(session.index + 1).toString().padStart(2, '0')}  /  ${n.toString().padStart(2, '0')}",
            color = TalkInk.Mist.copy(alpha = chromeAlpha),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
    }
    val progress = if (n <= 1) 1f else session.index / (n - 1).toFloat()
    val accent = slide.phase.accent()
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(TalkInk.Line)
            .drawBehind {
                drawLine(
                    color = accent,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width * progress, size.height / 2f),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round,
                )
            },
    )
}

@Composable
private fun TalkBottomBar(session: TalkSession, chromeAlpha: Float) {
    Column {
        if (session.notesVisible) {
            Text(
                text = session.current.notes,
                color = TalkInk.Mist,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { session.prev() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous slide",
                    tint = TalkInk.Mute.copy(alpha = chromeAlpha),
                )
            }
            Text(
                text = "vol  ·  d-pad  ·  space",
                color = TalkInk.Dim.copy(alpha = chromeAlpha),
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = if (session.notesVisible) "NOTES ON" else "NOTES",
                color = if (session.notesVisible) TalkInk.Amber else TalkInk.Dim,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { session.toggleNotes() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { session.next() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next slide",
                    tint = TalkInk.Mist.copy(alpha = chromeAlpha),
                )
            }
        }
    }
}

@Composable
private fun KeepPresentationAwake() {
    val view = LocalView.current
    DisposableEffect(view) {
        val previous = view.keepScreenOn
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }
}

@Composable
private fun LockLandscape() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val activity = context as? Activity
        val previous = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            if (previous != null) activity.requestedOrientation = previous
        }
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
private fun MotionOsPreview() {
    MotionOsApp(onOpenLab = {})
}
