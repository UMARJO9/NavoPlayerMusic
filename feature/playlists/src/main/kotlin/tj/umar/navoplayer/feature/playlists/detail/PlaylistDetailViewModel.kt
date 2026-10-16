package tj.umar.navoplayer.feature.playlists.detail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.common.result.onSuccess
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.model.RemovedPlaylistTrack
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.playlist.matchesOrder
import tj.umar.navoplayer.core.domain.playlist.movedTo
import tj.umar.navoplayer.core.domain.playlist.projected
import tj.umar.navoplayer.core.domain.usecase.DeletePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveTrackFromPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.RenamePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ReorderPlaylistTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RestorePlaylistTrackUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel

private const val REORDER_COMMIT_DELAY_MILLIS = 400L

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
    private val reorderPlaylistTracks: ReorderPlaylistTracksUseCase,
    private val restorePlaylistTrack: RestorePlaylistTrackUseCase,
) : MviViewModel<PlaylistDetailState, PlaylistDetailIntent, PlaylistDetailEffect>(PlaylistDetailState(playlistId)) {

    @AssistedFactory
    interface Factory {
        fun create(playlistId: Long): PlaylistDetailViewModel
    }

    private var playlistJob: Job? = null
    private var playbackJob: Job? = null
    private var hasLoaded = false
    private var reorderJob: Job? = null
    private var pendingOrder: List<Long>? = null
    private var pendingUndo: PendingUndo? = null
    private var undoTokenCounter = 0L

    override fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            PlaylistDetailIntent.ScreenStopped -> onScreenStopped()
            PlaylistDetailIntent.RetryLoad -> startObservingPlaylist()
            PlaylistDetailIntent.BackClicked -> onBackClicked()
            PlaylistDetailIntent.PlayClicked -> onPlayClicked()
            PlaylistDetailIntent.ShuffleClicked -> onShuffleClicked()
            is PlaylistDetailIntent.TrackClicked -> onTrackClicked(intent.trackId)
            is PlaylistDetailIntent.TrackLongPressed -> onRemoveTrackRequested(intent.trackId)
            is PlaylistDetailIntent.RemoveTrackRequested -> onRemoveTrackRequested(intent.trackId)
            is PlaylistDetailIntent.UndoRemoveClicked -> onUndoRemoveClicked(intent.token)
            is PlaylistDetailIntent.UndoRemoveDismissed -> onUndoRemoveDismissed(intent.token)
            is PlaylistDetailIntent.MoveTrack -> onMoveTrack(intent.trackId, intent.toIndex)
            PlaylistDetailIntent.RenameClicked -> onRenameClicked()
            is PlaylistDetailIntent.RenameConfirmed -> onRenameConfirmed(intent.name)
            PlaylistDetailIntent.DeleteClicked -> onDeleteClicked()
            PlaylistDetailIntent.DeleteConfirmed -> onDeleteConfirmed()
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
        val tracks = state.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { playTracks(tracks, 0, playlist.toPlaybackSource()) }
    }

    private fun onShuffleClicked() {
        val state = currentState
        val playlist = state.playlist ?: return
        val tracks = state.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { shufflePlayTracks(tracks, playlist.toPlaybackSource()) }
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        val playlist = state.playlist ?: return
        if (trackId == state.currentTrackId && state.isPlaylistActive) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val tracks = state.tracks
        val index = tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        viewModelScope.launch { playTracks(tracks, index, playlist.toPlaybackSource()) }
    }

    private fun onRemoveTrackRequested(trackId: Long) {
        val state = currentState
        if (!state.canEdit || trackId in state.removingTrackIds) return
        val track = state.tracks.firstOrNull { it.id == trackId } ?: return
        val token = ++undoTokenCounter
        setState { reprojected(removing = removingTrackIds + trackId) }
        val removal = viewModelScope.async(start = CoroutineStart.LAZY) {
            when (val result = withContext(NonCancellable) { removeTrackFromPlaylist(state.playlistId, trackId) }) {
                is NavoResult.Success -> result.data
                is NavoResult.Error -> {
                    onRemoveFailed(token, trackId)
                    null
                }
            }
        }
        pendingUndo = PendingUndo(token, trackId, removal)
        sendEffect(PlaylistDetailEffect.ShowRemoveUndo(token, track.title))
        removal.start()
    }

    private fun onRemoveFailed(token: Long, trackId: Long) {
        if (pendingUndo?.token == token) pendingUndo = null
        setState { reprojected(removing = removingTrackIds - trackId) }
        sendEffect(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RemoveFailed))
    }

    private fun onUndoRemoveClicked(token: Long) {
        val pending = pendingUndo?.takeIf { it.token == token } ?: return
        pendingUndo = null
        viewModelScope.launch {
            val removed = pending.removal.await() ?: return@launch
            when (withContext(NonCancellable) { restorePlaylistTrack(removed) }) {
                is NavoResult.Success -> setState { reprojected(removing = removingTrackIds - pending.trackId) }
                is NavoResult.Error -> sendEffect(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RestoreFailed))
            }
        }
    }

    private fun onUndoRemoveDismissed(token: Long) {
        if (pendingUndo?.token == token) pendingUndo = null
    }

    private fun PlaylistDetailState.reprojected(
        override: List<Long>? = orderOverride,
        removing: Set<Long> = removingTrackIds,
    ): PlaylistDetailState {
        val projected = playlist?.tracks.orEmpty().projected(override, removing)
        return copy(
            tracks = projected,
            orderOverride = override,
            removingTrackIds = removing,
            totalMinutes = projected.totalDurationMinutes(),
        )
    }

    private fun onMoveTrack(trackId: Long, toIndex: Int) {
        val state = currentState
        if (!state.canEdit) return
        val currentOrder = state.tracks.map { it.id }
        val newOrder = currentOrder.movedTo(trackId, toIndex)
        if (newOrder == currentOrder) return
        setState { reprojected(override = newOrder) }
        pendingOrder = newOrder
        reorderJob?.cancel()
        reorderJob = viewModelScope.launch {
            delay(REORDER_COMMIT_DELAY_MILLIS)
            commitPendingOrder()
        }
    }

    private suspend fun commitPendingOrder() {
        val order = pendingOrder ?: return
        pendingOrder = null
        val result = withContext(NonCancellable) { reorderPlaylistTracks(currentState.playlistId, order) }
        val saved = result is NavoResult.Success && result.data
        if (saved) {
            setState {
                val settled = orderOverride == order && pendingOrder == null && playlist?.tracks.orEmpty().matchesOrder(order)
                if (settled) reprojected(override = null) else this
            }
            return
        }
        if (currentState.orderOverride == order) {
            setState { reprojected(override = null) }
        }
        sendEffect(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.ReorderFailed))
    }

    private fun flushPendingOrder() {
        if (pendingOrder == null) return
        reorderJob?.cancel()
        reorderJob = viewModelScope.launch { commitPendingOrder() }
    }

    private fun onScreenStopped() {
        flushPendingOrder()
        stopObserving()
    }

    private fun onBackClicked() {
        flushPendingOrder()
        sendEffect(PlaylistDetailEffect.NavigateBack)
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
                val stored = playlist?.tracks.orEmpty()
                val storedIds = stored.mapTo(HashSet()) { it.id }
                copy(
                    isLoading = false,
                    playlist = playlist,
                    isMissing = playlist == null,
                    dialog = if (playlist == null) null else dialog,
                ).reprojected(
                    override = orderOverride?.takeUnless { pendingOrder == null && stored.matchesOrder(it) },
                    removing = removingTrackIds.filterTo(HashSet()) { it in storedIds },
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

private class PendingUndo(
    val token: Long,
    val trackId: Long,
    val removal: Deferred<RemovedPlaylistTrack?>,
)
