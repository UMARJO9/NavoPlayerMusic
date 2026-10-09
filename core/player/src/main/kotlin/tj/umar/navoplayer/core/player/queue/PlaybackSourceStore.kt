package tj.umar.navoplayer.core.player.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PlaybackSourceStore @Inject constructor() {

    private val state = MutableStateFlow<PlaybackSource?>(null)

    val source: StateFlow<PlaybackSource?> = state.asStateFlow()

    fun set(value: PlaybackSource?) {
        state.value = value
    }
}
