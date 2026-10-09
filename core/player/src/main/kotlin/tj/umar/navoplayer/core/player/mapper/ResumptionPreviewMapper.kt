package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

internal fun MediaItem.toResumptionPreview(artworkPng: ByteArray?): MediaItem {
    val metadata = mediaMetadata.buildUpon()
        .setIsPlayable(true)
        .setIsBrowsable(false)
        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
    if (artworkPng != null) metadata.setArtworkData(artworkPng, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
    return buildUpon().setMediaMetadata(metadata.build()).build()
}
