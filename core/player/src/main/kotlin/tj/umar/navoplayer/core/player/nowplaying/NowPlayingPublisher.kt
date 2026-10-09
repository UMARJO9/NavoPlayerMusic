package tj.umar.navoplayer.core.player.nowplaying

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tj.umar.navoplayer.core.domain.model.NowPlaying

internal class NowPlayingPublisher(
    private val changes: Flow<NowPlaying>,
    private val store: SessionNowPlayingStore,
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return
        job = changes.onEach(store::publish).launchIn(scope)
    }

    fun release() {
        job?.cancel()
        job = null
        store.publish(NowPlaying.Idle)
    }
}
