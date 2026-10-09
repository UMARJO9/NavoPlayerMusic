package tj.umar.navoplayer.feature.player.queue

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val AUTO_SCROLL_FRAME_MILLIS = 16L
private const val AUTO_SCROLL_EDGE_FRACTION = 0.1f
private const val AUTO_SCROLL_MAX_STEP = 24f

@Stable
internal class ReorderableListState<T>(
    val listState: LazyListState,
    initialItems: List<T>,
    private val keyOf: (T) -> Any,
    private val scope: CoroutineScope,
) {
    var items by mutableStateOf(initialItems)
        private set

    var draggingKey by mutableStateOf<Any?>(null)
        private set

    internal var onMove: (item: T, toIndex: Int) -> Unit = { _, _ -> }

    private var draggedDelta by mutableFloatStateOf(0f)
    private var initialOffset = 0
    private var startIndex = -1
    private var autoScrollJob: Job? = null

    fun sync(newItems: List<T>) {
        if (draggingKey == null) items = newItems
    }

    fun translationOf(key: Any): Float {
        if (key != draggingKey) return 0f
        val info = visible(key) ?: return 0f
        return initialOffset + draggedDelta - info.offset
    }

    fun onDragStart(key: Any) {
        val info = visible(key) ?: return
        draggingKey = key
        initialOffset = info.offset
        draggedDelta = 0f
        startIndex = info.index
        autoScrollJob = scope.launch {
            while (isActive) {
                val step = autoScrollStep()
                if (step != 0f) {
                    listState.scrollBy(step)
                    swapIfNeeded()
                }
                delay(AUTO_SCROLL_FRAME_MILLIS)
            }
        }
    }

    fun onDrag(delta: Float) {
        if (draggingKey == null) return
        draggedDelta += delta
        swapIfNeeded()
    }

    fun onDragEnd() {
        val key = draggingKey ?: return
        autoScrollJob?.cancel()
        autoScrollJob = null
        draggingKey = null
        draggedDelta = 0f
        val finalIndex = items.indexOfFirst { keyOf(it) == key }
        if (finalIndex >= 0 && finalIndex != startIndex) onMove(items[finalIndex], finalIndex)
        startIndex = -1
    }

    private fun swapIfNeeded() {
        val key = draggingKey ?: return
        val current = visible(key) ?: return
        val middle = (initialOffset + draggedDelta + current.size / 2f).toInt()
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key != key && middle in it.offset..(it.offset + it.size) && it.index in items.indices
        } ?: return
        val firstIndex = listState.firstVisibleItemIndex
        val firstOffset = listState.firstVisibleItemScrollOffset
        items = items.toMutableList().apply { add(target.index, removeAt(current.index)) }
        if (target.index == firstIndex || current.index == firstIndex) {
            scope.launch { listState.scrollToItem(firstIndex, firstOffset) }
        }
    }

    private fun autoScrollStep(): Float {
        val key = draggingKey ?: return 0f
        val current = visible(key) ?: return 0f
        val layout = listState.layoutInfo
        val viewport = (layout.viewportEndOffset - layout.viewportStartOffset).toFloat()
        val edge = viewport * AUTO_SCROLL_EDGE_FRACTION
        val start = initialOffset + draggedDelta
        val end = start + current.size
        return when {
            start < layout.viewportStartOffset + edge ->
                -((layout.viewportStartOffset + edge - start) / edge * AUTO_SCROLL_MAX_STEP).coerceAtMost(AUTO_SCROLL_MAX_STEP)
            end > layout.viewportEndOffset - edge ->
                ((end - (layout.viewportEndOffset - edge)) / edge * AUTO_SCROLL_MAX_STEP).coerceAtMost(AUTO_SCROLL_MAX_STEP)
            else -> 0f
        }
    }

    private fun visible(key: Any): LazyListItemInfo? =
        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }
}

@Composable
internal fun <T> rememberReorderableListState(
    listState: LazyListState,
    items: List<T>,
    keyOf: (T) -> Any,
    onMove: (item: T, toIndex: Int) -> Unit,
): ReorderableListState<T> {
    val scope = rememberCoroutineScope()
    val state = remember(listState) { ReorderableListState(listState, items, keyOf, scope) }
    val currentOnMove by rememberUpdatedState(onMove)
    SideEffect {
        state.onMove = { item, toIndex -> currentOnMove(item, toIndex) }
        state.sync(items)
    }
    return state
}

internal fun Modifier.reorderHandle(state: ReorderableListState<*>, key: Any): Modifier =
    pointerInput(state, key) {
        detectDragGestures(
            onDragStart = { state.onDragStart(key) },
            onDragEnd = state::onDragEnd,
            onDragCancel = state::onDragEnd,
            onDrag = { change, amount ->
                change.consume()
                state.onDrag(amount.y)
            },
        )
    }

@Composable
internal fun LazyItemScope.ReorderableItem(
    state: ReorderableListState<*>,
    key: Any,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    val isDragging = key == state.draggingKey
    val modifier = if (isDragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer { translationY = state.translationOf(key) }
    } else {
        Modifier.animateItem()
    }
    Box(modifier = modifier) { content(isDragging) }
}
