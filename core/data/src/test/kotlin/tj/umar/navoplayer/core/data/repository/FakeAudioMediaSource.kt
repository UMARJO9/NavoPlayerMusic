package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioRow

internal class FakeAudioMediaSource : AudioMediaSource {

    private val changes = MutableSharedFlow<Unit>()

    var rows: List<MediaStoreAudioRow> = emptyList()

    var queryCount: Int = 0
        private set

    override fun observeChanges(): Flow<Unit> = changes

    override suspend fun queryAudio(): List<MediaStoreAudioRow> {
        queryCount++
        return rows
    }

    suspend fun awaitObserver() {
        changes.subscriptionCount.first { it > 0 }
    }

    suspend fun notifyChange() {
        changes.emit(Unit)
    }
}

internal fun audioRow(id: Long, title: String) = MediaStoreAudioRow(
    id = id,
    title = title,
    displayName = "$title.mp3",
    artist = "Artist",
    artistId = 1,
    album = "Album",
    albumId = 1,
    durationMs = 1_000,
    track = 1,
    contentUri = "content://media/external/audio/media/$id",
)
