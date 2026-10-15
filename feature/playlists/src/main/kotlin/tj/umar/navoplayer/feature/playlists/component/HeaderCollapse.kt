package tj.umar.navoplayer.feature.playlists.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

internal const val HEADER_ITEM_KEY = "header"

private const val COLLAPSED_ALPHA_DROP = 0.7f
private const val COLLAPSED_SCALE_DROP = 0.15f
private const val PARALLAX_FACTOR = 0.5f
private val BottomCenter = TransformOrigin(0.5f, 1f)

internal fun collapseFraction(firstVisibleIndex: Int, firstVisibleOffsetPx: Int, headerHeightPx: Int): Float = when {
    firstVisibleIndex > 0 -> 1f
    headerHeightPx <= 0 -> 0f
    else -> (firstVisibleOffsetPx.toFloat() / headerHeightPx).coerceIn(0f, 1f)
}

internal fun LazyListState.headerCollapseFraction(): Float {
    val headerHeight = layoutInfo.visibleItemsInfo.firstOrNull { it.key == HEADER_ITEM_KEY }?.size ?: 0
    return collapseFraction(firstVisibleItemIndex, firstVisibleItemScrollOffset, headerHeight)
}

internal fun Modifier.collapsingHeader(fraction: () -> Float, motion: Boolean): Modifier = graphicsLayer {
    val collapse = fraction()
    alpha = 1f - COLLAPSED_ALPHA_DROP * collapse
    if (motion) {
        val scale = 1f - COLLAPSED_SCALE_DROP * collapse
        scaleX = scale
        scaleY = scale
        translationY = collapse * size.height * PARALLAX_FACTOR
        transformOrigin = BottomCenter
    }
}
