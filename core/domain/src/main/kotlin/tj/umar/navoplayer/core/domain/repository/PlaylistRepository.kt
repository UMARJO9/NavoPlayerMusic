package tj.umar.navoplayer.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.Playlist

interface PlaylistRepository {

    fun observePlaylists(): Flow<List<Playlist>>

    fun observePlaylist(id: Long): Flow<Playlist?>

    suspend fun createPlaylist(name: String, trackIds: List<Long>): Long

    suspend fun renamePlaylist(id: Long, name: String): Boolean

    suspend fun deletePlaylist(id: Long): Boolean

    suspend fun addTracks(playlistId: Long, trackIds: List<Long>): Int

    suspend fun removeTrack(playlistId: Long, trackId: Long): Boolean
}
