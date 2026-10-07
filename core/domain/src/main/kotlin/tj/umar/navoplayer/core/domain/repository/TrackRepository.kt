package tj.umar.navoplayer.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.Track

interface TrackRepository {
    fun observeTracks(): Flow<List<Track>>
}
