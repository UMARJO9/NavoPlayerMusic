package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import android.util.LruCache

internal class ArtworkMemoryCache(maxBytes: Int = defaultMaxBytes()) {

    private val bitmaps = object : LruCache<ArtworkCacheKey, Bitmap>(maxBytes) {
        override fun sizeOf(key: ArtworkCacheKey, value: Bitmap): Int = value.allocationByteCount
    }

    private val misses = LruCache<ArtworkCacheId, Unit>(MISS_CAPACITY)

    fun getAtLeast(id: ArtworkCacheId, bucketPx: Int): Bitmap? =
        ARTWORK_BUCKETS_PX.asSequence()
            .filter { it >= bucketPx }
            .firstNotNullOfOrNull { bitmaps.get(ArtworkCacheKey(id, it)) }

    fun put(id: ArtworkCacheId, bucketPx: Int, bitmap: Bitmap) {
        misses.remove(id)
        bitmaps.put(ArtworkCacheKey(id, bucketPx), bitmap)
    }

    fun isMiss(id: ArtworkCacheId): Boolean = misses.get(id) != null

    fun putMiss(id: ArtworkCacheId) {
        misses.put(id, Unit)
    }

    fun clearMisses() {
        misses.evictAll()
    }

    private companion object {
        const val MISS_CAPACITY = 2048
        const val MAX_CACHE_BYTES = 48L * 1024 * 1024

        fun defaultMaxBytes(): Int = minOf(Runtime.getRuntime().maxMemory() / 8, MAX_CACHE_BYTES).toInt()
    }
}
