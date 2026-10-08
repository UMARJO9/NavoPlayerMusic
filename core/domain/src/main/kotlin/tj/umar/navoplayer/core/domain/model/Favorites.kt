package tj.umar.navoplayer.core.domain.model

data class FavoriteTracks(
    val tracks: List<Track>,
    val missingTrackCount: Int,
)

data class FavoritesSummary(
    val trackCount: Int,
    val durationMs: Long,
) {
    companion object {
        val Empty = FavoritesSummary(trackCount = 0, durationMs = 0)
    }
}

data class PlaylistsOverview(
    val favorites: FavoritesSummary,
    val playlists: List<PlaylistSummary>,
)
