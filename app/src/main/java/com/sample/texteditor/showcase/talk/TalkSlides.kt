package com.sample.texteditor.showcase.talk

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.texteditor.showcase.CanvasPulseChartDemo
import com.sample.texteditor.showcase.KeyframesThemeWipeDemo
import com.sample.texteditor.showcase.MorphingActionButtonDemo
import com.sample.texteditor.showcase.OrbitalMenuLayoutDemo
import com.sample.texteditor.showcase.PortraitDemoActivity
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
        SlideKind.ApiCatalog -> ApiCatalogBody(slide, modifier)
        is SlideKind.Demo -> DemoCopy(slide, kind, visibilityScope, modifier)
        SlideKind.Excess -> ExcessChooser(modifier)
        SlideKind.Apply -> ApplyBody(slide, modifier)
        SlideKind.Qa -> QaBody(slide, modifier)
        SlideKind.Thanks -> ThankYouSlide(modifier)
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
private fun ApiCatalogBody(slide: DeckSlide, modifier: Modifier = Modifier) {
    SlideColumn(modifier.padding(horizontal = 40.dp, vertical = 8.dp)) {
        SlideHeading(slide)
        Spacer(Modifier.height(12.dp))
        AnimationApiCatalog()
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
            "aurora_unlock" -> PortraitLaunchPane(
                demoId = PortraitDemoActivity.DEMO_AURORA,
                title = "Aurora Unlock",
            )
            "neon_rush" -> PortraitLaunchPane(
                demoId = PortraitDemoActivity.DEMO_NEON,
                title = "Neon Rush",
            )
        }
    }
}

@Composable
internal fun ExcessChooser(modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
            onClick = { PortraitDemoActivity.open(context, PortraitDemoActivity.DEMO_AURORA) },
        )
        ExcessCard(
            kicker = "B · GAME LOOP",
            title = "Neon Rush",
            body = "withFrameNanos racer. Same Canvas primitives. Not default product UI.",
            modifier = Modifier.weight(1f),
            onClick = { PortraitDemoActivity.open(context, PortraitDemoActivity.DEMO_NEON) },
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
private fun PortraitLaunchPane(
    demoId: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(TalkInk.Panel)
            .clickable { PortraitDemoActivity.open(context, demoId) }
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("PORTRAIT", color = TalkInk.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, color = TalkInk.Mist, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "Opens a portrait activity. Rotate the emulator to fill the screen.",
            color = TalkInk.Mute,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Spacer(Modifier.height(24.dp))
        Text("Tap to open portrait  →", color = TalkInk.Cyan, fontWeight = FontWeight.SemiBold)
    }
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
        Text("Tap to open portrait  →", color = TalkInk.Cyan, fontWeight = FontWeight.SemiBold)
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
private fun QaBody(slide: DeckSlide, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 16.dp),
    ) {
        SlideHeading(slide)
        Spacer(Modifier.height(20.dp))
        Text(
            "Where have we fought motion jank?",
            color = TalkInk.Mist,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Any design ask that forced Canvas prematurely?\nWho wants a follow-up on Nav + shared elements?",
            color = TalkInk.Mute,
            fontSize = 18.sp,
            lineHeight = 26.sp,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "TAKE HOME",
            color = TalkInk.Teal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TakeHomeCard(
                kicker = "LAYOUT",
                body = "Measure children, then placeRelative — that’s custom layout from scratch. Canvas is for pixels, not for placing children.",
                modifier = Modifier.weight(1f),
            )
            TakeHomeCard(
                kicker = "ANIMATION",
                body = "Visibility you know. Custom means Animatable + a spec. Story timing is keyframes. Physics is spring. Continuity across screens is shared elements.",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TakeHomeCard(
    kicker: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TalkInk.Panel)
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Text(
            kicker,
            color = TalkInk.Cyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            body,
            color = TalkInk.Mist,
            fontSize = 16.sp,
            lineHeight = 24.sp,
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
