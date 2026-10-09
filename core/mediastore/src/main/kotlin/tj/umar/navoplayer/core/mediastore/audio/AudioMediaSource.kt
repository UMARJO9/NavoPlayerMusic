package tj.umar.navoplayer.core.mediastore.audio

import kotlinx.coroutines.flow.Flow

interface AudioMediaSource {
    fun observeChanges(): Flow<Unit>
    suspend fun queryAudio(): List<MediaStoreAudioRow>
    suspend fun queryAudio(ids: Collection<Long>): List<MediaStoreAudioRow>
}
