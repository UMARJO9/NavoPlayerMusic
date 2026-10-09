package tj.umar.navoplayer.core.testing.playback

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.playback.NowPlayingMonitor

class FakeNowPlayingMonitor(initial: NowPlaying = NowPlaying.Idle) : NowPlayingMonitor {

    val nowPlaying = MutableStateFlow(initial)

    override fun observeNowPlaying(): Flow<NowPlaying> = nowPlaying
}
