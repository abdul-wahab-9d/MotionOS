# XML / View Animation → Compose Bridge

For developers whose animation experience is `ObjectAnimator`, `MotionLayout`, or custom `View`
drawing. Each row shows the XML/View pattern next to its Compose equivalent so the translation is
explicit, not assumed.

---

## 1. Simple property animation

**XML/View — `ObjectAnimator`:**
```kotlin
ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
    duration = 300
    start()
}
```

**Compose — `animateFloatAsState` (reactive) or `Animatable` (imperative):**
```kotlin
// Reactive: alpha follows a boolean/state automatically
val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(300))
Box(Modifier.graphicsLayer { this.alpha = alpha })

// Imperative: explicit control, cancellable, sequenceable
val alpha = remember { Animatable(0f) }
LaunchedEffect(Unit) { alpha.animateTo(1f, tween(300)) }
```

Use `graphicsLayer` (not `Modifier.alpha()`) so the animation doesn't trigger recomposition.

---

## 2. Coordinated multi-property animation

**XML/View — `AnimatorSet`:**
```kotlin
AnimatorSet().apply {
    playTogether(
        ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f),
        ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f),
        ObjectAnimator.ofArgb(view, "backgroundColor", from, to),
    )
    duration = 320
    start()
}
```

**Compose — `updateTransition`:** one state drives every property, guaranteed lockstep, no
manual `AnimatorSet` bookkeeping.
```kotlin
val transition = updateTransition(targetState = state, label = "card")
val scale by transition.animateFloat(label = "scale") { s -> if (s == Pressed) 0.9f else 1f }
val bg by transition.animateColor(label = "bg") { s -> if (s == Pressed) pressedColor else idleColor }
```
Reference in this repo: `showcase/components/MorphingActionButton.kt`.

---

## 3. `ViewPropertyAnimator` chains

**XML/View:**
```kotlin
view.animate()
    .translationX(120f)
    .alpha(0.5f)
    .setDuration(250)
    .start()
```

**Compose:**
```kotlin
val offsetX = remember { Animatable(0f) }
val alpha = remember { Animatable(1f) }
LaunchedEffect(Unit) {
    launch { offsetX.animateTo(120f, tween(250)) }
    launch { alpha.animateTo(0.5f, tween(250)) }
}
Modifier
    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
    .graphicsLayer { this.alpha = alpha.value }
```

---

## 4. `MotionLayout` scene transitions

**XML/View:** a `MotionScene` XML file with `ConstraintSet`s per state and a `Transition` block,
driven by `motionLayout.transitionToState(id)`.

**Compose:**
- For a single component morphing between 2–4 states with a handful of animated properties
  (width, color, position) → `updateTransition`. No separate scene file; the states and their
  target values live next to the composable.
- For full-screen "this card becomes that detail screen" continuity across a navigation
  destination change → `SharedTransitionLayout` + `sharedElement`/`sharedBounds` with a stable
  key (e.g. `"swatch-$id"`). Reference: `showcase/SharedElementGalleryDemo.kt`.

There is no 1:1 Compose replacement for arbitrary `ConstraintSet` interpolation — most
`MotionLayout` use cases decompose into one of the two options above once you identify whether
it's "one component, many properties" or "continuity across a destination change."

---

## 5. Custom `View.onDraw()` + `invalidate()` loop

**XML/View:**
```kotlin
class GaugeView(context: Context) : View(context) {
    private var progress = 0f
    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(rect, 0f, progress * 360f, false, paint)
    }
    fun animateProgress(target: Float) {
        ValueAnimator.ofFloat(progress, target).apply {
            addUpdateListener { progress = it.animatedValue as Float; invalidate() }
            start()
        }
    }
}
```

**Compose — `Canvas` + `Animatable`, no manual `invalidate()`:**
```kotlin
val progress = remember { Animatable(0f) }
LaunchedEffect(target) { progress.animateTo(target, tween(400)) }
Canvas(Modifier.size(120.dp)) {
    drawArc(color, startAngle = 0f, sweepAngle = progress.value * 360f, useCenter = false)
}
```
Reading `progress.value` inside `Canvas`'s draw scope re-draws only that Canvas, not the whole
tree. Reference: `showcase/CanvasPulseChartDemo.kt`.

For a manual per-frame loop (game/particle style) instead of a tween, use `withFrameNanos` inside
a `LaunchedEffect` — see the spinning orbit in `showcase/components/MorphingActionButton.kt`
(`LoadingOrbit`) or the full game loop in `showcase/NeonRushGame.kt`.

---

## 6. Custom `ViewGroup.onMeasure()` / `onLayout()`

**XML/View:**
```kotlin
class FanLayout(context: Context) : ViewGroup(context) {
    override fun onMeasure(widthSpec: Int, heightSpec: Int) { /* measure children */ }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // trigonometry to place each child on an arc
    }
}
```

**Compose — custom `Layout`:**
```kotlin
Layout(content = { /* children */ }) { measurables, constraints ->
    val placeables = measurables.map { it.measure(constraints) }
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeables.forEachIndexed { i, p ->
            val angle = PI / 2 + 2 * PI * (i - selected) / placeables.size
            p.placeRelative(x = /* ellipse x */ 0, y = /* ellipse y */ 0, zIndex = frontness(i))
        }
    }
}
```
Placement logic (measure → compute position → `placeRelative`) replaces `onMeasure`/`onLayout`
directly. Depth/rotation effects (`graphicsLayer { rotationY = ...; scale = ... }`) are a
*separate* concern from placement — don't fold 3D-looking depth into the `Layout` math itself.
Reference: `showcase/OrbitalMenuLayoutDemo.kt`.

---

## 7. Interpolators → Easing

| `Interpolator` | Compose `Easing` |
|---|---|
| `LinearInterpolator` | `LinearEasing` |
| `AccelerateDecelerateInterpolator` | `FastOutSlowInEasing` |
| `DecelerateInterpolator` | `LinearOutSlowInEasing` |
| `AccelerateInterpolator` | `FastOutLinearInEasing` |
| Custom `PathInterpolator(x1, y1, x2, y2)` | `CubicBezierEasing(x1, y1, x2, y2)` |

---

## 8. `TransitionManager.beginDelayedTransition`

**XML/View:**
```kotlin
TransitionManager.beginDelayedTransition(container)
childView.visibility = View.GONE
otherView.visibility = View.VISIBLE
```

**Compose:**
```kotlin
AnimatedVisibility(visible = showChild) { ChildContent() }
// or, for swapping one content block for another:
AnimatedContent(targetState = uiState) { state -> /* render per state */ }
```

---

## Escalation rule

If a request maps cleanly to one row above, use that Compose API directly. If it's a genuinely
custom motion that doesn't map to a familiar XML pattern (e.g. a bespoke chart, a physics-driven
gesture), go back to the main decision trees in `SKILL.md` rather than forcing an XML analogy.
