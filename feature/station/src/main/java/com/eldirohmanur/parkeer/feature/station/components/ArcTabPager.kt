package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.telkomsel.dexterity.theme.DX
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ArcTab(val icon: ImageVector, val title: String)

@Composable
internal fun ArcTabPager(tabs: List<ArcTab>, modifier: Modifier = Modifier, content: @Composable (page: Int) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .then(Modifier.padding(top = DX.Spacing.L))
            .fillMaxSize(),
    ) {
        // Arc tabs row - sits on the arc line
        // The arc peaks at 30% of the full screen. Since this composable starts after
        // the top bar (innerPadding consumed), we need to offset by:
        // (0.30 * fullScreenHeight - topBarHeight) which roughly equals
        // 0.15 * available height (since top bar is ~15% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.15f),
        ) {
            val currentOffset = pagerState.currentPage + pagerState.currentPageOffsetFraction

            Layout(
                content = {
                    tabs.forEachIndexed { index, tab ->
                        val relativePos = index - currentOffset
                        val distance = kotlin.math.abs(relativePos)
                        val alpha = (1f - distance * 0.4f).coerceIn(0.4f, 1f)
                        val iconSize = if (distance < 0.5f) 28.dp else 22.dp

                        Column(
                            modifier = Modifier
                                .alpha(alpha)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                                .padding(DX.Spacing.M),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DX.Spacing.XS),
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(iconSize),
                                tint = if (distance < 0.5f) DX.Color.text.primary else DX.Color.text.secondary,
                            )
                            Text(
                                tab.title,
                                style = DX.Font.caption,
                                color = if (distance < 0.5f) DX.Color.text.primary else DX.Color.text.secondary,
                            )
                        }
                    }
                },
                measurePolicy = { measurables, constraints ->
                    val placeables = measurables.map { it.measure(constraints) }
                    val containerWidth = constraints.maxWidth.toFloat()
                    val containerHeight = constraints.maxHeight
                    // Gentle arc: selected tab at top-center, unselected drops down
                    val arcDrop = containerWidth * 0.08f

                    layout(constraints.maxWidth, containerHeight) {
                        placeables.forEachIndexed { index, placeable ->
                            val relativePos = index - currentOffset
                            val x =
                                (containerWidth / 2f + relativePos * containerWidth * 0.45f).roundToInt() - placeable.width / 2
                            val y =
                                containerHeight - placeable.height + (relativePos * relativePos * arcDrop).roundToInt()

                            placeable.place(x, y)
                        }
                    }
                },
            )
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(0.85f)
                .fillMaxWidth(),
        ) { page ->
            content(page)
        }
    }
}
