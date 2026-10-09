package tj.umar.navoplayer.feature.settings.equalizer

import java.util.Locale
import kotlin.math.abs

private const val HERTZ_PER_KILOHERTZ = 1000
private const val MILLIBELS_PER_DECIBEL = 100
private const val MINUS_SIGN = '−'

internal sealed interface FrequencyLabel {
    data class Hertz(val value: Int) : FrequencyLabel
    data class Kilohertz(val value: String) : FrequencyLabel
}

internal fun frequencyLabel(hz: Int): FrequencyLabel {
    if (hz < HERTZ_PER_KILOHERTZ) return FrequencyLabel.Hertz(hz)
    val tenths = (hz * 10 + HERTZ_PER_KILOHERTZ / 2) / HERTZ_PER_KILOHERTZ
    val value = if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
    return FrequencyLabel.Kilohertz(value)
}

internal fun formatDecibels(levelMb: Int, locale: Locale = Locale.ROOT): String {
    val magnitude = abs(levelMb)
    val number = if (magnitude % MILLIBELS_PER_DECIBEL == 0) {
        "${magnitude / MILLIBELS_PER_DECIBEL}"
    } else {
        String.format(locale, "%.1f", magnitude / MILLIBELS_PER_DECIBEL.toFloat())
    }
    return when {
        levelMb > 0 -> "+$number"
        levelMb < 0 -> "$MINUS_SIGN$number"
        else -> number
    }
}
