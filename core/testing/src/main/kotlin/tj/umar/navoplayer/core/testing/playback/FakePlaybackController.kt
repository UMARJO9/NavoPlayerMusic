package tj.umar.navoplayer.core.testing.playback

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.playback.PlaybackController

sealed interface PlaybackCommand {
    data class Play(val queue: List<Track>, val startIndex: Int, val source: PlaybackSource) : PlaybackCommand
    data class PlayShuffled(val queue: List<Track>, val source: PlaybackSource) : PlaybackCommand
    data object TogglePlayPause : PlaybackCommand
    data object SkipToNext : PlaybackCommand
    data object SkipToPrevious : PlaybackCommand
    data class SeekTo(val positionMs: Long) : PlaybackCommand
    data class SetShuffle(val enabled: Boolean) : PlaybackCommand
    data class SetRepeat(val mode: RepeatMode) : PlaybackCommand
    data class SkipToQueueItem(val id: QueueItemId) : PlaybackCommand
    data class RemoveQueueItem(val id: QueueItemId) : PlaybackCommand
    data class MoveQueueItem(val id: QueueItemId, val toIndex: Int) : PlaybackCommand
    data class Enqueue(val tracks: List<Track>, val insertion: QueueInsertion) : PlaybackCommand
}

class FakePlaybackController : PlaybackController {

    val state = MutableSharedFlow<PlaybackState>(replay = 1)
    val progress = MutableSharedFlow<PlaybackProgress>(replay = 1)
    val queue = MutableSharedFlow<PlaybackQueue>(replay = 1)

    var rejectQueueCommands: Boolean = false

    private val recorded = mutableListOf<PlaybackCommand>()
    val commands: List<PlaybackCommand> get() = recorded.toList()

    var stateSubscribers: Int = 0
        private set
    var queueSubscribers: Int = 0
        private set
    var progressSubscribers: Int = 0
        private set

    override fun observePlaybackState(): Flow<PlaybackState> = state
        .onStart { stateSubscribers++ }
        .onCompletion { stateSubscribers-- }

    override fun observeProgress(): Flow<PlaybackProgress> = progress
        .onStart { progressSubscribers++ }
        .onCompletion { progressSubscribers-- }

    override suspend fun play(queue: List<Track>, startIndex: Int, source: PlaybackSource) {
        recorded += PlaybackCommand.Play(queue, startIndex, source)
    }

    override suspend fun playShuffled(queue: List<Track>, source: PlaybackSource) {
        recorded += PlaybackCommand.PlayShuffled(queue, source)
    }

    override suspend fun togglePlayPause() {
        recorded += PlaybackCommand.TogglePlayPause
    }

    override suspend fun skipToNext() {
        recorded += PlaybackCommand.SkipToNext
    }

    override suspend fun skipToPrevious() {
        recorded += PlaybackCommand.SkipToPrevious
    }

    override suspend fun seekTo(positionMs: Long) {
        recorded += PlaybackCommand.SeekTo(positionMs)
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        recorded += PlaybackCommand.SetShuffle(enabled)
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        recorded += PlaybackCommand.SetRepeat(mode)
    }

    override fun observeQueue(): Flow<PlaybackQueue> = queue
        .onStart { queueSubscribers++ }
        .onCompletion { queueSubscribers-- }

    override suspend fun skipToQueueItem(id: QueueItemId) {
        recorded += PlaybackCommand.SkipToQueueItem(id)
    }

    override suspend fun removeQueueItem(id: QueueItemId): Boolean {
        recorded += PlaybackCommand.RemoveQueueItem(id)
        return !rejectQueueCommands
    }

    override suspend fun moveQueueItem(id: QueueItemId, toIndex: Int): Boolean {
        recorded += PlaybackCommand.MoveQueueItem(id, toIndex)
        return !rejectQueueCommands
    }

    override suspend fun enqueue(tracks: List<Track>, insertion: QueueInsertion): Boolean {
        recorded += PlaybackCommand.Enqueue(tracks, insertion)
        return !rejectQueueCommands
    }
}
