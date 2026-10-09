package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

internal fun MediaItem.toResumptionPreview(artworkPng: ByteArray?): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(mediaMetadata.title)
        .setArtist(mediaMetadata.artist)
        .setAlbumTitle(mediaMetadata.albumTitle)
        .setAlbumArtist(mediaMetadata.albumArtist)
        .setArtworkUri(mediaMetadata.artworkUri)
        .setIsPlayable(true)
        .setIsBrowsable(false)
        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
    if (artworkPng != null) metadata.setArtworkData(artworkPng, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(metadata.build())
        .build()
}
