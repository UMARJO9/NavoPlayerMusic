package tj.umar.navoplayer.core.player.nowplaying

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.playback.NowPlayingMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SessionNowPlayingStore @Inject constructor() : NowPlayingMonitor {

    private val state = MutableStateFlow(NowPlaying.Idle)

    override fun observeNowPlaying(): Flow<NowPlaying> = state.asStateFlow()

    fun publish(value: NowPlaying) {
        state.value = value
    }
}
