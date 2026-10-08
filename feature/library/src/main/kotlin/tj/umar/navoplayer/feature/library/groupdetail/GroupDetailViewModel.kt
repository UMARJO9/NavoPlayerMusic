package tj.umar.navoplayer.feature.library.groupdetail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.toPlaybackSource
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTrackGroupUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = GroupDetailViewModel.Factory::class)
internal class GroupDetailViewModel @AssistedInject constructor(
    @Assisted key: TrackGroupKey,
    private val observeTrackGroup: ObserveTrackGroupUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val playTracks: PlayTracksUseCase,
    private val shufflePlayTracks: ShufflePlayTracksUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
) : MviViewModel<GroupDetailState, GroupDetailIntent, GroupDetailEffect>(GroupDetailState(key)) {

    @AssistedFactory
    interface Factory {
        fun create(key: TrackGroupKey): GroupDetailViewModel
    }

    private var groupJob: Job? = null
    private var playbackJob: Job? = null
    private var hasLoaded = false

    override fun onIntent(intent: GroupDetailIntent) {
        when (intent) {
            is GroupDetailIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            GroupDetailIntent.ScreenStopped -> stopObserving()
            GroupDetailIntent.RetryLoad -> startObservingGroup()
            GroupDetailIntent.BackClicked -> sendEffect(GroupDetailEffect.NavigateBack)
            is GroupDetailIntent.TrackClicked -> onTrackClicked(intent.trackId)
            GroupDetailIntent.ShuffleClicked -> onShuffleClicked()
            GroupDetailIntent.SortClicked -> Unit
        }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (!hasPermission) {
            sendEffect(GroupDetailEffect.NavigateToWelcome)
            return
        }
        startObservingGroup()
        startObservingPlayback()
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        val group = state.group ?: return
        if (trackId == state.currentTrackId) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val index = group.tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        viewModelScope.launch { playTracks(group.tracks, index, group.toPlaybackSource()) }
    }

    private fun onShuffleClicked() {
        val group = currentState.group ?: return
        if (group.tracks.isEmpty()) return
        viewModelScope.launch { shufflePlayTracks(group.tracks, group.toPlaybackSource()) }
    }

    private fun startObservingGroup() {
        if (groupJob?.isActive == true) return
        setState { copy(isLoading = !hasLoaded, loadFailed = false) }
        groupJob = observeTrackGroup(currentState.key)
            .onEach(::onGroup)
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun onGroup(group: TrackGroup?) {
        hasLoaded = true
        setState {
            copy(
                isLoading = false,
                group = group,
                isMissing = group == null,
                totalMinutes = group?.tracks?.totalDurationMinutes() ?: 0,
            )
        }
    }

    private fun startObservingPlayback() {
        if (playbackJob?.isActive == true) return
        playbackJob = observePlaybackState()
            .onEach { playback ->
                setState { copy(currentTrackId = playback.currentTrack?.id, isPlaying = playback.isPlaying) }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        groupJob?.cancel()
        playbackJob?.cancel()
        groupJob = null
        playbackJob = null
    }
}
