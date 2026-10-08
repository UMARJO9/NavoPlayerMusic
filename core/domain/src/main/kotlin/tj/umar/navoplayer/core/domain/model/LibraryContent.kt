package tj.umar.navoplayer.core.domain.model

data class LibraryContent(
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val folders: List<Folder>,
)
