package tj.umar.navoplayer.feature.player.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.core.ui.R as CoreUiR

@Composable
internal fun QueueScreen(
    state: QueueState,
    onIntent: (QueueIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        QueueTopBar(
            trackCount = state.items.size,
            isShuffled = state.shuffleEnabled,
            onClose = { onIntent(QueueIntent.CloseClicked) },
        )
        if (!state.isLoading) {
            QueueList(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun QueueTopBar(trackCount: Int, isShuffled: Boolean, onClose: () -> Unit) {
    val colors = NavoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSpacing.ScreenHorizontal, top = 12.dp, end = NavoSpacing.ScreenHorizontal, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavoIconButton(
            icon = NavoIcons.ChevronDown,
            contentDescription = stringResource(R.string.player_queue_close),
            onClick = onClose,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.player_queue_title),
                style = NavoTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = colors.content,
                modifier = Modifier.semantics { heading() },
            )
            val count = pluralStringResource(R.plurals.player_queue_track_count, trackCount, trackCount)
            val shuffled = stringResource(R.string.player_queue_shuffled)
            Text(
                text = if (isShuffled) stringResource(CoreUiR.string.core_ui_group_subtitle, count, shuffled) else count,
                style = NavoTheme.typography.caption,
                color = colors.contentSecondary,
            )
        }
        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun QueueList(state: QueueState, onIntent: (QueueIntent) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (state.currentIndex - 1).coerceAtLeast(0))
    val reorder = rememberReorderableListState(
        listState = listState,
        items = state.items,
        keyOf = { it.id.value },
        onMove = { item, toIndex -> onIntent(QueueIntent.MoveItem(item.id, toIndex)) },
    )
    var previousCurrent by remember { mutableStateOf(state.currentItemId) }
    LaunchedEffect(state.currentItemId) {
        val previous = previousCurrent
        previousCurrent = state.currentItemId
        if (previous == null || previous == state.currentItemId || reorder.draggingKey != null) return@LaunchedEffect
        val wasVisible = listState.layoutInfo.visibleItemsInfo.any { it.key == previous.value }
        val index = state.currentIndex
        if (wasVisible && index >= 0) listState.animateScrollToItem((index - 1).coerceAtLeast(0))
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        itemsIndexed(reorder.items, key = { _, item -> item.id.value }) { index, item ->
            ReorderableItem(state = reorder, key = item.id.value) { isDragging ->
                QueueRow(
                    track = item.track,
                    isCurrent = item.id == state.currentItemId,
                    isPlaying = state.isPlaying,
                    isDragging = isDragging,
                    canMoveUp = index > 0,
                    canMoveDown = index < reorder.items.lastIndex,
                    onClick = { onIntent(QueueIntent.ItemClicked(item.id)) },
                    onRemove = { onIntent(QueueIntent.RemoveClicked(item.id)) },
                    onMoveUp = { onIntent(QueueIntent.MoveItem(item.id, index - 1)) },
                    onMoveDown = { onIntent(QueueIntent.MoveItem(item.id, index + 1)) },
                    handleModifier = Modifier.reorderHandle(reorder, item.id.value),
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun QueueScreenPreview() {
    NavoTheme {
        QueueScreen(state = previewQueueState, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun QueueScreenLongShuffledPreview() {
    NavoTheme {
        QueueScreen(state = previewLongQueueState, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun QueueScreenPausedPreview() {
    NavoTheme {
        QueueScreen(state = previewQueueState.copy(isPlaying = false, currentItemId = QueueItemId("q0")), onIntent = {})
    }
}
