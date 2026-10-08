package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.database.model.PlaylistWithTrackRows
import tj.umar.navoplayer.core.domain.model.Playlist

internal fun PlaylistWithTrackRows.toPlaylist(): Playlist = Playlist(
    id = playlist.id,
    name = playlist.name,
    createdAtMs = playlist.createdAt,
    updatedAtMs = playlist.updatedAt,
    trackIds = tracks
        .sortedWith(compareBy({ it.position }, { it.trackId }))
        .map { it.trackId },
)
