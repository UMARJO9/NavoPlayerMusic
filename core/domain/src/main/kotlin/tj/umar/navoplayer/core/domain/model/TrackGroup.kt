package tj.umar.navoplayer.core.domain.model

enum class TrackGroupType { Album, Artist, Folder }

data class TrackGroupKey(
    val type: TrackGroupType,
    val id: Long?,
    val name: String?,
) {
    val isUnknown: Boolean
        get() = id == null && name == null
}

sealed interface TrackGroup {
    val key: TrackGroupKey
    val tracks: List<Track>
}

data class Album(
    override val key: TrackGroupKey,
    val title: String?,
    val artist: String?,
    val hasVariousArtists: Boolean,
    override val tracks: List<Track>,
) : TrackGroup

data class Artist(
    override val key: TrackGroupKey,
    val name: String?,
    val albumCount: Int,
    override val tracks: List<Track>,
) : TrackGroup

data class Folder(
    override val key: TrackGroupKey,
    val name: String?,
    val path: String?,
    override val tracks: List<Track>,
) : TrackGroup
