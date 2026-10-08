package tj.umar.navoplayer.feature.playlists.detail

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
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.common.result.onSuccess
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.DeletePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveTrackFromPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.RenamePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = PlaylistDetailViewModel.Factory::class)
internal class PlaylistDetailViewModel @AssistedInject constructor(
    @Assisted playlistId: Long,
    private val observePlaylist: ObservePlaylistUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val playTracks: PlayTracksUseCase,
    private val shufflePlayTracks: ShufflePlayTracksUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
    private val renamePlaylist: RenamePlaylistUseCase,
    private val deletePlaylist: DeletePlaylistUseCase,
    private val removeTrackFromPlaylist: RemoveTrackFromPlaylistUseCase,
) : MviViewModel<PlaylistDetailState, PlaylistDetailIntent, PlaylistDetailEffect>(PlaylistDetailState(playlistId)) {

    @AssistedFactory
    interface Factory {
        fun create(playlistId: Long): PlaylistDetailViewModel
    }

    private var playlistJob: Job? = null
    private var playbackJob: Job? = null
    private var hasLoaded = false

    override fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            PlaylistDetailIntent.ScreenStopped -> stopObserving()
            PlaylistDetailIntent.RetryLoad -> startObservingPlaylist()
            PlaylistDetailIntent.BackClicked -> sendEffect(PlaylistDetailEffect.NavigateBack)
            PlaylistDetailIntent.PlayClicked -> onPlayClicked()
            PlaylistDetailIntent.ShuffleClicked -> onShuffleClicked()
            is PlaylistDetailIntent.TrackClicked -> onTrackClicked(intent.trackId)
            is PlaylistDetailIntent.TrackLongPressed -> onTrackLongPressed(intent.trackId)
            PlaylistDetailIntent.RenameClicked -> onRenameClicked()
            is PlaylistDetailIntent.RenameConfirmed -> onRenameConfirmed(intent.name)
            PlaylistDetailIntent.DeleteClicked -> onDeleteClicked()
            PlaylistDetailIntent.DeleteConfirmed -> onDeleteConfirmed()
            PlaylistDetailIntent.RemoveTrackConfirmed -> onRemoveTrackConfirmed()
            PlaylistDetailIntent.DialogDismissed -> setState { copy(dialog = null) }
        }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (!hasPermission) {
            sendEffect(PlaylistDetailEffect.NavigateToWelcome)
            return
        }
        startObservingPlaylist()
        startObservingPlayback()
    }

    private fun PlaylistDetail.toPlaybackSource(): PlaybackSource = PlaybackSource.Playlist(id, name)

    private fun onPlayClicked() {
        val state = currentState
        if (state.isPlaylistActive) {
            viewModelScope.launch { togglePlayPause() }
            return
        }
        val playlist = state.playlist ?: return
        if (playlist.tracks.isEmpty()) return
        viewModelScope.launch { playTracks(playlist.tracks, 0, playlist.toPlaybackSource()) }
    }

    private fun onShuffleClicked() {
        val playlist = currentState.playlist ?: return
        if (playlist.tracks.isEmpty()) return
        viewModelScope.launch { shufflePlayTracks(playlist.tracks, playlist.toPlaybackSource()) }
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        val playlist = state.playlist ?: return
        if (trackId == state.currentTrackId && state.isPlaylistActive) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val index = playlist.tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        viewModelScope.launch { playTracks(playlist.tracks, index, playlist.toPlaybackSource()) }
    }

    private fun onTrackLongPressed(trackId: Long) {
        val track = currentState.tracks.firstOrNull { it.id == trackId } ?: return
        setState { copy(dialog = PlaylistDetailDialog.ConfirmRemoveTrack(track.id, track.title)) }
    }

    private fun onRenameClicked() {
        val playlist = currentState.playlist ?: return
        setState { copy(dialog = PlaylistDetailDialog.Rename(playlist.name)) }
    }

    private fun onRenameConfirmed(name: String) {
        setState { copy(dialog = null) }
        viewModelScope.launch {
            renamePlaylist(currentState.playlistId, name)
                .onError { sendEffect(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RenameFailed)) }
        }
    }

    private fun onDeleteClicked() {
        if (currentState.playlist == null) return
        setState { copy(dialog = PlaylistDetailDialog.ConfirmDelete) }
    }

    private fun onDeleteConfirmed() {
        if (currentState.isDeleting) return
        setState { copy(dialog = null, isDeleting = true) }
        viewModelScope.launch {
            deletePlaylist(currentState.playlistId)
                .onSuccess { sendEffect(PlaylistDetailEffect.NavigateBack) }
                .onError {
                    setState { copy(isDeleting = false) }
                    sendEffect(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.DeleteFailed))
                }
        }
    }

    private fun onRemoveTrackConfirmed() {
        val dialog = currentState.dialog as? PlaylistDetailDialog.ConfirmRemoveTrack ?: return
        setState { copy(dialog = null) }
        viewModelScope.launch {
            removeTrackFromPlaylist(currentState.playlistId, dialog.trackId)
                .onError { sendEffect(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RemoveFailed)) }
        }
    }

    private fun startObservingPlaylist() {
        if (playlistJob?.isActive == true) return
        setState { copy(isLoading = !hasLoaded, loadFailed = false) }
        playlistJob = observePlaylist(currentState.playlistId)
            .onEach(::onPlaylist)
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun onPlaylist(playlist: PlaylistDetail?) {
        hasLoaded = true
        setState {
            if (playlist == null && isDeleting) {
                copy(isLoading = false)
            } else {
                copy(
                    isLoading = false,
                    playlist = playlist,
                    isMissing = playlist == null,
                    totalMinutes = playlist?.tracks?.totalDurationMinutes() ?: 0,
                    dialog = if (playlist == null) null else dialog,
                )
            }
        }
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
            .catch { setState { copy(currentTrackId = null, currentSource = null, isPlaying = false) } }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        playlistJob?.cancel()
        playbackJob?.cancel()
        playlistJob = null
        playbackJob = null
    }
}
