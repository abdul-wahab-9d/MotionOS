package com.sample.texteditor.showcase.talk

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.texteditor.showcase.AuroraUnlockScreen
import com.sample.texteditor.showcase.CanvasPulseChartDemo
import com.sample.texteditor.showcase.KeyframesThemeWipeDemo
import com.sample.texteditor.showcase.MorphingActionButtonDemo
import com.sample.texteditor.showcase.NeonRushGame
import com.sample.texteditor.showcase.OrbitalMenuLayoutDemo
import com.sample.texteditor.showcase.SharedElementGalleryDemo

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.TalkSlideBody(
    slide: DeckSlide,
    session: TalkSession,
    visibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    when (val kind = slide.kind) {
        SlideKind.Title -> TitleBody(session, modifier)
        is SlideKind.Beats -> BeatsBody(slide, kind, modifier)
        is SlideKind.Tree -> TreeBody(slide, kind, session, visibilityScope, modifier)
        SlideKind.Specs -> SpecsBody(slide, modifier)
        is SlideKind.Demo -> DemoCopy(slide, kind, visibilityScope, modifier)
        SlideKind.Excess -> ExcessChooser(modifier)
        SlideKind.Apply -> ApplyBody(slide, modifier)
        SlideKind.Qa -> QaBody(modifier)
        is SlideKind.Code -> CodeBody(kind, modifier)
    }
}

@Composable
private fun TitleBody(session: TalkSession, modifier: Modifier = Modifier) {
    SlideColumn(
        modifier = modifier.padding(horizontal = 48.dp, vertical = 8.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "MOTION OS",
            color = TalkInk.Violet,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Compose Motion & Pixels",
            color = TalkInk.Mist,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Choosing the right API  ·  live demos in this app",
            color = TalkInk.Mute,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(32.dp))
        HoldToStartCore(
            onComplete = { session.onHoldComplete() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "This deck is one updateTransition. Every flourish names its API.",
            color = TalkInk.Dim,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BeatsBody(slide: DeckSlide, kind: SlideKind.Beats, modifier: Modifier = Modifier) {
    SlideColumn(modifier.padding(horizontal = 48.dp, vertical = 12.dp)) {
        SlideHeading(slide)
        Spacer(Modifier.height(20.dp))
        StaggerColumn(kind.bullets, numbered = slide.id != "restraint")
        kind.footnote?.let {
            Spacer(Modifier.height(20.dp))
            Text(it, color = TalkInk.Mute, fontSize = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.TreeBody(
    slide: DeckSlide,
    kind: SlideKind.Tree,
    session: TalkSession,
    visibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    SlideColumn(modifier.padding(horizontal = 40.dp, vertical = 8.dp)) {
        SlideHeading(slide)
        Spacer(Modifier.height(12.dp))
        DecisionTree(
            heading = kind.heading,
            branches = kind.branches,
            onJump = { id -> session.goTo(id) },
            wrapApi = { sharedKey, content ->
                if (sharedKey != null) {
                    Box(
                        Modifier.sharedElement(
                            state = rememberSharedContentState(sharedKey),
                            animatedVisibilityScope = visibilityScope,
                        ),
                    ) { content() }
                } else {
                    content()
                }
            },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tappable branches jump to live proof.",
            color = TalkInk.Dim,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SpecsBody(slide: DeckSlide, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 8.dp)) {
        SlideHeading(slide)
        Spacer(Modifier.height(12.dp))
        SpecsPlayground(Modifier.weight(1f).fillMaxWidth())
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun SharedTransitionScope.DemoCopy(
    slide: DeckSlide,
    kind: SlideKind.Demo,
    visibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    SlideColumn(modifier.padding(start = 32.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)) {
        val title: @Composable () -> Unit = {
            Text(
                text = slide.title,
                color = TalkInk.Mist,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = slide.kicker,
            color = slide.phase.accent(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(8.dp))
        if (kind.sharedKey != null) {
            Box(
                Modifier.sharedElement(
                    state = rememberSharedContentState(kind.sharedKey),
                    animatedVisibilityScope = visibilityScope,
                ),
            ) { title() }
        } else {
            title()
        }
        Spacer(Modifier.height(14.dp))
        StaggerColumn(kind.beats, numbered = false)
        Spacer(Modifier.height(14.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            kind.apis.forEach { api ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(api, color = TalkInk.Cyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        CodeCaption(kind.code, kind.accentLine)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun TalkDemoStage(demoId: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(TalkInk.Bg)) {
        when (demoId) {
            "morphing_button" -> MorphingActionButtonDemo(Modifier.fillMaxSize().padding(top = 24.dp))
            "keyframes_wipe" -> KeyframesThemeWipeDemo(Modifier.fillMaxSize())
            "shared_elements" -> SharedElementGalleryDemo(Modifier.fillMaxSize())
            "canvas_chart" -> CanvasPulseChartDemo(Modifier.fillMaxSize())
            "orbital_layout" -> OrbitalMenuLayoutDemo(Modifier.fillMaxSize())
            "aurora_unlock" -> PortraitPhoneStage { AuroraUnlockScreen(Modifier.fillMaxSize()) }
            "neon_rush" -> PortraitPhoneStage { NeonRushGame(Modifier.fillMaxSize()) }
        }
    }
}

@Composable
internal fun ExcessChooser(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf<String?>(null) }
    if (expanded == "aurora") {
        Box(modifier.fillMaxSize()) {
            PortraitPhoneStage { AuroraUnlockScreen(Modifier.fillMaxSize()) }
            ExcessClose { expanded = null }
        }
        return
    }
    if (expanded == "neon") {
        Box(modifier.fillMaxSize()) {
            PortraitPhoneStage { NeonRushGame(Modifier.fillMaxSize()) }
            ExcessClose { expanded = null }
        }
        return
    }
    Row(
        modifier = modifier.fillMaxSize().padding(40.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExcessCard(
            kicker = "A · CINEMATIC UI",
            title = "Aurora Unlock",
            body = "Hold the core. Animatable, springs, PathMeasure, shared clock.",
            modifier = Modifier.weight(1f),
            onClick = { expanded = "aurora" },
        )
        ExcessCard(
            kicker = "B · GAME LOOP",
            title = "Neon Rush",
            body = "withFrameNanos racer. Same Canvas primitives. Not default product UI.",
            modifier = Modifier.weight(1f),
            onClick = { expanded = "neon" },
        )
    }
}

@Composable
private fun CodeBody(kind: SlideKind.Code, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "CODE",
            color = TalkInk.Amber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = kind.label,
            color = TalkInk.Mist,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF020617))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            kind.lines.forEachIndexed { i, line ->
                if (line.isEmpty()) {
                    Spacer(Modifier.height(6.dp))
                } else {
                    Text(
                        text = line,
                        color = if (i in kind.accentLines) TalkInk.Lime else TalkInk.Mute,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = if (i in kind.accentLines) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Navigate here with  goTo(\"${kind.label.substringBefore(" —").lowercase().replace(" ", "_")}\")  · advance to resume",
            color = TalkInk.Dim,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun PortraitPhoneStage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TalkInk.Bg),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val captionReserve = 28.dp
            val maxFrameHeight = (maxHeight * 0.92f - captionReserve).coerceAtLeast(120.dp)
            val maxFrameWidth = minOf(maxWidth * 0.42f, 420.dp, maxFrameHeight * 9f / 16f)
            val w = maxFrameWidth
            val h = w * 16f / 9f
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(w, h)
                        .clip(RoundedCornerShape(32.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(32.dp))
                        .background(TalkInk.Bg),
                ) {
                    content()
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Portrait canvas  ·  phone-shaped on purpose",
                    color = TalkInk.Dim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.6.sp,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ExcessClose(onClick: () -> Unit) {
    Text(
        text = "CLOSE",
        color = Color.White.copy(alpha = 0.8f),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 2.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 20.dp, end = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun ExcessCard(
    kicker: String,
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(TalkInk.Panel)
            .clickable(onClick = onClick)
            .padding(28.dp),
    ) {
        Text(kicker, color = TalkInk.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, color = TalkInk.Mist, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(body, color = TalkInk.Mute, fontSize = 16.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(24.dp))
        Text("Tap to play  →", color = TalkInk.Cyan, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ApplyBody(slide: DeckSlide, modifier: Modifier = Modifier) {
    SlideColumn(modifier.padding(horizontal = 40.dp, vertical = 8.dp)) {
        SlideHeading(slide)
        Spacer(Modifier.height(12.dp))
        ApplyTable()
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun QaBody(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Q&A", color = TalkInk.Teal, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
        Spacer(Modifier.height(12.dp))
        Text("Where have we fought motion jank?", color = TalkInk.Mist, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Any design ask that forced Canvas prematurely?\nWho wants a follow-up on Nav + shared elements?",
            color = TalkInk.Mute,
            fontSize = 20.sp,
            lineHeight = 30.sp,
        )
    }
}

@Composable
internal fun SlideHeading(slide: DeckSlide) {
    Text(
        text = slide.kicker,
        color = slide.phase.accent(),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 4.sp,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = slide.title,
        color = TalkInk.Mist,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
    )
}
