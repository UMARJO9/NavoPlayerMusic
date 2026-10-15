package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.common.dispatchers.IoDispatcher
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MediaStoreArtworkSource @Inject constructor(
    private val decoder: ArtworkDecoder,
    private val audioMediaSource: AudioMediaSource,
    @param:IoDispatcher ioDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : AudioArtworkSource {

    private val memoryCache = ArtworkMemoryCache()
    private val sharedLoads = SharedLoads<ArtworkCacheKey, Bitmap?>(
        scope = appScope,
        context = ioDispatcher.limitedParallelism(MAX_PARALLEL_DECODES),
    )
    private val observingChanges = AtomicBoolean(false)
    private val libraryGeneration = AtomicInteger(0)

    override fun cached(key: ArtworkKey, sizePx: Int): Bitmap? =
        memoryCache.getAtLeast(key.cacheId(), artworkBucketPx(sizePx))

    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap? {
        observeChangesOnce()
        val id = key.cacheId()
        val bucketPx = artworkBucketPx(sizePx)
        memoryCache.getAtLeast(id, bucketPx)?.let { return it }
        if (memoryCache.isMiss(id)) return null
        return sharedLoads.load(ArtworkCacheKey(id, bucketPx)) { decodeAndStore(key, id, bucketPx) }
    }

    private suspend fun decodeAndStore(key: ArtworkKey, id: ArtworkCacheId, bucketPx: Int): Bitmap? {
        val generation = libraryGeneration.get()
        val bitmap = try {
            decode(key, bucketPx)
        } catch (e: SecurityException) {
            return null
        }
        when {
            bitmap != null -> memoryCache.put(id, bucketPx, bitmap)
            generation == libraryGeneration.get() -> memoryCache.putMiss(id)
        }
        return bitmap
    }

    private suspend fun decode(key: ArtworkKey, bucketPx: Int): Bitmap? =
        key.albumId?.let { decoder.album(it, bucketPx) } ?: decoder.track(key.trackId, bucketPx)

    private fun observeChangesOnce() {
        if (!observingChanges.compareAndSet(false, true)) return
        appScope.launch {
            audioMediaSource.observeChanges().collect {
                libraryGeneration.incrementAndGet()
                memoryCache.clearMisses()
            }
        }
    }

    private companion object {
        const val MAX_PARALLEL_DECODES = 3
    }
}
