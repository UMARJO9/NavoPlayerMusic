package tj.umar.navoplayer.core.player.queue

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.domain.usecase.ClearSavedPlaybackQueueUseCase
import tj.umar.navoplayer.core.domain.usecase.SavePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.SavePlaybackQueueUseCase
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "QueueStateWriter"

@Singleton
internal class QueueStateWriter @Inject constructor(
    private val saveQueue: SavePlaybackQueueUseCase,
    private val saveProgress: SavePlaybackProgressUseCase,
    private val clearQueue: ClearSavedPlaybackQueueUseCase,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private val lock = Any()
    private var pending: QueueWrite? = null
    private val signals = Channel<Unit>(Channel.CONFLATED)
    private var consumer: Job? = null

    fun submit(write: QueueWrite) {
        synchronized(lock) {
            pending = pending.mergedWith(write)
            if (consumer == null) consumer = scope.launch { drain() }
        }
        signals.trySend(Unit)
    }

    private suspend fun drain() {
        for (signal in signals) {
            while (true) {
                val write = synchronized(lock) { pending.also { pending = null } } ?: break
                persist(write)
            }
        }
    }

    private suspend fun persist(write: QueueWrite) {
        try {
            when (write) {
                is QueueWrite.Full -> saveQueue(write.queue)
                is QueueWrite.Progress -> saveProgress(write.progress)
                QueueWrite.Clear -> clearQueue()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            Log.w(TAG, "Queue write failed", failure)
        }
    }
}
