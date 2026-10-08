package tj.umar.navoplayer.core.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val artistId: Long?,
    val durationMs: Long,
    val trackNumber: Int?,
    val contentUri: String,
    val folderPath: String?,
    val discNumber: Int?,
    val albumArtist: String?,
    val dateAddedMs: Long? = null,
)
