package tj.umar.navoplayer.core.domain.playlist

import tj.umar.navoplayer.core.domain.model.Playlist
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.Track

fun List<Track>.indexById(): Map<Long, Track> = associateBy { it.id }

fun Playlist.toSummary(library: Map<Long, Track>): PlaylistSummary {
    val tracks = trackIds.mapNotNull(library::get)
    return PlaylistSummary(
        id = id,
        name = name,
        trackCount = tracks.size,
        durationMs = tracks.sumOf { it.durationMs.coerceAtLeast(0) },
    )
}

fun Playlist.toDetail(library: Map<Long, Track>): PlaylistDetail {
    val tracks = trackIds.mapNotNull(library::get)
    return PlaylistDetail(
        id = id,
        name = name,
        tracks = tracks,
        missingTrackCount = trackIds.size - tracks.size,
    )
}
