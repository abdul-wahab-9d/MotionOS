package com.sample.texteditor.showcase.talk

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Chapters of the talk — one state machine drives chrome. */
enum class TalkPhase {
    Idle,
    Map,
    Proof,
    Draw,
    Layout,
    Excess,
    Ship,
    Close,
}

enum class SlideLayout {
    Full,
    Split,
    FullBleed,
}

@Immutable
data class TreeBranch(
    val question: String,
    val api: String,
    val jumpToId: String? = null,
    val sharedKey: String? = null,
)

@Immutable
sealed interface SlideKind {
    data object Title : SlideKind

    data class Beats(
        val bullets: List<String>,
        val footnote: String? = null,
    ) : SlideKind

    data class Tree(
        val heading: String,
        val branches: List<TreeBranch>,
    ) : SlideKind

    data object Specs : SlideKind
    data object ApiCatalog : SlideKind

    data class Demo(
        val demoId: String,
        val beats: List<String>,
        val apis: List<String>,
        val code: List<String>,
        val accentLine: Int = 0,
        val sharedKey: String? = null,
    ) : SlideKind

    data object Excess : SlideKind
    data object Comparison : SlideKind
    data object Apply : SlideKind
    data object Qa : SlideKind
    data object Thanks : SlideKind

    data class Code(
        val label: String,
        val lines: List<String>,
        val accentLines: Set<Int> = emptySet(),
        val studioFile: String = "",
    ) : SlideKind
}

@Immutable
data class DeckSlide(
    val id: String,
    val phase: TalkPhase,
    val layout: SlideLayout,
    val kicker: String,
    val title: String,
    val notes: String,
    val kind: SlideKind,
)

@Stable
class TalkSession : TalkClickerHandler {
    var index by mutableIntStateOf(0)
        private set
    var shortTrack by mutableStateOf(false)
        private set
    var notesVisible by mutableStateOf(false)
        private set
    var started by mutableStateOf(false)
        private set

    val slides: List<DeckSlide>
        get() = buildTalkDeck(shortTrack)

    val current: DeckSlide
        get() = slides.getOrElse(index) { slides.first() }

    override fun next(): Boolean {
        started = true
        val last = slides.lastIndex
        if (index >= last) return true
        index++
        return true
    }

    override fun prev(): Boolean {
        if (index <= 0) return true
        index--
        return true
    }

    override fun toggleNotes(): Boolean {
        notesVisible = !notesVisible
        return true
    }

    fun goTo(id: String) {
        started = true
        val i = slides.indexOfFirst { it.id == id }
        if (i >= 0) index = i
    }

    fun onHoldComplete() {
        started = true
        if (index == 0 && slides.size > 1) index = 1
    }

    fun toggleShortTrack() {
        val id = current.id
        shortTrack = !shortTrack
        val next = slides.indexOfFirst { it.id == id }
        index = if (next >= 0) next else index.coerceIn(0, slides.lastIndex)
    }
}

fun buildTalkDeck(shortTrack: Boolean): List<DeckSlide> {
    val all = listOf(
        DeckSlide(
            id = "title",
            phase = TalkPhase.Idle,
            layout = SlideLayout.Full,
            kicker = "MOTION OS",
            title = "Compose Motion & Pixels",
            notes = "Today is not every animation API. It’s how we choose — with live demos in this app.",
            kind = SlideKind.Title,
        ),
        DeckSlide(
            id = "goal",
            phase = TalkPhase.Map,
            layout = SlideLayout.Full,
            kicker = "GOAL",
            title = "Leave knowing which API",
            notes = "Success: next time design asks for a morphing pay button, you pick updateTransition on purpose.",
            kind = SlideKind.Beats(
                bullets = listOf(
                    "Which API for which problem — not a catalog dump",
                    "When Canvas / custom Layout is actually worth it",
                    "Shared performance rules we can ship with",
                ),
            ),
        ),
        DeckSlide(
            id = "agenda",
            phase = TalkPhase.Map,
            layout = SlideLayout.Full,
            kicker = "AGENDA",
            title = "Five beats, then Q&A",
            notes = "Walk the list. Promise the demos are the point.",
            kind = SlideKind.Beats(
                bullets = listOf(
                    "Animation decision tree + live proof",
                    "Graphics & Layout tree + live proof",
                    "Performance checklist",
                    "Apply to our apps",
                    "Q&A",
                ),
            ),
        ),
        DeckSlide(
            id = "restraint",
            phase = TalkPhase.Map,
            layout = SlideLayout.Full,
            kicker = "RESTRAINT",
            title = "When not to animate",
            notes = "Compose makes custom motion easy. That doesn’t mean every screen needs it.",
            kind = SlideKind.Beats(
                bullets = listOf(
                    "Prefer system / Material defaults first",
                    "Motion must serve hierarchy or feedback",
                    "If it fights readability or a11y — cut it",
                    "Games and Canvas are rare in product UI",
                ),
                footnote = "Custom is for moments that sell the product.",
            ),
        ),
        DeckSlide(
            id = "anim_tree",
            phase = TalkPhase.Map,
            layout = SlideLayout.Full,
            kicker = "ANIMATION",
            title = "Need motion?",
            notes = "Spine of the talk. We’ll demo the middle and bottom. Ask: where would a checkout button sit?",
            kind = SlideKind.Tree(
                heading = "Tap a branch we will prove",
                branches = listOf(
                    TreeBranch(
                        question = "Appear / disappear?",
                        api = "AnimatedVisibility",
                        jumpToId = "avis",
                        sharedKey = "branch-animatedVisibility",
                    ),
                    TreeBranch("Swap content / screens?", "AnimatedContent"),
                    TreeBranch(
                        question = "Many props, one state?",
                        api = "updateTransition",
                        jumpToId = "morph",
                        sharedKey = "branch-updateTransition",
                    ),
                    TreeBranch(
                        question = "Imperative / interruptible?",
                        api = "Animatable",
                        jumpToId = "wipe",
                        sharedKey = "branch-animatable",
                    ),
                    TreeBranch(
                        question = "Gesture / organic settle?",
                        api = "spring()",
                        jumpToId = "orbital",
                        sharedKey = "branch-spring",
                    ),
                    TreeBranch(
                        question = "Authored “story” timing?",
                        api = "keyframes",
                        jumpToId = "wipe",
                        sharedKey = "branch-keyframes",
                    ),
                    TreeBranch(
                        question = "List ↔ detail continuity?",
                        api = "SharedTransition",
                        jumpToId = "shared",
                        sharedKey = "branch-shared",
                    ),
                    TreeBranch(
                        question = "Per-frame draw / game?",
                        api = "withFrameNanos + Canvas",
                        jumpToId = "excess",
                        sharedKey = "branch-frame",
                    ),
                ),
            ),
        ),
        DeckSlide(
            id = "specs",
            phase = TalkPhase.Map,
            layout = SlideLayout.Full,
            kicker = "SPECS",
            title = "Intent, not decoration",
            notes = "Wrong spec = uncanny motion even if the values are fine. Play the same chip four ways.",
            kind = SlideKind.Specs,
        ),
        DeckSlide(
            id = "api_catalog",
            phase = TalkPhase.Map,
            layout = SlideLayout.Full,
            kicker = "CATALOG",
            title = "Animation APIs — what they do",
            notes = "Don’t read the table. The tree picks; this names them. Point at Appear, Drive, Specs, Continuity, Frame — then demo.",
            kind = SlideKind.ApiCatalog,
        ),
        DeckSlide(
            id = "avis",
            phase = TalkPhase.Proof,
            layout = SlideLayout.Split,
            kicker = "PROOF",
            title = "AnimatedVisibility",
            notes = "The baseline everyone already knows — made concrete. Same card, three Enter/Exit pairs, toggled live.",
            kind = SlideKind.Demo(
                demoId = "animated_visibility",
                beats = listOf(
                    "One boolean. Enter plays in, Exit plays out.",
                    "Combine transitions with + — fadeIn() + expandVertically().",
                    "Error banners, optional fields, tooltips — this is most of our motion.",
                ),
                apis = listOf("AnimatedVisibility", "EnterTransition", "ExitTransition"),
                code = listOf(
                    "AnimatedVisibility(visible = state) {",
                    "  Card { /* content */ }",
                    "}",
                ),
                accentLine = 0,
                sharedKey = "branch-animatedVisibility",
            ),
        ),
        DeckSlide(
            id = "morph",
            phase = TalkPhase.Proof,
            layout = SlideLayout.Split,
            kicker = "PROOF",
            title = "Morphing Action Button",
            notes = "Idle → Loading → Success/Error. Structure leads, label trails ~120ms. PathMeasure draws the check.",
            kind = SlideKind.Demo(
                demoId = "morphing_button",
                beats = listOf(
                    "One state. Width, color, glyph stay in lockstep.",
                    "Tween the structure. Spring the glyph.",
                    "Pay / save / sync in our apps.",
                ),
                apis = listOf("updateTransition", "animateDp", "PathMeasure", "graphicsLayer"),
                code = listOf(
                    "updateTransition(state)",
                    "animateDp { Idle → 280.dp; Loading → 66.dp }",
                    "textAlpha  delayMillis = 120  // structure leads",
                ),
                accentLine = 2,
                sharedKey = "branch-updateTransition",
            ),
        ),
        DeckSlide(
            id = "code_morph",
            phase = TalkPhase.Proof,
            layout = SlideLayout.Full,
            kicker = "PSEUDOCODE",
            title = "updateTransition — morphing button",
            notes = "Pseudocode only — open MorphingActionButton.kt in Studio for the real file.",
            kind = SlideKind.Code(
                label = "updateTransition — morphing button",
                lines = listOf(
                    "one state  →  many properties",
                    "",
                    "updateTransition(buttonState)",
                    "  width   ← tween     Idle / Loading / Done",
                    "  color   ← tween",
                    "  label   ← tween + 120ms delay   // structure leads",
                    "  glyph   ← spring                // micro-interaction",
                ),
                accentLines = setOf(2, 5, 6),
                studioFile = "showcase/components/MorphingActionButton.kt",
            ),
        ),
        DeckSlide(
            id = "wipe",
            phase = TalkPhase.Proof,
            layout = SlideLayout.FullBleed,
            kicker = "PROOF",
            title = "Keyframes Theme Wipe",
            notes = "Race ahead, overshoot, settle. Animation drives clipRect — a drawing primitive.",
            kind = SlideKind.Demo(
                demoId = "keyframes_wipe",
                beats = listOf("Tap to wipe light ↔ dark."),
                apis = listOf("Animatable", "keyframes", "clipRect"),
                code = listOf(
                    "keyframes {",
                    "  0.90f at 520  using cinematic",
                    "  overshoot at 700",
                    "  target at 850",
                    "}",
                ),
                accentLine = 2,
                sharedKey = "branch-keyframes",
            ),
        ),
        DeckSlide(
            id = "code_wipe",
            phase = TalkPhase.Proof,
            layout = SlideLayout.Full,
            kicker = "PSEUDOCODE",
            title = "keyframes + clipRect wipe",
            notes = "Pseudocode only — open KeyframesThemeWipeDemo.kt in Studio. Steal clipToFraction.",
            kind = SlideKind.Code(
                label = "keyframes + clipRect wipe",
                lines = listOf(
                    "Animatable fraction   0 → 1",
                    "",
                    "keyframes:",
                    "  race to 90%     @ 520ms",
                    "  overshoot       @ 700ms",
                    "  settle          @ 850ms",
                    "",
                    "clipRect(width × fraction)   // draw, not layout",
                ),
                accentLines = setOf(0, 3, 4, 5, 7),
                studioFile = "showcase/KeyframesThemeWipeDemo.kt",
            ),
        ),
        DeckSlide(
            id = "shared",
            phase = TalkPhase.Proof,
            layout = SlideLayout.Split,
            kicker = "PROOF",
            title = "Shared Element Gallery",
            notes = "Stable unique keys. Don’t share huge subtrees. Test back stack in real nav.",
            kind = SlideKind.Demo(
                demoId = "shared_elements",
                beats = listOf(
                    "The card travels — it doesn't swap.",
                    "Keys: swatch-id, title-id.",
                    "Compose's answer to hero transitions.",
                ),
                apis = listOf("SharedTransitionLayout", "sharedElement", "AnimatedContent"),
                code = listOf(
                    "SharedTransitionLayout {",
                    "  Modifier.sharedElement(",
                    "    rememberSharedContentState(\"swatch-\$id\")",
                    "  )",
                    "}",
                ),
                accentLine = 2,
                sharedKey = "branch-shared",
            ),
        ),
        DeckSlide(
            id = "gfx_tree",
            phase = TalkPhase.Draw,
            layout = SlideLayout.Full,
            kicker = "GRAPHICS",
            title = "Draw or place?",
            notes = "Canvas is not a layout system. Layout is not a drawing API.",
            kind = SlideKind.Tree(
                heading = "Custom drawing or layout?",
                branches = listOf(
                    TreeBranch("Tint / shape / clip only?", "Modifier / Shape"),
                    TreeBranch(
                        question = "Charts, gauges, game art?",
                        api = "Canvas + DrawScope",
                        jumpToId = "chart",
                        sharedKey = "branch-canvas",
                    ),
                    TreeBranch(
                        question = "Children in custom positions?",
                        api = "custom Layout",
                        jumpToId = "orbital",
                        sharedKey = "branch-layout",
                    ),
                    TreeBranch("Deferred / dependent measure?", "SubcomposeLayout"),
                ),
            ),
        ),
        DeckSlide(
            id = "chart",
            phase = TalkPhase.Draw,
            layout = SlideLayout.Split,
            kicker = "DRAW",
            title = "Canvas Pulse Chart",
            notes = "Grid, area path, stroke, bars — all DrawScope. One Animatable(0→1); bars stagger locally.",
            kind = SlideKind.Demo(
                demoId = "canvas_chart",
                beats = listOf(
                    "Designer gives you a custom chart? This is where you land.",
                    "One fraction. Many properties.",
                    "Same cinematic pattern as UI morphs.",
                ),
                apis = listOf("Canvas", "Path", "Animatable", "stagger"),
                code = listOf(
                    "val t = remember { Animatable(0f) }",
                    "Canvas { drawPath(area, brush) }",
                    "localT = ((t - i * 0.06f) / 0.7f)",
                ),
                accentLine = 0,
                sharedKey = "branch-canvas",
            ),
        ),
        DeckSlide(
            id = "orbital",
            phase = TalkPhase.Layout,
            layout = SlideLayout.Split,
            kicker = "LAYOUT",
            title = "Fan Deck",
            notes = "No Row/Column trigonometry hacks. Measure children, place on an ellipse, zIndex brings the front card forward. Swipe settles with a spring.",
            kind = SlideKind.Demo(
                demoId = "orbital_layout",
                beats = listOf(
                    "Layout places. Canvas draws the ring.",
                    "zIndex = frontness. graphicsLayer sells depth.",
                    "Swipe / tap — spring settle. Product: wallet, stories, picker.",
                ),
                apis = listOf("Layout", "placeRelative", "zIndex", "graphicsLayer", "spring"),
                code = listOf(
                    "val a = PI/2 + 2π * (i - selected) / n",
                    "p.placeRelative(x, y, zIndex = frontness)",
                    "// graphicsLayer { rotationY = …; scale = depth }",
                ),
                accentLine = 1,
                sharedKey = "branch-layout",
            ),
        ),
        DeckSlide(
            id = "code_orbital",
            phase = TalkPhase.Layout,
            layout = SlideLayout.Full,
            kicker = "PSEUDOCODE",
            title = "custom Layout — fan deck",
            notes = "Pseudocode only — open OrbitalMenuLayoutDemo.kt in Studio. zIndex vs graphicsLayer is the split.",
            kind = SlideKind.Code(
                label = "custom Layout — fan deck",
                lines = listOf(
                    "Layout:",
                    "  measure each child",
                    "  place on ellipse(selected)",
                    "  zIndex = frontness          // who is in front",
                    "",
                    "graphicsLayer:  rotationY, scale   // depth, not placement",
                    "Canvas:         draw the ring",
                    "",
                    "spring settle after swipe",
                ),
                accentLines = setOf(2, 3, 5),
                studioFile = "showcase/OrbitalMenuLayoutDemo.kt",
            ),
        ),
        DeckSlide(
            id = "excess",
            phase = TalkPhase.Excess,
            layout = SlideLayout.FullBleed,
            kicker = "INTENSITY",
            title = "Same toolbox. Different intensity.",
            notes = "Pick ONE. Tap opens a portrait activity — rotate the emulator. Aurora = cinematic UI. Neon Rush = game loop. Back returns to the landscape deck.",
            kind = SlideKind.Excess,
        ),
        DeckSlide(
            id = "comparison",
            phase = TalkPhase.Ship,
            layout = SlideLayout.Full,
            kicker = "WHY COMPOSE",
            title = "Same behavior. Fewer moving parts.",
            notes = "Tap a row to reveal the Compose side. XML column is structural — real API shape, not a specific implementation's line count. Don't over-claim; the point is fewer objects to wire together, not \"Compose is magic.\"",
            kind = SlideKind.Comparison,
        ),
        DeckSlide(
            id = "perf",
            phase = TalkPhase.Ship,
            layout = SlideLayout.Full,
            kicker = "SHIP",
            title = "Performance checklist",
            notes = "Walk bullets with what breaks if we ignore this.",
            kind = SlideKind.Beats(
                bullets = listOf(
                    "graphicsLayer / draw / offset { } — not layout every frame",
                    "Cache Path / paints in hot 60fps loops",
                    "One shared clock for coordinated effects",
                    "Don’t animate a state read in every parent",
                    "Shared elements: stable keys, small regions",
                    "Shaders / AGSL: API guards + fallbacks",
                ),
            ),
        ),
        DeckSlide(
            id = "apply",
            phase = TalkPhase.Ship,
            layout = SlideLayout.Full,
            kicker = "OUR APPS",
            title = "Motion moments → right API",
            notes = "Tap chips to reveal — capture 2–3 real tickets with the room. Canvas only when components can’t express the design.",
            kind = SlideKind.Apply,
        ),
        DeckSlide(
            id = "resources",
            phase = TalkPhase.Ship,
            layout = SlideLayout.Full,
            kicker = "LEAVE-BEHIND",
            title = "The lab is this repo",
            notes = "Lab files map 1:1 to demos today. Clicker: volume ↓ / D-pad / space — next slide.",
            kind = SlideKind.Beats(
                bullets = listOf(
                    "Compose Lab → app/.../showcase/",
                    "Handout → docs/compose-lab-handout.md",
                    "PDF deck → docs/talk/compose-motion-pixels.pdf",
                    "github.com/abdul-wahab-9d/MotionOS",
                ),
                footnote = "No tablet? Phone layout is a tap-through lab.",
            ),
        ),
        DeckSlide(
            id = "qa",
            phase = TalkPhase.Ship,
            layout = SlideLayout.Full,
            kicker = "Q&A",
            title = "Your motion bugs, please",
            notes = "If silent, use the prompts. Close on the two takeaways at the bottom — Layout vs Animation in one breath each.",
            kind = SlideKind.Qa,
        ),
        DeckSlide(
            id = "thanks",
            phase = TalkPhase.Close,
            layout = SlideLayout.FullBleed,
            kicker = "CLOSE",
            title = "Thank You",
            notes = "Let the letters land. Credit Hammad Nawaz and the team. Tap to replay the close. Don’t talk over the cascade.",
            kind = SlideKind.Thanks,
        ),
    )

    if (!shortTrack) return all
    val skip = setOf("excess", "code_morph", "code_wipe", "code_orbital")
    return all.filterNot { it.id in skip }
}
