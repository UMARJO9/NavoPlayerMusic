package tj.umar.navoplayer.feature.widget.update

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.domain.usecase.ObserveNowPlayingUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveThemeModeUseCase
import tj.umar.navoplayer.feature.widget.toWidgetUiState
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NavoWidgetUpdater"

@Singleton
class NavoWidgetUpdater @Inject internal constructor(
    private val observeNowPlaying: ObserveNowPlayingUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val refresher: WidgetRefresher,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private var job: Job? = null
    private val systemNight = MutableStateFlow<Boolean?>(null)

    fun onSystemNightModeChanged(night: Boolean) {
        systemNight.value = night
    }

    fun start() {
        if (job?.isActive == true) return
        val themeModes = observeThemeMode().catch { emit(ThemeMode.Default) }
        job = combine(
            observeNowPlaying().map { it.toWidgetUiState() },
            themeModes,
            systemNight,
        ) { state, themeMode, night -> Triple(state, themeMode, night) }
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
            Log.w(TAG, "Widget refresh failed", failure)
        }
    }
}
