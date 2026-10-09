package tj.umar.navoplayer.core.player.equalizer

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import tj.umar.navoplayer.core.domain.model.EqualizerBand
import tj.umar.navoplayer.core.domain.model.EqualizerCapabilities
import tj.umar.navoplayer.core.domain.model.EqualizerPreset
import javax.inject.Inject

private const val MILLIHERTZ_PER_HERTZ = 1000

internal fun interface EqualizerProbe {
    fun probe(): EqualizerCapabilities?
}

internal class AndroidEqualizerProbe @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : EqualizerProbe {

    override fun probe(): EqualizerCapabilities? {
        val available = runCatching {
            AudioEffect.queryEffects().orEmpty().any { it.type == AudioEffect.EFFECT_TYPE_EQUALIZER }
        }.getOrDefault(false)
        if (!available) return null
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return null
        val sessionId = audioManager.generateAudioSessionId()
        var equalizer: Equalizer? = null
        var bassBoost: BassBoost? = null
        return try {
            val probeEqualizer = Equalizer(0, sessionId).also { equalizer = it }
            val bassSupported = runCatching {
                BassBoost(0, sessionId).also { bassBoost = it }.strengthSupported
            }.getOrDefault(false)
            probeEqualizer.readCapabilities(bassSupported)
        } catch (failure: RuntimeException) {
            null
        } finally {
            runCatching { equalizer?.release() }
            runCatching { bassBoost?.release() }
        }
    }
}

internal fun Equalizer.readCapabilities(bassBoostSupported: Boolean): EqualizerCapabilities {
    val bandCount = numberOfBands.toInt()
    val range = bandLevelRange
    val bands = (0 until bandCount).map { band ->
        EqualizerBand(band, getCenterFreq(band.toShort()) / MILLIHERTZ_PER_HERTZ)
    }
    val presets = (0 until numberOfPresets.toInt()).map { preset ->
        val name = getPresetName(preset.toShort())
        usePreset(preset.toShort())
        EqualizerPreset(preset, name, (0 until bandCount).map { getBandLevel(it.toShort()).toInt() })
    }
    return EqualizerCapabilities(
        bands = bands,
        minLevelMb = range[0].toInt(),
        maxLevelMb = range[1].toInt(),
        presets = presets,
        bassBoostSupported = bassBoostSupported,
    )
}
