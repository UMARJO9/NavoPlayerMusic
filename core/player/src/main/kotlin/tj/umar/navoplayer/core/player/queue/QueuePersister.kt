package tj.umar.navoplayer.core.player.queue

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var structureGeneration = 0L
    private var savedGeneration = 0L
    private val playing = MutableStateFlow(false)

    private val structureDirty: Boolean
        get() = structureGeneration != savedGeneration

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(scope: CoroutineScope, saveNow: Boolean) {
        if (job != null) return
        this.scope = scope
        playing.value = player.isPlaying
        if (saveNow) structureGeneration++
        job = scope.launch {
            launch(start = CoroutineStart.UNDISPATCHED) { player.events.collect(::onEvent) }
            launch {
                playing.collectLatest { isPlaying ->
                    while (isPlaying) {
                        delay(tickMs)
                        flushNow()
                    }
                }
            }
            if (saveNow) scheduleFlush(0)
        }
    }

    fun flush() {
        if (job == null) return
        flushJob?.cancel()
        val captured = player.capture() ?: return
        val generation = structureGeneration
        if (generation != savedGeneration) {
            captured.toSavedQueue()?.let { writer.submit(QueueWrite.Full(it)) }
            savedGeneration = generation
        } else {
            writer.submit(QueueWrite.Progress(captured.toProgress()))
        }
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
                structureGeneration++
                scheduleFlush(debounceMs)
            }
            QueuePlayerEvent.ProgressChanged -> scheduleFlush(debounceMs)
            QueuePlayerEvent.Emptied -> {
                flushJob?.cancel()
                structureGeneration++
                savedGeneration = structureGeneration
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
        val generation = structureGeneration
        if (generation == savedGeneration) {
            writer.submit(QueueWrite.Progress(captured.toProgress()))
            return
        }
        val full = withContext(mappingDispatcher) { captured.toSavedQueue() }
        if (generation != structureGeneration) return
        full?.let { writer.submit(QueueWrite.Full(it)) }
        savedGeneration = generation
    }
}
