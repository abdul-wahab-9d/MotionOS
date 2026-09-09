# Compose Motion & Pixels — Team Handout (1 page)

**Talk:** Choosing the right Compose animation & graphics API · **Lab:** run the app — tablet = Motion OS deck, phone = Compose Lab

---

## Animation — pick an API

| Problem | API | Lab demo |
|---------|-----|----------|
| Show / hide | `AnimatedVisibility` | (baseline — not in lab) |
| Swap screens / content | `AnimatedContent` | Shared Element (host) |
| Many properties, one state | `updateTransition` | **Morphing Action Button** |
| Imperative / cancel / sequence | `Animatable` | **Aurora Unlock**, **Theme Wipe** |
| Gesture settle / organic | `spring()` | **Neon Rush** lanes, Fan Deck |
| Authored timing / overshoot | `keyframes` | **Keyframes Theme Wipe** |
| List ↔ detail continuity | `SharedTransitionLayout` + `sharedElement` | **Shared Element Gallery** |
| Per-frame sim / particles | `withFrameNanos` + Canvas | **Neon Rush**, Aurora orbits |

```
Appear? → Visibility │ Swap? → Content │ Multi-prop state? → updateTransition
Interruptible? → Animatable │ Physics? → spring │ Story curve? → keyframes
Cross-screen continuity? → Shared elements │ Game/draw loop? → Frame clock + Canvas
```

**Spec intent:** `tween` = directed · `spring` = physical · `keyframes` = hand-authored · `infinite` = ambient (use sparingly)

---

## Graphics & Layout — pick an API

| Problem | API | Lab demo |
|---------|-----|----------|
| Tint / clip / shape | `Modifier` / `Shape` | — |
| Charts, gauges, custom art | `Canvas` + `DrawScope` | **Canvas Pulse Chart**, Neon Rush |
| Children in custom positions | `Layout` / MeasurePolicy | **Fan Deck Layout** |
| Deferred dependent measure | `SubcomposeLayout` | (follow-up) |

```
Just styling? → Modifier  │  Draw pixels/paths? → Canvas
Place children yourself? → custom Layout  │  Measure depends on content? → SubcomposeLayout
```

**Rule of thumb:** Canvas draws. Layout places. Don’t use Canvas to fake layout, or Layout to fake drawing.

---

## Performance (ship checklist)

1. Animate with `graphicsLayer` / draw / `offset { }` — avoid layout every frame  
2. Cache `Path` / paints in hot draw loops  
3. Prefer **one** progress clock for coordinated effects  
4. Profile recomposition; keep animated reads local  
5. Shared elements: **stable unique keys**, small shared regions  
6. Shaders / AGSL: API guards + fallback  

---

## Suggested live order (demo day)

1. Morphing Button → 2. Theme Wipe → 3. Shared Elements → 4. Canvas Chart → 5. Fan Deck Layout → (optional) Aurora / Neon Rush

---

## Use in our apps (fill in)

| Moment in product | API we chose |
|-------------------|--------------|
| Async primary CTA | |
| List → detail | |
| Onboarding / flourish | |
| Analytics widget | |
| Everything else | Material defaults |

**Team agreement:** Custom Canvas / Layout only when Material + standard animation can’t express the design.

---

## Lab map (`app/.../showcase/`)

| File | Topic |
|------|--------|
| `components/MorphingActionButton.kt` | `updateTransition` (reusable) |
| `KeyframesThemeWipeDemo.kt` | `keyframes` + clip wipe |
| `SharedElementGalleryDemo.kt` | Shared elements |
| `CanvasPulseChartDemo.kt` | DrawScope charts |
| `OrbitalMenuLayoutDemo.kt` | Custom `Layout` fan deck |
| `AuroraUnlockScreen.kt` | `Animatable` + shared clock |
| `NeonRushGame.kt` | Canvas game loop |

Code slides in the deck are **pseudocode**. Open the files above in Android Studio for the real source.
