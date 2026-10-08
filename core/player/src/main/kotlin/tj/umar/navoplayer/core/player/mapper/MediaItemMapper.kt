package tj.umar.navoplayer.core.player.mapper

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import tj.umar.navoplayer.core.domain.model.Track

private const val EXTRA_ALBUM_ID = "navo.album_id"
private const val EXTRA_ARTIST_ID = "navo.artist_id"

internal fun Track.toMediaItem(): MediaItem {
    val uri = Uri.parse(contentUri)
    val extras = Bundle().apply {
        albumId?.let { putLong(EXTRA_ALBUM_ID, it) }
        artistId?.let { putLong(EXTRA_ARTIST_ID, it) }
    }
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setTrackNumber(trackNumber)
        .setDurationMs(durationMs)
        .setExtras(extras)
        .build()
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(metadata)
        .build()
}

internal fun MediaItem.toTrack(): Track {
    val extras = mediaMetadata.extras
    val uri = localConfiguration?.uri ?: requestMetadata.mediaUri
    return Track(
        id = mediaId.toLongOrNull() ?: 0L,
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString(),
        album = mediaMetadata.albumTitle?.toString(),
        albumId = extras?.longOrNull(EXTRA_ALBUM_ID),
        artistId = extras?.longOrNull(EXTRA_ARTIST_ID),
        durationMs = mediaMetadata.durationMs ?: 0L,
        trackNumber = mediaMetadata.trackNumber,
        contentUri = uri?.toString().orEmpty(),
    )
}

private fun Bundle.longOrNull(key: String): Long? = if (containsKey(key)) getLong(key) else null
