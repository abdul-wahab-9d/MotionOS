---
name: android-compose-motion-decisions
description: |
  Team decision framework for choosing the right Jetpack Compose animation, graphics, or layout API — distilled from the "Compose Motion & Pixels" team talk (MotionOS repo: github.com/abdul-wahab-9d/MotionOS). Covers when NOT to animate, the animation decision tree (AnimatedVisibility / AnimatedContent / updateTransition / Animatable / spring / keyframes / SharedTransitionLayout / Canvas frame loop), the graphics & layout decision tree (Modifier vs Canvas vs custom Layout), a spec-intent cheat sheet, a performance checklist, and an XML/View-animation → Compose translation table for developers who have only worked in XML.

  USE THIS SKILL whenever a request involves adding or reviewing motion/animation/custom drawing in Compose, especially for developers coming from XML views. Trigger on: "add animation", "should this animate", "which animation API", "animate this button/screen", "morph a button", "loading state animation", "shared element transition", "list to detail transition", "custom chart in Compose", "custom Layout in Compose", "coming from XML animations", "ObjectAnimator to Compose", "MotionLayout to Compose", "ValueAnimator equivalent in Compose", "how do I animate this in Compose".

  Always search official documentation to find the recommended APIs for Animations in Compose before locking an implementation choice.
---

# Compose Motion — Team Decision Framework

Source talk: "Compose Motion & Pixels: Choosing the Right API" (MotionOS repo). This skill is the
decision framework from that talk, not a syntax reference — for deep production technique
patterns (shimmer internals, `AnchoredDraggable`, `RuntimeShader`, staggered cascades, time
remapping, etc.) see the **`android-compose-animation`** skill, which this one complements.

Live reference implementations for every decision below exist in the MotionOS repo
(`app/src/main/java/com/sample/texteditor/showcase/`, `github.com/abdul-wahab-9d/MotionOS`) —
open the file named in each row for the real, working source. Code shown here is illustrative
only, not copy-paste-complete.

## 0. Always check official docs first

**Always search official documentation to find the recommended APIs for Animations in Compose.**

Do this before locking a choice from the trees below, and again when an API feels ambiguous or may have changed:

1. Search / open current Android developer docs for Jetpack Compose animation (e.g. [Animation](https://developer.android.com/develop/ui/compose/animation), [Animation overview](https://developer.android.com/develop/ui/compose/animation/introduction), Shared element / continuity docs).
2. Prefer the API the official docs recommend for that problem over training-data habit or outdated samples.
3. Use this skill’s decision trees and MotionOS demos as the team’s *intent* map — confirm signatures, experimental flags, and replacement APIs against the docs of the Compose / BOM version this project actually uses.
4. If docs and this skill disagree, follow the docs and note the mismatch.

## 1. Restraint first — decide whether to animate at all

Before picking an API, check these in order:

- Prefer system / Material defaults first — don't hand-roll motion Material already provides.
- Motion must serve hierarchy or feedback (state change, causality, spatial continuity) — not decoration.
- If it fights readability or accessibility (reduced-motion, focus order, contrast during transition) → cut it.
- Games/particle Canvas loops are rare in product UI — reserve for genuinely game-like moments.
- Most screens should stay on Material `AnimatedVisibility` / `AnimatedContent`. Custom motion (this skill) is for moments that sell the product: primary CTA, list↔detail continuity, onboarding flourish, a bespoke chart or picker.

## 2. Animation decision tree

```
Need motion?
├── Appear/disappear?           → AnimatedVisibility                (§ AnimatedVisibilityDemo.kt)
├── Swap content/screens?       → AnimatedContent
├── Many props, one state?      → updateTransition                 (§ MorphingActionButton.kt)
├── Imperative / interruptible? → Animatable                       (§ KeyframesThemeWipeDemo.kt, AuroraUnlockScreen.kt)
├── Gesture / organic settle?   → spring()                         (§ OrbitalMenuLayoutDemo.kt)
├── Authored "story" timing?    → keyframes                        (§ KeyframesThemeWipeDemo.kt)
├── List ↔ detail continuity?   → SharedTransitionLayout            (§ SharedElementGalleryDemo.kt)
└── Per-frame draw / game?      → withFrameNanos + Canvas          (§ NeonRushGame.kt)
```

Ask "where would this button/screen sit?" and route to exactly one branch. Multiple properties
changing together on one state machine (width, color, label, glyph) is the single strongest
signal for `updateTransition` — it is the most common right answer for product UI (payment
buttons, save/sync actions, form submit).

## 3. Graphics & layout decision tree

```
Custom drawing or layout?
├── Tint / shape / clip only?     → Modifier / Shape
├── Charts, gauges, game art?     → Canvas + DrawScope             (§ CanvasPulseChartDemo.kt)
├── Children in custom positions? → custom Layout                  (§ OrbitalMenuLayoutDemo.kt)
└── Deferred / dependent measure? → SubcomposeLayout
```

**Rule of thumb:** Canvas draws pixels; it does not place children. `Layout` places children; it
does not draw pixels. Mixing the two responsibilities is how custom UI becomes unmaintainable —
keep drawing (`Canvas`/`drawWithContent`) and placement (`Layout`/`placeRelative`) as separate
concerns even within the same composable (see `OrbitalMenuLayoutDemo.kt`: `Layout` places on an
ellipse, `graphicsLayer` sells depth via rotation/scale, `Canvas` draws the ring — three distinct
jobs, three distinct APIs).

## 4. Spec intent cheat sheet

Specs encode intent — the wrong spec produces uncanny motion even with correct values.

| Spec | Feels like | Use when |
|---|---|---|
| `tween` | Directed, timed | Enter/exit, structured morph |
| `spring` | Physical | Drag release, playful/interactive UI |
| `keyframes` | Hand-authored | Overshoot, pauses, cinematic story timing |
| `infiniteRepeatable` | Loop | Loaders, ambient effects (use sparingly) |

Container/shell animations (width, position of the whole element) generally want `tween` or a
low-bounce `spring` (`Spring.DampingRatioNoBouncy`); content *inside* the container (a glyph,
an icon scale) can take a bouncier `spring` as a micro-interaction — see `animateDp` (structure)
vs. `glyphScale` (spring, bouncy) in `MorphingActionButton.kt`.

## 5. Coordinating multiple properties on one state

When a component changes several properties together (width, color, label, glyph), drive them
all from a single `updateTransition(targetState = state)` rather than several independent
`animate*AsState` calls — this guarantees they stay in lockstep and share one timeline.
Reference: `showcase/components/MorphingActionButton.kt`.

A useful cinematic technique: delay content (text/label) animation slightly (~120–160ms) so
structure (shape/color) visibly leads and content trails — see `idleTextAlpha` /
`resultTextAlpha` in that same file.

## 6. New to Compose? XML/View animation → Compose

Most `ObjectAnimator` / `ValueAnimator` / `MotionLayout` / custom-`View` patterns have a direct
Compose equivalent — see **[xml-to-compose-bridge.md](xml-to-compose-bridge.md)** for the full
mapping table with side-by-side code pairs. Quick orientation:

| XML / View world | Compose equivalent |
|---|---|
| `ObjectAnimator.ofFloat(view, "alpha", ...)` | `animateFloatAsState` / `Animatable` + `graphicsLayer` |
| `AnimatorSet` coordinating several `ObjectAnimator`s | `updateTransition` |
| `ViewPropertyAnimator` (`.animate().translationX(...)`) | `Modifier.offset { }` driven by `Animatable`/`animateDpAsState` |
| `MotionLayout` scene transitions | `updateTransition` for simple cases, `SharedTransitionLayout` for cross-destination continuity |
| Custom `View.onDraw()` + `invalidate()` loop | `Canvas`/`DrawScope` inside a composable + `withFrameNanos` |
| Custom `ViewGroup.onMeasure()`/`onLayout()` | Custom `Layout { measurables, constraints -> ... placeRelative(...) }` |
| `Interpolator` (`AccelerateDecelerateInterpolator`, etc.) | `Easing` (`FastOutSlowInEasing`, `LinearOutSlowInEasing`, custom `CubicBezierEasing`) |
| `TransitionManager.beginDelayedTransition` | `AnimatedContent` / `AnimatedVisibility` |

When writing motion code for a developer whose background is XML, prefer explaining the change
in these terms first ("this replaces the `ObjectAnimator` + listener you'd write for this") —
it's the fastest way to make the Compose code land.

## 7. Performance checklist

Apply before merging any custom motion:

1. Animate with `graphicsLayer` / `drawWithContent` / `Modifier.offset { }` lambda — avoid layout-phase reads every frame.
2. Cache `Path` / `Paint` objects when animating at 60fps; don't allocate per frame.
3. Use one shared progress clock (`Animatable(0f)`) for coordinated effects instead of juggling multiple timelines.
4. Profile recomposition — don't read animated state in a parent composable that scopes a large subtree.
5. Shared elements: use stable unique keys, keep shared regions small, test back-stack/config changes in real navigation.
6. Shaders/AGSL: always guard by API level with a fallback for older devices.

## 8. Reference implementation map (MotionOS repo)

| File | Demonstrates |
|---|---|
| `showcase/AnimatedVisibilityDemo.kt` | `AnimatedVisibility` — Enter/Exit combined with `+` |
| `showcase/components/MorphingActionButton.kt` | `updateTransition` — reusable, drop-in async button |
| `showcase/KeyframesThemeWipeDemo.kt` | `Animatable` + `keyframes` + `clipRect` wipe |
| `showcase/SharedElementGalleryDemo.kt` | `SharedTransitionLayout` list ↔ detail |
| `showcase/CanvasPulseChartDemo.kt` | `Canvas`/`DrawScope` chart, one `Animatable` driving many properties |
| `showcase/OrbitalMenuLayoutDemo.kt` | Custom `Layout` (fan deck) + `graphicsLayer` depth + `Canvas` ring |
| `showcase/AuroraUnlockScreen.kt` | Imperative `Animatable` + springs + shared clock orbits |
| `showcase/NeonRushGame.kt` | `withFrameNanos` real-time game loop over Canvas |

## Additional resources

- Official docs — always search here first for recommended Compose animation APIs: [developer.android.com/develop/ui/compose/animation](https://developer.android.com/develop/ui/compose/animation)
- **[xml-to-compose-bridge.md](xml-to-compose-bridge.md)** — full XML/View → Compose animation translation table with code pairs.
