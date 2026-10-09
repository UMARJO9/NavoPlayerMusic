package tj.umar.navoplayer.feature.player.queue

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.usecase.MoveQueueItemUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackQueueUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveQueueItemUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToQueueItemUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class QueueViewModel @Inject constructor(
    private val observePlaybackQueue: ObservePlaybackQueueUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val skipToQueueItem: SkipToQueueItemUseCase,
    private val removeQueueItem: RemoveQueueItemUseCase,
    private val moveQueueItem: MoveQueueItemUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
) : MviViewModel<QueueState, QueueIntent, QueueEffect>(QueueState()) {

    private var queueJob: Job? = null
    private var stateJob: Job? = null
    private var closeRequested = false

    override fun onIntent(intent: QueueIntent) {
        when (intent) {
            QueueIntent.ScreenStarted -> startObserving()
            QueueIntent.ScreenStopped -> stopObserving()
            is QueueIntent.ItemClicked -> onItemClicked(intent.id)
            is QueueIntent.RemoveClicked -> onRemoveClicked(intent.id)
            is QueueIntent.MoveItem -> onMoveItem(intent.id, intent.toIndex)
            QueueIntent.CloseClicked -> requestClose()
        }
    }

    private fun startObserving() {
        if (queueJob?.isActive != true) {
            queueJob = observePlaybackQueue().onEach(::onQueue).catch { }.launchIn(viewModelScope)
        }
        if (stateJob?.isActive != true) {
            stateJob = observePlaybackState()
                .onEach { playback ->
                    setState {
                        copy(
                            isPlaying = playback.isPlaying,
                            shuffleEnabled = playback.shuffleEnabled,
                            source = playback.source,
                        )
                    }
                }
                .catch { }
                .launchIn(viewModelScope)
        }
    }

    private fun stopObserving() {
        queueJob?.cancel()
        stateJob?.cancel()
        queueJob = null
        stateJob = null
    }

    private fun onQueue(queue: PlaybackQueue) {
        if (queue.items.isEmpty()) {
            requestClose()
            return
        }
        setState { copy(isLoading = false, items = queue.items, currentItemId = queue.current?.id) }
    }

    private fun onItemClicked(id: QueueItemId) {
        val state = currentState
        if (id == state.currentItemId) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        viewModelScope.launch { skipToQueueItem(id) }
    }

    private fun onRemoveClicked(id: QueueItemId) {
        val state = currentState
        if (id == state.currentItemId || state.items.none { it.id == id }) return
        setState { copy(items = items.filterNot { it.id == id }) }
        viewModelScope.launch { removeQueueItem(id) }
    }

    private fun onMoveItem(id: QueueItemId, toIndex: Int) {
        val items = currentState.items
        val from = items.indexOfFirst { it.id == id }
        if (from < 0) return
        val target = toIndex.coerceIn(0, items.lastIndex)
        if (target == from) return
        val reordered = items.toMutableList().apply { add(target, removeAt(from)) }
        setState { copy(items = reordered) }
        viewModelScope.launch { moveQueueItem(id, target) }
    }

    private fun requestClose() {
        if (closeRequested) return
        closeRequested = true
        sendEffect(QueueEffect.Close)
    }
}
