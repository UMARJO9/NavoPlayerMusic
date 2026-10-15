package tj.umar.navoplayer.core.mediastore.artwork

internal val ARTWORK_BUCKETS_PX = intArrayOf(128, 256, 512, 1024)

internal fun artworkBucketPx(requestedPx: Int): Int =
    ARTWORK_BUCKETS_PX.firstOrNull { it >= requestedPx } ?: ARTWORK_BUCKETS_PX.last()

internal fun sampleSizeFor(width: Int, height: Int, targetPx: Int): Int {
    val shortSide = minOf(width, height)
    if (shortSide <= 0 || targetPx <= 0) return 1
    var sample = 1
    while (shortSide / (sample * 2) >= targetPx) sample *= 2
    return sample
}
