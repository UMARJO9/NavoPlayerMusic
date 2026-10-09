package tj.umar.navoplayer.core.player.equalizer

import tj.umar.navoplayer.core.domain.model.EqualizerProfile

internal interface SoundEffects {
    fun apply(profile: EqualizerProfile)
    fun release()
}

internal fun interface SoundEffectsFactory {
    fun create(sessionId: Int, bassBoostSupported: Boolean, onControlRegained: () -> Unit): SoundEffects
}
