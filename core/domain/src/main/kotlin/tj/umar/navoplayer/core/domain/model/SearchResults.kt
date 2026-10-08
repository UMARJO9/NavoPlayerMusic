package tj.umar.navoplayer.core.domain.model

data class SearchResults(
    val query: String,
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
) {
    val isEmpty: Boolean
        get() = tracks.isEmpty() && albums.isEmpty() && artists.isEmpty()

    companion object {
        fun empty(query: String = ""): SearchResults = SearchResults(query, emptyList(), emptyList(), emptyList())
    }
}
