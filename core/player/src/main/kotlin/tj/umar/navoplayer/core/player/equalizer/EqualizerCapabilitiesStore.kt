package tj.umar.navoplayer.core.player.equalizer

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.dispatchers.MainDispatcher
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.model.EqualizerCapabilities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EqualizerCapabilitiesStore @Inject constructor(
    private val probe: EqualizerProbe,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) {
    private val state = MutableStateFlow<EqualizerAvailability>(EqualizerAvailability.Probing)
    private val mutex = Mutex()

    val availability: StateFlow<EqualizerAvailability> = state.asStateFlow()

    suspend fun ensureProbed() {
        mutex.withLock {
            if (state.value != EqualizerAvailability.Probing) return
            val capabilities = withContext(mainDispatcher) { probe.probe() }
            if (state.value != EqualizerAvailability.Probing) return
            state.value = capabilities?.let(EqualizerAvailability::Supported) ?: EqualizerAvailability.Unsupported
        }
    }

    fun publish(capabilities: EqualizerCapabilities) {
        state.value = EqualizerAvailability.Supported(capabilities)
    }

    fun markUnsupported() {
        state.value = EqualizerAvailability.Unsupported
    }
}
