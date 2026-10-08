package tj.umar.navoplayer.core.domain.favorite

import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.model.FavoritesSummary
import tj.umar.navoplayer.core.domain.model.Track

fun List<Long>.toFavoriteTracks(library: Map<Long, Track>): FavoriteTracks {
    val tracks = mapNotNull(library::get)
    return FavoriteTracks(tracks = tracks, missingTrackCount = size - tracks.size)
}

fun List<Long>.toFavoritesSummary(library: Map<Long, Track>): FavoritesSummary {
    val tracks = mapNotNull(library::get)
    return FavoritesSummary(
        trackCount = tracks.size,
        durationMs = tracks.sumOf { it.durationMs.coerceAtLeast(0) },
    )
}
