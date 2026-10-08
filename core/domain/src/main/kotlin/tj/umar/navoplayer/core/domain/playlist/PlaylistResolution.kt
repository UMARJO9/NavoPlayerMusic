package tj.umar.navoplayer.core.domain.playlist

import tj.umar.navoplayer.core.domain.model.Playlist
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.settings.TrackCatalog

fun List<Track>.indexById(): Map<Long, Track> = associateBy { it.id }

fun Playlist.toSummary(catalog: TrackCatalog): PlaylistSummary {
    val tracks = catalog.visibleTracks(trackIds)
    return PlaylistSummary(
        id = id,
        name = name,
        trackCount = tracks.size,
        durationMs = tracks.sumOf { it.durationMs.coerceAtLeast(0) },
    )
}

fun Playlist.toDetail(catalog: TrackCatalog): PlaylistDetail = PlaylistDetail(
    id = id,
    name = name,
    tracks = catalog.visibleTracks(trackIds),
    missingTrackCount = catalog.missingCount(trackIds),
)
