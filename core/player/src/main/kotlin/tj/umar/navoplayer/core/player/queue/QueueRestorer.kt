package tj.umar.navoplayer.core.player.queue

import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.domain.model.QueueResumeResult
import tj.umar.navoplayer.core.domain.model.ResumableQueue
import tj.umar.navoplayer.core.domain.usecase.LoadResumableQueueUseCase
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.mapper.toPlayerRepeatMode

internal class QueueRestorer(
    private val loadResumableQueue: LoadResumableQueueUseCase,
    private val player: QueuePlayer,
    private val mappingDispatcher: CoroutineDispatcher,
) {
    private val settledState = MutableStateFlow(false)
    private var loaded: Deferred<RestoredMediaQueue?>? = null
    private var claimed = false

    val settled: StateFlow<Boolean> = settledState.asStateFlow()

    fun start(scope: CoroutineScope) {
        val pending = load(scope)
        scope.launch {
            try {
                val queue = runCatching { pending.await() }.getOrNull()
                if (queue != null && !claimed && !player.hasCurrentItem) player.apply(queue)
            } finally {
                settledState.value = true
            }
        }
    }

    fun resumption(scope: CoroutineScope, isForPlayback: Boolean): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val result = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
        scope.launch {
            val value = runCatching { resolveResumption(scope, isForPlayback) }.getOrNull()
            if (value != null) result.set(value) else result.setException(UnsupportedOperationException())
        }
        return result
    }

    private suspend fun resolveResumption(
        scope: CoroutineScope,
        isForPlayback: Boolean,
    ): MediaSession.MediaItemsWithStartPosition? {
        if (player.hasCurrentItem) return player.currentResumption()
        val queue = load(scope).await() ?: return null
        if (!isForPlayback) {
            return MediaSession.MediaItemsWithStartPosition(listOf(queue.items[queue.startIndex]), 0, queue.startPositionMs)
        }
        if (player.hasCurrentItem) return player.currentResumption()
        claimed = true
        player.prepareForResumption(queue)
        settledState.value = true
        return MediaSession.MediaItemsWithStartPosition(queue.items, queue.startIndex, queue.startPositionMs)
    }

    private fun load(scope: CoroutineScope): Deferred<RestoredMediaQueue?> = loaded ?: scope.async {
        val result = loadResumableQueue()
        if (result is QueueResumeResult.Resumable) {
            withContext(mappingDispatcher) { result.queue.toRestoredMediaQueue() }
        } else {
            null
        }
    }.also { loaded = it }
}

internal fun ResumableQueue.toRestoredMediaQueue(): RestoredMediaQueue = RestoredMediaQueue(
    items = items.map { it.track.toMediaItem(it.id.value) },
    startIndex = startIndex,
    startPositionMs = startPositionMs,
    shuffleOrder = shuffleOrder?.toIntArray(),
    shuffleEnabled = shuffleEnabled,
    repeatMode = repeatMode.toPlayerRepeatMode(),
    source = source,
)
