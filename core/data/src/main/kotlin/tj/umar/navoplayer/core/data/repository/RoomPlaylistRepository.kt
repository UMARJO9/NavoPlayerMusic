package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.data.mapper.toPlaylist
import tj.umar.navoplayer.core.database.dao.PlaylistDao
import tj.umar.navoplayer.core.domain.model.Playlist
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

internal class RoomPlaylistRepository @Inject constructor(
    private val dao: PlaylistDao,
    private val clock: NavoClock,
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> =
        dao.observePlaylists()
            .map { rows -> rows.map { it.toPlaylist() } }
            .distinctUntilChanged()

    override fun observePlaylist(id: Long): Flow<Playlist?> =
        dao.observePlaylist(id)
            .map { it?.toPlaylist() }
            .distinctUntilChanged()

    override suspend fun createPlaylist(name: String, trackIds: List<Long>): Long =
        dao.createWithTracks(name, trackIds, clock.nowMillis())

    override suspend fun renamePlaylist(id: Long, name: String): Boolean =
        dao.rename(id, name, clock.nowMillis()) > 0

    override suspend fun deletePlaylist(id: Long): Boolean = dao.delete(id) > 0

    override suspend fun addTracks(playlistId: Long, trackIds: List<Long>): Int =
        dao.appendTracks(playlistId, trackIds, clock.nowMillis())

    override suspend fun removeTrack(playlistId: Long, trackId: Long): Boolean =
        dao.removeTrack(playlistId, trackId, clock.nowMillis())
}
