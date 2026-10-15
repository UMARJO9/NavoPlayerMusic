package tj.umar.navoplayer.core.mediastore.artwork

import tj.umar.navoplayer.core.domain.model.ArtworkKey

internal sealed interface ArtworkCacheId {
    data class Album(val id: Long) : ArtworkCacheId
    data class Track(val id: Long) : ArtworkCacheId
}

internal fun ArtworkKey.cacheId(): ArtworkCacheId =
    albumId?.let { ArtworkCacheId.Album(it) } ?: ArtworkCacheId.Track(trackId)

internal data class ArtworkCacheKey(
    val id: ArtworkCacheId,
    val bucketPx: Int,
)
