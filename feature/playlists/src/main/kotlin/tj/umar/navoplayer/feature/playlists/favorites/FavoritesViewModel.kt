package tj.umar.navoplayer.feature.playlists.favorites

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RemovedFavorite
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.ObserveFavoriteTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.RestoreFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    private val observeFavoriteTracks: ObserveFavoriteTracksUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val playTracks: PlayTracksUseCase,
    private val shufflePlayTracks: ShufflePlayTracksUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    private val restoreFavorite: RestoreFavoriteUseCase,
) : MviViewModel<FavoritesState, FavoritesIntent, FavoritesEffect>(FavoritesState()) {

    private var favoritesJob: Job? = null
    private var playbackJob: Job? = null
    private var hasLoaded = false
    private var pendingUndo: PendingFavoriteUndo? = null
    private var undoTokenCounter = 0L

    override fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            FavoritesIntent.ScreenStopped -> stopObserving()
            FavoritesIntent.RetryLoad -> startObservingFavorites()
            FavoritesIntent.BackClicked -> sendEffect(FavoritesEffect.NavigateBack)
            FavoritesIntent.PlayClicked -> onPlayClicked()
            FavoritesIntent.ShuffleClicked -> onShuffleClicked()
            is FavoritesIntent.TrackClicked -> onTrackClicked(intent.trackId)
            is FavoritesIntent.TrackLongPressed -> onRemoveTrackRequested(intent.trackId)
            is FavoritesIntent.RemoveTrackRequested -> onRemoveTrackRequested(intent.trackId)
            is FavoritesIntent.UndoRemoveClicked -> onUndoRemoveClicked(intent.token)
            is FavoritesIntent.UndoRemoveDismissed -> onUndoRemoveDismissed(intent.token)
        }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (!hasPermission) {
            sendEffect(FavoritesEffect.NavigateToWelcome)
            return
        }
        startObservingFavorites()
        startObservingPlayback()
    }

    private fun onPlayClicked() {
        val state = currentState
        if (state.isFavoritesActive) {
            viewModelScope.launch { togglePlayPause() }
            return
        }
        val tracks = state.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { playTracks(tracks, 0, PlaybackSource.Favorites) }
    }

    private fun onShuffleClicked() {
        val tracks = currentState.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { shufflePlayTracks(tracks, PlaybackSource.Favorites) }
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        if (trackId == state.currentTrackId && state.isFavoritesActive) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val tracks = state.tracks
        val index = tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        viewModelScope.launch { playTracks(tracks, index, PlaybackSource.Favorites) }
    }

    private fun onRemoveTrackRequested(trackId: Long) {
        val state = currentState
        if (!state.canEdit || trackId in state.removingTrackIds) return
        val track = state.tracks.firstOrNull { it.id == trackId } ?: return
        val token = ++undoTokenCounter
        setRemoving(state.removingTrackIds + trackId)
        val removal = viewModelScope.async(start = CoroutineStart.LAZY) {
            when (val result = withContext(NonCancellable) { removeFavorite(trackId) }) {
                is NavoResult.Success -> result.data
                is NavoResult.Error -> {
                    onRemoveFailed(token, trackId)
                    null
                }
            }
        }
        pendingUndo = PendingFavoriteUndo(token, trackId, removal)
        sendEffect(FavoritesEffect.ShowRemoveUndo(token, track.title))
        removal.start()
    }

    private fun onRemoveFailed(token: Long, trackId: Long) {
        if (pendingUndo?.token == token) pendingUndo = null
        setRemoving(currentState.removingTrackIds - trackId)
        sendEffect(FavoritesEffect.RemoveFailed)
    }

    private fun onUndoRemoveClicked(token: Long) {
        val pending = pendingUndo?.takeIf { it.token == token } ?: return
        pendingUndo = null
        viewModelScope.launch {
            val removed = pending.removal.await() ?: return@launch
            when (withContext(NonCancellable) { restoreFavorite(removed) }) {
                is NavoResult.Success -> setRemoving(currentState.removingTrackIds - pending.trackId)
                is NavoResult.Error -> sendEffect(FavoritesEffect.RestoreFailed)
            }
        }
    }

    private fun onUndoRemoveDismissed(token: Long) {
        if (pendingUndo?.token == token) pendingUndo = null
    }

    private fun setRemoving(removing: Set<Long>) {
        setState { copy(removingTrackIds = removing).withVisibleTracks() }
    }

    private fun FavoritesState.withVisibleTracks(): FavoritesState {
        val visible = favorites?.tracks.orEmpty().filterNot { it.id in removingTrackIds }
        return copy(tracks = visible, totalMinutes = visible.totalDurationMinutes())
    }

    private fun startObservingFavorites() {
        if (favoritesJob?.isActive == true) return
        setState { copy(isLoading = !hasLoaded, loadFailed = false) }
        favoritesJob = observeFavoriteTracks()
            .onEach(::onFavorites)
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun onFavorites(favorites: FavoriteTracks) {
        hasLoaded = true
        val presentIds = favorites.tracks.mapTo(HashSet()) { it.id }
        setState {
            copy(
                isLoading = false,
                favorites = favorites,
                removingTrackIds = removingTrackIds.filterTo(HashSet()) { it in presentIds },
            ).withVisibleTracks()
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
        favoritesJob?.cancel()
        playbackJob?.cancel()
        favoritesJob = null
        playbackJob = null
    }
}

private class PendingFavoriteUndo(
    val token: Long,
    val trackId: Long,
    val removal: Deferred<RemovedFavorite?>,
)
