package tj.umar.navoplayer.core.player.queue

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.domain.model.SavedQueue

internal const val QUEUE_SAVE_DEBOUNCE_MILLIS = 500L
internal const val QUEUE_POSITION_TICK_MILLIS = 15_000L

internal class QueuePersister(
    private val player: QueuePlayer,
    private val writer: QueueStateWriter,
    private val mappingDispatcher: CoroutineDispatcher,
    private val debounceMs: Long = QUEUE_SAVE_DEBOUNCE_MILLIS,
    private val tickMs: Long = QUEUE_POSITION_TICK_MILLIS,
) {
    private var job: Job? = null
    private var flushJob: Job? = null
    private var scope: CoroutineScope? = null
    private var structureDirty = false
    private val playing = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(scope: CoroutineScope, saveNow: Boolean) {
        if (job != null) return
        this.scope = scope
        playing.value = player.isPlaying
        job = scope.launch {
            launch { player.events.collect(::onEvent) }
            launch {
                playing.collectLatest { isPlaying ->
                    while (isPlaying) {
                        delay(tickMs)
                        flushNow()
                    }
                }
            }
            if (saveNow) {
                structureDirty = true
                scheduleFlush(0)
            }
        }
    }

    fun flush() {
        if (job == null) return
        flushJob?.cancel()
        val captured = player.capture() ?: return
        submit(captured, captured.takeIf { structureDirty }?.toSavedQueue())
    }

    fun release() {
        flushJob?.cancel()
        job?.cancel()
        flushJob = null
        job = null
        scope = null
    }

    private fun onEvent(event: QueuePlayerEvent) {
        when (event) {
            QueuePlayerEvent.StructureChanged -> {
                structureDirty = true
                scheduleFlush(debounceMs)
            }
            QueuePlayerEvent.ProgressChanged -> scheduleFlush(debounceMs)
            QueuePlayerEvent.Emptied -> {
                flushJob?.cancel()
                structureDirty = false
                writer.submit(QueueWrite.Clear)
            }
            is QueuePlayerEvent.PlayingChanged -> {
                playing.value = event.isPlaying
                if (!event.isPlaying) scheduleFlush(0)
            }
        }
    }

    private fun scheduleFlush(delayMs: Long) {
        val target = scope ?: return
        flushJob?.cancel()
        flushJob = target.launch {
            delay(delayMs)
            flushNow()
        }
    }

    private suspend fun flushNow() {
        val captured = player.capture() ?: return
        val full = if (structureDirty) withContext(mappingDispatcher) { captured.toSavedQueue() } else null
        submit(captured, full)
    }

    private fun submit(captured: CapturedQueue, full: SavedQueue?) {
        if (structureDirty) {
            structureDirty = false
            if (full != null) writer.submit(QueueWrite.Full(full))
        } else {
            writer.submit(QueueWrite.Progress(captured.toProgress()))
        }
    }
}
