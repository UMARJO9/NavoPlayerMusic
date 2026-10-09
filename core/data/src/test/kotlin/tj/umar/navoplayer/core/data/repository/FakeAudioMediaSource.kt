package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioRow

internal class FakeAudioMediaSource : AudioMediaSource {

    private val changes = MutableSharedFlow<Unit>()

    var rows: List<MediaStoreAudioRow> = emptyList()

    var queryDelayMs: Long = 0

    var idQueryCount: Int = 0
        private set

    var queryCount: Int = 0
        private set

    override fun observeChanges(): Flow<Unit> = changes

    override suspend fun queryAudio(): List<MediaStoreAudioRow> {
        queryCount++
        val result = rows
        if (queryDelayMs > 0) delay(queryDelayMs)
        return result
    }

    override suspend fun queryAudio(ids: Collection<Long>): List<MediaStoreAudioRow> {
        idQueryCount++
        return rows.filter { it.id in ids }
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
