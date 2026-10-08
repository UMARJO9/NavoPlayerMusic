package tj.umar.navoplayer.core.mediastore.audio

data class MediaStoreAudioRow(
    val id: Long,
    val title: String?,
    val displayName: String?,
    val artist: String?,
    val artistId: Long?,
    val album: String?,
    val albumId: Long?,
    val durationMs: Long?,
    val track: Int?,
    val contentUri: String,
    val relativePath: String? = null,
    val dataPath: String? = null,
    val albumArtist: String? = null,
)
