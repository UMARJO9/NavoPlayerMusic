package tj.umar.navoplayer.feature.library.library

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val observeTracks: ObserveTracksUseCase,
) : MviViewModel<LibraryState, LibraryIntent, LibraryEffect>(LibraryState()) {

    private var tracksJob: Job? = null

    override fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.TabSelected -> selectTab(intent.tab)
            is LibraryIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            LibraryIntent.ScreenStopped -> stopObservingTracks()
            LibraryIntent.RetryLoadTracks -> startObservingTracks()
            LibraryIntent.SearchClicked,
            LibraryIntent.SettingsClicked,
            LibraryIntent.SortClicked,
            LibraryIntent.ShuffleClicked -> Unit
        }
    }

    private fun selectTab(tab: LibraryTab) {
        if (tab == currentState.selectedTab) return
        setState { copy(selectedTab = tab) }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (hasPermission) {
            startObservingTracks()
        } else {
            sendEffect(LibraryEffect.NavigateToWelcome)
        }
    }

    private fun startObservingTracks() {
        if (tracksJob?.isActive == true) return
        setState { copy(isLoadingTracks = true, tracksLoadFailed = false) }
        tracksJob = observeTracks()
            .onEach { tracks ->
                setState {
                    copy(tracks = tracks, totalMinutes = tracks.totalDurationMinutes(), isLoadingTracks = false)
                }
            }
            .catch { setState { copy(isLoadingTracks = false, tracksLoadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun stopObservingTracks() {
        tracksJob?.cancel()
        tracksJob = null
    }
}
