package tj.umar.navoplayer.feature.player.miniplayer

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class MiniPlayerViewModel @Inject constructor(
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val observePlaybackProgress: ObservePlaybackProgressUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
) : MviViewModel<MiniPlayerState, MiniPlayerIntent, MiniPlayerEffect>(MiniPlayerState()) {

    private var stateJob: Job? = null
    private var progressJob: Job? = null

    override fun onIntent(intent: MiniPlayerIntent) {
        when (intent) {
            MiniPlayerIntent.ScreenStarted -> startObserving()
            MiniPlayerIntent.ScreenStopped -> stopObserving()
            MiniPlayerIntent.PlayPauseClicked -> viewModelScope.launch { togglePlayPause() }
            MiniPlayerIntent.OpenClicked -> if (currentState.track != null) sendEffect(MiniPlayerEffect.OpenNowPlaying)
        }
    }

    private fun startObserving() {
        if (stateJob?.isActive != true) {
            stateJob = observePlaybackState()
                .onEach { playback ->
                    setState { copy(track = playback.currentTrack, isPlaying = playback.isPlaying) }
                }
                .catch { }
                .launchIn(viewModelScope)
        }
        if (progressJob?.isActive != true) {
            progressJob = observePlaybackProgress()
                .onEach { progress -> setState { copy(progress = progress.fraction) } }
                .catch { }
                .launchIn(viewModelScope)
        }
    }

    private fun stopObserving() {
        stateJob?.cancel()
        progressJob?.cancel()
        stateJob = null
        progressJob = null
    }
}
