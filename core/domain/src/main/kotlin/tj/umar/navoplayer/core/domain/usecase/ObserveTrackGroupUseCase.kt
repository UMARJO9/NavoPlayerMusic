package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.grouping.groupFor
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import javax.inject.Inject

class ObserveTrackGroupUseCase @Inject constructor(
    private val observeTracks: ObserveTracksUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(key: TrackGroupKey): Flow<TrackGroup?> = observeTracks()
        .map { tracks -> tracks.groupFor(key) }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
}
