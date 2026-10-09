package tj.umar.navoplayer.core.player.artwork

import android.net.Uri

private const val TRACK_ARTWORK_SCHEME = "navo-artwork"
private const val TRACK_ARTWORK_HOST = "track"

internal fun trackArtworkUri(trackId: Long): Uri = Uri.Builder()
    .scheme(TRACK_ARTWORK_SCHEME)
    .authority(TRACK_ARTWORK_HOST)
    .appendPath(trackId.toString())
    .build()

internal fun Uri.artworkTrackId(): Long? {
    if (scheme != TRACK_ARTWORK_SCHEME || host != TRACK_ARTWORK_HOST) return null
    return pathSegments.singleOrNull()?.toLongOrNull()
}
