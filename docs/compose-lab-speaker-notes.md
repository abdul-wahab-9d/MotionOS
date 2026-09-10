# Compose Motion & Pixels — Speaker Notes

**Title:** Compose Motion & Pixels: Choosing the Right API  
**Audience:** Android team  
**Length:** 45–60 min (trim marks for 30 min)  
**Lab:** Motion OS — run the app on a tablet (talk deck) or phone (demo list)

**Presenter setup**
- Tablet landscape (or Pixel Tablet AVD); Motion OS opens as the deck
- Clicker: volume / D-pad / space. Notes key: N
- Backup: 20–30s screen recordings of each demo
- Leave-behind PDF: `docs/talk/index.html` → Chrome Print → Save as PDF (Landscape, no margins, background graphics)

---

## Slide 1 — Title

**On screen:** Title + your name + “Live demos in Compose Lab”

**Say (~30s):**
> Today is not “every animation API.” It’s how we *choose* the right one — transitions, Canvas, and custom Layout — with demos you can open after this.

**Don’t:** Read a long bio.

---

## Slide 2 — Goal

**On screen:**
- Leave knowing *which* API for *which* problem
- Know when Canvas / custom Layout is worth it
- Shared performance rules for our apps

**Say (~45s):**
> Success looks like: next time design asks for a morphing pay button or a custom chart, you don’t guess — you pick `updateTransition`, shared elements, or Canvas on purpose.

---

## Slide 3 — Agenda *(30‑min: keep; cut “our app” if needed)*

**On screen:**
1. Animation decision tree + demos  
2. Graphics & Layout decision tree + demos  
3. Performance checklist  
4. Apply to our codebase  
5. Q&A  

**Say (~20s):** Walk the list. Promise demos are the point.

---

## Slide 4 — When *not* to animate

**On screen:** bullets
- Prefer system / Material defaults first
- Motion must serve hierarchy or feedback
- If it fights readability or a11y → cut it
- Games/Canvas are rare in product UI

**Say (~1 min):**
> Compose makes custom motion easy. That doesn’t mean every screen needs it. Most of our UI should stay on Material motion. Custom is for moments that sell the product.

---

## Slide 5 — Animation decision tree ★ keep visible

**On screen:** the tree

```
Need motion?
├── Appear/disappear?           → AnimatedVisibility
├── Swap content/screens?       → AnimatedContent
├── Many props, one state?      → updateTransition
├── Imperative / interruptible? → Animatable
├── Gesture / organic settle?   → spring()
├── Authored “story” timing?    → keyframes
├── List ↔ detail continuity?   → SharedTransition*
└── Per-frame draw / game?      → withFrameNanos + Canvas
```

**Say (~2 min):**
> This is the spine of the talk. We’ll demo the middle and bottom of this tree. Top two — Visibility and Content — you already know; we go beyond them.

**Ask room:** “Where would a checkout button sit?” → `updateTransition`.

---

## Slide 6 — Specs cheat sheet

**On screen:** table

| Spec | Feels like | Use when |
|------|------------|----------|
| `tween` | Directed, timed | Enter/exit, structured morph |
| `spring` | Physical | Drag release, playful UI |
| `keyframes` | Hand-authored | Overshoot, pauses, cinematic |
| `infiniteRepeatable` | Loop | Loaders, ambient (sparingly) |

**Say (~1.5 min):**
> Specs encode *intent*. Wrong spec = uncanny motion even if the values are fine.

---

## Slide 7 — Animation API catalog *(~45s, don’t read)*

**On screen:** table grouped Appear / Drive / Specs / Continuity / Frame

**Say:**
> The tree picks. This names the set. Appear and swap you know. Today we prove Drive, Specs, Continuity — and Frame only as intensity.

**30‑min:** Flash 20s; don’t walk rows.

---

## Slide 7.5 — DEMO: AnimatedVisibility *(~1.5–2 min)*

**Open:** Compose Lab → Animated Visibility Basics (or tap the "Appear / disappear?" branch on Slide 5)

**Say:**
> This one you already know — so fast. One boolean, `visible = true/false`. Enter runs in, Exit runs out. The trick is combining transitions with `+` — fade alone feels flat, fade + expand feels like the layout is breathing.

**Do:** Tap Fade → Expand → Slide + Scale, toggle the switch each time.

**Call out APIs:** `AnimatedVisibility`, `EnterTransition`, `ExitTransition`, the `+` combinator.

**Team hook:** “Error banners, optional form fields, tooltips — this is most of our motion. Everything after this slide is for the 20% that needs more.”

**30‑min:** Keep — it’s short and it’s the on-ramp before the “beyond the baseline” demos.

---

## Slide 8 — DEMO: Morphing Action Button *(~3–4 min)*

**Open:** Compose Lab → Morphing Action Button  

**Say while tapping Success / Error:**
> One state: Idle → Loading → Success/Error. Width, color, and glyph are coordinated with `updateTransition`. Label trails the structure by ~120ms — structure leads, content follows. PathMeasure draws the check.

**Call out APIs:** `updateTransition`, `animateDp` / `animateColor`, spring on glyph, Canvas orbit + PathMeasure.

**Team hook:** “Pay / save / sync actions in our apps.”

**Next slide (pseudocode):** Don’t read the file. Pattern only — then flip to Android Studio → `MorphingActionButton.kt`.

**30‑min:** Keep this demo.

---

## Slide 9 — DEMO: Keyframes Theme Wipe *(~3 min)*

**Open:** Keyframes Theme Wipe · tap to wipe  

**Say:**
> Springs are great until you need a *story*: race ahead, overshoot, settle. That’s `keyframes`. We clip with `clipRect` so dark theme reveals as a wipe — animation drives a drawing primitive.

**Call out:** `Animatable` + `keyframes { … at … using easing }`, `drawWithContent` / `clipRect`.

**Next slide (pseudocode):** Don’t read the file. Pattern only — then flip to Android Studio → `KeyframesThemeWipeDemo.kt`. Steal `clipToFraction`.

**30‑min:** Keep if time; else show 15s recording.

---

## Slide 10 — DEMO: Shared Element Gallery *(~4 min)*

**Open:** Shared Element Gallery · tap card · tap back  

**Say:**
> Continuity across destinations. `SharedTransitionLayout` provides scope; list and detail share keys (`swatch-id`, `title-id`). This is the modern answer to “hero transitions” in Compose.

**Call out:** `SharedTransitionLayout`, `sharedElement`, `rememberSharedContentState`, `AnimatedContent` as visibility scope.

**Caveat (~20s):** Stable unique keys; don’t share huge subtrees; test back stack / config changes in real nav.

**30‑min:** Keep this demo.

---

## Slide 11 — Graphics / Layout decision tree ★

**On screen:**

```
Custom drawing or layout?
├── Tint / shape / clip only?     → Modifier / Shape
├── Charts, gauges, game art?     → Canvas + DrawScope
├── Children in custom positions? → custom Layout
└── Deferred / dependent measure? → SubcomposeLayout (later)
```

**Say (~1.5 min):**
> Canvas is not a layout system. Layout is not a drawing API. Mixing them up is how we get unmaintainable UI.

---

## Slide 12 — DEMO: Canvas Pulse Chart *(~3–4 min)*

**Open:** Canvas Pulse Chart  

**Say:**
> Charts are a perfect Canvas use case. Grid, area path, stroke, bar rects — all `DrawScope`. Reveal is one `Animatable(0→1)`; bars use staggered local time. Same pattern as cinematic UI: one fraction, many properties.

**Call out:** `Canvas`, `Path`, `drawPath` / `drawRoundRect`, animation as progress.

**30‑min:** Keep this demo.

---

## Slide 13 — DEMO: Fan Deck Layout *(~3–4 min)*

**Open:** Fan Deck · swipe or tap a card  

**Say:**
> No Row/Column trigonometry hacks. We implement `Layout`: measure children, then `placeRelative` on an ellipse. Selected sits at 6 o’clock — `zIndex` is frontness. `graphicsLayer` sells the 3D; Canvas draws the ring. That split is the whole lesson.

**Call out:** `Layout { measurables, constraints -> … placeRelative(x, y, zIndex) }`, spring settle after swipe.

**Team hook:** wallet / stories / picker — not a circle of letter-dots.

**Next slide (pseudocode):** Don’t read the file. Pattern only — then flip to Android Studio → `OrbitalMenuLayoutDemo.kt`. Call out: `zIndex` is placement, `graphicsLayer` is depth.

**30‑min:** Keep this demo.

---

## Slide 14 — Optional wow *(60‑min only, pick ONE, ~3 min)*

**A — Aurora Unlock:** tap card → portrait activity. Rotate the emulator. Hold core.  
> Imperative `Animatable`, springs, shared clock orbits.

**B — Neon Rush:** tap card → portrait activity. Rotate the emulator. Tap to race.  
> `withFrameNanos` game loop — same Canvas primitives, real-time simulation. Cool, but not default product UI.

**Say:** “Same toolbox; different intensity. These two are phone UI — that’s why they leave the landscape deck.”

---

## Slide 15 — Performance checklist

**On screen:**
1. Prefer `graphicsLayer` / draw / `offset { }` over layout thrash every frame  
2. Cache `Path` / paints when animating at 60fps  
3. One shared clock for coordinated effects  
4. Profile recomposition; don’t animate state read in every parent  
5. Shared elements: stable keys, small shared regions  
6. Fancy shaders need API guards + fallbacks  

**Say (~3 min):** Walk bullets with “what breaks if we ignore this.”

---

## Slide 15.5 — DEMO: XML vs Compose — moving parts *(~1.5–2 min)*

**On screen:** table — Behavior · XML/View parts · Compose (tap to reveal)

**Say:**
> One more before we apply this. For each thing we just built, here's what you'd wire together in XML versus Compose. Tap a row.

**Do:** Tap 2–3 rows live (Morphing button is the strongest — 7 objects to coordinate vs. one `updateTransition`).

**Caveat (say this out loud, don't skip it):**
> These numbers are structural — the real shape of `AnimatorSet`, `MotionLayout`, custom `ViewGroup`. Not a specific implementation's line count. I'm not going to stand here and tell you Compose is magic — fewer moving parts is the honest claim.

**Team hook:** This is the "why bother learning `updateTransition`" answer for anyone still on the fence.

**30‑min:** Keep — short, and it's the strongest pitch to XML-only teammates in the whole deck.

---

## Slide 16 — Apply to *our* apps *(~5 min)*

**On screen:** empty table — fill live with the room

| Screen / moment | API |
|-----------------|-----|
| Primary CTA async | Morphing button / `updateTransition` |
| List → detail | Shared elements |
| Theme / onboarding flourish | keyframes (sparingly) |
| Analytics widget | Canvas chart |
| Radial / fan picker | custom Layout (if design requires) |
| Everything else | Material + Visibility/Content |

**Say:** Facilitate. Capture 2–3 real tickets. Agree: Canvas only when components can’t express the design.

---

## Slide 17 — Resources

**On screen:**
- Compose Lab in this repo (`showcase/`)
- Android docs: Shared elements, Animation
- Lab files map 1:1 to demos today

**Say (~30s):** Lab is the leave-behind.

---

## Slide 18 — Q&A

**On screen:** prompts + two take-home cards (Layout / Animation)

**Prompt questions if silent:**
- “Where have we fought motion jank?”
- “Any design asks that forced Canvas prematurely?”
- “Who wants a follow-up on Nav + shared elements?”

**Close on the cards (~20s):**
> Layout: measure children, then `placeRelative`. Canvas draws pixels; it does not place children.
> Animation: Visibility you know. Custom is `Animatable` + a spec. Story = `keyframes`. Physics = `spring`. Continuity = shared elements.

---

## Slide 19 — Thank You *(let it play)*

**On screen:** letter cascade · Team Lead Hammad Nawaz · signature flourish · API chips · “and the team”

**Say (~20s, after letters land):**
> Thank you. Team lead Hammad Nawaz — and the team. Tap the slide if you want to see the close again.

**Don’t:** Talk over the cascade. The animation *is* the closer.

---

## 30‑minute cut list

| Keep | Cut / recording-only |
|------|----------------------|
| Slides 1–7 (catalog = flash), 8, 10, 11–13, 15 | Slide 9 live, Slide 14, deep Slide 16 |
| Morphing, Shared, Chart, Fan Deck | Wipe live, Aurora/Rush |

---

## Timing cheat card (45–60)

| Block | Min |
|-------|-----|
| Open + when not to animate | 4 |
| Animation tree + specs + catalog | 5 |
| Morphing + Wipe + Shared | 12 |
| Graphics tree + Chart + Fan Deck | 12 |
| Wow (optional) | 3 |
| Performance + our apps | 8 |
| Q&A + thank you | 5–10 |
