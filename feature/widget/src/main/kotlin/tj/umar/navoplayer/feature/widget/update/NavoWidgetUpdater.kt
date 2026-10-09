package tj.umar.navoplayer.feature.widget.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.domain.usecase.ObserveNowPlayingUseCase
import tj.umar.navoplayer.feature.widget.toWidgetUiState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavoWidgetUpdater @Inject internal constructor(
    private val observeNowPlaying: ObserveNowPlayingUseCase,
    private val refresher: WidgetRefresher,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = observeNowPlaying()
            .map { it.toWidgetUiState() }
            .distinctUntilChanged()
            .onEach { refreshSafely() }
            .launchIn(scope)
    }

    private suspend fun refreshSafely() {
        try {
            refresher.refresh()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            return
        }
    }
}
