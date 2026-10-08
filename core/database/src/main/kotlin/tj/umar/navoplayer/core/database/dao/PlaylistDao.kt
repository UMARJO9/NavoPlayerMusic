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

    @Transaction
    open suspend fun removeTrack(playlistId: Long, trackId: Long, updatedAt: Long): Boolean {
        val removed = deleteTrack(playlistId, trackId) > 0
        if (removed) touch(playlistId, updatedAt)
        return removed
    }
}
