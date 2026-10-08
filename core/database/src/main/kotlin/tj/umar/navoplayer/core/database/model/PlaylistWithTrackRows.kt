package tj.umar.navoplayer.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity

data class PlaylistWithTrackRows(
    @Embedded val playlist: PlaylistEntity,
    @Relation(parentColumn = "id", entityColumn = "playlist_id")
    val tracks: List<PlaylistTrackEntity>,
)
