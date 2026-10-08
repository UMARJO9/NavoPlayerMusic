package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import tj.umar.navoplayer.core.domain.model.Playlist
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository

class FakePlaylistRepository(initial: List<Playlist> = emptyList()) : PlaylistRepository {

    private val playlists = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    private var clock = (initial.maxOfOrNull { it.updatedAtMs } ?: 0L) + 1

    var observeCalls: Int = 0
        private set

    var observeError: Throwable? = null

    var writeError: Throwable? = null

    val current: List<Playlist>
        get() = playlists.value

    override fun observePlaylists(): Flow<List<Playlist>> = flow {
        observeCalls++
        observeError?.let { throw it }
        emitAll(playlists.map { list -> list.sortedWith(compareByDescending<Playlist> { it.updatedAtMs }.thenByDescending { it.id }) })
    }

    override fun observePlaylist(id: Long): Flow<Playlist?> = flow {
        observeCalls++
        observeError?.let { throw it }
        emitAll(playlists.map { list -> list.firstOrNull { it.id == id } }.distinctUntilChanged())
    }

    override suspend fun createPlaylist(name: String, trackIds: List<Long>): Long {
        failIfNeeded()
        val id = nextId++
        val now = clock++
        playlists.update { it + Playlist(id, name, now, now, trackIds.distinct()) }
        return id
    }

    override suspend fun renamePlaylist(id: Long, name: String): Boolean {
        failIfNeeded()
        return modify(id) { it.copy(name = name) }
    }

    override suspend fun deletePlaylist(id: Long): Boolean {
        failIfNeeded()
        val exists = playlists.value.any { it.id == id }
        playlists.update { list -> list.filterNot { it.id == id } }
        return exists
    }

    override suspend fun addTracks(playlistId: Long, trackIds: List<Long>): Int {
        failIfNeeded()
        val playlist = playlists.value.firstOrNull { it.id == playlistId } ?: error("No playlist $playlistId")
        val newIds = trackIds.distinct().filterNot { it in playlist.trackIds }
        if (newIds.isNotEmpty()) modify(playlistId) { it.copy(trackIds = it.trackIds + newIds) }
        return newIds.size
    }

    override suspend fun removeTrack(playlistId: Long, trackId: Long): Boolean {
        failIfNeeded()
        val playlist = playlists.value.firstOrNull { it.id == playlistId } ?: return false
        if (trackId !in playlist.trackIds) return false
        return modify(playlistId) { it.copy(trackIds = it.trackIds - trackId) }
    }

    private fun modify(id: Long, transform: (Playlist) -> Playlist): Boolean {
        if (playlists.value.none { it.id == id }) return false
        val now = clock++
        playlists.update { list -> list.map { if (it.id == id) transform(it).copy(updatedAtMs = now) else it } }
        return true
    }

    private fun failIfNeeded() {
        writeError?.let { throw it }
    }
}
