package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import tj.umar.navoplayer.core.common.dispatchers.IoDispatcher
import tj.umar.navoplayer.core.data.mapper.toTrack
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import javax.inject.Inject

internal const val CHANGE_DEBOUNCE_MS = 300L

internal class MediaStoreTrackRepository @Inject constructor(
    private val audioSource: AudioMediaSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackRepository {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeTracks(): Flow<List<Track>> =
        audioSource.observeChanges()
            .debounce(CHANGE_DEBOUNCE_MS)
            .onStart { emit(Unit) }
            .conflate()
            .mapLatest {
                audioSource.queryAudio()
                    .map { it.toTrack() }
                    .sortedWith(trackTitleComparator())
            }
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
}
