package tj.umar.navoplayer.core.domain.playback

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.NowPlaying

interface NowPlayingMonitor {
    fun observeNowPlaying(): Flow<NowPlaying>
}
