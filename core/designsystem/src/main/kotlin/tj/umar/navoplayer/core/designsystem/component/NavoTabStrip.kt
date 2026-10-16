package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

import tj.umar.navoplayer.core.designsystem.glass.NavoGlassDefaults
import tj.umar.navoplayer.core.designsystem.glass.navoGlass
import tj.umar.navoplayer.core.designsystem.glass.navoGlassSource
import tj.umar.navoplayer.core.designsystem.glass.rememberNavoGlassBackdrop
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import kotlin.math.floor
import kotlin.math.roundToInt

private val StripInset = 4.dp
private val IndicatorCorner = 16.dp
private val IndicatorShape = RoundedCornerShape(IndicatorCorner)
private val TabMinHeight = NavoSpacing.MinTouchTarget
private val TabMinHorizontalPadding = 8.dp
private const val MIN_TEXT_SCALE = 0.78f

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
    val density = LocalDensity.current
    val baseStyle = NavoTheme.typography.label.copy(fontWeight = FontWeight.SemiBold)
    val textMeasurer = rememberTextMeasurer()
    val indicatorCornerPx = with(density) { IndicatorCorner.toPx() }
    val glassBackdrop = rememberNavoGlassBackdrop()
    val lensStyle = NavoGlassDefaults.lens()
    val tabBounds = remember(titles) { mutableStateMapOf<Int, TabSpan>() }
    val indicatorRect: () -> Rect? = {
        tabBounds.takeIf { it.size == titles.size }?.let { spans ->
            wormSpan(List(titles.size) { spans.getValue(it) }, indicatorPosition())
        }?.let { span -> Rect(span.start, 0f, span.end, span.height) }
    }

    BoxWithConstraints(modifier = modifier.padding(contentPadding)) {
        val availablePx = constraints.maxWidth - with(density) { StripInset.toPx() * 2 }
        val fit = remember(titles, availablePx, baseStyle, density) {
            val textWidths = titles.map { title ->
                textMeasurer.measure(title, baseStyle, maxLines = 1, softWrap = false).size.width.toFloat()
            }
            fitTabs(textWidths, availablePx, with(density) { TabMinHorizontalPadding.toPx() }, MIN_TEXT_SCALE)
        }
        val style = baseStyle.copy(fontSize = baseStyle.fontSize * fit.textScale)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(StripInset),
        ) {
            TabLabels(
                titles = titles,
                weights = fit.weights,
                style = style,
                color = colors.contentMuted,
                modifier = Modifier
                    .semantics { description?.let { contentDescription = it } }
                    .selectableGroup()
                    .drawWithContent {
                        val rect = indicatorRect()
                        if (rect == null) {
                            drawContent()
                        } else {
                            clipPath(rect.toRoundedPath(indicatorCornerPx), ClipOp.Difference) { this@drawWithContent.drawContent() }
                        }
                    }
                    .navoGlassSource(glassBackdrop)
                    .drawBehind { drawStripGlow(colors.raised, colors.accent, colors.turquoise, StripInset.toPx(), indicatorCornerPx + StripInset.toPx()) },
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
            Box(
                modifier = Modifier
                    .offset { indicatorRect()?.let { IntOffset(it.left.roundToInt(), it.top.roundToInt()) } ?: IntOffset.Zero }
                    .layout { measurable, _ ->
                        val rect = indicatorRect()
                        val width = rect?.width?.roundToInt() ?: 0
                        val height = rect?.height?.roundToInt() ?: 0
                        val placeable = measurable.measure(Constraints.fixed(width, height))
                        layout(width, height) { placeable.place(0, 0) }
                    }
                    .navoGlass(glassBackdrop, IndicatorShape, lensStyle),
            )
            TabLabels(
                titles = titles,
                weights = fit.weights,
                style = style,
                color = colors.content,
                modifier = Modifier
                    .clearAndSetSemantics { }
                    .drawWithContent {
                        val rect = indicatorRect() ?: return@drawWithContent
                        clipPath(rect.toRoundedPath(indicatorCornerPx)) { this@drawWithContent.drawContent() }
                    },
                tabModifier = { Modifier },
            )
        }
    }
}

@Composable
private fun TabLabels(
    titles: List<String>,
    weights: List<Float>,
    style: TextStyle,
    color: Color,
    modifier: Modifier,
    tabModifier: (Int) -> Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        titles.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(weights[index])
                    .then(tabModifier(index))
                    .heightIn(min = TabMinHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = style,
                    color = color,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Immutable
internal data class TabFit(val textScale: Float, val weights: List<Float>)

internal fun fitTabs(textWidths: List<Float>, available: Float, minPadding: Float, minScale: Float): TabFit {
    val paddings = textWidths.size * minPadding * 2
    val textTotal = textWidths.sum()
    val scale = if (textTotal <= 0f) 1f else ((available - paddings) / textTotal).coerceIn(minScale, 1f)
    return TabFit(textScale = scale, weights = textWidths.map { it * scale + minPadding * 2 })
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

private fun Rect.toRoundedPath(corner: Float): Path = Path().apply { addRoundRect(RoundRect(this@toRoundedPath, CornerRadius(corner))) }

@Preview(widthDp = 384)
@Composable
private fun NavoTabStripPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.background).padding(vertical = 16.dp)) {
            NavoTabStrip(
                titles = listOf("Треки", "Плейлисты", "Альбомы", "Исполнители", "Папки"),
                selectedIndex = 0,
                onTabClick = {},
                contentPadding = PaddingValues(horizontal = 20.dp),
                indicatorPosition = { 0.4f },
            )
        }
    }
}

@Preview(widthDp = 384)
@Composable
private fun NavoTabStripLightPreview() {
    NavoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(NavoTheme.colors.background).padding(vertical = 16.dp)) {
            NavoTabStrip(
                titles = listOf("Треки", "Плейлисты", "Альбомы", "Исполнители", "Папки"),
                selectedIndex = 3,
                onTabClick = {},
                contentPadding = PaddingValues(horizontal = 20.dp),
            )
        }
    }
}

private const val STRIP_GLOW_ALPHA = 0.18f

private fun DrawScope.drawStripGlow(base: Color, warm: Color, cool: Color, bleed: Float, corner: Float) {
    val topLeft = Offset(-bleed, -bleed)
    val area = Size(size.width + bleed * 2, size.height + bleed * 2)
    drawRoundRect(base, topLeft = topLeft, size = area, cornerRadius = CornerRadius(corner))
    drawRoundRect(
        Brush.horizontalGradient(
            0f to Color.Transparent,
            0.2f to warm.copy(alpha = STRIP_GLOW_ALPHA),
            0.45f to Color.Transparent,
            0.7f to cool.copy(alpha = STRIP_GLOW_ALPHA),
            0.9f to warm.copy(alpha = STRIP_GLOW_ALPHA / 2f),
            1f to Color.Transparent,
        ),
        topLeft = topLeft,
        size = area,
        cornerRadius = CornerRadius(corner),
    )
}
