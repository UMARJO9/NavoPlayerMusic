package tj.umar.navoplayer.core.domain.model

data class ArtworkKey(
    val trackId: Long,
    val albumId: Long?,
)

val Track.artworkKey: ArtworkKey
    get() = ArtworkKey(trackId = id, albumId = albumId.takeIf { album != null })

fun TrackGroup.artworkKey(): ArtworkKey? {
    if (this !is Album) return null
    val albumId = key.id ?: return null
    val first = tracks.firstOrNull() ?: return null
    return ArtworkKey(trackId = first.id, albumId = albumId)
}
