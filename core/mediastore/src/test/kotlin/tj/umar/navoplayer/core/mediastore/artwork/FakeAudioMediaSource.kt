package tj.umar.navoplayer.core.mediastore.artwork

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioRow

class FakeAudioMediaSource : AudioMediaSource {

    val changes = MutableSharedFlow<Unit>()

    override fun observeChanges(): Flow<Unit> = changes

    override suspend fun queryAudio(): List<MediaStoreAudioRow> = emptyList()

    override suspend fun queryAudio(ids: Collection<Long>): List<MediaStoreAudioRow> = emptyList()
}
