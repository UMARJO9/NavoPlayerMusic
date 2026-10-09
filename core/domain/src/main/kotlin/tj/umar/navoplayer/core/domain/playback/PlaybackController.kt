package tj.umar.navoplayer.core.domain.playback

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.Track

interface PlaybackController {
    fun observePlaybackState(): Flow<PlaybackState>
    fun observeProgress(): Flow<PlaybackProgress>
    fun observeQueue(): Flow<PlaybackQueue>
    suspend fun play(queue: List<Track>, startIndex: Int, source: PlaybackSource)
    suspend fun playShuffled(queue: List<Track>, source: PlaybackSource)
    suspend fun togglePlayPause()
    suspend fun skipToNext()
    suspend fun skipToPrevious()
    suspend fun seekTo(positionMs: Long)
    suspend fun setShuffleEnabled(enabled: Boolean)
    suspend fun setRepeatMode(mode: RepeatMode)
    suspend fun skipToQueueItem(id: QueueItemId)
    suspend fun removeQueueItem(id: QueueItemId): Boolean
    suspend fun moveQueueItem(id: QueueItemId, toIndex: Int): Boolean
    suspend fun enqueue(tracks: List<Track>, insertion: QueueInsertion): Boolean
}
