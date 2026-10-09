package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.repository.TrackRepository

class FakeTrackRepository : TrackRepository {

    private val tracks = MutableSharedFlow<List<Track>>(replay = 1)

    var observeCalls: Int = 0
        private set

    var error: Throwable? = null

    override fun observeTracks(): Flow<List<Track>> = flow {
        observeCalls++
        error?.let { throw it }
        emitAll(tracks)
    }

    var getTracksError: Throwable? = null

    override suspend fun getTracks(ids: List<Long>): List<Track> {
        getTracksError?.let { throw it }
        val byId = tracks.replayCache.lastOrNull().orEmpty().associateBy { it.id }
        return ids.mapNotNull(byId::get)
    }

    suspend fun emit(value: List<Track>) {
        tracks.emit(value)
    }
}
