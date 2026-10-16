package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import kotlin.math.floor
import kotlin.math.roundToInt

private val StripInset = 4.dp
private val TabMinHeight = 44.dp
private val TabHorizontalPadding = 18.dp

@Composable
fun NavoTabStrip(
    titles: List<String>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    indicatorPosition: () -> Float = { selectedIndex.toFloat() },
) {
    val colors = NavoTheme.colors
    val scrollState = rememberScrollState()
    val tabBounds = remember(titles) { mutableStateMapOf<Int, TabSpan>() }
    val indicatorRect: () -> Rect? = {
        tabBounds.takeIf { it.size == titles.size }?.let { spans ->
            wormSpan(spans.toSortedMap().values.toList(), indicatorPosition())
        }?.let { span -> Rect(span.start, 0f, span.end, tabBounds.values.first().height) }
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val contentLeftPx = with(density) { contentPadding.calculateLeftPadding(layoutDirection).toPx() + StripInset.toPx() }
    var hasCentered by remember { mutableStateOf(false) }
    LaunchedEffect(selectedIndex, layoutDirection) {
        val (span, viewport) = snapshotFlow { tabBounds[selectedIndex] to scrollState.viewportSize }
            .first { (span, viewport) -> span != null && viewport > 0 }
        val target = centeredScroll(
            tabCenter = contentLeftPx + span!!.start + span.width / 2f,
            viewport = viewport,
            maxScroll = scrollState.maxValue,
            rtl = layoutDirection == LayoutDirection.Rtl,
        )
        if (hasCentered) scrollState.animateScrollTo(target) else scrollState.scrollTo(target)
        hasCentered = true
    }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(contentPadding),
    ) {
        Box(
            modifier = Modifier
                .background(colors.raised, NavoShapes.Pill)
                .padding(StripInset)
                .drawBehind {
                    indicatorRect()?.let { rect ->
                        drawRoundRect(
                            color = colors.accent,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            cornerRadius = CornerRadius(rect.height / 2f),
                        )
                    }
                },
        ) {
            TabLabels(
                titles = titles,
                color = colors.contentMuted,
                modifier = Modifier
                    .semantics { description?.let { contentDescription = it } }
                    .selectableGroup(),
                tabModifier = { index ->
                    Modifier
                        .onPlaced { coordinates ->
                            val x = coordinates.positionInParent().x
                            tabBounds[index] = TabSpan(
                                start = x,
                                end = x + coordinates.size.width,
                                height = coordinates.size.height.toFloat(),
                            )
                        }
                        .selectable(
                            selected = index == selectedIndex,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onTabClick(index) },
                        )
                },
            )
            TabLabels(
                titles = titles,
                color = colors.onAccent,
                modifier = Modifier
                    .clearAndSetSemantics { }
                    .drawWithContent {
                        val rect = indicatorRect() ?: return@drawWithContent
                        val path = Path().apply {
                            addRoundRect(RoundRect(rect, CornerRadius(rect.height / 2f)))
                        }
                        clipPath(path) { this@drawWithContent.drawContent() }
                    },
                tabModifier = { Modifier },
            )
        }
    }
}

@Composable
private fun TabLabels(
    titles: List<String>,
    color: Color,
    modifier: Modifier,
    tabModifier: (Int) -> Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        titles.forEachIndexed { index, title ->
            Box(
                modifier = tabModifier(index)
                    .heightIn(min = TabMinHeight)
                    .padding(horizontal = TabHorizontalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = NavoTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = color,
                    maxLines = 1,
                )
            }
        }
    }
}

internal data class TabSpan(val start: Float, val end: Float, val height: Float = 0f) {
    val width: Float
        get() = end - start
}

internal fun wormSpan(spans: List<TabSpan>, position: Float): TabSpan? {
    if (spans.isEmpty()) return null
    val clamped = position.coerceIn(0f, spans.lastIndex.toFloat())
    val index = floor(clamped).toInt().coerceAtMost(spans.lastIndex)
    val fraction = clamped - index
    val from = spans[index]
    val to = spans.getOrNull(index + 1) ?: return from
    val fast = 1f - (1f - fraction) * (1f - fraction)
    val slow = fraction * fraction
    val forward = to.start >= from.start
    return TabSpan(
        start = from.start + (to.start - from.start) * if (forward) slow else fast,
        end = from.end + (to.end - from.end) * if (forward) fast else slow,
        height = from.height,
    )
}

internal fun centeredScroll(tabCenter: Float, viewport: Int, maxScroll: Int, rtl: Boolean): Int {
    val fromLeft = tabCenter - viewport / 2f
    val target = if (rtl) maxScroll - fromLeft else fromLeft
    return target.roundToInt().coerceIn(0, maxScroll)
}

@Preview
@Composable
private fun NavoTabStripPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.background).padding(16.dp)) {
            NavoTabStrip(
                titles = listOf("Треки", "Плейлисты", "Альбомы", "Исполнители", "Папки"),
                selectedIndex = 0,
                onTabClick = {},
                indicatorPosition = { 0.4f },
            )
        }
    }
}

@Preview
@Composable
private fun NavoTabStripLightPreview() {
    NavoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(NavoTheme.colors.background).padding(16.dp)) {
            NavoTabStrip(
                titles = listOf("Треки", "Плейлисты", "Альбомы", "Исполнители", "Папки"),
                selectedIndex = 2,
                onTabClick = {},
            )
        }
    }
}
