package tj.umar.navoplayer.core.testing.playback

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.playback.EqualizerController

class FakeEqualizerController(
    initial: EqualizerAvailability = EqualizerAvailability.Probing,
) : EqualizerController {

    val availability = MutableStateFlow(initial)

    var subscribers: Int = 0
        private set

    override fun observeAvailability(): Flow<EqualizerAvailability> = availability
        .onStart { subscribers++ }
        .onCompletion { subscribers-- }
}
