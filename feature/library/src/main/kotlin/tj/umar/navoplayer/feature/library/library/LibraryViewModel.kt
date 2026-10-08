package tj.umar.navoplayer.feature.library.library

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.common.result.onSuccess
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.CreatePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistsUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveLibraryUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val observeLibrary: ObserveLibraryUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val playTracks: PlayTracksUseCase,
    private val shufflePlayTracks: ShufflePlayTracksUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
    private val observePlaylists: ObservePlaylistsUseCase,
    private val createPlaylist: CreatePlaylistUseCase,
) : MviViewModel<LibraryState, LibraryIntent, LibraryEffect>(LibraryState()) {

    private var tracksJob: Job? = null
    private var playbackJob: Job? = null
    private var playlistsJob: Job? = null
    private var isCreatingPlaylist = false

    private var hasLoadedTracks = false

    override fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.TabSelected -> selectTab(intent.tab)
            is LibraryIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            LibraryIntent.ScreenStopped -> stopObserving()
            LibraryIntent.RetryLoadTracks -> startObservingTracks()
            is LibraryIntent.TrackClicked -> onTrackClicked(intent.trackId)
            is LibraryIntent.GroupClicked -> sendEffect(LibraryEffect.NavigateToGroup(intent.key))
            LibraryIntent.SearchClicked -> sendEffect(LibraryEffect.NavigateToSearch)
            LibraryIntent.ShuffleClicked -> onShuffleClicked()
            is LibraryIntent.PlaylistClicked -> sendEffect(LibraryEffect.NavigateToPlaylist(intent.playlistId))
            LibraryIntent.CreatePlaylistClicked -> setState { copy(isCreatePlaylistDialogVisible = true) }
            LibraryIntent.CreatePlaylistDismissed -> setState { copy(isCreatePlaylistDialogVisible = false) }
            is LibraryIntent.CreatePlaylistConfirmed -> onCreatePlaylistConfirmed(intent.name)
            LibraryIntent.RetryLoadPlaylists -> startObservingPlaylists()
            LibraryIntent.SettingsClicked,
            LibraryIntent.SortClicked -> Unit
        }
    }

    private fun selectTab(tab: LibraryTab) {
        if (tab == currentState.selectedTab) return
        setState { copy(selectedTab = tab) }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (hasPermission) {
            startObservingTracks()
            startObservingPlaylists()
            startObservingPlayback()
        } else {
            sendEffect(LibraryEffect.NavigateToWelcome)
        }
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        if (trackId == state.currentTrackId && state.currentSource == PlaybackSource.AllTracks) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val index = state.tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        val tracks = state.tracks
        viewModelScope.launch { playTracks(tracks, index, PlaybackSource.AllTracks) }
    }

    private fun onShuffleClicked() {
        val tracks = currentState.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { shufflePlayTracks(tracks, PlaybackSource.AllTracks) }
    }

    private fun startObservingTracks() {
        if (tracksJob?.isActive == true) return
        setState { copy(isLoadingTracks = !hasLoadedTracks, tracksLoadFailed = false) }
        tracksJob = observeLibrary()
            .onEach { content ->
                hasLoadedTracks = true
                setState {
                    copy(
                        tracks = content.tracks,
                        totalMinutes = content.tracks.totalDurationMinutes(),
                        albums = content.albums,
                        artists = content.artists,
                        folders = content.folders,
                        isLoadingTracks = false,
                    )
                }
            }
            .catch { setState { copy(isLoadingTracks = false, tracksLoadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun onCreatePlaylistConfirmed(name: String) {
        if (isCreatingPlaylist) return
        isCreatingPlaylist = true
        setState { copy(isCreatePlaylistDialogVisible = false) }
        viewModelScope.launch {
            createPlaylist(name)
                .onSuccess { sendEffect(LibraryEffect.NavigateToPlaylist(it)) }
                .onError { sendEffect(LibraryEffect.ShowCreatePlaylistFailed) }
            isCreatingPlaylist = false
        }
    }

    private fun startObservingPlaylists() {
        if (playlistsJob?.isActive == true) return
        setState { copy(isLoadingPlaylists = playlists.isEmpty(), playlistsLoadFailed = false) }
        playlistsJob = observePlaylists()
            .onEach { playlists -> setState { copy(playlists = playlists, isLoadingPlaylists = false) } }
            .catch { setState { copy(isLoadingPlaylists = false, playlistsLoadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun startObservingPlayback() {
        if (playbackJob?.isActive == true) return
        playbackJob = observePlaybackState()
            .onEach { playback ->
                setState {
                    copy(
                        currentTrackId = playback.currentTrack?.id,
                        currentSource = playback.source,
                        isPlaying = playback.isPlaying,
                    )
                }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        tracksJob?.cancel()
        playbackJob?.cancel()
        playlistsJob?.cancel()
        tracksJob = null
        playbackJob = null
        playlistsJob = null
    }
}
