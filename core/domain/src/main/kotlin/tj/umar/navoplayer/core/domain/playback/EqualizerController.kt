package tj.umar.navoplayer.core.domain.playback

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability

interface EqualizerController {
    fun observeAvailability(): Flow<EqualizerAvailability>
}
