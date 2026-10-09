package tj.umar.navoplayer.core.domain.model

data class LibraryContent(
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val folders: List<Folder>,
)

data class SortedLibrary(
    val content: LibraryContent,
    val trackSort: TrackSort,
    val groupSort: GroupSort,
)
