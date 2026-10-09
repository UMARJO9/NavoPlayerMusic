package tj.umar.navoplayer.core.player.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import tj.umar.navoplayer.core.domain.model.EqualizerProfile

internal class AndroidSoundEffects(
    sessionId: Int,
    bassBoostSupported: Boolean,
    onControlRegained: () -> Unit,
) : SoundEffects {

    private val equalizer = Equalizer(0, sessionId)
    private val bassBoost: BassBoost? =
        if (bassBoostSupported) runCatching { BassBoost(0, sessionId) }.getOrNull() else null

    init {
        equalizer.setControlStatusListener { _, granted -> if (granted) onControlRegained() }
    }

    override fun apply(profile: EqualizerProfile) {
        runCatching {
            profile.bandLevelsMb.forEachIndexed { band, level ->
                equalizer.setBandLevel(band.toShort(), level.toShort())
            }
        }
        runCatching { equalizer.enabled = true }
        bassBoost?.let { effect ->
            runCatching {
                if (profile.bassBoostStrength > 0) {
                    effect.setStrength(profile.bassBoostStrength.toShort())
                    effect.enabled = true
                } else {
                    effect.enabled = false
                }
            }
        }
    }

    override fun release() {
        runCatching { equalizer.release() }
        runCatching { bassBoost?.release() }
    }
}
