package tj.umar.navoplayer.core.domain.model

data class PlaybackProgress(
    val positionMs: Long,
    val durationMs: Long,
) {
    val fraction: Float
        get() = if (durationMs <= 0) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)

    companion object {
        val Zero = PlaybackProgress(positionMs = 0, durationMs = 0)
    }
}
