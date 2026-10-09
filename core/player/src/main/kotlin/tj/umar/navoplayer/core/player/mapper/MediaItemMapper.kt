package tj.umar.navoplayer.core.player.mapper

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import tj.umar.navoplayer.core.domain.model.QueueItem
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.Track

private const val EXTRA_ALBUM_ID = "navo.album_id"
private const val EXTRA_ARTIST_ID = "navo.artist_id"
private const val EXTRA_FOLDER_PATH = "navo.folder_path"
private const val EXTRA_DATE_ADDED = "navo.date_added"
private const val EXTRA_QUEUE_ITEM_ID = "navo.queue_item_id"

internal fun Track.toMediaItem(queueItemId: String): MediaItem {
    val uri = Uri.parse(contentUri)
    val extras = Bundle().apply {
        albumId?.let { putLong(EXTRA_ALBUM_ID, it) }
        artistId?.let { putLong(EXTRA_ARTIST_ID, it) }
        folderPath?.let { putString(EXTRA_FOLDER_PATH, it) }
        dateAddedMs?.let { putLong(EXTRA_DATE_ADDED, it) }
        putString(EXTRA_QUEUE_ITEM_ID, queueItemId)
    }
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setAlbumArtist(albumArtist)
        .setTrackNumber(trackNumber)
        .setDiscNumber(discNumber)
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
        folderPath = extras?.getString(EXTRA_FOLDER_PATH),
        discNumber = mediaMetadata.discNumber,
        albumArtist = mediaMetadata.albumArtist?.toString(),
        dateAddedMs = extras?.longOrNull(EXTRA_DATE_ADDED),
    )
}

internal fun MediaItem.queueItemId(): String? = mediaMetadata.extras?.getString(EXTRA_QUEUE_ITEM_ID)

internal fun MediaItem.toQueueItem(fallbackKey: Int): QueueItem =
    QueueItem(id = QueueItemId(queueItemId() ?: "$mediaId@$fallbackKey"), track = toTrack())

private fun Bundle.longOrNull(key: String): Long? = if (containsKey(key)) getLong(key) else null
