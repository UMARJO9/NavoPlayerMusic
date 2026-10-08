package tj.umar.navoplayer.core.domain.favorite

import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.model.FavoritesSummary
import tj.umar.navoplayer.core.domain.settings.TrackCatalog

fun List<Long>.toFavoriteTracks(catalog: TrackCatalog): FavoriteTracks =
    FavoriteTracks(tracks = catalog.visibleTracks(this), missingTrackCount = catalog.missingCount(this))

fun List<Long>.toFavoritesSummary(catalog: TrackCatalog): FavoritesSummary {
    val tracks = catalog.visibleTracks(this)
    return FavoritesSummary(
        trackCount = tracks.size,
        durationMs = tracks.sumOf { it.durationMs.coerceAtLeast(0) },
    )
}
