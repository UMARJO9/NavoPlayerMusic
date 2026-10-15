package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap

internal interface ArtworkDecoder {
    suspend fun album(albumId: Long, targetPx: Int): Bitmap?
    suspend fun track(trackId: Long, targetPx: Int): Bitmap?
}
