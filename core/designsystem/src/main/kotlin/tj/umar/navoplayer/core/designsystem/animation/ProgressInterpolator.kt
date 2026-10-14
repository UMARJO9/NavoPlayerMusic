package tj.umar.navoplayer.core.designsystem.animation

import kotlin.math.abs

class ProgressInterpolator(
    private val maxExtrapolationMillis: Long = 1_000L,
    private val driftBlendMillis: Long = 400L,
    private val jumpMillis: Long = 240L,
    private val jumpThresholdMillis: Long = 1_500L,
) {
    private enum class Correction { None, Drift, Jump }

    private var initialized = false
    private var anchorPosition = 0f
    private var anchorTime = 0L
    private var duration = 0L
    private var playing = false
    private var correction = Correction.None
    private var correctionStart = 0L
    private var driftOffset = 0f
    private var jumpFrom = 0f
    private var lastShown = 0f

    fun onSample(positionMs: Long, durationMs: Long, playing: Boolean, nowMillis: Long) {
        val target = positionMs.toFloat()
        if (!initialized) {
            initialized = true
            correction = Correction.None
            lastShown = target
        } else {
            val shown = positionAt(nowMillis)
            val delta = target - shown
            if (durationMs != duration || abs(delta) > jumpThresholdMillis) {
                correction = Correction.Jump
                jumpFrom = shown
            } else {
                correction = Correction.Drift
                driftOffset = shown - target
            }
            correctionStart = nowMillis
            lastShown = if (correction == Correction.Jump) jumpFrom else shown
        }
        anchorPosition = target
        anchorTime = nowMillis
        duration = durationMs
        this.playing = playing
    }

    fun positionAt(nowMillis: Long): Float {
        if (!initialized) return 0f
        val elapsed = if (playing) (nowMillis - anchorTime).coerceIn(0L, maxExtrapolationMillis) else 0L
        val base = anchorPosition + elapsed
        val sinceCorrection = nowMillis - correctionStart
        val jumping = correction == Correction.Jump && sinceCorrection < jumpMillis
        if (correction == Correction.Jump && !jumping) {
            correction = Correction.None
            lastShown = base
        }
        val value = when {
            jumping -> jumpFrom + (base - jumpFrom) * easeOut(sinceCorrection.toFloat() / jumpMillis)
            correction == Correction.Drift && sinceCorrection < driftBlendMillis ->
                base + driftOffset * (1f - sinceCorrection.toFloat() / driftBlendMillis)
            else -> base
        }
        val upper = duration.coerceAtLeast(0L).toFloat()
        val guarded = if (playing && !jumping) maxOf(value, lastShown.coerceAtMost(upper)) else value
        val clamped = guarded.coerceIn(0f, upper)
        lastShown = clamped
        return clamped
    }

    fun fractionAt(nowMillis: Long): Float {
        val position = positionAt(nowMillis)
        return if (duration > 0L) position / duration else 0f
    }

    fun isSettling(nowMillis: Long): Boolean {
        val sinceCorrection = nowMillis - correctionStart
        return when (correction) {
            Correction.None -> false
            Correction.Drift -> sinceCorrection < driftBlendMillis
            Correction.Jump -> sinceCorrection < jumpMillis
        }
    }

    private fun easeOut(t: Float): Float {
        val remaining = 1f - t.coerceIn(0f, 1f)
        return 1f - remaining * remaining * remaining
    }
}
