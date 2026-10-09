package tj.umar.navoplayer.core.player.equalizer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.playback.EqualizerController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultEqualizerController @Inject constructor(
    private val store: EqualizerCapabilitiesStore,
) : EqualizerController {

    override fun observeAvailability(): Flow<EqualizerAvailability> =
        store.availability.onStart { store.ensureProbed() }
}
