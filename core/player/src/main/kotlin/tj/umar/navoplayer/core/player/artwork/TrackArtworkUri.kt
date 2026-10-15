package tj.umar.navoplayer.core.player.artwork

import android.net.Uri
import tj.umar.navoplayer.core.domain.model.ArtworkKey

private const val TRACK_ARTWORK_SCHEME = "navo-artwork"
private const val TRACK_ARTWORK_HOST = "track"
private const val ALBUM_PARAMETER = "album"

internal fun trackArtworkUri(key: ArtworkKey): Uri = Uri.Builder()
    .scheme(TRACK_ARTWORK_SCHEME)
    .authority(TRACK_ARTWORK_HOST)
    .appendPath(key.trackId.toString())
    .apply { key.albumId?.let { appendQueryParameter(ALBUM_PARAMETER, it.toString()) } }
    .build()

internal fun Uri.artworkKey(): ArtworkKey? {
    if (scheme != TRACK_ARTWORK_SCHEME || host != TRACK_ARTWORK_HOST) return null
    val trackId = pathSegments.singleOrNull()?.toLongOrNull() ?: return null
    val albumId = getQueryParameter(ALBUM_PARAMETER)?.toLongOrNull()
    return ArtworkKey(trackId = trackId, albumId = albumId)
}
