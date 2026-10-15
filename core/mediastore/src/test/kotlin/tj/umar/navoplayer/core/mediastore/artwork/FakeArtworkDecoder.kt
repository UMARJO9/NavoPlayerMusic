package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import kotlinx.coroutines.CompletableDeferred

internal class FakeArtworkDecoder : ArtworkDecoder {

    val albumBitmaps = mutableMapOf<Long, Bitmap>()
    val trackBitmaps = mutableMapOf<Long, Bitmap>()
    val calls = mutableListOf<String>()
    var failure: Throwable? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun album(albumId: Long, targetPx: Int): Bitmap? {
        calls += "album:$albumId"
        awaitGate()
        return albumBitmaps[albumId]
    }

    override suspend fun track(trackId: Long, targetPx: Int): Bitmap? {
        calls += "track:$trackId"
        awaitGate()
        return trackBitmaps[trackId]
    }

    private suspend fun awaitGate() {
        gate?.await()
        failure?.let { throw it }
    }
}
