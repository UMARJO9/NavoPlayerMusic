package tj.umar.navoplayer.core.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val trackIds: List<Long>,
)

data class PlaylistSummary(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val durationMs: Long,
)

data class PlaylistDetail(
    val id: Long,
    val name: String,
    val tracks: List<Track>,
    val missingTrackCount: Int,
)

class InvalidPlaylistNameException : IllegalArgumentException()
