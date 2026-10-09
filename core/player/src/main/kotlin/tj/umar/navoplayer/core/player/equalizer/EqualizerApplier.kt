package tj.umar.navoplayer.core.player.equalizer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.model.EqualizerProfile
import tj.umar.navoplayer.core.domain.model.EqualizerStatus

internal class EqualizerApplier(
    private val status: Flow<EqualizerStatus>,
    private val sessionIds: Flow<Int>,
    private val factory: SoundEffectsFactory,
) {
    private var job: Job? = null
    private var effects: SoundEffects? = null
    private var effectsSession: Int? = null
    private var lastProfile: EqualizerProfile? = null

    fun start(scope: CoroutineScope) {
        job = scope.launch {
            combine(sessionIds, status) { sessionId, status -> sessionId to status }
                .collect { (sessionId, status) -> update(sessionId, status) }
        }
    }

    fun release() {
        job?.cancel()
        job = null
        releaseEffects()
    }

    private fun update(sessionId: Int, status: EqualizerStatus) {
        val ready = status as? EqualizerStatus.Ready
        val profile = ready?.profile?.takeIf { it.enabled }
        if (ready == null || profile == null) {
            releaseEffects()
            return
        }
        if (effects == null || effectsSession != sessionId) {
            releaseEffects()
            effects = createEffects(sessionId, ready.capabilities.bassBoostSupported) ?: return
            effectsSession = sessionId
        }
        lastProfile = profile
        applySafely(profile)
    }

    private fun createEffects(sessionId: Int, bassBoostSupported: Boolean): SoundEffects? = try {
        factory.create(sessionId, bassBoostSupported) { lastProfile?.let(::applySafely) }
    } catch (failure: RuntimeException) {
        null
    }

    private fun applySafely(profile: EqualizerProfile) {
        runCatching { effects?.apply(profile) }
    }

    private fun releaseEffects() {
        effects?.release()
        effects = null
        effectsSession = null
        lastProfile = null
    }
}
