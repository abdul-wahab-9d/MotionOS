package com.sample.texteditor.showcase

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared element transitions — list → detail.
 *
 * Talk beat: Beyond AnimatedVisibility → SharedTransitionLayout + sharedElement.
 * Tap a card; the color block + title morph into the detail header.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedElementGalleryDemo(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<GalleryItem?>(null) }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "gallery",
        ) { item ->
            if (item == null) {
                GalleryList(
                    onSelect = { selected = it },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                )
            } else {
                GalleryDetail(
                    item = item,
                    onBack = { selected = null },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                )
            }
        }
    }
}

@Immutable
private data class GalleryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val color: Color,
)

private val GalleryItems = listOf(
    GalleryItem("1", "Aurora Ring", "PathMeasure + glow", Color(0xFF22D3EE)),
    GalleryItem("2", "Spring Steer", "Animatable physics", Color(0xFFF472B6)),
    GalleryItem("3", "Morph Button", "updateTransition", Color(0xFFA3E635)),
    GalleryItem("4", "Neon Trail", "withFrameNanos", Color(0xFFA78BFA)),
    GalleryItem("5", "Keyframes Wipe", "AnimationSpec craft", Color(0xFFFBBF24)),
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GalleryList(
    onSelect: (GalleryItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "SHARED ELEMENTS",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )
        Text(
            text = "Tap a card — watch it morph",
            color = Color(0xFFF1F5F9),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(GalleryItems, key = { it.id }) { item ->
                with(sharedTransitionScope) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF111827))
                            .clickable { onSelect(item) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .sharedElement(
                                    state = rememberSharedContentState(key = "swatch-${item.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(item.color, item.color.copy(alpha = 0.55f)),
                                    ),
                                ),
                        )
                        Column {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState(key = "title-${item.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                            )
                            Text(
                                text = item.subtitle,
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GalleryDetail(
    item: GalleryItem,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B14))
                .verticalScroll(rememberScrollState())
                .clickable(onClick = onBack),
        ) {
            Box(
                modifier = Modifier
                    .sharedElement(
                        state = rememberSharedContentState(key = "swatch-${item.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(item.color, item.color.copy(alpha = 0.35f), Color(0xFF070B14)),
                        ),
                    ),
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(key = "title-${item.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "SharedTransitionLayout links matching keys across list and detail. " +
                        "Tap anywhere to go back.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SharedElementPreview() {
    SharedElementGalleryDemo()
}
