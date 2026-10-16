package tj.umar.navoplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity
import tj.umar.navoplayer.core.database.model.PlaylistWithTrackRows

@Dao
abstract class PlaylistDao {

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY updated_at DESC, id DESC")
    abstract fun observePlaylists(): Flow<List<PlaylistWithTrackRows>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract fun observePlaylist(id: Long): Flow<PlaylistWithTrackRows?>

    @Insert
    abstract suspend fun insertPlaylist(entity: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTracks(rows: List<PlaylistTrackEntity>): List<Long>

    @Query("SELECT track_id FROM playlist_tracks WHERE playlist_id = :playlistId")
    abstract suspend fun trackIds(playlistId: Long): List<Long>

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_tracks WHERE playlist_id = :playlistId")
    abstract suspend fun maxPosition(playlistId: Long): Int

    @Query("UPDATE playlists SET name = :name, updated_at = :updatedAt WHERE id = :id")
    abstract suspend fun rename(id: Long, name: String, updatedAt: Long): Int

    @Query("UPDATE playlists SET updated_at = :updatedAt WHERE id = :id")
    abstract suspend fun touch(id: Long, updatedAt: Long): Int

    @Query("DELETE FROM playlists WHERE id = :id")
    abstract suspend fun delete(id: Long): Int

    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId AND track_id = :trackId")
    abstract suspend fun deleteTrack(playlistId: Long, trackId: Long): Int

    @Transaction
    open suspend fun appendTracks(playlistId: Long, trackIds: List<Long>, updatedAt: Long): Int {
        val existing = trackIds(playlistId).toSet()
        val newIds = trackIds.distinct().filterNot { it in existing }
        if (newIds.isEmpty()) return 0
        val start = maxPosition(playlistId) + 1
        insertTracks(newIds.mapIndexed { index, trackId -> PlaylistTrackEntity(playlistId, trackId, start + index) })
        touch(playlistId, updatedAt)
        return newIds.size
    }

    @Transaction
    open suspend fun createWithTracks(name: String, trackIds: List<Long>, now: Long): Long {
        val id = insertPlaylist(PlaylistEntity(name = name, createdAt = now, updatedAt = now))
        val rows = trackIds.distinct().mapIndexed { index, trackId -> PlaylistTrackEntity(id, trackId, index) }
        if (rows.isNotEmpty()) insertTracks(rows)
        return id
    }

    @Query("SELECT * FROM playlist_tracks WHERE playlist_id = :playlistId ORDER BY position, track_id")
    abstract suspend fun orderedTracks(playlistId: Long): List<PlaylistTrackEntity>

    @Query("SELECT track_id FROM playlist_tracks WHERE playlist_id = :playlistId ORDER BY position, track_id")
    abstract suspend fun orderedTrackIds(playlistId: Long): List<Long>

    @Query("UPDATE playlist_tracks SET position = :position WHERE playlist_id = :playlistId AND track_id = :trackId")
    abstract suspend fun updatePosition(playlistId: Long, trackId: Long, position: Int): Int

    @Query("SELECT EXISTS(SELECT 1 FROM playlists WHERE id = :playlistId)")
    abstract suspend fun exists(playlistId: Long): Boolean

    @Transaction
    open suspend fun removeTrackAt(playlistId: Long, trackId: Long, updatedAt: Long): Int? {
        val rows = orderedTracks(playlistId)
        val index = rows.indexOfFirst { it.trackId == trackId }
        if (index < 0) return null
        deleteTrack(playlistId, trackId)
        val remaining = rows.filterNot { it.trackId == trackId }
        writeOrder(playlistId, remaining, remaining.map { it.trackId })
        touch(playlistId, updatedAt)
        return index
    }

    @Transaction
    open suspend fun restoreTrack(playlistId: Long, trackId: Long, index: Int, updatedAt: Long): Boolean {
        if (!exists(playlistId)) return false
        val rows = orderedTracks(playlistId)
        if (rows.any { it.trackId == trackId }) return false
        val target = index.coerceIn(0, rows.size)
        val restored = PlaylistTrackEntity(playlistId, trackId, target)
        insertTracks(listOf(restored))
        val order = rows.map { it.trackId }.toMutableList().apply { add(target, trackId) }
        writeOrder(playlistId, rows + restored, order)
        touch(playlistId, updatedAt)
        return true
    }

    @Transaction
    open suspend fun reorderTracks(playlistId: Long, orderedSubset: List<Long>): Boolean {
        if (!exists(playlistId)) return false
        val rows = orderedTracks(playlistId)
        val present = rows.mapTo(HashSet()) { it.trackId }
        val subset = orderedSubset.distinct().filter { it in present }
        val subsetIds = subset.toSet()
        val order = rows.map { it.trackId }.toMutableList()
        val slots = order.indices.filter { order[it] in subsetIds }
        slots.zip(subset).forEach { (slot, trackId) -> order[slot] = trackId }
        writeOrder(playlistId, rows, order)
        return true
    }

    private suspend fun writeOrder(playlistId: Long, current: List<PlaylistTrackEntity>, order: List<Long>) {
        val positions = current.associate { it.trackId to it.position }
        order.forEachIndexed { rank, trackId ->
            if (positions[trackId] != rank) updatePosition(playlistId, trackId, rank)
        }
    }
}
